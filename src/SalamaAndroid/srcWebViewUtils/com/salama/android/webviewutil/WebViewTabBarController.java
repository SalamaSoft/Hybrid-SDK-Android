package com.salama.android.webviewutil;

import android.os.Bundle;
import android.widget.RelativeLayout;

import com.salama.android.support.ServiceSupportApplication;
import com.salama.android.webcore.LocalWebView;

/**
 * 为简化tab bar画面，采用html显示tab bar。
 *
 */
public class WebViewTabBarController extends EasyTabBarController {

	private String _tabBarViewLocalPage = "";
	
	/**
	 * 设置tab bar页面
	 * @param localPage 页面
	 */
	public void setWebViewTabBarLocalPage(String localPage) {
		_tabBarViewLocalPage = localPage;
		
		if(getTabBarView() != null) {
			((LocalWebView)getTabBarView()).loadLocalPage(localPage);
		}
	}
	
	public String getWebViewTabBarLocalPage() {
		return _tabBarViewLocalPage;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTabBarView(createWebView());

		super.onCreate(savedInstanceState);
	}
	
	private LocalWebView createWebView() {
		LocalWebView webView = new LocalWebView(this, this);
		webView.setId(ServiceSupportApplication.singleton().newViewId());
		
		RelativeLayout.LayoutParams webViewLayoutPrams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);

		webView.setLayoutParams(webViewLayoutPrams);
		webView.setScrollbarFadingEnabled(true);
		
		webView.loadLocalPage(_tabBarViewLocalPage);
		
		return webView;
	}
}
