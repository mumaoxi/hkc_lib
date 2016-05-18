package com.hkc.http;



public class NotificationListGetRequest extends
		FreshRequest<NotificationListGetResponse> {


	@Override
	public String getApi() {
		return VoAPI.NOTIFICATION_LIST;
	}
	
	@Override
	public Class<NotificationListGetResponse> getResponseClass() {
		return NotificationListGetResponse.class;
	}
}
