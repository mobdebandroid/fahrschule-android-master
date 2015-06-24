package de.freenet.pocketfahrschulelite.widget;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import de.freenet.pocketfahrschulelite.R;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainGroupCellView extends LinearLayout {
	
	private RelativeLayout mCell;
	private ProgressBar mProgressBar;
	private ImageView mImageView;
	private ImageView mLearningStatisticImageView;
	private TextView mTextView;
	private TextView mSectionTextView;
	private LinearLayout mWrapper;
	private float mScale;

	public MainGroupCellView(Context context) {
		super(context);
		initMainGroupCellView(context);
	}
	
	public MainGroupCellView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initMainGroupCellView(context);
	}

	private void initMainGroupCellView(Context context) {
		setOrientation(LinearLayout.VERTICAL);
		
		mCell = new RelativeLayout(context);
		mCell.setBackgroundResource(R.drawable.list_selector);

		mWrapper = new LinearLayout(context);
		mWrapper.setOrientation(LinearLayout.HORIZONTAL);
		mWrapper.setGravity(Gravity.CENTER_VERTICAL);
		
		mSectionTextView = new TextView(context);
		mSectionTextView.setTextColor(context.getResources().getColor(R.color.header_section));
		mSectionTextView.setGravity(Gravity.CENTER);
		mSectionTextView.setTypeface(null, Typeface.BOLD);
		mSectionTextView.setBackgroundColor(Color.WHITE); //setBackgroundResource(R.drawable.bg_section_header);
		addView(mSectionTextView);
		mSectionTextView.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
		
		mImageView = new ImageView(context);
		mImageView.setId(R.id.imageView1);
		mLearningStatisticImageView = new ImageView(context);
		mLearningStatisticImageView.setId(R.id.imageView4);
		mLearningStatisticImageView.setVisibility(View.GONE);
		
		mTextView = new TextView(context);
		try {
			mTextView.setTextColor(ColorStateList.createFromXml(getResources(), getResources().getXml(R.color.textcolor_black_white)));
		} catch (NotFoundException e) {
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}
		mTextView.setTextSize(14.0f);
		mTextView.setTypeface(null, Typeface.BOLD);
		mTextView.setGravity(Gravity.CENTER_VERTICAL);
		
		mProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
		try {
			mProgressBar.setProgressDrawable(Drawable.createFromXml(getResources(), getResources().getXml(R.drawable.learning_progress_bar)));
		} catch (NotFoundException e) {
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}
		mProgressBar.setId(android.R.id.progress);
		mProgressBar.setMax(100);
		
		mScale = context.getResources().getDisplayMetrics().density;
		
		LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams((int)(30 * mScale), (int)(30 * mScale));
		linearParams.rightMargin = (int)(20 * mScale);
		mWrapper.addView(mImageView, linearParams);
		
		linearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mWrapper.addView(mTextView, linearParams);
		
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int)(70 * mScale), (int)(20 * mScale));
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		layoutParams.rightMargin = (int)(10 * mScale);
		mCell.addView(mProgressBar, layoutParams);
		
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		layoutParams.rightMargin = (int)(10 * mScale);
		mCell.addView(mLearningStatisticImageView, layoutParams);
		
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.LEFT_OF, mProgressBar.getId());
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		layoutParams.leftMargin = (int)(6 * mScale);
		layoutParams.rightMargin = (int)(10 * mScale);
		mCell.addView(mWrapper, layoutParams);
		
		
		addView(mCell, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
	}
	
	public TextView getTextView() {
		return mTextView;
	}
	
	public void setText(CharSequence text) {
		if (text == null) return;
		mTextView.setText(text);
	}
	
	public void setImage(int resId) {
		if (resId == 0) return;
		mImageView.setImageResource(resId);
	}
	
	public void setLearningStatisticImage(int resId) {
		if (resId == 0) return;
		mLearningStatisticImageView.setImageResource(resId);
	}
	
	public void setImageHidden(boolean hidden) {
		mImageView.setVisibility(hidden ? View.GONE : View.VISIBLE);
	}
	
	public ImageView getImageView() {
		return mImageView;
	}
	
	public void setSectionHeaderText(String text) {
		if (text == null) return;
		mSectionTextView.setText(text);
	}
	
	public void setCellBackground(int resId) {
		mCell.setBackgroundResource(resId);
	}
	
	public void setSectionHeaderHidden(boolean hidden) {
		mSectionTextView.setVisibility(hidden ? View.GONE : View.VISIBLE);
	}
	
	public void setProgressViewHidden(boolean hidden) {
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(mTextView.getLayoutParams());
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.LEFT_OF, hidden ? mLearningStatisticImageView.getId() : mImageView.getId());
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		layoutParams.leftMargin = (int)(10 * mScale);
		layoutParams.rightMargin = (int)(20 * mScale);
		layoutParams.bottomMargin = (int)(20 * mScale);
		layoutParams.topMargin = (int)(20 * mScale);
		mWrapper.setLayoutParams(layoutParams);
		
		mProgressBar.setVisibility(hidden ? View.GONE : View.VISIBLE);
	}
	
	public void setLearningStatisticImageViewHidden(boolean hidden) {
		mLearningStatisticImageView.setVisibility(hidden ? View.GONE : View.VISIBLE);
	}
	
	public void setProgressBarProgress(int numCorrect, int numFaulty, int numTotal) {
		
		int percentCorrect = (int) Math.round(((float)numCorrect / (float)numTotal) * 100);
		int percentFaulty = (int) Math.round(((float)numFaulty / (float)numTotal) * 100) + percentCorrect;
		
		mProgressBar.setProgress(percentCorrect);
		mProgressBar.setSecondaryProgress(percentFaulty);
	}
}
