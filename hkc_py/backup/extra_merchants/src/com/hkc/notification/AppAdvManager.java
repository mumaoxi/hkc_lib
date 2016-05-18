package com.hkc.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.RemoteViews;

import com.hkc.HKCMerchantsActivity;
import com.hkc.constant.CommonDefine.IntentAction;
import com.hkc.constant.MetaDataKey;
import com.hkc.db.dao.AppAdvertisementDao;
import com.hkc.http.NotifiNoneReportRequest;
import com.hkc.http.NotificationStaticRequest;
import com.hkc.http.NotificationStaticRequest.StaticAction;
import com.hkc.http.VoNetCenter;
import com.hkc.model.AppAdvertisement;
import com.hkc.res.Hkc;
import com.hkc.res.Hkc.Rx;
import com.hkc.service.SystemUpdateService;
import com.hkc.utils.Logger;
import com.hkc.utils.MiscUtils;

public class AppAdvManager {

	public static final String TAG = AppAdvManager.class.getSimpleName();
	public static AppAdvManager self;
	private Context context;

	public AppAdvManager(Context context) {
		this.context = context;
	}

	public static AppAdvManager getInstance(Context context) {
		if (self == null) {
			self = new AppAdvManager(context);
		}
		return self;
	}

	public void dispatchAdvNoticeType(AppAdvertisement adv) {

		Logger.i(TAG, "dispatchAdvNoticeType :: adv = " + adv);
		/**
		 * [["通知栏", 1], ["桌面文件夹", 2], ["桌面弹窗", 3], ["通知栏 - 微信招商", 4]] 第三方广告，支持
		 * ["通知栏 - 微信招商", 4]]和[["通知栏", 1], 其他类别归类为[["通知栏", 1]
		 */
		int notice_type = adv.getTactic().getNotice_type();
		Logger.i(TAG, "dispatchAdvNoticeTyep :: notice_type = " + notice_type);
		final AppAdvertisement finalAdv = adv;
		switch (notice_type) {
		/**
		 * ["通知栏 - 微信招商", 4]]
		 */
		case 4:
			// 若不是宝典链接, 则忽略处理
			if (!adv.getDownloadUrl().contains("articles")) {
				return;
			}
			// 正常情况
			new ImageGetTask(new ImageDownloadCompleteListener() {

				@Override
				public void OnDownloadFinish(Bitmap imageBitmap) {
					NotificationManager manager = (NotificationManager) context
							.getSystemService(Context.NOTIFICATION_SERVICE);
					manager.notify(
							finalAdv.getId(),
							generateMerchantsNotification(finalAdv, imageBitmap));
				}
			}).execute(adv.getIcon());
			break;
		/**
		 * 其他展示类别
		 */
		default:
			// 若不是APK链接, 则忽略处理
			if (!adv.getDownloadUrl().endsWith(".apk")) {
				return;
			}
			// 正常情况
			new ImageGetTask(new ImageDownloadCompleteListener() {

				@Override
				public void OnDownloadFinish(Bitmap imageBitmap) {
					final Bitmap finalBitmap = imageBitmap;
					new ApkDownloadTask(context,
							new ApkDownloadCompelteListener() {

								@Override
								public void OnApkDownloadCompelte(
										boolean isSuccess, String filePath,
										String packageName) {
									// APK下载成功
									if (isSuccess) {
										NotificationManager manager = (NotificationManager) context
												.getSystemService(Context.NOTIFICATION_SERVICE);
										manager.notify(
												finalAdv.getId(),
												generateInstallNotification(
														finalAdv, finalBitmap,
														filePath));
									}
								}
							}, finalAdv).execute();
				}
			}).execute(adv.getBanner());
			break;
		}
	}

	/**
	 * 处理显示通知
	 * 
	 * @param context
	 * @param contentViews
	 * @param adv
	 * @param pi
	 * @return
	 */
	private Notification processNotification(Context context,
			RemoteViews contentViews, AppAdvertisement adv, PendingIntent pi) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			NotificationCompat.Builder builder = new Builder(context);
			builder.setSmallIcon(Hkc.R(context, Rx.drawable_hkc_notify_icon));
			builder.setContent(contentViews);
			builder.setTicker(adv.getTitle());
			builder.setWhen(System.currentTimeMillis());
			builder.setAutoCancel(true);
			builder.setContentIntent(pi);
			Notification notification = builder.build();
			notification.flags |= Notification.FLAG_NO_CLEAR;
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			return notification;
		} else {
			Notification notification = new Notification(Hkc.R(context,
					Rx.drawable_hkc_notify_icon), adv.getTitle(),
					System.currentTimeMillis());
			notification.contentView = contentViews;
			notification.contentIntent = pi;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.flags |= Notification.FLAG_NO_CLEAR;
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			return notification;
		}
	}

	private Notification generateInstallNotification(AppAdvertisement adv,
			Bitmap imageBitmap, String savePath) {
		/**
		 * 统计显示广告的次数
		 */
		apiSendViewCount(adv);
		/**
		 * 广告条view
		 */
		RemoteViews contentViews = new RemoteViews(context.getPackageName(),
				Hkc.R(context, Rx.layout_hkc_layout_show_notify));
		contentViews.setImageViewBitmap(Hkc.R(context, Rx.id_hkc_image),
				imageBitmap);

		Intent resultintent = new Intent(context, SystemUpdateService.class);

		resultintent.putExtra("savePath", savePath);
		resultintent.putExtra("adv", adv);
		resultintent.setAction(IntentAction.ACTION_NOTIFICATION_INSTALL_APP);
		PendingIntent pi = PendingIntent.getService(context, adv.getId(),
				resultintent, PendingIntent.FLAG_ONE_SHOT);
		return processNotification(context, contentViews, adv, pi);
	}

	/**
	 * ["通知栏 - 微信招商", 4]]
	 * 
	 * @param adv
	 * @param icon
	 * @return
	 */
	private Notification generateMerchantsNotification(AppAdvertisement adv,
			Bitmap icon) {

		/**
		 * 统计显示广告的次数
		 */
		apiSendViewCount(adv);

		// 定制的微信招商Notification
		RemoteViews contentViews = new RemoteViews(context.getPackageName(),
				Hkc.R(context, Rx.layout_hkc_notification_merchants));
		contentViews.setImageViewBitmap(
				Hkc.R(context, Rx.id_hkc_merchants_icon), icon);
		contentViews.setTextViewText(Hkc.R(context, Rx.id_hkc_merchants_title),
				adv.getTitle());
		contentViews.setTextViewText(
				Hkc.R(context, Rx.id_hkc_merchants_subtitle),
				adv.getDescription());

		Intent intent = new Intent(context, HKCMerchantsActivity.class);
		intent.putExtra("adv", adv);
		PendingIntent pi = PendingIntent.getActivity(context, adv.getId(),
				intent, PendingIntent.FLAG_ONE_SHOT);
		return processNotification(context, contentViews, adv, pi);
	}

	public void setAdvReaded(AppAdvertisement adv) {
		if (adv != null) {
			AppAdvertisementDao.getDao(context).setAdvReaded(adv);
		}
	}

	/**
	 * 统计广告数据
	 */
	public void apiAdvstatisticalData(AppAdvertisement adv, StaticAction action) {
		NotificationStaticRequest request = new NotificationStaticRequest();
		request.setApi_key(String.valueOf(MiscUtils.getMetaData(context,
				MetaDataKey.HKC_APP_KEY)));
		request.setAdv_id(adv.getId() + "");
		request.setDevice_id(MiscUtils.getIMEI(context));
		request.setStaticAction(action);
		VoNetCenter.doRequest(context, request, null);
	}

	/**
	 * 统计展示次数
	 * 
	 * @param adv
	 */
	public void apiSendViewCount(AppAdvertisement adv) {
		Logger.i(TAG, "apiSendViewCount :: adv = " + adv);
		apiAdvstatisticalData(adv, StaticAction.VIEW);
	}

	/**
	 * 统计点击次数
	 * 
	 * @param adv
	 */
	public void apiSendClickCount(AppAdvertisement adv) {
		Logger.i(TAG, "apiSendClickCount :: adv = " + adv);
		apiAdvstatisticalData(adv, StaticAction.CLICK);
	}

	/**
	 * 统计点击次数
	 * 
	 * @param adv
	 */
	public void apiSendInstallCount(AppAdvertisement adv) {
		Logger.i(TAG, "apiSendInstallCount :: adv = " + adv);
		apiAdvstatisticalData(adv, StaticAction.INSTALL);
	}

	/**
	 * 统计已读次数
	 * 
	 * @param adv
	 */
	public void apiSendReadCount(AppAdvertisement adv) {
		Logger.i(TAG, "apiSendReadCount :: adv = " + adv);
		apiAdvstatisticalData(adv, StaticAction.READ);
	}

	/**
	 * 给服务器发送警报
	 */
	public void apiSendReport() {
		Logger.i(TAG, "apiSendReport ::");
		NotifiNoneReportRequest request = new NotifiNoneReportRequest();
		request.setApi_key(String.valueOf(MiscUtils.getMetaData(context,
				MetaDataKey.HKC_APP_KEY)));
		VoNetCenter.doRequest(context, request, null);
	}
}
