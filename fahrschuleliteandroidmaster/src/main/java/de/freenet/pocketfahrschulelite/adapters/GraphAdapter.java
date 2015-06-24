package de.freenet.pocketfahrschulelite.adapters;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.classes.Utils;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import de.freenet.pocketfahrschulelite.objects.ExamStatistic;
import de.freenet.pocketfahrschulelite.widget.GraphCellView;

public class GraphAdapter extends FahrschuleAdapter<ExamStatistic> {
	
	private static final String TAG = "GraphAdapter";

	private int mPassLimit;
	private int mHighestPoint;
	private float mDeltaY = 0.0f;
	private float mScale;
	private int mHeight = 0;
	
	public GraphAdapter(Context context, List<ExamStatistic> objects) {
		super(context);
		clearAndSetObject(objects);
		
		mPassLimit = 10; // Default
		mHighestPoint = 2 * mPassLimit; // Default
		try {
			JSONObject examSheet = new JSONObject(Utils.getContentFromFile(context.getAssets().open("exam_sheet.json")));
			mPassLimit = examSheet.getJSONObject(FahrschulePreferences.getInstance().getCurrentLicenseClassString())
					.getJSONObject(FahrschulePreferences.getInstance().getCurrentTeachingTypeString()).getInt("MaxPoints");
			mHighestPoint = 2 * mPassLimit;
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		} catch (JSONException e) {
			Log.e(TAG, "JSONException: " + e.getMessage());
		}
		
		int count = objects.size() < 7 ? objects.size() : 7;
		int lastMonth = 0;
        for (int i = objects.size() - 1;i >= 0;i--) {
        	if (i < count && objects.get(i).points > mHighestPoint)
        		mHighestPoint = objects.get(i).points;
        	
        	int currentMonth = Integer.valueOf(DateFormat.format("yyyyMM", objects.get(i).date).toString());
        	if (lastMonth != currentMonth) {
        		lastMonth = currentMonth;
        		objects.get(i).showDate = true;
        	}
        }
        
        mScale = mContext.getResources().getDisplayMetrics().density;
	}
	
	public void setHighestPoints(int points) {
		mHighestPoint = (mPassLimit * 2) < points ? points : (mPassLimit * 2);
		mDeltaY = (float)(mHeight - 57 * mScale) / (float)mHighestPoint;
	}
	
	public int getHighestPoint() {
		return mHighestPoint;
	}
	
	public int getPassLimit() {
		return mPassLimit;
	}
	
	public void startTranslateAnimation(final GraphCellView view) {
		final int points = Integer.valueOf(view.getPointsTextView().getText().toString());
		final int newY = (int)(mDeltaY * (mHighestPoint - points));
		
		TranslateAnimation anim = new TranslateAnimation(0, 0, view.lastY - view.startY, newY - view.startY);
		anim.setDuration(250);
		anim.setInterpolator(new AccelerateInterpolator(1.0f));
		anim.setFillAfter(true);
		anim.setFillEnabled(true);
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				view.lastY = newY;
			}

			@Override
			public void onAnimationRepeat(Animation animation) { }

			@Override
			public void onAnimationStart(Animation animation) { }
			
		});
		view.getPointsTextView().startAnimation(anim);
		view.getBallImageView().startAnimation(anim);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (mDeltaY == 0.0f) {
			mHeight = parent.getHeight();
			mDeltaY = (float)(mHeight - 57 * mScale) / (float)mHighestPoint;
		}
		
		GraphCellView v = null;
		try {
			v = (GraphCellView) convertView;
		} catch (ClassCastException e) { }
		
		if (v == null) {
			v = new GraphCellView(mContext);
		}
		
		ExamStatistic object = getItem(position);
		if (object != null) {
			v.getPointsTextView().clearAnimation();
			v.getBallImageView().clearAnimation();
			
			v.getPointsTextView().setText(String.valueOf(object.points));
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(v.getPointsTextView().getLayoutParams());
			params.setMargins(0, (int)(mDeltaY * (mHighestPoint - object.points)), 0, 0);
			v.getPointsTextView().setLayoutParams(params);
			v.startY = (int)(mDeltaY * (mHighestPoint - object.points));
			v.lastY = v.startY;
			
			if (object.showDate) {
				v.getDateTextView().setVisibility(View.VISIBLE);
				v.getDateTextView().setText(DateFormat.format("MMM", object.date));
			}
			else {
				v.getDateTextView().setVisibility(View.GONE);
			}
			
			if (object.points == 0)
				v.getBallImageView().setImageResource(R.drawable.daumen);
			else
				v.getBallImageView().setImageResource(object.passed ? R.drawable.green_ball : R.drawable.red_ball);
			
			v.invalidate();
		}
		
		return v;
	}
}
