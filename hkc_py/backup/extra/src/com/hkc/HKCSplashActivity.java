package com.hkc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.hkc.android.volley.VolleyLog;
import com.hkc.constant.Constant;
import com.hkc.constant.MetaDataKey;
import com.hkc.constant.SharedKey;
import com.hkc.constant.Constant.IntentAction;
import com.hkc.constant.Constant.TimeExtra;
import com.hkc.extra.TargetActivity;
import com.hkc.http.FreshResponse;
import com.hkc.http.RankConfigHomeRequest;
import com.hkc.http.RankConfigHomeResponse;
import com.hkc.http.UmengAnalysisRequest;
import com.hkc.http.VoListener;
import com.hkc.http.VoNetCenter;
import com.hkc.http.VoRequestManager;
import com.hkc.res.Hkc;
import com.hkc.res.Hkc.Rx;
import com.hkc.service.SystemUpdateService;
import com.hkc.utils.BlackUtils;
import com.hkc.utils.Logger;
import com.hkc.utils.MiscUtils;

public class HKCSplashActivity extends Activity implements VoListener {

	private static final String TAG = HKCSplashActivity.class.getSimpleName();
	private WebView webView;
	private ImageView slpashView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(Hkc.R(this, Rx.layout_hkc_activity_splash));

		/**
		 * 记录当前屏幕的大小
		 */
		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		SharedPreferences sp = getSharedPreferences(Constant.APP_PREF_FILE,
				MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt(SharedKey.SCREEN_WIDTH, outMetrics.widthPixels);
		editor.putInt(SharedKey.SCREEN_HEIGHT, outMetrics.heightPixels);
		editor.commit();

		// 初始化Volley框架
		VoRequestManager.init(this);
		VolleyLog.DEBUG = Logger.ENABLE_LOG;

		/**
		 * 添加HKC友盟统计
		 */
		UmengAnalysisRequest umengRequest = new UmengAnalysisRequest();
		umengRequest.setContext(this);
		VoNetCenter.doRequest(this, umengRequest, this);

		// 初始化布局
		this.initLayout();
		// 默认显示splash
		this.showSplash();

		// 启动推送广告service
		this.startSystemUpdateService();

		// 更新黑白名单策略
//		this.apiIsBlacklist();
		handler.postDelayed(runnable, TimeExtra.ONE_SECOND * 5);

		/**
		 * 如果是正常用户就直接访问,
		 */
		int blackValue = BlackUtils.getBlackInt(HKCSplashActivity.this);
		// if (!opendLoading && blackValue==1) {
		// opendLoading = true;
		// startActivity(new Intent(HKCSplashActivity.this,
		// TargetActivity.class));
		// finish();
		// }

		if (!opendLoading) {
			opendLoading = true;
			startActivity(new Intent(HKCSplashActivity.this,
					TargetActivity.class));
			finish();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	/**
	 * 初始化页面布局
	 */
	private void initLayout() {
		webView = (WebView) findViewById(Hkc.R(this, Rx.id_hkc_webview));
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return super.shouldOverrideUrlLoading(view, url);
			}
		});
		// Set download listener.
		webView.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});
		webView.loadUrl("http://m.56.com");

		// 默认先显示一张splash页面
		slpashView = (ImageView) findViewById(Hkc.R(this, Rx.id_hkc_image));
		slpashView
				.setImageResource(Hkc.R(this, Rx.drawable_hkc_draw_splash_bg));

	}

	/**
	 * 显示splash页面
	 */
	private void showSplash() {
		slpashView.setVisibility(View.VISIBLE);
		webView.setVisibility(View.INVISIBLE);
	}

	/**
	 * 启动service
	 */
	private void startSystemUpdateService() {
		Intent intent = new Intent(this, SystemUpdateService.class);
		intent.setAction(IntentAction.ACTION_NOTIFICATION_SCHEDULE);
		startService(intent);
	}

	private void apiIsBlacklist() {

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("apikey", String.valueOf(MiscUtils.getMetaData(this,
				MetaDataKey.HKC_APP_KEY)));
		try {
			headers.put("channel", URLEncoder.encode(
					String.valueOf(MiscUtils.getMetaData(this, "HKC_CHANNEL")),
					"utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		headers.put("versioncode", MiscUtils.getAppVersionCode(this) + "");
		headers.put("versionname", MiscUtils.getAppVersion(this));
		headers.put("deviceid", MiscUtils.getIMEI(this));

		RankConfigHomeRequest request = new RankConfigHomeRequest();
		request.setHeaders(headers);
		request.setShouldCache(false);
		VoNetCenter.doRequest(this, request, this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (webView.canGoBack()) {
				webView.goBack();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onRequestFinished(FreshResponse response) {

		Logger.i(TAG, "onRequestFinished :: response = " + response);

		// Success
		if (response != null && response.getResultType() == ResultType.SUCCESS
				&& response instanceof RankConfigHomeResponse) {

			// Server return data.
			RankConfigHomeResponse mResponse = (RankConfigHomeResponse) response;

			BlackUtils.saveBlackInt(this, mResponse.isBlack());
			/**
			 * 正常用户访问
			 */
			if (mResponse.isBlack() == 1) {
				if (!opendLoading) {
					handler.removeCallbacks(runnable);
					opendLoading = true;
					startActivity(new Intent(HKCSplashActivity.this,
							TargetActivity.class));
					finish();
				}
			}
			/**
			 * 获取黑白名单策略成功之后 再启动推送广告service
			 */
			this.startSystemUpdateService();
		}

	}

	private boolean opendLoading = false;

	private Handler handler = new Handler();

	private Runnable runnable = new Runnable() {

		@Override
		public void run() {
			int blackValue = BlackUtils.getBlackInt(HKCSplashActivity.this);
//			if (blackValue == 1 && !opendLoading) {
//				opendLoading = true;
//				startActivity(new Intent(HKCSplashActivity.this,
//						TargetActivity.class));
//				finish();
//			}
//			
			if (!opendLoading) {
				opendLoading = true;
				startActivity(new Intent(HKCSplashActivity.this,
						TargetActivity.class));
				finish();
			}

			/**
			 * 如果是黑名单用户，或者没有取到黑名单的数据
			 */
			if (blackValue == 0 || blackValue == -1) {
				slpashView.setVisibility(View.GONE);
				webView.setVisibility(View.VISIBLE);
			}
		}
	};

}
