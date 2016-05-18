package com.hkc.notification;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;

import com.hkc.constant.CommonDefine;
import com.hkc.utils.DateUtils;
import com.hkc.utils.Logger;

public class ApkAdvUtils {

	private static final String TAG = ApkAdvUtils.class.getSimpleName();
	private static final String SAVE_PATH = "/apks";

	private ApkAdvUtils() {

	}

	public static Bitmap getImageBitmap(String imageUrl) {
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

	public static void getImageBitmap(String imageUrl,
			ImageDownloadCompleteListener listener) {
		new ImageGetTask(listener).execute(imageUrl);
	}

	public static String getSavePath(Context context, String subDir) {
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

	public static boolean isExternalStorageAvailable() {
		return android.os.Environment.MEDIA_MOUNTED
				.equals(android.os.Environment.getExternalStorageState());
	}

	public static File getFile(Context context, String downloadUrl) {
		String savePath = getSavePath(context, SAVE_PATH);
		if (TextUtils.isEmpty(savePath)) {
			return null;
		}
		Logger.i(TAG, "getFile :: savePath = " + savePath);
		File saveFile = new File(savePath, getFileName(downloadUrl));
		return saveFile;
	}

	public static String getFileName(String downloadUrl) {
		String[] lists = downloadUrl.split("/");
		String fileName;
		if (lists.length > 1) {
			fileName = lists[lists.length - 1];
		} else {
			fileName = null;
		}
		return fileName;
	}

	public static boolean isInstalled(Context context, String packageName) {
		List<ApplicationInfo> infos = context.getPackageManager()
				.getInstalledApplications(0);
		for (ApplicationInfo applicationInfo : infos) {
			if (packageName.equals(applicationInfo.packageName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取广告生效时间
	 * 
	 * @param todoTime
	 * @return
	 */
	public static long getTrigerTime(String todoTime) {
		if (TextUtils.isEmpty(todoTime)) {
			return -1;
		}
		Date todoDate = DateUtils.parseDate(todoTime, DateUtils.DF_HHMM);
		Calendar calendarNow = Calendar.getInstance();
		Calendar todo = (Calendar) calendarNow.clone();
		todo.set(Calendar.HOUR_OF_DAY, todoDate.getHours());
		todo.set(Calendar.MINUTE, todoDate.getMinutes());
		Logger.i(TAG,
				"getTrigerTime :: hour = " + todo.get(Calendar.HOUR_OF_DAY)
						+ " minutes = " + todo.get(Calendar.MINUTE));
		Logger.i(TAG, "getTrigerTime :: cur = " + System.currentTimeMillis()
				+ " todo = " + todo.getTimeInMillis());
		return todo.getTimeInMillis();
	}

	/**
	 * 判断时间是否过期
	 * 
	 * @param value
	 * @return
	 */
	public static boolean timePast(String value) {
		Date todoDate = DateUtils.parseDate(value, DateUtils.DF_HHMM);
		Calendar todoTime = (Calendar) Calendar.getInstance().clone();
		todoTime.set(Calendar.HOUR_OF_DAY, todoDate.getHours());
		todoTime.set(Calendar.MINUTE, todoDate.getMinutes());
		boolean isPast = Calendar.getInstance().after(todoTime);
		Logger.i(TAG, "timePast isPast = " + isPast);
		return isPast;
	}

	/**
	 * 判断今天是否取过广告数据
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isGetDataToday(Context context) {
		SharedPreferences sharedPref = context.getSharedPreferences(
				CommonDefine.APP_PREF_FILE, Context.MODE_PRIVATE);
		String date = sharedPref.getString(CommonDefine.PrefKey.LAST_GET_ADV_DATE,
				"");
		Date dateToday = DateUtils.parseDate(date, DateUtils.DF_YYYYMMDD);
		boolean isToday = DateUtils.isToday(dateToday);
		if (TextUtils.isEmpty(date) || !isToday) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 标记今天已经取过广告数据
	 * 
	 * @param context
	 */
	public static void setGetDataToday(Context context) {

		SharedPreferences sharedPref = context.getSharedPreferences(
				CommonDefine.APP_PREF_FILE, Context.MODE_PRIVATE);
		Editor editor = sharedPref.edit();
		String today = DateUtils.getTimeNow();
		editor.putString(CommonDefine.PrefKey.LAST_GET_ADV_DATE, today);
		editor.commit();
	}

	/**
	 * 由于targetSdkVersion低于17，只能通过反射获取
	 * 
	 * @return
	 */
	public static String getUserSerial(Context context) {
		Object userManager = context.getSystemService("user");
		if (userManager == null) {
			Logger.e(TAG, "userManager not exsit !!!");
			return null;
		}

		try {
			Method myUserHandleMethod = android.os.Process.class.getMethod(
					"myUserHandle", (Class<?>[]) null);
			Object myUserHandle = myUserHandleMethod.invoke(
					android.os.Process.class, (Object[]) null);

			Method getSerialNumberForUser = userManager.getClass().getMethod(
					"getSerialNumberForUser", myUserHandle.getClass());
			long userSerial = (Long) getSerialNumberForUser.invoke(userManager,
					myUserHandle);
			return String.valueOf(userSerial);
		} catch (NoSuchMethodException e) {
			Logger.e(TAG, "", e);
		} catch (IllegalArgumentException e) {
			Logger.e(TAG, "", e);
		} catch (IllegalAccessException e) {
			Logger.e(TAG, "", e);
		} catch (InvocationTargetException e) {
			Logger.e(TAG, "", e);
		}

		return null;
	}
}
