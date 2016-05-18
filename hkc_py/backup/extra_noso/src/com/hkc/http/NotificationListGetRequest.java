package com.hkc.http;

import com.hkc.constant.ApiURL;


public class NotificationListGetRequest extends
		FreshRequest<NotificationListGetResponse> {


	@Override
	public String getApi() {
		return ApiURL.NOTIFICATION_LIST;
	}
	
	@Override
	public Class<NotificationListGetResponse> getResponseClass() {
		return NotificationListGetResponse.class;
	}
}
