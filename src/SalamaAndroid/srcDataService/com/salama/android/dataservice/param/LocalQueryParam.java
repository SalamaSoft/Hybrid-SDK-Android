package com.salama.android.dataservice.param;

public class LocalQueryParam {
	private String sql;
	
	private String dataClass;
	
	private String resourceNames;
	
	private String resourceDownloadNotification;

	/**
	 * 取得SQL文
	 * @return SQL文
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * 设置SQL文
	 * @param sql SQL文
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * 取得数据类型
	 * @return 数据类型
	 */
	public String getDataClass() {
		return dataClass;
	}

	/**
	 * 设置数据类型
	 * @param dataClass 数据类型
	 */
	public void setDataClass(String dataClass) {
		this.dataClass = dataClass;
	}

	/**
	 * 取得资源字段名(逗号分隔)
	 * @return 资源字段名(逗号分隔)
	 */
	public String getResourceNames() {
		return resourceNames;
	}

	/**
	 * 设置资源字段名(逗号分隔)
	 * @param resourceNames 资源字段名(逗号分隔)
	 */
	public void setResourceNames(String resourceNames) {
		this.resourceNames = resourceNames;
	}

	/**
	 * 取得资源下载通知名
	 * @return 资源下载通知名
	 */
	public String getResourceDownloadNotification() {
		return resourceDownloadNotification;
	}

	/**
	 * 设置资源下载通知名
	 * @param resourceDownloadNotification 资源下载通知名
	 */
	public void setResourceDownloadNotification(String resourceDownloadNotification) {
		this.resourceDownloadNotification = resourceDownloadNotification;
	}
	
	

}
