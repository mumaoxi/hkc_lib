package com.hkc.http;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

import com.hkc.android.volley.AuthFailureError;
import com.hkc.android.volley.NetworkResponse;
import com.hkc.android.volley.Request;
import com.hkc.android.volley.Response;
import com.hkc.android.volley.Response.ErrorListener;
import com.hkc.android.volley.Response.Listener;
import com.hkc.android.volley.toolbox.HttpHeaderParser;

public class VoRequest extends Request<NetworkResponse> {

	private final Listener<NetworkResponse> mListener;
	private Map<String, String> headers = new HashMap<String, String>();
	private Map<String, String> params = new HashMap<String, String>();
	private String paramsEncoding;
	private String bodyContentType;
	private String body;
	private String cacheKey;

	/**
	 * Creates a new request with the given method.
	 * 
	 * @param method
	 *            the request {@link Method} to use
	 * @param url
	 *            URL to fetch the string at
	 * @param listener
	 *            Listener to receive the NerworkResponse response
	 * @param errorListener
	 *            Error listener, or null to ignore errors
	 */
	public VoRequest(int method, String url,
			Listener<NetworkResponse> listener, ErrorListener errorListener) {

		super(method, url, errorListener);
		mListener = listener;
	}

	/**
	 * Creates a new GET request.
	 * 
	 * @param url
	 *            URL to fetch the string at
	 * @param listener
	 *            Listener to receive the NetworkResponse response
	 * @param errorListener
	 *            Error listener, or null to ignore errors
	 */
	public VoRequest(String url, Listener<NetworkResponse> listener,
			ErrorListener errorListener) {
		this(Method.GET, url, listener, errorListener);
	}

	@Override
	protected void deliverResponse(NetworkResponse response) {
		mListener.onResponse(response);
	}

	@Override
	protected Response<NetworkResponse> parseNetworkResponse(
			NetworkResponse response) {
		return Response.success(response,
				HttpHeaderParser.parseCacheHeaders(response));
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		return headers;
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return params;
	}

	@Override
	public String getUrl() {

		if (getMethod() == Method.GET && params != null && params.size() > 0) {
			return getOriginUrl()
					+ VoHttpUtils.genrateUrlparams(getOriginUrl(), params,
							getParamsEncoding());
		}

		return super.getUrl();
	}

	public void setBodyContentType(String bodyContentType) {
		this.bodyContentType = bodyContentType;
	}

	@Override
	public String getBodyContentType() {

		if (!TextUtils.isEmpty(bodyContentType)) {
			return bodyContentType;
		}

		return super.getBodyContentType();
	}

	public void setParamsEncoding(String paramsEncoding) {
		this.paramsEncoding = paramsEncoding;
	}

	@Override
	protected String getParamsEncoding() {

		if (!TextUtils.isEmpty(paramsEncoding)) {
			return paramsEncoding;
		}
		return super.getParamsEncoding();
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public byte[] getBody() throws AuthFailureError {
		
		if (!TextUtils.isEmpty(body)) {
			return body.getBytes();
		}
		return super.getBody();
	}

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
	}

	@Override
	public String getCacheKey() {

		if (!TextUtils.isEmpty(cacheKey)) {
			return cacheKey;
		}
		return super.getCacheKey();
	}
}
