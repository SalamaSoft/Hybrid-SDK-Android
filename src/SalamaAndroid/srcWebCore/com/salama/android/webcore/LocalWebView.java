package com.salama.android.webcore;

import java.lang.reflect.Method;
import java.util.List;

import com.salama.android.util.SSLog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class LocalWebView extends WebView {
	public static final String DefaultEncoding = "utf-8";
	
	private Object _thisView;
	private String _localPage = "";
	
	/**
	 * 取得页面名
	 * @return
	 */
	public String getLocalPage() {
		return _localPage;
	}

	/**
	 * 设置页面名
	 * @param localPage
	 */
	public void setLocalPage(String localPage) {
		_localPage = localPage;
	}
	
	private WebViewClient _webViewClient = new WebViewClient() {
		@Override
		public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
			SSLog.d("LocalWebView", "onPageStarted() url:" + url);
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			SSLog.d("LocalWebView", "onPageFinished() url:" + url);
		}
		
		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			SSLog.d("LocalWebView", "onReceivedError() errorCode:" + errorCode + " description:" + description);
		}
		
		@Override
		public void onLoadResource(WebView view, String url) {
			SSLog.d("LocalWebView", "onLoadResource() url:" + url);

			if(handleSpecialUrl(url)) {
				return;
			}
			
			Object msg = NativeService.parseNativeServiceCmd(url);
			if(msg != null) {
				WebManager.getWebController().invokeNativeService(msg, LocalWebView.this, _thisView);
			} else {
				super.onLoadResource(view, url);
			}
		}

		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			SSLog.d("LocalWebView", "shouldOverrideUrlLoading() url:" + url);

			if(handleSpecialUrl(url)) {
				return true;
			}
			
			/*
			Object msg = NativeService.parseNativeServiceCmd(url);
			if(msg != null) {
				WebManager.getWebController().invokeNativeService(msg, LocalWebView.this, _thisView);
			} else {
				view.loadUrl(url);
			}
			
			return true;
			*/
			
			return WebManager.getWebController().handleUrlLoadingEvent(url, view, _thisView);
		}
			
		
		private boolean handleSpecialUrl(String url) {
			if(url == null) {
				return false;
			}
			
			
			if(url.toLowerCase().startsWith("tel:")) {
				String url2 = null;
				if(url.toLowerCase().startsWith("tel://")) {
					url2 = "tel:".concat(url.substring(6));
				} else {
					url2 = url;
				}
				
				//dial tel
				LocalWebView.this.getContext().startActivity(
						new Intent(Intent.ACTION_CALL, Uri.parse(url2)));
				
				return true;
			}
			
			return false;
		}

		@Override
		public void onUnhandledKeyEvent(WebView view, android.view.KeyEvent event) {
			SSLog.d("LocalWebView", "onUnhandledKeyEvent() event:" + event);
		}
		
		@Override
		public boolean shouldOverrideKeyEvent(WebView view, android.view.KeyEvent event) {
			SSLog.d("LocalWebView", "shouldOverrideKeyEvent() event:" + event);
			
			if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		public void onReceivedSslError(WebView view, android.webkit.SslErrorHandler handler, android.net.http.SslError error) {
			SSLog.d("LocalWebView", "onReceivedSslError() error:" + error);
			handler.proceed();
		}

	};
	
	private long _lastMoveEventTime = 0;
	private long _moveEventMaxInterval = 33;
	public boolean onTouchEvent(android.view.MotionEvent ev) {
		long eventTime = ev.getEventTime();
		if(ev.getAction() == MotionEvent.ACTION_MOVE) {
			if((eventTime - _lastMoveEventTime) >= _moveEventMaxInterval) {
				return super.onTouchEvent(ev);
			} else {
				return true;
			}
		} else {
			return super.onTouchEvent(ev);
		}
	};
		
	private WebChromeClient _webChromeClient = new WebChromeClient() {
		@Override
		public boolean onJsAlert(WebView view, String url, String message, android.webkit.JsResult result) {
			return false;
		}
	};

	/**
	 * 构造函数
	 * @param context 上下文
	 */
	public LocalWebView(Context context) {
		super(context);
		
		_thisView = this;
		init();
	}
	
	/**
	 * 构造函数
	 * @param context 上下文
	 * @param attrs 属性设置
	 */
	public LocalWebView(Context context, AttributeSet attrs) {
		super(context, attrs);

		_thisView = this;
		init();
	}
	
	/**
	 * 构造函数
	 * @param context 上下文
	 * @param attrs 属性设置
	 * @param defStyle 风格
	 */
	public LocalWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		_thisView = this;
		init();
	}
	
	/**
	 * 构造函数
	 * @param thisView thisView实例
	 * @param context 上下文
	 */
	public LocalWebView(Object thisView, Context context) {
		super(context);
		
		_thisView = thisView;
		init();
	}
	
	/**
	 * 构造函数
	 * @param thisView thisView实例
	 * @param context 上下文
	 * @param attrs 属性设置
	 */
	public LocalWebView(Object thisView, Context context, AttributeSet attrs) {
		super(context, attrs);

		_thisView = thisView;
		init();
	}
	
	/**
	 * 
	 * @param thisView thisView实例
	 * @param context 上下文
	 * @param attrs 属性设置
	 * @param defStyle 风格
	 */
	public LocalWebView(Object thisView, Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		_thisView = thisView;
		init();
	}

	private void init() {
		setWebViewClient(_webViewClient);
		setWebChromeClient(_webChromeClient);
		
		enablePlatformNotifications();
		
		getSettings().setJavaScriptEnabled(true);
		
		//for performance
		getSettings().setRenderPriority(RenderPriority.HIGH);
		
		if(Build.VERSION.SDK_INT >= 11) 
		{
			try {
				Method methodOfsetLayerType = WebView.class.getMethod("setLayerType", int.class, Paint.class);
				if(methodOfsetLayerType != null) {
					//LAYER_TYPE_SOFTWARE
					methodOfsetLayerType.invoke(this, 1, null);
					SSLog.d("LocalWebView", "setLayerType to LAYER_TYPE_SOFTWARE");
				}
			} catch (Exception e) {
			}
		}
		
		
		//getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		getSettings().setAllowFileAccess(true);
		getSettings().setBuiltInZoomControls(false);
		getSettings().setDefaultTextEncodingName(DefaultEncoding);
		getSettings().setSaveFormData(false);
		getSettings().setSavePassword(false);
		getSettings().setSupportZoom(false);
		//getSettings().setDomStorageEnabled(true);
	    getSettings().setBlockNetworkImage(false);
	    getSettings().setBlockNetworkLoads(false);
		
		setLongClickable(false);
		setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY);
		setFocusable(true);
		requestFocus();
	}
	
	/*
	public void clearAllCachedValues() {
		_webVariableStackForScopePage.clear();
		_webVariableStackForScopeTemp.clear();
		_transitionParams.clear();
	}
	*/
	
	/**
	 * 装载本地页面
	 */
	public void loadLocalPage() {
		WebManager.getWebController().loadLocalPage(_localPage, this);
	}

	/**
	 * 装载本地页面
	 * @param relativeUrl 指定的页面
	 */
	public void loadLocalPage(String relativeUrl) {
		_localPage = relativeUrl;
		
		WebManager.getWebController().loadLocalPage(relativeUrl, this);
	}
	
	/**
	 * 调用JavaScript函数
	 * @param functionName
	 * @param params
	 */
	public void callJavaScript(String functionName, List<String> params) {
		StringBuilder script = new StringBuilder(WebController.LOAD_URL_JAVASCRIPT + functionName + "(");
		
		if(params != null && params.size() > 0) {
			script.append("'").append(WebController.encodeToScriptStringValue(params.get(0))).append("'");
			
			for(int i = 1; i < params.size(); i++) {
				script.append(", '").append(WebController.encodeToScriptStringValue(params.get(i))).append("'");
			}
		}
		
		script.append(")");
		
		String scriptStr = script.toString();
		loadUrl(scriptStr);
		
		if(SSLog.getSSLogLevel() <= SSLog.SSLogLevelDebug) {
			SSLog.d("LocalWebView", "callJavaScript():" + scriptStr);
		}
	}
	
	
}
