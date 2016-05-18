package com.hkc.http;

import com.hkc.android.volley.Request.Method;

/**
 * 获取文章详情 GET
 * 
 * @author tustar
 * 
 */
public class ArticleDetailRequest extends FreshRequest<ArticleDetailResponse> {

	private String article_url;

	@Override
	public String getApi() {
		return null;
	}

	@Override
	public String getUrl() {
		return article_url;
	}

	@Override
	public int getMethod() {
		return Method.GET;
	}

	@Override
	public Class<ArticleDetailResponse> getResponseClass() {
		return ArticleDetailResponse.class;
	}

	public void setArticle_url(String article_url) {
		this.article_url = article_url;
	}
}
