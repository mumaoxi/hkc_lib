package com.hkc.utils;

import java.math.BigDecimal;
import java.util.Arrays;

public class NumUtils {

	public static String getStringNum(long num) {
		if (num >= 10000) {
			float n = num / 10000.0f;
			BigDecimal bigDecimal = new BigDecimal(n);
			n = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			return n + "万";
		}
		return num + "";
	}

	public static String genRandomNum(int length) {
		String nums = "";
		for (int i = 0; i < length; i++) {
			nums += (int) (Math.random() * 10) + "";
		}
		return nums;
	}

	/**
	 * 保留小数点后两位
	 * 
	 * @param num
	 * @param digitalLength
	 * @return
	 */
	public static float formatFloat(float num, int digitalLength) {
		BigDecimal bigDecimal = new BigDecimal(num);
		float n = bigDecimal.setScale(digitalLength, BigDecimal.ROUND_HALF_UP)
				.floatValue();
		return n;
	}

	public static Integer[] stringToIntArray(String string) {
		Integer[] intArray = new Integer[0];
		try {
			if (string != null && string.toString().startsWith("[")
					&& string.toString().endsWith("]")) {
				String[] array = string.replace("[", "").replace("]", "")
						.split(",");
				if (array != null) {
					intArray = new Integer[array.length];
					for (int i = 0; i < array.length; i++) {
						intArray[i] = 0;
						try {
							intArray[i] = Integer.valueOf(array[i].trim());
						} catch (Exception e) {
						}
					}
				}
			}
		} catch (Exception e) {
		}

		return intArray;
	}

	/**
	 * 小数点精度
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// float num = formatFloat(1.00600004f, 2);
		float num1 = 505.2f;
		float num2 = 0.3f;
		// System.out.println(num+"");
		System.out.println(0.7 + 0.1);

		int[] x = new int[2];
		x[0] = 0;
		x[1] = 1;
		int[] y = null;
		System.out.println(Arrays.toString(x));

		String string = "[0, 1,4e]";

		Integer[] testx = new Integer[0];
		if (string != null && string.toString().startsWith("[")
				&& string.toString().endsWith("]")) {
			String[] array = string.replace("[", "").replace("]", "")
					.split(",");
			if (array != null) {
				testx = new Integer[array.length];
				for (int i = 0; i < array.length; i++) {
					testx[i] = 0;
					try {
						testx[i] = Integer.valueOf(array[i].trim());
					} catch (Exception e) {
					}

				}
			}
		}

		for (int i : testx) {
			System.out.println("i:" + i);
		}

		System.out.println(Arrays.asList(testx).contains(4));
	}

}
