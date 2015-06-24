package de.freenet.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class FileCacheManager {

	private static final String TAG = "FileCacheManager";
	private static final int BUFFER_SIZE = 8 * 1024;
	private static final String DEFAULT_SD_CARD_CACHE_DIRECTORY_WITH_FORMAT = "/Android/data/%s/cache/";
	
	private String mCachePath;
	private File mCacheRoot;
	private FileCacheListener mFileCacheListener;
	
	/**
	 * File cache manager constructor
	 * @param ctx The application context. Used for getting the package name
	 */
	public FileCacheManager(Context ctx) {
		
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			mCacheRoot = Environment.getExternalStorageDirectory();
			mCachePath = String.format(DEFAULT_SD_CARD_CACHE_DIRECTORY_WITH_FORMAT, ctx.getPackageName());
		}
		else {
			mCacheRoot = ctx.getCacheDir();
			mCachePath = "";
		}
		
		File f = new File(mCacheRoot, mCachePath);
        if (!f.exists()) {
        	f.mkdirs();
        }
	}
	
	/**
	 * File cache manager constructor for custom cache location
	 * @param ctx The application context. Used for getting the package name.
	 * @param path Custom path to use for saving and reading files.
	 * @deprecated Use FileCacheManager(Context, String, boolean)
	 */
	public FileCacheManager(Context ctx, String path) {
		
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			mCacheRoot = Environment.getExternalStorageDirectory();
			mCachePath = path;
		}
		else {
			mCacheRoot = ctx.getCacheDir();
			mCachePath = path;
		}
		
		File f = new File(mCacheRoot, mCachePath);
        if (!f.exists()) {
        	f.mkdirs();
        }
	}
	
	/**
	 * File cache manager constructor for custom cache location
	 * @param ctx The application context. Used for getting the package name.
	 * @param path Custom path to use for saving and reading files.
	 */
	public FileCacheManager(Context ctx, String path, boolean isPathRelative) {
		
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			mCacheRoot = Environment.getExternalStorageDirectory();
		}
		else {
			mCacheRoot = ctx.getCacheDir();
		}
		
		mCachePath = isPathRelative ? String.format(DEFAULT_SD_CARD_CACHE_DIRECTORY_WITH_FORMAT, ctx.getPackageName()) + path : path;
		
		File f = new File(mCacheRoot, mCachePath);
        if (!f.exists()) {
        	f.mkdirs();
        }
	}
	
	/**
	 * Set the file cache listener
	 * @param listener the listener
	 */
	public void setFileCacheListener(FileCacheListener listener) {
		mFileCacheListener = listener;
	}
	
	/**
	 * Creates a new file to fill with data at a later point
	 * @param filename the filename
	 * @return a File object for the newly created file
	 * @throws IOException throws a IOException if the file could not be created
	 */
	public File createNewFile(String filename) throws IOException {
		File f = new File(mCacheRoot, mCachePath + filename);
		if (!f.exists())
			f.createNewFile();
		
		return f;
	}
	
	/**
	 * Check if file exists.
	 * @param filename Name of the file to look for
	 * @return Returns true if file exists. False otherwise.
	 */
	public boolean fileExists(String filename) {
		File f = new File(mCacheRoot, mCachePath + filename);
		return f.exists();
	}
	
	/**
	 * Saves a file to the local store
	 * @param filename Name of the file to be stored
	 * @param inputStream The stream of content to be stored as a file in the local store
	 * @return Returns true if save was successfully completed. False otherwise
	 */
	public boolean saveFile(String filename, InputStream inputStream) {
		
		boolean success = true;
		
		File f = new File(mCacheRoot, mCachePath);
        if (!f.exists()) {
        	f.mkdirs();
        }
        
        BufferedInputStream in = null;
        BufferedOutputStream boust = null;
        FileOutputStream foust = null;
		try {
			f = new File(mCacheRoot, mCachePath + filename);
			if (!f.exists() && f.canWrite()) {
				if (!f.createNewFile())
					return false;
			}
			else if (f.exists() && !f.canWrite()) {
				return false;
			}
			else {
				deleteFile(filename);
			}
			
			in = new BufferedInputStream(inputStream, BUFFER_SIZE);
			foust = new FileOutputStream(f);
			boust = new BufferedOutputStream(foust, BUFFER_SIZE);
			
			int n = 0;
			byte[] buffer = new byte[BUFFER_SIZE];
			int byteRead = 0;
			while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
				boust.write(buffer, 0, n);
				byteRead += n;
				if (mFileCacheListener != null) mFileCacheListener.downloadProgressUpdated(byteRead);
			}
			boust.flush();
			
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException: " + e.getMessage());
			success = false;
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
			success = false;
		} finally {
			try {
				if (boust != null) boust.close();
	        } catch (IOException e) {
	        	Log.e(TAG, "IOException: " + e.getMessage());
	        }
	        try {
	        	if (foust != null) foust.close();
	        } catch (IOException e) {
	        	Log.e(TAG, "IOException: " + e.getMessage());
	        }
	        try {
	        	if (in != null) in.close();
	        } catch (IOException e) {
	        	Log.e(TAG, "IOException: " + e.getMessage());
	        }
		}
        
        return success;
	}
	
	/**
	 * Saves a serializable object to a file
	 * @param filename the filename of the file to save
	 * @param serializableObject the object to save. Must implement serializable
	 * @return true if the file was created, false otherwise
	 */
	public boolean saveObjectAsFile(String filename, Object serializableObject) {
		
		boolean success = true;
		
		File f = new File(mCacheRoot, mCachePath);
        if (!f.exists()) {
        	f.mkdirs();
        }
	
		FileOutputStream foust = null;
		ObjectOutputStream oost = null;
		try {
			f = new File(mCacheRoot, mCachePath + filename);
			if (!f.exists() && f.canWrite()) {
				if (!f.createNewFile())
					return false;
			}
			else if (f.exists() && !f.canWrite()) {
				return false;
			}
			else {
				deleteFile(filename);
			}
			foust = new FileOutputStream(f);
			
	    	// Write object with ObjectOutputStream
			oost = new ObjectOutputStream(foust);
			
	    	// Write object out to disk
			oost.writeObject(serializableObject);
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
			success = false;
		} finally {
			try {
	        	if (foust != null) foust.close();
	        } catch (IOException e) {
	        	Log.e(TAG, "IOException: " + e.getMessage());
	        }
			try {
	        	if (oost != null) oost.close();
	        } catch (IOException e) {
	        	Log.e(TAG, "IOException: " + e.getMessage());
	        }
		}
		
		return success;
	}
	
	/**
	 * Get a serializable object from a file.
	 * @param filename the filename of the file to read
	 * @return the serializable object, null if file does not exist
	 */
	public Object getObjectFromFile(String filename) {
		File f = new File(mCacheRoot, mCachePath + filename);
		
		if (!f.exists())
			return null;
		
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		Object obj = null;
		try {
			fis = new FileInputStream(f);
			
			// Read object using ObjectInputStream
			ois = new ObjectInputStream (fis);

			// Read an object
			obj = ois.readObject();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException: " + e.getMessage());
		} catch (StreamCorruptedException e) {
			Log.e(TAG, "StreamCorruptedException: " + e.getMessage());
			deleteFile(filename);
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "ClassNotFoundException: " + e.getMessage());
		} finally {
			try {
	        	if (fis != null) fis.close();
	        } catch (IOException e) {
	        	Log.e(TAG, "IOException: " + e.getMessage());
	        }
			try {
	        	if (ois != null) ois.close();
	        } catch (IOException e) {
	        	Log.e(TAG, "IOException: " + e.getMessage());
	        }
		}
		
		return obj;
	}
	
	/**
	 * Opens an InputStream of the given file
	 * @param filename Name of the file to look for
	 * @return Returns the stream.
	 * @throws FileNotFoundException Throws exception if file is not found. 
	 */
	public FileInputStream getFileInputStream(String filename) throws FileNotFoundException {
		
		File f = new File(mCacheRoot, mCachePath + filename);
		
		if (!f.exists())
			return null;
		
		FileInputStream fis = new FileInputStream(f);
		
		return fis;
	}
	
	/**
	 * Opens an OutputStream of the given file
	 * @param filename Name of the file to save
	 * @return Returns the stream. null if file does not exist
	 */
	public FileOutputStream getFileOutputStream(String filename) {
		File f = new File(mCacheRoot, mCachePath + filename);
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f, false);
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		}
		
		return fos;
	}
	
	/**
	 * Get a File object for a given filename
	 * @param filename the filename
	 * @return the file
	 */
	public File getFile(String filename) {
		return new File(mCacheRoot, mCachePath + filename);
	}

	/**
	 * Properly deletes a file. This method also delete the metadata file
	 * @param filename the filename of the file to delete.
	 * @return true if file was deleted, false otherwise
	 */
	public boolean deleteFile(String filename) {
		File f = getFile(".metadata/" + filename);
		if (f.exists())
			f.delete();
		
		f = getFile(filename);
		if (f.exists())
			return f.delete();
		
		return false;
	}
	
	/**
	 * Opens an InputStream of the given file
	 * @param filename Name of the file to look for
	 * @return Returns the stream.
	 * @throws FileNotFoundException Throws exception if file is not found. 
	 */
	public Uri getFileUri(String filename) throws FileNotFoundException {
		
		File f = getFile(filename);
		
		if (!f.exists())
			return null;
		
		return Uri.fromFile(f);
	}
	
	/**
	 * Sets a metadata value for a certain file
	 * @param filename the filename of a file whos metadata should be set
	 * @param key the metadata value key. If key exists it will be overwritten.
	 * @param value the metadata value to be set
	 */
	public void setMetadata(String filename, String key, String value) {
		String metaDataFilename = ".metadata/" + filename;
		File f = new File(mCacheRoot, mCachePath + ".metadata/");
		if (!f.exists()) {
			f.mkdirs();
		}
		
		Properties prop = new Properties();
		try {
			FileInputStream fis = getFileInputStream(metaDataFilename);
			if (fis != null) {
				prop.loadFromXML(fis);
			}
		} catch (InvalidPropertiesFormatException e) {
			Log.e(TAG, "InvalidPropertiesFormatException: " + e.getMessage());
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		}
		
		prop.put(key, value);
		try {
			FileOutputStream fos = getFileOutputStream(metaDataFilename);
			if (fos != null)
				prop.storeToXML(fos, "Property file", "UTF-8");
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		}
	}
	
	public void setETag(String filename, String etag) {
		if (etag == null || etag.equals("")) return;
		
		setMetadata(filename, "etag", etag);
	}
	
	/**
	 * Gets the ETag of the given file
	 * @param filename Name of the file to look for
	 * @return Returns the ETag generated from the size of the file. Empty string if file does not exist
	 */
	public String getETag(String filename) {
		String metaDataFilename = ".metadata/" + filename;
		
		Properties prop = new Properties();
		String etag = "";
		try {
			FileInputStream fis = getFileInputStream(metaDataFilename);
			if (fis != null) {
				prop.loadFromXML(fis);
				etag = prop.getProperty("etag", "");
			}
		} catch (InvalidPropertiesFormatException e) {
			Log.e(TAG, "InvalidPropertiesFormatException: " + e.getMessage());
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		}
		
		return etag;
	}
}
