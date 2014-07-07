package com.salama.android.dataservice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import com.salama.android.util.ResourceFileManager;
import com.salama.android.util.http.HttpClientUtil;
import com.salama.android.util.http.MultiPartFile;

public class WebService {

	protected int _requestTimeoutSeconds;

	protected ResourceFileManager _resourceFileManager;

	/**
	 * 取得请求timeout秒数
	 * @return 请求timeout秒数
	 */
	public int getRequestTimeoutSeconds() {
		return _requestTimeoutSeconds;
	}

	/**
	 * 设置请求timeout秒数
	 * @param requestTimeoutSeconds 请求timeout秒数
	 */
	public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
		_requestTimeoutSeconds = requestTimeoutSeconds;
		HttpClientUtil.setRequestTimeout(_requestTimeoutSeconds * 1000);
	}

	/**
	 * 取得资源管理器
	 * @return 资源管理器
	 */
	public ResourceFileManager getResourceFileManager() {
		return _resourceFileManager;
	}

	/**
	 * 设置资源管理器
	 * @param resourceFileManager 资源管理器
	 */
	public void setResourceFileManager(ResourceFileManager resourceFileManager) {
		_resourceFileManager = resourceFileManager;
	}

	/**
	 * 执行基本方法
	 * @param url URL
	 * @param isPost 是否POST
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @return 返回结果
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public String doBasic(String url, boolean isPost, List<String> paramNames,
			List<String> paramValues) throws ClientProtocolException,
			IOException {
		if (isPost) {
			return HttpClientUtil.doPost(url, paramNames, paramValues, null);
		} else {
			return HttpClientUtil.doGet(url, paramNames, paramValues);
		}
	}

	/**
	 * 执行GET方法
	 * @param url URL 
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @return 返回结果
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public String doGet(String url, List<String> paramNames,
			List<String> paramValues) throws ClientProtocolException,
			IOException {
		return HttpClientUtil.doGet(url, paramNames, paramValues);
	}

	/**
	 * 执行POST方法
	 * @param url URL 
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @return 返回结果
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public String doPost(String url, List<String> paramNames,
			List<String> paramValues) throws ClientProtocolException,
			IOException {
		return HttpClientUtil.doPost(url, paramNames, paramValues, null);
	}

	/**
	 * 执行下载(POST方式)
	 * @param url URL 
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @return 返回结果(文件内容)
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public byte[] doDownload(String url, List<String> paramNames,
			List<String> paramValues) throws ClientProtocolException,
			IOException {
		return HttpClientUtil
				.doPostDownload(url, paramNames, paramValues, null);
	}

	/**
	 * 执行下载(POST方式)
	 * @param url URL 
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @param saveTo 保存路径
	 * @return 是否成功
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public boolean doDownloadToSave(String url, List<String> paramNames,
			List<String> paramValues, String saveTo)
			throws ClientProtocolException, IOException {
		return HttpClientUtil.doPostDownloadToSave(url, paramNames, paramValues, null, saveTo);
	}

	/**
	 * 执行上传
	 * @param url URL 
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @param multiPartNames 上传文件名列表
	 * @param multiPartFilePaths 上传文件路径列表
	 * @return 返回结果
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public String doUpload(String url, List<String> paramNames,
			List<String> paramValues, List<String> multiPartNames,
			List<String> multiPartFilePaths) throws ClientProtocolException,
			IOException {
		List<MultiPartFile> multiPartFileList = getMultiPartFileListFromFilePaths(
				multiPartNames, multiPartFilePaths);

		return HttpClientUtil.doPost(url, paramNames, paramValues,
				multiPartFileList);
	}

	/**
	 * 执行上传并下载文件
	 * @param url URL 
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @param multiPartNames 上传文件名列表
	 * @param multiPartFilePaths 上传文件路径列表
	 * @return 返回结果(文件内容)
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public byte[] doUploadAndDownload(String url, List<String> paramNames,
			List<String> paramValues, List<String> multiPartNames,
			List<String> multiPartFilePaths) throws ClientProtocolException,
			IOException {
		List<MultiPartFile> multiPartFileList = getMultiPartFileListFromFilePaths(
				multiPartNames, multiPartFilePaths);

		return HttpClientUtil.doPostDownload(url, paramNames, paramValues,
				multiPartFileList);
	}

	/**
	 * 执行上传并下载文件
	 * @param url URL 
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @param multiPartNames 上传文件名列表
	 * @param multiPartFilePaths 上传文件路径列表
	 * @param saveTo 下载文件保存路径
	 * @return 是否成功
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public boolean doUploadAndDownloadToSave(String url,
			List<String> paramNames, List<String> paramValues,
			List<String> multiPartNames, List<String> multiPartFilePaths,
			String saveTo) throws ClientProtocolException, IOException {
		List<MultiPartFile> multiPartFileList = getMultiPartFileListFromFilePaths(
				multiPartNames, multiPartFilePaths);
		return HttpClientUtil.doPostDownloadToSave(url, paramNames, paramValues, multiPartFileList, saveTo);
	}

	/**
	 * 下载资源文件 
	 * @param url URL 
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @param saveToResId 保存用资源Id
	 * @return 是否成功
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public boolean doDownloadResource(String url, List<String> paramNames,
			List<String> paramValues, String saveToResId)
			throws ClientProtocolException, IOException {
		return HttpClientUtil.doPostDownloadToSave(url, paramNames, paramValues, null, 
				_resourceFileManager.getResourceFilePath(saveToResId));
	}
	
	/**
	 * 上传资源文件 
	 * @param url URL 
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @param multiPartNames 上传文件名列表
	 * @param multiPartResIds 上传文件资源Id
	 * @return 返回结果
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public String doUploadResource(String url, List<String> paramNames,
			List<String> paramValues, List<String> multiPartNames,
			List<String> multiPartResIds) throws ClientProtocolException,
			IOException {
		List<MultiPartFile> multiPartFileList = getMultiPartFileListFromResIds(
				multiPartNames, multiPartResIds);

		return HttpClientUtil.doPost(url, paramNames, paramValues,
				multiPartFileList);
	}

	/**
	 * 上传资源文件并下载
	 * @param url URL 
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @param multiPartNames 上传文件名列表
	 * @param multiPartResIds 上传文件资源Id
	 * @param saveToResId 下载文件保存用资源Id
	 * @return 是否成功
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public boolean doUploadAndDownloadResource(String url,
			List<String> paramNames, List<String> paramValues,
			List<String> multiPartNames, List<String> multiPartResIds,
			String saveToResId) throws ClientProtocolException, IOException {
		List<MultiPartFile> multiPartFileList = getMultiPartFileListFromResIds(
				multiPartNames, multiPartResIds);

		return HttpClientUtil.doPostDownloadToSave(url, paramNames, paramValues, multiPartFileList,
				_resourceFileManager.getResourceFilePath(saveToResId));
	}

	private static void saveToFilePath(byte[] data, String filePath)
			throws IOException {
		FileOutputStream resFileOS = null;

		try {
			resFileOS = new FileOutputStream(new File(filePath));
			resFileOS.write(data);
			resFileOS.flush();
		} finally {
			try {
				resFileOS.close();
			} catch (Exception e) {
			}
		}
	}

	private static List<MultiPartFile> getMultiPartFileListFromFilePaths(
			List<String> multiPartNames, List<String> multiPartFilePaths) {
		List<MultiPartFile> multiPartFileList = new ArrayList<MultiPartFile>();

		MultiPartFile multiPart = null;
		if(multiPartNames != null) {
			for (int i = 0; i < multiPartNames.size(); i++) {
				multiPart = new MultiPartFile();
				multiPart.setName(multiPartNames.get(i));
				multiPart.setFile(new File(multiPartFilePaths.get(i)));

				multiPartFileList.add(multiPart);
			}
		}

		return multiPartFileList;
	}

	private List<MultiPartFile> getMultiPartFileListFromResIds(
			List<String> multiPartNames, List<String> multiPartResIds) {
		List<MultiPartFile> multiPartFileList = new ArrayList<MultiPartFile>();

		MultiPartFile multiPart = null;
		if(multiPartNames != null) {
			for (int i = 0; i < multiPartNames.size(); i++) {
				multiPart = new MultiPartFile();
				multiPart.setName(multiPartNames.get(i));

				multiPart.setFile(new File(_resourceFileManager
						.getResourceFilePath(multiPartResIds.get(i))));

				multiPartFileList.add(multiPart);
			}
		}

		return multiPartFileList;
	}

}
