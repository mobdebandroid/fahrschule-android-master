package de.freenet.c2dm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.OpenUDID.OpenUDID_manager;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.freenet.c2dm.MessageConstants.RequestType;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.util.Log;

public abstract class PushalotService extends Service {
	
	private static final String TAG = "PushalotService"; 
	private static final String C2DM_TOKEN_KEY = "c2dmToken";
	private static final String PREFS_NAME = "de.freenet.c2dm.PushalotService_preferences";
	public final static byte[] SALT = { -72, 6, 80, -119, -3, -88, -21, -41, -87, 4, 72, -6, -81, 9, -48, 59 };
	
	private static ConcurrentLinkedQueue<PushRequest> mPendingRequests = new ConcurrentLinkedQueue<PushRequest>();
	
	private SharedPreferences mSettings;
	private SharedPreferences.Editor mEditor;
	private Context mContext;
	private String mPushalotIdentifier;
	private String mDeviceIdPostfix;
	private PushRequestThread mThread;
	
	/**
	 * This method should return the service url intended to use. Ex. in a debugging
	 * environment this can be the url to the test server.
	 * @return the service url
	 */
	protected abstract String getAppServicesUrl();
	
	/**
	 * The Push-a-lot server needs a username and password to handle requests. This
	 * method return these, which should be unique for evert application.
	 * @return the credentials used for Pushalot requests
	 */
	protected abstract UsernamePasswordCredentials getUsernamePasswordCredentials();
	
	/**
	 * Forwards a array containing the subscriptions this device is registered for.
	 * This function will be called asynchronous cause the array first has to be received
	 * from the Push-a-lot server.
	 * @param subscriptions a JSONArray containing the subscriptions for this device
	 */
	protected abstract void registeredSubscriptions(JSONArray subscriptions);
	
	/**
	 * Public constructor
	 * @param context application context
	 */
	public PushalotService(Context context) {
		mSettings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		mEditor = mSettings.edit();
		mContext = context;
	}
	
	@Override
    public void onStart(Intent intent, int startId) {
		handleCommand(intent);
    }
	
	/**
	 * Handles the various requests handed to the Push-a-lot service. If overridden, super method should be placed last.
	 * @param intent the intent
	 */
	protected void handleCommand(Intent intent) {
		try {
			initializePushalotService(mContext);
		} catch (NameNotFoundException e) {
			if (MessageConstants.DEBUG) {
				e.printStackTrace();
			}
			return;
		} catch (MissingMetaDataException e) {
			if (MessageConstants.DEBUG) {
				e.printStackTrace();
			}
			return;
		}
		
		if (intent == null || intent.getAction() == null)
			return;
		
		String action = intent.getAction();
		if (action.equals(MessageConstants.REGISTER_GOOGLE_INTENT_ACTION)) {
			registerUserGoogle();
		}
		else if (action.equals(MessageConstants.REGISTRATION_INTENT_ACTION)) {
			handleRegistration(mContext, intent);
		}
		
		runPendingRequests();
	}
	
	@Override
    public void onDestroy() {
		super.onDestroy();
		
		if (mThread != null)
			mThread.interrupt();
    }
	
	private void runPendingRequests() {
		
		if (mThread == null) {
			mThread = new PushRequestThread();
			mThread.start();
		}
	}
	
	/**
	 * Initialize the service setting the necessary parameters for Pushalot requests.
	 * @param context the application context
	 * @throws MissingMetaDataException is thrown if the necessart <meta-data> parameters pushalotIdentifier and deviceIdPostfix are missing
	 * @throws NameNotFoundException thrown if a receiver with the given class name can not be found on the system.
	 */
	private void initializePushalotService(Context context) throws MissingMetaDataException, NameNotFoundException {
		if (mDeviceIdPostfix == null || mPushalotIdentifier == null) {
			OpenUDID_manager.sync(context);
			
			ComponentName cn = new ComponentName(context, getClass());
	    	ServiceInfo si = context.getPackageManager().getServiceInfo(cn, PackageManager.GET_META_DATA);
	    	
	    	String pushalotIdentifier = si.metaData.getString("pushalotIdentifier");
	    	if (pushalotIdentifier == null) {
	    		throw new MissingMetaDataException("C2DM BroadcastReceiver is missing meta data for key pushalotIdentifier");
	    	}
	    	else {
	    		mPushalotIdentifier = pushalotIdentifier;
	    	}
	    	
	    	String deviceIdPostfix = si.metaData.getString("deviceIdPostfix");
	    	if (deviceIdPostfix == null) {
	    		throw new MissingMetaDataException("C2DM BroadcastReceiver is missing meta data for key deviceIdPostfix");
	    	}
	    	else {
	    		mDeviceIdPostfix = deviceIdPostfix;
	    	}
	    	
	    	// If this is a new installation the C2DM token is not yet set. A subscription fetch request will however be made
	    	// after the registration is completed.
	    	if (isRegisteredForPush()) {
	    		mPendingRequests.offer(new PushRequest(null, RequestType.FETCH_SUBSCRIPTIONS));
	    	}
		}
	}
	
	/**
	 * Register a device for Googles push service. (C2DM)
	 * If successful, the response from Google is handled by the MessageReceiver.
	 */
	public void registerUserGoogle() {
		/**
		 * User is not yet registered for push notification.
		 * Therefore we'll pass a registration intent to the system in order
		 * to register this device.
		 */
		if (!isRegisteredForPush()) {
			StringCrypto crypto = new StringCrypto(SALT);
			Intent registrationIntent = new Intent(MessageConstants.REGISTER_INTENT_ACTION);
	    	registrationIntent.putExtra("app", PendingIntent.getBroadcast(mContext, 0, new Intent(), 0)); // boilerplate
	    	// Our sender e-mail is encoded to prevent misuse.
	    	registrationIntent.putExtra("sender", crypto.decrypt("E0C0F013CC71A0087F3FB240ADB2545261AF315451B6846950D1E9EC3FD8C41B"));
	    	mContext.startService(registrationIntent);
		}
	}
	
	/**
	 * Register a device for push notifications. The payload will be sent to our
	 * Push-a-lot server after Google has provided us with the registration id.
	 * @param payload the payload that should be passed to the server.
	 * @return true if the operation was successful, false otherwise
	 */
	public boolean registerUserPushalot(JSONObject payload) {
		boolean success = true;
		final DefaultHttpClient httpClient = new DefaultHttpClient();
        final HttpPost post = new HttpPost(String.format("%s/pushalot/rest/android/device", getAppServicesUrl()));
        post.addHeader("Content-Type", "application/json");
        
        post.addHeader(BasicScheme.authenticate(getUsernamePasswordCredentials(), "UTF-8", false));
        try {
			post.setEntity(new StringEntity(payload.toString()));
			HttpResponse response = httpClient.execute(post);
			
			if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK)
				success = false;

		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "UnsupportedEncodingException: " + e.getMessage());
			success = false;
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException: " + e.getMessage());
			success = false;
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
			success = false;
		} finally {
			/**
			 * When HttpClient instance is no longer needed,
			 * shut down the connection manager to ensure
			 * immediate deallocation of all system resources
			 */
            httpClient.getConnectionManager().shutdown();
            
            // As last thing we save the registration ID for later use.
            if (success) {
            	setC2DMToken(payload.optString("androidToken", ""));
            	
            	// User might have reinstalled the application. Fetch any previous subscription.
            	mPendingRequests.offer(new PushRequest(null, RequestType.FETCH_SUBSCRIPTIONS));
            }
        }
        
        return success;
	}
	
	/**
	 * A push registration was received. We save the registration id from Google and then forward it to our Push-a-lot server.
	 * @param context the context
	 * @param intent the received message intent
	 */
	private void handleRegistration(Context context, Intent intent) {
	    String registration = intent.getStringExtra("registration_id");
	    if (intent.getStringExtra("error") != null) {
	    	// Registration failed, should try again later.
	    	mPendingRequests.offer(new PushRequest(null, RequestType.GOOGLE_REGISTRATION));
	    } else if (intent.getStringExtra("unregistered") != null) {
	        // Do nothing
	    } else if (registration != null) {
	    	
	    	String android_id = OpenUDID_manager.getOpenUDID() + mDeviceIdPostfix;
	    	
	    	/**
	    	 * Send the registration ID to the push-a-lot server.
	    	 * We do this in an separate thread to avoid the UI from freezing.
	    	 */
	    	JSONObject payload = new JSONObject();
	    	try {
	    		payload.put("androidToken", registration);
				payload.put("deviceToken", android_id);
				payload.put("appIdentifier", mPushalotIdentifier);
			} catch (JSONException e) {
				Log.e(TAG, "JSONException: " + e.getMessage());
			}
	    	
	    	mPendingRequests.offer(new PushRequest(payload, RequestType.REGISTRATION));
	    }
	}
	
	/**
	 * Fetches and delivers the JSONArray containing subscriptions for the current device to registeredSubscriptions()
	 * @return true if the request was successful, false otherwise
	 */
	private boolean fetchSubscriptions() {
		JSONArray result = null;
		final DefaultHttpClient httpClient = new DefaultHttpClient();
		
		String android_id = OpenUDID_manager.getOpenUDID() + mDeviceIdPostfix;
        final HttpGet get = new HttpGet(String.format("%s/pushalot/rest/android/device/%s/%s/%s", getAppServicesUrl(),
        		mPushalotIdentifier, getC2DMToken(), android_id));
        
        get.addHeader(BasicScheme.authenticate(getUsernamePasswordCredentials(), "UTF-8", false));
        try {
			HttpResponse response = httpClient.execute(get);
			
			if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
				result = new JSONArray(getContentFromStream(response.getEntity().getContent()));
			}

		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "UnsupportedEncodingException: " + e.getMessage());
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		} catch (IllegalStateException e) {
			Log.e(TAG, "IllegalStateException: " + e.getMessage());
		} catch (JSONException e) {
			Log.e(TAG, "JSONException: " + e.getMessage());
		} finally {
			/**
			 * When HttpClient instance is no longer needed,
			 * shut down the connection manager to ensure
			 * immediate deallocation of all system resources
			 */
            httpClient.getConnectionManager().shutdown();
            
            /**
             * If we received the array containing all subscriptions we forward them to the abstract method
             * registeredSubscriptions(JSONArray) so it can be taken cared of accordingly.
             */
            if (result != null) {
            	registeredSubscriptions(result);
            }
        }
        
        return result != null;
	}
	
	public void registerSubscription(JSONObject payload) {
		if (payload == null) return;
		
		mPendingRequests.offer(new PushRequest(payload, RequestType.SUBSCRIPTION));
	}
	
	/**
	 * Register or unregister user for push subscription. Example of this is
	 * a Bundesliga match/team or e-mail push for certain e-mail address.
	 * @param payload the subscription payload.
	 * @return true if the request was successfully delivered, false otherwise.
	 */
	private boolean handleSubscription(JSONObject payload) {
		boolean success = true;
		final DefaultHttpClient httpClient = new DefaultHttpClient();
		
		String android_id = OpenUDID_manager.getOpenUDID() + mDeviceIdPostfix;
        final HttpPut put = new HttpPut(String.format("%s/pushalot/rest/android/device/%s/%s/%s", getAppServicesUrl(),
        		mPushalotIdentifier, getC2DMToken(), android_id));
        put.addHeader("Content-Type", "application/json");
        
        put.addHeader(BasicScheme.authenticate(getUsernamePasswordCredentials(), "UTF-8", false));
        try {
        	put.setEntity(new StringEntity(payload.toString()));
			HttpResponse response = httpClient.execute(put);
			
			if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK)
				success = false;

		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "UnsupportedEncodingException: " + e.getMessage());
			success = false;
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException: " + e.getMessage());
			success = false;
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
			success = false;
		} finally {
			/**
			 * When HttpClient instance is no longer needed,
			 * shut down the connection manager to ensure
			 * immediate deallocation of all system resources
			 */
            httpClient.getConnectionManager().shutdown();
        }
        
		return success;
	}
	
	/**
	 * Get the c2dm token from the Google registration.
	 * @return the c2dm token or empty string if no token is set.
	 */
	public String getC2DMToken() {
		return mSettings.getString(C2DM_TOKEN_KEY, "");
	}
	
	/**
	 * Set the c2dm token. This function is set private because any extension of this class have no
	 * reason to set the token. The registration is taken care of in this class.
	 * @param token the c2dm token
	 */
	private void setC2DMToken(String token) {
		mEditor.putString(C2DM_TOKEN_KEY, token);
		mEditor.commit();
	}
	
	/**
	 * Check to see if this device is registered for push notification. If the device is not yet registered,
	 * you can register it by starting this service using the Intent action MessageConstants.REGISTER_GOOGLE_INTENT_ACTION
	 * @return true if device is registered for push notification, false otherwise.
	 */
	public boolean isRegisteredForPush() {
		return mSettings.contains(C2DM_TOKEN_KEY) && !mSettings.getString(C2DM_TOKEN_KEY, "").equals("");
	}
	
	/**
	 * Thread which is responsible for registering the user by our Push-a-lot server.
	 */
	private class PushRequestThread extends Thread {
		
		private static final int THREAD_SLEEP_TIME = 10 * 1000; // 10 seconds
		private static final int MAX_SEND_RETRIES = 5;
		
		@Override
		public synchronized void start() {
			super.start();
		}
		
		@Override
		public void run() {
			
			PushRequest request;
	        while ((request = mPendingRequests.poll()) != null) {
	        	if (isInterrupted())
	        		break;
	        	
	        	if (MessageConstants.DEBUG) {
	        		Log.i(TAG, String.format("Handling a %s request. %d still in queue.", request.requestType.toString(), mPendingRequests.size()));
	        	}
	        	
	        	boolean success = true;
	        	switch(request.requestType) {
	        		case REGISTRATION:
	        			success = registerUserPushalot(request.payload);
	        			break;
	        		case SUBSCRIPTION:
	        			success = handleSubscription(request.payload);
	        			break;
	        		case FETCH_SUBSCRIPTIONS:
	        			success = fetchSubscriptions();
	        			break;
	        		case GOOGLE_REGISTRATION:
	        			registerUserGoogle();
	        			break;
	        	}
	        	
	        	if (!success) {
	        		request.retries++;
	        		if (MessageConstants.DEBUG) {
		        		Log.e(TAG, String.format("%s request failed. Re-adding to queue for retry nr. %d", request.requestType.toString(), request.retries));
		        	}
	        		if (request.retries <= MAX_SEND_RETRIES) mPendingRequests.offer(request);
	        	}
	        	
	        	try {
					sleep(THREAD_SLEEP_TIME); // sleep before handling next request
				} catch (InterruptedException e) {
					if (MessageConstants.DEBUG) {
						e.printStackTrace();
					}
				} 
	        }
	        
	        mThread = null;
		}
	}
	
	private static String getContentFromStream(InputStream stream) {
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
	
	private class PushRequest {
		public JSONObject payload;
		public RequestType requestType;
		public int retries;
		
		public PushRequest(JSONObject payload, RequestType requestType) {
			this.payload = payload;
			this.requestType = requestType;
			this.retries = 0;
		}
	}
}
