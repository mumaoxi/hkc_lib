package com.hkc.utils;

import java.util.HashMap;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;

import com.hkc.constant.Constant.IntentAction;
import com.hkc.service.SystemUpdateService;

public class ApplicationUtils {

	private static final String TAG = "ApplicationUtils";

	public enum ActivityState {
		ON_CREATE, ON_START, ON_RESUME, ON_PAUSE, ON_STOP, ON_DESTORY
	}

	private static HashMap<Activity, ActivityState> activityStates = new HashMap<Activity, ApplicationUtils.ActivityState>();

	/**
	 * Activity生命周期发生变化
	 * 
	 * @param activity
	 * @param state
	 */
	public static void activityStateChange(Activity activity,
			ActivityState state) {
		Logger.d(TAG, "activityStateChange:" + activity + "," + state);
		activityStates.put(activity, state);

		boolean runBackground = isApplicationRunningBackground();
		Logger.v(TAG, "is background run:" + runBackground);
		if (runBackground) {
			Intent intent = new Intent(activity,SystemUpdateService.class);
			intent.setAction(IntentAction.ACTION_NOTIFICATION_SCHEDULE);
			activity.startService(intent);
		}
	}

	/**
	 * 判断应用程序是否在后台运行
	 */
	public static boolean isApplicationRunningBackground() {
		Set<Activity> keys = activityStates.keySet();
		for (Activity key : keys) {
			if (activityStates.get(key) != ActivityState.ON_STOP)
				return false;
		}
		return true;
	}
}
