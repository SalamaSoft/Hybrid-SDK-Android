package com.salama.android.webcore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class WebManager {
	
	private static WebController _webController = null;

	/**
	 * 初始化本地页面
	 * @param webPackageName 本地页面压缩包名
	 * @param htmlPackageStream 本地页面压缩包InputStream
	 * @throws IOException
	 */
	public static void initWithWebPackageName(String webPackageName, InputStream htmlPackageStream) throws IOException {
		_webController = new WebController(webPackageName, htmlPackageStream);
		
	}
		
	/**
	 * 初始化本地页面
	 * @param webPackageName 本地页面压缩包名
	 * @param htmlPackageStream 本地页面压缩包InputStream
	 * @param webBaseDir 本地页面基本目录路径
	 * @throws IOException
	 */
	public static void initWithWebPackageName(String webPackageName, InputStream htmlPackageStream, File webBaseDir) throws IOException {
		_webController = new WebController(webPackageName, htmlPackageStream, webBaseDir);
	}
	
	public static void initWithExistingWebRootDir(File existingWebRootDir) {
		_webController = new WebController(existingWebRootDir);
	}
	
	/**
	 * 取得WebController
	 * @return
	 */
	public static WebController getWebController() {
		return _webController;
	}
}
