package com.kinetiqa.raindrops.util;

public class RandomAlphaNumeric {
	private static final String ALPHA_NUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public static String getAlphaNumeric(int len) {
		StringBuffer sb = new StringBuffer(len);
		for (int i = 0; i < len; i++) {
			int ndx = (int) (Math.random() * ALPHA_NUM.length());
			sb.append(ALPHA_NUM.charAt(ndx));
		}
		return sb.toString();
	}
}
