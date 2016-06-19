package com.salama.android.baseapp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import com.salama.android.dataservice.SalamaDataService;
import com.salama.android.dataservice.SalamaDataServiceConfig;
import com.salama.android.dataservice.WebService;
import com.salama.android.jsservice.base.natives.SalamaNativeService;
import com.salama.android.support.ServiceSupportApplication;
import com.salama.android.support.ServiceSupportUtil;
import com.salama.android.util.HexUtil;
import com.salama.android.util.SSLog;
import com.salama.android.webcore.WebController;
import com.salama.android.webcore.WebManager;

public class BaseAppService {
	private final static String LOG_TAG = BaseAppService.class.getSimpleName();

    public final static String DEFAULT_WEB_PACKAGE_DIR = "html";
    public final static String DEFAULT_WEB_RESOURCE_DIR = "res";

	private static boolean _debugMode = false;
    
	// --------------- arguments ---------------
	private final String _udid;
	private final int _httpRequestTimeoutSeconds;
	private final String _webPackageDirName;
	private final String _webResourceDirName;
    //private final File _webBaseDir = null;
	

	// --------------- variables ----------------
    private String _bundleId = null;
    //private SalamaDataServiceConfig _config;
    private SalamaDataService _dataService;
    private String _systemLanguage = null;
    //private String _systemLanguagePrefix = null;
    private String _textFileName = null;

	private SalamaNativeService _nativeService = null;
	private WebService _webService = null;

	
    private AtomicInteger _dataIdSeq = new AtomicInteger(0); 


    public static void setDebugMode(boolean debugMode) {
    	_debugMode = debugMode;
    	
        if (_debugMode) {
            //默认debug模式下，html.zip每次都解压。正式发布时，设置debugMode为false，软件发布版本发生变化时，html.zip会被解压。
            WebController.setDebugMode(true);

            //默认debugLevel,正式发布时改为errorLevel
            SSLog.setSSLogLevel(SSLog.SSLogLevelDebug);
        } else {
            WebController.setDebugMode(false);

            //默认debugLevel,正式发布时改为errorLevel
            SSLog.setSSLogLevel(SSLog.SSLogLevelError);
        }
    }
    
    public static boolean isDebugMode() {
		return _debugMode;
	}
    
    public String getUdid() {
		return _udid;
	}
    
    public String getBundleId() {
		return _bundleId;
	}
    public String getSystemLanguage() {
        return _systemLanguage;
    }

    public SalamaDataService getDataService() {
        return _dataService;
    }
    
    public WebService getWebService() {
		return _webService;
	}
    
    public SalamaNativeService getNativeService() {
		return _nativeService;
	}

    public String getTextByKey(String key) {
        return ServiceSupportUtil.getStringsValueByKey(key, _textFileName);
    }
    
    /**
     * 生成dataId(可以作为本地数据库的数据主键)
     */
    public String generateNewDataId() {
    	//It is unlikely that count of generated id is more than Short.MAX_VALUE in 1 millisecond
    	short seq = (short)(_dataIdSeq.incrementAndGet() & 0xFFFF);
        return _udid
        		.concat(HexUtil.toHexString(System.currentTimeMillis()))
        		.concat(HexUtil.toHexString(seq))
        		;
    }
    
    /**
     * 
     * @param debugMode Optional
     * @param udid Required
     * @param webPackageDirName Optional
     * @param webResourceDirName Optional
     */
    public BaseAppService(
    		String udid,
    		int httpRequestTimeoutSeconds,
    		String webPackageDirName,
    		String webResourceDirName
    		) {
    	_udid = udid;
    	_httpRequestTimeoutSeconds = httpRequestTimeoutSeconds;
    	_webPackageDirName = webPackageDirName;
    	_webResourceDirName = webResourceDirName;

        //get bundle id
        _bundleId = ServiceSupportApplication.singleton().getPackageName();

        SSLog.i(LOG_TAG, "_udid:" + _udid);
        SSLog.i(LOG_TAG, "_httpRequestTimeoutSeconds:" + _httpRequestTimeoutSeconds);
        SSLog.i(LOG_TAG, "_webPackageDirName:" + _webPackageDirName);
        SSLog.i(LOG_TAG, "_webResourceDirName:" + _webResourceDirName);
        SSLog.i(LOG_TAG, "_bundleId:" + _bundleId);

        
        checkTextFile();
        
        initWebController();
        
        initServices();
    }
    
    protected void initWebController() {
    	final String webPackageName = _webPackageDirName; 
        int htmlId = ServiceSupportApplication.singleton().getResources().getIdentifier(
        		webPackageName, "raw", ServiceSupportApplication.singleton().getPackageName());
        SSLog.i(LOG_TAG, "htmlId:" + htmlId);

        InputStream htmlPackageStream = ServiceSupportApplication.singleton().
                getResources().openRawResource(htmlId);
        try {
            WebManager.initWithWebPackageName(webPackageName, htmlPackageStream);

            SSLog.i(LOG_TAG, "webBaseDir:" + WebManager.getWebController().getWebBaseDirPath());
            SSLog.i(LOG_TAG, "webRootDir:" + WebManager.getWebController().getWebRootDirPath());
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void initServices() {
        _dataService = new SalamaDataService(makeDataServiceConfig());
        
        _nativeService = new SalamaNativeService(_dataService);
        
        _webService = new WebService();
        _webService.setRequestTimeoutSeconds(_httpRequestTimeoutSeconds);
        _webService.setResourceFileManager(_dataService.getResourceFileManager());
    }

    private SalamaDataServiceConfig makeDataServiceConfig() {
    	SalamaDataServiceConfig config = new SalamaDataServiceConfig();

        config.setHttpRequestTimeout(_httpRequestTimeoutSeconds);

        //resource directory name
        File resDir = new File(WebManager.getWebController().getWebRootDirPath(), _webResourceDirName);
        config.setResourceStorageDirPath(resDir.getAbsolutePath());

        //local Sqlite file name
        if(!DEFAULT_WEB_PACKAGE_DIR.equals(_webPackageDirName)) {
            config.setDbName("localDB" + "_" + _webPackageDirName);
        } else {
            config.setDbName("localDB");
        }
        
        return config;
    }
    
    private void checkTextFile() {
        _systemLanguage = Locale.getDefault().getLanguage();
        SSLog.i(LOG_TAG, "_systemLanguage:" + _systemLanguage);

        //init .strings file
        _textFileName = "text_".concat(_systemLanguage.substring(0, 2));

        int textFileId = ServiceSupportApplication.singleton().getResources().getIdentifier(
                _textFileName, "raw", ServiceSupportApplication.singleton().getPackageName());
        if(textFileId == 0) {
            //file not exists
            SSLog.i(LOG_TAG, _textFileName + ".strings does not exist. Change to use text_en.strings");

            _textFileName = "text_en";
            textFileId = ServiceSupportApplication.singleton().getResources().getIdentifier(
                    _textFileName, "raw", ServiceSupportApplication.singleton().getPackageName());
        }
        SSLog.i(LOG_TAG, "_textFileName:" + _textFileName + " textFileId:" + textFileId);

        if(textFileId != 0) {
            try {
                ServiceSupportUtil.loadStringsFile(_textFileName,
                        ServiceSupportApplication.singleton().getResources().openRawResource(textFileId));
            } catch (Exception e) {
                SSLog.e(LOG_TAG, "Error in openning file:" + _textFileName, e);
            }
        }
    }
	
    
    protected void printWebDirFiles() {
        File webBaseDir = new File(WebManager.getWebController().getWebBaseDirPath());
        SSLog.d(LOG_TAG, "list webBaseDir:" + webBaseDir);
        printSubFiles(webBaseDir);

        File webRootDir = new File(WebManager.getWebController().getWebRootDirPath());
        SSLog.d(LOG_TAG, "list webRootDir:" + webRootDir);
        printSubFiles(webRootDir);
    }

    private void printSubFiles(File dir) {
        File[] files = dir.listFiles();
        if(files != null) {
            for(File file : files) {
                SSLog.d(LOG_TAG, "isDir:" + file.isDirectory() + " path:" + file.getName());
            }
        }
    }
    
}
