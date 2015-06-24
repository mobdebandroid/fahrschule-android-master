package de.freenet.remoteresources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import de.freenet.cache.FileCacheManager;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

public class RemoteResourceManager {

	private static final String TAG = "RemoteResourceManager";
	
	private URL mResourceUrl;
	private FileCacheManager mFileCacheManager;
	private Context mContext;
	private String mResourceFilename;
	
	/**
	 * Remote resource manager constructor
	 * @param context The application context
	 * @param resourceUrl The external URL to the resource file
	 * @param resourceFilename Name of the local file where the external resource are / will be saved
	 */
	public RemoteResourceManager(Context context, URL resourceUrl, String resourceFilename) {
		mResourceUrl = resourceUrl;
		mFileCacheManager = new FileCacheManager(context);
		mContext = context;
		mResourceFilename = resourceFilename;
	}
	
	public void moveResourceForced() {
		AssetManager assetMgr = mContext.getAssets();
		try {
			InputStream is = assetMgr.open(mResourceFilename);
			if (mFileCacheManager.fileExists(mResourceFilename)) {
				mFileCacheManager.deleteFile(mResourceFilename);
			}
			mFileCacheManager.saveFile(mResourceFilename, is);
			
		} catch (IOException e) {
			Log.e(TAG, "moveResourceForced: " + e.getMessage());
		}
	}
	
	/**
	 * Function which copies the internal static version of the resource file as
	 * fallback in case a updated version from the remote resource server were
	 * not able to be retrieved. 
	 */
	public void moveResourceOnFirstStart() {
		AssetManager assetMgr = mContext.getAssets();
		try {
			InputStream is = assetMgr.open(mResourceFilename);
			if (!mFileCacheManager.fileExists(mResourceFilename)) {
				mFileCacheManager.saveFile(mResourceFilename, is);
			}
			
		} catch (IOException e) {
			Log.e(TAG, "moveResourceOnFirstStart: " + e.getMessage());
		}
	}
	
	/**
	 * Get an InputStream for the current resource
	 * @return the InputStream
	 * @throws FileNotFoundException thrown if file does not exist 
	 */
	public InputStream getResourceInputStream() throws FileNotFoundException {
		return mFileCacheManager.getFileInputStream(mResourceFilename);
	}
	
	/**
	 * Starts a background thread which checks for a new version of the given resource
	 * The background thread will upon completion call the appropriate interface function
	 * from RemoteResourceInterface on the context given in the object constructor.
	 */
	public void checkForUpdateOnServer() {
		new ResourceDownloaderTask().execute();
	}
	
	private class ResourceDownloaderTask extends AsyncTask<Void, Void, Boolean> {

		protected Boolean doInBackground(Void... params) {
			
			boolean success = true;
			
			HttpURLConnection conn = null;
			try {
				HttpURLConnection.setFollowRedirects(true);
				conn = (HttpURLConnection) mResourceUrl.openConnection();
				conn.addRequestProperty("If-None-Match", mFileCacheManager.getETag(mResourceFilename));

				switch(conn.getResponseCode()) {
					case 200:
						// File is updated. Download new version
						
						InputStream is = null;
		        		if (conn.getHeaderField("Content-Encoding") != null) {
		        			// This is where we will end up when the request was not properly decompressed.
		        			// We are able to decompress the content by wrapping the HTTP requests' InputStream
		        			// with a GZIPInputStream.
		        			is = new GZIPInputStream(conn.getInputStream());
		        		}
		        		else {
		        			is = conn.getInputStream();
		        		}
						
						success = mFileCacheManager.saveFile(mResourceFilename, is);
						String etag = conn.getHeaderField("ETag");
						mFileCacheManager.setETag(mResourceFilename, etag);
						Log.d(TAG, "New version of file " + mResourceFilename + " downloaded.");
						break;
					case 304:
						// Files match. Do nothing
						
						Log.d(TAG, "Local file " + mResourceFilename + " is latest version.");
						break;
					default:
						success = false;
						break;
				}
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				success = false;
			} finally {
				if (conn != null) conn.disconnect();
			}
			
			return success;
		}
		
		protected void onPostExecute(Boolean result) {
			
			try {
				
				if (result)
					((RemoteResourceInterface)mContext).updateCheckSucceded(mResourceUrl, mResourceFilename);
				else
					((RemoteResourceInterface)mContext).updateCheckDidFail(mResourceUrl, mResourceFilename);
				
			} catch (ClassCastException e) {
				Log.v(TAG, "ClassCastException: " + e.getMessage());
			} catch (NullPointerException e) {
				Log.e(TAG, "NullPointerException: " + e.getMessage());
			}
			
		}
	}
}
