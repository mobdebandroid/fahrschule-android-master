package de.freenet.advertisement;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

public class AdWebView extends WebView {
	
	private Advertisement mAdvertisement;

	public AdWebView(Context context, Advertisement ad) {
		super(context);
		initAdWebView();
		mAdvertisement = ad;
	}

	public AdWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAdWebView();
	}
	
	public AdWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initAdWebView();
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	public void initAdWebView() {
		getSettings().setJavaScriptEnabled(true);
	}
	
	public final Advertisement getAdvertisement() {
		return mAdvertisement;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev)  {
		
		switch (ev.getAction())
	    {
	    case MotionEvent.ACTION_DOWN:
	    	ev.getEventTime();
	        break;
	    case MotionEvent.ACTION_CANCEL:
	    case MotionEvent.ACTION_UP:
            try {
                Field fmNumSamples = ev.getClass().getDeclaredField("mNumSamples");
                fmNumSamples.setAccessible(true);
                Field fmTimeSamples = ev.getClass().getDeclaredField("mTimeSamples");
                fmTimeSamples.setAccessible(true);
                long newTimeSamples[] = new long[fmNumSamples.getInt(ev)];
                newTimeSamples[0] = ev.getEventTime() + 250;
                fmTimeSamples.set(ev, newTimeSamples);
            } catch (Exception e) { }
	        break;
	    }
	    return super.onTouchEvent(ev);
	}
	
	@Override
	public void scrollTo(int x, int y) { }
	
	@Override
	public void scrollBy(int x, int y) { }
	
	@Override
	public void flingScroll(int vx, int vy)	{ }
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) { }
}
