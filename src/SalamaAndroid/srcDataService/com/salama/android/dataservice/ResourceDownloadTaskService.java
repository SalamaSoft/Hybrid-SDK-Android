package com.salama.android.dataservice;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import android.util.Log;

import com.salama.android.support.ServiceSupportApplication;
import com.salama.android.util.ResourceFileManager;
import com.salama.android.util.SSLog;

public class ResourceDownloadTaskService {
	private ExecutorService _downloadQueue;
	
	private ResourceDownloadHandler _resourceDownloadHandler;
	
	private ResourceFileManager _resourceFileManager;
	
	private String _keyForNotificationUserObj;
	
	/**
	 * 取得ResourceFileManager
	 * @return ResourceFileManager
	 */
	public ResourceFileManager getResourceFileManager() {
		return _resourceFileManager;
	}

	/**
	 * 设置ResourceFileManager
	 * @param resourceFileManager ResourceFileManager
	 */
	public void setResourceFileManager(ResourceFileManager resourceFileManager) {
		_resourceFileManager = resourceFileManager;
	}

	/**
	 * 取得通知中的用户数据的存放名
	 * @return 通知中的用户数据的存放名
	 */
	public String getKeyForNotificationUserObj() {
		return _keyForNotificationUserObj;
	}

	/**
	 * 设置通知中的用户数据的存放名
	 * @param keyForNotificationUserObj 通知中的用户数据的存放名
	 */
	public void setKeyForNotificationUserObj(String keyForNotificationUserObj) {
		_keyForNotificationUserObj = keyForNotificationUserObj;
	}

	/**
	 * 取得资源下载处理器
	 * @return 资源下载处理器
	 */
	public ResourceDownloadHandler getResourceDownloadHandler() {
		return _resourceDownloadHandler;
	}

	/**
	 * 设置资源下载处理器
	 * @param resourceDownloadHandler 资源下载处理器
	 */
	public void setResourceDownloadHandler(
			ResourceDownloadHandler resourceDownloadHandler) {
		_resourceDownloadHandler = resourceDownloadHandler;
	}

	/**
	 * 取得下载队列
	 * @return 下载队列
	 */
	public ExecutorService getDownloadQueue() {
		return _downloadQueue;
	}

	/**
	 * 构造函数
	 */
	public ResourceDownloadTaskService() {
		_downloadQueue = ServiceSupportApplication.singleton().createSingleThreadPool();
	}
		
	/**
	 * 添加下载任务
	 * @param resId 资源Id
	 * @param notificationName 通知名
	 */
	public void addDownloadTaskWithResId(String resId, String notificationName) {
		if(resId == null || resId.length() == 0) {
			return;
		}
		
		SSLog.d("ResourceDownloadTaskService", "addDownloadTaskWithResId:" + resId);
		
		if(_resourceFileManager.isResourceFileExists(resId)) {
			SSLog.d("ResourceDownloadTaskService", "addDownloadTaskWithResId:" + resId + " already exists.");
			
			if(notificationName != null && notificationName.length() > 0) {
				//send notify
//				Intent intent = new Intent(notificationName);
//				intent.putExtra(_keyForNotificationUserObj, resId);
				
				ServiceSupportApplication.singleton().sendWrappedLocalBroadcast(
						notificationName, resId, _keyForNotificationUserObj);
				
			}
			
			return;
		}
		
		final String resIdTmp = resId;
		final String notificationNameTmp = notificationName;
		
		_downloadQueue.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
					
					//download
					//byte[] data = _resourceDownloadHandler.downloadByResId(resIdTmp);
					boolean success = _resourceDownloadHandler.downloadByResId(
							resIdTmp, _resourceFileManager.getResourceFilePath(resIdTmp));
					
					//if(data != null && data.length > 0) {
					if(success) {
						SSLog.d("ResourceDownloadTaskService", "addDownloadTaskWithResId:" + resIdTmp + " download succeeded.");
			            
			            //_resourceFileManager.saveResourceFileWithData(data, resIdTmp);
			            
			            //notify the invoker
						if(notificationNameTmp != null && notificationNameTmp.length() > 0) {
							//send notify
//							Intent intent = new Intent(notificationNameTmp);
//							intent.putExtra(_keyForNotificationUserObj, resIdTmp);
							
							ServiceSupportApplication.singleton().sendWrappedLocalBroadcast(
									notificationNameTmp, resIdTmp, _keyForNotificationUserObj);
						}
			            
					} else {
						SSLog.d("ResourceDownloadTaskService", "addDownloadTaskWithResId:" + resIdTmp + " download failed.");
	
			            //notify the invoker
						if(notificationNameTmp != null && notificationNameTmp.length() > 0) {
							//send notify
//							Intent intent = new Intent(notificationNameTmp);
//							intent.putExtra(_keyForNotificationUserObj, "");
							
							ServiceSupportApplication.singleton().sendWrappedLocalBroadcast(
									notificationNameTmp, resIdTmp, _keyForNotificationUserObj);
						}
					}
					
//					Thread.sleep(50);
//				} catch(InterruptedException e) {
//					Log.e("ResourceDownloadTaskService", "addDownloadTaskWithResId()", e); 
				} catch(Exception e) {
					Log.e("ResourceDownloadTaskService", "addDownloadTaskWithResId()", e); 
				}
			}
		});
		
	}

}
