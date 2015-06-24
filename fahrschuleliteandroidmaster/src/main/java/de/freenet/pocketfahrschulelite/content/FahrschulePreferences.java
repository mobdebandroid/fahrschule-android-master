package de.freenet.pocketfahrschulelite.content;

import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import de.freenet.pocketfahrschulelite.app.FahrschuleApplication;
import de.freenet.pocketfahrschulelite.classes.Utils;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;

public class FahrschulePreferences {
	
	private static final String TAG = "FahrschulePreferences";
	
	public enum LicenseClass {
		A (1),
		A1 (2),
		B (4),
		C (8),
		C1 (16),
		CE (32),
		D (64),
		D1 (128),
		S (256),
		T (512),
		L (1024),
		M (2048),
		MOFA (4096);
		
		private final int mId;
		private LicenseClass (int id) {
	        mId = id;
	    }
		
		public int getId() { return mId; } 
	};
	public enum TeachingType {
		FIRST_TIME_LICENSE (1),
		ADDITIONAL_LICENSE (2);
		
		private final int mId;
		private TeachingType (int id) {
	        mId = id;
	    }
		
		public int getId() { return mId; } 
	};
	public static enum ApplicationStore { MARKET, SAMSUNG_APPS, ANDROIDPIT, AMAZON };
	
	private static final String PREFS_NAME = "de.freenet.pocketfahrschulelite_preferences";
	
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mEditor;
	private Properties mAbakusProperties;
	
	private static FahrschulePreferences sFahrschulePreferences;

	public static FahrschulePreferences getInstance() {
		
		if (sFahrschulePreferences == null)
			sFahrschulePreferences = new FahrschulePreferences(FahrschuleApplication.getAppContext());
		
		return sFahrschulePreferences;
	}
	
	public FahrschulePreferences(Context context) {
		mSharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
		
		mAbakusProperties = new Properties();
		try {
			mAbakusProperties.loadFromXML(context.getAssets().open("abakus.xml"));
		} catch (InvalidPropertiesFormatException e) {
			Log.e(TAG, "InvalidPropertiesFormatException: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		}
	}
	
	public boolean isFirstStart() {
		return getCurrentLicenseClassString().equals("-1");
	}
	
	public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		mSharedPreferences.registerOnSharedPreferenceChangeListener(listener);
	}
	
	public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
		mSharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
	}
	
	public boolean setCurrentLicenseClass(int licenseClass) {
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.putString("licenseClass", String.valueOf(licenseClass));
		return editor.commit();
	}
	
	public LicenseClass getCurrentLicenseClass() {
		int licenseClass = Integer.valueOf(getCurrentLicenseClassString());
		
		return Utils.getLicenseClassFromId(licenseClass);
	}
	
	public boolean forceLicenseClassUpdate() {
		SharedPreferences.Editor editor = mSharedPreferences.edit();
		
		String currentLicenseClass = getCurrentLicenseClassString();
		editor.putString("licenseClass", "0");
		editor.commit();
		editor.putString("licenseClass", currentLicenseClass);
		
		return editor.commit();
	}
	
	public String getCurrentLicenseClassString() {
		return mSharedPreferences.getString("licenseClass", "-1");
	}
	
	public TeachingType getCurrentTeachingType() {
		String defValue = "";
		if (getCurrentLicenseClass() == LicenseClass.C || getCurrentLicenseClass() == LicenseClass.C1 ||
			getCurrentLicenseClass() == LicenseClass.CE || getCurrentLicenseClass() == LicenseClass.D ||
			getCurrentLicenseClass() == LicenseClass.D1 || getCurrentLicenseClass() == LicenseClass.D) {
			defValue = "2";
		}
		else {
			defValue = "1";
		}
		int teachingType = Integer.valueOf(mSharedPreferences.getString(String.format("teachingType_%s", getCurrentLicenseClass().toString()), defValue));
		
		switch (teachingType) {
			case 1:
				return TeachingType.FIRST_TIME_LICENSE;
			case 2:
				return TeachingType.ADDITIONAL_LICENSE;
		}
		
		return TeachingType.FIRST_TIME_LICENSE;
	}
	
	public String getCurrentTeachingTypeString() {
		String defValue = "";
		if (getCurrentLicenseClass() == LicenseClass.C || getCurrentLicenseClass() == LicenseClass.C1 ||
			getCurrentLicenseClass() == LicenseClass.CE || getCurrentLicenseClass() == LicenseClass.D ||
			getCurrentLicenseClass() == LicenseClass.D1 || getCurrentLicenseClass() == LicenseClass.D) {
			defValue = "2";
		}
		else {
			defValue = "1";
		}
		return mSharedPreferences.getString(String.format("teachingType_%s", getCurrentLicenseClass().toString()), defValue);
	}
	
	public boolean isGuestMode() {
		return mSharedPreferences.getBoolean("guestMode", false);
	}
	
	public boolean isInstantSolutionMode() {
		return false;
	}
	
	public boolean isOfficialExamLayout() {
		return mSharedPreferences.getBoolean("officialExamLayout", false);
	}
	
	public void setStVOBookmarkedPage(int page) {
		mEditor.putInt("stvoPage", page);
		mEditor.commit();
	}
	
	public int getStVOBookmarkedPage() {
		return mSharedPreferences.getInt("stvoPage", 0);
	}
	
	public void setFormulaIndex(int index) {
		mEditor.putInt("formulaIndex", index);
		mEditor.commit();
	}
	
	public int getFormulaIndex() {
		return mSharedPreferences.getInt("formulaIndex", 0);
	}
	
	public void setVelocity(int velocity) {
		mEditor.putInt("velocity", velocity);
		mEditor.commit();
	}
	
	public int getVelocity() {
		return mSharedPreferences.getInt("velocity", 0);
	}
	
	public void setActiveBrakingDistanceButton(int id) {
		mEditor.putInt("activeBrakingDistanceButton", id);
		mEditor.commit();
	}
	
	public int getActiveBrakingDistanceButton() {
		return mSharedPreferences.getInt("activeBrakingDistanceButton", 0);
	}

	public void setTrafficSignIndex(int index) {
		mEditor.putInt("trafficSignIndex", index);
		mEditor.commit();
	}
	
	public int getTrafficSignIndex() {
		return mSharedPreferences.getInt("trafficSignIndex", 0);
	}
	
	public String getTrackingUrl(String key) {
		return mAbakusProperties.getProperty(key, "").replace("<klassen_id>", getCurrentLicenseClass().name().toLowerCase());
	}
	
	public ApplicationStore getApplicationStore() {
		return ApplicationStore.MARKET;
	} 
}
