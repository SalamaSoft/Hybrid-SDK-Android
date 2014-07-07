package com.salama.android.webcore;

import android.view.ViewGroup;
import android.webkit.WebView;

public class WebViewSettings {
	private int _width = ViewGroup.LayoutParams.FILL_PARENT;
	private int _height = ViewGroup.LayoutParams.FILL_PARENT;
	
    private boolean _builtInZoomControls = false;
    private boolean _supportZoom = false;

    /**
     * 取得是否允许内建缩放控件
     * @return true:是 false:否
     */
	public boolean isBuiltInZoomControls() {
		return _builtInZoomControls;
	}

	/**
	 * 设置是否允许内建缩放控件
	 * @param builtInZoomControls 是否允许内建缩放控件
	 */
	public void setBuiltInZoomControls(boolean builtInZoomControls) {
		_builtInZoomControls = builtInZoomControls;
	}


	/**
	 * 取得是否支持缩放
	 * @return true:是 false:否
	 */
	public boolean isSupportZoom() {
		return _supportZoom;
	}

	/**
	 * 设置是否支持缩放
	 * @param supportZoom 是否支持缩放
	 */
	public void setSupportZoom(boolean supportZoom) {
		_supportZoom = supportZoom;
	}

	/**
	 * 装载设置
	 * @param webView 所设置的WebView对象
	 */
	public void loadWebViewSettings(WebView webView) {
		webView.getSettings().setSupportZoom(_supportZoom);
    	webView.getSettings().setBuiltInZoomControls(_builtInZoomControls);
    	webView.getLayoutParams().width = _width;
    	webView.getLayoutParams().height = _height;
    }

	/**
	 * 取得宽度
	 * @return 宽度
	 */
	public int getWidth() {
		return _width;
	}

	/**
	 * 设置宽度
	 * @param width 宽度
	 */
	public void setWidth(int width) {
		_width = width;
	}

	/**
	 * 取得高度
	 * @return 高度
	 */
	public int getHeight() {
		return _height;
	}

	/**
	 * 设置高度
	 * @param height 高度
	 */
	public void setHeight(int height) {
		_height = height;
	}
	
	
}
