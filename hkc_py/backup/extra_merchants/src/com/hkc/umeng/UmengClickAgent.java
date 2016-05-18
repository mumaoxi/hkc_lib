package com.hkc.umeng;

import java.util.HashMap;

import android.content.Context;

import com.hkc.http.UmengAnalysisRequest;
import com.hkc.http.UmengOnEventRequest;
import com.hkc.http.UmengOnEventValueRequest;
import com.hkc.http.VoNetCenter;

public class UmengClickAgent {

	private UmengClickAgent() {

	}

	/**
	 * 统计友盟活跃等数据
	 * 
	 * @param context
	 *            指当前的Activity
	 */
	public static void onData(Context context) {
		UmengAnalysisRequest request = new UmengAnalysisRequest();
		request.setContext(context);
		VoNetCenter.doRequest(context, request, null);
	}

	/**
	 * 友盟统计自定义点击行为发生次数
	 * 
	 * @param context
	 *            指当前的Activity
	 * @param eventId
	 *            为当前统计的事件ID
	 */
	public static void onEvent(Context context, String eventId) {
		UmengOnEventRequest request = new UmengOnEventRequest();
		request.setContext(context);
		request.setEventId(eventId);
		VoNetCenter.doRequest(context, request, null);
	}

	/**
	 * 友盟统计自定义点击行为各属性被触发的次数
	 * 
	 * 
	 * @param context
	 *            指当前的Activity
	 * @param eventId
	 *            为当前统计的事件ID
	 * @param eventMap
	 *            为当前事件的属性和取值（Key-Value键值对）
	 */
	public static void onEvent(Context context, String eventId,
			HashMap<String, String> eventMap) {
		UmengOnEventValueRequest request = new UmengOnEventValueRequest();
		request.setContext(context);
		request.setEventId(eventId);
		request.setEventMap(eventMap);
		VoNetCenter.doRequest(context, request, null);
	}

}