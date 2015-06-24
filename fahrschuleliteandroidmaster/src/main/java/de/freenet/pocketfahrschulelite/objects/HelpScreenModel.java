package de.freenet.pocketfahrschulelite.objects;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.freenet.pocketfahrschulelite.app.FahrschuleApplication;

import android.util.DisplayMetrics;
import android.util.Log;

public class HelpScreenModel {
	
	private static final String TAG = "HelpScreenModel";
	
	private static DisplayMetrics sMetrics;

	public boolean isWelcomeScreen;
	public int helpScreenResourceId;
	public List<HelpIndicator> helpIndicators;
	
	public HelpScreenModel() {
		super();
		if (sMetrics == null) {
			sMetrics = FahrschuleApplication.getAppContext().getResources().getDisplayMetrics();
		}
		isWelcomeScreen = false;
		helpScreenResourceId = -1;
		helpIndicators = Arrays.asList(new HelpIndicator[0]);
	}
	
	public HelpScreenModel(int resourceId, JSONArray array) {
		this();
		helpScreenResourceId = resourceId;
		
		HelpIndicator[] indicators = new HelpIndicator[array.length()];
		for (int i = 0;i < array.length();i++) {
			indicators[i] = new HelpIndicator(array.optJSONObject(i));
		}
		helpIndicators = Arrays.asList(indicators);
	}
	
	public class HelpIndicator {
		
		public int x;
		public int y;
		public String text;
		public boolean rotateImage;
		
		public HelpIndicator() {
			super();
			x = 0;
			y = 0;
			text = "";
			rotateImage = false;
		}
		
		public HelpIndicator(JSONObject object) {
			this();
			
			if (object != null) {
				try {
					x = (object.has("x_mdpi") && sMetrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM) ? object.getInt("x_mdpi") - 20 : 
						((object.has("x_xhdpi") && sMetrics.densityDpi == 320) ? object.getInt("x_xhdpi") - 20 : object.getInt("x") - 20);
					y = (object.has("y_mdpi") && sMetrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM) ? object.getInt("y_mdpi") - 20 :
						((object.has("y_xhdpi") && sMetrics.densityDpi == 320) ? object.getInt("y_xhdpi") - 20 : object.getInt("y") - 20);
					text = object.getString("text");
					rotateImage = object.has("icon");
				} catch (JSONException e) {
					Log.e(TAG, "JSONException: " + e.getMessage());
				}
			}
		}
	}
}
