package de.freenet.pocketfahrschulelite;

import de.freenet.pocketfahrschulelite.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class PocketFahrschuleActivity extends Activity {
	
	private int mTitleBarResId = R.layout.titlebar;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    }
    
    public void setAlternativeTitleBar(int resId) {
    	mTitleBarResId = resId;
    }
    
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, mTitleBarResId);
    }
    
    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, mTitleBarResId);
    }
    
    @Override
    public void setTitle(CharSequence title) {
    	TextView tv = (TextView) findViewById(R.id.titleTextView);
    	tv.setText(title);
    }
    
    @Override
    public void setTitle(int title) {
    	TextView tv = (TextView) findViewById(R.id.titleTextView);
    	tv.setText(title);
    }
}
