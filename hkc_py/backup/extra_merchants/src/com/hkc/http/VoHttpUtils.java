package com.hkc.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import com.hkc.utils.Logger;
import com.hkc.utils.TextUtils;

public class VoHttpUtils {

	private static final String TAG = VoHttpUtils.class.getSimpleName();

	/**
	 * 
	 * @param url
	 * @param params
	 * @param charsetName
	 * @return
	 */
	public static String genrateUrlparams(String url,
			Map<String, String> params, String charsetName) {

		if (TextUtils.isEmpty(url)) {
			Logger.w(TAG, "genrateUrlparams :: url is empty");
			return "";
		}

		if (params == null || params.size() < 1) {
			Logger.w(TAG, "genrateUrlparams :: params is empty");
			return "";
		}

		StringBuffer urlString = new StringBuffer("");
		if (url.contains("?")) {
			urlString = new StringBuffer("&");
		} else {
			urlString = new StringBuffer("?");
		}
		try {
			for (Entry<String, String> param : params.entrySet()) {
				urlString.append(param.getKey() + "="
						+ URLEncoder.encode(param.getValue(), charsetName)
						+ "&");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Logger.e(TAG,
					"genrateUrlparams :: UnsupportedEncodingException Erroe => "
							+ e.getMessage());
		}

		urlString = new StringBuffer(urlString.reverse().toString()
				.replaceFirst("&", ""));
		return urlString.reverse().toString();
	}

}
