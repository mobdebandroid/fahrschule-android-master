package de.freenet.pocketfahrschulelite.adapters;

import java.util.List;

import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.objects.HelpScreenModel;
import de.freenet.pocketfahrschulelite.objects.HelpScreenModel.HelpIndicator;
import de.freenet.pocketfahrschulelite.widget.InstructionCellView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class InstructionAdapter extends FahrschuleAdapter<HelpScreenModel> {

	private float mScale;
	private AlphaAnimation mFadeInAnimation;
	private AlphaAnimation mFadeOutAnimation;
	private TextView mIndicatorTextView;
	
	public InstructionAdapter(Context context, List<HelpScreenModel> objects) {
		super(context);
		clearAndSetObject(objects);
		mScale = context.getResources().getDisplayMetrics().widthPixels / 320.0f;
		
		mFadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
        mFadeInAnimation.setDuration(250);
        mFadeInAnimation.setFillAfter(true);
        
        mFadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        mFadeOutAnimation.setDuration(250);
        mFadeOutAnimation.setFillAfter(true);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		InstructionCellView instructionCellView = null;
		try {
			instructionCellView = (InstructionCellView) convertView;
		} catch (ClassCastException e) { }
		
		if (instructionCellView == null) {
			instructionCellView = new InstructionCellView(mContext);
		}
		
		final HelpScreenModel object = getItem(position);
		if (object != null) {
			if (object.isWelcomeScreen) {
				instructionCellView.findViewById(R.id.relativeLayout1).setVisibility(View.GONE);
				instructionCellView.findViewById(R.id.welcomeScreenRelativeLayout).setVisibility(View.VISIBLE);
			}
			else {
				instructionCellView.findViewById(R.id.relativeLayout1).setVisibility(View.VISIBLE);
				instructionCellView.findViewById(R.id.welcomeScreenRelativeLayout).setVisibility(View.GONE);
				
				instructionCellView.getImageView().setImageResource(object.helpScreenResourceId);
				
				// Hack to force overlay to set alpha level to 0.6. Needed for other fade animations later.
				AlphaAnimation alphaAnim = new AlphaAnimation(0.6f, 0.6f);
				alphaAnim.setFillAfter(true);
				instructionCellView.getOverlayView().startAnimation(alphaAnim);
				
				for (final HelpIndicator indicator : object.helpIndicators) {
					
					final InstructionCellView view = instructionCellView;
					final TextView indicatorTextView = getIndicatorTextView(indicator);
					
					// An indicator, ?, is clicked
					indicatorTextView.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(final View v) {
							
							// Revert background to black. At this moment we don't want any holes!
							view.getOverlayView().setBackgroundColor(Color.BLACK);
							
							if (v.equals(mIndicatorTextView)) { // If a selected indicator is clicked again, meaning that view should be "closed" 
								AlphaAnimation alphaAnim = new AlphaAnimation(0.8f, 0.6f);
								alphaAnim.setDuration(250);
								alphaAnim.setFillAfter(true);
								view.getOverlayView().startAnimation(alphaAnim);
								
								scaleDownIndicatorView(null);
								
								view.getInstructionTextView().startAnimation(mFadeOutAnimation);
							}
							else {
								
								// An other indicator view is clicked. We should "close" the old one and open the new window.
								if (mIndicatorTextView != null) {
									scaleDownIndicatorView(v);
								}
								else { // An indicator is clicked without any other being selected. Animate alpha value of overlay view. It will be darker.
									mIndicatorTextView = (TextView) v;
									
									AlphaAnimation alphaAnim = new AlphaAnimation(0.6f, 0.8f);
									alphaAnim.setDuration(250);
									alphaAnim.setFillAfter(true);
									view.getOverlayView().startAnimation(alphaAnim);
								}
								
								view.getInstructionTextView().setText(indicator.text);
								view.getInstructionTextView().setMaxWidth((int)(280 * mScale));
								view.getInstructionTextView().invalidate();
								view.getInstructionTextView().startAnimation(mFadeInAnimation);
								
								// Set layout parameters for description text dialog box.
								RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(view.getInstructionTextView().getLayoutParams());
								int top = (int)((indicator.y + 60) * mScale);
								int textHeight = (int) (Math.ceil(view.getInstructionTextView().getPaint().measureText(indicator.text) / (264 * mScale)) * 35 * mScale);
								
								if (top + textHeight > view.getHeight()) {
									top = (int)((indicator.y - 2.0f) * mScale - textHeight);
								}
								
								params.setMargins(0, top, 0, 0);
								params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
								view.getInstructionTextView().setLayoutParams(params);
								
								// Scale up the clicked indicator to show the larger circular window through the overlay
								ScaleAnimation scaleUpAnim = new ScaleAnimation(1.0f, 1.6f, 1.0f, 1.6f, 20.0f * mScale, 20.0f * mScale);
								scaleUpAnim.setDuration(250);
								scaleUpAnim.setFillAfter(true);
								
								// Only show window if the indicator is a '?'. Otherwise only scale up
								if (!indicator.rotateImage) {
									scaleUpAnim.setAnimationListener(new AnimationListener() {

										@Override
										public void onAnimationEnd(Animation animation) {
											indicatorTextView.setText("");
											
											// Paint used to "cut out" the circle shape
											Paint p = new Paint();
										    p.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
											
										    // Create the new overlay bitmap
											Bitmap overlay = Bitmap.createBitmap(view.getOverlayView().getWidth(),
													view.getOverlayView().getHeight(), Bitmap.Config.ARGB_8888);
											
											Canvas canvas = new Canvas(overlay);
											
											// Overlay is initially black
											canvas.drawColor(Color.BLACK);
											
											// Calculate the indicator view center
											float centerX = v.getLeft() + v.getWidth() / 2.0f;
											float centerY = v.getTop() + v.getHeight() / 2.0f;
											
											// Cut out a circle from the overlay
											canvas.drawCircle(centerX, centerY, 31.0f * mScale, p);
										    
											view.getOverlayView().setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), overlay));
											v.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.help_indicator_selected));
										}

										@Override
										public void onAnimationRepeat(Animation animation) { }

										@Override
										public void onAnimationStart(Animation animation) { }
										
							        });
								}
								
								v.startAnimation(scaleUpAnim);
							}
						}
						
					});
					
					// Add the indicator view, ?, to the window
					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(mScale * 40), (int)(mScale * 40));
					params.setMargins((int)(indicator.x * mScale), (int)(indicator.y * mScale), 0, 0);
					instructionCellView.getWrapperView().addView(indicatorTextView, 2, params);
				}
			}
		}
		
		return instructionCellView;
	}
	
	/**
	 * Helpful method that scales down the given indicator view
	 * @param v the indicator view, should be of type TextView
	 */
	private void scaleDownIndicatorView(final View v) {
		HelpIndicator indicator = (HelpIndicator) mIndicatorTextView.getTag();
		if (indicator.rotateImage) {
			mIndicatorTextView.setBackgroundResource(R.drawable.help_indicator_rotate_circle);
			mIndicatorTextView.setText("");
		}
		else {
			mIndicatorTextView.setBackgroundResource(R.drawable.help_screen_circle_background);
			mIndicatorTextView.setText("?");
		}
		
		ScaleAnimation scaleDownAnim = new ScaleAnimation(1.6f, 1.0f, 1.6f, 1.0f, 20.0f * mScale, 20.0f * mScale);
		scaleDownAnim.setDuration(250);
		scaleDownAnim.setFillAfter(true);
		scaleDownAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				mIndicatorTextView = v != null ? (TextView) v : null;
			}

			@Override
			public void onAnimationRepeat(Animation animation) { }

			@Override
			public void onAnimationStart(Animation animation) { }
			
		});
		
		mIndicatorTextView.startAnimation(scaleDownAnim);
	}
	
	public void clearIndicatorTextView() {
		mIndicatorTextView = null;
	}
	
	/**
	 * Creates an indicator view
	 * @param indicator The indicator information. This information is available in an HelpIndicatorModel object.
	 * @return the indicator view
	 */
	private TextView getIndicatorTextView(HelpIndicator indicator) {
		TextView tv = new TextView(mContext);
		tv.setGravity(Gravity.CENTER);
		tv.setTextColor(Color.WHITE);
		tv.setTextSize(28.0f);
		tv.setTypeface(Typeface.DEFAULT_BOLD);
		tv.setTag(indicator);
		
		if (indicator.rotateImage) {
			tv.setBackgroundResource(R.drawable.help_indicator_rotate_circle);
			tv.setText("");
		}
		else {
			tv.setBackgroundResource(R.drawable.help_screen_circle_background);
			tv.setText("?");
		}
		
		return tv;
	}
}
