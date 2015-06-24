package de.freenet.remoteresources;

public class RemoteResourceException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public RemoteResourceException(String errorMessage) {
		super(errorMessage);
	}
}
