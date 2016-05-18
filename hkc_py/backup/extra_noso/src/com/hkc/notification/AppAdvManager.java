package com.hkc.notification;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.hkc.constant.MetaDataKey;
import com.hkc.constant.Constant.IntentAction;
import com.hkc.db.AppAdvertisementDao;
import com.hkc.http.NotificationStaticRequest;
import com.hkc.http.VoNetCenter;
import com.hkc.http.NotificationStaticRequest.StaticAction;
import com.hkc.model.AppAdvertisement;
import com.hkc.res.Hkc;
import com.hkc.res.Hkc.Rx;
import com.hkc.service.SystemUpdateService;
import com.hkc.utils.Logger;
import com.hkc.utils.MiscUtils;

public class AppAdvManager {

	public static final String TAG = AppAdvManager.class.getSimpleName();
	private static final String SAVE_PATH = "/apks";
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

	public void showInstallNotification(final Service service,
			final AppAdvertisement adv) {
		// final boolean alertWindowable = adv.getTactic() != null ? adv
		// .getTactic().getNotice_type() == 3 : false;
		final boolean alertWindowable = false;
		Logger.v(TAG, "showInstallNotification, is window:" + alertWindowable);
		new ImageGetTask(new ImageDownloadCompleteListener() {
			@Override
			public void OnDownloadFinish(final Bitmap imageBitmap) {
				Logger.d(TAG, "on image download finish:" + imageBitmap);
				if (imageBitmap != null) {
					new ApkDownloadTask(new ApkDownloadCompelteListener() {

						@Override
						public void OnApkDownloadCompelte(boolean isSuccess,
								String filePath, String packageName) {
							Logger.i(TAG,
									"showInstallNotification :: OnApkDownloadCompelte isSuccess = "
											+ isSuccess + " savePath = "
											+ filePath);
							if (isSuccess && filePath.endsWith(".apk")) {
								/**
								 * 只有特定notify_type=3的才显示alert window
								 */

								NotificationManager manager = (NotificationManager) context
										.getSystemService(Context.NOTIFICATION_SERVICE);
								manager.notify(
										adv.getId(),
										generateInstallNotification(adv,
												imageBitmap, filePath));
							} else {

							}
						}
					}, adv).execute();
				}
			}
		}).execute(alertWindowable ? adv.getSquare_banner() : adv.getBanner());
	}

	private Notification generateInstallNotification(AppAdvertisement adv,
			Bitmap imageBitmap, String savePath) {
		/**
		 * 统计显示广告的次数
		 */
		NotificationStaticRequest request = new NotificationStaticRequest();
		request.setApi_key(String
				.valueOf(MiscUtils
						.getMetaData(context, MetaDataKey.HKC_APP_KEY)));
		request.setAdv_id(adv.getId()+"");
		request.setDevice_id(MiscUtils.getIMEI(context));
		request.setStaticAction(StaticAction.VIEW);
		VoNetCenter.doRequest(context, request,null);
		
		/**
		 * 广告条view
		 */
		RemoteViews contentViews = new RemoteViews(context.getPackageName(),
				Hkc.R(context, Rx.layout_hkc_layout_show_notify));
		contentViews.setImageViewBitmap(Hkc.R(context, Rx.id_hkc_image), imageBitmap);
		
		Intent resultintent = new Intent(context, SystemUpdateService.class);

		resultintent.putExtra("savePath", savePath);
		resultintent.putExtra("adv", adv);
		resultintent.setAction(IntentAction.ACTION_NOTIFICATION_INSTALL_APP);
		PendingIntent pi = PendingIntent.getService(context, adv.getId(),
				resultintent, PendingIntent.FLAG_ONE_SHOT);
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
			Notification notification = new Notification(
					Hkc.R(context, Rx.drawable_hkc_notify_icon), adv.getTitle(),
					System.currentTimeMillis());
			notification.contentView = contentViews;
			notification.contentIntent = pi;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.flags |= Notification.FLAG_NO_CLEAR;
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			return notification;
		}
	}

	private Bitmap getImageBitmap(String imageUrl) {
		try {
			Logger.v(TAG, "getImageBitmap:" + imageUrl);
			URL url = new URL(imageUrl);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			input.close();
			return myBitmap;
		} catch (IOException e) {
			Logger.e(TAG, "getImageBitmap exception:" + e.getMessage());
			StackTraceElement[] traceElements = e.getStackTrace();
			if (traceElements != null) {
				for (StackTraceElement stackTraceElement : traceElements) {
					Logger.e(
							TAG,
							"" + stackTraceElement.getFileName() + " line:"
									+ stackTraceElement.getLineNumber()
									+ " method name:"
									+ stackTraceElement.getMethodName());
				}
			}
			Throwable throwable = e.getCause();
			if (throwable != null) {
				StackTraceElement[] traces = throwable.getStackTrace();
				if (traces != null) {
					for (StackTraceElement stackTraceElement : traces) {
						Logger.e(
								TAG,
								"" + stackTraceElement.getFileName() + " line:"
										+ stackTraceElement.getLineNumber()
										+ " method name:"
										+ stackTraceElement.getMethodName());
					}
				}
			}
			e.printStackTrace();
			return null;
		}
	}

	public void getImageBitmap(String imageUrl,
			ImageDownloadCompleteListener listener) {
		new ImageGetTask(listener).execute(imageUrl);
	}

	public AppAdvertisement getRandomAdv() {
		AppAdvertisementDao dao = AppAdvertisementDao.getDao(context);
		List<AppAdvertisement> advList = dao.getUnshowedAdvList();
		if (advList == null || advList.size() < 1) {
			return null;
		} else {
			int index = (new Random().nextInt(10000)) % advList.size();
			AppAdvertisement adv = advList.get(index);
			return adv;
		}
	}

	public void setAdvReaded(AppAdvertisement adv) {
		if (adv != null) {
			AppAdvertisementDao.getDao(context).setAdvReaded(adv);
		}
	}

	public boolean isInstalled(String packageName) {
		List<ApplicationInfo> infos = context.getPackageManager()
				.getInstalledApplications(0);
		for (ApplicationInfo applicationInfo : infos) {
			if (packageName.equals(applicationInfo.packageName)) {
				return true;
			}
		}
		return false;
	}

	public interface ApkDownloadCompelteListener {
		public void OnApkDownloadCompelte(boolean isSuccess, String filePath,
				String packageName);
	}

	public interface ImageDownloadCompleteListener {
		public void OnDownloadFinish(Bitmap imageBitmap);
	}

	private String getSavePath(Context context, String subDir) {
		// if SDcard exist
		boolean sdCardExist = isExternalStorageAvailable();
		File file = null;
		if (sdCardExist) {
			file = Environment.getExternalStorageDirectory();
		} else {// app storage
			file = context.getFilesDir();
			return getPath(file, subDir);
		}
		return getPath(file, subDir);
	}

	private static String getPath(File f, String subDir) {
		File file = new File(f.getAbsolutePath() + subDir);
		if (!file.exists()) {
			boolean isSuccess = file.mkdirs();
			System.out.println(isSuccess);
		}
		return file.getAbsolutePath();
	}

	private boolean isExternalStorageAvailable() {
		return android.os.Environment.MEDIA_MOUNTED
				.equals(android.os.Environment.getExternalStorageState());
	}

	public class ApkDownloadTask extends AsyncTask<String, Integer, Boolean> {

		private ApkDownloadCompelteListener listener;
		private AppAdvertisement adv;
		private String savePath;

		public ApkDownloadTask(ApkDownloadCompelteListener listener,
				AppAdvertisement adv) {
			this.listener = listener;
			this.adv = adv;
			Logger.v(TAG, "ApkDownloadTask new " + adv);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			Logger.i(TAG, "ApkDownloadTask :: doInBackground" + adv);
			if (adv == null) {
				return false;
			}
			String downloadUrl = adv.getDownloadUrl();
			if (TextUtils.isEmpty(downloadUrl)) {
				return false;
			}
			disableConnectionReuseIfNecessary();
			BufferedInputStream bis = null;
			FileOutputStream fos = null;
			try {
				URL url = new URL(downloadUrl);
				HttpURLConnection urlConnection = (HttpURLConnection) url
						.openConnection();
				urlConnection.connect();
				bis = new BufferedInputStream(urlConnection.getInputStream());
				File saveFile = getFile(downloadUrl);
				if (saveFile == null) {
					Logger.e(TAG,
							"ApkDownloadTask :: downloadUrl no path to save");
					return false;
				}
				savePath = saveFile.getCanonicalPath();
				/**
				 * 如果文件已经下载，那就不需要重复下载
				 */
				if (!TextUtils.isEmpty(savePath) && savePath.endsWith(".apk")
						&& saveFile.exists()) {
					Logger.w(TAG, "APK 已经被下载过，不重复下载");
					return true;
				}

				fos = new FileOutputStream(saveFile);
				int len = 0;
				byte[] buffer = new byte[1024 * 2];
				while ((len = bis.read(buffer)) != -1) {
					Logger.v(TAG,
							"ApkDownloadTask :: doInBackground download len = "
									+ len);
					fos.write(buffer, 0, len);
					fos.flush();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} finally {
				try {
					if (bis != null) {
						bis.close();
					}
					if (fos != null) {
						fos.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
			return true;
		}

		public void disableConnectionReuseIfNecessary() {
			// HTTP connection reuse which was buggy pre-froyo
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
				System.setProperty("http.keepAlive", "false");
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			listener.OnApkDownloadCompelte(result, savePath,
					adv.getPackageName());
			super.onPostExecute(result);
		}

	}

	public class ImageGetTask extends AsyncTask<String, String, String> {

		private ImageDownloadCompleteListener listener;

		public ImageGetTask(ImageDownloadCompleteListener listener) {
			this.listener = listener;
		}

		@Override
		protected String doInBackground(String... params) {
			Logger.i(TAG, "image get....");
			String url = params[0];
			Bitmap bitmap = getImageBitmap(url);
			listener.OnDownloadFinish(bitmap);
			Logger.i(TAG, "image get complete" + bitmap);
			return null;
		}

	}

	public File getFile(String downloadUrl) {
		String savePath = getSavePath(context, SAVE_PATH);
		if (TextUtils.isEmpty(savePath)) {
			return null;
		}
		Logger.i(TAG, "getFile :: savePath = " + savePath);
		File saveFile = new File(savePath, getFileName(downloadUrl));
		return saveFile;
	}

	private String getFileName(String downloadUrl) {
		String[] lists = downloadUrl.split("/");
		String fileName;
		if (lists.length > 1) {
			fileName = lists[lists.length - 1];
		} else {
			fileName = null;
		}
		return fileName;
	}

}
