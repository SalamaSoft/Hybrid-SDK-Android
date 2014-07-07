package com.salama.android.dataservice.param;

public class DataQueryParam {

	private WebServiceParam webService;
	
	private LocalStorageParam localStorage;
	
	private LocalQueryParam localQuery;

	/**
	 * 取得WebService参数
	 * @return
	 */
	public WebServiceParam getWebService() {
		return webService;
	}

	/**
	 * 设置WebService参数
	 * @param webService
	 */
	public void setWebService(WebServiceParam webService) {
		this.webService = webService;
	}

	/**
	 * 取得本地存储参数
	 * @return
	 */
	public LocalStorageParam getLocalStorage() {
		return localStorage;
	}

	/**
	 * 设置本地存储参数
	 * @param localStorage
	 */
	public void setLocalStorage(LocalStorageParam localStorage) {
		this.localStorage = localStorage;
	}

	/**
	 * 取得本地查询参数
	 * @return
	 */
	public LocalQueryParam getLocalQuery() {
		return localQuery;
	}

	/**
	 * 设置本地查询参数
	 * @param localQuery
	 */
	public void setLocalQuery(LocalQueryParam localQuery) {
		this.localQuery = localQuery;
	}
	
	
}
