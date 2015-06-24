package de.freenet.pocketfahrschulelite;

import org.json.JSONException;

import de.freenet.pocketfahrschulelite.adapters.LicenseClassAdapter;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class LicenseClassPicker extends PocketFahrschuleListActivity implements OnItemClickListener {
	
	private static final String TAG = "LicenseClassPicker";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ListView lv = new ListView(this);
        lv.setId(android.R.id.list);
        lv.setBackgroundResource(R.drawable.bg_mit_gitter);
        setContentView(lv);
        
        setTitle(R.string.license_class);
        
        setListAdapter(new LicenseClassAdapter(this));
        getListView().setOnItemClickListener(this);
    }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		try {
			int licenseClass = ((LicenseClassAdapter) getListAdapter()).getItem(arg2).getInt("id");
			
			if (licenseClass != 4) return;
			
			boolean firstStart = FahrschulePreferences.getInstance().isFirstStart();
			FahrschulePreferences.getInstance().setCurrentLicenseClass(licenseClass);
			
			if (firstStart) {
				Intent i = new Intent(this, Instruction.class);
				i.putExtra(Instruction.EXTRA_IS_WELCOME_SCREEN, true);
				startActivity(i);
    		}
			
			finish();
		} catch (JSONException e) {
			Log.e(TAG, "JSONException: " + e.getMessage());
		}
	}
}
