package com.salama.android.developer.util.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.os.Build;

import com.salama.android.developer.SalamaAppService;
import com.salama.android.util.SSLog;
import com.salama.android.util.http.MultiPartFile;
import com.salama.android.util.http.EasySSLSocketFactory;

public class SalamaHttpClientUtil {
	public static final String DEFAULT_CHARSET = HTTP.UTF_8;
	public static final Charset DefaultCharset = Charset.forName(DEFAULT_CHARSET);
	
	public static final int DEFAULT_CONNECTION_POOL_TIMEOUT_MS = 10000;
	public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 10000;
	public static final int DEFAULT_REQUEST_TIMEOUT_MS = 30000;
	
	private static final int RESPONSE_STATUS_SUCCESS = 200;

	//Salama Easy App在AppToken认证不通过的时候，采用code 401来表征
	private final static int HTTP_STATUS_CODE_APP_TOKEN_INVALID = 405;
	
	
	private static ClientConnectionManager connectionManager;
	private static HttpParams httpParams = new BasicHttpParams();
	
	//2K byte
	private static final int TEMP_BUFFER_DEFAULT_LENGTH = 2048;
	

	public static void initHttpParams(
			int connectionPooltimeoutMS,
			int httpConnectionTimeoutMS, int httpRequestTimeoutMS,
			int httpPort, int httpsPort) {
		// 设置一些基本参数
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(httpParams, DEFAULT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(httpParams, true);
				
		// HttpProtocolParams
		// .setUserAgent(
		// params,
		// "Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) "
		// +
		// "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
		
		HttpProtocolParams.setUserAgent(httpParams, 
				"Android " + Build.VERSION.RELEASE + ";" 
				+ "API " + Build.VERSION.SDK_INT + ";salama;");

		// 超时设置
		setTimeout(DEFAULT_CONNECTION_POOL_TIMEOUT_MS, DEFAULT_CONNECTION_TIMEOUT_MS, DEFAULT_REQUEST_TIMEOUT_MS);

		// 设置我们的HttpClient支持HTTP和HTTPS两种模式
		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), httpPort));
		schReg.register(new Scheme("https", new EasySSLSocketFactory(), httpsPort));

		connectionManager = new ThreadSafeClientConnManager(httpParams, schReg);
	}
	
	public static void setUserAgent(String userAgent) {
		HttpProtocolParams.setUserAgent(httpParams, userAgent);
	}
	
	public static void setTimeout(int connectionPooltimeoutMS,
			int httpConnectionTimeoutMS, int httpRequestTimeoutMS) {
		ConnManagerParams.setTimeout(httpParams, connectionPooltimeoutMS);
		/* 连接超时 */
		HttpConnectionParams.setConnectionTimeout(httpParams,
				httpConnectionTimeoutMS);
		/* 请求超时 */
		HttpConnectionParams.setSoTimeout(httpParams, httpRequestTimeoutMS);
	}

	public static void setRequestTimeout(int httpRequestTimeoutMS) {
		HttpConnectionParams.setSoTimeout(httpParams, httpRequestTimeoutMS);
	}

	public static Object doBasicGet(boolean isDownload, String url,
			List<String> paramNames, List<String> paramValues, 
			List<BasicNameValuePair> overrideHeaders) throws ClientProtocolException, IOException {
		return doBasicGet(isDownload, url, paramNames, paramValues, overrideHeaders, null);
	}
	
	public static boolean doBasicGetDownloadToSave(String url,
			List<String> paramNames, List<String> paramValues, 
			String saveTo) throws ClientProtocolException, IOException {
		return (doBasicGet(true, url, paramNames, paramValues, null, saveTo) != null);
	}
	
	public static boolean doBasicGetDownloadToSave(String url,
			List<String> paramNames, List<String> paramValues, 
			List<BasicNameValuePair> overrideHeaders,
			String saveTo) throws ClientProtocolException, IOException {
		return (doBasicGet(true, url, paramNames, paramValues, overrideHeaders, saveTo) != null);
	}
	
	private static Object doBasicGet(boolean isDownload, String url,
			List<String> paramNames, List<String> paramValues, 
			List<BasicNameValuePair> overrideHeaders,
			String saveTo) throws ClientProtocolException, IOException {
		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		//封装请求参数
		if(paramNames != null) {
			for (int i = 0; i < paramNames.size(); i++) {
				pairs.add(new BasicNameValuePair(paramNames.get(i), paramValues
						.get(i)));
			}
		}
		
		//URL Params
		StringBuilder urlWithParams = new StringBuilder(url);
		if(url.indexOf("?") < 0) {
			urlWithParams.append("?");
		}
		urlWithParams.append(URLEncodedUtils.format(pairs, DEFAULT_CHARSET));
		
		//Http client
		HttpGet request = new HttpGet(urlWithParams.toString());

		try {
			addDefaultHeaders(request, overrideHeaders);
			
			HttpResponse response = getHttpClient().execute(request);

			if (response.getStatusLine().getStatusCode() == RESPONSE_STATUS_SUCCESS) {
				if(isDownload && saveTo != null && saveTo.length() > 0) {
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(saveTo);
						int downloadLen = saveResponseContent(response.getEntity(), fos);

						if(downloadLen > 0) {
							return Boolean.valueOf(true);
						} else {
							return null;
						}
					} finally {
						try {
							fos.close();
						} catch(Exception e) {
						}
					}
				} else {
					byte[] bResult = getResponseContent(response.getEntity());
					if (bResult != null) {
						if (isDownload) {
							return bResult;
						} else {
							return new String(bResult, DEFAULT_CHARSET);
						}
					} else {
						return null;
					}
				}
			} else {
				if(response.getStatusLine().getStatusCode() == HTTP_STATUS_CODE_APP_TOKEN_INVALID) {
					SalamaAppService.singleton().appLogin();
				}
				
				return null;
			}
		} finally {
			try {
				request.abort();
			} catch(Exception e) {
			}
		}
	}
	
	public static Object doBasicPost(boolean isDownload, String url,
			List<String> paramNames, List<String> paramValues,
			List<BasicNameValuePair> overrideHeaders)
					throws ClientProtocolException, IOException {
		return doBasicPost(isDownload, url, paramNames, paramValues, overrideHeaders, null);
	}

	public static boolean doBasicPostDownloadToSave(String url,
			List<String> paramNames, List<String> paramValues,
			List<BasicNameValuePair> overrideHeaders,
			String saveTo) throws ClientProtocolException, IOException {
		return (doBasicPost(true, url, paramNames, paramValues, overrideHeaders, saveTo) != null);
	}
	
	public static Object doBasicPost(boolean isDownload, String url,
			List<String> paramNames, List<String> paramValues,
			List<BasicNameValuePair> overrideHeaders,
			String saveTo)
			throws ClientProtocolException, IOException {
		// 封装请求参数
		List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		
		if(paramNames != null) {
			for (int i = 0; i < paramNames.size(); i++) {
				pairs.add(new BasicNameValuePair(paramNames.get(i), paramValues
						.get(i)));
			}
		}
		
		// 把请求参数变成请求体部分
		UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(pairs, DEFAULT_CHARSET);
		
		HttpPost request = new HttpPost(url);
		
		try {
			addDefaultHeaders(request, overrideHeaders);

			request.setEntity(formEntity);
			HttpResponse response = getHttpClient().execute(request);

			if (response.getStatusLine().getStatusCode() == RESPONSE_STATUS_SUCCESS) {
				if(isDownload && saveTo != null && saveTo.length() > 0) {
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(saveTo);
						int downloadLen = saveResponseContent(response.getEntity(), fos);

						if(downloadLen > 0) {
							return Boolean.valueOf(true);
						} else {
							return null;
						}
					} finally {
						try {
							fos.close();
						} catch(Exception e) {
						}
					}
				} else {
					byte[] bResult = getResponseContent(response.getEntity());
					if (bResult != null) {
						if (isDownload) {
							return bResult;
						} else {
							return new String(bResult, DEFAULT_CHARSET);
						}
					} else {
						return null;
					}
				}
			} else {
				if(response.getStatusLine().getStatusCode() == HTTP_STATUS_CODE_APP_TOKEN_INVALID) {
					SalamaAppService.singleton().appLogin();
				}
				
				return null;
			}
		} finally {
			try {
				request.abort();
			} catch(Exception e) {
			}
		}
		
	}
	
	public static Object doMultipartPost(boolean isDownload, String url,
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues) throws ClientProtocolException, IOException {
		return doMultipartPost(isDownload, url, paramNames, paramValues, filePartValues, null);
	}
	
	public static boolean doMultipartPostDownloadToSave(String url,
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues,
			String saveTo) throws ClientProtocolException, IOException {
		return (doMultipartPost(true, url, paramNames, paramValues, filePartValues, saveTo) != null);
	}
	
	private static Object doMultipartPost(boolean isDownload, String url,
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues,
			String saveTo) throws ClientProtocolException, IOException {

		MultipartEntity multipartEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE, null, DefaultCharset);

		// 封装请求参数
		if(paramNames != null) {
			for (int i = 0; i < paramNames.size(); i++) {
				multipartEntity.addPart(paramNames.get(i), 
						new StringBody(paramValues.get(i), DefaultCharset));
			}
		}
		// 上传文件
		if(filePartValues != null) {
			for (int i = 0; i < filePartValues.size(); i++) {
				multipartEntity.addPart(filePartValues.get(i).getName(), new FileBody(
						filePartValues.get(i).getFile()));
			}
		}
		
		// 使用HttpPost对象设置发送的URL路径
		HttpPost request = new HttpPost(url);

		try {
			addPostMultipartHeaders(request);

			request.setEntity(multipartEntity);
			HttpResponse response = getHttpClient().execute(request);

			if (response.getStatusLine().getStatusCode() == RESPONSE_STATUS_SUCCESS) {
				if(isDownload && saveTo != null && saveTo.length() > 0) {
					FileOutputStream fos = null;
					try {
						fos = new FileOutputStream(saveTo);
						int downloadLen = saveResponseContent(response.getEntity(), fos);

						if(downloadLen > 0) {
							return Boolean.valueOf(true);
						} else {
							return null;
						}
					} finally {
						try {
							fos.close();
						} catch(Exception e) {
						}
					}
				} else {
					byte[] bResult = getResponseContent(response.getEntity());
					if (bResult != null) {
						if (isDownload) {
							return bResult;
						} else {
							return new String(bResult, DEFAULT_CHARSET);
						}
					} else {
						return null;
					}
				}
			} else {
				if(response.getStatusLine().getStatusCode() == HTTP_STATUS_CODE_APP_TOKEN_INVALID) {
					SalamaAppService.singleton().appLogin();
				}
				
				return null;
			}
		} finally {
			try {
				request.abort();
			} catch(Exception e) {
			}
		}
	
	}	

	
	private static HttpClient getHttpClient() {
		HttpClient customerHttpClient = new DefaultHttpClient(connectionManager,
				httpParams);

		return customerHttpClient;

	}
	
	private static void addDefaultHeaders(HttpRequestBase request) {
		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
		request.addHeader("Accept-Encoding", "gzip");
		request.addHeader("accept", "*/*");
	}
	
	private static void addDefaultHeaders(HttpRequestBase request, List<BasicNameValuePair> overrideHeaders) {
		if(overrideHeaders == null) {
			addDefaultHeaders(request);
		} else {
			BasicNameValuePair nameVal = null;
			for(int i = 0; i < overrideHeaders.size(); i++) {
				nameVal = overrideHeaders.get(i);
				request.addHeader(nameVal.getName(), nameVal.getValue());
			}
		}
	}
	
	private static void addPostMultipartHeaders(HttpRequestBase request) {
		//request.addHeader("Content-Type", "multipart/form-data");
		request.addHeader("Accept-Encoding", "gzip");
		request.addHeader("accept", "*/*");
	}

	private static byte[] getResponseContent(HttpEntity httpEntity) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		saveResponseContent(httpEntity, output);
		
		return output.toByteArray();
	}
	
	private static int saveResponseContent(HttpEntity httpEntity, OutputStream output) throws IOException {
		InputStream is = null;
		BufferedInputStream bis = null;
		
		try {
			is = httpEntity.getContent();
			bis = new BufferedInputStream(is);
			
			boolean isGzip = false;
			HeaderElement[] headers = httpEntity.getContentEncoding().getElements();
			if(headers != null) {
				for(int i = 0; i < headers.length; i++) {
					SSLog.d("HttpClientUtil", "HttpHeader " + headers[i].getName() + ":" + headers[i].getValue());
					if("gzip".equalsIgnoreCase(headers[i].getName())) {
						isGzip = true;
					}
				}
			}
			/*
			byte[] gzipSignBytes = new byte[2];
			bis.mark(2);
			
			int readed = bis.read(gzipSignBytes, 0, 2);
			
			if(readed == 0) {
				return 0;
			} else 
			*/	
			{
				//bis.reset();
				
				InputStream is2 = null;
				
				try {
					//if((readed == 2) && (gzipSignBytes[0] == 31) && (gzipSignBytes[1] == 139)) {
					if(isGzip) {
						//gzip
						is2 = new GZIPInputStream(bis);
					} else {
						is2 = bis;
					}
					
					return readAllBytesToOutput(is2, output);
				} finally {
					try {
						is2.close();
					} catch(Exception e) {
					}
				}
			}
		} finally {
			try {
				bis.close();
			} catch(Exception e) {
			}
			try {
				is.close();
			} catch(Exception e) {
			}
			try {
				httpEntity.consumeContent();
			} catch(Exception e) {
			}
		}
		
	}
	
	/*
	private static byte[] readAllBytes(InputStream is) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] tempBuffer = new byte[TEMP_BUFFER_DEFAULT_LENGTH];
		int readed;
		
		while(true) {
			readed = is.read(tempBuffer, 0, TEMP_BUFFER_DEFAULT_LENGTH);
			if(readed < 0) {
				//no more to read
				break;
			}

			if(readed != 0) {
				output.write(tempBuffer, 0, readed);
			}
		}
		
		return output.toByteArray();
	}
	*/
	private static int readAllBytesToOutput(InputStream is, OutputStream output) throws IOException {
		//ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] tempBuffer = new byte[TEMP_BUFFER_DEFAULT_LENGTH];
		int readed;
		int totalRead = 0;
		
		while(true) {
			readed = is.read(tempBuffer, 0, TEMP_BUFFER_DEFAULT_LENGTH);
			if(readed < 0) {
				//no more to read
				break;
			}
			
			totalRead += readed;

			if(readed != 0) {
				output.write(tempBuffer, 0, readed);
				output.flush();
			}
		}
		
		return totalRead;
	}
	
}
