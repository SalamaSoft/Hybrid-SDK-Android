package com.salama.android.developer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Locale;

import MetoXML.XmlDeserializer;
import MetoXML.XmlSerializer;
import android.content.Context;
import android.util.Log;

import com.salama.android.dataservice.SalamaDataService;
import com.salama.android.dataservice.SalamaDataServiceConfig;
import com.salama.android.dataservice.WebService;
import com.salama.android.developer.cloud.SalamaCloudService;
import com.salama.android.developer.natives.SalamaNativeService;
import com.salama.android.developer.user.SalamaUserService;
import com.salama.android.developer.util.SalamaWebService;
import com.salama.android.developer.util.http.SalamaHttpClientUtil;
import com.salama.android.support.ServiceSupportApplication;
import com.salama.android.support.ServiceSupportUtil;
import com.salama.android.util.HexUtil;
import com.salama.android.util.MD5Util;
import com.salama.android.util.SSLog;
import com.salama.android.util.http.HttpClientUtil;
import com.salama.android.webcore.WebController;
import com.salama.android.webcore.WebManager;

public class SalamaAppService {
	public final static String EASY_APP_SERVICE_URI = "/easyApp/cloudDataService.do";
	public final static String CLOUD_DATA_SERVICE_URI = "/cloudDataService.do";
	public final static String EASY_APP_AUTH_SERVICE = "com.salama.easyapp.service.AppAuthService";
	private final static String FILE_NAME_APP_AUTH_INFO_PREFIX = "salamaappauth_";
	
	private final static String DEFAULT_WEB_PACKAGE_NAME = "html";
	
	private final static int DEFAULT_HTTP_REQUEST_TIMEOUT_SECONDS = 30;
	
	private static File webBaseDir = null;
	
	private boolean _dedicatedServerMode = false;
	
	private String _appId = null;
	private String _appSecret = null;
	private String _bundleId = null;
	
	AppAuthInfo _appAuthInfo = null;
	AppInfo _appInfo = null;
	
	SalamaDataService _dataService = null;
	Object _lockForNewDataId = new Object();
	
	//private String _easyAppServiceHttpHost = null;
	//private String _easyAppServiceHttpsHost = null;
	private String _easyAppServiceHostPrefix = null;
	private int _easyAppServiceHttpPort = 0;
	private int _easyAppServiceHttpsPort = 0;
	private String _easyAppServiceHttpUrl = null;
	private String _easyAppServiceHttpsUrl = null;

	private String _host = null;
	private String _myAppServiceHttpUrl = null;
	private String _myAppServiceHttpsUrl = null;

    //private String _udid = null;
	
	private String _systemLanguage = null;
	//private String _systemLanguagePrefix = null;
	private String _textFileName = null;
	
	private WebService _webService = null;
	private boolean _notUseEasyAppService = false;
	private SalamaDataServiceConfig _config = null;
	
	private static SalamaAppService _singleton = null;
	
	public static void setWebBaseDir(File webBaseDir) {
		SalamaAppService.webBaseDir = webBaseDir;
	}
	
	public static SalamaAppService singleton() {
		if(_singleton == null) {
			_singleton = new SalamaAppService();
		}
		
		return _singleton;
	}

	private SalamaAppService() {
		_bundleId = ServiceSupportApplication.singleton().getPackageName();
		SSLog.i("SalamaAppService", "_bundleId:" + _bundleId);
		
		_appInfo = new AppInfo();
		
		checkTextFile();
	}
	
	private void initWebController() {
		//init web controller
		if(checkWebPackageExists(DEFAULT_WEB_PACKAGE_NAME)) {
			initObjsWithWebPackageName(DEFAULT_WEB_PACKAGE_NAME, "res");
		} else {
			initObjsForMultiWebRootMode();
		}
	}

	private boolean checkWebPackageExists(String webPackageName) {
		int htmlId = ServiceSupportApplication.singleton().getResources().getIdentifier(
				"html", "raw", ServiceSupportApplication.singleton().getPackageName());
		
		return htmlId != 0;
	}
	
	public String getAppId() {
		return _appId;
	}

	public String getAppSecret() {
		return _appSecret;
	}

	public String getBundleId() {
		return _bundleId;
	}

	public AppAuthInfo getAppAuthInfo() {
		return _appAuthInfo;
	}

	public AppInfo getAppInfo() {
		return _appInfo;
	}

	public SalamaDataService getDataService() {
		return _dataService;
	}

	public String getAppServiceHttpUrl() {
		return _easyAppServiceHttpUrl;
	}

	public String getAppServiceHttpsUrl() {
		return _easyAppServiceHttpsUrl;
	}

	/**
	 * @return OpenUDID
	 */
	public String getUDID() {
		return SalamaApplication.getUDID();
	}

	public String getSystemLanguage() {
		return _systemLanguage;
	}
	
	public WebService getWebService() {
		return _webService;
	}

	public SalamaUserService getUserService() {
		return SalamaUserService.singleton();
	}
	
	public SalamaNativeService getNativeService() {
		return SalamaNativeService.singleton();
	}
	
	public SalamaCloudService getCloudService() {
		return SalamaCloudService.singleton();
	}
	
	public String getAppToken() {
		if(_appAuthInfo == null || _appAuthInfo.getAppToken() == null) {
			return "";
		} else {
			return _appAuthInfo.getAppToken();
		}
	}
	
	/**
	 * 改变本地网页根路径
	 */
	public void switchToWebRootDirPath(String webRootDirPath) {
		WebManager.getWebController().switchToWebRootDirPath(webRootDirPath);
		
		initServicesWithIsMultiWebRootMode(true);
		
		initWebService();
	}
	
	/**
	 * 初始化App.
	 */
	public void initApp() {
		initWebController();
		
		_notUseEasyAppService = true;
		
		initWebService();
	}
	
	/**
	 * @param appId
	 * @param appSecret
	 */
	public void initApp(String appId, String appSecret) {
		initWebController();
		
		initAppWithNoAuthenticating(appId, appSecret);

		initWebService();
		
		authenticateApp();

	}

	/**
	 * @param appId
	 * @param appSecret
	 */
	public void initAppAsync(String appId, String appSecret) {
		initWebController();
		
		initAppWithNoAuthenticating(appId, appSecret);

		initWebService();
		
		_dataService.getQueueForQueryWebService().execute(new Runnable() {
			@Override
			public void run() {
				authenticateApp();
			}
		});
	}
	
	/**
	 * 初始化App,独立服务器模式
	 * @param appId
	 * @param appSecret
	 */
	public void initAppInDedicatedServerMode(String appId, String appSecret, String host, int port)
	{
		initWebController();
		
	    _dedicatedServerMode = true;
	    _host = host;
	    _easyAppServiceHttpPort = port;
	    _easyAppServiceHttpsPort = 443;
	    
		initWebService();

		initAppWithNoAuthenticating(appId, appSecret);

	    authenticateApp();
	}
	
	/**
	 * 初始化App,独立服务器模式，异步执行
	 * @param appId
	 * @param appSecret
	 */
	public void initAppInDedicatedServerModeAsync(final String appId, final String appSecret, String host, int port)
	{
		initWebController();
		
	    _dedicatedServerMode = true;
	    _host = host;
	    _easyAppServiceHttpPort = port;
	    _easyAppServiceHttpsPort = 443;
	    
		initWebService();

		_dataService.getQueueForQueryWebService().execute(new Runnable() {
			@Override
			public void run() {
			    initAppWithNoAuthenticating(appId, appSecret);

			    authenticateApp();
			}
		});
	}
	
	private void initAppHostInDedicatedServerMode() {
		_easyAppServiceHttpUrl = "http://" + _host + ":" + _easyAppServiceHttpPort + EASY_APP_SERVICE_URI;
		_easyAppServiceHttpsUrl = "https://" + _host + ":" + _easyAppServiceHttpPort + EASY_APP_SERVICE_URI;
		
		_myAppServiceHttpUrl = "http://" + _host + ":" + _easyAppServiceHttpPort 
				+ "/" + _appId + CLOUD_DATA_SERVICE_URI;
		_myAppServiceHttpsUrl = "https://" + _host + ":" + _easyAppServiceHttpsPort 
				+ "/" + _appId + CLOUD_DATA_SERVICE_URI;

		_appInfo.setAppServiceHttpUrl(_easyAppServiceHttpUrl);
		_appInfo.setAppServiceHttpsUrl(_easyAppServiceHttpsUrl);
		
		_appInfo.setMyAppServiceHttpUrl(_myAppServiceHttpUrl);
		_appInfo.setMyAppServiceHttpsUrl(_myAppServiceHttpsUrl);
	}

	public void initAppInDebugMode(String appId, String appSecret) {
		initAppWithNoAuthenticating(appId, appSecret);
		
		_easyAppServiceHttpPort = 8080;
		_easyAppServiceHttpsPort = 8080;
		_easyAppServiceHttpUrl = "http://127.0.0.1:8080" + EASY_APP_SERVICE_URI;
		_easyAppServiceHttpsUrl = _easyAppServiceHttpUrl;
		
		_appInfo.setAppServiceHttpUrl(_easyAppServiceHttpUrl);
		_appInfo.setAppServiceHttpsUrl(_easyAppServiceHttpsUrl);

		initWebService();
		
		authenticateApp();
	}
		
	/**
	 * 认证AppId和AppSecret。认证失败的场合，则所有其他的WebService请求都会被拒绝
	 * @return true:成功 false:失败
	 */
	public boolean appLogin() {
		AppAuthInfo appAuthInfo = appLoginByAppSecret(_appSecret);
		
		if(appAuthInfo != null) {
			if(isAppAuthInfoValid(appAuthInfo)) {
				if(_appAuthInfo == null) {
					_appAuthInfo = new AppAuthInfo();
					_appAuthInfo.setAppId(appAuthInfo.getAppId());
				}
				
				_appAuthInfo.setAppToken(appAuthInfo.getAppToken());
				_appAuthInfo.setExpiringTime(appAuthInfo.getExpiringTime());
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 取得text_xx.strings的内容，其中的xx为当前系统语言的2字符前缀。英语:en，简体汉语:zh，法语:fr，德语:de，日语:ja。
	 * 其他的语言参考IOS的文档中提供的链接 http://www.loc.gov/standards/iso639-2/php/English_list.php
	 * 如果系统语言对应的text_xx.strings文件不存在，则读取text_en.strings。
	 * @param key text内容的key
	 * @return text内容
	 */
	public String getTextByKey(String key) {
		return ServiceSupportUtil.getStringsValueByKey(key, _textFileName);
	}
	
	/**
	 * 生成dataId(可以作为本地数据库的数据主键)
	 * 采用较为简单的方法：<udid> + <UTC>。方法内有锁，线程安全。但1秒只能产生1000个。
	 */
	public String generateNewDataId() {
		synchronized (_lockForNewDataId) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				Log.e("SalamaAppService", "Error in generateNewDataId()", e);
			}
			return getUDID().concat(HexUtil.toHexString(System.currentTimeMillis()));
		}
	}
	
	private void checkTextFile() {
		_systemLanguage = Locale.getDefault().getLanguage();
		SSLog.i("SalamaAppService", "_systemLanguage:" + _systemLanguage);
		
		//init .strings file
		_textFileName = "text_".concat(_systemLanguage.substring(0, 2));
		
		int textFileId = ServiceSupportApplication.singleton().getResources().getIdentifier(
				_textFileName, "raw", ServiceSupportApplication.singleton().getPackageName());
		if(textFileId == 0) {
			//file not exists
			SSLog.i("SalamaAppService", _textFileName + ".strings does not exist. Change to use text_en.strings");
			
			_textFileName = "text_en";
			textFileId = ServiceSupportApplication.singleton().getResources().getIdentifier(
					_textFileName, "raw", ServiceSupportApplication.singleton().getPackageName());
		}
		SSLog.i("SalamaAppService", "_textFileName:" + _textFileName + " textFileId:" + textFileId);
		
		if(textFileId != 0) {
			try {
				ServiceSupportUtil.loadStringsFile(_textFileName, 
						ServiceSupportApplication.singleton().getResources().openRawResource(textFileId));
			} catch (Exception e) {
				Log.e("SalamaAppService", "Error in openning file:" + _textFileName, e);
			}
		}
		
	}
	
	private void initObjsWithWebPackageName(String webPackageName, String resourceDirName) {
	    //DEBUG
		//WebController.setDebugMode(false);

		//init webmanager(include extract html) ---------------
		int htmlId = ServiceSupportApplication.singleton().getResources().getIdentifier(
				"html", "raw", ServiceSupportApplication.singleton().getPackageName());
		SSLog.i("SalamaAppService", "htmlId:" + htmlId);
		
		InputStream htmlPackageStream = ServiceSupportApplication.singleton().
				getResources().openRawResource(htmlId);
		try {
			if(webBaseDir == null) {
				WebManager.initWithWebPackageName(webPackageName, htmlPackageStream);
			} else {
				WebManager.initWithWebPackageName(webPackageName, htmlPackageStream, webBaseDir);
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		initServicesWithIsMultiWebRootMode(false);

	    // Register Service ----------------------------------
		WebManager.getWebController().getNativeService().registerService("salama", this);
	}

	private void initObjsForMultiWebRootMode() {
	    //DEBUG
		//WebController.setDebugMode(false);

		//init webmanager(include extract html) ---------------
		//init default htmlRootDir ----------------------------
		if(webBaseDir == null) {
			webBaseDir = SalamaApplication.singleton().getExternalFilesDir(null);
		}
		
		File defaultHtmlRootDir = new File(webBaseDir, DEFAULT_WEB_PACKAGE_NAME);
		if(!defaultHtmlRootDir.exists()) {
			defaultHtmlRootDir.mkdirs();
		}
		WebManager.initWithExistingWebRootDir(defaultHtmlRootDir);

		initServicesWithIsMultiWebRootMode(true);

	    // Register Service ----------------------------------
		WebManager.getWebController().getNativeService().registerService("salama", this);
	}
	
	private void initServicesWithIsMultiWebRootMode(boolean isMultiWebRootMode) {
	    //init dataService ------------------------------------
		_config = new SalamaDataServiceConfig();
		
		_config.setHttpRequestTimeout(DEFAULT_HTTP_REQUEST_TIMEOUT_SECONDS);
		File resDir = new File(WebManager.getWebController().getWebRootDirPath(), "res");
		_config.setResourceStorageDirPath(resDir.getAbsolutePath());
		
		if(isMultiWebRootMode) {
			_config.setDbName("localDB" + "_" + WebManager.getWebController().getWebPackageName());
		} else {
			_config.setDbName("localDB");
		}
		
		_dataService = new SalamaDataService(_config);
	}
	
	private void initWebService() {
		if(_notUseEasyAppService) {
			HttpClientUtil.initHttpParams(
					HttpClientUtil.DEFAULT_CONNECTION_POOL_TIMEOUT_MS, 
					HttpClientUtil.DEFAULT_CONNECTION_TIMEOUT_MS, 
					DEFAULT_HTTP_REQUEST_TIMEOUT_SECONDS, 
					HttpClientUtil.DEFAULT_HTTP_PORT, 
					HttpClientUtil.DEFAULT_HTTPS_PORT);
			_webService = new WebService();
		} else {
			SalamaHttpClientUtil.initHttpParams(
					SalamaHttpClientUtil.DEFAULT_CONNECTION_POOL_TIMEOUT_MS, 
					SalamaHttpClientUtil.DEFAULT_CONNECTION_TIMEOUT_MS, 
					DEFAULT_HTTP_REQUEST_TIMEOUT_SECONDS, 
					_easyAppServiceHttpPort, 
					_easyAppServiceHttpsPort);
			
			_webService = new SalamaWebService();
		}
		_webService.setRequestTimeoutSeconds(_config.getHttpRequestTimeout());
		_webService.setResourceFileManager(_dataService.getResourceFileManager());
	}

	private void initAppWithNoAuthenticating(String appId, String appSecret) {
		_appId = appId;
		_appSecret = appSecret;
		
		if(_dedicatedServerMode) {
			initAppHostInDedicatedServerMode();
		} else {
			initAppHost();
		}
	}
	
	private void initAppHost() {
		String serverDivisionNum = _appId.substring(0, 4);
		_easyAppServiceHostPrefix = "dev".concat(serverDivisionNum);
		
		String serverNumHex = _appId.substring(4, 6);
		byte serverNumByte = Byte.parseByte(serverNumHex, 16);
		_easyAppServiceHttpPort = 30000 + serverNumByte;
		_easyAppServiceHttpsPort = 40000 + serverNumByte;
		
		_easyAppServiceHttpUrl = "http://".concat(_easyAppServiceHostPrefix).concat(".salama.com.cn:")
				.concat(Integer.toString(_easyAppServiceHttpPort)).concat(EASY_APP_SERVICE_URI);
		_easyAppServiceHttpsUrl = "https://".concat(_easyAppServiceHostPrefix).concat(".salama.com.cn:")
				.concat(Integer.toString(_easyAppServiceHttpsPort)).concat(EASY_APP_SERVICE_URI);
		
		_appInfo.setAppServiceHttpUrl(_easyAppServiceHttpUrl);
		_appInfo.setAppServiceHttpsUrl(_easyAppServiceHttpsUrl);
		
//		SSLog.d("SalamaAppService", "initAppHost() http url:" + _easyAppServiceHttpUrl);
//		SSLog.d("SalamaAppService", "initAppHost() https url:" + _easyAppServiceHttpsUrl);
		
	}
	
	private AppAuthInfo authenticateApp() {
	    //login
		boolean loginSuccess = checkAppLogin();
		if(!loginSuccess) {
			SSLog.i("SalamaAppService", "App login failed");
		}
		
//		_appInfo.setAppId(_appAuthInfo.getAppId());
//		_appInfo.setAppToken(_appAuthInfo.getAppToken());
//		_appInfo.setExpiringTime(_appAuthInfo.getExpiringTime());
		
		return _appAuthInfo;
	}
	
	private boolean checkAppLogin() {
		boolean loginSuccess = false;
		
		_appAuthInfo = getStoredAppAuthInfoWithAppId(_appId);
		
		if(_appAuthInfo != null 
				&& (_appAuthInfo.getExpiringTime() - 120000) >= System.currentTimeMillis()
				&& _appAuthInfo.getAppToken() != null 
				) {
	        //token尚未过期
			AppAuthInfo appAuthInfo = appLoginByAppToken(_appAuthInfo.getAppToken());
			
			if(appAuthInfo != null) {
				_appAuthInfo.setAppToken(appAuthInfo.getAppToken());
				_appAuthInfo.setExpiringTime(appAuthInfo.getExpiringTime());
				
				if(isAppAuthInfoValid(appAuthInfo)) {
					loginSuccess = true;
				}
			}
		}
		
		if(!loginSuccess) {
			AppAuthInfo appAuthInfo = appLoginByAppSecret(_appSecret);
			if(appAuthInfo != null) {
				if(isAppAuthInfoValid(appAuthInfo)) {
					if(_appAuthInfo == null) {
						_appAuthInfo = new AppAuthInfo();
						_appAuthInfo.setAppId(appAuthInfo.getAppId());
					}
					
					_appAuthInfo.setAppToken(appAuthInfo.getAppToken());
					_appAuthInfo.setExpiringTime(appAuthInfo.getExpiringTime());
					
					loginSuccess = true;
				}
			} else {
				if(_appAuthInfo == null) {
					_appAuthInfo = new AppAuthInfo();
				}
			}
		}
		
		if(!loginSuccess) {
			if(_appAuthInfo != null) {
				_appAuthInfo.setAppToken("");
				_appAuthInfo.setExpiringTime(0);
			}
		}
		
		if(_appAuthInfo != null) {
			storeAppAuthInfoWithAppId(_appId, _appAuthInfo);
		}
		
		return loginSuccess;
	}
	
	private AppAuthInfo appLoginByAppToken(String appToken) {
		try {
			String appAuthInfoXml = (String) SalamaHttpClientUtil.doBasicGet(false, _easyAppServiceHttpsUrl, 
					ServiceSupportUtil.newList(new String[]{"serviceType", "serviceMethod", "appId", "appToken"}), 
					ServiceSupportUtil.newList(new String[]{EASY_APP_AUTH_SERVICE, "appLoginByToken", _appId, appToken}),
					null);

			SSLog.d("SalamaAppService", "appLoginByAppToken() webservice result:" + appAuthInfoXml);

			if(appAuthInfoXml == null || appAuthInfoXml.length() == 0) {
				return null;
			} else {
				return (AppAuthInfo) XmlDeserializer.stringToObject(appAuthInfoXml, AppAuthInfo.class, ServiceSupportApplication.singleton());
			}
		} catch (Exception e) {
			Log.e("SalamaAppService", "Error in appLoginByAppToken()", e);
			return null;
		}
	}
	
	
	private AppAuthInfo getStoredAppAuthInfoWithAppId(String appId) {
		FileInputStream fis = null;
		InputStreamReader reader = null;
		
		try {
			fis = ServiceSupportApplication.singleton().openFileInput(
					FILE_NAME_APP_AUTH_INFO_PREFIX.concat(appId));
			reader = new InputStreamReader(fis, XmlDeserializer.DefaultCharset);
			
			XmlDeserializer xmlDes = new XmlDeserializer();
			return (AppAuthInfo) xmlDes.Deserialize(
					reader, AppAuthInfo.class, ServiceSupportApplication.singleton());
		} catch(FileNotFoundException e) {
			SSLog.d("SalamaAppService", "getStoredAppAuthInfoWithAppId() file does not exist.");
			return null;
		} catch(Exception e) {
			Log.e("SalamaAppService", "Error in getStoredAppAuthInfoWithAppId()", e);
			return null;
		} finally {
			try {
				fis.close();
			} catch(Exception e) {
			}
			try {
				reader.close();
			} catch(Exception e) {
			}
		}
	}
	
	private void storeAppAuthInfoWithAppId(String appId, AppAuthInfo appAuthInfo) {
		FileOutputStream fos = null;
		OutputStreamWriter writer = null;
		
		try {
			fos = ServiceSupportApplication.singleton().openFileOutput(
					FILE_NAME_APP_AUTH_INFO_PREFIX.concat(appId), Context.MODE_PRIVATE);
			writer = new OutputStreamWriter(fos, XmlDeserializer.DefaultCharset);
			
			XmlSerializer xmlSer = new XmlSerializer();
			xmlSer.Serialize(writer, appAuthInfo, AppAuthInfo.class);
			
			writer.flush();
		} catch(Exception e) {
			Log.e("SalamaAppService", "Error in storeAppAuthInfoWithAppId()", e);
		} finally {
			try {
				fos.close();
			} catch(Exception e) {
			}
			try {
				writer.close();
			} catch(Exception e) {
			}
		}
		
	}
	
	private boolean isAppAuthInfoValid(AppAuthInfo appAuthInfo) {
		if(appAuthInfo == null) {
			return false;
		} else {
			if(!_appId.equals(appAuthInfo.getAppId())) {
				return false;
			}
			
			if(appAuthInfo.getAppToken() == null) {
				return false;
			}
			
			if(appAuthInfo.getExpiringTime() <= System.currentTimeMillis()) {
				return false;
			}
			
			return true;
		}
	}
	
	private AppAuthInfo appLoginByAppSecret(String appSecret) {
		String utcTimeStr = Long.toString(System.currentTimeMillis());
		String utcTimeMD5 = MD5Util.md5String(utcTimeStr);
		String secretMD5 = MD5Util.md5String(appSecret);
		String secretMD5MD5 = MD5Util.md5String(secretMD5.concat(utcTimeMD5));
		
		try {
			String appAuthInfoXml = (String)SalamaHttpClientUtil.doBasicGet(false, _easyAppServiceHttpsUrl, 
					ServiceSupportUtil.newList(new String[]{"serviceType", "serviceMethod", "appId", "appSecretMD5MD5", "utcTime"}), 
					ServiceSupportUtil.newList(new String[]{EASY_APP_AUTH_SERVICE, "appLogin", _appId, secretMD5MD5, utcTimeStr}),
					null);			
			
			SSLog.d("SalamaAppService", "appLoginByAppSecret() webservice result:" + appAuthInfoXml);
			
			if(appAuthInfoXml == null || appAuthInfoXml.length() == 0) {
				return null;
			} else {
				return (AppAuthInfo)XmlDeserializer.stringToObject(appAuthInfoXml, AppAuthInfo.class);
			}
		} catch (Exception e) {
			Log.e("SalamaAppService", "Error in appLoginByAppSecret()", e);
			return null;
		}
		
	}
	
}
