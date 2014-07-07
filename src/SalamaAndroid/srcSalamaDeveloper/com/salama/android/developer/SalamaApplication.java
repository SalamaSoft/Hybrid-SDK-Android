package com.salama.android.developer;

import org.OpenUDID.IOpenUDIDInitCompleted;
import org.OpenUDID.OpenUDID_manager;

import android.util.Log;

import com.salama.android.support.ServiceSupportApplication;
import com.salama.android.util.SSLog;

public class SalamaApplication extends ServiceSupportApplication implements IOpenUDIDInitCompleted {
	private static String _udid = null;
	
	@Override
	public void onCreate() {
		super.onCreate();

		//init Open udid
		OpenUDID_manager.sync(this, this);
	}
	
	@Override
	public void openUDIDInitCompleted() {
		_udid = OpenUDID_manager.getOpenUDID();
		SSLog.i("SalamaApplication", "_udid:" + _udid);
	}

	/**
	 * 取得udid
	 * @return udid。采用openUDID。
	 */
	public static String getUDID() {
		return _udid;
	} 
	
}
