package net.etticat.dokabox.models;


import java.util.Date;
import java.util.UUID;

import android.R.string;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs {

	private static final String PREFS_NAME = "Dokabox";
	private Context context;

    public SharedPrefs(Context context) {
		this.context = context;
	}
    
	public String getUsername() {
	       SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
	       return settings.getString("username", "");
	}

	public void setUsername(String username){

	      SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
	      SharedPreferences.Editor editor = settings.edit();
	      editor.putString("username", username);
	      editor.commit();
	}

	public String getAccessToken() {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		return settings.getString("accessToken", null);
	}
	
	public void setAccessToken(String username){
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("accessToken", username);
		editor.commit();
	}
	
	public String getEncryptedPassword() {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		return settings.getString("encryptedPassword", null);
	}
	
	public void setEncryptedPassword(String encryptedPassword){
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("encryptedPassword", encryptedPassword);
		editor.commit();
	}
	public String getUuid() {
		SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
		String uuid = settings.getString("uuid", "");
		if(uuid == "")
			uuid = UUID.randomUUID().toString();
		
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("uuid", uuid);
		editor.commit();
		return uuid;
	}
	

    public void clear() {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }

}
