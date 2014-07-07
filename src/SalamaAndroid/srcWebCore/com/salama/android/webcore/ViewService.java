package com.salama.android.webcore;

public abstract class ViewService {

	private LocalWebViewFragment _thisView;

	/**
	 * 取得当前View
	 * @return
	 */
	public LocalWebViewFragment getThisView() {
		return _thisView;
	}

	/**
	 * 设置当前View
	 * @param thisView
	 */
	public void setThisView(LocalWebViewFragment thisView) {
		_thisView = thisView;
	}
	
	/**
	 * events of View
	 */
	public abstract void viewDidLoad(); 
	
	/**
	 * events of View
	 */
	public abstract void viewDidUnload(); 
	
	/**
	 * events of View
	 */
	public abstract void viewWillUnload(); 
	
	/**
	 * events of View
	 */
	public abstract void viewWillAppear(); 
	
	/**
	 * events of View
	 */
	public abstract void viewWillDisappear(); 
	
	/**
	 * events of View
	 */
	public abstract void viewDidAppear(); 

	/**
	 * events of View
	 */
	public abstract void viewDidDisappear(); 
}
