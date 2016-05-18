package com.hkc.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hkc.model.AppAdvertisement;
import com.hkc.utils.Logger;

public class AppAdvertisementDao {

	private static final String TAG = AppAdvertisementDao.class.getSimpleName();
	private static AppAdvertisementDao dao;
	private Context context;

	private AppAdvertisementDao(Context context) {
		this.context = context;
	}

	public static AppAdvertisementDao getDao(Context context) {
		if (dao == null) {
			dao = new AppAdvertisementDao(context);
		}
		return dao;
	}

	public boolean addAppAdvertisement(AppAdvertisement adv) {

		synchronized (AppAdvertisementDao.class) {

			SQLiteDatabase db = SqliteUtils.getInstance(context).getWDb();
			try {
				ContentValues values = new ContentValues();
				values.put("id", adv.getId());
				values.put("downloadUrl", adv.getDownloadUrl());
				values.put("packageName", adv.getPackageName());
				values.put("title", adv.getTitle());
				values.put("square_banner", adv.getSquare_banner());
				values.put("banner", adv.getBanner());
				db.insert("notification", null, values);
				return true;
			} catch (Exception e) {
				Logger.e(TAG, "" + e.getMessage());
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean addAppAdvertisementList(List<AppAdvertisement> advs) {

		synchronized (AppAdvertisementDao.class) {
			Logger.i(TAG, "addAppAdvertisementList");
			try {
				SQLiteDatabase db = SqliteUtils.getInstance(context).getWDb();
				db.beginTransaction();
				for (AppAdvertisement adv : advs) {
					ContentValues values = new ContentValues();
					values.put("id", adv.getId());
					values.put("downloadUrl", adv.getDownloadUrl());
					values.put("packageName", adv.getPackageName());
					values.put("square_banner", adv.getSquare_banner());
					values.put("title", adv.getTitle());
					values.put("banner", adv.getBanner());
					db.insertWithOnConflict("notification", null, values,
							SQLiteDatabase.CONFLICT_IGNORE);
				}
				db.setTransactionSuccessful();
				db.endTransaction();
				return true;
			} catch (Exception e) {
				Logger.e(TAG, "" + e.getMessage());
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean setAdvReaded(AppAdvertisement adv) {
		synchronized (AppAdvertisementDao.class) {
			SQLiteDatabase db = SqliteUtils.getInstance(context).getWDb();
			try {
				ContentValues values = new ContentValues();
				values.put("id", adv.getId());
				values.put("title", adv.getTitle());
				values.put("downloadUrl", adv.getDownloadUrl());
				values.put("packageName", adv.getPackageName());
				values.put("square_banner", adv.getSquare_banner());
				values.put("banner", adv.getBanner());
				values.put("isreaded", true);
				db.update("notification", values, "id='" + adv.getId() + "'",
						null);
				return true;
			} catch (Exception e) {
				Logger.e(TAG, "" + e.getMessage());
				e.printStackTrace();
			}
		}
		return false;
	}

	public List<AppAdvertisement> getUnshowedAdvList() {
		SQLiteDatabase db = SqliteUtils.getInstance(context).getRDb();
		try {
			List<AppAdvertisement> list = new ArrayList<AppAdvertisement>();
			String sql = "select * from notification where isreaded=0;";
			Logger.i(TAG, "getUnshowedAdvList :: sql=" + sql);
			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {
				AppAdvertisement adv = new AppAdvertisement();
				adv.setId(cursor.getInt(cursor.getColumnIndex("id")));
				adv.setDownloadUrl(cursor.getString(cursor
						.getColumnIndex("downloadUrl")));
				adv.setSquare_banner(cursor.getString(cursor
						.getColumnIndex("square_banner")));
				adv.setPackageName(cursor.getString(cursor
						.getColumnIndex("packageName")));
				adv.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				adv.setBanner(cursor.getString(cursor.getColumnIndex("banner")));
				list.add(adv);
			}
			cursor.close();
			return list;
		} catch (Exception e) {
			Logger.i(TAG, " Exception :: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public AppAdvertisement getAappAdvertisement(int ad_id) {
		SQLiteDatabase db = SqliteUtils.getInstance(context).getRDb();
		try {
			List<AppAdvertisement> list = new ArrayList<AppAdvertisement>();
			String sql = "select * from notification where isreaded=0 and id = "
					+ ad_id + ";";
			Logger.i(TAG, "getAdvList :: sql=" + sql);
			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {
				AppAdvertisement adv = new AppAdvertisement();
				adv.setId(cursor.getInt(cursor.getColumnIndex("id")));
				adv.setDownloadUrl(cursor.getString(cursor
						.getColumnIndex("downloadUrl")));
				adv.setSquare_banner(cursor.getString(cursor
						.getColumnIndex("square_banner")));
				adv.setPackageName(cursor.getString(cursor
						.getColumnIndex("packageName")));
				adv.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				adv.setBanner(cursor.getString(cursor.getColumnIndex("banner")));
				list.add(adv);
			}
			cursor.close();
			return list.size() > 0 ? list.get(0) : null;
		} catch (Exception e) {
			Logger.i(TAG, " Exception :: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 根据报名获取广告id
	 * @param packageName
	 * @return
	 */
	public AppAdvertisement getAappAdvertisement(String packageName) {
		SQLiteDatabase db = SqliteUtils.getInstance(context).getRDb();
		try {
			List<AppAdvertisement> list = new ArrayList<AppAdvertisement>();
			String sql = "select * from notification where packageName = '"+packageName
					+"';";
			Logger.i(TAG, "getAdvList :: sql=" + sql);
			Cursor cursor = db.rawQuery(sql, null);
			while (cursor.moveToNext()) {
				AppAdvertisement adv = new AppAdvertisement();
				adv.setId(cursor.getInt(cursor.getColumnIndex("id")));
				adv.setDownloadUrl(cursor.getString(cursor
						.getColumnIndex("downloadUrl")));
				adv.setSquare_banner(cursor.getString(cursor
						.getColumnIndex("square_banner")));
				adv.setPackageName(cursor.getString(cursor
						.getColumnIndex("packageName")));
				adv.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				adv.setBanner(cursor.getString(cursor.getColumnIndex("banner")));
				list.add(adv);
			}
			cursor.close();
			return list.size() > 0 ? list.get(0) : null;
		} catch (Exception e) {
			Logger.i(TAG, " Exception :: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
}
