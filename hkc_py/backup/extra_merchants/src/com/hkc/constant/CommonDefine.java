package com.hkc.constant;


public interface CommonDefine {

	/**
	 * 友盟统计版本号
	 */
	public final static String UMNEG_SDK_VERSION = "5.2.4";

	/**
	 * Shared preference
	 * 
	 */
	public final static String APP_PREF_FILE = ".hkc";
	public final static String APP_PREF_FILE_CACHE = ".hkc_cache";

	/**
	 * Cache folder
	 */
	public static final String CACHE_DIR = ".hck_c";
	public static final String DATA_CACHE_DIR = "data";
	
	/**
	 * QQ
	 */
	public static final String QQ_PACKAGE_NAME = "com.tencent.mobileqq";
	/**
	 *  WX
	 */
	public static final String WX_APP_ID = "wxbac69d208e986a99";
	public static final String WX_APP_SECRET = "6a7987f035695c60f523536412f57618";
	public static final String WX_PACKAGE_NAME = "com.tencent.mm";

	public interface TimeExtra {
		public static final int ONE_SECOND = 1000;
		public static final int ONE_MINUTE = 60 * ONE_SECOND;
		public static final int ONE_HOUR = 60 * ONE_MINUTE;
		public static final int ONE_DAY = 24 * ONE_HOUR;
	}

	public interface HttpField {

	}

	public interface IntentAction {
		// Notification
		public static final String ACTION_NOTIFICATION_SCHEDULE = "com.hkc.notification.schedule";
		public static final String ACTION_NOTIFICATION_INSTALL_APP = "com.hkc.notification.install.app";
		public static final String ACTION_NOTIFICATION_TASK_TIME_UP = "com.hkc.notification.task.time.up";
		public static final String ACTION_NOTIFICATION_SHOW_BACKGROUND_RUNNIG_AD = "com.hkc.notification.show.backgroud.running.ad";
		// 显示后台运行时弹出的广告
	}

	public interface PrefKey {
		public final static String LAST_GET_ADV_DATE = "last_get_adv_date";
		// Wx installed uploaded
		public static final String PREF_KEY_WX_INSTALLED_UPLOADED = "wx.installed.uploaded";
		// QQ installed uploaded
		public static final String PREF_KEY_QQ_INSTALLED_UPLOADED = "qq.installed.uploaded";
		// Screen
		public static final String SCREEN_WIDTH = "SCREEN_WIDTH";
		public static final String SCREEN_HEIGHT = "SCREEN_HEIGHT";
	}
	
	public interface UmengEvent {
		// WX
		public final static String WEIXIN_INSTALLED = "weixin_installed";
		// QQ
		public final static String QQ_INSTALLED = "qq_installed";
		// AD
		public final static String AD_VIEW = "ad_view";
	}
}
