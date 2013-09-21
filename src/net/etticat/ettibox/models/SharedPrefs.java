package net.etticat.ettibox.models;


import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs extends ContextProvider{

	private static final String PREFS_NAME = "Ettibox";
	
	private static final String PREFS_KEY_USERNAME = "username";
	private static final String PREFS_KEY_ACCESS_TOKEN = "accessToken";
	private static final String PREFS_KEY_ENCRYPTED_PASSWORD = "encryptedPassword";
	private static final String PREFS_KEY_UUID = "uuid";
	
	private static SharedPreferences sSharedPreferences = null;

	private SharedPrefs(){}
	
	private static SharedPreferences getSharedPreferences(){
		if (sSharedPreferences == null){
			sSharedPreferences = sContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		}
		return sSharedPreferences;
	}
	private static void editValue(String key, String value){
	      SharedPreferences.Editor editor = getSharedPreferences().edit();
	      editor.putString(key, value);
	      editor.commit();
	}
	public static String getUsername() {
	       return getSharedPreferences().getString(PREFS_KEY_USERNAME, "");
	}

	public static void setUsername(String username){
		editValue(PREFS_KEY_USERNAME, username);
	}

	public static String getAccessToken() {
	       return getSharedPreferences().getString(PREFS_KEY_ACCESS_TOKEN, "");
	}
	
	public static void setAccessToken(String accessToken){
		editValue(PREFS_KEY_ACCESS_TOKEN, accessToken);
	}
	
	public static String getEncryptedPassword() {
	       return getSharedPreferences().getString(PREFS_KEY_ENCRYPTED_PASSWORD, "");
	}
	
	public static void setEncryptedPassword(String encryptedPassword){
		editValue(PREFS_KEY_ENCRYPTED_PASSWORD, encryptedPassword);
	}
	public static String getUuid() {
		String uuid = getSharedPreferences().getString(PREFS_KEY_UUID, "");
		if(uuid == ""){
			uuid = UUID.randomUUID().toString();
			editValue(PREFS_KEY_UUID, uuid);
		}
		return uuid;
	}
	

    public void clear() {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.clear();
        editor.commit();
    }

}
