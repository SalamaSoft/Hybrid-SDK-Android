package com.salama.android.util;

import android.util.Log;

public class SSLog {

	public static final int SSLogLevelDebug = 0;
	public static final int SSLogLevelInfo = 1;
	public static final int SSLogLevelWarn = 2;
	public static final int SSLogLevelError = 3;
	
	private static int _logLevel = SSLogLevelDebug;
	
	/**
	 * 设置日志输出Level
	 * @param logLevel 日志level(SSLogLevelDebug,SSLogLevelInfo,SSLogLevelWarn,SSLogLevelError)
	 */
	public static void setSSLogLevel(int logLevel) {
		_logLevel = logLevel;
	}
	
	public static int getSSLogLevel() {
		return _logLevel;
	}

	public static void d(String tag, String msg) {
		if(_logLevel <= SSLogLevelDebug) {
			Log.d(tag, msg);
		}
	}
	
	public static void d(String tag, String msg, Throwable tr) {
		if(_logLevel <= SSLogLevelDebug) {
			Log.d(tag, msg, tr);
		}
	}

	public static void i(String tag, String msg) {
		if(_logLevel <= SSLogLevelInfo) {
			Log.i(tag, msg);
		}
	}
	
	public static void i(String tag, String msg, Throwable tr) {
		if(_logLevel <= SSLogLevelInfo) {
			Log.i(tag, msg, tr);
		}
	}

	public static void w(String tag, String msg) {
		if(_logLevel <= SSLogLevelWarn) {
			Log.w(tag, msg);
		}
	}
	
	public static void w(String tag, String msg, Throwable tr) {
		if(_logLevel <= SSLogLevelWarn) {
			Log.w(tag, msg, tr);
		}
	}

	public static void e(String tag, String msg) {
		Log.e(tag, msg);
	}
	
	public static void e(String tag, String msg, Throwable tr) {
		Log.e(tag, msg, tr);
	}
}
