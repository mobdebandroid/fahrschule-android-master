package de.freenet.pocketfahrschulelite.widget;

import de.freenet.pocketfahrschulelite.R;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GraphCellView extends RelativeLayout {
	
	private TextView mPointsTextView;
	private ImageView mBallImageView;
	private TextView mDateTextView;
	private LinearLayout mLinearLayout;
	
	public int startY;
	public int lastY;
	public int prevY;
	public int nextY;
	
	public GraphCellView(Context context) {
		super(context);
		initGraphCellView(context);
	}
	
	public GraphCellView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initGraphCellView(context);
	}
	
	public GraphCellView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initGraphCellView(context);
	}
	
	private void initGraphCellView(Context context) {
		mLinearLayout = new LinearLayout(context);
		mLinearLayout.setOrientation(LinearLayout.VERTICAL);
		mLinearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
		
		final float scale = context.getResources().getDisplayMetrics().density;
		
		mPointsTextView = new TextView(context);
		mPointsTextView.setGravity(Gravity.CENTER_HORIZONTAL);
		mPointsTextView.setTextColor(Color.parseColor("#9CB3E0"));
		mPointsTextView.setTypeface(Typeface.DEFAULT_BOLD);
		mPointsTextView.setTextSize(17.0f);
		mLinearLayout.addView(mPointsTextView, new LinearLayout.LayoutParams((int)(73 * scale), RelativeLayout.LayoutParams.WRAP_CONTENT));
		
		mBallImageView = new ImageView(context);
		mBallImageView.setImageResource(R.drawable.green_ball);
		mLinearLayout.addView(mBallImageView, new LinearLayout.LayoutParams((int)(16 * scale), (int)(16 * scale)));
		
		addView(mLinearLayout, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.FILL_PARENT));
		
		mDateTextView = new TextView(context);
		mDateTextView.setGravity(Gravity.CENTER_HORIZONTAL);
		mDateTextView.setTextColor(Color.WHITE);
		mDateTextView.setVisibility(View.GONE);
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		
		addView(mDateTextView, params);
	}
	
	public TextView getPointsTextView() {
		return mPointsTextView;
	}
	
	public TextView getDateTextView() {
		return mDateTextView;
	}
	
	public ImageView getBallImageView() {
		return mBallImageView;
	}
}
