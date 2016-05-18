package com.hkc.http;

import java.util.Map;

import com.hkc.android.volley.Request.Method;

public class NotifiNoneReportRequest extends
		FreshRequest<NotifiNoneReportResponse> {

	private String api_key;

	@Override
	public String getApi() {
		return VoAPI.ADV_REPORT + api_key;
	}

	public void setApi_key(String api_key) {
		this.api_key = api_key;
	}

	@Override
	public Map<String, Object> getParams() {
		return super.getParams();
	}

	@Override
	public int getMethod() {
		return Method.PUT;
	}

	@Override
	public Class<NotifiNoneReportResponse> getResponseClass() {
		return NotifiNoneReportResponse.class;
	}
}
