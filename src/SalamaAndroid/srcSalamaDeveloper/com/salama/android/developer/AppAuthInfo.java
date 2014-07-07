package com.salama.android.developer;

public class AppAuthInfo {

	private String appId = null;
	private String appToken = null;
	private long expiringTime = 0;
	
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getAppToken() {
		return appToken;
	}
	public void setAppToken(String appToken) {
		this.appToken = appToken;
	}
	public long getExpiringTime() {
		return expiringTime;
	}
	public void setExpiringTime(long expiringTime) {
		this.expiringTime = expiringTime;
	}
	
	
}
