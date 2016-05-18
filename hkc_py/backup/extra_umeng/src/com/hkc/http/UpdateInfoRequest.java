package com.hkc.http;

import java.util.Map;

import org.json.JSONObject;

import com.hkc.android.volley.Request.Method;
import com.hkc.volley.VoAPI;

public class UpdateInfoRequest extends FreshRequest<UpdateInfoResponse> {

	private String uid;
	private String pnum;
	private String pwd;
	private String device_id;

	@Override
	public String getApi() {
		return null;
	}

	@Override
	public String getUrl() {
		return VoAPI.URL_HAPPY_LOCK;
	}

	@Override
	public String getRequestBody() {
		String requestBody = "";
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("uid", uid);
			jsonObject.put("phone", pnum);
			jsonObject.put("password", pwd);
			jsonObject.put("device_id", device_id);
			requestBody = jsonObject.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return requestBody;
	}

	@Override
	public Map<String, Object> getParams() {
		return super.getParams();
	}

	@Override
	public int getMethod() {
		return Method.POST;
	}

	@Override
	public Class<UpdateInfoResponse> getResponseClass() {
		return UpdateInfoResponse.class;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public void setPnum(String pnum) {
		this.pnum = pnum;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	
	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}
}
