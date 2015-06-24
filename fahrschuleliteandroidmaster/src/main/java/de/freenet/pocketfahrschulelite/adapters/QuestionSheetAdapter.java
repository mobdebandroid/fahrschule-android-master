package de.freenet.pocketfahrschulelite.adapters;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.freenet.pocketfahrschulelite.QuestionImage;
import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.objects.Question;
import de.freenet.pocketfahrschulelite.objects.QuestionModel;
import de.freenet.pocketfahrschulelite.objects.Question.Type;

public class QuestionSheetAdapter extends FahrschuleAdapter<QuestionModel> implements OnClickListener {
	
	private OnCheckedChangeListener mOnCheckedChangeListener;
	private OnFocusChangeListener mOnFocusChangeListener;
	private TextWatcher mTextWatcher;
	private final FahrschuleDatabaseHelper mDb;
	private final boolean mIsOfficialLayout;
	
	public QuestionSheetAdapter(Context context, List<QuestionModel> objects, boolean isOfficialLayout) {
		super(context);
		clearAndSetObject(objects);
		mDb = new FahrschuleDatabaseHelper(context);
		mIsOfficialLayout = isOfficialLayout;
	}
	
	public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
		mOnCheckedChangeListener = listener;
	}
	
	public void setOnFocusChangeListener(OnFocusChangeListener listener) {
		mOnFocusChangeListener = listener;
	}
	
	public void setTextWatcher(TextWatcher watcher) {
		mTextWatcher = watcher;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
    	
    	View v = convertView;
    	if (v == null) {
			LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = mIsOfficialLayout ? li.inflate(R.layout.question_official, parent, false) : li.inflate(R.layout.question, parent, false);
		}
    	
    	QuestionModel object = getItem(position);
    	if (object != null) {
    		v.findViewById(R.id.relativeLayout1).requestFocus();
    		
    		TextView question = (TextView) v.findViewById(R.id.questionTextView);
    		if (mIsOfficialLayout) {
    			question.setText(object.question.text);
    			
    			TextView prefix = (TextView) v.findViewById(R.id.questionPrefixTextView);
    			if (object.question.prefix.equals("")) {
    				prefix.setVisibility(View.GONE);
    			}
    			else {
    				prefix.setText(object.question.prefix);
    				prefix.setVisibility(View.VISIBLE);
    			}
    			
    			int numberOfQuestionsNotAnswered = numberOfQuestionsNotAnswered();
    			if (numberOfQuestionsNotAnswered > 0) {
    				TextView questionsLeft = (TextView) v.findViewById(R.id.questionsLeftTextView);
        			questionsLeft.setText(mContext.getString(R.string.questions_left, numberOfQuestionsNotAnswered));
    			}
    			else {
    				v.findViewById(R.id.questionsLeftLinearLayout).setVisibility(View.GONE);
    			}
    			
    			// Hide hand in button if solution has been shown aka. is examine an old exam.
    			if (object.hasSolutionBeenShown) {
    				v.findViewById(R.id.handInButton).setVisibility(View.GONE);
    				v.findViewById(R.id.questionsLeftLinearLayout).setVisibility(View.GONE);
    			}
    			
    			// Set tag question button text
    			((Button) v.findViewById(R.id.tagQuestionButton)).setText(object.isOfficialExamTagged ? R.string.official_untag : R.string.official_tag);
    		}
    		else {
    			question.setText(object.question.text + (object.question.prefix.equals("") ? "" : String.format("\n%s", object.question.prefix)));
    		}
    		
    		CheckBox markedQuestionCheckBox = (CheckBox) v.findViewById(R.id.markedQuestionCheckBox);
    		markedQuestionCheckBox.setTag(object.question.id);
    		markedQuestionCheckBox.setChecked(mDb.isQuestionTagged(object.question.id));
    		if (mOnCheckedChangeListener != null) markedQuestionCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
    		
    		ImageView questionMacotImageView = (ImageView) v.findViewById(R.id.questionMacotImageView);
    		ImageButton questionImageButton = (ImageButton) v.findViewById(R.id.questionImageButton);
    		ImageView questionWrapperBottomImageView = (ImageView) v.findViewById(R.id.questionWrapperBottomImageView);
    		
    		if (!object.question.image.equals("")) {
    		    Bitmap bmp = null;
				try {
					InputStream buffer = mContext.getResources().getAssets().open(String.format("fragenbilder/%s", object.question.image));
					bmp = BitmapFactory.decodeStream(buffer);
				} catch (IOException e) {
				}
				
    			if (bmp != null) {
    				questionWrapperBottomImageView.setImageResource(R.drawable.bg_frage);
    				if (!mIsOfficialLayout) questionMacotImageView.setVisibility(View.GONE);
    				questionImageButton.setVisibility(View.VISIBLE);
    				questionImageButton.setImageBitmap(bmp);
    				questionImageButton.setTag(position);
    				questionImageButton.setOnClickListener(this);
    			}
    			else {
    				questionWrapperBottomImageView.setImageResource(R.drawable.bg_frage_ohne_pfeil);
    				if (!mIsOfficialLayout) questionMacotImageView.setVisibility(View.VISIBLE);
    				questionImageButton.setVisibility(View.INVISIBLE);
    			}
    		}
    		else {
    			questionWrapperBottomImageView.setImageResource(R.drawable.bg_frage_ohne_pfeil);
    			if (!mIsOfficialLayout) questionMacotImageView.setVisibility(View.VISIBLE);
    			questionImageButton.setVisibility(View.INVISIBLE);
    		}
    		
    		LinearLayout questionAnswerCheckBoxWrapper = (LinearLayout) v.findViewById(R.id.questionAnswerCheckBoxWrapper);
    		LinearLayout questionNumberAnswerWrapper = (LinearLayout) v.findViewById(R.id.questionNumberAnswerWrapper);
    		
    		if (object.question.type == Type.CHOICE) {
    			questionAnswerCheckBoxWrapper.setVisibility(View.VISIBLE);
    			questionNumberAnswerWrapper.setVisibility(View.GONE);
    			
    			CheckBox cb = (CheckBox) v.findViewById(R.id.questionAnswerCheckBox1);
    			
    			cb.setText(object.question.answers.get(0).text);
    			cb.setTag(object.question.answers.get(0).id);
    			cb.setChecked(object.givenAnswers.containsKey(object.question.answers.get(0).id));
    			if (mOnCheckedChangeListener != null) cb.setOnCheckedChangeListener(mOnCheckedChangeListener);
    			
    			cb = (CheckBox) v.findViewById(R.id.questionAnswerCheckBox2);
    			cb.setText(object.question.answers.get(1).text);
    			cb.setTag(object.question.answers.get(1).id);
    			cb.setChecked(object.givenAnswers.containsKey(object.question.answers.get(1).id));
    			if (mOnCheckedChangeListener != null) cb.setOnCheckedChangeListener(mOnCheckedChangeListener);
        		
    			cb = (CheckBox) v.findViewById(R.id.questionAnswerCheckBox3);
    			RelativeLayout questionAnswerLinearLayoutWrapper = (RelativeLayout) v.findViewById(R.id.questionAnswerLinearLayoutWrapper);
        		if (object.question.answers.size() == 3) {
        			cb.setText(object.question.answers.get(2).text);
        			cb.setTag(object.question.answers.get(2).id);
        			cb.setChecked(object.givenAnswers.containsKey(object.question.answers.get(2).id));
        			cb.setEnabled(true);
        			if (mOnCheckedChangeListener != null) cb.setOnCheckedChangeListener(mOnCheckedChangeListener);
        			
        			questionAnswerLinearLayoutWrapper.setVisibility(View.VISIBLE);
        		}
        		else {
        			cb.setText("");
        			cb.setEnabled(false);
        			
        			questionAnswerLinearLayoutWrapper.setVisibility(View.GONE);
        		}
    		}
    		else if (object.question.type == Type.NUMBER) {
    			questionAnswerCheckBoxWrapper.setVisibility(View.GONE);
    			questionNumberAnswerWrapper.setVisibility(View.VISIBLE);
    			
    			
    			// The special case when two numbers has to be entered.
    			LinearLayout questionNumberAnswerEditTextWrapper2 = (LinearLayout) v.findViewById(R.id.questionNumberAnswerEditTextWrapper2);
    			questionNumberAnswerEditTextWrapper2.setVisibility(object.question.number.equals("11.11.1") ? View.VISIBLE : View.GONE);
    			
    			EditText et = (EditText) v.findViewById(R.id.questionNumberEditText);
    			et.clearFocus();
    			
    			if (mOnFocusChangeListener != null) et.setOnFocusChangeListener(mOnFocusChangeListener);
    			if (mTextWatcher != null) et.removeTextChangedListener(mTextWatcher);
    			
    			TextView tv = (TextView) v.findViewById(R.id.questionNumberAnswerTextView);
    			tv.setText(object.question.answers.get(0).text);
    			
    			if (object.givenAnswers.containsKey(object.question.answers.get(0).id)) {
    				et.setText((String) object.givenAnswers.get(object.question.answers.get(0).id));
    			}
    			
    			/**
    			 * Need to remove and then re-add the TextChangeListener due to behavior occurring when using EditText in Gallery.
    			 * setText() triggers the onTextChanged() interface method which is not wanted here.
    			 */
    			
    			if (mTextWatcher != null) et.addTextChangedListener(mTextWatcher);
    			
    			if (object.question.number.equals("11.11.1")) {
    				et = (EditText) v.findViewById(R.id.questionNumberEditText2);
    				et.clearFocus();
        			
        			if (mOnFocusChangeListener != null) et.setOnFocusChangeListener(mOnFocusChangeListener);
        			if (mTextWatcher != null) et.removeTextChangedListener(mTextWatcher);
        			
        			if (object.givenAnswers.containsKey(-object.question.answers.get(0).id)) {
        				et.setText((String) object.givenAnswers.get(-object.question.answers.get(0).id));
        			}
        			
        			if (mTextWatcher != null) et.addTextChangedListener(mTextWatcher);
    			}
    		}
    		
    		if (object.hasSolutionBeenShown) {
    			showSolution(v, position);
    		}
    	}
    	
    	return v;
    }
	
	public boolean isAnAnswerGiven() {
		
		for (QuestionModel model : getItems()) {
			if (model.isAnAnswerGiven()) return true;
		}
		
		return false;
	}
	
	public ArrayList<QuestionModel> getQuestionModelsWithGivenAnswers() {
		
		ArrayList<QuestionModel> models = new ArrayList<QuestionModel>();
		for (QuestionModel model : getItems()) {
			if (model.isAnAnswerGiven())
				models.add(model);
		}
		
		return models;
	}
	
	/**
	 * Shows the solution of a question
	 * @param v the current question view
	 * @param position the position in the Gallery view which corresponds to the array index
	 */
	public void showSolution(View v, int position) {
		if (v != null) {
			
			QuestionModel model = getItem(position);
    		
    		if (model.question.type == Question.Type.CHOICE) {
    			// Reusable views
    			CheckBox cb = null;
    			ImageView iv = null;
    			
    			if (mIsOfficialLayout) {
    				cb = (CheckBox) v.findViewById(R.id.questionAnswerCheckBox1);
        			cb.setClickable(false);
        			if (model.question.answers.get(0).correct || model.givenAnswers.containsKey(model.question.answers.get(0).id)) {
        				cb.setButtonDrawable(model.question.answers.get(0).correct ? R.drawable.btn_check_correct : R.drawable.btn_check_incorrect);
        			}
        			
        			cb = (CheckBox) v.findViewById(R.id.questionAnswerCheckBox2);
        			cb.setClickable(false);
        			if (model.question.answers.get(1).correct || model.givenAnswers.containsKey(model.question.answers.get(1).id)) {
        				cb.setButtonDrawable(model.question.answers.get(1).correct ? R.drawable.btn_check_correct : R.drawable.btn_check_incorrect);
        			}
        			
        			if (model.question.answers.size() > 2) {
            			cb = (CheckBox) v.findViewById(R.id.questionAnswerCheckBox3);
            			cb.setClickable(false);
            			if (model.question.answers.get(2).correct || model.givenAnswers.containsKey(model.question.answers.get(2).id)) {
            				cb.setButtonDrawable(model.question.answers.get(2).correct ? R.drawable.btn_check_correct : R.drawable.btn_check_incorrect);
            			}
            		}
    			}
    			else {
        			if (model.question.answers.get(0).correct || model.givenAnswers.containsKey(model.question.answers.get(0).id)) {
            			iv = (ImageView) v.findViewById(R.id.questionAnswerImageView1);
                		iv.setVisibility(View.VISIBLE);
                		iv.setImageResource(model.question.answers.get(0).correct ? R.drawable.icon_richtig : R.drawable.icon_falsch);
        			}
        			
        			if (model.question.answers.get(1).correct || model.givenAnswers.containsKey(model.question.answers.get(1).id)) {
        				iv = (ImageView) v.findViewById(R.id.questionAnswerImageView2);
                		iv.setVisibility(View.VISIBLE);
                		iv.setImageResource(model.question.answers.get(1).correct ? R.drawable.icon_richtig : R.drawable.icon_falsch);
        			}
            		
            		if (model.question.answers.size() > 2) {
            			cb = (CheckBox) v.findViewById(R.id.questionAnswerCheckBox3);
            			cb.setClickable(false);
            			
            			if (model.question.answers.get(2).correct || model.givenAnswers.containsKey(model.question.answers.get(2).id)) {
            				iv = (ImageView) v.findViewById(R.id.questionAnswerImageView3);
                    		iv.setVisibility(View.VISIBLE);
                    		iv.setImageResource(model.question.answers.get(2).correct ? R.drawable.icon_richtig : R.drawable.icon_falsch);
            			}
            		}
    			}
    		}
    		else if (model.question.type == Question.Type.NUMBER) {
    			EditText et = (EditText) v.findViewById(R.id.questionNumberEditText);
    			et.setFocusable(false);
    			
    			TextView tv = (TextView) v.findViewById(R.id.questionNumberAnswerTextView);
    			if (model.question.number.equals("11.11.1")) {
    				et = (EditText) v.findViewById(R.id.questionNumberEditText2);
        			et.setFocusable(false);
        			
    				String answers[] = String.valueOf(model.question.answers.get(0).number).split("\\.");
    				tv.setText(model.question.answers.get(0).text.replace("X", answers[0]).replace("Y", answers[1]));
    			}
    			else {
    				tv.setText(model.question.answers.get(0).text.replace("X", String.valueOf(model.question.answers.get(0).number).replace(".0", "")));
    			}
    			
    			ImageView iv = (ImageView) v.findViewById(R.id.questionNumberImageView);
    			iv.setVisibility(View.VISIBLE);
    			iv.setImageResource(model.hasAnsweredCorrectly() ? R.drawable.icon_richtig : R.drawable.icon_falsch);
    		}
    		
    		model.hasSolutionBeenShown = true;
    	}
	}

	@Override
	public void onClick(View v) {
		Integer position = (Integer) v.getTag();
		
		if (position != null) {
			QuestionModel model = getItem(position);
			Intent i = new Intent(mContext, QuestionImage.class);
			i.putExtra(QuestionImage.EXTRA_IMAGE_FILENAME, model.question.image);
			i.putExtra(QuestionImage.EXTRA_QUESTION_TEXT, model.question.text);
			i.putExtra(QuestionImage.EXTRA_OFFICIAL_LAYOUT, mIsOfficialLayout);
			mContext.startActivity(i);
			if (mContext instanceof Activity) {
				((Activity) mContext).overridePendingTransition(R.anim.zoom_in, R.anim.no_sliding);
			}
		}
	}
	
	/**
     * Calculates the number of questions the user has not answered. Used for exams.
     * @return number of questions not answered
     */
    private int numberOfQuestionsNotAnswered() {
    	int numAnsweredQuestions = 0;
        for (QuestionModel model : mListItems) {
        	if (model.givenAnswers.size() > 0)
        		numAnsweredQuestions++;
        }
        
        return mListItems.size() - numAnsweredQuestions;
    }

	@Override
	protected void finalize() {
		try {
			super.finalize();
		} catch (Throwable e) {
		}
		mDb.close();
	}
}
