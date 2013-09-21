package net.etticat.ettibox.models;

import android.content.Context;

public class ContextProvider {
	protected static Context sContext;

	public static Context getContext() {
		return sContext;
	}

	public static void setContext(Context context) {
		sContext = context.getApplicationContext();
	}
	
}
