package de.freenet.pocketfahrschulelite;

import java.io.IOException;
import java.io.InputStream;

import de.freenet.pocketfahrschulelite.R;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class QuestionImage extends Activity implements OnClickListener {
	
	public static final String EXTRA_IMAGE_FILENAME = "QuestionImage_Extra_ImageFilename";
	public static final String EXTRA_QUESTION_TEXT = "QuestionImage_Extra_QuestionText";
	public static final String EXTRA_EXIT_WITH_ORIENTATION  = "QuestionImage_Extra_ExitWithOrientation";
	public static final String EXTRA_OFFICIAL_LAYOUT  = "QuestionImage_Extra_OfficialLayout";
	
	private FrameLayout mFrameLayout;
	private boolean mExitWithOrientation = false;
	private boolean mIsOfficialLayout = false;
	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        String filename = "";
        String question = "";
        if (getIntent().getExtras() != null) {
        	filename = getIntent().getExtras().getString(EXTRA_IMAGE_FILENAME);
        	question = getIntent().getExtras().getString(EXTRA_QUESTION_TEXT);
        	mExitWithOrientation = getIntent().getExtras().getBoolean(EXTRA_EXIT_WITH_ORIENTATION, false);
        	mIsOfficialLayout = getIntent().getExtras().getBoolean(EXTRA_OFFICIAL_LAYOUT, false);
        }
        
        if (filename == null || filename.equals("")) {
        	finish();
        }
        
        if (mIsOfficialLayout) {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        
        Bitmap bmp = null;
		try {
			InputStream buffer = getResources().getAssets().open(String.format("fragenbilder/%s", filename));
			bmp = BitmapFactory.decodeStream(buffer);
		} catch (IOException e) {
		}
		
		if (bmp != null) {
			ImageView iv = new ImageView(this);
			iv.setImageBitmap(bmp);
			iv.setBackgroundColor(Color.TRANSPARENT);
			iv.setOnClickListener(this);
        	setContentView(iv);
        	
        	if (question != null && !question.equals("")) {
        		final float scale = getResources().getDisplayMetrics().density;
        		
        		TextView tv = new TextView(this);
            	tv.setText(question);
            	tv.setPadding((int) (scale * 20), (int) (scale * 10), (int) (scale * 20), (int) (scale * 10));
            	tv.setGravity(Gravity.CENTER_HORIZONTAL);
            	tv.setTextColor(Color.WHITE);
            	tv.setBackgroundColor(Color.parseColor("#aa2c3342"));
            	addContentView(tv, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        	}
        	
        	mFrameLayout = (FrameLayout) iv.getParent();
        	if (mIsOfficialLayout) {
        		mFrameLayout.setBackgroundResource(R.color.official_exam_light_green);
        	}
        	else {
        		mFrameLayout.setBackgroundResource(R.drawable.bg_mit_gitter);
        	}
		}
		else {
			finish();
		}
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			finish();
			overridePendingTransition(R.anim.no_sliding, R.anim.zoom_out);
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		finish();
		overridePendingTransition(R.anim.no_sliding, R.anim.zoom_out);
	}
	
	@Override
	public void onConfigurationChanged (Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT && mExitWithOrientation) {
			finish();
		}
		else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			mFrameLayout.setBackgroundResource(R.drawable.bg_mit_gitter);
		}
		else {
			mFrameLayout.setBackgroundResource(R.drawable.bg_gitter_landscape);
		}
	}
}
