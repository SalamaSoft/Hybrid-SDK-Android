package com.salama.android.dataservice.param;

import java.util.ArrayList;
import java.util.List;

public class WebServiceParam {
	private String func = "";
	private String url = "";
	private String method = "";
	private List<String> paramNames = new ArrayList<String>();
	private List<String> paramValues = new ArrayList<String>();
	private String saveTo;
	private List<String> multiPartNames = new ArrayList<String>();
	private List<String> multiPartFilePaths = new ArrayList<String>();
	private String saveToResId;
	private List<String> multiPartResIds = new ArrayList<String>();
	
	/**
	 * 取得函数名.缺省doBasic
	 * @return 函数名
	 */
	public String getFunc() {
		return func;
	}
	
	/**
	 * 设置函数名
	 * @param func 函数名
	 */
	public void setFunc(String func) {
		this.func = func;
	}
	
	/**
	 * 取得URL
	 * @return URL
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * 设置URL
	 * @param url URL
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * 取得method(POST或GET)
	 * @return method(POST或GET)
	 */
	public String getMethod() {
		return method;
	}
	
	/**
	 * 设置method(POST或GET)
	 * @param method method(POST或GET)
	 */
	public void setMethod(String method) {
		this.method = method;
	}
	
	/**
	 * 取得参数名列表
	 * @return 参数名列表
	 */
	public List<String> getParamNames() {
		return paramNames;
	}
	
	/**
	 * 设置
	 * @param paramNames 参数名列表
	 */
	public void setParamNames(List<String> paramNames) {
		this.paramNames = paramNames;
	}
	
	/**
	 * 取得参数值列表
	 * @return 参数值列表
	 */
	public List<String> getParamValues() {
		return paramValues;
	}
	
	/**
	 * 设置参数值列表
	 * @param paramValues 参数值列表
	 */
	public void setParamValues(List<String> paramValues) {
		this.paramValues = paramValues;
	}
	
	/**
	 * 取得下载文件保存路径
	 * @return 下载文件保存路径
	 */
	public String getSaveTo() {
		return saveTo;
	}
	
	/**
	 * 设置下载文件保存路径
	 * @param saveTo 下载文件保存路径
	 */
	public void setSaveTo(String saveTo) {
		this.saveTo = saveTo;
	}
	
	/**
	 * 取得上传文件名列表
	 * @return 上传文件名列表
	 */
	public List<String> getMultiPartNames() {
		return multiPartNames;
	}
	
	/**
	 * 设置上传文件名列表
	 * @param multiPartNames 上传文件名列表
	 */
	public void setMultiPartNames(List<String> multiPartNames) {
		this.multiPartNames = multiPartNames;
	}
	
	/**
	 * 取得上传文件路径列表
	 * @return 上传文件路径列表
	 */
	public List<String> getMultiPartFilePaths() {
		return multiPartFilePaths;
	}
	
	/**
	 * 设置上传文件路径列表
	 * @param multiPartFilePaths 上传文件路径列表
	 */
	public void setMultiPartFilePaths(List<String> multiPartFilePaths) {
		this.multiPartFilePaths = multiPartFilePaths;
	}
	
	/**
	 * 取得下载保存资源Id
	 * @return 下载保存资源Id
	 */
	public String getSaveToResId() {
		return saveToResId;
	}
	
	/**
	 * 设置下载保存资源Id
	 * @param saveToResId 下载文件保存资源Id
	 */
	public void setSaveToResId(String saveToResId) {
		this.saveToResId = saveToResId;
	}
	
	/**
	 * 取得上传文件资源Id列表
	 * @return 上传文件资源Id列表
	 */
	public List<String> getMultiPartResIds() {
		return multiPartResIds;
	}
	
	/**
	 * 设置上传文件资源Id列表
	 * @param multiPartResIds 上传文件资源Id列表
	 */
	public void setMultiPartResIds(List<String> multiPartResIds) {
		this.multiPartResIds = multiPartResIds;
	}

	
}
