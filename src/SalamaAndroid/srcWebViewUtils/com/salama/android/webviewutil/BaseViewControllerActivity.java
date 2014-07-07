package com.salama.android.webviewutil;

import com.salama.android.support.ServiceSupportApplication;
import com.salama.android.util.SSLog;
import com.salama.android.webcore.BaseViewController;
import com.salama.android.webcore.LocalWebViewFragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.RelativeLayout;

/**
 * 提供这个类，是为了便于以present方式显示CommonWebViewController画面
 */
public class BaseViewControllerActivity extends FragmentActivity {
	private final static String LOG_TAG = "BaseViewControllerActivity";
	public final static String EXTRA_NAME_VIEW_CONTROLLER_SESSION_KEY = "baseViewControllerSessionKey";
	
	public static final int VIEW_CONTAINER_ID = 40;

	private RelativeLayout _contentLayout;
	
	private BaseViewController _baseViewController;
	private String _baseViewControllerSessionKey;
	
	public BaseViewController getBaseViewController() {
		return _baseViewController;
	}

	public void setBaseViewController(BaseViewController baseViewController) {
		_baseViewController = baseViewController;
	}
	
	/**
	 * 以present方式显示BaseViewController画面
	 * @param context 上下文.通常是当前画面的Activity。
	 * @param viewController 要显示的画面
	 */
	public static void presentViewController(Context context, BaseViewController viewController) {
		Intent intent = new Intent(context, BaseViewControllerActivity.class);
		
		String vcSessionKey = "PresentBaseViewController." + System.currentTimeMillis();
		ServiceSupportApplication.singleton().setSessionValue(vcSessionKey, viewController);
		intent.putExtra(EXTRA_NAME_VIEW_CONTROLLER_SESSION_KEY, vcSessionKey);
		
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle arg0) {
		try {
			super.onCreate(arg0);
			
			//SSLog.d("BaseViewControllerActivity", "onCreate()");

			_baseViewControllerSessionKey = getIntent().getStringExtra("baseViewControllerSessionKey");
			if(_baseViewControllerSessionKey != null) {
				BaseViewController baseVC = (BaseViewController) ServiceSupportApplication.singleton().
						getSessionValue(_baseViewControllerSessionKey);
				if(baseVC != null) {
					_baseViewController = baseVC;
				}
			}
			
			_contentLayout = new RelativeLayout(this);
			_contentLayout.setId(VIEW_CONTAINER_ID);
			RelativeLayout.LayoutParams contentLayoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT,
					RelativeLayout.LayoutParams.FILL_PARENT);
			_contentLayout.setLayoutParams(contentLayoutParams);
			setContentView(_contentLayout);

			_baseViewController.setViewContainerId(VIEW_CONTAINER_ID);

			// show fragment ----------
			FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
	        trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
	        
	        if(LocalWebViewFragment.class.isAssignableFrom(_baseViewController.getClass())) {
	            trans.add(_baseViewController.getViewContainerId(), _baseViewController, ((LocalWebViewFragment)_baseViewController).getLocalPage());
	        } else {
	            trans.add(_baseViewController.getViewContainerId(), _baseViewController, _baseViewController.getClass().getName());
	        }
	        
	        trans.addToBackStack(null);
	        
	        int backStackEntryId = trans.commit();
	        _baseViewController.setBackStackEntryId(backStackEntryId);
		} catch(Throwable e) {
			Log.e(LOG_TAG, "", e);
		}
		
	}
	
	@Override
	public void onBackPressed() {
		int backStackEntryCnt = getSupportFragmentManager().getBackStackEntryCount();
		SSLog.d(LOG_TAG, "onBackPressed() backStackEntryCnt:" + backStackEntryCnt);
		
		if(backStackEntryCnt <= 1) {
			finish();
		} else {
			super.onBackPressed();
		}
	}

	
	@Override
	public void finish() {
		if(_baseViewControllerSessionKey != null) {
			ServiceSupportApplication.singleton().removeSessionValue(_baseViewControllerSessionKey);
		}
		super.finish();
	}

	@Override
	protected void onDestroy() {
		if(onDestroyListener != null) {
			onDestroyListener.onDestroy();
		}
		
		super.onDestroy();
	}
	
	/**
	 * 用于在onDestroy()中添加处理
	 */
	public OnDestroyListener onDestroyListener = null;
	
	public interface OnDestroyListener {
		void onDestroy();
	}
}
