package com.salama.android.webviewutil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import MetoXML.XmlSerializer;
import android.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.salama.android.support.ServiceSupportApplication;
import com.salama.android.support.ServiceSupportUtil;
import com.salama.android.util.SSLog;
import com.salama.android.webcore.LocalWebViewFragment;

public class BaseWebViewController extends LocalWebViewFragment {
	private final static String LOG_TAG = "BaseWebViewController";
	
	/**
	 * 广播传递异步调用返回结果的参数名
	 */
	public static final String DATA_SERVICE_NOTIFICATION_USER_INFO_RESULT = "result";
		
	//private int _senderTagSeed;
	//private ConcurrentHashMap<String, String> _senderTagToJSCallBackMapping = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> _notificationNameToJSCallBackMapping = new ConcurrentHashMap<String, String>();
	
	protected ProgressDialog _spinnerForWaiting = null;

	public BaseWebViewController() {
		super();
	}
	
	public BaseWebViewController(String viewServiceClassName) {
		super(viewServiceClassName);
	}
	
	@Override
	public void onDestroy() {
		//clear receivers
		for(int i = 0; i < _localBroadcastReceiverList.size(); i++) {
			LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(_localBroadcastReceiverList.get(i));
		}
		_localBroadcastReceiverList.clear();
		
		super.onDestroy();
	}
	
	private List<BroadcastReceiver> _localBroadcastReceiverList = new ArrayList<BroadcastReceiver>();
	private class NotifySupportBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				final String jsCallBack = _notificationNameToJSCallBackMapping.get(intent.getAction());
				
				if(jsCallBack != null) {
					Object userInfoObj = ServiceSupportApplication.singleton().getWrappedDataFromLocalBroadcast(
							intent, DATA_SERVICE_NOTIFICATION_USER_INFO_RESULT);
					final List<String> params = new ArrayList<String>();
					
					if(userInfoObj != null) {
						if(userInfoObj.getClass().isAssignableFrom(String.class)) {
							params.add((String)userInfoObj);
						} else {
							try {
								String jsParamXml = XmlSerializer.objectToString(
										userInfoObj, userInfoObj.getClass(), 
										false, false);
								params.add(jsParamXml);
							} catch(Exception e) {
								Log.e(LOG_TAG, "onReceive()", e);
							}
						}
					}

					BaseWebViewController.this.getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							try {
								callJavaScript(jsCallBack, params);
							} catch(Throwable e) {
								Log.e(LOG_TAG, "", e);
							}
						}
					});
				}
				
			} catch(Throwable e) {
				Log.e(LOG_TAG, "", e);
			}
		}
	};
	
	
	/**
	 * 注册JavaScript函数,同通知名绑定
	 * @param notificationName 通知名
	 * @param jsCallBack JavaScript回调函数
	 */
	public void registerJSCallBackToNotification(String notificationName, String jsCallBack) {
		if(notificationName == null || notificationName.length() == 0
				|| jsCallBack == null || jsCallBack.length() == 0) {
			return;
		}
		
		if(!_notificationNameToJSCallBackMapping.containsKey(notificationName)) {
			_notificationNameToJSCallBackMapping.put(notificationName, jsCallBack);
			SSLog.d(LOG_TAG, 
					"registerJSCallBackToNotification() notificationName:" + notificationName 
					+ " jsCallBack:" + jsCallBack);
			
			//unregister localBroadcast
			//LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(_localBroadcastReceiver);
			
			IntentFilter localBroadcastIntentFilter = new IntentFilter();
			localBroadcastIntentFilter.addAction(notificationName);
			
			NotifySupportBroadcastReceiver receiver = new NotifySupportBroadcastReceiver();
			_localBroadcastReceiverList.add(receiver);
			
			LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
					receiver, localBroadcastIntentFilter);
		}
	}

	/**
	 * 显示Alert画面
	 * @param title 标题
	 * @param message 消息内容
	 * @param buttonTitleList 按钮标题列表
	 * @param jsCallBack JavaScript函数
	 */
	public void showAlert(final String title, final String message, final List<String> buttonTitleList, final String jsCallBack) {
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				try {
					AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
					AlertDialog alert = alertBuilder.create();
					
					alert.setTitle(title);
					alert.setMessage(message);
					
					final String jsCallBackTmp = jsCallBack;
					DialogInterface.OnClickListener listener = null;
					
					if(jsCallBackTmp != null && jsCallBackTmp.length() > 0) {
						listener = new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								callJavaScript(jsCallBackTmp, ServiceSupportUtil.newList(
										new String[]{Integer.toString(Math.abs(which)-1)})); 
							}
						}; 
					}
					
					if(buttonTitleList.size() > 0) {
						alert.setButton(buttonTitleList.get(0), listener);
					}
					if(buttonTitleList.size() > 1) {
						alert.setButton2(buttonTitleList.get(1), listener);
					}
					if(buttonTitleList.size() > 2) {
						alert.setButton3(buttonTitleList.get(2), listener);
					}
					
					alert.show();
				} catch(Throwable e) {
					Log.e(LOG_TAG, "", e);
				}
			}
		});
		
	}
	
	/**
	 * 显示装载等待动画
	 * @param spinnerUIStyle 风格。此参数仅为保持和iOS版兼容，Android中不起作用。
	 */
	public void startWaitingSpinnerAnimating(int spinnerUIStyle) {
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				try {
					if(_spinnerForWaiting == null) {
						_spinnerForWaiting = new ProgressDialog(getActivity());
						//_spinnerForWaiting.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						//_spinnerForWaiting.requestWindowFeature(Window.FEATURE_NO_TITLE);
						//_spinnerForWaiting.setTitle(null);
						_spinnerForWaiting.setCancelable(true);
						
						//Layout --------------------------------------
						RelativeLayout layout = new RelativeLayout(getActivity());
						RelativeLayout.LayoutParams layoutParam = new RelativeLayout.LayoutParams(
								RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
						layoutParam.addRule(RelativeLayout.CENTER_IN_PARENT);
						layout.setLayoutParams(layoutParam);
						layout.setBackgroundColor(Color.TRANSPARENT);
						
						//progressBar ---------------------------------
						ProgressBar progressBar = new ProgressBar(getActivity(),
								null, android.R.attr.progressBarStyleLarge);
						RelativeLayout.LayoutParams layoutParam2 = new RelativeLayout.LayoutParams(
								RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
						//layoutParam2.addRule(RelativeLayout.CENTER_IN_PARENT);
						progressBar.setLayoutParams(layoutParam2);
						
						//progressBar.setBackgroundColor(Color.TRANSPARENT);
						progressBar.setIndeterminate(true);
						//progressBar.setVisibility(View.VISIBLE);

						layout.addView(progressBar);

						_spinnerForWaiting.show();

						_spinnerForWaiting.setContentView(layout);
					} else {
						_spinnerForWaiting.cancel();
						_spinnerForWaiting.show();
					}
				} catch(Exception e) {
					SSLog.e(LOG_TAG, "startWaitingSpinnerAnimating()", e);
				}
			}
		});
	}

	/**
	 * 停止装载等待动画
	 */
	public void stopWaitingSpinnerAnimating() {
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				try {
					if(_spinnerForWaiting != null) {
						_spinnerForWaiting.cancel();
					}
				} catch(Throwable e) {
					SSLog.e(LOG_TAG, "stopWaitingSpinnerAnimating()", e);
				}
			}
		});
	}

}
