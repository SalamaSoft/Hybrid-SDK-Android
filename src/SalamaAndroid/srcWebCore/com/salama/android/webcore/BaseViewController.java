package com.salama.android.webcore;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public abstract class BaseViewController extends Fragment {
	private final static String LOG_TAG = "BaseViewController";
	
	/**
	 * It could simply be treated as one property in most cases.
	 * @return
	 */
	public abstract int getViewContainerId();
	
	/**
	 * It could simply be treated as one property in most cases.
	 * @return
	 */
	public abstract void setViewContainerId(int viewContainerId);
	
//	public abstract int getRootBackStackEntryId();
//
//	public abstract void setRootBackStackEntryId(int rootBackStackEntryId);

	/**
	 * It could simply be treated as one property in most cases.
	 * @return
	 */
	public abstract int getBackStackEntryId();
	
	/**
	 * It could simply be treated as one property in most cases.
	 * @return
	 */
	public abstract void setBackStackEntryId(int backStackEntryId);
	
	private static int _defaultFragmentPushAnimSlideIn = android.R.anim.slide_in_left;
	private static int _defaultFragmentPushAnimSlideOut = android.R.anim.slide_out_right;

	
	public static int getDefaultFragmentPushAnimSlideIn() {
		return _defaultFragmentPushAnimSlideIn;
	}

	public static void setDefaultFragmentPushAnimSlideIn(
			int defaultFragmentPushAnimSlideIn) {
		_defaultFragmentPushAnimSlideIn = defaultFragmentPushAnimSlideIn;
	}

	
	public static int getDefaultFragmentPushAnimSlideOut() {
		return _defaultFragmentPushAnimSlideOut;
	}

	public static void setDefaultFragmentPushAnimSlideOut(
			int defaultFragmentPushAnimSlideOut) {
		_defaultFragmentPushAnimSlideOut = defaultFragmentPushAnimSlideOut;
	}

	/**
	 * push方式显示BaseViewController
	 * @param viewController 画面
	 */
	public void pushView(final BaseViewController viewController) {
		try {
			getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					try {
						if(viewController == null) {
							Log.i("BaseViewController", "viewController is null");
						}
						viewController.setViewContainerId(getViewContainerId());
						//viewController.setRootViewControllerId(getRootViewControllerId());
						
				        FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction();
				        
				        trans.setCustomAnimations(
				        		getDefaultFragmentPushAnimSlideIn(), 
				        		getDefaultFragmentPushAnimSlideOut());
				        
				        //trans.add(_viewContainerId, pageView, pageView.getLocalPage());
				        if(LocalWebViewFragment.class.isAssignableFrom(viewController.getClass())) {
				            trans.add(getViewContainerId(), viewController, ((LocalWebViewFragment)viewController).getLocalPage());
				        } else {
				            trans.add(getViewContainerId(), viewController, viewController.getClass().getName());
				        }
				        
				        trans.addToBackStack(null);

				        int backStackEntryId = trans.commit();
				        viewController.setBackStackEntryId(backStackEntryId);
					} catch(Throwable e) {
						Log.e(LOG_TAG, "pushView()", e);
					}
				}
			});
		} catch(Throwable e) {
			Log.e(LOG_TAG, "pushView()", e);
		}
	}
	
	/**
	 * 返回上一个画面
	 */
	public void popSelf() {
		try {
			getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					try {
						getActivity().getSupportFragmentManager().popBackStack();
					} catch(Throwable e) {
						Log.e(LOG_TAG, "popSelf()", e);
					}
				}
			});
		} catch(Throwable e) {
			Log.e(LOG_TAG, "popSelf()", e);
		}
	}
	
	/**
	 * 返回至根画面
	 */
	public void popToRoot() {
		try {
			getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					try {
						//getActivity().getSupportFragmentManager().popBackStack(getRootViewControllerId(), 0);
						getActivity().getSupportFragmentManager().popBackStack(
								getActivity().getSupportFragmentManager().getBackStackEntryAt(0).getId(), 0);
					} catch(Throwable e) {
						Log.e(LOG_TAG, "popToRoot()", e);
					}
				}
			});
		} catch(Throwable e) {
			Log.e(LOG_TAG, "popToRoot()", e);
		}
	}
	
}
