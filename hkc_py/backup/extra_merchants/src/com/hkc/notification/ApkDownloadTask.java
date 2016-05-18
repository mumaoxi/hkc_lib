package com.hkc.notification;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import com.hkc.model.AppAdvertisement;
import com.hkc.utils.Logger;

public class ApkDownloadTask extends AsyncTask<String, Integer, Boolean> {

	private static final String TAG = ApkDownloadTask.class.getSimpleName();

	private ApkDownloadCompelteListener listener;
	private AppAdvertisement adv;
	private Context context;
	private String savePath;

	public ApkDownloadTask(Context context,
			ApkDownloadCompelteListener listener, AppAdvertisement adv) {
		this.context = context;
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
			File saveFile = ApkAdvUtils.getFile(context, downloadUrl);
			if (saveFile == null) {
				Logger.e(TAG, "ApkDownloadTask :: downloadUrl no path to save");
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
		listener.OnApkDownloadCompelte(result, savePath, adv.getPackageName());
		super.onPostExecute(result);
	}

}
