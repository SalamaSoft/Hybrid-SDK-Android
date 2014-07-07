package com.salama.android.developer.user;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import MetoXML.XmlDeserializer;
import MetoXML.XmlSerializer;
import android.content.Context;
import android.util.Log;

import com.salama.android.developer.SalamaAppService;
import com.salama.android.developer.util.http.SalamaHttpClientUtil;
import com.salama.android.support.ServiceSupportApplication;
import com.salama.android.support.ServiceSupportUtil;
import com.salama.android.util.MD5Util;
import com.salama.android.util.SSLog;

public class SalamaUserService {
	protected final static String USER_INFO_STORED_KEY = "salamauserauth_";
	protected final static String EASY_APP_USER_AUTH_SERVICE = "com.salama.easyapp.service.UserAuthService";
	
	private UserAuthInfo _userAuthInfo = null;
	
	private static SalamaUserService _singleton = null;
	
	public static SalamaUserService singleton() {
		if(_singleton == null) {
			_singleton = new SalamaUserService();
		}
		
		return _singleton;
	}

	private SalamaUserService() {
		_userAuthInfo = getStoredUserAuthInfo();
		if(_userAuthInfo == null) {
			_userAuthInfo = new UserAuthInfo();
		}
	}
	
	/**
	 * 取得用户认证信息
	 * @return 用户认证信息
	 */
	public UserAuthInfo getUserAuthInfo() {
		return _userAuthInfo;
	}
	
	/**
	 * 返回用户是否已登录(检测是否有登录票据，以及票据是否过期)
	 * @return 1:是  0:否
	 */
	public int isUserAuthValid() {
		if(_userAuthInfo == null || _userAuthInfo.getAuthTicket() == null || _userAuthInfo.getAuthTicket().length() == 0) {
			//SSLog.d("SalamaUserService", "isUserAuthValid() 0 a");
			return 0;
		} else {
			if(_userAuthInfo.getExpiringTime() <= System.currentTimeMillis()) {
				//SSLog.d("SalamaUserService", "isUserAuthValid() 0 b expTime:" + _userAuthInfo.getExpiringTime());
				return 0;
			} else {
				return 1;
			}
		}
	}
	
	/**
	 * 保存用户认证信息(存储至手机中)
	 */
	public void storeUserAuthInfo(UserAuthInfo userAuthInfo) {
		if(userAuthInfo == null) {
			clearUserAuthInfo();
		} else {
			_userAuthInfo.setLoginId(userAuthInfo.getLoginId());
			_userAuthInfo.setReturnCode(userAuthInfo.getReturnCode());
			_userAuthInfo.setUserId(userAuthInfo.getUserId());
			_userAuthInfo.setAuthTicket(userAuthInfo.getAuthTicket());
			_userAuthInfo.setExpiringTime(userAuthInfo.getExpiringTime());
		}
		
		FileOutputStream fos = null;
		OutputStreamWriter writer = null;
		
		try {
			fos = ServiceSupportApplication.singleton().openFileOutput(
					USER_INFO_STORED_KEY.concat(SalamaAppService.singleton().getAppId()), Context.MODE_PRIVATE);
			writer = new OutputStreamWriter(fos, XmlDeserializer.DefaultCharset);
			
			XmlSerializer xmlSer = new XmlSerializer();
			xmlSer.Serialize(writer, _userAuthInfo, UserAuthInfo.class);
			
			writer.flush();
		} catch(Exception e) {
			Log.e("SalamaAppService", "Error in storeAppAuthInfoWithAppId()", e);
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
	
	/**
	 * 用户注册
	 * @param loginId 登录ID
	 * @param password 密码
	 * @return 用户认证信息(包含Ticket，登录操作的结果)。
	 * 其中returnCode是操作的结果，有以下种类:
	 * 0:成功 
	 * -8:失败。loginId格式不正确(正确的格式：长度小于等于32；内容仅允许英文字母，数字，和三种符号'.','_','-'，不能以符号开头或结尾)
	 * -9:失败。password格式不正确(正确的格式：长度小于等于32)
	 * -20:失败。loginId重复
	 * -30:失败。其他错误。
	 */
	public UserAuthInfo signUp(String loginId, String password) {
		try {
			SSLog.d("SalamaUserService", "signUp() loginId:" + loginId);

			String passwordMD5 = MD5Util.md5String(password);
			String resultXml = (String)SalamaHttpClientUtil.doBasicGet(
					false,
					SalamaAppService.singleton().getAppServiceHttpsUrl(), 
					ServiceSupportUtil.newList(new String[]{"serviceType", "serviceMethod", "appToken", "loginId", "passwordMD5"}), 
					ServiceSupportUtil.newList(new String[]{EASY_APP_USER_AUTH_SERVICE, "signUp", SalamaAppService.singleton().getAppToken(), loginId, passwordMD5}),
					null
					);
			
			UserAuthInfo authInfo = null;
			if(resultXml != null && resultXml.length() > 0) {
				authInfo = (UserAuthInfo) XmlDeserializer.stringToObject(resultXml, UserAuthInfo.class);
			}
			
			storeUserAuthInfo(authInfo);
			
			return authInfo;
		} catch(Exception e) {
			SSLog.e("SalamaUserService", "signUp()", e);
			return null;
		}
	}
	
	/**
	 * 用户登录
	 * @param loginId 登录ID
	 * @param password 密码
	 * @return 用户认证信息(包含Ticket，登录操作的结果)。
	 * 其中returnCode是操作的结果，有以下种类:
	 * 0:成功
	 * -8:失败。loginId格式不正确(正确的格式：长度小于等于32；内容仅允许英文字母，数字，和三种符号'.','_','-'，不能以符号开头或结尾)
	 * -9:失败。password格式不正确(正确的格式：长度小于等于32)
	 * -10:失败。登录id和密码验证不通过
	 * -20:失败。loginId重复
	 * -30:失败。其他错误。
	 */
	public UserAuthInfo login(String loginId, String password) {
		String utcTime = Long.toString(System.currentTimeMillis());
		String utcTimeMD5 = MD5Util.md5String(utcTime);
		String passwordMD5 = MD5Util.md5String(password);
		String passwordMD5MD5 = MD5Util.md5String(passwordMD5.concat(utcTimeMD5));
		
		try {
			String resultXml = (String)SalamaHttpClientUtil.doBasicGet(
					false,
					SalamaAppService.singleton().getAppServiceHttpsUrl(), 
					ServiceSupportUtil.newList(new String[]{"serviceType", "serviceMethod", "appToken", "loginId", "passwordMD5MD5", "utcTime"}), 
					ServiceSupportUtil.newList(new String[]{EASY_APP_USER_AUTH_SERVICE, "login", SalamaAppService.singleton().getAppToken(), loginId, passwordMD5MD5, utcTime}),
					null
					);
			
			UserAuthInfo authInfo = null;
			if(resultXml != null && resultXml.length() > 0) {
				authInfo = (UserAuthInfo) XmlDeserializer.stringToObject(resultXml, UserAuthInfo.class);
			}
			
			storeUserAuthInfo(authInfo);
			
			return authInfo;
		} catch(Exception e) {
			SSLog.e("SalamaUserService", "login()", e);
			return null;
		}
	}
	
	/**
	 * 用户通过存储在手机中的登录票据登录
	 * @return 用户认证信息(包含Ticket，登录操作的结果)。
	 * 其中returnCode是操作的结果，有以下种类:
	 * 0:成功
	 * -11:失败。登录票据过期失效
	 * -30:失败。其他错误。
	 */
	public UserAuthInfo loginByTicket() {
		try {
			String resultXml = (String)SalamaHttpClientUtil.doBasicGet(
					false,
					SalamaAppService.singleton().getAppServiceHttpsUrl(), 
					ServiceSupportUtil.newList(new String[]{"serviceType", "serviceMethod", "appToken", "authTicket"}), 
					ServiceSupportUtil.newList(new String[]{EASY_APP_USER_AUTH_SERVICE, "loginByTicket", 
							SalamaAppService.singleton().getAppToken(), 
							_userAuthInfo.getAuthTicket()==null?"":_userAuthInfo.getAuthTicket()}),
					null
					);
			
			UserAuthInfo authInfo = null;
			if(resultXml != null && resultXml.length() > 0) {
				authInfo = (UserAuthInfo) XmlDeserializer.stringToObject(resultXml, UserAuthInfo.class);
			}
			
			storeUserAuthInfo(authInfo);
			
			return authInfo;
		} catch(Exception e) {
			SSLog.e("SalamaUserService", "loginByTicket()", e);
			return null;
		}
	}
	
	/**
	 * 修改密码
	 * @param loginId 登录ID
	 * @param password 原密码
	 * @param newPassword 新密码
	 * @return 用户认证信息(包含Ticket，登录操作的结果)。
	 * 其中returnCode是操作的结果，有以下种类:
	 * 0:成功
	 * -8:失败。loginId格式不正确(正确的格式：长度小于等于32；内容仅允许英文字母，数字，和三种符号'.','_','-'，不能以符号开头或结尾)
	 * -9:失败。password格式不正确(正确的格式：长度小于等于32)
	 * -10:失败。登录id和密码验证不通过
	 * -30:失败。其他错误。
	 */
	public UserAuthInfo changePassword(String loginId, String password, String newPassword) {
		String passwordMD5 = MD5Util.md5String(password);
		String newPasswordMD5 = MD5Util.md5String(newPassword);
		
		try {
			String resultXml = (String)SalamaHttpClientUtil.doBasicGet(
					false,
					SalamaAppService.singleton().getAppServiceHttpsUrl(), 
					ServiceSupportUtil.newList(new String[]{"serviceType", "serviceMethod", "appToken", 
							"loginId", "passwordMD5", "newPasswordMD5"}), 
					ServiceSupportUtil.newList(new String[]{EASY_APP_USER_AUTH_SERVICE, "changePassword", 
							SalamaAppService.singleton().getAppToken(), loginId, 
							passwordMD5, newPasswordMD5}),
					null
					);
			
			UserAuthInfo authInfo = null;
			if(resultXml != null && resultXml.length() > 0) {
				authInfo = (UserAuthInfo) XmlDeserializer.stringToObject(resultXml, UserAuthInfo.class);
			}
			
			storeUserAuthInfo(authInfo);
			
			return authInfo;
		} catch(Exception e) {
			SSLog.e("SalamaUserService", "changePassword()", e);
			return null;
		}
	}
	
	/**
	 * 登出
	 * @return 1:成功 以外:失败
	 */
	public String logout() {
		try {
			String result = (String)SalamaHttpClientUtil.doBasicGet(
					false,
					SalamaAppService.singleton().getAppServiceHttpsUrl(), 
					ServiceSupportUtil.newList(new String[]{"serviceType", "serviceMethod", "appToken", 
							"authTicket"}), 
					ServiceSupportUtil.newList(new String[]{EASY_APP_USER_AUTH_SERVICE, "changePassword", 
							SalamaAppService.singleton().getAppToken(), 
							_userAuthInfo.getAuthTicket()==null?"":_userAuthInfo.getAuthTicket()}),
					null		
					);

			_userAuthInfo.setAuthTicket("");
			_userAuthInfo.setExpiringTime(0);
			
			storeUserAuthInfo(_userAuthInfo);
			
			return result;
		} catch(Exception e) {
			SSLog.e("SalamaUserService", "logout()", e);
			return null;
		}
	}
	
	private void clearUserAuthInfo() {
		_userAuthInfo.setReturnCode("");
		_userAuthInfo.setUserId("");
		_userAuthInfo.setAuthTicket("");
		_userAuthInfo.setExpiringTime(0);
	}
	
	private UserAuthInfo getStoredUserAuthInfo() {
		FileInputStream fis = null;
		InputStreamReader reader = null;
		
		try {
			fis = ServiceSupportApplication.singleton().openFileInput(
					USER_INFO_STORED_KEY.concat(SalamaAppService.singleton().getAppId()));
			reader = new InputStreamReader(fis, XmlDeserializer.DefaultCharset);
			
			XmlDeserializer xmlDes = new XmlDeserializer();
			return (UserAuthInfo) xmlDes.Deserialize(
					reader, UserAuthInfo.class, ServiceSupportApplication.singleton());
		} catch(FileNotFoundException e) {
			SSLog.d("SalamaUserService", "getStoredUserAuthInfo() file does not exist.");
			return null;
		} catch(Exception e) {
			Log.e("SalamaUserService", "Error in getStoredUserAuthInfo()", e);
			return null;
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
}
