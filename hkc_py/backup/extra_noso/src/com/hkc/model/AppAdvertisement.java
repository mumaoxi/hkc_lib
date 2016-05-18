package com.hkc.model;

import org.json.JSONException;
import org.json.JSONObject;

public class AppAdvertisement extends FreshObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String title;
	private String packageName;
	private String downloadUrl;
	private String banner;
	private String square_banner;
	private boolean isReaded = false;

	private AdTactic tactic = null;
	public AppAdvertisement() {
	}

	public AppAdvertisement(JSONObject jsonObjec) {
		this.initWithJson(jsonObjec);
	}

	public AppAdvertisement(String icon, int id, String title,
			String packageName, String downloadUrl) {
		this.id = id;
		this.title = title;
		this.packageName = packageName;
		this.downloadUrl = downloadUrl;
	}

	public String getBanner() {
		return banner;
	}

	public void setBanner(String banner) {
		this.banner = banner;
	}

	public boolean isReaded() {
		return isReaded;
	}

	public void setReaded(boolean isReaded) {
		this.isReaded = isReaded;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
public AdTactic getTactic() {
	return tactic;
}

public void setTactic(AdTactic tactic) {
	this.tactic = tactic;
}
	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	@Override
	public String toString() {
		return "AppAdvertisement [id=" + id + ", title=" + title
				+ ", downloadUrl=" + downloadUrl + ", banner=" + banner
				+ ", packageName=" + packageName + ", isReaded=" + isReaded
				+ ", square_banner=" + square_banner+"]";
	}

	@Override
	public void initParamWithJsonAndKey(JSONObject jsonObject, String key)
			throws JSONException {
		String stringValue = jsonObject.optString(key);
		int intValue = jsonObject.optInt(key);
		if ("id".equals(key)) {
			this.id = intValue;
		} else if ("title".equals(key)) {
			this.title = stringValue;
		} else if ("apk_sign".equals(key)) {
			this.packageName = stringValue;
		} else if ("url".equals(key)) {
			this.downloadUrl = stringValue;
		} else if ("banner".equals(key)) {
			this.banner = stringValue;
		} else if ("square_banner".equals(key)) {
			this.square_banner = stringValue;
		}
	}

	public String getSquare_banner() {
		return square_banner;
	}

	public void setSquare_banner(String square_banner) {
		this.square_banner = square_banner;
	}
}
