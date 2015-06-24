package de.freenet.pocketfahrschulelite;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.RelativeLayout;

public class Impressum extends PocketFahrschuleActivity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        RelativeLayout layout = new RelativeLayout(this);
        layout.setBackgroundResource(R.drawable.bg_mit_gitter);
        
        setContentView(layout);        
        setTitle(R.string.impressum);

        WebView webView = new WebView(this);
        webView.loadUrl("file:///android_asset/impressum.html");
        webView.setBackgroundColor(0x00000000);
        
        layout.addView(webView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));
    }
}
