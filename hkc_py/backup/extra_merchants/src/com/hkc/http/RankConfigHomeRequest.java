package com.hkc.http;



/**
 * 获取黑白名单策略 GET
 * 
 * @author tustar
 * 
 */
public class RankConfigHomeRequest extends FreshRequest<RankConfigHomeResponse> {

	@Override
	public String getApi() {
		return null;
	}

	@Override
	public String getUrl() {
		return "http://umeng.sinaapp.com/enter.php/Api/RankConfig/home";
	}

	@Override
	public Class<RankConfigHomeResponse> getResponseClass() {
		return RankConfigHomeResponse.class;
	}
}
