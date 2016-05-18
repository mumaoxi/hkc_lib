package com.hkc.notification;

public interface ApkDownloadCompelteListener {
	public void OnApkDownloadCompelte(boolean isSuccess, String filePath,
			String packageName);
}