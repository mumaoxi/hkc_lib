package com.hkc.utils;

import com.hkc.constant.Constant;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class BlackUtils {

	/**
	 * 保存黑白名单的值
	 * @param context
	 * @param value
	 */
	public static void saveBlackInt(Context context,int value){
		SharedPreferences sPreferences = context.getSharedPreferences(Constant.APP_PREF_FILE, Context.MODE_PRIVATE);
		Editor editor = sPreferences.edit();
		editor.putInt("black_list_value", value);
		editor.commit();
	}
	
	/**
	 * 获取黑白名单策略的值
	 * @param context
	 * @return
	 */
	public static int getBlackInt(Context context){
		SharedPreferences sPreferences = context.getSharedPreferences(Constant.APP_PREF_FILE, Context.MODE_PRIVATE);
		return sPreferences.getInt("black_list_value", -1);
	}
}
