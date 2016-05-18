package com.hkc.http;

import java.net.URLEncoder;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.hkc.HKCSplashActivity;
import com.hkc.android.volley.Request.Method;
import com.hkc.constant.Constant;
import com.hkc.constant.Constant.TimeExtra;
import com.hkc.constant.MetaDataKey;
import com.hkc.constant.SharedKey;
import com.hkc.extra.TargetActivity;
import com.hkc.utils.DateUtils;
import com.hkc.utils.MD5;
import com.hkc.utils.MiscUtils;
import com.hkc.utils.UmengUtils;
import com.hkc.volley.VoAPI;

public class UmengAnalysisRequest extends FreshRequest<UmengAnalysisResponse> {

	private Context context;

	@Override
	public String getApi() {
		return null;
	}

	@Override
	public String getUrl() {
		return VoAPI.URL_UMENG_ANALYSIS;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	public String getRequestBody() {
		String requestBody = "";
		try {
			JSONObject jsonObject = new JSONObject();
			JSONObject bodyObject = new JSONObject();
			JSONObject headerObject = new JSONObject();

			String device_id = MiscUtils.getIMEI(context);
			/**
			 * body
			 */
			Date dateNow = new Date();
			JSONObject launchObject = new JSONObject();
			launchObject.put("date",
					DateUtils.formatDate(dateNow, "yyyy-MM-dd"));
			launchObject.put("session_id",
					MD5.encrypt(device_id + System.currentTimeMillis())
							.toUpperCase(Locale.CHINA));
			launchObject.put("time", DateUtils.formatDate(dateNow, "HH:mm:ss"));
			JSONArray launchArray = new JSONArray();
			launchArray.put(launchObject);
			bodyObject.put("launch", launchArray);

			JSONObject terminateObject = new JSONObject();
			long duration = System.currentTimeMillis() % 550 + 10;
			terminateObject.put("duration", duration);
			terminateObject.put("date",
					DateUtils.formatDate(dateNow, "yyyy-MM-dd"));
			JSONArray activityArray = new JSONArray();
			long duration1 = duration % 2 + 2;
			JSONArray activityArrayItem1 = new JSONArray();
			activityArrayItem1.put(HKCSplashActivity.class.getName());
			activityArrayItem1.put(duration1);
			JSONArray activityArrayItem2 = new JSONArray();
			activityArrayItem2.put(TargetActivity.class.getName());
			activityArrayItem2.put(duration - duration1);
			activityArray.put(activityArrayItem1);
			activityArray.put(activityArrayItem2);
			terminateObject.put("activities", activityArray);

			terminateObject.put(
					"session_id",
					MD5.encrypt(
							device_id + System.currentTimeMillis()
									+ System.currentTimeMillis() % 500)
							.toUpperCase(Locale.CHINA));
			terminateObject.put(
					"time",
					DateUtils.formatDate(new Date(System.currentTimeMillis()
							- System.currentTimeMillis() % 300*TimeExtra.ONE_SECOND), "HH:mm:ss"));

			JSONArray terminateArray = new JSONArray();
			terminateArray.put(terminateObject);
			bodyObject.put("terminate", terminateArray);

			/**
			 * header
			 */
			SharedPreferences sp = context.getSharedPreferences(
					Constant.APP_PREF_FILE, Context.MODE_PRIVATE);
			headerObject.put("access_subtype", "UMTS");
			headerObject.put("appkey", String.valueOf(MiscUtils.getMetaData(
					context, MetaDataKey.HKC_UM_APP_KEY)));
			headerObject.put("app_version", MiscUtils.getAppVersion(context));
			headerObject.put(
					"resolution",
					sp.getInt(SharedKey.SCREEN_WIDTH, 480) + "*"
							+ sp.getInt(SharedKey.SCREEN_HEIGHT, 800));
			headerObject.put("version_code",
					MiscUtils.getAppVersionCode(context));
			headerObject.put("device_model", Build.MODEL);
			headerObject.put("timezone", 8);
			headerObject.put("device_name", Build.DEVICE);
			headerObject.put("req_time", duration % 500);
			headerObject.put("carrier", MiscUtils.getNetOpraterName(context));
			headerObject.put("device_board", Build.BOARD);
			headerObject.put("device_manufacturer", Build.MANUFACTURER);
			headerObject.put("os", "Android");
			headerObject.put("package_name", context.getPackageName());
			headerObject.put("cpu", Build.CPU_ABI);
			headerObject.put("sdk_version", Constant.UMNEG_SDK_VERSION);
			headerObject.put("device_id", device_id);
			headerObject.put("device_brand", Build.BRAND);
			headerObject.put("access", MiscUtils.getAPN(context));
			headerObject.put("country", MiscUtils.getCountry());
			headerObject.put("os_version", Build.VERSION.RELEASE);
			headerObject.put("idmd5",
					MD5.encrypt(device_id));
			headerObject.put("sdk_type", "Android");
			headerObject.put("device_manuid", Build.ID);
			headerObject.put("language", MiscUtils.getLanguage());
			headerObject.put("channel", String.valueOf(MiscUtils.getMetaData(
					context, MetaDataKey.HKC_CHANNEL)));
			headerObject.put("device_manutime", Build.TIME);

			jsonObject.put("body", bodyObject);
			jsonObject.put("header", headerObject);
			
			requestBody = "content="+jsonObject.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return requestBody;
	}

	@Override
	public Map<String, String> getHeaders() {
		Map<String, String> header = super.getHeaders();
		header.put("X-Umeng-Sdk", UmengUtils.getXUmengSdk(context));
		return header;
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
	public Class<UmengAnalysisResponse> getResponseClass() {
		return UmengAnalysisResponse.class;
	}
}
