package de.freenet.imageloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import de.freenet.cache.FileCacheManager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader {
	
	private static final String TAG = "ImageLoader";
	
	private static ImageLoader mInstance;
	
	private FileCacheManager mFileCacheMgr;
	private ConcurrentLinkedQueue<ImageLoaderListener> mImageLoaderListeners;
	private static Resources mResources;
	
	private static final int SOFT_CACHE_CAPACITY = 10;
    private static final int DELAY_BEFORE_PURGE = 10 * 1000; // in milliseconds

    // Soft cache for bitmaps kicked out of hard cache
    private final static ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>(SOFT_CACHE_CAPACITY);
	
    private final Handler purgeHandler = new Handler();
    
    private final Runnable purger = new Runnable() {
        public void run() {
            clearCache();
        }
    };
    
	/**
	 * Create a singleton ImageLoader for the given context
	 * @param context Context
	 * @return ImageLoader
	 */
	public static ImageLoader createInstance(Context context) {
		
		if (mInstance == null) {
			mInstance = new ImageLoader(context);
		}
		
		return mInstance;
	}
	
	/**
	 * Singleton access to a ImageLoader. An IllegalStateException will be thrown if createInstance(Context) is not called before.
	 * @return ImageLoader
	 */
	public static ImageLoader getInstance() {
		
		if (mInstance == null) {
			throw new IllegalStateException("Instance is null. Call createInstance(Context) first.");
		}
		
		return mInstance;
	}
	
	private ImageLoader(Context context) {
		mFileCacheMgr = new FileCacheManager(context);
		mResources = context.getResources();
		mImageLoaderListeners = new ConcurrentLinkedQueue<ImageLoaderListener>();
	}
	
	public void addImageLoaderListener(ImageLoaderListener listener) {
		if (listener == null) return;
		mImageLoaderListeners.add(listener);
	}
	
	public boolean removeImageLoaderListener(ImageLoaderListener listener) {
		if (listener == null) return false;
		return mImageLoaderListeners.remove(listener);
	}

	/** 
	 * Queues an image url for download. Upon completion will the image be shown in the provided image view.
	 * @param url location of the image
	 * @param imageView image view that should display the image once it has finished downloading
	 */
	public void queueUrlForImageView(String url, ImageView imageView) {
		this.queueUrlForImageView(url, imageView, new Preferences());
	}
	
	/**
	 * Queues an image url for download. Upon completion will the image be shown in the provided image view. 
	 * @param url location of the image
	 * @param imageView image view that should display the image once it has finished downloading
	 * @param preferences image preferences. Will manipulate the downloaded image accordingly.
	 */
	public void queueUrlForImageView(String url, ImageView imageView, Preferences preferences) {
		
		if (url == null || url.trim().equals("")) {
            return;
        }
		
		resetPurgeTimer();
		Bitmap bitmap = getBitmapFromCache(url, preferences);
		
		if (bitmap == null && cancelPotentialDownload(url, imageView)) {
			File f = mFileCacheMgr.getFile(String.format("%s%d", preferences.relativePath, url.hashCode()));
			if (f.exists()) {
				imageView.setImageDrawable(Drawable.createFromPath(f.getAbsolutePath()));
				return;
			}
			
        	BitmapDownloaderTask task = new BitmapDownloaderTask(imageView, preferences);
        	
        	DownloadedDrawable downloadedDrawable = null;
        	try {
				Integer placeHolderResource = (Integer) imageView.getTag();
				if (placeHolderResource != null) {
					downloadedDrawable = new DownloadedDrawable(task, placeHolderResource);
				}
			} catch (ClassCastException e) {
				downloadedDrawable = new DownloadedDrawable(task);
			}
            
            try {
            	task.execute(url);
            	if (downloadedDrawable != null) {
            		imageView.setImageDrawable(downloadedDrawable);
            	}
            } catch (RejectedExecutionException e) {
            	Integer placeHolderResource = (Integer) imageView.getTag();
            	if (placeHolderResource != null) {
            		imageView.setImageResource(placeHolderResource);
            	}
            }
        } else {
            cancelPotentialDownload(url, imageView);
            if (bitmap != null) {
            	imageView.setImageBitmap(bitmap);
            }
        }
	}
	
	/**
	 * Cancel old task assigned to the given image view
	 * @param url the url of the file to download
	 * @param imageView the imageview
	 * @return true if no task assigned to image view or old task was canceled, false if the same url is already being downloaded. 
	 */
	private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }
	
	/**
	 * Try to get image from cache. Either from soft cache or hard cache (SD-card) 
	 * @param url the image url
	 * @param preferences image preferences
	 * @return
	 */
	public Bitmap getBitmapFromCache(String url, Preferences preferences) {

        // Try the soft reference cache first
        SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
        if (bitmapReference != null) {
            final Bitmap bitmap = bitmapReference.get();
            if (bitmap != null) {
                // Bitmap found in soft cache
                return bitmap;
            } else {
                // Soft reference has been Garbage Collected, remove reference
                sSoftBitmapCache.remove(url);
            }
        }
        
        // If not found in the soft reference, try the file system cache
        if (!preferences.relativePath.equals("")) {
			File f = new File(mFileCacheMgr.getFile(""), preferences.relativePath);
			if (!f.exists()) {
				f.mkdirs();
			}
		}

        // Image not found in cache
        return null;
	}
	
	/**
	 * Asynchronous task that downloads the image.
	 */
	private class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private String url;
        private final WeakReference<ImageView> imageViewReference;
        private final Preferences mPreferences;

        public BitmapDownloaderTask(ImageView imageView, Preferences preferences) {
            imageViewReference = new WeakReference<ImageView>(imageView);
            mPreferences = preferences;
        }

        /**
         * Actual download method.
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            
            final DefaultHttpClient httpClient = new DefaultHttpClient();
            final HttpGet request = new HttpGet(url);
            Bitmap bmp = null;
            
			try {
		        HttpResponse response = httpClient.execute(request);
		        
		        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
		        	final HttpEntity entity = response.getEntity();
		        	
		        	if (entity != null) {
			        	InputStream inputStream = null;
			        	
			        	try {
		                    inputStream = entity.getContent();
		                    
		                    if (mPreferences.roundedCorners) {
					        	bmp = getRoundedCornerBitmap(BitmapFactory.decodeStream(new FlushedInputStream(inputStream)));
					        }
					        else {
					        	bmp = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
					        }
		                    
		                } finally {
		                    if (inputStream != null) inputStream.close();
		                    entity.consumeContent();
		                }
		        	}
		        }
		        else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
		        	for (ImageLoaderListener listener : mImageLoaderListeners) {
		        		listener.imageNotFound(url, imageViewReference.get());
		        	}
		        }
				
			} catch (IOException e) {
				request.abort();
				Log.e(TAG, "I/O error while retrieving bitmap from " + url);
			} catch (IllegalStateException e) {
				request.abort();
	            Log.w(TAG, "Incorrect URL: " + url, e);
	        }
			
			if (bmp == null) {
				for (ImageLoaderListener listener : mImageLoaderListeners) {
	        		listener.imageNotFound(url, imageViewReference.get());
	        	}
			}
			
			addBitmapToCache(url, bmp, mPreferences);
            
            return bmp;
        }

        /**
         * Once the image is downloaded, set it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
            	bitmap.recycle();
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {
                ImageView imageView = imageViewReference.get();
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                // Change bitmap only if this process is still associated with it
                if ((this == bitmapDownloaderTask)) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
        
        /**
         * Generates an image with rounded corners
         * @param bitmap the original bitmap
         * @return a new bitmap with rounded corners
         */
        private Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        	if (bitmap == null) return null;
        	
			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
					bitmap.getHeight(), Config.ARGB_8888);
			
			Canvas canvas = new Canvas(output);
			final int color = 0xff424242;
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()); 
			final RectF rectF = new RectF(rect);
			final float roundPx = 12;
			
			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			canvas.drawBitmap(bitmap, rect, rect, paint);
			
			bitmap.recycle();
			
			return output;
		}
    }
	
	public static class Preferences {
		
		public boolean roundedCorners;
		public String relativePath;
		
		public Preferences() {
			roundedCorners = false;
			relativePath = "";
		}
	}
	
	private static class DownloadedDrawable extends BitmapDrawable {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
            super(mResources);
            bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }
        
        public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask, int placeholderResourceId) {
            super(mResources.openRawResource(placeholderResourceId));
            bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }
	
	private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }
	
	/**
	 * Adds an image to the cache. Both the soft cache and the hard cache (SD-card)
	 * @param url the image url
	 * @param bitmap the generated bitmap
	 * @param preferences image preferences
	 */
	private void addBitmapToCache(String url, Bitmap bitmap, Preferences preferences) {
        if (bitmap != null) {
            synchronized (sSoftBitmapCache) {
            	sSoftBitmapCache.put(url, new SoftReference<Bitmap>(bitmap));
            }
            
            bitmap.compress(CompressFormat.PNG, 90, mFileCacheMgr.getFileOutputStream(String.format("%s%d", preferences.relativePath, url.hashCode())));
        }
    }
	
	/**
	 * Clear the soft cache
	 */
	public void clearCache() {
        sSoftBitmapCache.clear();
    }
	
	/**
	 * Reset purger
	 */
	private void resetPurgeTimer() {
        purgeHandler.removeCallbacks(purger);
        purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
    }
}
