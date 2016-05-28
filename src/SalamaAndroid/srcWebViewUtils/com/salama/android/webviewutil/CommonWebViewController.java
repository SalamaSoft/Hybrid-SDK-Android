package com.salama.android.webviewutil;

import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.salama.android.support.ServiceSupportApplication;
import com.salama.android.util.SSLog;
import com.salama.android.webcore.BaseViewController;
import com.salama.android.webcore.LocalWebView;
import com.salama.android.webcore.LocalWebViewFragment;

@SuppressLint("ValidFragment")
public class CommonWebViewController extends BaseWebViewController {
	private final static String LOG_TAG = "CommonWebViewController";
	private ConcurrentHashMap<String, String> _viewEventNameToJSCallBackMapping = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, Long> _localPageLastCreateTimeMapping = new ConcurrentHashMap<String, Long>();

	protected String _title = "";

	protected RelativeLayout _contentLayout = null;
	protected TitleBar _titleBarLayout = null;
	protected LocalWebView _webView = null;
		
	//Title bar setting -----------------------------------
	protected TitleBarSetting _titleBarSetting = new TitleBarSetting();
	
	protected static boolean _defaultTitleBarHidden = false;

	/**
	 * 标题栏是否隐藏的默认设置(全局)
	 * @return
	 */
	public static boolean isDefaultTitleBarHidden() {
		return _defaultTitleBarHidden;
	}

	/**
	 * 标题栏是否隐藏的默认设置(全局)
	 * @param defaultTitleBarHidden
	 */
	public static void setDefaultTitleBarHidden(boolean defaultTitleBarHidden) {
		_defaultTitleBarHidden = defaultTitleBarHidden;
	}

	public void setNavigationBarHidden(boolean hidden) {
		_titleBarSetting.setHidden(hidden);
	}
	/**
	 * 取得标题栏设置
	 * @return 标题栏设置
	 */
	public TitleBarSetting getTitleBarSetting() {
		return _titleBarSetting;
	}

	/**
	 * 设置标题栏设置
	 * @param titleBarSetting 标题栏设置
	 */
	public void setTitleBarSetting(final TitleBarSetting titleBarSetting) {
		_titleBarSetting = titleBarSetting;
		
		if(_titleBarLayout != null && getActivity() != null) {
			getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					try {
						_titleBarLayout.loadSetting(getActivity(), titleBarSetting);
					} catch(Throwable e) {
						Log.e(LOG_TAG, "setTitleBarSetting()", e);
					}
				}
			});
		}
	}
	
	/**
	 * 装载标题栏设置
	 */
	public void loadTitleBarSetting() {
		if(_titleBarLayout != null && getActivity() != null) {
			getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					try {
						_titleBarLayout.loadSetting(getActivity(), _titleBarSetting);
					} catch(Throwable e) {
						Log.e(LOG_TAG, "loadTitleBarSetting()", e);
					}
				}
			});
		}
	}

	/**
	 * 取得标题栏Layout
	 * @return 标题栏Layout
	 */
	public TitleBar getTitleBarLayout() {
		return _titleBarLayout;
	}

	/**
	 * 取得标题栏Layout
	 */
	public LocalWebView getWebView() {
		return _webView;
	}

	/**
	 * 取得内容Layout
	 * @return
	 */
	public RelativeLayout getContentLayout() {
		return _contentLayout;
	}
	
	/**
	 * 取得标题
	 * @return 标题
	 */
	public String getTitle() {
		return _title;
	}

	/**
	 * 设置标题
	 * @param title
	 */
	public void setTitle(String title) {
		_title = title;
		_titleBarSetting.setCenterViewTitle(title);

		if(_titleBarLayout != null) {
			getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					try {
						_titleBarLayout.setTitle(_title);
					} catch(Throwable e) {
						Log.e(LOG_TAG, "setTitle()", e);
					}
				}
			});
		}
	}
	
	public CommonWebViewController() {
		this(null);
	}
	
	public CommonWebViewController(String viewServiceClassName) {
		super(viewServiceClassName);
		
		_titleBarSetting.setHidden(_defaultTitleBarHidden);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(container != null) {
			_viewContainerId = container.getId();
			
			Log.d("CommonWebViewFragment", "container != null :" + _viewContainerId);
		}
		
		//Layout ------------------------------------------
		_contentLayout = new RelativeLayout(getActivity());
		RelativeLayout.LayoutParams contentLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
		_contentLayout.setLayoutParams(contentLayoutParams);
		
		//title bar layout
		_titleBarLayout = createTitleBarLayout();
		if(_titleBarLayout != null) {
			_contentLayout.addView(_titleBarLayout);
			
			//_titleBarLayout.setTitle(_title);
		}
		
		//WebView
		_webView = createWebView();
		if(_webView != null) {
			setWebView(_webView);
			if(_titleBarLayout != null) {
				((RelativeLayout.LayoutParams) _webView.getLayoutParams()).
				addRule(RelativeLayout.BELOW, _titleBarLayout.getId());
			}
			
			_contentLayout.addView(_webView);

			_webView.loadLocalPage(getLocalPage());
			loadWebViewSettings();
		}
		
		//Handle event
		if(getThisViewService() != null) {
			getThisViewService().viewDidLoad();
		}
		handleViewEvent("viewDidLoad");
		
		return _contentLayout; 
	}
	
	@Override
	public void onDestroyView() {
		//JsCallBack
		if(getThisViewService() != null) {
			getThisViewService().viewWillUnload();
		}
		handleViewEvent("viewWillUnload");

		super.onDestroyView();
		
		//Handle event
		if(getThisViewService() != null) {
			getThisViewService().viewDidUnload();
		}
		handleViewEvent("viewDidUnload");
	}
	
	@Override
	public void onResume() {
		//Handle event
		if(getThisViewService() != null) {
			getThisViewService().viewWillAppear();
		}
		handleViewEvent("viewWillAppear");

		super.onResume();
		
		//Handle event
		if(getThisViewService() != null) {
			getThisViewService().viewDidAppear();
		}
		handleViewEvent("viewDidAppear");
	}
	
	@Override
	public void onPause() {
		//Handle event
		if(getThisViewService() != null) {
			getThisViewService().viewWillDisappear();
		}
		handleViewEvent("viewWillDisappear");

		super.onPause();
		
		//Handle event
		if(getThisViewService() != null) {
			getThisViewService().viewDidDisappear();
		}
		handleViewEvent("viewDidDisappear");
	}
	
	protected void handleViewEvent(String eventName) {
		//JsCallBack
		String jsCallBack = _viewEventNameToJSCallBackMapping.get(eventName);
		if(jsCallBack != null) {
			callJavaScript(jsCallBack, null);
		}
	}
	
	protected TitleBar createTitleBarLayout() {
		if(_titleBarSetting.isHidden()) {
			return null;
		}
		
		//Title bar layout --------------------------
		TitleBar titleBarLayout = new TitleBar(getActivity());
		titleBarLayout.setId(ServiceSupportApplication.singleton().newViewId());
		
		titleBarLayout.loadSetting(getActivity(), _titleBarSetting);
		
		return titleBarLayout;
	}
	
	protected LocalWebView createWebView() {
		LocalWebView webView = new LocalWebView(this, getActivity());
		webView.setId(ServiceSupportApplication.singleton().newViewId());
		
		RelativeLayout.LayoutParams webViewLayoutPrams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);

		webView.setLayoutParams(webViewLayoutPrams);
		
		return webView;
	}
	
	/************************************* Interfaces Below  ------->  ***************************/
	/******* These methods below are designed to be compatible to the javascript in salamNativeService. 
	 * The code style is like IOS, because the first version of Salama Hybrid SDK is written in IOS. 
	 **&**************************************************************************************************/
	
	/********** Navigation Bar ***********/
	/**
	 * 设置导航栏颜色
	 * @param red 红(0-1.0)
	 * @param green 绿(0-1.0)
	 * @param blue 蓝(0-1.0)
	 * @param alpha alpha(0-1.0)
	 */
	public void setNavigationBarTintColor(float red, float green, float blue, float alpha) {
		final int iRed = (int)(red * 255);
		final int iGreen = (int)(green * 255);
		final int iBlue = (int)(blue * 255);
		final int iAlpha = (int)(alpha * 255);

		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				try {
					_titleBarSetting.setBackgroundTColor(TitleBar.toIntColor(iRed, iGreen, iBlue, iAlpha));
					
					if(_titleBarLayout != null) {
						_titleBarLayout.setBackGroundTInt255Color(iRed, iGreen, iBlue, iAlpha);
					}
				} catch(Throwable e) {
					Log.e(LOG_TAG, "setNavigationBarTintColor()", e);
				}
			}
		});
	}
	
	
	/**
	 * 将jsCallBack同画面事件绑定
	 * @param eventName 画面事件名
	 * 支持的事件名: viewDidLoad,viewDidUnload, viewWillUnload, viewWillAppear, viewWillDisappear, viewDidAppear, viewDidDisappear
	 * @param jsCallBack JavaScript回调函数
	 */
	public void registerJSCallBackToViewEvent(String eventName, String jsCallBack) {
		if(eventName == null || eventName.length() == 0
				|| jsCallBack == null || jsCallBack.length() == 0) {
			return;
		}
		
		if(!_viewEventNameToJSCallBackMapping.containsKey(eventName)) {
			_viewEventNameToJSCallBackMapping.put(eventName, jsCallBack);
			SSLog.d("CommonWebViewFragment", 
					"registerJSCallBackToViewEvent() eventName:" + eventName 
					+ " jsCallBack:" + jsCallBack);
		}
	}
	
	/**
	 * 创建页面View
	 * @param pageName 页面名
	 * @return 页面View
	 */
	public CommonWebViewController createPageView(String pageName) {
		if(isCreateLocalPageTwiceInvoked(pageName)) {
			return null;
		}
		
		CommonWebViewController vc = new CommonWebViewController();
		vc.setLocalPage(pageName);
		vc.setViewContainerId(_viewContainerId);
		//vc.setRootViewControllerId(getRootViewControllerId());
		vc.setTitleBarSetting(_titleBarSetting);
		
		return vc;
	}
	
	/**
	 * 创建页面View
	 * @param pageName 页面名
	 * @param commonWebViewControllerClassName 页面View类型名
	 * @return 页面View
	 */
	public CommonWebViewController createPageView(String pageName, String commonWebViewControllerClassName) {
		if(isCreateLocalPageTwiceInvoked(pageName)) {
			return null;
		}

		try {
			Class<?> cls = ServiceSupportApplication.singleton().findClass(commonWebViewControllerClassName);
			CommonWebViewController vc = (CommonWebViewController) cls.newInstance();
			vc.setLocalPage(pageName);
			vc.setViewContainerId(_viewContainerId);
			//vc.setRootViewControllerId(getRootViewControllerId());
			vc.setTitleBarSetting(_titleBarSetting);
			
			return vc;
		} catch(Throwable e) {
			Log.e(LOG_TAG, "createPageView()", e);
			return null;
		}
	}

	/**
	 * 创建页面View
	 * @param pageName 页面名
	 * @param commonWebViewControllerClassName 页面View类型名
	 * @param viewServiceClassName Class name of thisViewService
	 * @return 页面View
	 */
	public CommonWebViewController createPageView(String pageName, String commonWebViewControllerClassName,
			String viewServiceClassName) {
		if(isCreateLocalPageTwiceInvoked(pageName)) {
			return null;
		}

		try {
			Class<?> cls = ServiceSupportApplication.singleton().findClass(commonWebViewControllerClassName);
			
			CommonWebViewController vc = (CommonWebViewController) cls.
					getConstructor(String.class).newInstance(viewServiceClassName);
			
			vc.setLocalPage(pageName);
			vc.setViewContainerId(_viewContainerId);
			//vc.setRootViewControllerId(getRootViewControllerId());
			vc.setTitleBarSetting(_titleBarSetting);
			
			return vc;
		} catch(Throwable e) {
			Log.e(LOG_TAG, "createPageView()", e);
			return null;
		}
	}
	
	/**
	 * push方式显示页面View
	 * @param pageView 页面View
	 * @param setIntoNavigationAsRoot Not be used. It is to be compatible with javascript invoking 
	 * which supports IOS, Android, Windows Phone.
	 */
	public void pushPageView(final BaseViewController viewController, final boolean setIntoNavigationAsRoot) {
		/*
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				try {
					if(viewController == null) {
						Log.i("CommonWebViewController", "viewController is null");
					}
					viewController.setViewContainerId(_viewContainerId);
					//viewController.setRootViewControllerId(getRootViewControllerId());
					
			        FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction();
			        
			        trans.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
			        
			        //trans.add(_viewContainerId, pageView, pageView.getLocalPage());
			        if(LocalWebViewFragment.class.isAssignableFrom(viewController.getClass())) {
			            trans.add(_viewContainerId, viewController, ((LocalWebViewFragment)viewController).getLocalPage());
			        } else {
			            trans.add(_viewContainerId, viewController, viewController.getClass().getName());
			        }
			        
			        trans.addToBackStack(null);

			        int backStackEntryId = trans.commit();
			        viewController.setBackStackEntryId(backStackEntryId);
				} catch(Throwable e) {
					Log.e("CommonWebViewController", "pushPageView()", e);
				}
			}
		});
		*/
		
		pushView(viewController);
	}

	/**
	 * push方式显示页面View
	 * @param pageName 页面View
	 */
	public void pushPage(final String pageName) {
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				pushPageView(createPageView(pageName), false);
			}
		});
	}
	
	/**
	 * 返回至指定页面
	 * @param pageName 页面名
	 */
	public void popToPage(final String pageName) {
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				try {
					getActivity().getSupportFragmentManager().popBackStack(pageName, 0);
				} catch(Throwable e) {
					Log.e(LOG_TAG, "popToRoot()", e);
				}
			}
		});
	}

	/**
	 * present方式显示页面
	 * @param pageView 页面View
	 * @param setIntoNavigationAsRoot 该参数为保持和iOS版兼容，Android中不使用
	 */
	public void presentPageView(final BaseViewController viewController, final boolean setIntoNavigationAsRoot) {
		/*
		viewController.setViewContainerId(_viewContainerId);
		//viewController.setRootViewControllerId(getRootViewControllerId());

		FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction();
        
        trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        
        if(LocalWebViewFragment.class.isAssignableFrom(viewController.getClass())) {
            trans.add(_viewContainerId, viewController, ((LocalWebViewFragment)viewController).getLocalPage());
        } else {
            trans.add(_viewContainerId, viewController, viewController.getClass().getName());
        }
        
        trans.addToBackStack(null);

        trans.commit();
        */
		
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Intent intent = new Intent(getActivity(), BaseViewControllerActivity.class);
					
					String vcSessionKey = "PresentBaseViewController." + System.currentTimeMillis();
					ServiceSupportApplication.singleton().setSessionValue(vcSessionKey, viewController);
					intent.putExtra("baseViewControllerSessionKey", vcSessionKey);
					
					getActivity().startActivity(intent);
				} catch(Throwable e) {
					Log.e(LOG_TAG, "presentPageView()", e);
				}
			}
		});
		
	}
	
	/**
	 * present方式显示页面
	 * @param pageName 页面名
	 * @param setIntoNavigationAsRoot 该参数为保持和iOS版兼容，Android中不使用
	 */
	public void presentPage(String pageName, boolean setIntoNavigationAsRoot) {
		presentPageView(createPageView(pageName), false);
	}
		
	/**
	 * 关闭present方式显示的画面(为了和IOS版兼容，保留参数animated)
	 */
	public void dismissSelf(boolean animated) {
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				try {
					//getActivity().getSupportFragmentManager().popBackStack();
					getActivity().finish();
				} catch(Throwable e) {
					Log.e(LOG_TAG, "dismissSelf()", e);
				}
			}
		});
	}
	
	public void enableScrollBar() {
		getWebView().setScrollbarFadingEnabled(false);
		getWebView().setVerticalScrollBarEnabled(true);
	}
	
	public void disableScrollBar() {
		getWebView().setScrollbarFadingEnabled(true);
		getWebView().setVerticalScrollBarEnabled(false);
	}
	
	private final static long CREATE_PAGE_LIMIT_INTERVAL_MS = 700;
	/**
	 * 在部分Android版本的WebView中存在一次click事件被提交2次的bug，此方法用于解决这个问题。
	 * @param pageName
	 * @return
	 */
	private boolean isCreateLocalPageTwiceInvoked(String pageName) {
		Long currentTime = Long.valueOf(System.currentTimeMillis());
		Long lastTime = _localPageLastCreateTimeMapping.get(pageName);
		
		boolean isTwiceInvoked = false;
		if(lastTime != null
				&& (currentTime.longValue() - lastTime.longValue()) <= CREATE_PAGE_LIMIT_INTERVAL_MS) {
			SSLog.d(LOG_TAG, "isCreateLocalPageTwiceInvoked() true");
			isTwiceInvoked = true;
		}
		
		_localPageLastCreateTimeMapping.put(pageName, currentTime);
		
		return isTwiceInvoked;
	}
	
	/************************************* Interfaces above <--------  **************************/
}
