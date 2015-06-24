package de.freenet.pocketfahrschulelite.widget;

import de.freenet.pocketfahrschulelite.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class QuestionSelectButton extends Button {
	
	private boolean mSelected = false;
	private boolean mTagged = false;
	private boolean mAnswered = false;

	public QuestionSelectButton(Context context) {
		super(context);
		initButton();	
	}
	
	public QuestionSelectButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		initButton();
	}
	
	public QuestionSelectButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initButton();
	}
	
	private void initButton() {
		setBackgroundDrawable(getResources().getDrawable(R.drawable.question_btn));
	}
	
	public void setSelected(boolean selected) {
		if (mSelected == selected) return;
		mSelected = selected;
		setButtonBackground();
	}
	
	public void setTagged(boolean tagged) {
		if (mTagged == tagged) return;
		mTagged = tagged;
		setButtonBackground();
	}
	
	public void setAnswered(boolean answered) {
		if (mAnswered == answered) return;
		mAnswered = answered;
		setButtonBackground();
	}
	
	public boolean getSelected() {
		return mSelected;
	}
	
	public boolean getTagged() {
		return mTagged;
	}
	
	public boolean getAnswered() {
		return mAnswered;
	}
	
	private void setButtonBackground() {
		int state = 0;
		state |= mSelected ? 1 : 0;
		state |= mTagged ? 2 : 0;
		state |= mAnswered ? 4 : 0;
		
		switch (state) {
			case 1:
				setBackgroundDrawable(getResources().getDrawable(R.drawable.question_btn_frame_normal));
				break;
			case 2:
				setBackgroundDrawable(getResources().getDrawable(R.drawable.question_btn_tagged));
				break;
			case 3:
				setBackgroundDrawable(getResources().getDrawable(R.drawable.question_btn_frame_normal_tagged));
				break;
			case 4:
				setBackgroundDrawable(getResources().getDrawable(R.drawable.question_btn_selected));
				break;
			case 5:
				setBackgroundDrawable(getResources().getDrawable(R.drawable.question_btn_frame_selected_normal));
				break;
			case 6:
				setBackgroundDrawable(getResources().getDrawable(R.drawable.question_btn_selected_tagged));
				break;
			case 7:
				setBackgroundDrawable(getResources().getDrawable(R.drawable.question_btn_frame_selected_tagged));
				break;
			default:
				setBackgroundDrawable(getResources().getDrawable(R.drawable.question_btn));
		}
	}
}
