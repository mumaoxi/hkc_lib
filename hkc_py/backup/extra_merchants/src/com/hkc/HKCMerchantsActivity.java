package com.hkc;

import org.apache.http.protocol.HTTP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.hkc.constant.CommonDefine;
import com.hkc.http.ArticleDetailRequest;
import com.hkc.http.ArticleDetailResponse;
import com.hkc.http.FreshResponse;
import com.hkc.http.VoAPI;
import com.hkc.http.VoListener;
import com.hkc.http.VoNetCenter;
import com.hkc.model.AppAdvertisement;
import com.hkc.notification.AppAdvManager;
import com.hkc.res.Hkc;
import com.hkc.res.Hkc.Rx;
import com.hkc.utils.HelperUtils;
import com.hkc.utils.Logger;
import com.hkc.utils.TextUtils;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class HKCMerchantsActivity extends Activity implements VoListener {

	private static final String TAG = HKCMerchantsActivity.class
			.getSimpleName();

	private WebView mWebView;
	private Context context;
	// Weixin
	private IWXAPI api;
	private AppAdvertisement adv;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		// 展示招商页面
		setContentView(Hkc.R(context, Rx.layout_hkc_activity_merchants));
		mWebView = (WebView) findViewById(Hkc.R(context,
				Rx.id_hkc_merchants_webview));
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.addJavascriptInterface(new WebAppInterface(context),
				"adultshop");
		adv = (AppAdvertisement) getIntent().getSerializableExtra("adv");
		if (adv != null && !TextUtils.isEmpty(adv.getDownloadUrl())) {
			// 下载宝典详情
			String article_url = adv.getDownloadUrl();
			ArticleDetailRequest request = new ArticleDetailRequest();
			request.setArticle_url(article_url);
			request.setTag(TAG);
			request.setShouldCache(true);
			VoNetCenter.doRequest(context, request, this);

			// 标记已读
			AppAdvManager.getInstance(this).setAdvReaded(adv);

			// 统计访问数
			AppAdvManager.getInstance(context).apiSendClickCount(adv);
		} else {
			mWebView.loadUrl(VoAPI.UNINSTALL_URL);
		}

		// 注册微信
		api = WXAPIFactory.createWXAPI(context, CommonDefine.WX_APP_ID, false);
		api.registerApp(CommonDefine.WX_APP_ID);
	}

	@Override
	public void onRequestFinished(FreshResponse response) {

		Logger.i(TAG, "onResponse :: response = " + response);

		if (response != null && response instanceof ArticleDetailResponse) {

			com.hkc.http.ArticleDetailResponse mResponse = (ArticleDetailResponse) response;
			Logger.e(
					TAG,
					"onRequestFinished :: body = "
							+ mResponse.getArtilce_body());

			// Success
			if (!TextUtils.isEmpty(mResponse.getArtilce_body())) {

				// Update data & UI.
				mWebView.loadDataWithBaseURL(VoAPI.API_ROOT,
						mResponse.getArtilce_body(), "text/html", HTTP.UTF_8,
						null);
			} else {
				mWebView.loadUrl(VoAPI.UNINSTALL_URL);
			}
		} else {
			mWebView.loadUrl(VoAPI.UNINSTALL_URL);
		}
	}

	public class WebAppInterface {
		Context mContext;

		/** Instantiate the interface and set the context */
		WebAppInterface(Context context) {
			mContext = context;
		}

		@JavascriptInterface
		public void openWX(String weixin_id) {
			Logger.i(TAG, "openWX :: weixin_id = " + weixin_id);
			ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			cm.setText(weixin_id.trim());
			Toast.makeText(context,
					Hkc.R(context, Rx.string_hkc_copy_success),
					Toast.LENGTH_SHORT).show();

			if (HelperUtils.isInstalledWx(mContext)) {
				api.openWXApp();
			}

			if (adv != null) {
				// 统计点击了多少次加微信妹子
				AppAdvManager.getInstance(mContext).apiSendClickCount(adv);
			}
		}
	}
}
