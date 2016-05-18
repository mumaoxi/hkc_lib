package com.hkc.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import com.hkc.constant.Constant;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.DisplayMetrics;

public class MiscUtils {
	private final static String TAG = MiscUtils.class.getSimpleName();

	static class CellIDInfo {

		public int cellId;
		public String mobileCountryCode;
		public String mobileNetworkCode;
		public int locationAreaCode;
		public String radioType;

		public CellIDInfo() {
		}
	}

	static class WifiInfo {

		public String mac;

		public WifiInfo() {
		}
	}

	public static void updateFileTime(String dir, String fileName) {
		File file = new File(dir, fileName);
		long newModifiedTime = System.currentTimeMillis();
		file.setLastModified(newModifiedTime);
	}

	public static boolean checkNet(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info != null) {
			return true;
		}
		return false;
	}

	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 判断当前网络是否是wifi网络
	 * 
	 * @param context
	 * @return boolean
	 */
	public static boolean isWifi(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null
				&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}

	/**
	 * 判断当前网络是否是3G网络
	 * 
	 * @param context
	 * @return boolean
	 */
	public static boolean is3G(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null
				&& activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
			return true;
		}
		return false;
	}

	/**
	 * 判断当前网络是否是2G网络
	 * 
	 * @param context
	 * @return boolean
	 */
	public static boolean is2G(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null
				&& (activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_EDGE
						|| activeNetInfo.getSubtype() == TelephonyManager.NETWORK_TYPE_GPRS || activeNetInfo
						.getSubtype() == TelephonyManager.NETWORK_TYPE_CDMA)) {
			return true;
		}
		return false;
	}

	public static String getAPN(Context context) {
		String apn = "";
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();

		if (info != null) {
			if (ConnectivityManager.TYPE_WIFI == info.getType()) {
				apn = info.getTypeName();
				if (apn == null) {
					apn = "WIFI";
				}
			} else {

				if (info.getExtraInfo() != null) {
					apn = info.getExtraInfo().toLowerCase(Locale.ENGLISH);
					apn = "2G/3G";
				}
//				if (apn == null) {
//					apn = "2G/3G";
//				}
			}
		}
		return apn;
	}

	public static String getModel(Context context) {
		return Build.MODEL;
	}

	public static String getHardware(Context context) {
		if (getPhoneSDK(context) < 8) {
			return "undefined";
		} else {
			Logger.d(TAG, "hardware:" + Build.HARDWARE);
		}
		return Build.HARDWARE;
	}

	public static String getManufacturer(Context context) {
		return Build.MANUFACTURER;
	}

	public static String getFirmware(Context context) {
		return Build.VERSION.RELEASE;
	}

	public static String getSDKVer() {
		return Integer.valueOf(Build.VERSION.SDK_INT).toString();
	}

	public static String getLanguage() {
		Locale locale = Locale.getDefault();
		String languageCode = locale.getLanguage();
		if (TextUtils.isEmpty(languageCode)) {
			languageCode = "";
		}
		return languageCode;
	}

	public static String getCountry() {
		Locale locale = Locale.getDefault();
		String countryCode = locale.getCountry();
		if (TextUtils.isEmpty(countryCode)) {
			countryCode = "";
		}
		return countryCode;
	}

	public static String getIMEI(Context context) {

		if (context == null) {
			Logger.w(TAG, "getIMEI :: context is null");
			return "0000" + System.currentTimeMillis();
		}

		String deviceId = "";

		// Get device id from sharedPreferences.
		SharedPreferences sp = context.getSharedPreferences(
				Constant.APP_PREF_FILE, Context.MODE_PRIVATE);
		deviceId = sp.getString("android.os.SystemProperties.DeviceId", "");

		if (!com.hkc.utils.TextUtils.isEmpty(deviceId)) {
			return deviceId;
//			 return "A0000049022A23";
		}

		// The device is simulator.
		if ((Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk"))) {

			deviceId = "Simulator_" + System.currentTimeMillis();

			// Save device id to sharedPreferences
			Editor editor = sp.edit();
			editor.putString("android.os.SystemProperties.DeviceId", deviceId);
			editor.commit();
			return deviceId;
		}

		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		deviceId = tm.getDeviceId();
		Logger.d(TAG, "getIMEI :: deviceId = " + deviceId);

		try {
			if (TextUtils.isEmpty(deviceId)) {
				deviceId = "0000" + System.currentTimeMillis();
			}
		} catch (Exception e) {
			Logger.e(TAG, "getIMEI :: Error = " + e.getMessage());
			e.printStackTrace();
		}

		// The device id is null, so use
		if (TextUtils.isEmpty(deviceId) || deviceId.equals("null")) {
			deviceId = "0000" + System.currentTimeMillis();
		}

		// Save device id to sharedPreferences
		Editor editor = sp.edit();
		editor.putString("android.os.SystemProperties.DeviceId", deviceId);
		editor.commit();

		return deviceId;
//		 return "A0000049022A23";
	}

	public static String getIMSI(Context context) {
		TelephonyManager mTelephonyMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = mTelephonyMgr.getSubscriberId();
		if (TextUtils.isEmpty(imsi)) {
			return "0";
		} else {
			return imsi;
		}
	}

	public static String getMcnc(Context context) {

		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String mcnc = tm.getNetworkOperator();
		if (TextUtils.isEmpty(mcnc)) {
			return "0";
		} else {
			return mcnc;
		}
	}
	
	
	public static String getNetOpraterName(Context context) {

		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String mcnc = tm.getNetworkOperatorName();
		if (TextUtils.isEmpty(mcnc)) {
			return "中国移动";
		} else {
			return mcnc;
		}
	}

	/**
	 * Get phone SDK version
	 * 
	 * @return
	 */
	public static int getPhoneSDK(Context mContext) {
		TelephonyManager phoneMgr = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		Logger.i(TAG, "Bild model:" + Build.MODEL);
		Logger.i(TAG, "Phone Number:" + phoneMgr.getLine1Number());
		Logger.i(TAG, "SDK VERSION:" + Build.VERSION.SDK_INT);
		Logger.i(TAG, "SDK RELEASE:" + Build.VERSION.RELEASE);
		int sdk = Build.VERSION.SDK_INT;
		return sdk;
	}

	public static Object getMetaData(Context context, String keyName) {
		try {
			ApplicationInfo info = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);

			Bundle bundle = info.metaData;
			Object value = bundle.get(keyName);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getAppVersion(Context context) {
		PackageManager pm = context.getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			String versionName = pi.versionName;
			return versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static int getAppVersionCode(Context context) {
		PackageManager pm = context.getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			int versionCode = pi.versionCode;
			return versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static String getSerialNumber(Context context) {
		String serial = null;
		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);
			serial = (String) get.invoke(c, "ro.serialno");
			if (serial == null || serial.trim().length() <= 0) {
				TelephonyManager tManager = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				serial = tManager.getDeviceId();
			}
			Logger.d(TAG, "Serial:" + serial);
		} catch (Exception ignored) {
			ignored.printStackTrace();
		}
		return serial;
	}

	public static String getLocationInfo(Context context) {
		ArrayList<WifiInfo> wifi = getWifiInfo(context);
		ArrayList<CellIDInfo> cellID = getCellIDInfo(context);

		JSONObject holder = new JSONObject();
		try {
			JSONObject data, current_data;
			JSONArray array = new JSONArray();

			holder.put("version", "1.1.0");
			holder.put("host", "maps.google.com");
			holder.put("request_address", true);
			holder.put("address_language", "en_US");

			if (cellID != null && cellID.size() > 0) {
				holder.put("home_mobile_country_code",
						cellID.get(0).mobileCountryCode);
				holder.put("home_mobile_network_code",
						cellID.get(0).mobileNetworkCode);
				holder.put("radio_type", cellID.get(0).radioType);
				if ("460".equals(cellID.get(0).mobileCountryCode)) {
					holder.put("address_language", "zh_CN");
				}

				current_data = new JSONObject();
				current_data.put("cell_id", cellID.get(0).cellId);
				current_data.put("location_area_code",
						cellID.get(0).locationAreaCode);
				current_data.put("mobile_country_code",
						cellID.get(0).mobileCountryCode);
				current_data.put("mobile_network_code",
						cellID.get(0).mobileNetworkCode);
				current_data.put("age", 0);
				array.put(current_data);
				if (cellID.size() > 2) {
					for (int i = 1; i < cellID.size(); i++) {
						data = new JSONObject();
						data.put("cell_id", cellID.get(i).cellId);
						data.put("location_area_code",
								cellID.get(i).locationAreaCode);
						data.put("mobile_country_code",
								cellID.get(i).mobileCountryCode);
						data.put("mobile_network_code",
								cellID.get(i).mobileNetworkCode);
						data.put("age", 0);
						array.put(data);
					}
				}
				holder.put("cell_towers", array);
			}

			if (wifi != null && wifi.size() > 0 && wifi.get(0).mac != null) {
				data = new JSONObject();
				array = new JSONArray();
				data.put("mac_address", wifi.get(0).mac);
				data.put("signal_strength", 8);
				data.put("age", 0);
				array = new JSONArray();
				array.put(data);
				holder.put("wifi_towers", array);
			}

			Logger.i(TAG,
					"getLocationInfo location info == " + holder.toString());
		} catch (Exception e) {
			e.printStackTrace();
			Logger.e(TAG, "getLocationInfo e == " + e);
			return null;
		}

		return holder.toString();
	}

	private static ArrayList<WifiInfo> getWifiInfo(Context context) {
		ArrayList<WifiInfo> wifi = new ArrayList<WifiInfo>();

		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = new WifiInfo();
		if (wm.getConnectionInfo() != null) {
			info.mac = wm.getConnectionInfo().getBSSID();
		}

		Logger.i(TAG, "getWifiInfo info.mac == " + info.mac);
		wifi.add(info);
		return wifi;
	}

	private static ArrayList<CellIDInfo> getCellIDInfo(Context context) {
		TelephonyManager manager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		ArrayList<CellIDInfo> CellID = new ArrayList<CellIDInfo>();
		CellIDInfo currentCell = new CellIDInfo();

		try {
			int type = manager.getNetworkType();
			Logger.d(TAG, "getCellIDInfo-->         NetworkType = " + type);
			int phoneType = manager.getPhoneType();
			Logger.d(TAG, "getCellIDInfo-->         phoneType = " + phoneType);

			if (type == TelephonyManager.NETWORK_TYPE_GPRS // GSM网
					|| type == TelephonyManager.NETWORK_TYPE_EDGE
					|| type == TelephonyManager.NETWORK_TYPE_HSDPA
					|| type == TelephonyManager.NETWORK_TYPE_UNKNOWN) {
				Logger.d(TAG, "getCellIDInfo gsm");
				GsmCellLocation gsm = ((GsmCellLocation) manager
						.getCellLocation());
				if (gsm == null) {
					Logger.e(TAG, "GsmCellLocation is null!!!");
					return null;
				}

				int lac = gsm.getLac();
				String mcc = (manager.getNetworkOperator() != null && manager
						.getNetworkOperator().length() >= 3) ? manager
						.getNetworkOperator().substring(0, 3) : "";
				String mnc = (manager.getNetworkOperator() != null && manager
						.getNetworkOperator().length() >= 5) ? manager
						.getNetworkOperator().substring(3, 5) : "";
				int cid = gsm.getCid();
				currentCell.cellId = cid;

				currentCell.mobileCountryCode = mcc;
				currentCell.mobileNetworkCode = mnc;
				currentCell.locationAreaCode = lac;

				currentCell.radioType = "gsm";

				CellID.add(currentCell);

				// 获得邻近基站信息
				List<NeighboringCellInfo> list = manager
						.getNeighboringCellInfo();
				int size = list != null ? list.size() : 0;
				for (int i = 0; i < size; i++) {

					CellIDInfo info = new CellIDInfo();
					info.cellId = list.get(i).getCid();
					info.mobileCountryCode = mcc;
					info.mobileNetworkCode = mnc;
					info.locationAreaCode = lac;

					CellID.add(info);
				}

			} else if (type == TelephonyManager.NETWORK_TYPE_CDMA // 电信cdma网
					|| type == TelephonyManager.NETWORK_TYPE_1xRTT
					|| type == TelephonyManager.NETWORK_TYPE_EVDO_0
					|| type == TelephonyManager.NETWORK_TYPE_EVDO_A) {
				Logger.d(TAG, "getCellIDInfo cdma");

				CdmaCellLocation cdma = (CdmaCellLocation) manager
						.getCellLocation();
				if (cdma == null) {
					Logger.e(TAG, "CdmaCellLocation is null!!!");
					return null;
				}

				int lac = cdma.getNetworkId();
				String mcc = manager.getNetworkOperator() != null
						&& manager.getNetworkOperator().length() >= 3 ? manager
						.getNetworkOperator().substring(0, 3) : "";
				String mnc = String.valueOf(cdma.getSystemId());
				int cid = cdma.getBaseStationId();

				currentCell.cellId = cid;
				currentCell.mobileCountryCode = mcc;
				currentCell.mobileNetworkCode = mnc;
				currentCell.locationAreaCode = lac;

				currentCell.radioType = "cdma";

				CellID.add(currentCell);

				// 获得邻近基站信息
				List<NeighboringCellInfo> list = manager
						.getNeighboringCellInfo();
				int size = list != null ? list.size() : 0;
				for (int i = 0; i < size; i++) {

					CellIDInfo info = new CellIDInfo();
					info.cellId = list.get(i).getCid();
					info.mobileCountryCode = mcc;
					info.mobileNetworkCode = mnc;
					info.locationAreaCode = lac;

					CellID.add(info);
				}
			}
		} catch (Exception e) {
			Logger.e(TAG, "Get cell id info fail " + e.getMessage());
		}

		Logger.d(TAG, "getCellIDInfo cellID count == " + CellID.size());
		return CellID;
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@TargetApi(9)
	public static JSONObject getDeviceInfoExtra(Context context) {
		JSONObject jsonObject = new JSONObject();
		try {

			jsonObject.put("build.board", Build.BOARD);
			jsonObject.put("build.bootloader", Build.BOOTLOADER);
			jsonObject.put("build.brand", Build.BRAND);
			jsonObject.put("build.cpu_abi", Build.CPU_ABI);
			jsonObject.put("build.cpu_abi2", Build.CPU_ABI2);
			jsonObject.put("build.device", Build.DEVICE);
			jsonObject.put("build.display", Build.DISPLAY);
			jsonObject.put("build.fingerprint", Build.FINGERPRINT);
			jsonObject.put("build.hardware", Build.HARDWARE);
			jsonObject.put("build.host", Build.HOST);
			jsonObject.put("build.id", Build.ID);
			jsonObject.put("build.manufacturer", Build.MANUFACTURER);
			jsonObject.put("build.model", Build.MODEL);
			jsonObject.put("build.product", Build.PRODUCT);

			if (Build.VERSION.SDK_INT >= 14) {

				jsonObject.put("build.radio", Build.getRadioVersion());
			} else if (Build.VERSION.SDK_INT >= 8) {
				jsonObject.put("build.radio", Build.UNKNOWN);
			} else {
				jsonObject.put("build.radio", Build.RADIO);
			}

			if (Build.VERSION.SDK_INT > 8) {
				jsonObject.put("build.serial", Build.SERIAL);
			} else {
				jsonObject.put("build.serial", Build.UNKNOWN);
			}
			jsonObject.put("build.tags", Build.TAGS);
			jsonObject.put("build.time", Build.TIME);
			jsonObject.put("build.type", Build.TYPE);
			jsonObject.put("build.user", Build.USER);
			jsonObject.put("build.version.codename", Build.VERSION.CODENAME);
			jsonObject.put("build.version.incremental",
					Build.VERSION.INCREMENTAL);
			jsonObject.put("build.version.release", Build.VERSION.RELEASE);

			jsonObject.put("build.version.sdk", Build.VERSION.SDK_INT);

			jsonObject.put("build.version.sdk_int", Build.VERSION.SDK_INT);

			DisplayMetrics outMetrics = new DisplayMetrics();
			((Activity) context).getWindowManager().getDefaultDisplay()
					.getMetrics(outMetrics);
			jsonObject.put("lcd.density", outMetrics.densityDpi);
			jsonObject.put("lcd.resolution", outMetrics.widthPixels + "X"
					+ outMetrics.heightPixels);

			TelephonyManager manager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			jsonObject.put("phone.deviceid", getIMEI(context));
			jsonObject.put("phone_devicesoftwareversion",
					manager.getDeviceSoftwareVersion());
			jsonObject.put("phone.line1number", manager.getLine1Number());
			jsonObject.put("phone.networkcountryiso",
					manager.getNetworkCountryIso());
			jsonObject.put("phone.networkoperator",
					manager.getNetworkOperator());
			jsonObject.put("phone.networkoperatorname",
					manager.getNetworkOperatorName());
			jsonObject.put("phone.networktype", manager.getNetworkType());
			jsonObject.put("phone.type", manager.getPhoneType());
			jsonObject.put("phone.simcountryiso", manager.getSimCountryIso());
			jsonObject.put("phone.simoperator", manager.getSimOperator());
			jsonObject.put("phone.simoperatorname",
					manager.getSimOperatorName());
			jsonObject.put("phone.simserialnumber",
					manager.getSimSerialNumber());
			jsonObject.put("phone.simstate", manager.getSimState());
			jsonObject.put("phone.subscriberid", manager.getSubscriberId());
		} catch (Exception e) {
			Logger.e(TAG, "exception:" + e.getMessage());
			e.printStackTrace();
		}

		Logger.d(TAG, "device_extra_info:" + jsonObject.toString());
		return jsonObject;
	}

}
