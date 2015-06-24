package de.freenet.remoteresources;

import java.net.URL;

public interface RemoteResourceInterface {
	
	/**
	 * Function is called when an update of the remote resource is completed
	 */
	public void updateCheckSucceded(URL url, String filename);
	
	/**
	 * Function is called when an update of the remote resource is unsuccessful
	 */
	public void updateCheckDidFail(URL url, String filename);
}
