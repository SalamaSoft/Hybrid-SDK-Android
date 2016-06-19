package com.salama.android.util;

public class HexUtil {
	
	/**
	 * 数值转16进制字符串
	 * @param val 数值
	 * @return 16进制字符串
	 */	
	public static String toHexString(byte val) {
		StringBuilder hexStr = new StringBuilder(Integer.toHexString(val));
		if(hexStr.length() < 2) {
			hexStr.insert(0, '0');
		}
		
		return hexStr.substring(hexStr.length() - 2);
	}

	/**
	 * 数值转16进制字符串
	 * @param val 数值
	 * @return 16进制字符串
	 */	
	public static String toHexString(short val) {
		StringBuilder hexStr = new StringBuilder(Integer.toHexString(val));

		for(int i = hexStr.length(); i < 4; i++) {
			hexStr.insert(0, '0');
		}
		
		return hexStr.substring(hexStr.length() - 4);
	}

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
	 * 数值转16进制字符串
	 * @param val 数值
	 * @return 16进制字符串
	 */	
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
	 * @return 16进制字符串
	 */
	public static String toHexString(byte[] val) {
		return toHexString(val, 0, val.length);
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

	/**
	 * 16进制字符串转byte数组
	 * @param hexStr 16进制字符串
	 * @return
	 */
	public static byte[] parseHexString(String hexStr) {
		int byteLen = hexStr.length() / 2;
		byte[] buffer = new byte[byteLen];
		
		parseHexString(hexStr, buffer, 0);
		
		return buffer;
	}

	/**
	 * 16进制字符串转byte数组
	 * @param hexStr 16进制字符串
	 * @param destBuff 目标数组
	 * @param offset 目标数组偏移
	 */
	public static void parseHexString(String hexStr, byte[] destBuff, int offset) {
		int byteMax = offset + hexStr.length() / 2;
		int beginIndex = 0;
		for(int i = offset; i < byteMax; i++) {
			destBuff[i] = (byte)(
					hexCharToByte(hexStr.charAt(beginIndex + 1)) 
					| ((hexCharToByte(hexStr.charAt(beginIndex)) << 4) & 0xf0)
					);
			
			beginIndex += 2;
		}
	}

	/**
	 * 16进制字符转数值
	 * @param c 16进制字符
	 * @return 数值
	 */
	public static byte hexCharToByte(char c) {
		if(c <= '9' && c >= '0') {
			return (byte)(c - '0');
		} else {
			if(c >= 'a' && c <= 'f') {
				return (byte)(c - 'a' + 10);
			} else if(c >= 'A' && c <= 'F') {
				return (byte)(c - 'A' + 10);
			} else {
				return 0;
			}
		}
	}
	
	private static long[] BIT_AND_MASK_F_FOR_LONG = new long[]{
		0xFL, 
		0xF0L, 
		0xF00L, 
		0xF000L, 
		0xF0000L, 
		0xF00000L, 
		0xF000000L, 
		0xF0000000L, 
		0xF00000000L, 
		0xF000000000L, 
		0xF0000000000L, 
		0xF00000000000L, 
		0xF000000000000L, 
		0xF0000000000000L, 
		0xF00000000000000L,
		0xF000000000000000L
	};
	
	/**
	 * 16进制字符串转数值
	 * @param hex 16进制字符串
	 * @return 数值
	 */
	public static long hexToLong(String hex) {
		int len = hex.length();
		
		long val = 0;
		char c;
		int bitOffset = 0;
		int index = len - 1;
		for(int i = 0; i < len; i++) {
			c = hex.charAt(index);
			val |= ((hexCharToLong(c) << bitOffset) & BIT_AND_MASK_F_FOR_LONG[i]);
			
			index --;
			bitOffset +=4;
		}

		return val;
	}
	
	/**
	 * 16进制字符转数值
	 * @param c 16进制字符
	 * @return 数值
	 */
	public static long hexCharToLong(char c) {
		if(c <= '9' && c >= '0') {
			return (long)(c - '0');
		} else {
			if(c >= 'a' && c <= 'f') {
				return (long)(c - 'a' + 10);
			} else if(c >= 'A' && c <= 'F') {
				return (long)(c - 'A' + 10);
			} else {
				return 0;
			}
		}
	}

}
