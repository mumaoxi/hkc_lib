package com.hkc.model;

import java.io.Serializable;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class FreshObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int identifer;

	public String json;

	public FreshObject() {
	}

	public void initWithJson(JSONObject jsonObject) {
		if (jsonObject == null) {
			return;
		}
		json = jsonObject.toString();
		// Step1.Get all the keys list
		@SuppressWarnings("unchecked")
		Iterator<String> keysIterator = jsonObject.keys();

		while (keysIterator.hasNext()) {
			String key = keysIterator.next();
			try {
				this.initParamWithJsonAndKey(jsonObject, key);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 
	 * @param jsonObject
	 * @param key
	 * @throws JSONException
	 */
	public abstract void initParamWithJsonAndKey(JSONObject jsonObject,
			String key) throws JSONException;

	/**
	 * @return the identifer
	 */
	public int getIdentifer() {
		return identifer;
	}

	/**
	 * @param identifer
	 *            the identifer to set
	 */
	public void setIdentifer(int identifer) {
		this.identifer = identifer;
	}

}
