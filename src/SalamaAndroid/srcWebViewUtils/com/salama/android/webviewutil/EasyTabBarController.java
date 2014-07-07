package com.salama.android.webviewutil;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class EasyTabBarController extends Activity {
	public final static int DEFAULT_TAB_BAR_HEIGHT = 98;

	protected RelativeLayout _contentLayout = null;
	protected RelativeLayout _tabBarLayout = null;
	protected RelativeLayout _tabContentContainerLayout = null;
	protected List<Class<? extends FragmentActivity>> _tabContentList = 
			new ArrayList<Class<? extends FragmentActivity>>();

	protected View _tabBarView = null;

	protected FrameLayout _tabContentLayout = null;
	private LocalActivityManager _localActivityManager = null;
	private List<TabContentSpec> _tabContentSpecList = new ArrayList<TabContentSpec>();
	private View _launchedView = null;
	private int _selectedTabIndex = 0;
	private boolean _viewInited = false;
	
	private int _tabBarHeight = DEFAULT_TAB_BAR_HEIGHT;
	
	public int getTabBarHeight() {
		return _tabBarHeight;
	}
	
	/**
	 * 
	 * @param tabBarHeight tab bar高度
	 */
	public void setTabBarHeight(int tabBarHeight) {
		_tabBarHeight = tabBarHeight;
	}
	
	/**
	 * @return tab bar以及tab content都在这个layout中
	 */
	public RelativeLayout getContentLayout() {
		return _contentLayout;
	}
	public void setContentLayout(RelativeLayout contentLayout) {
		_contentLayout = contentLayout;
	}
	
	/**
	 * @return tab bar所在的layout
	 */
	public RelativeLayout getTabBarLayout() {
		return _tabBarLayout;
	}
	public void setTabBarLayout(RelativeLayout tabBarLayout) {
		_tabBarLayout = tabBarLayout;
	}
	
	/**
	 * @return tab content所在layout
	 */
	public RelativeLayout getTabContentContainerLayout() {
		return _tabContentContainerLayout;
	}
	public void setTabContentContainerLayout(
			RelativeLayout tabContentContainerLayout) {
		_tabContentContainerLayout = tabContentContainerLayout;
	}
	
	/**
	 * 
	 * @return 当前选中的tab下标(0开始)
	 */
	public int getSelectedTabIndex() {
		return _selectedTabIndex;
	}
	
	public View getTabBarView() {
		return _tabBarView;
	}
	
	/**
	 * 需要在onCreate()之前设置
	 * @param tabBarView tab bar的View
	 */
	public void setTabBarView(View tabBarView) {
		_tabBarView = tabBarView;
	}
	public List<Class<? extends FragmentActivity>> getTabContentList() {
		return _tabContentList;
	}
	
	/**
	 * 
	 * @param tabContentList tab内容页列表
	 */
	public void setTabContentList(
			List<Class<? extends FragmentActivity>> tabContentList) {
		_tabContentList = tabContentList;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		_localActivityManager = new LocalActivityManager(this, false);
		_localActivityManager.dispatchCreate(savedInstanceState);
		
		if(_contentLayout == null) {
	        {
	            _contentLayout = new RelativeLayout(this);
	    		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
	    				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
	    		_contentLayout.setLayoutParams(layoutParams);
	        }
	        
	        {
	    		_tabBarLayout = new RelativeLayout(this);
	    		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
	    				RelativeLayout.LayoutParams.FILL_PARENT, _tabBarHeight);
	    		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	    		_tabBarLayout.setLayoutParams(layoutParams);
	    		_tabBarLayout.setId(10);
	    		
	    		_contentLayout.addView(_tabBarLayout);
	        }
			
	        {
	        	_tabContentContainerLayout = new RelativeLayout(this);
	    		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
	    				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
	    		layoutParams.addRule(RelativeLayout.ABOVE, _tabBarLayout.getId());
	    		_tabContentContainerLayout.setLayoutParams(layoutParams);

	    		_contentLayout.addView(_tabContentContainerLayout);
	        }
		
	        {
	    		_tabContentLayout = new FrameLayout(this);
	    		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
	    				FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
	    		_tabContentLayout.setLayoutParams(layoutParams);
	    		
	    		_tabContentContainerLayout.addView(_tabContentLayout);
	        }
			
	        _tabBarLayout.addView(_tabBarView);
		}
		
		setContentView(_contentLayout);
		
        for(int i = 0; i < _tabContentList.size(); i++) {
        	addTabContentSpec(Integer.toString(i), _tabContentList.get(i));
        }
        
        _viewInited = true;

        setSelectedTabIndexImp(_selectedTabIndex);
	}
	
	private void addTabContentSpec(String tag, Class<? extends FragmentActivity> contentClass) {
        _tabContentSpecList.add(new TabContentSpec(tag, new Intent(this, contentClass)));
	}
	
	
	@Override
	protected void onPause() {
		_localActivityManager.dispatchPause(isFinishing());

		super.onPause();
	}
	
	@Override
	protected void onResume() {
		_localActivityManager.dispatchResume();

		super.onResume();
	}
	
	/**
	 * 设置选中tab页
	 * @param tabIndex 下标(0开始)
	 */
	public void setSelectedTabIndex(final int tabIndex) {
		setSelectedTabIndexImp(tabIndex);
	}

	public void setSelectedTabIndexImp(final int tabIndex) {
		_selectedTabIndex = tabIndex;

		if(!_viewInited) {
			return;
		}
		
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				TabContentSpec tabContentSpec = _tabContentSpecList.get(_selectedTabIndex);
				final Window w = _localActivityManager.startActivity(
						tabContentSpec.getTag(), tabContentSpec.getIntent());
				View wd = null;
				if(w != null) {
					wd = w.getDecorView();
				}
				
				//hide old
				if(_launchedView != null && _launchedView != wd) {
					if(_launchedView.getParent() != null) {
						_tabContentLayout.removeView(_launchedView);
					}
				}
				
				_launchedView = wd;
				
				//show new
				if(_launchedView != null) {
					_launchedView.setVisibility(View.VISIBLE);
					_launchedView.setFocusableInTouchMode(true);
					((ViewGroup)_launchedView).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
					
					if(_launchedView.getParent() == null) {
						_tabContentLayout.addView(_launchedView, new ViewGroup.LayoutParams(
								ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
					}
				}
			}
		});
		
	}
	
	/**
	 * tab bar隐藏状态
	 * @return true:隐藏 false:显示
	 */
	public boolean isTabBarHidden() {
		return (_tabBarLayout.getVisibility() == RelativeLayout.INVISIBLE);
	}
	
	/**
	 * 设置tab bar隐藏
	 * @param hidden
	 */
	public void setTabBarHidden(final boolean hidden) {
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if(hidden) {
					_tabBarLayout.setVisibility(RelativeLayout.INVISIBLE);
		    		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
		    				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
		    		_tabContentContainerLayout.setLayoutParams(layoutParams);
				} else {
					_tabBarLayout.setVisibility(RelativeLayout.VISIBLE);
		    		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
		    				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
		    		layoutParams.addRule(RelativeLayout.ABOVE, _tabBarLayout.getId());
		    		_tabContentContainerLayout.setLayoutParams(layoutParams);
				}
			}
		});
	}
}
