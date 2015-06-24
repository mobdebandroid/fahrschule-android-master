package de.freenet.pocketfahrschulelite;

import java.io.IOException;

import de.freenet.pocketfahrschulelite.app.FahrschuleApplication;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import de.freenet.tracking.TrackingManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.CookieSyncManager;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

public class Splash extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
        TrackingManager.getInstance().setTesting(FahrschuleApplication.DEBUG);
        
        if (FahrschuleApplication.DEBUG) {
        	findViewById(R.id.debugTextView).setVisibility(View.VISIBLE);
        }
        
        startApp();

//        checkForUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        UpdateManager.unregister();
    }

    @Override
    protected void onResume() {
    	super.onResume();
//        checkForCrashes();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }

//    //  Functions update and crash reporting for HOCKEY APP
//    //-----------------------------------------------
//    private void checkForCrashes() {
//        CrashManager.register(this, APP_ID);
//    }
//
//    private void checkForUpdates() {
//        // Remove this for store / production builds!
//        UpdateManager.register(this, APP_ID);
//    }
//    //------------------------------------------------
    private void startApp() {
    	
    	FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
        try {
			db.createDataBase();
		} catch (IOException e) {
			e.printStackTrace();
		}
        db.close();
        
        CookieSyncManager.createInstance(this);
        TrackingManager.getInstance().sendStatistics(this, FahrschulePreferences.getInstance().getTrackingUrl("A1"));
        
        new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				Intent i = null;
				if (FahrschulePreferences.getInstance().isFirstStart()) {
					i = new Intent(Splash.this, LicenseClassPicker.class);
				}
				else {
					i = new Intent(Splash.this,MainNavigation.class);
				}
				startActivity(i);
		        finish();
			}
			
        }, 3000);
    }
}