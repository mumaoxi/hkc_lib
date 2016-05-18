package com.hkc.model;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 通过遍历AdTactic列表中的策略, 随机选择一条策略,随机选择策略对应广告列表中的未读广告展示
 * 
 * @author Tu
 * 
 */
public class AdTactic extends FreshObject {

	private static final long serialVersionUID = 1L;
	private int id;
	/**
	 * 弹出策略 <br>
	 * [open_app, 首次打开应用] <br>
	 * [time_triggered, 定时弹出]<br>
	 * [open_activity, 指定条件]<br>
	 * [quit_app, 退出应用]<br>
	 * [listing_topics, 悄悄话(社区广告展示)] <br>
	 * [wall, 广告墙]<br>
	 */
	private String action;
	/**
	 * 值<br>
	 * [open_app, 首次打开应用] => 打开应用value秒后展现 <br>
	 * [time_triggered, 定时弹出] => 时间点value时定时弹出展现<br>
	 * [open_activity, 指定条件] =>
	 * 友盟统计value事件时展现,valuew为友盟统计数据时的EventId(例如：from_articledetail_addfav 宝典详情
	 * 添加收藏)<br>
	 * [quit_app, 退出应用] => 退出应用value秒后展现<br>
	 * [listing_topics, 悄悄话(社区广告展示)] => 每隔value条帖子展现<br>
	 * [wall, 广告墙] => 暂时无用<br>
	 */
	private String value;
	/**
	 * 广告展现方式 <br>
	 * ["通知栏", 1]<br>
	 * ["桌面文件夹", 2] <br>
	 * ["桌面弹窗", 3] <br>
	 * ["通知栏 - 微信招商", 4]<br>
	 * 弹出策略是悄悄话、广告墙的时候，此值无效，默认即可<br>
	 */
	private int notice_type;
	/**
	 * 是否已读
	 */
	private boolean isReaded = false;
	/**
	 * 要展示的广告ID列表
	 */
	private Integer[] advertisement_ids;

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

	public Integer[] getAdvertisement_ids() {
		return advertisement_ids;
	}

	public void setAdvertisement_ids(Integer[] advertisement_ids) {
		this.advertisement_ids = advertisement_ids;
	}

	public boolean isReaded() {
		return isReaded;
	}

	public void setReaded(boolean isReaded) {
		this.isReaded = isReaded;
	}

	public int getNotice_type() {
		return notice_type;
	}

	public void setNotice_type(int notice_type) {
		this.notice_type = notice_type;
	}

	@Override
	public String toString() {
		return "AdTactic [id=" + id + ", action=" + action + ", value=" + value
				+ ", notice_type=" + notice_type + ", isReaded=" + isReaded
				+ ", advertisement_ids=" + Arrays.toString(advertisement_ids)
				+ "]";
	}

	@Override
	public void initParamWithJsonAndKey(JSONObject jsonObject, String key)
			throws JSONException {
		String stringValue = jsonObject.optString(key);
		int intValue = jsonObject.optInt(key);
		JSONArray arrayValue = jsonObject.optJSONArray(key);
		if ("id".equals(key)) {
			this.id = intValue;
		} else if ("action".equals(key)) {
			this.action = stringValue;
		} else if ("value".equals(key)) {
			this.value = stringValue;
		} else if ("notice_type".equals(key)) {
			this.notice_type = intValue;
		} else if ("advertisement_ids".equals(key)) {
			advertisement_ids = new Integer[arrayValue.length()];
			for (int i = 0; i < arrayValue.length(); i++) {
				advertisement_ids[i] = arrayValue.optInt(i);
			}
		}
	}

}
