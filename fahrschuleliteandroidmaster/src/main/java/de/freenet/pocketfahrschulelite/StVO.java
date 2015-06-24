package de.freenet.pocketfahrschulelite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import de.freenet.pocketfahrschulelite.pdfview.PDF;
import de.freenet.pocketfahrschulelite.pdfview.PDFPagesProvider;
import de.freenet.pocketfahrschulelite.pdfview.PagesView;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StVO extends PocketFahrschuleActivity implements OnClickListener {
	
	private static final String TAG = "StVO";
	
	private PagesView mPagesView;
	private TextView mPageNumberTextView;
	private PDFPagesProvider mPDFPagesProvider;
	
	// Zoom buttons, layout and fade animation
	private ImageButton zoomDownButton;
	private ImageButton zoomUpButton;
	private Animation mFadeOutAnimation;
	private Animation mFastFadeInAnimation;
	private LinearLayout mZoomLinearLayout;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        PDF pdf = null;
		try {
	    	File file = new File(this.getCacheDir(), "stvo.pdf");
			if (!file.exists()) {
				file.createNewFile();
				
				InputStream is = getAssets().open("stvo.pdf");
		    	FileOutputStream fos = new FileOutputStream(file);
		 
		    	// Transfer bytes from the inputfile to the outputfile
		    	byte[] buffer = new byte[1024];
		    	int length;
		    	while ((length = is.read(buffer)) > 0) {
		    		fos.write(buffer, 0, length);
		    	}
		 
		    	//Close the streams
		    	fos.flush();
		    	fos.close();
		    	is.close();
			}
			
			pdf = new PDF(file, 2);
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		}
		
		if (pdf == null) {
			 finish();
		}
		else {
			final float scale = getResources().getDisplayMetrics().density;
			
			RelativeLayout rl = new RelativeLayout(this);
	        
        	mPDFPagesProvider = new PDFPagesProvider(this, pdf, false, false, true);
        	
        	mPagesView = new PagesView(this);
        	mPagesView.setPagesProvider(mPDFPagesProvider);
        	rl.addView(mPagesView);
        	mPagesView.goToPage(FahrschulePreferences.getInstance().getStVOBookmarkedPage());
			
        	// Add TextView to display the current visible page
        	mPageNumberTextView = new TextView(this);
	        mPageNumberTextView.setTextSize(16.0f);
	        mPageNumberTextView.setBackgroundResource(R.drawable.page_number_background);
	        mPageNumberTextView.setPadding((int)(6 * scale), (int)(2 * scale), (int)(6 * scale), (int)(2 * scale));
	        mPageNumberTextView.setTextAppearance(this, android.R.style.TextAppearance_Large);
	        
	        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
	        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	        lp.setMargins(0, (int)(6 * scale), (int)(6 * scale), 0);
	        rl.addView(mPageNumberTextView, lp);
        	
	        setZoomLayout(rl);
	        
	        setContentView(rl);
	        
	        setTitle(R.string.stvo);
	        
	        mFastFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fast_fade_in);
	        mFadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		}
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	showPageNumber(true);
    	
    	mPagesView.setPageWithVolume(true);
    	mPagesView.setVerticalScrollLock(false);
    	mPagesView.setZoomIncrement(1.414f);
    	mPagesView.setSideMargins(false);
    	mPagesView.goToPage(FahrschulePreferences.getInstance().getStVOBookmarkedPage());
    	
    	mFadeOutAnimation.setFillAfter(true);
    	mZoomLinearLayout.startAnimation(mFadeOutAnimation);
    	mPageNumberTextView.startAnimation(mFadeOutAnimation);
    	
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	FahrschulePreferences.getInstance().setStVOBookmarkedPage(mPagesView.getCurrentPage());
    }
    
    private void setZoomLayout(ViewGroup viewGroup) {
    	final float scale = getResources().getDisplayMetrics().density;
    	
    	mZoomLinearLayout = new LinearLayout(this);
    	mZoomLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
    	mZoomLinearLayout.setGravity(Gravity.RIGHT);
        
		zoomDownButton = new ImageButton(this);
		zoomDownButton.setId(android.R.id.button1);
		zoomDownButton.setImageResource(android.R.drawable.btn_minus);
		zoomDownButton.setBackgroundColor(Color.TRANSPARENT);
		zoomDownButton.setOnClickListener(this);
		mZoomLinearLayout.addView(zoomDownButton, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		
		zoomUpButton = new ImageButton(this);
		zoomUpButton.setId(android.R.id.button2);
		zoomUpButton.setImageResource(android.R.drawable.btn_plus);
		zoomUpButton.setBackgroundColor(Color.TRANSPARENT);
		zoomUpButton.setOnClickListener(this);
		
		
		LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		linearParams.setMargins(0, 0, (int)(-27 * scale), 0);
		mZoomLinearLayout.addView(zoomUpButton, linearParams);
		
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lp.setMargins(0, 0, 0, (int)(8 * scale));
		
		viewGroup.addView(mZoomLinearLayout, lp);
    }
    
    public boolean dispatchTouchEvent(MotionEvent event) {
    	
    	if (!mFastFadeInAnimation.hasStarted() && mFadeOutAnimation.hasEnded() || 
    			mFastFadeInAnimation.hasStarted() && mFastFadeInAnimation.hasEnded() && mFadeOutAnimation.hasStarted() && mFadeOutAnimation.hasEnded()) {
    		mFastFadeInAnimation.setFillAfter(true);
    		mFastFadeInAnimation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation animation) {
					mZoomLinearLayout.clearAnimation();
					mPageNumberTextView.clearAnimation();
					
					mFadeOutAnimation.setFillAfter(true);
			    	mZoomLinearLayout.startAnimation(mFadeOutAnimation);
			    	mPageNumberTextView.startAnimation(mFadeOutAnimation);
				}

				@Override
				public void onAnimationRepeat(Animation animation) { }

				@Override
				public void onAnimationStart(Animation animation) { }
    			
    		});
    		
    		mZoomLinearLayout.clearAnimation();
			mPageNumberTextView.clearAnimation();
    		
    		mZoomLinearLayout.startAnimation(mFastFadeInAnimation);
	    	mPageNumberTextView.startAnimation(mFastFadeInAnimation);
    	}
    	else if (mFadeOutAnimation.hasStarted() && !mFadeOutAnimation.hasEnded()) {
    		mPageNumberTextView.clearAnimation();
        	mPageNumberTextView.setVisibility(View.VISIBLE);
        	
        	mZoomLinearLayout.clearAnimation();
        	mZoomLinearLayout.setVisibility(View.VISIBLE);
        	
        	mFadeOutAnimation.setFillAfter(true);
	    	mZoomLinearLayout.startAnimation(mFadeOutAnimation);
	    	mPageNumberTextView.startAnimation(mFadeOutAnimation);
    	}
    	
    	return super.dispatchTouchEvent(event);
    }
    
    public void showPageNumber(boolean force) {
    	String newText = String.format("%d / %d", mPagesView.getCurrentPage() + 1, mPDFPagesProvider.getPageCount());
    	
    	if (!force && newText.equals(mPageNumberTextView.getText()))
    		return;
    	
    	mPageNumberTextView.setText(newText);
    }
    
    public void showFindDialog() { }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case android.R.id.button1:
				mPagesView.zoomDown();
				break;
			case android.R.id.button2:
				mPagesView.zoomUp();
				break;
		}
	}
}
