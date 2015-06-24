package de.freenet.pocketfahrschulelite;

import de.freenet.pocketfahrschulelite.classes.ApplicationStoreHelper;
import de.freenet.pocketfahrschulelite.classes.Utils;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import de.freenet.tracking.TrackingManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

public class Extras extends Activity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extras);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	TrackingManager.getInstance().sendStatistics(this, FahrschulePreferences.getInstance().getTrackingUrl("E2"));
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		Utils.showQuitApplicationDialog(this);
    	    
    	    return true;
    	}
    	
    	return super.onKeyDown(keyCode, event);
    }
    
    public void clickInstructions(View v) {
    	TrackingManager.getInstance().sendStatistics(this, FahrschulePreferences.getInstance().getTrackingUrl("E3.1"));
    	Intent i = new Intent(this, Instruction.class);
    	startActivity(i);
    }
    
    public void clickFormulary(View v) {
    	TrackingManager.getInstance().sendStatistics(this, FahrschulePreferences.getInstance().getTrackingUrl("E3.2"));
    	Intent i = new Intent(this, Formulas.class);
    	startActivity(i);
    }

    public void clickBuyFullVersion(View v) {
    	ApplicationStoreHelper.openFullVersionStorePage(this);
    }

    public void clickStVO(View v) {
    	TrackingManager.getInstance().sendStatistics(this, FahrschulePreferences.getInstance().getTrackingUrl("E3.4"));
    	Intent i = new Intent(this, StVO.class);
    	startActivity(i);
	}

    public void clickRateApp(View v) {
    	ApplicationStoreHelper.openLiteVersionStorePage(this);
    }
}
