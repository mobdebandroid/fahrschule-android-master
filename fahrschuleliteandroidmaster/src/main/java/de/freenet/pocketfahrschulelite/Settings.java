package de.freenet.pocketfahrschulelite;

import de.freenet.pocketfahrschulelite.classes.Utils;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences.LicenseClass;
import de.freenet.tracking.TrackingManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceClickListener {
	
	private static final String LICENSE_CLASS_PREFERENCE_KEY = "licenseClass";
	
	private String mCurrentTeachingTypePreferenceKey = "teachingType";
	private ListPreference mTeachingTypePreference;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        addPreferencesFromResource(R.xml.preferences);
        
        mTeachingTypePreference = (ListPreference) findPreference(mCurrentTeachingTypePreferenceKey);
        mCurrentTeachingTypePreferenceKey = String.format("teachingType_%s", FahrschulePreferences.getInstance().getCurrentLicenseClass().toString());
        setupTeachingTypePreference();
        
        FahrschulePreferences.getInstance().registerOnSharedPreferenceChangeListener(this);
        
        Preference clearStatistic = findPreference("customPref");
        clearStatistic.setOnPreferenceClickListener(this);
        
        Preference licenseClassPref = findPreference(LICENSE_CLASS_PREFERENCE_KEY);
        licenseClassPref.setSummary(getLicenseClassSummary());
        licenseClassPref.setOnPreferenceClickListener(this);
        licenseClassPref.setWidgetLayoutResource(mTeachingTypePreference.getWidgetLayoutResource());
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	TrackingManager.getInstance().sendStatistics(this, FahrschulePreferences.getInstance().getTrackingUrl("D2"));
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	FahrschulePreferences.getInstance().unregisterOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		Utils.showQuitApplicationDialog(this);
    	    
    	    return true;
    	}
    	
    	return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
        // Handle item selection
        switch (item.getItemId()) {
	        case R.id.menu_impressum:
	        	Intent i = new Intent(this, Impressum.class);
	        	startActivity(i);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
    private String getLicenseClassSummary() {
    	
    	String[] licenseClassArray = getResources().getStringArray(R.array.licenseClassArray);
    	String licenseClass = "";
    	switch (FahrschulePreferences.getInstance().getCurrentLicenseClass()) {
			case A:
				licenseClass = licenseClassArray[0];
				break;
			case A1:
				licenseClass = licenseClassArray[1];
				break;
			case B:
				licenseClass = licenseClassArray[2];
				break;
			case C:
				licenseClass = licenseClassArray[3];
				break;
			case C1:
				licenseClass = licenseClassArray[4];
				break;
			case CE:
				licenseClass = licenseClassArray[5];
				break;
			case D:
				licenseClass = licenseClassArray[6];
				break;
			case D1:
				licenseClass = licenseClassArray[7];
				break;
			case S:
				licenseClass = licenseClassArray[8];
				break;
			case T:
				licenseClass = licenseClassArray[9];
				break;
			case L:
				licenseClass = licenseClassArray[10];
				break;
			case M:
				licenseClass = licenseClassArray[11];
				break;
			case MOFA:
				licenseClass = licenseClassArray[12];
				break;
		}
    	
    	return String.format("%s (%s)", licenseClass, FahrschulePreferences.getInstance().getCurrentLicenseClass().toString());
    }
    
    private String getTeachingTypeSummary() {
    	
    	String[] teachingTypeArray = getResources().getStringArray(R.array.teachingTypeArray);
    	switch (FahrschulePreferences.getInstance().getCurrentTeachingType()) {
    	
	    	case FIRST_TIME_LICENSE:
	    		return teachingTypeArray[0];
	    	case ADDITIONAL_LICENSE:
	    		return teachingTypeArray[1];
    	}
    	
    	return teachingTypeArray[0];
    }
    
    private void setupTeachingTypePreference() {
    	mTeachingTypePreference.setKey(mCurrentTeachingTypePreferenceKey);
		mTeachingTypePreference.setSummary(getTeachingTypeSummary());
		mTeachingTypePreference.setValue(FahrschulePreferences.getInstance().getCurrentTeachingTypeString());
		
		if (FahrschulePreferences.getInstance().getCurrentLicenseClass() == LicenseClass.MOFA) {
			mTeachingTypePreference.setEntries(R.array.teachingTypeArray_first_time);
			mTeachingTypePreference.setEntryValues(R.array.teachingTypeValues_first_time);
		}
		else if (FahrschulePreferences.getInstance().getCurrentLicenseClass() == LicenseClass.C ||
				 FahrschulePreferences.getInstance().getCurrentLicenseClass() == LicenseClass.C1 ||
				 FahrschulePreferences.getInstance().getCurrentLicenseClass() == LicenseClass.CE ||
				 FahrschulePreferences.getInstance().getCurrentLicenseClass() == LicenseClass.D ||
				 FahrschulePreferences.getInstance().getCurrentLicenseClass() == LicenseClass.D1 ||
				 FahrschulePreferences.getInstance().getCurrentLicenseClass() == LicenseClass.D) {
			mTeachingTypePreference.setEntries(R.array.teachingTypeArray_additional);
			mTeachingTypePreference.setEntryValues(R.array.teachingTypeValues_additional);
		}
		else {
			mTeachingTypePreference.setEntries(R.array.teachingTypeArray);
			mTeachingTypePreference.setEntryValues(R.array.teachingTypeValues);
		}
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		
		mCurrentTeachingTypePreferenceKey = String.format("teachingType_%s", FahrschulePreferences.getInstance().getCurrentLicenseClass().toString());
		
		if (arg1.equals(LICENSE_CLASS_PREFERENCE_KEY)) {
			Preference licenseClassPref = findPreference(LICENSE_CLASS_PREFERENCE_KEY);
			licenseClassPref.setSummary(getLicenseClassSummary());
			
			setupTeachingTypePreference();
		}
		else if (arg1.equals(mCurrentTeachingTypePreferenceKey)) {
			mTeachingTypePreference.setSummary(getTeachingTypeSummary());
			mTeachingTypePreference.setValue(FahrschulePreferences.getInstance().getCurrentTeachingTypeString());
		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		
		if (preference.getKey().equals("customPref")) {
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	                switch (which){
	                case DialogInterface.BUTTON_POSITIVE:
	                    FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(Settings.this);
	                    db.clearLearnStatistics();
	                    db.close();
	                    FahrschulePreferences.getInstance().forceLicenseClassUpdate();
	                    Toast.makeText(Settings.this, getString(R.string.statistics_deleted, getLicenseClassSummary()), Toast.LENGTH_LONG).show();
	                    break;
	                case DialogInterface.BUTTON_NEGATIVE:
	                    // Do nothing
	                    break;
	                }
	            }
	        };

	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setMessage(R.string.delete_statistic_dialogbox).setPositiveButton(R.string.yes, dialogClickListener)
	            .setNegativeButton(R.string.no, dialogClickListener).setTitle(R.string.delete_statistic).show();
		}
		else {
			Intent i = new Intent(this, LicenseClassPicker.class);
			startActivity(i);
		}
        
		return true;
	}
}
