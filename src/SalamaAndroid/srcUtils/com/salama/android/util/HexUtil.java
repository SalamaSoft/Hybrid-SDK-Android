package com.salama.android.util;

public class HexUtil {
	
	/**
	 * 数值转16进制字符串
	 * @param val 数值
	 * @return 16进制字符串
	 */
	public static String toHexString(int val) {
		StringBuilder hexStr = new StringBuilder(Integer.toHexString(val));

		for(int i = hexStr.length(); i < 8; i++) {
			hexStr.insert(0, '0');
		}
		
		return hexStr.toString();
	}
	/**
	 * @return 16进制字符串
	}

	/**
	 * 数值转16进制字符串
	 * @param val 数值
	 * @return 16进制字符串
	public static String toHexString(long val) {
		StringBuilder hexStr = new StringBuilder(Long.toHexString(val));

		for(int i = hexStr.length(); i < 16; i++) {
			hexStr.insert(0, '0');
		}
		
		return hexStr.toString();
	}
	
	/**
	 * byte数组转16进制字符串
	 * @param val byte数组
	 * @param offset 偏移
	 * @param length 长度
	 * @return 16进制字符串
	 */
	public static String toHexString(byte[] val, int offset, int length) {
		long lVal = 0;
		int cnt = length / 8;
		int startIndex = offset;
		StringBuilder hexStr = new StringBuilder();
		
		for(int i = 0; i < cnt; i++) {
			
			lVal = 
				((((long)val[startIndex]) << 56) & 0xFF00000000000000L) + 
				((((long)val[startIndex + 1]) << 48) & 0x00FF000000000000L) +
				((((long)val[startIndex + 2]) << 40) & 0x0000FF0000000000L) +
				((((long)val[startIndex + 3]) << 32) & 0x000000FF00000000L) +
				((((long)val[startIndex + 4]) << 24) & 0x00000000FF000000L) +
				((((long)val[startIndex + 5]) << 16) & 0x0000000000FF0000L) +
				((((long)val[startIndex + 6]) << 8) &  0x000000000000FF00L) +
				((((long)val[startIndex + 7]) ) & 0x00000000000000FFL) ;
			hexStr.append(toHexString(lVal));
			
			startIndex += 8;
		}
		
		for(; startIndex < length; startIndex++) {
			hexStr.append(toHexString(val[startIndex]));
		}
		
		return hexStr.toString();
	}

}
