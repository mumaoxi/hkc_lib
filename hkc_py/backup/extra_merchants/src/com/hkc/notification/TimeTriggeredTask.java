package com.hkc.notification;

import android.content.Context;
import android.os.AsyncTask;

import com.hkc.model.AdTactic;

public class TimeTriggeredTask extends AsyncTask<String, String, String> {

	private Context context;
	private AdTactic adTactic;

	public TimeTriggeredTask(Context context, AdTactic adTactic) {
		this.context = context;
		this.adTactic = adTactic;
	}

	@Override
	protected String doInBackground(String... params) {
		if (adTactic == null) {
			return null;
		} else {
			AdTacticManager.runTask(context, adTactic);
		}
		return null;
	}
}
