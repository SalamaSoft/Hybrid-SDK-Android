package com.salama.android.developer.user;

public class UserAuthInfo {

	private String loginId = null;
	private String returnCode = null;
	private String userId = null;
	private String authTicket = null;
	private long expiringTime = 0;
	
	public String getLoginId() {
		return loginId;
	}
	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}
	public String getReturnCode() {
		return returnCode;
	}
	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getAuthTicket() {
		return authTicket;
	}
	public void setAuthTicket(String authTicket) {
		this.authTicket = authTicket;
	}
	public long getExpiringTime() {
		return expiringTime;
	}
	public void setExpiringTime(long expiringTime) {
		this.expiringTime = expiringTime;
	}
	
}
