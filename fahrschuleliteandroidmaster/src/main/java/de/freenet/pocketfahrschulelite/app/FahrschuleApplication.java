package de.freenet.pocketfahrschulelite.app;

import android.app.Application;
import android.content.Context;

public class FahrschuleApplication extends Application {
	
	public static final boolean DEBUG = false;

	private static Context sContext;

    public void onCreate() {
    	super.onCreate();
    	sContext = getApplicationContext();
    }
    
    public static Context getAppContext() {
    	return sContext;
    }
}
