/**
 * 
 */
package com.hkc.http;


/**
 * @author Stream
 * 
 */
public interface VoListener {

	public void onRequestFinished(FreshResponse response);

	public enum ResultType {

		SUCCESS("fresh_state_success"), 
		REQUEST_TIMEOUT("fresh_state_request_time_out"), 
		NO_NETWORK("fresh_state_no_network"), 
		CLIENT_ERROR("fresh_state_client_error"), 
		SERVER_ERROR("fresh_state_server_error"), 
		UNKNOWN_ERROR("fresh_state_unknown_error"), 
		GATEWAY_TIMEOUT("fresh_state_gateway_timeout");

		public final String resName;

		ResultType(String resName) {
			this.resName = resName;
		}

	}
}
