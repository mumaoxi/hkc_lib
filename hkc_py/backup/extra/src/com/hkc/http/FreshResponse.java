package com.hkc.http;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.protocol.HTTP;

import com.hkc.android.volley.NetworkResponse;
import com.hkc.http.VoListener.ResultType;

public abstract class FreshResponse {

	private static final String DEFAULT_RESPONSE_CHARSET = HTTP.UTF_8;
	private String body;
	private Map<String, String> headers = new HashMap<String, String>();
	private int statusCode;
	private ResultType resultType;
	private String tag;

	public String getBody() {
		return body;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getResponseEncoding() {
		return DEFAULT_RESPONSE_CHARSET;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public abstract void parseResponseBody();

	public void parserNetworkResponse(NetworkResponse response) {

		if (response != null) {
			statusCode = response.statusCode;
			headers = response.headers;
		}
	}

	public void parserResponseData(byte[] data) {

		if (data != null && data.length > 0) {
			try {
				body = new String(data, getResponseEncoding());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	public void setResultType(ResultType resultType) {
		this.resultType = resultType;
	}

	public ResultType getResultType() {
		return resultType;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}

	@Override
	public String toString() {
		return "FreshResponse [body=" + body + ", headers=" + headers
				+ ", statusCode=" + statusCode + ", resultType=" + resultType
				+ ", tag=" + tag + "]";
	}

}
