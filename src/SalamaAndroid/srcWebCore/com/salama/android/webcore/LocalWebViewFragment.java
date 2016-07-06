package com.salama.android.webcore;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.salama.android.support.ServiceSupportApplication;
import com.salama.android.util.SSLog;

@SuppressLint("ValidFragment")
public class LocalWebViewFragment extends BaseViewController implements WebVariableStack {
	
	protected int _viewContainerId;
	/**
	 * 取得容器Id
	 * @return 容器Id
	 */
	@Override
	public int getViewContainerId() {
		return _viewContainerId;
	}
	
	/**
	 * 设置容器Id
	 * @param viewContainerId 容器Id
	 */
	@Override
	public void setViewContainerId(int viewContainerId) {
		_viewContainerId = viewContainerId;
	}

//	private int _rootViewControllerId = 0;

	/**
	 * 取得根容器Id
	 * @return 根容器Id
	 */
//	@Override
//	public int getRootViewControllerId() {
//		return _rootViewControllerId;
//	}

	/**
	 * 设置根容器Id
	 * @param rootViewControllerId 根容器Id
	 */
//	@Override
//	public void setRootViewControllerId(int rootViewControllerId) {
//		_rootViewControllerId = rootViewControllerId;
//	}
	
	protected int _backStackEntryId = -1;
	
	@Override
	public int getBackStackEntryId() {
		return _backStackEntryId;
	}
	
	@Override
	public void setBackStackEntryId(int backStackEntryId) {
		_backStackEntryId = backStackEntryId;
	}
	
	public boolean isRootViewController() {
		return (_backStackEntryId == getActivity().getSupportFragmentManager().getBackStackEntryAt(0).getId());
	}
	
	private LocalWebView _webView = null;
	private String _localPage = "";
	
    private String _thisViewServiceClassName;
    private ViewService _thisViewService;

    private ConcurrentHashMap<String, Object> _webVariableStackForScopePage = new ConcurrentHashMap<String, Object>();
    private ConcurrentHashMap<String, Object> _webVariableStackForScopeTemp = new ConcurrentHashMap<String, Object>();
	
    private ConcurrentHashMap<String, String> _transitionParams = new ConcurrentHashMap<String, String>();
    
	//webView setting -------------------------------------
    WebViewSettings _webViewSettings = new WebViewSettings();
    
    /**
     * 取得本地页面
     * @return
     */
	public String getLocalPage() {
		return _localPage;
	}

	/**
	 * 设置本地页面
	 * @param localPage
	 */
	public void setLocalPage(String localPage) {
		_localPage = localPage;
	}

	/**
	 * 取得WebView实例
	 * @return
	 */
	public LocalWebView getWebView() {
		return _webView;
	}

	/**
	 * 设置WebView实例
	 * @param webView
	 */
	public void setWebView(LocalWebView webView) {
		_webView = webView;
	}
		
	/**
	 * 取得ThisViewService类型名
	 * @return
	 */
	public String getThisViewServiceClassName() {
		return _thisViewServiceClassName;
	}
	
	/**
	 * 取得WebViewSetting
	 * @return
	 */
	public WebViewSettings getWebViewSettings() {
		return _webViewSettings;
	}

	/**
	 * 设置WebViewSetting
	 * @param webViewSettings
	 */
	public void setWebViewSettings(WebViewSettings webViewSettings) {
		_webViewSettings = webViewSettings;
	}

	/**
	 * 装载WebViewSetting
	 */
	public void loadWebViewSettings() {
		if(_webView != null) {
			_webViewSettings.loadWebViewSettings(_webView);
		}
	}

	/**
	 * 设置thisViewService类型名
	 * @param thisViewServiceClassName
	 */
	public void setThisViewServiceClassName(String thisViewServiceClassName) {
		if(thisViewServiceClassName == null 
				|| thisViewServiceClassName.length() == 0) {
			return;
		}
		
		if(_thisViewService != null) {
			return;
		}
		
		_thisViewServiceClassName = thisViewServiceClassName;
		
		/* Change to only set once
		if(_thisViewService != null) {
			_thisViewService.setThisView(null);
			_thisViewService = null;
		}
		*/
		
		try {
			_thisViewService = (ViewService) ServiceSupportApplication.singleton().findClass(_thisViewServiceClassName).newInstance();
			_thisViewService.setThisView(this);
		} catch(Exception e) {
			Log.e("LocalWebView", "setThisViewServiceClassName()", e);
		}

	}
	
	/**
	 * 取得thisViewService
	 * @return
	 */
	public ViewService getThisViewService() {
		return _thisViewService;
	}

	/**
	 * 设置thisViewService
	 * @param thisViewService
	 */
	public void setThisViewService(ViewService thisViewService) {
		_thisViewService.setThisView(this);
		_thisViewService = thisViewService;
	}
	
	public LocalWebViewFragment() {
		super();
	}
	
	public LocalWebViewFragment(String viewServiceClassName) {
		super();
		
		setThisViewServiceClassName(viewServiceClassName);
	}
	
	public void log(String msg) {
		SSLog.d(_localPage, msg);
	}
	
	/**
	 * 调用JavaScript函数
	 * @param functionName
	 * @param params
	 */
	public void callJavaScript(final String functionName, final List<String> params) {
		_webView.callJavaScript(functionName, params);
	}
	
	/**
	 * 设置画面迁移参数
	 * @param paramValue 参数值
	 * @param paramName 参数名
	 */
	public void setTransitionParam(String paramValue, String paramName) {
		_transitionParams.put(paramName, paramValue);
	}
	
	/**
	 * 取得画面迁移参数
	 * @param paramName 参数名
	 * @return 参数值
	 */
	public String getTransitionParamByName(String paramName) {
		return _transitionParams.get(paramName);
	}
	
	/**
	 * 设置session值
	 * @param name 名称
	 * @param value 值
	 */
	public void setSessionValueWithName(String name, String value) {
		WebManager.getWebController().setSessionValueWithName(name, value);
	}
	
	/**
	 * 删除session值
	 * @param name 名称
	 */
	public void removeSessionValueWithName(String name) {
		WebManager.getWebController().removeSessionValueWithName(name);
	}
	
	/**
	 * 取得session值
	 * @param name 名称
	 * @return
	 */
	public String getSessionValueWithName(String name) {
		return WebManager.getWebController().getSessionValueWithName(name);
	}
	
	@Override
	public void clearVariablesOfAllScope() {
		_webVariableStackForScopePage.clear();
		_webVariableStackForScopeTemp.clear();
	}

	@Override
	public void clearVariablesOfScope(int scope) {
		if(scope == WebVariableStackScopeTemp) {
			_webVariableStackForScopeTemp.clear();
		} if(scope == WebVariableStackScopePage) {
			_webVariableStackForScopePage.clear();
		}
	}

	@Override
	public void setVariable(Object value, String name, int scope) {
		if(scope == WebVariableStackScopeTemp) {
			_webVariableStackForScopeTemp.put(name, value);
		} if(scope == WebVariableStackScopePage) {
			_webVariableStackForScopePage.put(name, value);
		}
	}

	@Override
	public Object getVariable(String name, int scope) {
		if(scope == WebVariableStackScopeTemp) {
			return _webVariableStackForScopeTemp.get(name);
		} if(scope == WebVariableStackScopePage) {
			return _webVariableStackForScopePage.get(name);
		} else {
			return null;
		}
	}

	@Override
	public void removeVariable(String name, int scope) {
		if(scope == WebVariableStackScopeTemp) {
			_webVariableStackForScopeTemp.remove(name);
		} if(scope == WebVariableStackScopePage) {
			_webVariableStackForScopePage.remove(name);
		}
	}

}
