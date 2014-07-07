package com.salama.android.dataservice;

public interface ResourceDownloadHandler {

	/**
	 * 下载资源文件
	 * @param resId 资源Id
	 * @return 文件内容
	 */
	//byte[] downloadByResId(String resId);
	boolean downloadByResId(String resId, String saveTo);
}
