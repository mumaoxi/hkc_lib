package com.hkc.model;

import org.json.JSONException;
import org.json.JSONObject;

public class AdTactic extends FreshObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String action;
	private String value;
	private int notice_type;
	private boolean isReaded = false;

	private int advertisement_id;

	public AdTactic() {

	}

	public AdTactic(JSONObject json) {
		this.initWithJson(json);
	}

	public AdTactic(int id, String action, String value) {
		this.id = id;
		this.action = action;
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getAdvertisement_id() {
		return advertisement_id;
	}

	public void setAdvertisement_id(int advertisement_id) {
		this.advertisement_id = advertisement_id;
	}

	public boolean isReaded() {
		return isReaded;
	}

	public void setReaded(boolean isReaded) {
		this.isReaded = isReaded;
	}

	@Override
	public String toString() {
		return "AdTactic [id=" + id + ", action=" + action + ", value=" + value
				+ ", notice_type=" + notice_type + ", isReaded=" + isReaded
				+ "]";
	}

	@Override
	public void initParamWithJsonAndKey(JSONObject jsonObject, String key)
			throws JSONException {
		String stringValue = jsonObject.optString(key);
		int intValue = jsonObject.optInt(key);
		if ("id".equals(key)) {
			this.id = intValue;
		} else if ("action".equals(key)) {
			this.action = stringValue;
		} else if ("value".equals(key)) {
			this.value = stringValue;
		} else if ("notice_type".equals(key)) {
			this.notice_type = intValue;
		} else if ("advertisement_id".equals(key)) {
			this.advertisement_id = intValue;
		}
	}

	public int getNotice_type() {
		return notice_type;
	}

	public void setNotice_type(int notice_type) {
		this.notice_type = notice_type;
	}

}
