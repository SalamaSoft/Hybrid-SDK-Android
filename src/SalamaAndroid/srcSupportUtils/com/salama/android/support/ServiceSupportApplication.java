package com.salama.android.support;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.salama.android.developer.util.http.SalamaHttpClientUtil;
import com.salama.android.util.SSLog;
import com.salama.android.util.http.HttpClientUtil;

import MetoXML.Util.ClassFinder;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.TypedValue;
import dalvik.system.DexFile;

public class ServiceSupportApplication extends Application implements ClassFinder {
	private static ServiceSupportApplication _singleton = null;

	private ExecutorService _defaultThreadPool;
	private LocalBroadcastManager _localBroadcastManager = null;
	private ConcurrentHashMap<String, Object> _sessionValueMap = new ConcurrentHashMap<String, Object>();
	private ConcurrentLinkedQueue<SessionKeyObj> _sessionKeyObjQueue = new ConcurrentLinkedQueue<ServiceSupportApplication.SessionKeyObj>();
	private volatile long _lastClearSessionTime = System.currentTimeMillis();
	
	private List<ExecutorService> _threadPoolList = new ArrayList<ExecutorService>();
	
	private static final String SESSION_KEY_PREFIX_BROADCAST_DATA = "broadcastData.";
	private long _sessionKeySeq = Long.MIN_VALUE;
	private Object _sessionKeySeqLocker = new Object();
	
	private HashMap<String, Class<?>> _classSimpleNameClassMapping = new HashMap<String, Class<?>>();
	private HashMap<String, Class<?>> _classNameClassMapping = new HashMap<String, Class<?>>();
	
	private static final int VIEW_ID_MIN = 100;
	private static final int VIEW_ID_MAX = 300;
	private int _viewIdSeed = VIEW_ID_MIN;
	private Object _viewIdSeedLocker = new Object();
	
	public static ServiceSupportApplication singleton() {
		return _singleton;
	}
		
	@Override
	public void onCreate() {
		super.onCreate();
		
		SSLog.d("ServiceSupportApplication", "onCreate()");

		_singleton = this;
		
		_defaultThreadPool = Executors.newCachedThreadPool();
		
		_localBroadcastManager = LocalBroadcastManager.getInstance(this);
		
		scanClassesOfApplication();
		
		initHttpUserAgent();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		
		_defaultThreadPool.shutdown();
		
		for(int i = 0; i < _threadPoolList.size(); i++) {
			_threadPoolList.get(i).shutdown();
		}
		_threadPoolList.clear();
		
		_singleton = null;
		SSLog.d("ServiceCommonApp", "onTerminate()");
	}

	/**
	 * 创建线程池(单线程模式)
	 * @return 线程池对象(ExecutorService)
	 */
	public ExecutorService createSingleThreadPool() {
		ExecutorService threadPool = Executors.newSingleThreadExecutor();
		
		_threadPoolList.add(threadPool);
		
		return threadPool;
	}

	/**
	 * 创建线程池(缓冲模式)
	 * @return 线程池对象(ExecutorService)
	 */
	public ExecutorService createCachedThreadPool() {
		ExecutorService threadPool = Executors.newCachedThreadPool();
		
		_threadPoolList.add(threadPool);
		
		return threadPool;
	}
	
	/**
	 * 创建线程池(固定数量模式)
	 * @param threadMaxCount 最大线程数
	 * @return 线程池对象(ExecutorService)
	 */
	public ExecutorService createFixedThreadPool(int threadMaxCount) {
		ExecutorService threadPool = Executors.newFixedThreadPool(threadMaxCount);
		
		_threadPoolList.add(threadPool);
		
		return threadPool;
	}

	/**
	 * 异步执行线程任务(使用cachedThreadPool执行)
	 * @param task 线程任务
	 */
	public void asyncExecute(Runnable task) {
		_defaultThreadPool.execute(task);
	}
	
	/**
	 * 发送本地通知
	 * @param intent
	 */
	public void sendLocalBroadcast(Intent intent) {
		_localBroadcastManager.sendBroadcast(intent);
	}
	
	/**
	 * 发送本地通知(数据通过某种方式封装传递给接收方)
	 * @param broadcastName 通知名
	 * @param data 参数对象
	 * @param extraName 参数名(接收时用)
	 */
	public void sendWrappedLocalBroadcast(String broadcastName, Object data, String extraName) {
		Intent intent = new Intent(broadcastName);
		
		if(data != null) {
			String dataSessionKey = SESSION_KEY_PREFIX_BROADCAST_DATA + getNewSessionKey(); 
			setSessionValue(dataSessionKey, data);
			intent.putExtra(extraName, dataSessionKey);

			_sessionKeyObjQueue.add(new SessionKeyObj(dataSessionKey));
		}

		_localBroadcastManager.sendBroadcast(intent);
	}
	
	/**
	 * 本地通知的接收方获取参数值
	 * @param intent 本地通知接收时获得的intent
	 * @param extraName 参数名
	 * @return
	 */
	public Object getWrappedDataFromLocalBroadcast(Intent intent, String extraName) {
		String sessionKey = intent.getStringExtra(extraName);
		
		if(sessionKey == null) {
			return null;
		} else {
			clearOldSessionKey();
			return getSessionValue(sessionKey);
		}
	}
	
	private void clearOldSessionKey() {
		long curTime = System.currentTimeMillis(); 
		long clearInterval = 300000;
		
		if((curTime - _lastClearSessionTime) >= clearInterval) {
			int i = 0;
			SessionKeyObj keyObj = null;
			
			while(i < 100) {
				keyObj = _sessionKeyObjQueue.peek();
				
				if(keyObj == null) {
					break;
				} else {
					if((curTime - keyObj.createTime) >= clearInterval) {
						//delete
						_sessionValueMap.remove(keyObj.sessionKey);
						_sessionKeyObjQueue.poll();
					} else {
						break;
					}
				}
				
				i++;
			}
		}
	}
	
	/**
	 * 取得session值.为便于各个画面之间共享数据，此处提供session容器存储变量。
	 * @param key
	 * @return 
	 */
	public Object getSessionValue(String key) {
		return _sessionValueMap.get(key);
	}

	/**
	 * 取得session值，并且删除
	 * @param key
	 * @return
	 */
	public Object getSessionValueAndRemove(String key) {
		Object returnVal = _sessionValueMap.get(key);
		_sessionValueMap.remove(key);
		return returnVal;
	}
	
	/**
	 * 设置session值
	 * @param key
	 * @param value
	 */
	public void setSessionValue(String key, Object value) {
		_sessionValueMap.put(key, value);
	}

	/**
	 * 删除session值
	 * @param key
	 */
	public void removeSessionValue(String key) {
		_sessionValueMap.remove(key);
	}
	
//	public Class<?> loadClass(String className) throws ClassNotFoundException {
//		return getClassLoader().loadClass(className);
//	}

	/**
	 * 根据类名取得类 
	 * @param className 类名。可以是简称也可以全称(包含package)
	 * @return 类
	 */
	@Override
	public Class<?> findClass(String className) throws ClassNotFoundException {
		Class<?> cls = _classSimpleNameClassMapping.get(className);
		
		if(cls != null) {
			return cls;
		}

		cls = _classNameClassMapping.get(className);
		if(cls != null) {
			return cls;
		}
		
		return getClassLoader().loadClass(className);
	}
	
	private void scanClassesOfApplication() {
		try {
			/* hack way
			Field dexField = PathClassLoader.class.getDeclaredField("mDexs");
			dexField.setAccessible(true);
			DexFile[] dexs = (DexFile[]) dexField.get(getClassLoader());
			*/
			
			String currentAppPackageName = getPackageName(); 
			
			String sourceDirPath = getPackageManager().getApplicationInfo(
					getPackageName(), 0).sourceDir;
			DexFile dexFile = new DexFile(sourceDirPath);
			
			Enumeration<String> entries = dexFile.entries();
			String className;
			Class<?> cls;
			ClassLoader classLoader = getClassLoader();
			while(entries.hasMoreElements()) {
				className = entries.nextElement();
				
				try {
					//cls = dexFile.loadClass(className, getClassLoader());
					cls = classLoader.loadClass(className);

					if(className.startsWith(currentAppPackageName)
							|| className.startsWith("com.salama.android.")
							) {
						_classSimpleNameClassMapping.put(cls.getSimpleName(), cls);
						_classNameClassMapping.put(className, cls);

						SSLog.d("ServiceSupportApplication", "scanClassesOfApplication() class:" + className);
					}
				} catch(Exception e) {
					//Log.e("ServiceSupportApplication", "scanClassesOfApplication() Error in while{}", e);
				}
			}
		} catch(Exception e) {
			Log.e("ServiceSupportApplication", "scanClassesOfApplication()", e);
		}
		
	}
		
	private long getNewSessionKey() {
		synchronized (_sessionKeySeqLocker) {
			if(_sessionKeySeq == Long.MAX_VALUE) {
				_sessionKeySeq = Long.MIN_VALUE;
			} else {
				_sessionKeySeq++;
			}
			
			return _sessionKeySeq;
		}
	}
	
	/**
	 * 取得新viewId。提供了一个顺序自增长的序号。
	 * @return
	 */
	public int newViewId() {
		synchronized (_viewIdSeedLocker) {
			if(_viewIdSeed == VIEW_ID_MAX) {
				_viewIdSeed = VIEW_ID_MIN;
			} else {
				_viewIdSeed++;
			}
			
			return _viewIdSeed;
		}
	}
	
	private class SessionKeyObj {
		public String sessionKey = null;
		public long createTime = System.currentTimeMillis();
		
		public SessionKeyObj(String key) {
			sessionKey = key;
		}
	}

	private void initHttpUserAgent() {
		PackageInfo pkgInfo;
		try {
			pkgInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
			
			String userAgent = 	"Android " + Build.VERSION.RELEASE + ";" 
					+ "API " + Build.VERSION.SDK_INT + ";"
					+ "APP " + this.getPackageName() + " " + pkgInfo.versionName + ";"
					+ "salama" + ";";
 
			HttpClientUtil.setUserAgent(userAgent);
			SalamaHttpClientUtil.setUserAgent(userAgent);
			
			SSLog.d("ServiceSupportApplication", "setUserAgent:" + userAgent);
			
		} catch (Exception e) {
			SSLog.i("ServiceSupportApplication", "", e);
		}
	}
	
	
}

