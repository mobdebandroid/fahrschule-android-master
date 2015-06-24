package de.freenet.pocketfahrschulelite.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Gallery;

public class CustomGallery extends Gallery {
	
	private OnFlingListener mOnFlingListener;
	private InputMethodManager mgr;

    /** 
     * The distance the user has to move their finger, in density independent
     * pixels, before we count the motion as A) intended for the ScrollView if
     * the motion is in the vertical direction or B) intended for ourselves, if
     * the motion is in the horizontal direction - after the user has moved this
     * amount they are "locked" into this direction until the next ACTION_DOWN
     * event
     */
    private static final int DRAG_BOUNDS_IN_DP = 10;

    /**
     * A value representing the "unlocked" state - we test all MotionEvents
     * when in this state to see whether a lock should be make
     */
    private static final int SCROLL_LOCK_NONE = 0;

    /**
     * A value representing a lock in the vertical direction - once in this state
     * we will never redirect MotionEvents from the ScrollView to ourself
     */
    private static final int SCROLL_LOCK_VERTICAL = 1;

    /**
     * A value representing a lock in the horizontal direction - once in this
     * state we will not deliver any more MotionEvents to the ScrollView, and
     * will deliver them to ourselves instead.
     */
    private static final int SCROLL_LOCK_HORIZONTAL = 2;

    /**
     * The drag bounds in density independent pixels converted to actual pixels
     */
    private int mDragBoundsInPx = 0;

    /**
     * The coordinates of the intercepted ACTION_DOWN event
     */
    private float mTouchStartX;

    /**
     * The current scroll lock state
     */
    private int mScrollLock = SCROLL_LOCK_NONE;

    public CustomGallery(Context context) {
        super(context);
        initCustomGallery(context);
    }

    public CustomGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCustomGallery(context);
    }

    public CustomGallery(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initCustomGallery(context);
    }

    private void initCustomGallery(Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        mDragBoundsInPx = (int) (scale * DRAG_BOUNDS_IN_DP + 0.5f);
        mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }
    
    public void setOnFlingListener(OnFlingListener listener) {
    	mOnFlingListener = listener;
    }    
    
    @Override
    public void onLongPress(MotionEvent event) { }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	boolean handled = false;
        for (View v : getTouchables()) {
        	if (v instanceof EditText) {
        		int[] location = new int[2];
        		v.getLocationOnScreen(location);
        		Rect r = new Rect(location[0], location[1], location[0] + v.getWidth(), location[1] + v.getHeight());
        		if (r.contains((int)event.getRawX(), (int)event.getRawY()) && v.isFocusable()) {
        			v.requestFocus();
        			handled = mgr.showSoftInput(v, 0);
        		}
        		else {
        			v.clearFocus();
        		}
        	}
        }

        if (!handled) {
            handled = super.onTouchEvent(event);
        }
        
    	return handled;
    }
    
    /**
     * This will be called before the intercepted views onTouchEvent is called
     * Return false to keep intercepting and passing the event on to the target view
     * Return true and the target view will receive ACTION_CANCEL, and the rest of the
     * events will be delivered to our onTouchEvent
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action) {
	        case MotionEvent.ACTION_DOWN:
	            mTouchStartX = ev.getX();
	            mScrollLock = SCROLL_LOCK_NONE;
	
	            /**
	             * Deliver the down event to the Gallery to avoid jerky scrolling
	             * if we decide to redirect the ScrollView events to ourself
	             */
	            onTouchEvent(ev);
	            break;
	            
	        case MotionEvent.ACTION_MOVE:
	            if (mScrollLock == SCROLL_LOCK_VERTICAL) {
	                // keep returning false to pass the events
	                // onto the ScrollView
	                return false;
	            }
	            
	            final float touchDistanceX = (ev.getX() - mTouchStartX);
	
	            if (Math.abs(touchDistanceX) > mDragBoundsInPx) {
	                mScrollLock = SCROLL_LOCK_HORIZONTAL; // gallery action
	                return true; // redirect MotionEvents to ourself
	            }
	            
	            return super.onInterceptTouchEvent(ev);
	
	        case MotionEvent.ACTION_CANCEL:
	        case MotionEvent.ACTION_UP:
	        	/**
	        	 * if we're still intercepting at this stage, make sure the gallery
	        	 * also receives the up/cancel event as we gave it the down event earlier
	        	 */
	        	return super.onInterceptTouchEvent(ev);
	        default:
	        	onTouchEvent(ev);
	        	break;
        }
        
        return false;
    }
    
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    	if (velocityX > 0) {
    		if (mOnFlingListener != null) {
        		if (mOnFlingListener.didFling(-1, getSelectedItemPosition())) return true;
        	}
    		
            return onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null);
        } else {
        	
        	if (mOnFlingListener != null) {
        		if (mOnFlingListener.didFling(1, getSelectedItemPosition())) return true;
        	}
        	
            return onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
        }
    }
    
    @Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    	if (mOnFlingListener != null && !mOnFlingListener.isAllowedToFling(distanceX > 0 ? 1 : -1)) return true;
    	
		return super.onScroll(e1, e2, distanceX, distanceY);
	}
    
    public static interface OnFlingListener {
    	
    	/**
    	 * Callback to inform the controller that the user is switching question
    	 * @param direction direction to fling, 1 for right and -1 for left
    	 * @return true if the event was consumed by the callback, false otherwise
    	 */
    	public boolean didFling(int direction, int position);
    	
    	/**
    	 * The gallery asks if it is allowed to fling.
    	 * @param direction direction to fling, 1 for right and -1 for left
    	 * @return true if the gallery should scroll, false otherwise
    	 */
    	public boolean isAllowedToFling(int direction);
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = false;
        
        if (getFocusedChild() != null) {
            handled = getFocusedChild().dispatchKeyEvent(event);
        }

        if (!handled) {
            handled = event.dispatch(this, null, null);
        }

        return handled;
    }
}
