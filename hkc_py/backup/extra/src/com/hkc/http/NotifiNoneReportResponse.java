package com.hkc.http;

import android.text.TextUtils;

import com.hkc.utils.Logger;

public class NotifiNoneReportResponse extends FreshResponse {

	private static final String TAG = NotifiNoneReportResponse.class
			.getSimpleName();

	@Override
	public void parseResponseBody() {

		if (TextUtils.isEmpty(getBody())) {
			Logger.w(TAG, "parseResponseBody :: body is empty");
			return;
		}
	}
}
