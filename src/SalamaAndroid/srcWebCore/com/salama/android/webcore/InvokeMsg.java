package com.salama.android.webcore;

import java.util.ArrayList;
import java.util.List;

import MetoXML.XmlDeserializer;
import android.util.Log;

import com.salama.android.support.ServiceSupportApplication;
import com.salama.android.util.SSLog;
import com.salama.android.util.URLStringEncoder;

public class InvokeMsg {

	private String _target;
	private String _method;
	private List<String> _params;
	private String _callBackWhenSucceed;
	private String _callBackWhenError;
	private boolean _isAsync;
	private String _returnValueKeeper;
	private String _keeperScope;
	private String _notification;
	
	/**
	 * 取得目标对象名
	 * @return
	 */
	public String getTarget() {
		return _target;
	}
	
	/**
	 * 设置目标对象名
	 * @param target
	 */
	public void setTarget(String target) {
		_target = target;
	}
	
	/**
	 * 取得方法名
	 * @return 方法名
	 */
	public String getMethod() {
		return _method;
	}
	
	/**
	 * 设置方法名
	 * @param method
	 */
	public void setMethod(String method) {
		_method = method;
	}
	
	/**
	 * 取得参数列表
	 * @return
	 */
	public List<String> getParams() {
		return _params;
	}

	/**
	 * 设置参数列表
	 * @param params
	 */
	public void setParams(List<String> params) {
		_params = params;
	}
	
	/**
	 * 取得操作成功时回调函数名 
	 * @return
	 */
	public String getCallBackWhenSucceed() {
		return _callBackWhenSucceed;
	}

	/**
	 * 设置操作成功时回调函数名
	 * @param callBackWhenSucceed
	 */
	public void setCallBackWhenSucceed(String callBackWhenSucceed) {
		_callBackWhenSucceed = callBackWhenSucceed;
	}
	
	/**
	 * 取得操作出错时回调函数名
	 * @return
	 */
	public String getCallBackWhenError() {
		return _callBackWhenError;
	}
	
	/**
	 * 设置操作出错时回调函数名
	 * @param callBackWhenError
	 */
	public void setCallBackWhenError(String callBackWhenError) {
		_callBackWhenError = callBackWhenError;
	}
	
	/**
	 * 取得是否异步方式执行
	 * @return
	 */
	public boolean isAsync() {
		return _isAsync;
	}
	
	/**
	 * 设置是否异步方式执行
	 * @param isAsync
	 */
	public void setAsync(boolean isAsync) {
		_isAsync = isAsync;
	}
	
	/**
	 * 取得返回值保存变量名
	 * @return
	 */
	public String getReturnValueKeeper() {
		return _returnValueKeeper;
	}
	
	/**
	 * 设置返回值保存变量名
	 * @param returnValueKeeper
	 */
	public void setReturnValueKeeper(String returnValueKeeper) {
		_returnValueKeeper = returnValueKeeper;
	}
	
	/**
	 * 取得返回值保存变量范围
	 * @return
	 */
	public String getKeeperScope() {
		return _keeperScope;
	}
	
	/**
	 * 设置返回值保存变量范围
	 * @param keeperScope
	 */
	public void setKeeperScope(String keeperScope) {
		_keeperScope = keeperScope;
	}

	public String getNotification() {
		return _notification;
	}

	/**
	 * notification名。如果非空，则忽略callBackWhenSucceed以及callBackWhenError。调用完成时，将发送通知，调用的返回值通过通知的数据参数传递。
	 * @return
	 */
	public void setNotification(String notification) {
		_notification = notification;
	}

	/**
	 * 创建InvokeMsg
	 * @param xml InvokeMsg的Xml内容
	 * @return InvokeMsg或List<InvokeMsg>
	 */
	public static Object invokeMsgWithXml(String xml) {
		try {
			SSLog.d("InvokeMsg", "invokeMsgWithXml() " + xml);
			
			int indexfirstTag = xml.indexOf('<');
			if(xml.indexOf("<List>") == indexfirstTag) {
				//List
				return XmlDeserializer.stringToObject(xml, ArrayList.class, 
						ServiceSupportApplication.singleton());
			} else {
				//One msg
				return XmlDeserializer.stringToObject(xml, InvokeMsg.class, 
						ServiceSupportApplication.singleton());
			}
				
		} catch(Exception e) {
			Log.e("InvokeMsg", "invokeMsgWithXml()", e);
			return null;
		}
	}
	
	/**
	 * 解码URL字符串
	 * @param urlStr URL字符串
	 * @return 解码后的URL字符串
	 */
	public static String decodeURLString(String urlStr) {
		return URLStringEncoder.decodeURLString(urlStr);
	}
}
