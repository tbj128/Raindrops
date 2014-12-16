package com.kinetiqa.raindrops.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Secure {
	public static String secureHash(String message)
			throws NoSuchAlgorithmException {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-512");

			md.update(message.getBytes());
			byte[] mb = md.digest();
			String out = "";
			for (int i = 0; i < mb.length; i++) {
				byte temp = mb[i];
				String s = Integer.toHexString(new Byte(temp));
				while (s.length() < 2) {
					s = "0" + s;
				}
				s = s.substring(s.length() - 2);
				out += s;
			}
			return out;
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
}
