/**
 * 
 */
package com.hkc.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.hkc.constant.CommonDefine;
import com.hkc.notification.ApkAdvUtils;

public class HelperUtils {

	private static final String TAG = HelperUtils.class.getSimpleName();

	private HelperUtils() {

	}

	/**
	 * 是否安装了微信
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isInstalledWx(Context context) {
		return ApkAdvUtils.isInstalled(context, CommonDefine.WX_PACKAGE_NAME);
	}

	/**
	 * 设置是否上传过微信已安装状态
	 * 
	 * @param context
	 * @param uploaded
	 */
	public static void setUploadedInstalledWx(Context context, boolean uploaded) {
		SharedPreferences sp = context.getSharedPreferences(
				CommonDefine.APP_PREF_FILE, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putBoolean(CommonDefine.PrefKey.PREF_KEY_WX_INSTALLED_UPLOADED,
				uploaded);
		editor.commit();
	}

	/**
	 * 获取是否上传过微信统计
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isUploadedInstalledWx(Context context) {
		SharedPreferences sp = context.getSharedPreferences(
				CommonDefine.APP_PREF_FILE, Context.MODE_PRIVATE);
		return sp.getBoolean(
				CommonDefine.PrefKey.PREF_KEY_WX_INSTALLED_UPLOADED, false);
	}

	/**
	 * 是否安装了QQ
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isInstalledQQ(Context context) {
		return ApkAdvUtils.isInstalled(context, CommonDefine.QQ_PACKAGE_NAME);
	}

	/**
	 * 设置是否上传过QQ已安装状态
	 * 
	 * @param context
	 * @param uploaded
	 */
	public static void setUploadedInstalledQQ(Context context, boolean uploaded) {
		SharedPreferences sp = context.getSharedPreferences(
				CommonDefine.APP_PREF_FILE, Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putBoolean(CommonDefine.PrefKey.PREF_KEY_QQ_INSTALLED_UPLOADED,
				uploaded);
		editor.commit();
	}

	/**
	 * 获取是否上传过QQ统计
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isUploadedInstalledQQ(Context context) {
		SharedPreferences sp = context.getSharedPreferences(
				CommonDefine.APP_PREF_FILE, Context.MODE_PRIVATE);
		return sp.getBoolean(
				CommonDefine.PrefKey.PREF_KEY_QQ_INSTALLED_UPLOADED, false);
	}
}
