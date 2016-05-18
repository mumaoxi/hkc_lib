package com.hkc.http;


public class UpdateInfoResponse extends FreshResponse {

	private boolean  success = false;

	@Override
	public void parseResponseBody() {

		if (getStatusCode() == 201) {
			success = true;
		}
	}

	public boolean isSuccess() {
		return success;
	}
}
