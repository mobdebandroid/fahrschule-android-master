package de.freenet.advertisement;

import java.lang.reflect.Method;

import de.freenet.library.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class SplashAd extends Activity {
	
	public static String TAG = "SplashAd";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.spashad);
        
        Advertisement ad = null;
        
        try {
        	ad = (Advertisement) this.getIntent().getExtras().get(TAG);
        } catch (NullPointerException e) {
        	Log.v(TAG, "No advertisement included. Splash will not show.");
        }
        
        if (ad != null) {
	        final ProgressBar pb = (ProgressBar) findViewById(R.id.loadingProgressBar);
	        
	        WebView wv = (WebView) findViewById(R.id.webView);
	        wv.getSettings().setJavaScriptEnabled(false);
	        wv.setWebViewClient(new WebViewClient() {
				
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					
	    	    	view.loadUrl(url);
	    	    	return true;
	    	    }
			});
	        
	        wv.setWebChromeClient(new WebChromeClient() {
	    	    
	    	    @Override
	    	    public void onProgressChanged (WebView view, int newProgress) {
	    	    	super.onProgressChanged(view, newProgress);
	    	    	
	    	    	switch(newProgress) {
	    	    		case 100:
	    	    			pb.setVisibility(View.GONE);
	    	    			break;
	    	    		default:
	    	    			pb.setVisibility(View.VISIBLE);
	    	    			break;
	    	    	}
	    	    	pb.setProgress(newProgress);
	    	    }
	    	    
	    	});
	        
	        if (!ad.webContent.equals("")) {
	        	wv.loadData(ad.webContent, "text/html", "utf-8");
	        }
	        else {
	        	wv.loadUrl(ad.url.toString());
	        }
        }
        else {
        	finish();
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	
    	if(keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
    	}
    	
    	return super.onKeyDown(keyCode, event);
    }
    
    public void closeSplashAd(View v) {
    	setResult(RESULT_OK);
    	finish();
		try {
		    Method method = Activity.class.getMethod("overridePendingTransition", new Class[]{int.class, int.class});
		    method.invoke(this, 0, R.anim.slide_top_to_bottom);
		} catch (Exception e) {
		    // Can't change animation, so do nothing
		}
    }
}
