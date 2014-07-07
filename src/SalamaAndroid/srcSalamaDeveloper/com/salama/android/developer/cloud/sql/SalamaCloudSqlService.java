package com.salama.android.developer.cloud.sql;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.util.Log;

import com.salama.android.developer.SalamaAppService;
import com.salama.android.support.ServiceSupportUtil;

public class SalamaCloudSqlService {
	private final static String EASY_APP_SQL_SERVICE = "com.salama.easyapp.service.SQLService";
	
	private static SalamaCloudSqlService _singleton = null;
	
	public static SalamaCloudSqlService singleton() {
		if(_singleton == null) {
			_singleton = new SalamaCloudSqlService();
		}
		
		return _singleton;
	}

	private SalamaCloudSqlService() {
	}
	
	/**
	 * 执行查询语句
	 * @param sql sql文
	 * @return 查询结果(Xml，格式如<List><TestData>...</TestData><TestData>...</TestData>......</List>)
	 */
	public String executeQuery(String sql) {
		try {
			return SalamaAppService.singleton().getWebService().doPost(
					SalamaAppService.singleton().getAppServiceHttpUrl(), 
					ServiceSupportUtil.newList(new String[]{"serviceType", "serviceMethod", "sql"}), 
					ServiceSupportUtil.newList(new String[]{EASY_APP_SQL_SERVICE, "executeQuery", sql}));
		} catch (ClientProtocolException e) {
			Log.e("SalamaCloudSqlService", "Error in executeQuery()", e);
			return null;
		} catch (IOException e) {
			Log.e("SalamaCloudSqlService", "Error in executeQuery()", e);
			return null;
		}
	}
	
	/**
	 * 执行更新语句(update或delete)
	 * @param sql sql文
	 * @return 1:成功 0:出错
	 */
	public String executeUpdate(String sql) {
		try {
			return SalamaAppService.singleton().getWebService().doPost(
					SalamaAppService.singleton().getAppServiceHttpUrl(), 
					ServiceSupportUtil.newList(new String[]{"serviceType", "serviceMethod", "sql"}), 
					ServiceSupportUtil.newList(new String[]{EASY_APP_SQL_SERVICE, "executeUpdate", sql}));
		} catch (ClientProtocolException e) {
			Log.e("SalamaCloudSqlService", "Error in executeUpdate()", e);
			return null;
		} catch (IOException e) {
			Log.e("SalamaCloudSqlService", "Error in executeUpdate()", e);
			return null;
		}
	}
	
	/**
	 * 插入数据
	 * @param dataTable 表名
	 * @param dataXml 数据XML
	 * @param aclRestrictUserRead 指定拥有读权限的用户(多个用户idd逗号分割.该值未指定或空则仅仅数据创建者可以操作.'%'代表任何用户可以操作),
	 * @param aclRestrictUserUpdate 指定拥有读权限的用户
	 * @param aclRestrictUserDelete 指定拥有读权限的用户
	 * @return 实际插入的数据XML。如果为空，则代表操作出错
	 */
	public String insertData(String dataTable, String dataXml, 
			String aclRestrictUserRead, String aclRestrictUserUpdate, String aclRestrictUserDelete) {
		try {
			return SalamaAppService.singleton().getWebService().doPost(
					SalamaAppService.singleton().getAppServiceHttpUrl(), 
					ServiceSupportUtil.newList(new String[]{"serviceType", "serviceMethod", 
							"dataTable", "dataXml", 
							"aclRestrictUserRead", "aclRestrictUserUpdate", "aclRestrictUserDelete"}), 
					ServiceSupportUtil.newList(new String[]{EASY_APP_SQL_SERVICE, "insertData",
							dataTable, dataXml,
							aclRestrictUserRead, aclRestrictUserUpdate, aclRestrictUserDelete
							}));
		} catch (ClientProtocolException e) {
			Log.e("SalamaCloudSqlService", "Error in insertData()", e);
			return null;
		} catch (IOException e) {
			Log.e("SalamaCloudSqlService", "Error in insertData()", e);
			return null;
		}
	}
}
