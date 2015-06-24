package de.freenet.advertisement;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.freenet.library.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class AdManager {
	
	private static final String TAG = "AdManager";
	private static final int BUFFER_SIZE = 8 * 1024;
	private static AdManager sInstance;
	
	private Hashtable<AdvertisementType, Advertisement> mAdvertisements;
	private LoadAdsListener mLoadAdsListener;
	private boolean mShowAds;
	
	public static enum AdvertisementType { INTERSTITIAL, BANNER1, BANNER2 };
	public static final int RETURN_FROM_SPLASH_AD = 100;
	
	public static AdManager getInstance() {
		
		if(sInstance == null) {
			sInstance = new AdManager();
		}
		
		return sInstance;
	}
	
	/**
	 * Advertisement manager constructor
	 */
	public AdManager() {
		mAdvertisements = new Hashtable<AdvertisementType, Advertisement>();
		mShowAds = true;
	}
	
	/**
	 * Should ads be shown.
	 * @param showAds true if ads should be shown, false otherwise
	 */
	public void setShowAds(boolean showAds) {
		mShowAds = showAds;
	}
	
	/**
	 * Should ads be shown.
	 * @return true if ads should be shown, false otherwise
	 */
	public boolean getShowAds() {
		return mShowAds;
	}
	
	/**
	 * Set an advertisement listener.
	 * @param listener The advertisement listener on which the interface functions should be called.
	 */
	public void setLoadAdsListener(LoadAdsListener listener) {
		mLoadAdsListener = listener;
	}
	
	/**
	 * Starts a background thread which downloads and parses the advertisement XML
	 * @param adsUrl
	 */
	public void loadAdsFromServer(String adsUrl) {
		mAdvertisements = new Hashtable<AdvertisementType, Advertisement>();
		
		try {
			URL url = new URL(adsUrl);
			new LoadAdsFromServerTask().execute(url);
		} catch (MalformedURLException e) {
			Log.e(TAG, "MalformedURLException: " + e.getMessage());
		}
	}
	
	/**
	 * Shows an advertisement
	 * @param type The type of the advertisement to show
	 * @param activity The application activity where to show the advertisement
	 */
	public void showAd(AdvertisementType type, Activity activity) {
		if (mShowAds) {
			new ShowAdTask(activity, type).execute();	
		}
	}
	
	/**
	 * Shows an advertisement after some delay
	 * @param type The type of the advertisement to show
	 * @param activity The application activity where to show the advertisement
	 * @param delay The delay i milliseconds
	 */
	public void showAd(final AdvertisementType type, final Activity activity, int delay) {
		if (mShowAds) {
			new Handler().postDelayed(new Runnable() {
	
				@Override
				public void run() {
					showAd(type, activity);
				}
	        	 
	        }, delay);
		}
	}
	
	/**
     * Asynchronous task that loads the advertisements from the server.
     */
	private class LoadAdsFromServerTask extends AsyncTask<URL, Void, Hashtable<AdvertisementType, Advertisement>> {

		protected Hashtable<AdvertisementType, Advertisement> doInBackground(URL... params) {
			
			if (params != null && params[0] != null) {
				HttpURLConnection conn = null;
				try {
					HttpURLConnection.setFollowRedirects(true);
					conn = (HttpURLConnection)params[0].openConnection();
					BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
					
					SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser sp = spf.newSAXParser();
                    XMLReader xr = sp.getXMLReader();
                    
                    AdHandler handler = new AdHandler();
                    xr.setContentHandler(handler);
                    
                    xr.parse(new InputSource(in));
                    
                    return handler.getParsedData();

				} catch (IOException e) {
					Log.e(TAG, "IOException: " + e.getMessage());
				} catch (ParserConfigurationException e) {
					Log.e(TAG, "ParserConfigurationException: " + e.getMessage());
				} catch (SAXException e) {
					Log.e(TAG, "SAXException: " + e.getMessage());
				} finally {
					if (conn != null) conn.disconnect();
				}
			}
			
			return new Hashtable<AdvertisementType, Advertisement>();
		}
		
		protected void onPostExecute(Hashtable<AdvertisementType, Advertisement> result) {
			mAdvertisements = result;
			
			if (mLoadAdsListener != null) {
				mLoadAdsListener.adsLoadedFromServer();
			}
		}
	}
	
	/**
     * Asynchronous task that loads the advertisements from the server.
     */
	private class ShowAdTask extends AsyncTask<Void, Void, Boolean> {
		
		private Activity mActivity;
		private AdvertisementType mType;
		
		public ShowAdTask(Activity activity, AdvertisementType type) {
			mActivity = activity;
			mType = type;
		}

		protected Boolean doInBackground(Void... params) {
			if (mAdvertisements.containsKey(mType) && mAdvertisements.get(mType).frequencyCounter % mAdvertisements.get(mType).frequency == 0) {
				
				HttpURLConnection conn = null;
				BufferedInputStream in = null;
				StringBuilder sb = new StringBuilder();
				try {
					conn = (HttpURLConnection)mAdvertisements.get(mType).url.openConnection();
					
					try {
						CookieManager cookieManager = CookieManager.getInstance();
						String cookie = cookieManager.getCookie(mAdvertisements.get(mType).url.toString());
						if (cookie != null) {
							conn.addRequestProperty("Cookie", cookie);
						}
					} catch (IllegalStateException e) {
						Log.e(TAG, "IllegalStateException: " + e.getMessage());
					}
					
					String cookie = conn.getHeaderField("Set-Cookie");
					if (cookie != null) {
						try {
							CookieManager cookieManager = CookieManager.getInstance();
							cookieManager.setAcceptCookie(true);
						    cookieManager.setCookie(mAdvertisements.get(mType).url.toString(), cookie);
						    CookieSyncManager.getInstance().sync();
						} catch (IllegalStateException e) {
							Log.e(TAG, "IllegalStateException: " + e.getMessage());
						}
					}
					in = new BufferedInputStream(conn.getInputStream(), BUFFER_SIZE);
					
					byte[] buffer = new byte[BUFFER_SIZE];
			    	int read;
			    	while ((read = in.read(buffer)) != -1) {
			    		String s = new String(buffer, 0, read);
			    		
			    		if (s.contains("appAction:removeBanner")) {
							Log.i(TAG, String.format("Server says it is not time to show ad! (%s)", mType.toString()));
							return false;
						}
			    		
			    		sb.append(s);
			    	}
				} catch (IOException e) {
					Log.e(TAG, "IOException: " + e.getMessage());
					return false;
				} finally {
					if (conn != null) conn.disconnect();
					
					try {
			        	if (in != null) in.close();
			        } catch (IOException e) {
			        	Log.e(TAG, "IOException: " + e.getMessage());
			        }
					
			        mAdvertisements.get(mType).webContent = sb.toString();
				}
				
				return true;
			}
			
			return false;
		}
		
		protected void onPostExecute(Boolean result) {
			
			if (mAdvertisements != null && mAdvertisements.size() > 0)
				mAdvertisements.get(mType).frequencyCounter++;
			
			if (result == null || result.equals(Boolean.FALSE)) return;
			
			if (mLoadAdsListener != null) {
				mLoadAdsListener.willShowAd(mType);
			}
			
			switch(mType) {
				case INTERSTITIAL:
					Intent i = new Intent(mActivity, SplashAd.class);
					i.putExtra(SplashAd.TAG, mAdvertisements.get(mType));
					mActivity.startActivityForResult(i, RETURN_FROM_SPLASH_AD);
			        try {
					    Method method = Activity.class.getMethod("overridePendingTransition", new Class[]{int.class, int.class});
					    method.invoke(mActivity, R.anim.slide_bottom_to_top, R.anim.no_sliding);
					} catch (Exception e) {
					    // Can't change animation, so do nothing
					}
					break;
				case BANNER1:
					AdWebView wv = new AdWebView(mActivity, mAdvertisements.get(mType));
					wv.loadDataWithBaseURL(mAdvertisements.get(mType).url.toString(), mAdvertisements.get(mType).webContent, "text/html", "utf-8", null);
					wv.setHorizontalScrollBarEnabled(false);
					wv.setVerticalScrollBarEnabled(false);
					
					if (mLoadAdsListener != null) {
						final float scale = mActivity.getResources().getDisplayMetrics().density;
						mLoadAdsListener.timeToShowBanner(wv, (int)(mAdvertisements.get(mType).width * scale), (int)(mAdvertisements.get(mType).height * scale));
					}
					break;
				case BANNER2:
					break;
			}
		}
	}
}
