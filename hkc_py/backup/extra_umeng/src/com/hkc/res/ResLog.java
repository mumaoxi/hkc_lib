package com.hkc.res;

public class ResLog {

	private static boolean DEBUG = true;

	public static void o(String tag, String msg) {
		if (DEBUG) {
			try {
				System.out.println("Tag: " + tag + "  |----| " + msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void e(String tag, String msg) {
		if (DEBUG) {
			try {
				System.err.println("Tag: " + tag + "  |----| " + msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void t(Exception e) {
		if (DEBUG) {
			try {
				e.printStackTrace();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}
