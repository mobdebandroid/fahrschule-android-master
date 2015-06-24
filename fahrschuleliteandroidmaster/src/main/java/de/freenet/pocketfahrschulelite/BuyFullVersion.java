package de.freenet.pocketfahrschulelite;

import de.freenet.pocketfahrschulelite.classes.ApplicationStoreHelper;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class BuyFullVersion extends PocketFahrschuleActivity {
	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_full_version);
        
        TextView tv = (TextView) this.findViewById(R.id.textView2);
        tv.setText(Html.fromHtml(getString(R.string.buy_full_version_text)));
	}

	public void buyFullVersion(View v) {
		ApplicationStoreHelper.openFullVersionStorePage(this);
	}
}
