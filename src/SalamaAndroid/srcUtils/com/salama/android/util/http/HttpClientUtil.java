package com.salama.android.util.http;

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
import org.apache.http.conn.ssl.SSLSocketFactory;
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

import com.salama.android.util.SSLog;

import android.os.Build;
import android.util.Log;

public class HttpClientUtil {
	public static final String DEFAULT_CHARSET = HTTP.UTF_8;
	public static final Charset DefaultCharset = Charset.forName(DEFAULT_CHARSET);
	
	public static final int DEFAULT_CONNECTION_POOL_TIMEOUT_MS = 10000;
	public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 10000;
	public static final int DEFAULT_REQUEST_TIMEOUT_MS = 30000;
	
	public static final int DEFAULT_HTTP_PORT = 80;
	public static final int DEFAULT_HTTPS_PORT = 443;
	
	public static final int RESPONSE_STATUS_SUCCESS = 200;
	
	private static ClientConnectionManager connectionManager;
	private static HttpParams httpParams = new BasicHttpParams();
	
	//2K byte
	private static final int TEMP_BUFFER_DEFAULT_LENGTH = 2048;
	
	static {
		initHttpParams(
				DEFAULT_CONNECTION_POOL_TIMEOUT_MS, 
				DEFAULT_CONNECTION_TIMEOUT_MS, 
				DEFAULT_REQUEST_TIMEOUT_MS, 
				DEFAULT_HTTP_PORT, DEFAULT_HTTPS_PORT);
	};

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
	
	private HttpClientUtil() {

	}
	
	public static void setUserAgent(String userAgent) {
		HttpProtocolParams.setUserAgent(httpParams, userAgent);
	}

	/**
	 * 设置连接超时毫秒数
	 * @param httpConnectionTimeoutMS
	 */
	public static void setConnetionTimeout(int httpConnectionTimeoutMS) {
		HttpConnectionParams.setConnectionTimeout(httpParams,
				httpConnectionTimeoutMS);
	}

	/**
	 * 设置请求超时毫秒数
	 * @param httpRequestTimeoutMS
	 */
	public static void setRequestTimeout(int httpRequestTimeoutMS) {
		HttpConnectionParams.setSoTimeout(httpParams, httpRequestTimeoutMS);
	}

	/**
	 * 设置超时毫秒数
	 * @param connectionPooltimeoutMS 连接池超时毫秒数
	 * @param httpConnectionTimeoutMS 连接超时毫秒数
	 * @param httpRequestTimeoutMS 请求超时毫秒数
	 */
	public static void setTimeout(int connectionPooltimeoutMS,
			int httpConnectionTimeoutMS, int httpRequestTimeoutMS) {
		ConnManagerParams.setTimeout(httpParams, connectionPooltimeoutMS);

		HttpConnectionParams.setConnectionTimeout(httpParams,
				httpConnectionTimeoutMS);

		HttpConnectionParams.setSoTimeout(httpParams, httpRequestTimeoutMS);
	}
	
	public static HttpClient getHttpClient() {
		HttpClient customerHttpClient = new DefaultHttpClient(connectionManager,
				httpParams);

		return customerHttpClient;
	}

	public static void addDefaultHeaders(HttpRequestBase request) {
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
	
	public static void addPostMultipartHeaders(HttpRequestBase request) {
		//request.addHeader("Content-Type", "multipart/form-data");
		request.addHeader("Accept-Encoding", "gzip");
		request.addHeader("accept", "*/*");
	}

	public static byte[] getResponseContent(HttpEntity httpEntity) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		saveResponseContent(httpEntity, output);
		
		return output.toByteArray();
	}
	
	public static InputStream getResponseInputStream(HttpEntity httpEntity) throws IllegalStateException, IOException {
		boolean isGzip = false;
		if(httpEntity.getContentEncoding() != null) {
			HeaderElement[] headers = httpEntity.getContentEncoding().getElements();
			if(headers != null) {
				for(int i = 0; i < headers.length; i++) {
					SSLog.d("HttpClientUtil", "HttpHeader " + headers[i].getName() + ":" + headers[i].getValue());
					if("gzip".equalsIgnoreCase(headers[i].getName())) {
						isGzip = true;
					}
				}
			}
		}
		
		if(isGzip) {
			//gzip
			return new GZIPInputStream(httpEntity.getContent());
		} else {
			return httpEntity.getContent();
		}
	}

	private static int saveResponseContent(HttpEntity httpEntity, OutputStream output) throws IOException {
		InputStream is = null;
		
		try {
			is = getResponseInputStream(httpEntity);
			return readAllBytesToOutput(is, output);
		} finally {
			try {
				is.close();
			} catch(Throwable e) {
			}
		}
	}
	/*
	private static int saveResponseContent(HttpEntity httpEntity, OutputStream output) throws IOException {
		InputStream is = null;
		BufferedInputStream bis = null;
		
		try {
			is = httpEntity.getContent();
			bis = new BufferedInputStream(is);
			
			boolean isGzip = false;
			if(httpEntity.getContentEncoding() != null) {
				HeaderElement[] headers = httpEntity.getContentEncoding().getElements();
				if(headers != null) {
					for(int i = 0; i < headers.length; i++) {
						SSLog.d("HttpClientUtil", "HttpHeader " + headers[i].getName() + ":" + headers[i].getValue());
						if("gzip".equalsIgnoreCase(headers[i].getName())) {
							isGzip = true;
						}
					}
				}
			}
			//byte[] gzipSignBytes = new byte[2];
			//bis.mark(2);
			//int readed = bis.read(gzipSignBytes, 0, 2);
			//if(readed == 0) {
			//	return 0;
			//} else 
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
	*/
	
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

	public static int readAllBytesToOutput(InputStream is, OutputStream output) throws IOException {
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
	
	/**
	 * 执行GET方法
	 * @param url URL
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @return 返回结果(字符串)
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String doGet(String url,
			List<String> paramNames, List<String> paramValues) throws ClientProtocolException, IOException {
		return (String) doBasicGet(false, url, paramNames, paramValues);
	}

	/**
	 * 执行GET方法下载文件
	 * @param url URL
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @return 返回结果(byte数组)
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static byte[] doGetDownload(String url,
			List<String> paramNames, List<String> paramValues) throws ClientProtocolException, IOException {
		return (byte[]) doBasicGet(true, url, paramNames, paramValues);
	}

	/**
	 * 执行GET方法
	 * @param isDownload 是否下载(true:是 false:否)
	 * @param url URL
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @return 返回结果(字符串或byte数组)
	 * @throws ClientProtocolException
	 * @throws IOException
	 */ 
	public static Object doBasicGet(boolean isDownload, String url,
			List<String> paramNames, List<String> paramValues) throws ClientProtocolException, IOException {
		return doBasicGet(isDownload, url, paramNames, paramValues, null);
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
	
	/**
	 * 执行GET方法
	 * @param isDownload 是否下载(true:是 false:否)
	 * @param url URL
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @param overrideHeaders 自己指定请求头部内容
	 * @return 返回结果(字符串或byte数组)
	 * @throws ClientProtocolException
	 * @throws IOException
	 */ 
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
				return null;
			}
		} finally {
			try {
				request.abort();
			} catch(Exception e) {
			}
		}
	}
	
	/**
	 * 执行POST方法
	 * @param url URL
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @param filePartValues 上传文件列表。如果不为null，则使用MultiPart form提交。
	 * @return 返回结果(字符串)
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String doPost(String url,
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues) throws ClientProtocolException, IOException {
		if (filePartValues == null) {
			return (String) doBasicPost(false, url, paramNames,
					paramValues);
		} else {
			return (String) doMultipartPost(false, url, paramNames,
					paramValues, filePartValues);

		}

	}

	/**
	 * 执行POST方法下载文件
	 * @param url URL
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @param filePartValues 上传文件列表。如果不为null，则使用MultiPart form提交。
	 * @return 返回结果(byte数组)
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static byte[] doPostDownload(String url,
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues) throws ClientProtocolException, IOException {
		if (filePartValues == null) {
			return (byte[]) doBasicPost(true, url, paramNames,
					paramValues);
		} else {
			return (byte[]) doMultipartPost(true, url, paramNames,
					paramValues, filePartValues);
		}
	}

	public static boolean doPostDownloadToSave(String url,
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues,
			String saveTo) throws ClientProtocolException, IOException {
		if (filePartValues == null) {
			return doBasicPostDownloadToSave(url, paramNames, paramValues, saveTo);
		} else {
			return (doMultipartPost(true, url, paramNames, paramValues, filePartValues, saveTo) != null);
		}
	}
	
	/**
	 * 执行POST方法下载文件
	 * @param isDownload 是否下载(true:是 false:否)
	 * @param url URL
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @return 返回结果(字符串或byte数组)
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static Object doBasicPost(boolean isDownload, String url,
			List<String> paramNames, List<String> paramValues)
					throws ClientProtocolException, IOException {
		return doBasicPost(isDownload, url, paramNames, paramValues, null);
	}

	public static Object doBasicPost(boolean isDownload, String url,
			List<String> paramNames, List<String> paramValues,
			List<BasicNameValuePair> overrideHeaders)
			throws ClientProtocolException, IOException {
		return doBasicPost(isDownload, url, paramNames, paramValues, overrideHeaders, null);
	}
	
	public static boolean doBasicPostDownloadToSave(String url,
			List<String> paramNames, List<String> paramValues,
			String saveTo)
			throws ClientProtocolException, IOException {
		return (doBasicPost(true, url, paramNames, paramValues, null, saveTo) != null);
	}

	public static boolean doBasicPostDownloadToSave(String url,
			List<String> paramNames, List<String> paramValues,
			List<BasicNameValuePair> overrideHeaders, 
			String saveTo)
			throws ClientProtocolException, IOException {
		return (doBasicPost(true, url, paramNames, paramValues, overrideHeaders, saveTo) != null);
	}
	
	
	/**
	 * 执行POST方法下载文件
	 * @param isDownload 是否下载(true:是 false:否)
	 * @param url URL
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @param overrideHeaders 自己指定请求头部内容
	 * @return 返回结果(字符串或byte数组)
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private static Object doBasicPost(boolean isDownload, String url,
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
	
	/**
	 * 执行POST方法(MultiPart form)
	 * @param isDownload 是否下载(true:是 false:否)
	 * @param url URL
	 * @param paramNames 参数名列表
	 * @param paramValues 参数值列表
	 * @param filePartValues 上传文件列表。
	 * @return 返回结果(字符串或byte数组)
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static Object doMultipartPost(boolean isDownload, String url,
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
				return null;
			}
		} finally {
			try {
				request.abort();
			} catch(Exception e) {
			}
		}
	
	}
	
	/**
	 * 执行GET方法下载文件，已Encode过的URL。
	 * @param url URL(已经Encode过)
	 * @param downloadToPath 下载文件保存路径
	 * @return true:成功 false:失败
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static boolean doGetMethodDownloadWithEncodedUrl(String url, String downloadToPath) 
			throws ClientProtocolException, IOException {
		SSLog.d("HttpClientUtil", "doGetMethodDownloadWithEncodedUrl() url:".concat(url));
		
		//Http client
		HttpGet request = new HttpGet(url);

		try {
//			request.addHeader("Content-Type", "application/x-www-form-urlencoded");
			request.addHeader("Accept-Encoding", "gzip");
			request.addHeader("accept", "*/*");
			
			HttpResponse response = getHttpClient().execute(request);

			if (response.getStatusLine().getStatusCode() == RESPONSE_STATUS_SUCCESS) {
				FileOutputStream output = null;
				
				try {
					output = new FileOutputStream(downloadToPath);
					int downloadContentLen = saveResponseContent(response.getEntity(), output);
					
					if(downloadContentLen > 0) {
						return true;
					} else {
						SSLog.d("HttpClientUtil", "doGetMethodDownloadWithEncodedUrl() downloadContentLen is 0.");
						return false;
					}
				} catch(IOException ioe) {
					Log.e("HttpClientUtil", "", ioe);
					return false;
				} finally {
					try {
						output.close();
					} catch(Exception e) {}
				}
				
			} else {
				SSLog.d("HttpClientUtil", "doGetMethodDownloadWithEncodedUrl() failed: http status code is not success.");
				return false;
			}
		} finally {
			try {
				request.abort();
			} catch(Exception e) {
			}
		}
	}
	
	/**
	 * 执行GET方法下载文件，已Encode过的URL。
	 * @param url URL(已经Encode过)
	 * @return 下载的文件内容(byte数组)
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static byte[] doGetMethodDownloadWithEncodedUrl(String url) 
			throws ClientProtocolException, IOException {
		SSLog.d("HttpClientUtil", "doGetMethodDownloadWithEncodedUrl() url:".concat(url));
		
		//Http client
		HttpGet request = new HttpGet(url);

		try {
//			request.addHeader("Content-Type", "application/x-www-form-urlencoded");
			request.addHeader("Accept-Encoding", "gzip");
			request.addHeader("accept", "*/*");
			
			HttpResponse response = getHttpClient().execute(request);

			if (response.getStatusLine().getStatusCode() == RESPONSE_STATUS_SUCCESS) {
				try {
					byte[] bResult = getResponseContent(response.getEntity());
					if (bResult != null) {
						return bResult;
					} else {
						return null;
					}
				} catch(IOException ioe) {
					Log.e("HttpClientUtil", "", ioe);
					return null;
				}
			} else {
				SSLog.d("HttpClientUtil", "doGetMethodDownloadWithEncodedUrl() failed: http status code is not success.");
				return null;
			}
		} finally {
			try {
				request.abort();
			} catch(Exception e) {
			}
		}
	}
	
}
