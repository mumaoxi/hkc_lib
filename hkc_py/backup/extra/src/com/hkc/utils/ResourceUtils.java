package com.hkc.utils;

import java.io.InputStream;
import java.lang.reflect.Field;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ResourceUtils {

	public static int getResourceId(String packageName,
			String resouceClass, String fieldName) {
		try {
			Class draClass = Class.forName(packageName + ".R$" + resouceClass);
			Field localField = draClass.getField(fieldName);
			return localField.getInt(fieldName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static int getLayout(Context context, String packageName,
			String layouteName) {
		try {
			Class draClass = Class.forName(packageName + ".R$layout");
			Field localField = draClass.getField(layouteName);
			return localField.getInt(layouteName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static int getDrawable(Context context, String packageName,
			String drawableName) {
		try {
			Class draClass = Class.forName(packageName + ".R$drawable");
			Field localField = draClass.getField(drawableName);
			return localField.getInt(drawableName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static int getId(Context context, String packageName, String idName) {
		try {
			Class draClass = Class.forName(packageName + ".R$id");
			Field localField = draClass.getField(idName);
			return localField.getInt(idName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * 从Assets中读取图片
	 */
	public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
		Bitmap image = null;
		AssetManager am = context.getResources().getAssets();
		try {
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is);
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return image;

	}
}
