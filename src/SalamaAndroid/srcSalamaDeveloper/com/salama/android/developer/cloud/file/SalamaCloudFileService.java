package com.salama.android.developer.cloud.file;

import java.io.File;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import MetoXML.XmlDeserializer;
import android.util.Log;

import com.salama.android.dataservice.SalamaDataService;
import com.salama.android.developer.SalamaAppService;
import com.salama.android.support.ServiceSupportApplication;
import com.salama.android.support.ServiceSupportUtil;
import com.salama.android.util.SSLog;
import com.salama.android.util.http.HttpClientUtil;

public class SalamaCloudFileService {
	public final static String EASY_APP_FILE_SERVICE = "com.salama.easyapp.service.FileService";
	
	private static SalamaCloudFileService _singleton = null;
	
	public static SalamaCloudFileService singleton() {
		if(_singleton == null) {
			_singleton = new SalamaCloudFileService();
		}
		
		return _singleton;
	}

	private SalamaCloudFileService() {
	}
	
	/**
	 * 下载文件(文件保存至默认的资源文件目录下:"/html/res/")
	 * @param fileId
	 * @return 文件操作结果
	 */
	public FileOperateResult downloadByFileId(String fileId) {
		String saveToFilePath = SalamaAppService.singleton().getDataService().getResourceFileManager().getResourceFilePath(fileId);
		return downloadByFileId(fileId, saveToFilePath);
	}

	/**
	 * 下载文件(文件保存至指定的文件路径)
	 * @param fileId
	 * @param saveToFilePath 文件保存路径
	 * @return 文件操作结果
	 */
	public FileOperateResult downloadByFileId(String fileId, String saveToFilePath) {
		FileOperateResult result = new FileOperateResult();
		result.setFileId(fileId);
		result.setSuccess(0);
		
		try {
			String downloadUrl = SalamaAppService.singleton().getWebService().doGet(
					SalamaAppService.singleton().getAppServiceHttpUrl(), 
					ServiceSupportUtil.newList(new String[]{"serviceType", "serviceMethod", "fileId"}), 
					ServiceSupportUtil.newList(new String[]{EASY_APP_FILE_SERVICE, "getFileDownloadUrl", fileId})
					);

			if(downloadUrl == null || downloadUrl.length() == 0) {
				return result;
			}
			
		    //download from OSS
			boolean success = HttpClientUtil.doGetMethodDownloadWithEncodedUrl(downloadUrl, saveToFilePath);
			if(success) {
				result.setSuccess(1);
			}
			
			return result;
		} catch (ClientProtocolException e) {
			Log.e("SalamaCloudFileService", "Error in downloadByFileId()", e);
			return result;
		} catch (IOException e) {
			Log.e("SalamaCloudFileService", "Error in downloadByFileId()", e);
			return result;
		}
		
	}
	
	/**
	 * 增加文件(上传)
	 * @param filePath
	 * @param aclRestrictUserRead 指定拥有读权限的用户。
	 * (多个用户id逗号分割，则指定的用户可以操作。该值未指定或空则仅仅数据创建者可以操作。'%'代表任何用户可以操作),
	 * @param aclRestrictUserUpdate 指定拥有更新权限的用户
	 * @param aclRestrictUserDelete 指定拥有删除权限的用户
	 * @return FileOperateResult(其中的fileId为服务器端分配的序列号)
	 */
	public FileOperateResult addFile(String filePath, 
			String aclRestrictUserRead, String aclRestrictUserUpdate, String aclRestrictUserDelete) {
		try {
			String resultXml = SalamaAppService.singleton().getWebService().doUpload(
					SalamaAppService.singleton().getAppServiceHttpUrl(), 
					ServiceSupportUtil.newList(new String[]{"serviceType", "serviceMethod", 
							"aclRestrictUserRead", "aclRestrictUserUpdate", "aclRestrictUserDelete"}), 
					ServiceSupportUtil.newList(new String[]{EASY_APP_FILE_SERVICE, "addFile", 
							aclRestrictUserRead==null?"":aclRestrictUserRead, 
							aclRestrictUserUpdate==null?"":aclRestrictUserUpdate, 
							aclRestrictUserDelete==null?"":aclRestrictUserDelete
									}),
					ServiceSupportUtil.newList(new String[]{"file"}), 
					ServiceSupportUtil.newList(new String[]{filePath})
					);
			
			return (FileOperateResult) XmlDeserializer.stringToObject(resultXml, FileOperateResult.class);
		} catch (Exception e) {
			Log.e("SalamaCloudFileService", "Error in addFile()", e);
			return null;
		}
		
	}
	
	/**
	 * 更新文件
	 * @param fileId
	 * @param filePath 上传文件路径
	 * @return FileOperateResult
	 */
	public FileOperateResult updateByFileId(String fileId, String filePath) {
		try {
			String resultXml = SalamaAppService.singleton().getWebService().doUpload(
					SalamaAppService.singleton().getAppServiceHttpUrl(), 
					ServiceSupportUtil.newList(new String[]{"serviceType", "serviceMethod", "fileId"}), 
					ServiceSupportUtil.newList(new String[]{EASY_APP_FILE_SERVICE, "updateFile", fileId}),
					ServiceSupportUtil.newList(new String[]{"file"}), 
					ServiceSupportUtil.newList(new String[]{filePath})
					);
			
			return (FileOperateResult) XmlDeserializer.stringToObject(resultXml, FileOperateResult.class);
		} catch (Exception e) {
			Log.e("SalamaCloudFileService", "Error in updateByFileId()", e);
			return null;
		}
	}
	
	/**
	 * 删除文件
	 * @param fileId
	 * @return FileOperateResult
	 */
	public FileOperateResult deleteByFileId(String fileId) {
		try {
			String resultXml = SalamaAppService.singleton().getWebService().doGet(
					SalamaAppService.singleton().getAppServiceHttpUrl(), 
					ServiceSupportUtil.newList(new String[]{"serviceType", "serviceMethod", "fileId"}), 
					ServiceSupportUtil.newList(new String[]{EASY_APP_FILE_SERVICE, "deleteFile", fileId})
					);
			
			return (FileOperateResult) XmlDeserializer.stringToObject(resultXml, FileOperateResult.class);
		} catch (Exception e) {
			Log.e("SalamaCloudFileService", "Error in deleteByFileId()", e);
			return null;
		}
	}
	
	/**
	 * 添加下载任务(自动保存至res目录)
	 * @param resId 资源Id
	 * @param notificationName 通知名
	 */
	public void addDownloadTaskWithFileId(String fileId, String notificationName) {
		String filePath = SalamaAppService.singleton().getDataService().getResourceFileManager().getResourceFilePath(fileId);
		addDownloadTaskWithFileId(fileId, filePath, notificationName);
	}
	
	/**
	 * 添加下载任务(保存至指定文件路径)
	 * @param resId 资源Id
	 * @param saveToFilePath 文件保存路径
	 * @param notificationName 通知名
	 */
	public void addDownloadTaskWithFileId(String fileId, String saveToFilePath, String notificationName) {
		if(fileId == null || fileId.length() == 0 
				|| saveToFilePath == null || saveToFilePath.length() == 0) {
			return;
		}
		
		SSLog.d("SalamaCloudFileService", "addDownloadTaskWithFileId:" + fileId);
		
		File resFile = new File(saveToFilePath);
		if(resFile.exists()) {
			SSLog.d("SalamaCloudFileService", "addDownloadTaskWithFileId:" + fileId + " already exists.");
			
			if(notificationName != null && notificationName.length() > 0) {
				//send notify
				ServiceSupportApplication.singleton().sendWrappedLocalBroadcast(
						notificationName, fileId, SalamaDataService.DATA_SERVICE_NOTIFICATION_USER_INFO_RESULT);
				
			}
			
			return;
		}
		
		final String resIdTmp = fileId;
		final String notificationNameTmp = notificationName;
		final String saveToFilePathTmp = saveToFilePath;
		
		SalamaAppService.singleton().getDataService().getResourceDownloadTaskService().getDownloadQueue().execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
					
					//download
					FileOperateResult result = downloadByFileId(resIdTmp, saveToFilePathTmp);
					
					if(result != null && result.getSuccess() == 1) {
						SSLog.d("SalamaCloudFileService", "addDownloadTaskWithFileId:" + resIdTmp + " download succeeded.");
			            
			            //notify the invoker
						if(notificationNameTmp != null && notificationNameTmp.length() > 0) {
							//send notify
							ServiceSupportApplication.singleton().sendWrappedLocalBroadcast(
									notificationNameTmp, resIdTmp, SalamaDataService.DATA_SERVICE_NOTIFICATION_USER_INFO_RESULT);
						}
			            
					} else {
						SSLog.d("SalamaCloudFileService", "addDownloadTaskWithFileId:" + resIdTmp + " download failed.");
	
			            //notify the invoker
						if(notificationNameTmp != null && notificationNameTmp.length() > 0) {
							//send notify
							ServiceSupportApplication.singleton().sendWrappedLocalBroadcast(
									notificationNameTmp, resIdTmp, SalamaDataService.DATA_SERVICE_NOTIFICATION_USER_INFO_RESULT);
						}
					}
					
//					Thread.sleep(50);
				} catch(Exception e) {
					Log.e("SalamaCloudFileService", "addDownloadTaskWithFileId()", e); 
				}
			}
		});
	}
}
