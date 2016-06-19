package com.salama.android.webcore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import MetoXML.XmlDeserializer;
import MetoXML.XmlSerializer;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.webkit.WebView;

import com.salama.android.support.ServiceSupportApplication;
import com.salama.android.util.ResourceFileManager;
import com.salama.android.util.SSLog;

public class WebController {
	public static final String WEB_RESOURCE_FILE_DIR_NAME = "res";
	public static final String LOCAL_WEB_URL_PREFIX = "file://";
	public static final String LOAD_URL_JAVASCRIPT = "javascript:";
	
	private final static String INIT_CHECK_FILE_NAME = ".init_file_CHECK_just_for_check";
	
	private final static String NOTIFICATION_FOR_JAVASCRIPT_USER_INFO_RESULT_NAME = "result";
	
	private enum NativeServiceCmdPositionInBlock {
		NativeServiceCmdPositionInBlockFirstCmd, 
		NativeServiceCmdPositionInBlockNotFirstAndLastCmd, 
		NativeServiceCmdPositionInBlockLastCmd
	};
	
	private String _webPackageName = null;
	
	private String _webBaseDirPath = null; 
	private String _webRootDirPath = null;
	private String _tempPath = null; 
	//private String _currentDirPath = null;
	
	private ResourceFileManager _resourceFileManager = null;
	private ConcurrentHashMap<String, String> _sessionContainer = new ConcurrentHashMap<String, String>();
	
	private NativeService _nativeService;

	private static boolean debugMode = true;
	
	private ExecutorService _queueForWeb;
	
	/**
	 * 取得本地页面压缩包文件名
	 * @return
	 */
	public String getWebPackageName() {
		return _webPackageName;
	}

	/**
	 * 取得本地页面基本目录路径
	 * @return
	 */
	public String getWebBaseDirPath() {
		return _webBaseDirPath;
	}

	/**
	 * 取得本地页面根目录路径
	 * @return
	 */
	public String getWebRootDirPath() {
		return _webRootDirPath;
	}

	/**
	 * 取得临时目录路径
	 * @return
	 */
	public String getTempPath() {
		return _tempPath;
	}
	
	public String toRealPath(String virtualPath) {
		if(virtualPath == null) {
			return null;
		}
		
		File file = new File(_webRootDirPath, virtualPath);
		return file.getAbsolutePath();
	}

	/**
	 * 取得ResourceFileManager
	 * @return ResourceFileManager
	 */
	public ResourceFileManager getResourceFileManager() {
		return _resourceFileManager;
	}

	/**
	 * 设置ResourceFileManager
	 * @return
	 */
	public void setResourceFileManager(ResourceFileManager resourceFileManager) {
		_resourceFileManager = resourceFileManager;
	}

	/**
	 * 取得nativeService
	 * @return NativeService
	 */
	public NativeService getNativeService() {
		return _nativeService;
	}

	/**
	 * 设置nativeService
	 * @return
	 */
	public void setNativeService(NativeService nativeService) {
		_nativeService = nativeService;
	}
	
	/**
	 * 设置debug模式
	 * @param isDebug debug模式
	 */
	public static void setDebugMode(boolean isDebug) {
		debugMode = isDebug;
	}
	
	/**
	 * 构造函数
	 * @param webPackageName 本地页面压缩包文件名
	 * @param htmlPackageStream 压缩包InputStream
	 * @throws IOException
	 */
	public WebController(String webPackageName, InputStream htmlPackageStream) throws IOException {
		this(webPackageName, htmlPackageStream, new File(ServiceSupportApplication.singleton().getFilesDir().getAbsolutePath()));
	}
	
	/**
	 * 构造函数
	 * @param webPackageName 本地页面压缩包文件名
	 * @param htmlPackageStream 压缩包InputStream
	 * @param webBaseDir 本地页面基本目录路径
	 * @throws IOException
	 */
	public WebController(String webPackageName, InputStream htmlPackageStream, File webBaseDir) throws IOException {
		//default base dir:files
		_webBaseDirPath = webBaseDir.getAbsolutePath();
		_webPackageName = webPackageName;
		
		initObjs(htmlPackageStream);
	}
	
	/**
	 * 初始化(本地网页目录已存在，无需解压zip)
	 * @param existingWebRootDir 本地网页根路径
	 * @return WebController
	 */
	public WebController(File existingWebRootDir) {
		
		switchToWebRootDirPath(existingWebRootDir.getAbsolutePath());
		
		initServiceObjs();
	}
	
	/**
	 * 装载本地页面
	 * @param relativeUrl 本地页面URL
	 * @param webView LocalWebView
	 */
	public void loadLocalPage(String relativeUrl, LocalWebView webView) {
		if(relativeUrl == null) {
			return;
		}
		
		File absolutePath = new File(_webRootDirPath, relativeUrl);
		String url = LOCAL_WEB_URL_PREFIX + absolutePath;
		webView.loadUrl(url);
	}
	
	/**
	 * 设置session值
	 * @param name 名称
	 * @param value 值
	 */
	public void setSessionValueWithName(String name, String value) {
		_sessionContainer.put(name, value);
	}
	
	/**
	 * 删除session值
	 * @param name 名称
	 */
	public void removeSessionValueWithName(String name) {
		_sessionContainer.remove(name);
	}
	
	/**
	 * 取得session值
	 * @param name 名称
	 * @return session值
	 */
	public String getSessionValueWithName(String name) {
		return _sessionContainer.get(name);
	}
	
	public boolean handleUrlLoadingEvent(String url, final WebView webView, final Object thisView) {
		final Object msg = NativeService.parseNativeServiceCmd(url);
		if(msg != null) {
			invokeNativeService(msg, webView, thisView);
		} else {
			webView.loadUrl(url);
		}
		
		return true;
	}
	
	/**
	 * 调用本地Service
	 * @param msg 指令
	 * @param webView LocalWebView实例
	 * @param thisView 当前View实例
	 */
	public void invokeNativeService(final Object msg, final WebView webView, final Object thisView) {
		_queueForWeb.execute(new Runnable() {
			@Override
			public void run() {
				
				NativeServiceCmdPositionInBlock cmdPosition;
				
				if(NativeService.isInstanceAssignableToClass(msg, List.class)) {
					@SuppressWarnings("unchecked")
					List<InvokeMsg> msgList = (List<InvokeMsg>)msg;
					
					for(int i = 0; i < msgList.size(); i++) {
						if(i == (msgList.size() - 1)) {
							cmdPosition = NativeServiceCmdPositionInBlock.NativeServiceCmdPositionInBlockLastCmd;
						} else if(i == 0) {
							cmdPosition = NativeServiceCmdPositionInBlock.NativeServiceCmdPositionInBlockFirstCmd;
						} else {
							cmdPosition = NativeServiceCmdPositionInBlock.NativeServiceCmdPositionInBlockNotFirstAndLastCmd;
						}

						/* Android4.0开始，默认的StrictMode不允许在UIThread里进行网络操作，因而为了接口方法代码的简洁，所有接口调用都在后台Thread里进行。
						 * 所以，接口方法里如果有UI操作的话，必须放入UIThread。
						if(msgList.get(i).getNotification() == null || msgList.get(i).getNotification().length() == 0) {
							invokeNativeServiceSingleCmd(msgList.get(i), 
									webView, thisView, cmdPosition);
						} else
						{
							final InvokeMsg msgTmp = msgList.get(i);
							final NativeServiceCmdPositionInBlock cmdPositionTmp = cmdPosition;
							_queueForWeb.execute(new Runnable() {
								@Override
								public void run() {
									invokeNativeServiceSingleCmd(msgTmp, 
											webView, thisView, cmdPositionTmp);
								}
							});
						}
						*/
						final InvokeMsg msgTmp = msgList.get(i);
						final NativeServiceCmdPositionInBlock cmdPositionTmp = cmdPosition;
						invokeNativeServiceSingleCmd(msgTmp, 
								webView, thisView, cmdPositionTmp);
					}
				} else {
					/* Android4.0开始，默认的StrictMode不允许在UIThread里进行网络操作，因而为了接口方法代码的简洁，所有接口调用都在后台Thread里进行。
					 * 所以，接口方法里如果有UI操作的话，必须放入UIThread。
					if(((InvokeMsg)msg).getNotification() == null || ((InvokeMsg)msg).getNotification().length() == 0) {
						invokeNativeServiceSingleCmd((InvokeMsg)msg, webView, thisView, 
								NativeServiceCmdPositionInBlock.NativeServiceCmdPositionInBlockLastCmd);
					} else
					{
						final InvokeMsg msgTmp = (InvokeMsg)msg;
						_queueForWeb.execute(new Runnable() {
							@Override
							public void run() {
								invokeNativeServiceSingleCmd(msgTmp, webView, thisView, 
										NativeServiceCmdPositionInBlock.NativeServiceCmdPositionInBlockLastCmd);
							}
						});
					}
					*/
					final InvokeMsg msgTmp = (InvokeMsg)msg;
					invokeNativeServiceSingleCmd(msgTmp, webView, thisView, 
							NativeServiceCmdPositionInBlock.NativeServiceCmdPositionInBlockLastCmd);
				}
				
			}
		});
	}
		
	private void invokeNativeServiceSingleCmd(InvokeMsg invokeMsg, final WebView webView, 
			Object thisView, NativeServiceCmdPositionInBlock cmdPositionInBlock) {
		try {
			SSLog.d("WebController", "invokeNativeServiceSingleCmd() target:" + invokeMsg.getTarget()
					+ " method:" + invokeMsg.getMethod());
			
	        //invoke the service method
			Object returnVal = _nativeService.invoke(
					invokeMsg.getTarget(), invokeMsg.getMethod(), 
					invokeMsg.getParams(), thisView);
			
	        //handle the variableStack
			
			if(NativeService.isInstanceAssignableToClass(thisView, WebVariableStack.class)) {
				if(invokeMsg.getReturnValueKeeper() != null 
						&& invokeMsg.getReturnValueKeeper().length() > 0) {
					int scope;
					
					if(invokeMsg.getKeeperScope() == null 
							|| invokeMsg.getKeeperScope().length() == 0) {
						scope = WebVariableStack.WebVariableStackScopeTemp;
					} else {
						if(invokeMsg.getKeeperScope().equalsIgnoreCase("page")) {
							scope = WebVariableStack.WebVariableStackScopePage;
						} else if(invokeMsg.getKeeperScope().equalsIgnoreCase("temp")) {
							scope = WebVariableStack.WebVariableStackScopeTemp;
						} else {
							scope = WebVariableStack.WebVariableStackScopeTemp;
						}
					}
					
					if(returnVal == null) {
						((WebVariableStack)thisView).removeVariable(invokeMsg.getReturnValueKeeper(), scope);
					} else {
						((WebVariableStack)thisView).setVariable(returnVal, invokeMsg.getReturnValueKeeper(), scope);
						SSLog.d("WebController", "WebVariableStack setVariable() valueKeeper:" + invokeMsg.getReturnValueKeeper());
					}
				}
				
				if(cmdPositionInBlock == NativeServiceCmdPositionInBlock.NativeServiceCmdPositionInBlockLastCmd) {
	                //clear temp scope
					((WebVariableStack)thisView).clearVariablesOfScope(WebVariableStack.WebVariableStackScopeTemp);
				}
			}
			
	        //handle callback
			final String script = scriptCallBackWhenSucceed(invokeMsg.getCallBackWhenSucceed(), returnVal);
			if(invokeMsg.getNotification() == null || invokeMsg.getNotification().length() == 0) {
				if(script != null && script.length() > 0) {
					SSLog.d("nativeService() script:", script);
					getActivityFromThisView(thisView).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							webView.loadUrl(LOAD_URL_JAVASCRIPT + script);
						}
					});
				}
			} else {
	            //post notification
				String result = returnValueToResultString(returnVal);
				ServiceSupportApplication.singleton().sendWrappedLocalBroadcast(
						invokeMsg.getNotification(), result, 
						NOTIFICATION_FOR_JAVASCRIPT_USER_INFO_RESULT_NAME);
			}
			
			returnVal = null;
		} catch(Exception e) {
			try {
				Log.e("WebController", "invokeNativeServiceSingleCmd()", e);
				if(invokeMsg.getNotification() == null || invokeMsg.getNotification().length() == 0) {
					final String script = scriptCallBackWhenError(invokeMsg.getCallBackWhenError(), e);
					if(script != null) {
						getActivityFromThisView(thisView).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								webView.loadUrl(LOAD_URL_JAVASCRIPT + script);
							}
						});
					}
				} else {
		            //post notification
					String result = returnValueToResultString(errorMsgOfException(e));
					ServiceSupportApplication.singleton().sendWrappedLocalBroadcast(
							invokeMsg.getNotification(), result, 
							NOTIFICATION_FOR_JAVASCRIPT_USER_INFO_RESULT_NAME);
				}
			} catch(Exception e1) {
				Log.e("WebController", "invokeNativeServiceSingleCmd()", e1);
			}
		}
		
	}
	

	private Activity getActivityFromThisView(Object thisViewObj) {
		if(Activity.class.isAssignableFrom(thisViewObj.getClass())) {
			return (Activity)thisViewObj;
		} else if(Fragment.class.isAssignableFrom(thisViewObj.getClass())) {
			return ((Fragment)thisViewObj).getActivity();
		} else {
			try {
				return (Activity)thisViewObj.getClass()
						.getMethod("getActivity", (Class[])null).invoke(thisViewObj, (Object[])null);
			} catch (Exception e) {
				Log.e("WebController", "getActivityFromThisView()", e);
				return null;
			}
		}
	}
	
	private String returnValueToResultString(Object returnVal) 
			throws IOException, InvocationTargetException, IllegalAccessException {
		if(returnVal == null) {
			return "";
		} else {
			//SSLog.d("WebController", "returnValueToResultString() returnVal.getClass():" + returnVal.getClass().getName());
			if(isPrimitiveType(returnVal.getClass())) {
				//SSLog.d("WebController", "returnValueToResultString() isPrimitive");
				return String.valueOf(returnVal);
			} else if(returnVal.getClass().isAssignableFrom(String.class)) {
				//SSLog.d("WebController", "returnValueToResultString() isString");
				return (String) returnVal;
			} else {
				return XmlSerializer.objectToString(
						returnVal, returnVal.getClass(), false, false);
			}
		}
	}
	
	private boolean isPrimitiveType(Class cls) {
		if(cls.isPrimitive()) {
			return true;
		} else if(cls == Byte.class || cls == Short.class || cls == Integer.class || cls == Long.class
				|| cls == Float.class || cls == Double.class) {
			return true;
		} else {
			return false;
		}
	}
	
	private String scriptCallBackWhenSucceed(String callBackFuncName, Object returnVal) 
			throws IOException, InvocationTargetException, IllegalAccessException {
		if(callBackFuncName != null && callBackFuncName.length() > 0) {
			String returnValStr = returnValueToResultString(returnVal);
			
			String callBackFuncScript = callBackFuncName + "('" + encodeToScriptStringValue(returnValStr) + "')";
			
			return callBackFuncScript;
		} else {
			return null;
		}
	}
	
	private String errorMsgOfException(Throwable e) {
		return e.getClass().getName() + " " + e.getMessage();
	}
	
	private String scriptCallBackWhenError(String callBackFuncName, Throwable e) {
		if(callBackFuncName == null || callBackFuncName.length() == 0) {
			return null;
		} else {
			String errorMsg = errorMsgOfException(e);
			String callBackFuncScript = callBackFuncName + "('" + encodeToScriptStringValue(errorMsg) + "')";
			
			return callBackFuncScript;
		}
	}
		
	private void initObjs(InputStream htmlPackageStream) throws IOException {
		initWebDirPaths(htmlPackageStream);
		
		initServiceObjs();
	}
	
	private void initWebDirPaths(InputStream htmlPackageStream) throws IOException {
		//
		File webRootDir = new File(_webBaseDirPath, _webPackageName);
		_webRootDirPath = webRootDir.getAbsolutePath();
		
		SSLog.d("WebController", "_webRootDirPath:" + _webRootDirPath);

		resetPathsAfterWebRootDirPathChanged();

		//unzip the html.zip
		extractWebSource(htmlPackageStream);
	}
	
	private void initServiceObjs() {
		_queueForWeb = ServiceSupportApplication.singleton().createCachedThreadPool();
		_nativeService = new NativeService();
	}
	
	/**
	 * 改变本地网页根路径
	 */
	public void switchToWebRootDirPath(String webRootDirPath) {
		if(webRootDirPath.endsWith("/")) {
			_webRootDirPath = webRootDirPath.substring(0, webRootDirPath.length() - 1);
		} else {
			_webRootDirPath = webRootDirPath;
		}
		
		int index = _webRootDirPath.lastIndexOf('/');
		_webBaseDirPath = _webRootDirPath.substring(0, index);
		_webPackageName = _webRootDirPath.substring(index + 1);
		
		resetPathsAfterWebRootDirPathChanged();
	}
	
	private void resetPathsAfterWebRootDirPathChanged() {
		File cacheDir = ServiceSupportApplication.singleton().getCacheDir();
		File tempDir = new File(cacheDir, "tmp");
		_tempPath = tempDir.getAbsolutePath();
		if(!tempDir.exists()) {
			tempDir.mkdirs();
		}

		File resStorageDir = new File(_webRootDirPath, WEB_RESOURCE_FILE_DIR_NAME);
		_resourceFileManager = new ResourceFileManager(resStorageDir);
	}
	
	/**
	 * 
	 * @return the rootDirName in the zip file
	 */
	private void extractWebSource(InputStream htmlPackageStream) throws IOException {
		//source file is supposed in res/raw
		ZipInputStream zipInputS = null;
		File webBaseDir = new File(_webBaseDirPath);

		try {
			boolean isNeedUnzip = false;
			
			if(debugMode) {
				isNeedUnzip = true;
				SSLog.d("WebController", "In debug mode, then it extracts html.zip every time.");
			} else {
				File webRootDir = new File(_webRootDirPath);
				if(!webRootDir.exists()) {
					isNeedUnzip = true;
				} else {
		            //html root dir exists, then check the init_time_check_file
					isNeedUnzip = isNeedExtractZipByCheckingInitFile();
				}
			}
			
			if(!isNeedUnzip) {
				return;
			}
						
			zipInputS = new ZipInputStream(htmlPackageStream);
			ZipEntry entry;
			File file = null;
			String entryName = null;
			byte[] tempBuf = new byte[1024];

			FileOutputStream fos = null;
			int readCnt;

			int entryCount = 0;
			
			while(true) {
				entry = zipInputS.getNextEntry();
				if(entry == null) {
					break;
				}
				
				entryCount++;

				entryName = entry.getName();
				
				if(entry.isDirectory()) {
					//create dir
					file = new File(webBaseDir, entryName);
					file.mkdir();
				} else {
					file = new File(webBaseDir, entryName);
					
					//save the file
					fos = new FileOutputStream(file);
					try {
						while(true) {
							readCnt = zipInputS.read(tempBuf, 0, tempBuf.length);
							
							if(readCnt <= 0) {
								break;
							}
							
							fos.write(tempBuf, 0, readCnt);
							fos.flush();
						}
					} finally {
						try {
							fos.close();
						} catch(Exception e) {
						}
					}
					
				}
				
				zipInputS.closeEntry();
			}
			
			SSLog.d("WebController", "Extracted " + entryCount + " zip entries");
		} finally {
			try {
				zipInputS.close();
			} catch(Exception e) {
			}
			try {
				htmlPackageStream.close();
			} catch(Exception e) {
			}
		}
	}
	
	private boolean isNeedExtractZipByCheckingInitFile() {
		File initCheckFile = new File(_webRootDirPath, INIT_CHECK_FILE_NAME);
		String curVersion = getCurrentPackageVersion();

		if(initCheckFile.exists()) {
			//check time
			try {
				String lastVersion = readAllText(initCheckFile);

				SSLog.d("WebController", "isNeedExtractZipByCheckingInitFile() lastVersion:" + lastVersion + " curVersion:" + curVersion);
				
				if(curVersion != null && curVersion.equals(lastVersion)) {
					return false;
				} else {
					return true;
				}
								
			} catch (IOException e) {
				SSLog.d("WebController", "isNeedExtractZipByCheckingInitTimeCompareToZipFileTime()", e);
				return true;
			}
		} else {
			try {
				writeTextToFile(initCheckFile, curVersion);
			} catch (IOException e) {
				SSLog.d("WebController", "isNeedExtractZipByCheckingInitTimeCompareToZipFileTime()", e);
			}
			return true;
		}
	}
	
	private String getCurrentPackageVersion() {
		try {
			PackageInfo pkgInfo = ServiceSupportApplication.singleton().getPackageManager().getPackageInfo(
					ServiceSupportApplication.singleton().getPackageName(), 0);
			
			return pkgInfo.versionName;
		} catch (NameNotFoundException e) {
			SSLog.d("WebController", "getCurrentPackageVersion()", e);
			
			return null;
		}
	}
	
	private String readAllText(File file) throws IOException {
		InputStreamReader reader = null;
		FileInputStream fis = null;
		StringBuilder sb = new StringBuilder();
		char[] chBuff = new char[32];
		int readCnt = 0;
		
		try {
			fis = new FileInputStream(file);
			reader = new InputStreamReader(fis, XmlDeserializer.DefaultCharset);
			
			while(true) {
				readCnt = reader.read(chBuff, 0, chBuff.length);
				if(readCnt < 0) {
					break;
				}
				
				if(readCnt > 0) {
					sb.append(chBuff, 0, readCnt);
				}
			}
			
			return sb.toString();
		} finally {
			try {
				fis.close();
			} catch(Exception e) {
			}
			try {
				reader.close();
			} catch(Exception e) {
			}
		}
	}
	
	private void writeTextToFile(File file, String text) throws IOException {
		FileOutputStream fos = null;
		OutputStreamWriter writer = null;
		
		try {
			fos = new FileOutputStream(file);
			writer = new OutputStreamWriter(fos, XmlDeserializer.DefaultCharset);
			
			writer.write(text);
			writer.flush();
			
		} finally {
			try {
				fos.close();
			} catch(Exception e) {
			}
			try {
				writer.close();
			} catch(Exception e) {
			}
		}
	}
	
	public static String encodeToScriptStringValue(String input) {
		if(input == null) {
			return null;
		}
		
		StringBuilder output = new StringBuilder();
		int len = input.length();
		char c;
		
		for(int i = 0; i < len; i++) {
			c = input.charAt(i);
			
			if(c == '"') {
				output.append("\\\"");
			} else if (c == '\r') {
				output.append("\\r");
			} else if (c == '\n') {
				output.append("\\n");
			} else if (c == '\'') {
				output.append("\\'");
			} else {
				output.append(c);
			}
		}
		
		return output.toString();
	}
}
