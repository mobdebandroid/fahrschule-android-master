package de.freenet.imageloader;

import android.widget.ImageView;

public interface ImageLoaderListener {

	/**
	 * An image was not found at the given URL. 
	 */
	public void imageNotFound(String url, ImageView imageView);
}
