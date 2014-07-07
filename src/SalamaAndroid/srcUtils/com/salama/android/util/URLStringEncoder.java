package com.salama.android.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import android.util.Log;

public class URLStringEncoder {
	public static final String DefaultEncoding = "utf-8";
	
	/**
	 * 解码URL(百分号编码，和JavaScript的百分号编码一致，具体参考W3C的相关资料)
	 * @param urlStr URL字符串
	 * @param 解码后的URL
	 */
	public static String decodeURLString(String urlStr) {
		try {
			return URLDecoder.decode(urlStr.replaceAll("%20", "\\+"), DefaultEncoding);
		} catch (UnsupportedEncodingException e) {
			Log.e("URLStringEncoder", "decodeURLString()", e);
			return "";
		}	
	}

	/**
	 * 编码URL(百分号编码，和JavaScript的百分号编码一致，具体参考W3C的相关资料)
	 * @param urlStr URL字符串
	 * @param 编码后的URL
	 */
	public static String encodeURLString(String urlStr) {
		try {
			return URLEncoder.encode(urlStr, DefaultEncoding).replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			Log.e("URLStringEncoder", "encodeURLString()", e);
			return "";
		}	
	}
}
