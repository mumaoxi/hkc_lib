package com.hkc.utils;

import java.net.URLEncoder;
import java.security.MessageDigest;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.hkc.constant.MetaDataKey;

public class UmengUtils {

	public static String getXUmengSdk(Context context) {
		StringBuffer localStringBuffer1 = new StringBuffer();
		localStringBuffer1.append("Android");
		localStringBuffer1.append("/");
		localStringBuffer1.append("5.2.4");
		localStringBuffer1.append(" ");
		try {
			StringBuffer localStringBuffer2 = new StringBuffer();
			localStringBuffer2.append(getApplicationLabel(context));
			localStringBuffer2.append("/");
			localStringBuffer2.append(getVersionName(context));
			localStringBuffer2.append(" ");
			localStringBuffer2.append(Build.MODEL);
			localStringBuffer2.append("/");
			localStringBuffer2.append(Build.VERSION.RELEASE);

			localStringBuffer2.append(" ");
			localStringBuffer2.append(md5(getMetaData(context, MetaDataKey.HKC_UM_APP_KEY)
					.toString()));

			localStringBuffer1.append(URLEncoder.encode(
					localStringBuffer2.toString(), "UTF-8"));
		} catch (Exception localException) {
			localException.printStackTrace();
		}

		return localStringBuffer1.toString();
	}

	public static String getApplicationLabel(Context context) {
		return context.getPackageManager()
				.getApplicationLabel(context.getApplicationInfo()).toString();
	}

	public static String getVersionName(Context paramContext) {
		try {
			PackageInfo localPackageInfo = paramContext.getPackageManager()
					.getPackageInfo(paramContext.getPackageName(), 0);
			return localPackageInfo.versionName;
		} catch (PackageManager.NameNotFoundException localNameNotFoundException) {
		}
		return "";
	}

	public static String getMetaData(Context context, String key) {
		try {
			ApplicationInfo info = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);

			Bundle bundle = info.metaData;
			String value = bundle.getString(key);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String md5(String paramString) {

		if (paramString == null) {
			return null;
		}

		try {
			byte[] arrayOfByte1 = paramString.getBytes();
			MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
			localMessageDigest.reset();
			localMessageDigest.update(arrayOfByte1);
			byte[] arrayOfByte2 = localMessageDigest.digest();
			StringBuffer localStringBuffer = new StringBuffer();
			for (int i = 0; i < arrayOfByte2.length; i++) {
				localStringBuffer.append(String.format("%02X",
						new Object[] { Byte.valueOf(arrayOfByte2[i]) }));
			}

			return localStringBuffer.toString();
		} catch (Exception localException) {
		}
		return paramString.replaceAll("[^[a-z][A-Z][0-9][.][_]]", "");
	}
}
