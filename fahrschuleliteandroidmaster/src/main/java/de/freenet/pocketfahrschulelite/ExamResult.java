package de.freenet.pocketfahrschulelite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.freenet.pocketfahrschulelite.ExamResult;
import de.freenet.pocketfahrschulelite.QuestionSheet;
import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.objects.ExamResultObject;
import de.freenet.pocketfahrschulelite.widget.FlowLayout;
import de.freenet.pocketfahrschulelite.classes.Utils;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import de.freenet.pocketfahrschulelite.objects.QuestionModel;

public class ExamResult extends PocketFahrschuleActivity {
	
	public static final String TAG = "ExamResult";
	
	public static final String EXTRA_EXAM_TIME = "ExamResult_Extra_ExamTime";
	
	private ArrayList<QuestionModel> mQuestionModels;
	private boolean mIsOfficialExamLayout;
	
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        int time = 0;
        if (getIntent().getExtras() == null)
        	finish();
        else {
        	time = 3600 - getIntent().getExtras().getInt(EXTRA_EXAM_TIME, 0);
        }
        
        mQuestionModels = new ArrayList<QuestionModel>(QuestionModel.getQuestionModels());
        if (mQuestionModels == null || mQuestionModels.size() == 0) finish();
        
        mIsOfficialExamLayout = FahrschulePreferences.getInstance().isOfficialExamLayout();
        
        int points = 0;
        int numFivePointsQuestionFalse = 0;
        int maxPoints = 10;
		try {
			JSONObject examSheet = new JSONObject(Utils.getContentFromFile(getAssets().open("exam_sheet.json")));
			maxPoints = examSheet.getJSONObject(FahrschulePreferences.getInstance().getCurrentLicenseClassString())
				.getJSONObject(FahrschulePreferences.getInstance().getCurrentTeachingTypeString()).getInt("MaxPoints");
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		} catch (JSONException e) {
			Log.e(TAG, "JSONException: " + e.getMessage());
		}
        
		Map<Integer, Integer> questionIdIndexMap = new HashMap<Integer, Integer>();
		for (int i = 0;i < mQuestionModels.size();i++) {
        	QuestionModel model = mQuestionModels.get(i);
			model.hasSolutionBeenShown = true;
			if (!model.hasAnsweredCorrectly()) {
				points += model.question.points;
				if (model.question.points == 5) numFivePointsQuestionFalse++;
			}
			
			questionIdIndexMap.put(model.question.id, i);
		}
        
        // Set proper orientation. Landscape for official exam layout and portrait for normal layout.
        if (mIsOfficialExamLayout) {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        	setAlternativeTitleBar(R.layout.titlebar_green);
        }
        else {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        
        setContentView(mIsOfficialExamLayout ? R.layout.exam_result_official : R.layout.exam_result);
        
     // Set exam points
        TextView tv = (TextView) findViewById(R.id.examResultPointsTextView);
        if (mIsOfficialExamLayout) {
        	tv.setText(getString(R.string.license_class_and_error_points,
        		FahrschulePreferences.getInstance().getCurrentLicenseClass().toString(), points));
        	
        	// Set top right exam passed / failed indicator
        	if (points > maxPoints || (numFivePointsQuestionFalse == 2 && points == 10 && maxPoints == 10)) {
                tv = (TextView) findViewById(R.id.examResultTextView);
                tv.setText(R.string.exam_failed);
                tv.setBackgroundResource(R.color.official_exam_dark_red);
            }
        	
        	// Fetch a sorted list adding main group id, name
        	FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
        	List<ExamResultObject> resultObjects = db.arrangeQuestionsForOfficialExamLayoutResultsView(mQuestionModels);
        	db.close();
        	
        	View v = View.inflate(this, R.layout.exam_result_cell, null);
        	FlowLayout flowLayout = (FlowLayout) v.findViewById(R.id.flowLayout);
        	((TextView) v.findViewById(android.R.id.text1)).setText(resultObjects.get(0).mainGroupName);
        	
        	int lastGroupId = resultObjects.get(0).mainGroupId;
        	int totalQuestions = 0;
        	int correctQuestions = 0;
        	int errorPoints = 0;
        	
        	int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 43.0f, getResources().getDisplayMetrics());
        	int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40.0f, getResources().getDisplayMetrics());
        	ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
        	
        	// For the question boxes. When clicked open the question sheet with the selected question active
        	OnClickListener clickListener = new OnClickListener() {
				@Override
				public void onClick(View v) {
					QuestionModel.clearAndSetQuestionModels(mQuestionModels);
					Intent i = new Intent(ExamResult.this, QuestionSheet.class);
					i.putExtra(QuestionSheet.EXTRA_HIDE_SOLUTION_BUTTON, true);
					i.putExtra(QuestionSheet.EXTRA_IS_OLD_OFFICIAL_EXAM, true);
					i.putExtra(QuestionSheet.EXTRA_INDEX, (Integer) v.getTag());
					startActivity(i);
				}
			};
        	
        	for (ExamResultObject obj : resultObjects) {
        		// Reached a new main group? Then add the current one to the view and create a new one.
        		if (lastGroupId != obj.mainGroupId) {
        			if (lastGroupId < 8 && obj.mainGroupId >= 8) {
        				tv = (TextView) findViewById(R.id.examMainResultTextView);
        				tv.setText(getString(R.string.official_result_main_title, errorPoints));
        				errorPoints = 0;
        			}
        			
        			int resId = lastGroupId > 7 ? R.id.additionalQuestionsLinearLayout : R.id.mainQuestionsLinearLayout;
        			((TextView) v.findViewById(android.R.id.text2)).setText(String.format("%d/%d", correctQuestions, totalQuestions));
        			((LinearLayout) findViewById(resId)).addView(v);
        			
        			correctQuestions = 0;
        			totalQuestions = 0;
        			
        			lastGroupId = obj.mainGroupId;
        			v = View.inflate(this, R.layout.exam_result_cell, null);
        			flowLayout = (FlowLayout) v.findViewById(R.id.flowLayout);
        			((TextView) v.findViewById(android.R.id.text1)).setText(obj.mainGroupName);
        		}
        		
        		// Add new question button for the current main group. Each button is represented by either a red or green box depending on
        		// if you answered the question correctly or not.
        		totalQuestions++;
        		Button btn = new Button(this);
        		btn.setTag(questionIdIndexMap.get(obj.questionId));
        		btn.setMinWidth(width);
        		btn.setMinHeight(height);
        		btn.setOnClickListener(clickListener);
    			if (obj.answeredCorrectly) {
    				correctQuestions++;
    				btn.setBackgroundResource(R.color.official_exam_darker_green);
    			}
    			else {
    				errorPoints += obj.points;
    				btn.setBackgroundResource(R.color.official_exam_dark_red);
    			}
    			flowLayout.addView(btn, params);
        	}
        	
        	// Add the last view to the layout
        	((TextView) v.findViewById(android.R.id.text2)).setText(String.format("%d/%d", correctQuestions, totalQuestions));
        	int resId = lastGroupId > 7 ? R.id.additionalQuestionsLinearLayout : R.id.mainQuestionsLinearLayout;
			((LinearLayout) findViewById(resId)).addView(v);
        	
        	tv = (TextView) findViewById(R.id.examAdditionalResultTextView);
			tv.setText(getString(R.string.official_result_additional_title, FahrschulePreferences.getInstance().getCurrentLicenseClass().toString(), errorPoints));
        }
        else {
        	tv.setText(getString(R.string.points_exam_result, points));
        	
        	// Set time spent on exam
            tv = (TextView) findViewById(R.id.examResultTimeTextView);
            if (time > 59) {
            	tv.setText(getString(R.string.minutes_exam_result, time / 60));
            }
            else {
            	tv.setText(getString(R.string.seconds_exam_result, time));
            }
            
            // Set mascot text
            tv = (TextView) findViewById(android.R.id.text1);
            if (points == 0) {
                tv.setText(getResources().getTextArray(R.array.exam_texts)[0]);
            }
            else if (points <= maxPoints || (numFivePointsQuestionFalse != 2 && points == 10 && maxPoints == 10)) {
            	tv.setText(getResources().getTextArray(R.array.exam_texts)[1]);
            }
            else if (points > maxPoints && points <= 20 || (numFivePointsQuestionFalse == 2 && points == 10 && maxPoints == 10)) {
            	tv.setText(getResources().getTextArray(R.array.exam_texts)[2]);
            }
            else if (points > 20 && points <= 60) {
            	tv.setText(getResources().getTextArray(R.array.exam_texts)[3]);
            }
            else {
            	tv.setText(getResources().getTextArray(R.array.exam_texts)[4]);
            }
            
            // Set mascot image and enable / disable faulty questions button
            if (points > maxPoints || (numFivePointsQuestionFalse == 2 && points == 10 && maxPoints == 10)) {
                ImageView iv = (ImageView) findViewById(R.id.examResultMascotImageView);
                iv.setImageResource(R.drawable.image_endpruefung_nicht_bestanden);
            }
            else if (points == 0) {
            	Button btn = (Button) findViewById(R.id.button3);
            	btn.setEnabled(false);
            	btn.setTextColor(Color.GRAY);
            }
        }
        
        setResult(RESULT_OK);
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i = new Intent(this, BuyFullVersion.class);
			startActivity(i);
			finish();
			
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	public void retryExam(View v) {
		for (QuestionModel model : mQuestionModels) {
			model.hasSolutionBeenShown = false;
			model.givenAnswers.clear();
		}
		
		QuestionModel.clearAndSetQuestionModels(mQuestionModels);
		Intent i = new Intent(this, QuestionSheet.class);
		i.putExtra(QuestionSheet.EXTRA_IS_EXAM, true);
		startActivityForResult(i, Exam.REQUEST_CODE);
		finish();
	}
	
	public void showAllQuestions(View v) {
		QuestionModel.clearAndSetQuestionModels(mQuestionModels);
		Intent i = new Intent(this, QuestionSheet.class);
		i.putExtra(QuestionSheet.EXTRA_HIDE_SOLUTION_BUTTON, true);
		startActivity(i);
	}
	
	public void showFaultyQuestions(View v) {
		ArrayList<QuestionModel> models = new ArrayList<QuestionModel>();
		for (QuestionModel model : mQuestionModels) {
			if (!model.hasAnsweredCorrectly())
				models.add(model);
		}
		
		QuestionModel.clearAndSetQuestionModels(models);
		Intent i = new Intent(this, QuestionSheet.class);
		i.putExtra(QuestionSheet.EXTRA_HIDE_SOLUTION_BUTTON, true);
		startActivity(i);
	}
}
