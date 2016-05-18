package com.hkc.http;

public interface VoAPI {

	/**
	 * API服务器的地址
	 */
	public static final String API_ROOT = "http://api.aihuo360.com/v2";

	/**
	 * 应用卸载展示地址
	 */
	public static final String UNINSTALL_URL = "http://compath.sinaapp.com/index.php/Home/Uninstall/uninstall/";

	/**
	 * 广告
	 */
	// 广告列表
	public static final String NOTIFICATION_LIST = "/adsenses";
	// 广告统计
	public static final String ADV_COUNT = "/advertisements/%1$s?api_key=%2$s&device_id=%3$s&action=%4$s";
	// 无广告警报统计
	public static final String ADV_REPORT = "/report?api_key=";
	public static final String API_ADV_REPORT = "/report";

	/**
	 * 友盟统计
	 */
	public static final String URL_UMENG_ANALYSIS = "http://alog.umeng.com/app_logs";
}
