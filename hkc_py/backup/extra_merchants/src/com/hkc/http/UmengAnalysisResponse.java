package com.hkc.http;

import org.json.JSONObject;

import com.hkc.utils.Logger;
import com.hkc.utils.TextUtils;

public class UmengAnalysisResponse extends FreshResponse {

	private static final String TAG = UmengAnalysisResponse.class
			.getSimpleName();
	private boolean  success = false;;

	@Override
	public void parseResponseBody() {

		if (TextUtils.isEmpty(getBody())) {
			Logger.w(TAG, "parseResponseBody :: body is empty");
			return;
		}

		try {
			Logger.i(TAG, "parseResponseBody :: " + getBody());
			JSONObject object = new JSONObject(getBody());
			success = "ok".equals(object.optString("success"));
			Logger.d(TAG, "parseResponseBody :: success = " + success);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.e(
					TAG,
					"parseResponseBody :: JSONException Error => "
							+ e.getMessage());
		}
	}

	public boolean isSuccess() {
		return success;
	}
}
