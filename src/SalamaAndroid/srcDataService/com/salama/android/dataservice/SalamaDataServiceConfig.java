package com.salama.android.dataservice;

public class SalamaDataServiceConfig {
	private int httpRequestTimeout;
	
	private String resourceStorageDirPath;
	
	private String dbName;
	
	//private String dbDirPath;

	/**
	 * 取得请求timeout秒数
	 * @return timeout 请求timeout秒数
	 */
	public int getHttpRequestTimeout() {
		return httpRequestTimeout;
	}

	/**
	 * 设置请求timeout秒数
	 * @param httpRequestTimeout 请求timeout秒数
	 */
	public void setHttpRequestTimeout(int httpRequestTimeout) {
		this.httpRequestTimeout = httpRequestTimeout;
	}

	/**
	 * 取得资源文件保存目录路径
	 * @return 资源文件保存目录路径
	 */
	public String getResourceStorageDirPath() {
		return resourceStorageDirPath;
	}

	/**
	 * 设置资源文件保存目录路径
	 * @param resourceStorageDirPath 资源文件保存目录路径
	 */
	public void setResourceStorageDirPath(String resourceStorageDirPath) {
		this.resourceStorageDirPath = resourceStorageDirPath;
	}

	/**
	 * 取得数据库文件名
	 * @return 数据库文件名
	 */
	public String getDbName() {
		return dbName;
	}

	/**
	 * 设置数据库文件名
	 * @param dbName 数据库文件名
	 */
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

//	public String getDbDirPath() {
//		return dbDirPath;
//	}
//
//	public void setDbDirPath(String dbDirPath) {
//		this.dbDirPath = dbDirPath;
//	}
	
	
}
