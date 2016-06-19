package com.salama.android.baseapp;

import com.salama.android.webcore.WebManager;

public class SalamaBaseAppService extends BaseAppService {
    public final static int DEFAULT_HTTP_REQUEST_TIMEOUT_SECONDS = 30;
    public final static String SALAMA_SERVICE_NAME = "salama";

    private static int _httpRequestTimeoutSeconds = DEFAULT_HTTP_REQUEST_TIMEOUT_SECONDS;
    private static SalamaBaseAppService _singleton = null;
    
    /**
     * This method should be invoked before singleton().
     * @param httpRequestTimeOutSeconds
     */
    public static void setHttpRequestTimeOutSeconds(int httpRequestTimeOutSeconds) {
    	_httpRequestTimeoutSeconds = httpRequestTimeOutSeconds;
    }
    
    public static SalamaBaseAppService singleton() {
        if(_singleton == null) {
        	synchronized (SalamaBaseAppService.class) {
                _singleton = new SalamaBaseAppService();
			}
        }
        return _singleton;
    }
    
    private SalamaBaseAppService() {
    	super(
    			SalamaBaseApp.singleton().getUDID(), 
    			_httpRequestTimeoutSeconds, 
    			DEFAULT_WEB_PACKAGE_DIR, 
    			DEFAULT_WEB_RESOURCE_DIR
    			);
    	
	    // Register Service ----------------------------------
		WebManager.getWebController().getNativeService().registerService(SALAMA_SERVICE_NAME, this);
    }
    
}
