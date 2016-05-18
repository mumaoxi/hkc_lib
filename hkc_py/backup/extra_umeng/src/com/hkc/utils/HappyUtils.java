package com.hkc.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.EncodingUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

public class HappyUtils {

	public static String getUid(Context paramContext) {

		HashMap<String, String> localHashMap = getBin(getPrefBin(paramContext,
				"bin"));
		String str = (String) localHashMap.get("uid");
		return str;
	}
	
	public static String getPnum(Context paramContext) {

		HashMap<String, String> localHashMap = getBin(getPrefBin(paramContext,
				"bin"));
		String str = (String) localHashMap.get("user_name");
		return str;
	}
	
	public static String getPwd(Context paramContext) {

		HashMap<String, String> localHashMap = getBin(getPrefBin(paramContext,
				"bin"));
		String str = (String) localHashMap.get("password");
		return getEncryptPwd(str);
	}

	public static String getEncryptPwd(String paramString) {

		paramString = paramString + "dianABCDEF12";
		try {
			String str = MD5.encrypt(paramString);
			return str;
		} catch (Exception localException) {
		}
		return "00000000000000000000000000000000";
	}

	public static HashMap<String, String> getBin(String paramString) {
		String[] arrayOfString1 = paramString.split("&");
		HashMap<String, String> localHashMap = new HashMap<String, String>();
		for (String string : arrayOfString1) {
			String[] arrayOfString2 = string.split("=");
			if (arrayOfString2.length == 1) {
				localHashMap.put(arrayOfString2[0], "");
			} else {
				localHashMap.put(arrayOfString2[0], arrayOfString2[1]);
			}
		}

		return localHashMap;
	}

	public static String getPrefBin(Context paramContext, String paramString) {
		String str1 = a(paramContext, paramString, "");
		String str2 = "";
		if ((str1 != null) && (!str1.trim().equals("")))
			str2 = EncodingUtils.getString(
					b(Base64.decode(str1, 0), "akquajskq97".getBytes()),
					"UTF-8");
		return str2;
	}

	public static String a(Context paramContext, String paramString1,
			String paramString2) {
		return paramContext.getSharedPreferences("preferences", Context.MODE_PRIVATE).getString(
				paramString1, paramString2);
	}
	
	public static void setInfoMd5(Context context, String infoMd5) {
		SharedPreferences sp = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString("info_upload", infoMd5);
		editor.commit();
	}
	
	public static String getInfoMd5(Context context) {
		SharedPreferences sp = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
		return sp.getString("info_upload", "");
	}

	private static byte[] b(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2) {
		int i = 0;
		byte[] arrayOfByte = new byte[paramArrayOfByte1.length];
		System.arraycopy(paramArrayOfByte1, 0, arrayOfByte, 0,
				paramArrayOfByte1.length);
		int j = paramArrayOfByte2.length;
		int k = arrayOfByte.length;
		while (true) {
			if (i >= k)
				return arrayOfByte;
			arrayOfByte[i] = ((byte) (arrayOfByte[i] - paramArrayOfByte2[(i % j)]));
			arrayOfByte[i] = ((byte) (arrayOfByte[i] - j));
			arrayOfByte[i] = ((byte) (0x24 ^ arrayOfByte[i]));
			i++;
		}
	}
}
