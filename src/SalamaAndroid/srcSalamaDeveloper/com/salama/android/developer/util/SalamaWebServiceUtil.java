package com.salama.android.developer.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import com.salama.android.developer.SalamaAppService;
import com.salama.android.developer.user.SalamaUserService;
import com.salama.android.developer.util.http.SalamaHttpClientUtil;
import com.salama.android.util.http.MultiPartFile;

public class SalamaWebServiceUtil {
	private final static String PARAM_NAME_BUNDLE_ID = "bundleId";
	private final static String PARAM_NAME_APP_TOKEN = "appToken";
	private final static String PARAM_NAME_AUTH_TICKET = "authTicket";

	public static Object doBasicMethod(String url, boolean isDownload, boolean isPostMethod,
			List<String> paramNames, List<String> paramValues) throws ClientProtocolException, IOException {
		return doBasicMethod(url, isDownload, isPostMethod, paramNames, paramValues, 0);
	}
	
	/**
	 * 
	 * @param url
	 * @param isDownload
	 * @param isPostMethod
	 * @param paramNames
	 * @param paramValues
	 * @param requestTimeoutInterval 请求超时时间(单位:秒)
	 * @return web service返回值。类型为String 或者 byte[]。
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static Object doBasicMethod(String url, boolean isDownload, boolean isPostMethod,
			List<String> paramNames, List<String> paramValues, int requestTimeoutInterval) throws ClientProtocolException, IOException {
		List<String> paramNamesTmp = paramNames;
		List<String> paramValuesTmp = paramValues;
		
		if(paramNamesTmp == null) {
			paramNamesTmp = new ArrayList<String>();
		}
		if(paramValuesTmp == null) {
			paramValuesTmp = new ArrayList<String>();
		}
		
		editParamNameValues(paramNamesTmp, paramValuesTmp);
		
		if(requestTimeoutInterval > 0) {
			SalamaHttpClientUtil.setRequestTimeout(requestTimeoutInterval * 1000);
		}
		
		if(!isPostMethod) {
			return SalamaHttpClientUtil.doBasicGet(isDownload, 
					url, paramNamesTmp, paramValuesTmp, null);
		} else {
			return SalamaHttpClientUtil.doBasicPost(isDownload, 
					url, paramNamesTmp, paramValuesTmp, null);
		}
	}

	public static boolean doBasicMethodDownloadToSave(String url, boolean isPostMethod,
			List<String> paramNames, List<String> paramValues, String saveTo, 
			int requestTimeoutInterval) throws ClientProtocolException, IOException {
		List<String> paramNamesTmp = paramNames;
		List<String> paramValuesTmp = paramValues;
		
		if(paramNamesTmp == null) {
			paramNamesTmp = new ArrayList<String>();
		}
		if(paramValuesTmp == null) {
			paramValuesTmp = new ArrayList<String>();
		}
		
		editParamNameValues(paramNamesTmp, paramValuesTmp);
		
		if(requestTimeoutInterval > 0) {
			SalamaHttpClientUtil.setRequestTimeout(requestTimeoutInterval * 1000);
		}
		
		if(!isPostMethod) {
			return SalamaHttpClientUtil.doBasicGetDownloadToSave(url, paramNamesTmp, paramValuesTmp, saveTo);
		} else {
			return SalamaHttpClientUtil.doBasicPostDownloadToSave(url, paramNamesTmp, paramValuesTmp, null, saveTo);
		}
	}
	
	public static Object doMultipartMethod(String url, boolean isDownload, 
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues) throws ClientProtocolException, IOException {
		return doMultipartMethod(url, isDownload, paramNames, paramValues, filePartValues, 0);
	}
	
	public static Object doMultipartMethod(String url, boolean isDownload, 
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues,
			int requestTimeoutInterval) throws ClientProtocolException, IOException {
		List<String> paramNamesTmp = paramNames;
		List<String> paramValuesTmp = paramValues;
		
		if(paramNamesTmp == null) {
			paramNamesTmp = new ArrayList<String>();
		}
		if(paramValuesTmp == null) {
			paramValuesTmp = new ArrayList<String>();
		}
		
		editParamNameValues(paramNamesTmp, paramValuesTmp);

		if(requestTimeoutInterval > 0) {
			SalamaHttpClientUtil.setRequestTimeout(requestTimeoutInterval * 1000);
		}
		
		return SalamaHttpClientUtil.doMultipartPost(isDownload, url, paramNamesTmp, paramValuesTmp, filePartValues);
	}
	
	public static boolean doMultipartMethodDownloadToSave(String url, 
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues, String saveTo,
			int requestTimeoutInterval) throws ClientProtocolException, IOException {
		List<String> paramNamesTmp = paramNames;
		List<String> paramValuesTmp = paramValues;
		
		if(paramNamesTmp == null) {
			paramNamesTmp = new ArrayList<String>();
		}
		if(paramValuesTmp == null) {
			paramValuesTmp = new ArrayList<String>();
		}
		
		editParamNameValues(paramNamesTmp, paramValuesTmp);

		if(requestTimeoutInterval > 0) {
			SalamaHttpClientUtil.setRequestTimeout(requestTimeoutInterval * 1000);
		}
		
		return SalamaHttpClientUtil.doMultipartPostDownloadToSave(url, paramNamesTmp, paramValuesTmp, filePartValues, saveTo);
	}

	private static void editParamNameValues(List<String> paramNames, List<String> paramValues) {
		boolean isAppTokenAdded = false;
		boolean isAuthTicketAdded = false;
		String authTicket = "";
		if(SalamaUserService.singleton().getUserAuthInfo() != null 
				&& SalamaUserService.singleton().getUserAuthInfo().getAuthTicket() != null) {
			authTicket = SalamaUserService.singleton().getUserAuthInfo().getAuthTicket();
		}

		String paramName = null;
		for(int i = 0; i < paramNames.size(); i++) {
			paramName = paramNames.get(i);
			
			if(PARAM_NAME_APP_TOKEN.equals(paramName)) {
				paramValues.set(i, SalamaAppService.singleton().getAppToken());
				isAppTokenAdded = true;
			} else if(PARAM_NAME_AUTH_TICKET.equals(paramName)) {
				paramValues.set(i, authTicket);
				isAuthTicketAdded = true;
			}
		}
		
		if(!isAppTokenAdded) {
			paramNames.add(PARAM_NAME_APP_TOKEN);
			paramValues.add(SalamaAppService.singleton().getAppToken());
		}
		
		if(!isAuthTicketAdded) {
			paramNames.add(PARAM_NAME_AUTH_TICKET);
			paramValues.add(authTicket);
		}
		
		paramNames.add(PARAM_NAME_BUNDLE_ID);
		paramValues.add(SalamaAppService.singleton().getBundleId());
		
	}
}
