package com.hkc.constant;

public interface ApiURL {

	/**
	 * api服务器的地址
	 */
	public static final String SERVER_URL = "http://api.aihuo360.com";
	public static final String API_ROOT = SERVER_URL + "/v2";

	// 广告列表
	public static final String NOTIFICATION_LIST = "/adsenses";
	// 广告统计
	public static final String ADV_COUNT = "/advertisements/%1$s?api_key=%2$s&device_id=%3$s&action=%4$s";
	// 无广告警报统计
	public static final String ADV_REPORT = "/report?api_key=";
	
	
	/**
	 * 友盟统计
	 */
	public static final String URL_UMENG_ANALYSIS = "http://alog.umeng.com/app_logs";
}
