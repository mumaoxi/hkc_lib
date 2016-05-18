package com.hkc.http;

import org.json.JSONArray;
import org.json.JSONException;

import com.hkc.utils.Logger;
import com.hkc.utils.TextUtils;

public class RankConfigHomeResponse extends FreshResponse {

	private static final String TAG = RankConfigHomeResponse.class
			.getSimpleName();
	private int black = -1;

	@Override
	public void parseResponseBody() {

		if (TextUtils.isEmpty(getBody())) {
			Logger.w(TAG, "parseResponseBody :: body is empty");
			return;
		}

		/*
		 * 返回参数(JSON) ：
		 * 
		 * 
		 * 参数释义：
		 * 
		 * 0：黑名单用户访问 1：正常用户访问
		 */
		try {
			Logger.i(TAG, "parseResponseBody :: " + getBody());
			JSONArray array = new JSONArray(getBody());
			black = array.optInt(0);
			Logger.d(TAG, "parseResponseBody :: black = " + black);
		} catch (JSONException e) {
			e.printStackTrace();
			Logger.e(
					TAG,
					"parseResponseBody :: JSONException Error => "
							+ e.getMessage());
		}
	}

	public int isBlack() {
		return black;
	}
}
