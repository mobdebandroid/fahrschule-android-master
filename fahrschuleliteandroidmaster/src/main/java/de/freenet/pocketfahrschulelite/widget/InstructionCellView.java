package de.freenet.pocketfahrschulelite.widget;

import de.freenet.pocketfahrschulelite.R;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class InstructionCellView extends RelativeLayout {
	
	private ImageView mImageView;
	private View mOverlay;
	private TextView mInstructionTextView;

	public InstructionCellView(Context context) {
		super(context);
		initInstructionCellView(context);
	}

	public InstructionCellView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initInstructionCellView(context);
	}
	
	public InstructionCellView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initInstructionCellView(context);
	}
	
	private void initInstructionCellView(Context context) {
		mImageView = new ImageView(context);
		mImageView.setScaleType(ScaleType.FIT_START);
		
		mOverlay = new View(context);
		mOverlay.setBackgroundColor(Color.BLACK);
		
		final int padding = (int)(8 * context.getResources().getDisplayMetrics().density);
		
		mInstructionTextView = new TextView(context);
		mInstructionTextView.setTextColor(Color.WHITE);
		mInstructionTextView.setBackgroundResource(R.drawable.help_indicator_textbox_background);
		mInstructionTextView.setPadding(padding, padding, padding, padding);
		mInstructionTextView.setVisibility(View.INVISIBLE);
		
		// Adding views to wrapper layout
		RelativeLayout layout = new RelativeLayout(context);
		layout.setId(R.id.relativeLayout1);
		layout.addView(mImageView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));
		layout.addView(mOverlay, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));
		layout.addView(mInstructionTextView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
		
		// Add wrapper layout to the main layout
		addView(layout, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));
		
		// Add the welcome screen to the main layout
		LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = li.inflate(R.layout.welcome_screen, null);
		v.findViewById(R.id.welcomeScreenRelativeLayout).setVisibility(View.GONE);
		addView(v, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
	}
	
	public View getOverlayView() {
		return mOverlay;
	}
	
	public ImageView getImageView() {
		return mImageView;
	}
	
	public TextView getInstructionTextView() {
		return mInstructionTextView;
	}
	
	public RelativeLayout getWrapperView() {
		return (RelativeLayout) findViewById(R.id.relativeLayout1);
	}
}
