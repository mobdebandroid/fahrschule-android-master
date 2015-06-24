package de.freenet.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

public class ScrollViewGallery extends Gallery {

	private OnFlingListener mOnFlingListener;

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
    private float mTouchStartY;
    
    /**
     * The allowed scrolling directions
     */
    private boolean mAllowHorizontalScrolling = true;
    private boolean mAllowVerticalScrolling = true;

    /**
     * The current scroll lock state
     */
    private int mScrollLock = SCROLL_LOCK_NONE;

    public ScrollViewGallery(Context context) {
        super(context);
        initCustomGallery(context);
    }

    public ScrollViewGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCustomGallery(context);
    }

    public ScrollViewGallery(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        initCustomGallery(context);
    }

    private void initCustomGallery(Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        mDragBoundsInPx = (int) (scale * DRAG_BOUNDS_IN_DP + 0.5f);
    }
    
    public void setOnFlingListener(OnFlingListener listener) {
    	mOnFlingListener = listener;
    }
    
    public void setAllowHorizontalScrolling(boolean allow) {
    	mAllowHorizontalScrolling = allow;
    }
    
    public void setAllowVerticalScrolling(boolean allow) {
    	mAllowVerticalScrolling = allow;
    }

    /**
     * This will be called before the intercepted views onTouchEvent is called
     * Return false to keep intercepting and passing the event on to the target view
     * Return true and the target view will recieve ACTION_CANCEL, and the rest of the
     * events will be delivered to our onTouchEvent
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action) {
	        case MotionEvent.ACTION_DOWN:
	            mTouchStartX = ev.getX();
	            mTouchStartY = ev.getY();
	            mScrollLock = SCROLL_LOCK_NONE;
	
	            /**
	             * Deliver the down event to the Gallery to avoid jerky scrolling
	             * if we decide to redirect the ScrollView events to ourself
	             */
	            super.onTouchEvent(ev);
	            break;
	            
	        case MotionEvent.ACTION_MOVE:
	            if (mScrollLock == SCROLL_LOCK_VERTICAL) {
	            	/**
	            	 * keep returning false to pass the events
	            	 * onto the ScrollView
	            	 */
	                return false;
	            }
	
	            final float touchDistanceX = (ev.getX() - mTouchStartX);
	            final float touchDistanceY = (ev.getY() - mTouchStartY);
	
	            if (Math.abs(touchDistanceY) > mDragBoundsInPx && mAllowVerticalScrolling) {
	                mScrollLock = SCROLL_LOCK_VERTICAL;
	                return false;
	            }
	            if (Math.abs(touchDistanceX) > mDragBoundsInPx && mAllowHorizontalScrolling) {
	                mScrollLock = SCROLL_LOCK_HORIZONTAL; // gallery action
	                return true; // redirect MotionEvents to ourself
	            }
	            
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
    public void onLongPress(MotionEvent event) { }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	return super.onTouchEvent(event);
    }
    
    @Override
    public boolean onFling (MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    	
    	if (!mAllowHorizontalScrolling) return true;
    	
    	if (mOnFlingListener != null) mOnFlingListener.didFling();
    	
    	if (velocityX > 0) {
            return onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null);
        } else {
            return onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
        }
    }
    
    @Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return super.onScroll(e1, e2, distanceX, distanceY);
	}
    
    /**
     * Delegate methods for fling events
     */
    public static interface OnFlingListener {
    	
    	/**
    	 * Notify the delegate that the Gallery's onFling method has been executed 
    	 */
    	public void didFling();
    	
    }
}
