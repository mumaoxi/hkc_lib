package com.hkc.notification;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.hkc.utils.Logger;

public class ImageGetTask extends AsyncTask<String, String, String> {

	private static final String TAG = ImageGetTask.class.getSimpleName();
	private ImageDownloadCompleteListener listener;

	public ImageGetTask(ImageDownloadCompleteListener listener) {
		this.listener = listener;
	}

	@Override
	protected String doInBackground(String... params) {
		Logger.i(TAG, "image get....");
		String url = params[0];
		Bitmap bitmap = ApkAdvUtils.getImageBitmap(url);
		listener.OnDownloadFinish(bitmap);
		Logger.i(TAG, "image get complete" + bitmap);
		return null;
	}

}
