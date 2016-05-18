package com.hkc.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.text.TextUtils;

@SuppressLint("SimpleDateFormat")
public class DateUtils {

	private static final String TAG = DateUtils.class.getSimpleName();

	public static final String DF_YYYYMMDD = "yyyy-MM-dd";
	public static final String DF_HHMM = "HH:mm";
	public static final String DF_HHMMSS = "HH:mm:ss";
	public static final String DF_YYYYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss";
	public static final String DF_YYYYMMDD_HHMM = "yyyy-MM-dd HH:mm";
	public static final String DF_YYYYMMDD_T_HHMMSS_Z = "yyyy-MM-dd'T'HH:mm:ssZ";
	public static final String DF_MMDD_HHMM = "MM-dd HH:mm";

	public static String formatDate(Date date, String format) {
		try {
			if (date != null) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
				return simpleDateFormat.format(date);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static Date parseDate(String date, String format) {
		try {
			if (!TextUtils.isEmpty(date)) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
				return simpleDateFormat.parse(date);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isToday(Date date) {
		try {
			String dateStr = formatDate(date, DF_YYYYMMDD);
			String dateToday = formatDate(new Date(), DF_YYYYMMDD);
			return dateStr.equals(dateToday);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	

	public static String getTimeNow() {

		SimpleDateFormat sdf = new SimpleDateFormat(DF_YYYYMMDD_HHMMSS);
		return sdf.format(new Date());
	}

	public static long getInterval(String from, String to) {

		SimpleDateFormat sdf = new SimpleDateFormat(DF_YYYYMMDD_HHMMSS);
		try {
			Date start = sdf.parse(from);
			Date end = sdf.parse(to);
			long span = start.getTime() - end.getTime();
			return span;
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return 0;
	}

}
