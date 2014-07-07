package com.salama.android.dataservice;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ExecutorService;

import MetoXML.XmlDeserializer;
import MetoXML.Base.XmlParseException;
import MetoXML.Util.PropertyDescriptor;
import android.util.Log;

import com.salama.android.datacore.DBDataUtil;
import com.salama.android.datacore.DBManager;
import com.salama.android.datacore.SqliteUtilException;
import com.salama.android.dataservice.param.DataQueryParam;
import com.salama.android.dataservice.param.LocalQueryParam;
import com.salama.android.dataservice.param.LocalStorageParam;
import com.salama.android.dataservice.param.WebServiceParam;
import com.salama.android.support.ServiceSupportApplication;
import com.salama.android.util.ResourceFileManager;

public class SalamaDataService {
	public static final String DATA_SERVICE_NOTIFICATION_USER_INFO_RESULT = "result";
	
	private DBManager _dbManager;
	private ResourceFileManager _resourceFileManager;
	private WebService _webService;

	private LocalStorageService _localStorageService;
	private ResourceDownloadHandler _resourceDownloadHandler;
	private ResourceDownloadTaskService _resourceDownloadTaskService;
	
	private ExecutorService _queueForQueryWebService;
	private ExecutorService _queueForQueryLocalDB;
	
//	private int _queueMaxCountForQueryWebService = 5;
//	private int _queueMaxCountForQueryLocalDB = 5;
	
	/**
	 * 取得资源下载处理器
	 * @return 资源下载处理器
	 */
	public ResourceDownloadHandler getResourceDownloadHandler() {
		return _resourceDownloadHandler;
	}
	
	/**
	 * 设置资源下载处理器
	 * @param resourceDownloadHandler 资源下载处理器
	 */
	public void setResourceDownloadHandler(
			ResourceDownloadHandler resourceDownloadHandler) {
		_resourceDownloadHandler = resourceDownloadHandler;
		_resourceDownloadTaskService.setResourceDownloadHandler(_resourceDownloadHandler);
	}
	
	/**
	 * 取得DBManager
	 * @return DBManager
	 */
	public DBManager getDbManager() {
		return _dbManager;
	}
	
	/**
	 * 取得资源管理器
	 * @return 资源管理器
	 */
	public ResourceFileManager getResourceFileManager() {
		return _resourceFileManager;
	}
	
	/**
	 * 取得WebService处理器
	 * @return WebService处理器
	 */
	public WebService getWebService() {
		return _webService;
	}
	
	/**
	 * 取得本地存储处理器
	 * @return 本地存储处理器
	 */
	public LocalStorageService getLocalStorageService() {
		return _localStorageService;
	}
	
	/**
	 * 取得资源下载任务处理器
	 * @return 资源下载任务处理器
	 */
	public ResourceDownloadTaskService getResourceDownloadTaskService() {
		return _resourceDownloadTaskService;
	}
	
	/**
	 * 取得WebService任务队列
	 * @return
	 */
	public ExecutorService getQueueForQueryWebService() {
		return _queueForQueryWebService;
	}
	
	/**
	 * 取得本地数据库查询任务队列
	 * @return
	 */
	public ExecutorService getQueueForQueryLocalDB() {
		return _queueForQueryLocalDB;
	}
	
	/**
	 * 构造函数
	 */
	public SalamaDataService() {
		initObjs();
	} 
	
	/**
	 * 构造函数
	 * @param config 配置
	 */
	public SalamaDataService(SalamaDataServiceConfig config) {
		initObjs();
		
		loadConfig(config);
	}

	/*
	public SalamaDataService(int queueMaxCountForQueryWebService, int queueMaxCountForQueryLocalDB) {
		_queueMaxCountForQueryWebService = queueMaxCountForQueryWebService;
		_queueMaxCountForQueryLocalDB = queueMaxCountForQueryLocalDB;
		
		initObjs();
	} 
	
	public SalamaDataService(SalamaDataServiceConfig config, 
			int queueMaxCountForQueryWebService, int queueMaxCountForQueryLocalDB) {
		_queueMaxCountForQueryWebService = queueMaxCountForQueryWebService;
		_queueMaxCountForQueryLocalDB = queueMaxCountForQueryLocalDB;

		initObjs();
		
		loadConfig(config);
	}
	*/

	private void initObjs() {
		_webService = new WebService();
		_resourceDownloadTaskService = new ResourceDownloadTaskService();
		_resourceDownloadTaskService.setKeyForNotificationUserObj(
				DATA_SERVICE_NOTIFICATION_USER_INFO_RESULT);
		
		_localStorageService = new LocalStorageService();
		
		_queueForQueryLocalDB = ServiceSupportApplication.singleton().createSingleThreadPool();
		_queueForQueryWebService = ServiceSupportApplication.singleton().createFixedThreadPool(4);
	}
	
	/**
	 * 装载配置
	 * @param config 配置
	 */
	public void loadConfig(SalamaDataServiceConfig config) {
		_dbManager = null;
		_dbManager = new DBManager(ServiceSupportApplication.singleton(), config.getDbName());
		
		_resourceFileManager = null;
		_resourceFileManager = new ResourceFileManager(config.getResourceStorageDirPath());
		
		_webService.setRequestTimeoutSeconds(config.getHttpRequestTimeout());
		_webService.setResourceFileManager(_resourceFileManager);
		
		_resourceDownloadTaskService.setResourceFileManager(_resourceFileManager);
	}
	
	/**
	 * 查询数据
	 * @param queryParam 查询参数
	 * @return 数据列表
	 * @throws SqliteUtilException
	 * @throws IOException
	 * @throws XmlParseException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
	public List<?> query(DataQueryParam queryParam) throws SqliteUtilException,
	IOException, XmlParseException, InvocationTargetException, IllegalAccessException, InstantiationException,
	NoSuchMethodException, ClassNotFoundException {
		DBDataUtil dbDataUtil = _dbManager.createNewDBDataUtil();
		
		try {
	        //query
			return query(queryParam, dbDataUtil);
		} finally {
			dbDataUtil.close();
		}
	}
	
	/**
	 * 查询数据
	 * @param queryParam 查询参数
	 * @param dbDataUtil DBDataUtil
	 * @return 数据列表
	 * @throws SqliteUtilException
	 * @throws IOException
	 * @throws XmlParseException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
	public List<?> query(DataQueryParam queryParam, DBDataUtil dbDataUtil) throws SqliteUtilException,
	IOException, XmlParseException, InvocationTargetException, IllegalAccessException, InstantiationException,
	NoSuchMethodException, ClassNotFoundException {
		if(queryParam == null) {
			return null;
		}
		
	    //query through web service and sotre the result to localDB
		List<?> wsResult = queryWebService(queryParam.getWebService(), queryParam.getLocalStorage(), 
				dbDataUtil);
		if(queryParam.getLocalQuery() == null) {
			return wsResult;
		}
		
	    //query from local db ---
		return queryLocalDB(queryParam.getLocalQuery(), dbDataUtil);
	}

	/**
	 * 查询WebService
	 * @param webServiceParam WebService参数
	 * @param localStorageParam 本地存储参数
	 * @return 数据列表
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws XmlParseException
	 * @throws IOException
	 * @throws SqliteUtilException
	 */
	public List<?> queryWebService(WebServiceParam webServiceParam, LocalStorageParam localStorageParam) throws NoSuchMethodException, InstantiationException, IllegalAccessException,
			InvocationTargetException, XmlParseException, IOException, SqliteUtilException {
		DBDataUtil dbDataUtil = _dbManager.createNewDBDataUtil();
		
		try {
			return queryWebService(webServiceParam, localStorageParam, dbDataUtil);
		} finally {
			dbDataUtil.close();
		}
	}
	
	/**
	 * 查询WebService
	 * @param webServiceParam WebService参数
	 * @param localStorageParam 本地存储参数
	 * @param dbDataUtil DBDataUtil
	 * @return 数据列表
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws XmlParseException
	 * @throws IOException
	 * @throws SqliteUtilException
	 */
	public List<?> queryWebService(WebServiceParam webServiceParam, LocalStorageParam localStorageParam,
			DBDataUtil dbDataUtil) throws NoSuchMethodException, InstantiationException, IllegalAccessException,
			InvocationTargetException, XmlParseException, IOException, SqliteUtilException {
		if(webServiceParam == null) {
			return null;
		}
		
		boolean isPost = true;
		if("GET".equalsIgnoreCase(webServiceParam.getMethod())) {
			isPost = false;
		}
		
		String wsResult = _webService.doBasic(webServiceParam.getUrl(), isPost, 
				webServiceParam.getParamNames(), webServiceParam.getParamValues());

		//parse xml result to object
		List<?> wsResultObj = (List<?>) XmlDeserializer.stringToObject(wsResult, List.class,
				ServiceSupportApplication.singleton());
		
	    //save data to local db ---
		if(localStorageParam != null
				&& localStorageParam.getTableName() != null
				&& localStorageParam.getTableName().length() > 0) {
	        //store data into local db
			saveToLocalDB(localStorageParam, wsResultObj, dbDataUtil);
		}
		
		return wsResultObj;
	}

	/**
	 * 保存数据至本地数据库
	 * @param localStorageParam 本地存储参数
	 * @param datas 数据列表
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SqliteUtilException
	 */
	public void saveToLocalDB(LocalStorageParam localStorageParam, List<?> datas) 
			throws InvocationTargetException, IllegalAccessException, SqliteUtilException {
		DBDataUtil dbDataUtil = _dbManager.createNewDBDataUtil();
		
		try {
			saveToLocalDB(localStorageParam, datas, dbDataUtil);
		} finally {
			dbDataUtil.close();
		}
		
	}

	/**
	 * 保存数据至本地数据库
	 * @param localStorageParam 本地存储参数
	 * @param datas 数据列表
	 * @param dbDataUtil DBDataUtil
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SqliteUtilException
	 */
	public void saveToLocalDB(LocalStorageParam localStorageParam, List<?> datas, DBDataUtil dbDataUtil) 
			throws InvocationTargetException, IllegalAccessException, SqliteUtilException {
		if(datas != null) {
			_localStorageService.storeDataToTable(
					localStorageParam.getTableName(), datas, 
					localStorageParam.getExtraIndexNames(), 
					localStorageParam.getExtraIndexValues(), dbDataUtil);
		}
	}
	
	/**
	 * 查询本地数据
	 * @param localQueryParam 本地查询参数
	 * @return 数据列表
	 * @throws SqliteUtilException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public List<?> queryLocalDB(LocalQueryParam localQueryParam) 
			throws SqliteUtilException, ClassNotFoundException, NoSuchMethodException, 
			InvocationTargetException, IllegalAccessException {
		DBDataUtil dbDataUtil = _dbManager.createNewDBDataUtil();
		
		try {
			return queryLocalDB(localQueryParam, dbDataUtil);
		} finally {
			dbDataUtil.close();
		}		
	}
	
	/**
	 * 查询本地数据
	 * @param localQueryParam 本地查询参数
	 * @param dbDataUtil DBDataUtil
	 * @return 数据列表
	 * @throws SqliteUtilException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public List<?> queryLocalDB(LocalQueryParam localQueryParam, DBDataUtil dbDataUtil) 
			throws SqliteUtilException, ClassNotFoundException, NoSuchMethodException, 
			InvocationTargetException, IllegalAccessException {
		if(localQueryParam == null) {
			return null;
		}
		
		List<?> dataList = null;
		
		if(localQueryParam.getSql() != null && localQueryParam.getSql().length() > 0) {
	        //get data
			Class<?> dataClass = loadClass(localQueryParam.getDataClass());
			dataList = dbDataUtil.getSqliteUtil().findDataList(
					localQueryParam.getSql(), dataClass);
			
	        //download resource file
			if(localQueryParam.getResourceNames() != null 
					&& localQueryParam.getResourceNames().length() > 0
					&& _resourceDownloadHandler != null) {
				String[] resNameArray = localQueryParam.getResourceNames().split(",");
				String resNameTmp = null;
				String resIdTmp = null;
				Object dataTmp = null;
				int i,j;
				PropertyDescriptor propertyInfo;
				
				if(resNameArray.length > 0) {
					for(j = 0; j < resNameArray.length; j++) {
						resNameTmp = resNameArray[j];
						propertyInfo = new PropertyDescriptor(resNameTmp, dataClass);
						
						for(i = 0; i < dataList.size(); i++) {
							dataTmp = dataList.get(i);
							
							resIdTmp = (String) propertyInfo.getReadMethod().invoke(dataTmp);
							
							if(resIdTmp != null && resIdTmp.length() > 0) {
								_resourceDownloadTaskService.addDownloadTaskWithResId(
										resIdTmp, localQueryParam.getResourceDownloadNotification());
							}
						}
					}
				}
			}
		}
		
		return dataList;
	}
	
	/**
	 * 查询数据(异步-通知模式)
	 * @param queryParam 查询参数
	 * @param notification 通知名
	 */
	public void queryAsync(DataQueryParam queryParam, String notification) {
		final DataQueryParam queryParamTmp = queryParam;
		final String queryNotificationTmp = notification;
		
		_queueForQueryWebService.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					List<?> result = query(queryParamTmp);
					ServiceSupportApplication.singleton().sendWrappedLocalBroadcast(
							queryNotificationTmp, result, 
							DATA_SERVICE_NOTIFICATION_USER_INFO_RESULT);
				} catch (Exception e) {
					Log.e("SalamaDataService", "queryWebServiceAsync()", e);
				}
			}
		});
	}
	
	/**
	 * 查询WebService(异步-通知模式)
	 * @param webServiceParam WebService参数
	 * @param localStorageParam 本地存储参数
	 * @param notification 通知名
	 */
	public void queryWebServiceAsync(WebServiceParam webServiceParam, LocalStorageParam localStorageParam, 
			String notification) {
		final WebServiceParam webServiceParamTmp = webServiceParam;
		final LocalStorageParam localStorageParamTmp = localStorageParam;
		final String queryNotificationTmp = notification;
		
		_queueForQueryWebService.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					List<?> result = queryWebService(webServiceParamTmp, localStorageParamTmp);
					ServiceSupportApplication.singleton().sendWrappedLocalBroadcast(
							queryNotificationTmp, result, 
							DATA_SERVICE_NOTIFICATION_USER_INFO_RESULT);
				} catch (Exception e) {
					Log.e("SalamaDataService", "queryWebServiceAsync()", e);
				}
			}
		});
	}
	
	/**
	 * 保存数据至本地数据库(异步-通知模式)
	 * @param localStorageParam 本地存储参数
	 * @param datas 数据列表
	 * @param notification 通知名
	 */
	public void saveToLocalDBAsync(LocalStorageParam localStorageParam, final List<?> datas, String notification) {
		final LocalStorageParam localStorageParamTmp = localStorageParam;
		final String queryNotificationTmp = notification;
		
		_queueForQueryLocalDB.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					saveToLocalDB(localStorageParamTmp, datas);

					ServiceSupportApplication.singleton().sendWrappedLocalBroadcast(
							queryNotificationTmp, null, 
							DATA_SERVICE_NOTIFICATION_USER_INFO_RESULT);
				} catch (Exception e) {
					Log.e("SalamaDataService", "saveToLocalDBAsync()", e);
				}
			}
		});
	}
	
	/**
	 * 查询本地数据(异步-通知模式)
	 * @param localQueryParam 本地查询参数
	 * @param notification 通知名
	 */
	public void queryLocalDBAsync(LocalQueryParam localQueryParam, String notification) {
		final LocalQueryParam localQueryParamTmp = localQueryParam;
		final String queryNotificationTmp = notification;
		
		_queueForQueryLocalDB.execute(new Runnable() {
			
			@Override
			public void run() {
				List<?> result = null;
				try {
					DBDataUtil dbDataUtil = _dbManager.createNewDBDataUtil();
					
					try {
						result = queryLocalDB(localQueryParamTmp, dbDataUtil);
					} finally {
						dbDataUtil.close();
						dbDataUtil = null;
					}
					
					ServiceSupportApplication.singleton().sendWrappedLocalBroadcast(
							queryNotificationTmp, result, 
							DATA_SERVICE_NOTIFICATION_USER_INFO_RESULT);
				} catch (Exception e) {
					Log.e("SalamaDataService", "queryLocalDBAsync()", e);
				}
				
			}
		});
	}
	
	/**
	 * 发送通知
	 * @param notification 通知名
	 * @param result 数据
	 */
	public void postNotification(String notification, Object result) {
		ServiceSupportApplication.singleton().sendWrappedLocalBroadcast(
				notification, result, 
				DATA_SERVICE_NOTIFICATION_USER_INFO_RESULT);
	}
	
	private Class<?> loadClass(String className) throws ClassNotFoundException {
		return ServiceSupportApplication.singleton().findClass(className);
	}
}

