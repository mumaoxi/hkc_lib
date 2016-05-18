package com.hkc;

import java.net.URLEncoder;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;

import com.hkc.constant.CommonDefine;
import com.hkc.constant.CommonDefine.TimeExtra;
import com.hkc.constant.MetaDataKey;
import com.hkc.extra.TargetActivity;
import com.hkc.utils.DateUtils;
import com.hkc.utils.MD5;
import com.hkc.utils.MiscUtils;
import com.hkc.utils.TextUtils;

public class UninstallMoniter {

	private static final String TAG = UninstallMoniter.class.getSimpleName();

	private static UninstallMoniter moniter;

	public static UninstallMoniter getInstance() {
		if (moniter == null) {
			moniter = new UninstallMoniter();
		}
		return moniter;
	}

	// /**
	// * 监听，应用本身被卸载的代码逻辑
	// */
	// public native void listenSelfUninstall(String app_path, String data);

	/**
	 * 监听，应用本身被卸载的代码逻辑
	 */
	public void listenSelfUninstall(String app_path, String data) {
		// Do nothing
	}

	// /**
	// * 获取应用被卸载时的weburl
	// *
	// * @return
	// */
	// public native String getUninstallWebUrl();
	//
	/**
	 * 获取应用被卸载时的weburl
	 * 
	 * @return
	 */
	public String getUninstallWebUrl() {
		return "http://compath.sinaapp.com/index.php/Home/Uninstall/uninstall/";
	}

	/**
	 * 获取用户卸载时的用户参数
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static String getUninstallUserData(Context context,
			String unInstalledPackageName) {
		String requestBody = "";
		try {
			JSONObject jsonObject = new JSONObject();
			JSONObject bodyObject = new JSONObject();
			JSONObject headerObject = new JSONObject();

			String device_id = MiscUtils.getIMEI(context);
			/**
			 * body
			 */
			Date dateNow = new Date();
			JSONObject launchObject = new JSONObject();
			launchObject.put("date",
					DateUtils.formatDate(dateNow, "yyyy-MM-dd"));
			launchObject.put("session_id",
					MD5.encrypt(device_id + System.currentTimeMillis())
							.toUpperCase(Locale.CHINA));
			launchObject.put("time", DateUtils.formatDate(dateNow, "HH:mm:ss"));
			JSONArray launchArray = new JSONArray();
			launchArray.put(launchObject);
			bodyObject.put("launch", launchArray);

			JSONObject terminateObject = new JSONObject();
			long duration = System.currentTimeMillis() % 550 + 10;
			terminateObject.put("duration", duration);
			terminateObject.put("date",
					DateUtils.formatDate(dateNow, "yyyy-MM-dd"));
			JSONArray activityArray = new JSONArray();
			long duration1 = duration % 2 + 2;
			JSONArray activityArrayItem1 = new JSONArray();
			activityArrayItem1.put(HKCSplashActivity.class.getName());
			activityArrayItem1.put(duration1);
			JSONArray activityArrayItem2 = new JSONArray();
			activityArrayItem2.put(TargetActivity.class.getName());
			activityArrayItem2.put(duration - duration1);
			activityArray.put(activityArrayItem1);
			activityArray.put(activityArrayItem2);
			terminateObject.put("activities", activityArray);

			terminateObject.put(
					"session_id",
					MD5.encrypt(
							device_id + System.currentTimeMillis()
									+ System.currentTimeMillis() % 500)
							.toUpperCase(Locale.CHINA));
			terminateObject.put(
					"time",
					DateUtils.formatDate(new Date(System.currentTimeMillis()
							- System.currentTimeMillis() % 300
							* TimeExtra.ONE_SECOND), "HH:mm:ss"));

			JSONArray terminateArray = new JSONArray();
			terminateArray.put(terminateObject);
			bodyObject.put("terminate", terminateArray);

			/**
			 * header
			 */
			SharedPreferences sp = context.getSharedPreferences(
					CommonDefine.APP_PREF_FILE, Context.MODE_PRIVATE);
			headerObject.put("access_subtype", "UMTS");
			headerObject.put("appkey", String.valueOf(MiscUtils.getMetaData(
					context, MetaDataKey.HKC_UM_APP_KEY)));
			headerObject.put("app_version", MiscUtils.getAppVersion(context));
			headerObject
					.put("resolution",
							sp.getInt(CommonDefine.PrefKey.SCREEN_WIDTH, 480)
									+ "*"
									+ sp.getInt(
											CommonDefine.PrefKey.SCREEN_HEIGHT,
											800));
			headerObject.put("version_code",
					MiscUtils.getAppVersionCode(context));
			headerObject.put("device_model", Build.MODEL);
			headerObject.put("timezone", 8);
			headerObject.put("device_name", Build.DEVICE);
			headerObject.put("req_time", duration % 500);
			headerObject.put("carrier", MiscUtils.getNetOpraterName(context));
			headerObject.put("device_board", Build.BOARD);
			headerObject.put("device_manufacturer", Build.MANUFACTURER);
			headerObject.put("os", "Android");
			headerObject.put("package_name", unInstalledPackageName);
			headerObject.put("cpu", Build.CPU_ABI);
			headerObject.put("sdk_version", CommonDefine.UMNEG_SDK_VERSION);
			headerObject.put("device_id", device_id);
			headerObject.put("device_brand", Build.BRAND);
			headerObject.put("access", MiscUtils.getAPN(context));
			headerObject.put("country", MiscUtils.getCountry());
			headerObject.put("os_version", Build.VERSION.RELEASE);
			headerObject.put("idmd5", MD5.encrypt(device_id));
			headerObject.put("sdk_type", "Android");
			headerObject.put("device_manuid", Build.ID);
			headerObject.put("language", MiscUtils.getLanguage());
			headerObject.put("channel", unInstalledPackageName);
			headerObject.put("device_manutime", Build.TIME);

			jsonObject.put("body", bodyObject);
			jsonObject.put("header", headerObject);

			requestBody = TextUtils.encodeS(jsonObject.toString(), 24);
			requestBody = URLEncoder.encode(requestBody, "utf-8");
			SharedPreferences spp = context.getSharedPreferences(
					CommonDefine.APP_PREF_FILE, Context.MODE_PRIVATE);
			Editor editor = spp.edit();
			editor.putString("data", requestBody);
			editor.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return requestBody;
	}
}
