package com.hkc.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hkc.model.AdTactic;
import com.hkc.utils.Logger;

public class AdTacticDao {

    private static final String TAG = AdTacticDao.class.getSimpleName();
    private static AdTacticDao self;
    private Context context;

    private AdTacticDao(Context context) {
        this.context = context;
    }

    public static AdTacticDao getDao(Context context) {
        if (null == self) {
            self = new AdTacticDao(context);
        }
        return self;
    }

    public boolean addAdTactic(AdTactic tactic) {

        synchronized (AdTacticDao.class) {

            SQLiteDatabase db = SqliteUtils.getInstance(context).getWDb();
            try {
                ContentValues values = new ContentValues();
                values.put("id", tactic.getId());
                values.put("advertisement_id", tactic.getAdvertisement_id());
                values.put("value", tactic.getValue());
                values.put("notice_type", tactic.getNotice_type());
                values.put("action", tactic.getAction());
                db.insert("tactics", null, values);
                return true;
            } catch (Exception e) {
                Logger.e(TAG, "" + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean addAdTacticList(List<AdTactic> tactics) {

        synchronized (AdTacticDao.class) {
            Logger.i(TAG, "addAdTacticList");
            try {
                SQLiteDatabase db = SqliteUtils.getInstance(context).getWDb();
                db.beginTransaction();
                for (AdTactic tactic : tactics) {
                    ContentValues values = new ContentValues();
                    values.put("id", tactic.getId());
                    values.put("advertisement_id", tactic.getAdvertisement_id());
                    values.put("value", tactic.getValue());
                    values.put("notice_type", tactic.getNotice_type());
                    values.put("action", tactic.getAction());
                    db.insert("tactics", null, values);
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

    public boolean setTacticReaded(AdTactic tactic) {
        Logger.i(TAG, "setTacticReaded :: tactic = " + tactic);
        synchronized (AdTactic.class) {
            SQLiteDatabase db = SqliteUtils.getInstance(context).getWDb();
            try {
                ContentValues values = new ContentValues();
                values.put("id", tactic.getId());
                values.put("advertisement_id", tactic.getAdvertisement_id());
                values.put("value", tactic.getValue());
                values.put("notice_type", tactic.getNotice_type());
                values.put("action", tactic.getAction());
                values.put("isreaded", true);
                db.update("tactics", values, "id='" + tactic.getId() + "'",
                        null);
                return true;
            } catch (Exception e) {
                Logger.e(TAG, "" + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    public List<AdTactic> getUnReadedTacticsList() {
        SQLiteDatabase db = SqliteUtils.getInstance(context).getRDb();
        try {
            List<AdTactic> list = new ArrayList<AdTactic>();
            String sql = "select * from tactics where isreaded=0;";
            Logger.i(TAG, "getUnReadedTacticsList :: sql=" + sql);
            Cursor cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                AdTactic tactic = new AdTactic();
                tactic.setId(cursor.getInt(cursor.getColumnIndex("id")));
                tactic.setAdvertisement_id(cursor.getInt(cursor.getColumnIndex("advertisement_id")));
                tactic.setAction(cursor.getString(cursor
                        .getColumnIndex("action")));
                tactic.setNotice_type(cursor.getInt(cursor
                        .getColumnIndex("notice_type")));
                tactic.setValue(cursor.getString(cursor.getColumnIndex("value")));
                list.add(tactic);
            }
            cursor.close();
            return list;
        } catch (Exception e) {
            Logger.i(TAG, " Exception :: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /*
     * actionName must as same as one UmengEventId every Umeng onEvent method is
     * called this should be check if has unfinished task remain
     */
    public AdTactic getUnreadedActionTactic(String actionName) {
        SQLiteDatabase db = SqliteUtils.getInstance(context).getRDb();
        try {
            String sql = "select * from tactics where isreaded=0 and value = '"
                    + actionName + "';";
            Logger.i(TAG, "getUnreadedActionTactic :: sql = " + sql);
            Cursor cursor = db.rawQuery(sql, null);
            AdTactic tactic = null;
            if (cursor.moveToNext()) {
                tactic = new AdTactic();
                tactic.setId(cursor.getInt(cursor.getColumnIndex("id")));
                tactic.setAdvertisement_id(cursor.getInt(cursor.getColumnIndex("advertisement_id")));
                tactic.setNotice_type(cursor.getInt(cursor
                        .getColumnIndex("notice_type")));
                tactic.setAction(cursor.getString(cursor.getColumnIndex("action")));
                tactic.setValue(cursor.getString(cursor.getColumnIndex("value")));
            }
            cursor.close();
            Logger.i(TAG, "getUnreadedActionTactic :: tactic = " + tactic);
            return tactic;
        } catch (Exception e) {
            Logger.i(TAG, " Exception :: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteAll() {
        synchronized (AdTactic.class) {
            try {
                SQLiteDatabase db = SqliteUtils.getInstance(context).getWDb();
                db.delete("tactics", null, null);
            } catch (Exception e) {
                Logger.i(TAG, "deleteAll :: Exception = " + e.getMessage());
                return false;
            }
        }
        return true;
    }

}
