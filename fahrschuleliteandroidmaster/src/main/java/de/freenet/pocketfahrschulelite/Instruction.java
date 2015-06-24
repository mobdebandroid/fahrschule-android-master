package de.freenet.pocketfahrschulelite;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import de.freenet.pocketfahrschulelite.adapters.InstructionAdapter;
import de.freenet.pocketfahrschulelite.classes.Utils;
import de.freenet.pocketfahrschulelite.objects.HelpScreenModel;
import de.freenet.pocketfahrschulelite.widget.CustomGallery;
import de.freenet.pocketfahrschulelite.MainNavigation;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageButton;

public class Instruction extends Activity implements OnItemSelectedListener, AnimationListener {
	
	private static final String TAG = "Instruction";
	
	public static final String EXTRA_IS_WELCOME_SCREEN = "Extra_Instruction_IsWelcomeScreen";
	
	private CustomGallery mGallery;
	private ImageButton mLeftArrow;
	private ImageButton mRightArrow;
	private boolean mLeftArrowIsHidden = true;
	private boolean mRightArrowIsHidden = false;
	private boolean mIsWelcomeScreen = false;
	private AlphaAnimation mFadeInAnimation;
	private AlphaAnimation mFadeOutAnimation;
	private AlphaAnimation mDelayedFadeOutAnimation;
	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.instruction);
        
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(EXTRA_IS_WELCOME_SCREEN)) {
        	mIsWelcomeScreen = getIntent().getExtras().getBoolean(EXTRA_IS_WELCOME_SCREEN, false);
        }
        
        mGallery = (CustomGallery) findViewById(R.id.gallery1);
        
        int[] resourceIds = { R.drawable.helpscreen01, R.drawable.helpscreen02, R.drawable.helpscreen03, R.drawable.helpscreen04,
        		R.drawable.helpscreen05, R.drawable.helpscreen07 };
        
        ArrayList<HelpScreenModel> screens = new ArrayList<HelpScreenModel>();
        
        if (mIsWelcomeScreen) {
        	HelpScreenModel model = new HelpScreenModel();
        	model.helpScreenResourceId = R.layout.welcome_screen;
        	model.isWelcomeScreen = true;
        	screens.add(model);
        }
        
		try {
			JSONArray array = new JSONArray(Utils.getContentFromFile(getAssets().open("instruction.json")));
	        for (int i = 0;i < resourceIds.length;i++) {
	        	screens.add(new HelpScreenModel(resourceIds[i], array.optJSONArray(i)));
	        }
		} catch (JSONException e) {
			Log.e(TAG, "JSONException: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		}
		
        mGallery.setAdapter(new InstructionAdapter(this, screens));
        mGallery.setOnItemSelectedListener(this);
        
        mLeftArrow = (ImageButton) findViewById(R.id.imageButton1);
        mRightArrow = (ImageButton) findViewById(R.id.imageButton2);
        
        mFadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
        mFadeInAnimation.setDuration(250);
        mFadeInAnimation.setFillAfter(true);
        mFadeInAnimation.setAnimationListener(this);
        mFadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        mFadeOutAnimation.setDuration(250);
        mFadeOutAnimation.setFillAfter(true);
        mFadeOutAnimation.setAnimationListener(this);
        mDelayedFadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        mDelayedFadeOutAnimation.setDuration(250);
        mDelayedFadeOutAnimation.setFillAfter(true);
        mDelayedFadeOutAnimation.setStartOffset(5000);
        mDelayedFadeOutAnimation.setAnimationListener(this);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK && mIsWelcomeScreen) {
			Intent i = new Intent(this, MainNavigation.class);
			startActivity(i);
			finish();
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	public void closeInstruction(View v) {
		if (mIsWelcomeScreen) {
			Intent i = new Intent(this, MainNavigation.class);
			startActivity(i);
		}
		finish();
	}
	
	/**
     * OnClick method which is called when the user click on one of the two arrows.
     * @param v the Button clicked
     */
    public void changeHelpScreen(View v) {
    	
    	switch (v.getId()) {
	    	case R.id.imageButton1:
	    		if (mGallery.getSelectedItemPosition() == 0) {
	    			return;
	    		}
	    		mGallery.onScroll(null, null, -50.0f, 0.0f);
				mGallery.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null);
	    		break;
	    	case R.id.imageButton2:
	    		if (mGallery.getSelectedItemPosition() == mGallery.getCount() - 1) {
	    			return;
	    		}
	    		mGallery.onScroll(null, null, 50.0f, 0.0f);
				mGallery.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
	    		break;
    	}
    }

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		
		((InstructionAdapter) mGallery.getAdapter()).clearIndicatorTextView();
		
		if (arg2 == 0 && mLeftArrowIsHidden) {
			mLeftArrow.clearAnimation();
			mLeftArrow.setVisibility(View.INVISIBLE);
		}
		else if (arg2 == 0 && !mLeftArrowIsHidden) {
			mLeftArrowIsHidden = true;
			mLeftArrow.startAnimation(mFadeOutAnimation);
		}
		else if (!mLeftArrowIsHidden) {
			mLeftArrowIsHidden = false;
			mDelayedFadeOutAnimation.reset();
			mLeftArrow.startAnimation(mDelayedFadeOutAnimation);
		}
		else {
			mLeftArrowIsHidden = false;
			mLeftArrow.startAnimation(mFadeInAnimation);
		}
		
		if (arg2 == mGallery.getCount() - 1 && mRightArrowIsHidden) {
			mRightArrow.clearAnimation();
			mRightArrow.setVisibility(View.INVISIBLE);
		}
		else if (arg2 == mGallery.getCount() - 1 && !mRightArrowIsHidden) {
			mRightArrowIsHidden = true;
			mRightArrow.startAnimation(mFadeOutAnimation);
		}
		else if (!mRightArrowIsHidden) {
			mDelayedFadeOutAnimation.reset();
			mRightArrow.startAnimation(mDelayedFadeOutAnimation);
		}
		else {
			mRightArrowIsHidden = false;
			mRightArrow.startAnimation(mFadeInAnimation);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) { }

	@Override
	public void onAnimationEnd(Animation animation) {
		if (animation.getStartOffset() == 5000 && animation.hasEnded()) {
			mRightArrowIsHidden = true;
			mLeftArrowIsHidden = true;
		}
		else {
			if (!mLeftArrowIsHidden) {
				mLeftArrow.clearAnimation();
				mDelayedFadeOutAnimation.reset();
				mLeftArrow.startAnimation(mDelayedFadeOutAnimation);
			}
			
			if (!mRightArrowIsHidden) {
				mRightArrow.clearAnimation();
				mDelayedFadeOutAnimation.reset();
				mRightArrow.startAnimation(mDelayedFadeOutAnimation);
			}
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) { }

	@Override
	public void onAnimationStart(Animation animation) { }
}