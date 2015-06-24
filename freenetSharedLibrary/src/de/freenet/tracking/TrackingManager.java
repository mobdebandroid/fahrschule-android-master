package de.freenet.tracking;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;

public class TrackingManager {
	
	private static final String TAG = "TrackingManager";

	private static TrackingManager mInstance;
	private final ExecutorService mThreadPool = new ThreadPoolExecutor(1, 50, 10, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	private boolean mTesting;
	private final DefaultHttpClient mClient = new DefaultHttpClient();
	
	/**
	 * Get a singleton instance of this object
	 * @return The object instance
	 */
	public static TrackingManager getInstance() {
		
		if(mInstance == null) {
			mInstance = new TrackingManager();
		}
		
		return mInstance;
	}
	
	public TrackingManager() {
		super();
		mTesting = false;
	}
	
	/**
	 * Set testing mode. If testing is set to true, no tracking pixels will be transfered.
	 * @param testing the boolean value.
	 */
	public void setTesting(boolean testing) {
		mTesting = testing;
	}
	
	/**
	 * Add statistics to the sending queue.
	 * @param ctx The application context
	 * @param urlString A string representation of the URL where the statistics should be passed.
	 * This string can contain the various placeholders which will later be replaced with the according
	 * values.
	 */
	public void sendStatistics(Context ctx, String urlString) {
		
		if(urlString == null || urlString.trim().equals("") || ctx == null) return;
		
		try {
			
			Random rand = new Random(new Date().getTime());
			
			urlString = urlString.replace("<versions_nr>", ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName);
			urlString = urlString.replace("<randomValue>", String.valueOf(((long)Integer.MAX_VALUE * 2) % rand.nextInt(Integer.MAX_VALUE)));
			urlString = urlString.replace("<Referrer>", "AndroidApp");
			urlString = urlString.replace("<os_version>", Build.VERSION.RELEASE);
			try {
				urlString = urlString.replace("<device_platform>", URLEncoder.encode(Build.MODEL, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				urlString = urlString.replace("<device_platform>", Build.MODEL);
			}
			
			URL url = new URL(urlString);
			sendStatistics(url);
			
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		} catch (MalformedURLException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	/**
	 * Add statistics to the sending queue.
	 * @param url URL where the statistics should be passed.
	 */
	public void sendStatistics(final URL url) {
		if(url == null || mTesting) return;
		
		mThreadPool.submit(new Runnable() {

			@Override
			public void run() {
				try {
					HttpGet get = new HttpGet(url.toString());
					
					try {
						CookieManager cookieManager = CookieManager.getInstance();
						String cookie = cookieManager.getCookie(url.toString());
						if (!TextUtils.isEmpty(cookie)) {
							get.addHeader("Cookie", cookie);
						}
					} catch (IllegalStateException e) {
						Log.e(TAG, "IllegalStateException: " + e.getMessage());
					}
					HttpResponse response = mClient.execute(get);
					
					Log.i(TAG, "Pixel sent: " + response.getStatusLine().getStatusCode());
				}
				catch (IOException e) {
					Log.e(TAG, e.getClass().getSimpleName(), e);
				}
			}
			
		});		
	}
}
