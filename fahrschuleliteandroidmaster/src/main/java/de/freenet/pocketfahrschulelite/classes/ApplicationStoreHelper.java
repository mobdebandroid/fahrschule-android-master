package de.freenet.pocketfahrschulelite.classes;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;

public class ApplicationStoreHelper {

	public static void openFullVersionStorePage(Context context) {
		openStorePageInternat(context, "de.freenet.pocketfahrschule");
	}
	
	public static void openLiteVersionStorePage(Context context) {
		openStorePageInternat(context, "de.freenet.pocketfahrschulelite");
	}
	
	private static void openStorePageInternat(Context context, String packageName) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
    	switch (FahrschulePreferences.getInstance().getApplicationStore()) {
    		case ANDROIDPIT:
    			intent.setData(Uri.parse("appcenter://package/" + packageName));
    			try {
    				context.startActivity(intent);
    	    	}
    	    	catch (ActivityNotFoundException ex) {
    	    		Utils.createDialog(context, context.getString(R.string.androidpit_not_installed), context.getString(R.string.androidpit_not_installed_desc));
    	    	}
    			break;
    		case MARKET:
    			intent.setData(Uri.parse("market://details?id=" + packageName));
    			try {
    				context.startActivity(intent);
    	    	}
    	    	catch (ActivityNotFoundException ex) {
    	    		Utils.createDialog(context, context.getString(R.string.market_not_installed), context.getString(R.string.market_not_installed_desc));
    	    	}
    			break;
    		case SAMSUNG_APPS:
    			intent.setData(Uri.parse("samsungapps://ProductDetail/" + packageName));
    	    	try {
    	    		context.startActivity(intent);
    	    	}
    	    	catch (ActivityNotFoundException ex) {
    	    		Utils.createDialog(context, context.getString(R.string.samsungapps_not_installed), context.getString(R.string.samsungapps_not_installed_desc));
    	    	}
    			break;
    		case AMAZON:
    			intent.setData(Uri.parse("amzn://apps/android?p=" + packageName));
    	    	try {
    	    		context.startActivity(intent);
    	    	}
    	    	catch (ActivityNotFoundException ex) {
    	    		Utils.createDialog(context, context.getString(R.string.amazon_not_installed), context.getString(R.string.amazon_not_installed_desc));
    	    	}
    			break;
    	}
	}
}
