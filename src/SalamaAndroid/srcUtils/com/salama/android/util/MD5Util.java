package com.salama.android.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
	
	/**
	 * 生成MD5 Hash编码
	 * @param inputStr 原始字符串
	 * @return MD5 Hash编码(16进制字符串)
	 */
	public static String md5String(String inputStr) {
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		byte[] md5bytes = md5.digest(inputStr.getBytes());
		
		return HexUtil.toHexString(md5bytes);
	}
}
