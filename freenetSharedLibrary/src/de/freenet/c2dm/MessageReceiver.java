package de.freenet.c2dm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public abstract class MessageReceiver extends BroadcastReceiver {
	
	private static final String TAG = "MessageReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(MessageConstants.REGISTRATION_INTENT_ACTION)) {
			if (MessageConstants.DEBUG) {
				Log.i(TAG, "Registered new device");
			}
			
			Intent newIntent = new Intent(intent);
			newIntent.setClass(context, getServiceClass());
	        context.startService(newIntent);
	        
	    } else if (intent.getAction().equals(MessageConstants.RECEIVE_INTENT_ACTION)) {
	    	if (MessageConstants.DEBUG) {
				Log.i(TAG, "A message was received");
			}
	        handleMessage(context, intent);
	    }
	}
	
	/**
	 * A push message is received. We show a notification with the text contained.
	 * @param context the context
	 * @param intent our message intent
	 */
	protected abstract void handleMessage(Context context, Intent intent);
	
	/**
	 * Get the service super class
	 * @return the service class
	 */
	protected abstract Class<?> getServiceClass();
}
