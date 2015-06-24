package de.freenet.pocketfahrschulelite.classes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences.LicenseClass;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.util.Log;

public class Utils {
	
	private static final String TAG = "Utils";
	private static final int BUFFER_SIZE = 8 * 1024;
	
	/**
	 * Creates a simple dialog with one OK button to close the dialog.
	 * @param ctx The Context used.
	 * @param title The title of the dialog.
	 * @param message The message of the dialog.
	 */
	public static void createDialog(Context ctx, String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);		
		builder.setTitle(title).setMessage(message)
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.dismiss();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	/**
	 * Checks to see if there is a connection to the Internet available.
	 * @param ctx The Context used.
	 * @return true if a connection to the Internet exists, false otherwise.
	 */
	public static boolean hasInternetConnection(Context ctx) {
		ConnectivityManager conMgr =  (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conMgr != null && conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable()
			&& conMgr.getActiveNetworkInfo().isConnected()) {
			
			return true;
		}
		
		return false;
	}
	
	public static String getContentFromFile(File file) {
		if (file == null) return "";
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			
			char[] buffer = new char[BUFFER_SIZE];
	        while(reader.read(buffer) != -1) {
	            sb.append(buffer);
	            buffer = new char[BUFFER_SIZE];
	        }
	        
	        reader.close();
	        return sb.toString();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException: " + e.getMessage());
		}
		catch (IOException e) {
			Log.e(TAG, "FileNotFoundException: " + e.getMessage());
		}
		
		return "";
	}
	
	public static String getContentFromFile(InputStream stream) {
		if (stream == null) return "";
		StringBuilder sb = new StringBuilder();
		
        try {
        	BufferedReader br = new BufferedReader(new InputStreamReader(stream));
    		
    		String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			
			br.close();
		} catch (IOException e) {
			return "";
		}
        
        return sb.toString();
	}
	
	public static LicenseClass getLicenseClassFromId(int licenseClassId) {
		
		switch (licenseClassId) {
			case 1:
				return LicenseClass.A;
			case 2:
				return LicenseClass.A1;
			case 4:
				return LicenseClass.B;
			case 8:
				return LicenseClass.C;
			case 16:
				return LicenseClass.C1;
			case 32:
				return LicenseClass.CE;
			case 64:
				return LicenseClass.D;
			case 128:
				return LicenseClass.D1;
			case 256:
				return LicenseClass.S;
			case 512:
				return LicenseClass.T;
			case 1024:
				return LicenseClass.L;
			case 2048:
				return LicenseClass.M;
			case 4096:
				return LicenseClass.MOFA;
		}
		
		return LicenseClass.B;
	}
	
	public static void showQuitApplicationDialog(final Activity activity) {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            switch (which) {
	            case DialogInterface.BUTTON_POSITIVE:
	            	activity.finish();
	            	break;
	            case DialogInterface.BUTTON_NEGATIVE:
	                // Do nothing
	                break;
	            }
	        }
	    };
	    
	    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
	    builder.setMessage(R.string.quit_dialogbox).setPositiveButton(R.string.yes, dialogClickListener)
	        .setNegativeButton(R.string.no, dialogClickListener).setTitle(R.string.quit).show();
	}
}