package de.freenet.view;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationManager {
	
	public enum Orientation {
		PORTRAIT,
		LANDSCAPE,
		REVERSE_PORTRAIT,
		REVERSE_LANDSCAPE,
		UNKNOWN;
		
		public boolean isLandscape() {
			return this == LANDSCAPE || this == REVERSE_LANDSCAPE;
		}
		
		public boolean isPortrait() {
			return this == PORTRAIT || this == REVERSE_PORTRAIT;
		}
	};

	private Context mContext;
	private SensorManager mSensorManager;
	private boolean mRunning;
	private OrientationListener mListener;
	
	public OrientationManager(Context ctx) {
		super();
		mContext = ctx;
		mRunning = false;
	}

	/**
	 * Starts listening for orientation changes and also registers a listener  
	 * @param listener the orientation listener
	 */
    public void startListening(OrientationListener listener) {
    	mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        if (sensors.size() > 0) {
            mRunning = mSensorManager.registerListener(mSensorEventListener, sensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
            mListener = listener;
        }
    }
    
    /**
     * Stop listening for orientation changes and unregister the orientation listener
     */
    public void stopListening() {
    	mRunning = false;
        try {
            if (mSensorManager != null && mSensorEventListener != null) {
            	mSensorManager.unregisterListener(mSensorEventListener);
            }
        } catch (Exception e) {}
    }
    
    /**
     * Indicates whether the manager is listening
     * @return true if the manager currently is listening, false otherwise
     */
    public boolean isListening() {
        return mRunning;
    }
    
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
    	
    	private Orientation mOldOrientation = Orientation.UNKNOWN;

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// Do nothing
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			
			if (mListener == null) return;
			
			final float pitch = event.values[1]; // pitch
			final float roll = event.values[2]; // roll
			
			Orientation currentOrientation = Orientation.UNKNOWN;
			
			if (pitch < -45 && pitch > -135) {
				currentOrientation = Orientation.PORTRAIT;
            } else if (pitch > 45 && pitch < 135) {
            	currentOrientation = Orientation.REVERSE_PORTRAIT;
            } else if (roll > 45) {
            	currentOrientation = Orientation.LANDSCAPE;
            } else if (roll < -45) {
            	currentOrientation = Orientation.REVERSE_LANDSCAPE;
            }
			
			if (currentOrientation != Orientation.UNKNOWN && !currentOrientation.equals(mOldOrientation)) {
				mListener.onOrientationChange(currentOrientation);
				mOldOrientation = currentOrientation;
			}
		}
    };
}
