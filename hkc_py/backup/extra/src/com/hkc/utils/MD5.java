package com.hkc.utils;

import java.security.MessageDigest;

public class MD5 {

	/**
	 * 加密字符串
	 * 
	 * @param string
	 * @return
	 */
	public static String encrypt(String string) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(string.getBytes());
			byte[] m = md5.digest();// 加密
			return getString(m);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private static String getString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xff;
			if (v < 16) {
				sb.append(0);
			} else
				sb.append(Integer.toHexString(v));
		}
		return sb.toString();
	}
}
