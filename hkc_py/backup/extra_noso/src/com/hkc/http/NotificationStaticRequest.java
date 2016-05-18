package com.hkc.http;

import java.util.Map;

import com.hkc.android.volley.Request.Method;
import com.hkc.constant.ApiURL;

public class NotificationStaticRequest extends
		FreshRequest<NotificationStaticResponse> {

	private String device_id;
	private String api_key;
	private String adv_id;

	public enum StaticAction {
		INSTALL("install"), VIEW("view"), CLICK("click");
		public final String value;

		private StaticAction(String value) {
			this.value = value;
		}
	}

	public void setApi_key(String api_key) {
		this.api_key = api_key;
	}

	private StaticAction staticAction = StaticAction.VIEW;

	@Override
	public String getApi() {
		return String.format(ApiURL.ADV_COUNT, adv_id, api_key,
				device_id, staticAction.value);
	}

	@Override
	public Map<String, Object> getParams() {
		return super.getParams();
	}

	public void setAdv_id(String adv_id) {
		this.adv_id = adv_id;
	}

	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}

	public void setStaticAction(StaticAction staticAction) {
		this.staticAction = staticAction;
	}

	@Override
	public int getMethod() {
		return Method.PUT;
	}

	@Override
	public Class<NotificationStaticResponse> getResponseClass() {
		return NotificationStaticResponse.class;
	}
}
