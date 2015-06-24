package de.freenet.view;

import de.freenet.view.OrientationManager.Orientation;

public interface OrientationListener {
	
	/**
	 * Called if the device changed it's orientation
	 */
	public void onOrientationChange(Orientation orientation);
}
