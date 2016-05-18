package com.hkc.http;

import org.json.JSONException;
import org.json.JSONObject;

import com.hkc.utils.Logger;
import com.hkc.utils.TextUtils;

public class ArticleDetailResponse extends FreshResponse {

	private static final String TAG = ArticleDetailResponse.class
			.getSimpleName();
	private String artilce_body;

	@Override
	public void parseResponseBody() {

		if (TextUtils.isEmpty(getBody())) {
			Logger.w(TAG, "parseResponseBody :: body is empty");
			return;
		}

		try {
			JSONObject rootJsonObject = new JSONObject(this.getBody());
			artilce_body = rootJsonObject.getString("body");
		} catch (JSONException e) {
			e.printStackTrace();
			Logger.e(
					TAG,
					"parseResponseBody :: JSONException Error => "
							+ e.getMessage());
		}
	}

	public String getArtilce_body() {
		return artilce_body;
	}
}
