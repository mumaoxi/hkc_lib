package com.hkc.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.hkc.utils.Logger;

public class FreshSQLHelper extends SQLiteOpenHelper {

	public static final String TAG = FreshSQLHelper.class.getSimpleName();

	public static final int DB_VERSION = 7;
	public static final String DB_NAME = "fresh.db";
	private static FreshSQLHelper helper;

	// Tables
	public static final String TABLE_CHAT = "CHAT";
	public static final String TABLE_PM = "private_msg";

	// Table chat properties
	public interface TableChat {

		public final String id = "_ID";
		public final String user = "USER";
		public final String content = "CONTENT";
		public final String date = "DATE";
		public final String msg_from = "MSG_FROM";
		public final String status = "STATUS";
	}

	public static FreshSQLHelper getHelper(Context context) {
		if (helper == null) {
			helper = new FreshSQLHelper(context, null, DB_VERSION);
		}
		return helper;
	}

	public FreshSQLHelper(Context context, CursorFactory factory, int version) {
		super(context, DB_NAME, factory, version);
	}

	/**
	 * 
	 * @param db
	 * @param sqls
	 */
	private static void executeBatchSqls(SQLiteDatabase db) {
		try {
			String sql = "drop table if exists `cache`;";
			String sql2 = "create table `cache`(`keyname` text primary key,`keyvalue` text,`update_time` text);";
			//private_msg
			String sql3 = "drop table if exists `private_msg`;";
			String sql4 = "create table `private_msg`("
					+ "`id` varchar primary key," + "`receiver_id` varchar,"
					+ "`body` text," + "`opened` boolean default 0,"
					+ "`created_at` varchar," + "`member_id` varchar,"
					+ "`member_nickname` varchar,"
					+ "`member_gender` boolean default 0,"
					+ "`member_avatar` varchar,"
					+ "`member_level` integer default 0,"
					+ "`member_score_total` integer default 0,"
					+ "`member_verified` boolean default 0);";
			//notification adv
			String sql5="drop table if exists `notification`;";
			String sql6="create table `notification`("
					+"'id' interger primary key ,"
					+"'downloadUrl' varchar,"
					+"'isreaded' interger default 0,"
					+"'square_banner' varchar,"
					+"'packageName' varchar,"
					+"'banner' varchar,"
					+"'title' varchar)";
			//notification tactics
			String sql7="drop table if exists `tactics`;";
			String sql8="create table `tactics`("
					+"'id' interger primary key ,"
					+"'action' varchar,"
					+"'isreaded' interger default 0,"
					+"'notice_type' interger,"
					+"'advertisement_id' interger,"
					+"'value' varchar,"
					+"'unit' varchar)";
			db.execSQL(sql);
			db.execSQL(sql2);
			db.execSQL(sql3);
			db.execSQL(sql4);
			db.execSQL(sql5);
			db.execSQL(sql6);
			db.execSQL(sql7);
			db.execSQL(sql8);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Logger.d(TAG, " create talbe fresh.db");
		executeBatchSqls(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Logger.d(TAG, " drop talbe fresh.db");
		executeBatchSqls(db);
	}




}