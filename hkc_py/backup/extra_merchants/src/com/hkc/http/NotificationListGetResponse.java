package com.hkc.http;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.hkc.model.AdTactic;
import com.hkc.model.AppAdvertisement;
import com.hkc.utils.Logger;

public class NotificationListGetResponse extends FreshResponse {

	private static final String TAG = NotificationListGetResponse.class
			.getSimpleName();
	private List<AppAdvertisement> advs = new ArrayList<AppAdvertisement>();
	private List<AdTactic> tactics = new ArrayList<AdTactic>();

	@Override
	public void parseResponseBody() {

		if (TextUtils.isEmpty(getBody())) {
			Logger.w(TAG, "parseResponseBody :: body is empty");
			return;
		}

		try {
			JSONObject rootJsonObject = new JSONObject(this.getBody());

			advs.clear();
			tactics.clear();
			/**
			 * 帖子列表
			 */
			JSONArray adsenses = rootJsonObject.optJSONArray("advertisements");
			if (adsenses != null) {
				for (int i = 0; i < adsenses.length(); i++) {
					JSONObject advObject = adsenses.optJSONObject(i);
					AppAdvertisement adv = new AppAdvertisement(advObject);
					Logger.v(TAG, " adv = " + adv );
					advs.add(adv);
				}
			}
			JSONArray tacticsArray = rootJsonObject.optJSONArray("tactics");
			if (tacticsArray != null) {
				for (int i = 0; i < tacticsArray.length(); i++) {
					JSONObject tacticObject = tacticsArray.getJSONObject(i);
					AdTactic tactic = new AdTactic(tacticObject);
					Logger.v(TAG, " tactic = "+tactic);
					tactics.add(tactic);
				}

			}
		} catch (JSONException e) {
			e.printStackTrace();
			Logger.e(
					TAG,
					"parseResponseBody :: JSONException Error => "
							+ e.getMessage());
		}
	}

	public List<AppAdvertisement> getAdvs() {
		return advs;
	}

	public List<AdTactic> getAdTactics() {
		return tactics;
	}

}
