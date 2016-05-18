package com.hkc.utils;

import java.util.Arrays;
import java.util.List;

public class TextUtils {

	public static boolean isEmpty(CharSequence str) {
		if (str == null || str.length() == 0
				|| str.toString().toLowerCase().trim().equals("null"))
			return true;
		else
			return false;
	}

	public static String deleteAllSpace(String text) {
		try {
			return text.replaceAll(" ", "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

	/**
	 * 升序排列
	 * 
	 * @param lists
	 * @return
	 */
	public static String ascList(List<String> lists) {
		try {
			String temp;
			for (int i = 0; i < lists.size(); i++) {
				for (int j = i; j < lists.size(); j++) {
					if (lists.get(i).compareTo(lists.get(j)) > 0) {
						temp = lists.get(i);
						lists.set(i, lists.get(j));
						lists.set(j, temp);
					}
				}
			}
			StringBuffer sBuffer = new StringBuffer();
			for (int i = 0; i < lists.size(); i++) {
				sBuffer.append(lists.get(i));
			}
			return sBuffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String encodeS(String origString, int secret) {
		try {
			byte[] origBytes = origString.getBytes("utf-8");

			byte[] bytes = new byte[origBytes.length];
			if (origString != null) {
				int index = 0;
				for (int i = 0; i < origString.length(); i++) {
					char c = origString.charAt(i);
					byte[] charBytes = String.valueOf(c).getBytes();
					if (charBytes.length > 1) {
						for (int j = 0; j < charBytes.length; j++) {
							bytes[index] = (byte)(secret ^charBytes[j]);
							index++;
						}
					} else {
						bytes[index] = (byte) (secret ^ c);
						index++;
					}

				}
			}
			return Arrays.toString(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
