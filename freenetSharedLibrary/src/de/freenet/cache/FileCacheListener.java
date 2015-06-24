package de.freenet.cache;

public interface FileCacheListener {

	/**
	 * Passes information about the progress of the current download.
	 * @param byteTotalRead Total amount of bytes read up until this point
	 */
	public void downloadProgressUpdated(int byteTotalRead);
}
