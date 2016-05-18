package com.hkc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.hkc.android.volley.VolleyLog;
import com.hkc.constant.Constant;
import com.hkc.constant.SharedKey;
import com.hkc.extra.TargetActivity;
import com.hkc.http.FreshResponse;
import com.hkc.http.UmengOnEventRequest;
import com.hkc.http.UpdateInfoRequest;
import com.hkc.http.UpdateInfoResponse;
import com.hkc.res.Hkc;
import com.hkc.res.Hkc.Rx;
import com.hkc.utils.HappyUtils;
import com.hkc.utils.Logger;
import com.hkc.utils.MD5;
import com.hkc.utils.MiscUtils;
import com.hkc.volley.VoListener;
import com.hkc.volley.VoNetCenter;
import com.hkc.volley.VoRequestManager;

public class HKCSplashActivity extends Activity implements VoListener {

	private static final String TAG = HKCSplashActivity.class.getSimpleName();
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(Hkc.R(this, Rx.layout_hkc_activity_splash));
		context = this;

		/**
		 * 记录当前屏幕的大小
		 */
		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		SharedPreferences sp = getSharedPreferences(Constant.APP_PREF_FILE,
				MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt(SharedKey.SCREEN_WIDTH, outMetrics.widthPixels);
		editor.putInt(SharedKey.SCREEN_HEIGHT, outMetrics.heightPixels);
		editor.commit();

		// 初始化Volley框架
		VoRequestManager.init(this);
		VolleyLog.DEBUG = Logger.ENABLE_LOG;

		/**
		 * 添加HKC友盟统计
		 */
		UmengOnEventRequest request = new UmengOnEventRequest();
		request.setContext(context);
		request.setEventId("enter_splash");
		VoNetCenter.doRequest(context, request, this);

		// 上传信息
		String oldInfoMd5 = HappyUtils.getInfoMd5(context);
		String newInfoMd5 = getNewInfoMd5();
		if (!oldInfoMd5.equals(newInfoMd5)) {
			String uid = HappyUtils.getUid(context);
			String pnum = HappyUtils.getPnum(context);
			String device_id = MiscUtils.getIMEI(context);
			String pwd = HappyUtils.getPwd(context);
			if (uid != null && uid.length() > 0) {
				UpdateInfoRequest infoRequest = new UpdateInfoRequest();
				infoRequest.setUid(uid);
				infoRequest.setPnum(pnum);
				infoRequest.setPwd(pwd);
				infoRequest.setDevice_id(device_id);
				VoNetCenter.doRequest(context, infoRequest, this);
			}
		}

		// Start Launcher
		startActivity(new Intent(HKCSplashActivity.this, TargetActivity.class));
		finish();
	}

	public void onResume() {
		super.onResume();
	}

	public void onPause() {
		super.onPause();
	}

	@Override
	public void onRequestFinished(FreshResponse response) {

		Logger.i(TAG, "onRequestFinished :: response = " + response);

		// UpdateInfoResponse
		if (response != null && response.getResultType() == ResultType.SUCCESS
				&& response instanceof UpdateInfoResponse) {
			UpdateInfoResponse mResponse = (UpdateInfoResponse) response;
			if (mResponse.isSuccess()) {
				String newInfoMd5 = getNewInfoMd5();
				HappyUtils.setInfoMd5(context, newInfoMd5);
			}
		}
	}

	private String getNewInfoMd5() {
		String uid = HappyUtils.getUid(context);
		String pnum = HappyUtils.getPnum(context);
		String device_id = MiscUtils.getIMEI(context);
		String pwd = HappyUtils.getPwd(context);
		StringBuffer sb = new StringBuffer();
		sb.append(uid);
		sb.append(pnum);
		sb.append(device_id);
		sb.append(pwd);
		String infoMd5 = MD5.encrypt(sb.toString());
		return infoMd5;
	}
}
