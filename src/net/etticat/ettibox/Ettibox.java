package net.etticat.ettibox;

import net.etticat.ettibox.models.ContextProvider;
import android.app.Application;

public class Ettibox extends Application {

	@Override
	public void onCreate() {
			
		ContextProvider.setContext(getApplicationContext());
		super.onCreate();
	}
	
}
