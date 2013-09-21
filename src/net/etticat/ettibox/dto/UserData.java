package net.etticat.ettibox.dto;

import java.util.Date;

public class UserData {
	
	private int id;
	private String name;
	private String passwordKey;
	private String encryptedPassword;
	
	private String pushToken;
	private String accessToken;
	private String accessTokenValidity;
	private String iPadId;

	public String getEncryptedPassword() {
		return encryptedPassword;
	}
	public void setEncryptedPassword(String encryptedPassword) {
		this.encryptedPassword = encryptedPassword;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPasswordKey() {
		return passwordKey;
	}
	public void setPasswordKey(String passwordKey) {
		this.passwordKey = passwordKey;
	}
	public String getPushToken() {
		return pushToken;
	}
	public void setPushToken(String pushToken) {
		this.pushToken = pushToken;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getAccessTokenValidity() {
		return accessTokenValidity;
	}
	public void setAccessTokenValidity(String accessTokenValidity) {
		this.accessTokenValidity = accessTokenValidity;
	}
	public String getiPadId() {
		return iPadId;
	}
	public void setiPadId(String iPadId) {
		this.iPadId = iPadId;
	}
	
	
}
