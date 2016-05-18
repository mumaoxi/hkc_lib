package com.hkc.model;

import org.json.JSONException;
import org.json.JSONObject;

public class AppAdvertisement extends FreshObject {

	private static final long serialVersionUID = 1L;
	/**
	 * ID
	 */
	private int id;
	/**
	 * 标题
	 */
	private String title;
	/**
	 * 描述
	 */
	private String description;
	/**
	 * 包名
	 */
	private String packageName;
	/**
	 * APK下载地址
	 */
	private String downloadUrl;
	/**
	 * 应用图标
	 */
	private String icon;
	/**
	 * 通知栏的Banner
	 */
	private String banner;
	/**
	 * 悬浮框的Banner
	 */
	private String square_banner;
	/**
	 * 是否已读
	 */
	private boolean isReaded = false;
	/**
	 * 策略
	 */
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

	public String getSquare_banner() {
		return square_banner;
	}

	public void setSquare_banner(String square_banner) {
		this.square_banner = square_banner;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "AppAdvertisement [id=" + id + ", title=" + title
				+ ", description=" + description + ", packageName="
				+ packageName + ", downloadUrl=" + downloadUrl + ", icon="
				+ icon + ", banner=" + banner + ", square_banner="
				+ square_banner + ", isReaded=" + isReaded + ", tactic="
				+ tactic + "]";
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
		} else if ("icon".equals(key)) {
			this.icon = stringValue;
		} else if ("description".equals(key)) {
			this.description = stringValue;
		}
	}
}
