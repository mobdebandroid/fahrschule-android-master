package de.freenet.pocketfahrschulelite;

import java.util.List;

import de.freenet.pocketfahrschulelite.QuestionImage;
import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.adapters.QuestionSheetAdapter;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import de.freenet.pocketfahrschulelite.objects.QuestionModel;
import de.freenet.pocketfahrschulelite.objects.ExamStatistic.ExamState;
import de.freenet.pocketfahrschulelite.objects.LearnStatistic.StatisticState;
import de.freenet.pocketfahrschulelite.widget.CustomGallery;
import de.freenet.pocketfahrschulelite.widget.CustomGallery.OnFlingListener;
import de.freenet.pocketfahrschulelite.widget.QuestionSelectButton;
import de.freenet.view.OrientationListener;
import de.freenet.view.OrientationManager;
import de.freenet.view.OrientationManager.Orientation;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class QuestionSheet extends ActionBarActivity/*PocketFahrschuleActivity*/ implements OnItemSelectedListener, OnFlingListener, OnCheckedChangeListener, TextWatcher, OrientationListener,
	OnFocusChangeListener {
	
	public static final String EXTRA_INDEX = "QuestionSheet_Extra_Index";
	public static final String EXTRA_TIME_LEFT = "QuestionSheet_Extra_TimeLeft";
	public static final String EXTRA_IS_EXAM = "QuestionSheet_Extra_IsExam";
	public static final String EXTRA_HIDE_SOLUTION_BUTTON = "QuestionSheet_Extra_HideSolutionButton";
	public static final String EXTRA_IS_OLD_OFFICIAL_EXAM = "QuestionSheet_Extra_IsOldOfficialExam";
	
	private CustomGallery mGallery;
	private List<QuestionModel> mQuestionModels;
	private View mCurrentQuestionView;
	private Button mSolutionButton;
	private TextView mExamTimeTextView;
	private EditText mCurrentEditText;
	private Handler mHandler;
	private Runnable mUpdateTimeTask;
	private long mStartTime;
	private boolean mIsExam;
	private boolean mIsOldOfficialExam;
	private boolean mHideSolutionButton;
	private OrientationManager mOrientationManager;
	private ImageButton mLeftArrowButton;
	private ImageButton mRightArrowButton;
	private RelativeLayout mQuestionsOverviewRelativeLayout;
	private boolean mIsAnimating = false;
	
	// Official exam layout
	private RelativeLayout mMainQuestionsRelativeLayout;
	private RelativeLayout mAdditionalQuestionsRelativeLayout;
	int mNumMainQuestions = 0;
	int mNumAdditionalQuestions = 0;

	private Toolbar toolbar;
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        int index = 0;
        mHideSolutionButton = false;
        int timeLeft = 3600;
        if (getIntent().getExtras() != null) {
        	index = getIntent().getExtras().getInt(EXTRA_INDEX, 0);
        	timeLeft = getIntent().getExtras().getInt(EXTRA_TIME_LEFT, 3600);
        	mIsExam = getIntent().getExtras().getBoolean(EXTRA_IS_EXAM, false);
        	mIsOldOfficialExam = getIntent().getExtras().getBoolean(EXTRA_IS_OLD_OFFICIAL_EXAM, false);
        	mHideSolutionButton = getIntent().getExtras().getBoolean(EXTRA_HIDE_SOLUTION_BUTTON, false);
        }
        
        if (savedInstanceState != null) {
        	timeLeft = savedInstanceState.getInt(EXTRA_TIME_LEFT, timeLeft);
        }
        
        mQuestionModels = QuestionModel.getQuestionModels();
        
        if (mQuestionModels == null || mQuestionModels.size() == 0)
        	finish();
        		     
        if (isOfficialLayout()) {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        	setAlternativeTitleBar(R.layout.titlebar_green);
        }
        else {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.question_sheet);

		toolbar = (Toolbar) findViewById(R.id.app_bar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mLeftArrowButton = (ImageButton) findViewById(R.id.questionSheetLeftImageButton);
        mRightArrowButton = (ImageButton) findViewById(R.id.questionSheetRightImageButton);
        
        if (mQuestionModels.size() == 1) {
        	mLeftArrowButton.setEnabled(false);
        	mRightArrowButton.setEnabled(false);
        }
        
        QuestionSheetAdapter adapter = new QuestionSheetAdapter(this, mQuestionModels, isOfficialLayout());
        adapter.setOnCheckedChangeListener(this);
        adapter.setTextWatcher(this);
        adapter.setOnFocusChangeListener(this);
        
        mGallery = (CustomGallery) findViewById(R.id.questionSheetGallery);
        mGallery.setOnItemSelectedListener(this);
        mGallery.setAdapter(adapter);
        mGallery.setSelection(index);
        mGallery.setOnFlingListener(this);
        
        if (isOfficialLayout()) {
        	findViewById(R.id.questionSheetLinearLayout).setVisibility(View.GONE);
        	initializeQuestionSelectButtons();
        	selectQuestionSelectButton(index);

        	// Set question answered state
        	for (int i = 0;i < mQuestionModels.size();i++) {
        		boolean isAnAnswerGiven = mQuestionModels.get(i).isAnAnswerGiven();
        		boolean isOfficialExamTagged = mQuestionModels.get(i).isOfficialExamTagged;
        		if (isAnAnswerGiven || isOfficialExamTagged) {
        			QuestionSelectButton btn = getQuestionSelectButton(i);
        			btn.setAnswered(isAnAnswerGiven);
        			btn.setTagged(isOfficialExamTagged);
        		}
        	}
        }
        
        mSolutionButton = (Button) findViewById(R.id.questionSheetSolutionButton);
        mSolutionButton.setVisibility(mHideSolutionButton ? View.GONE : View.VISIBLE);
        if (mIsExam) {
        	mSolutionButton.setText(R.string.hand_in_exam);
        	
        	mStartTime = SystemClock.elapsedRealtime() - (3600 - timeLeft) * 1000;
        	
        	mExamTimeTextView = (TextView) findViewById(R.id.examTimeTextView);
        	mExamTimeTextView.setText(getTimeLeftString((3600 - timeLeft)));
        	((LinearLayout) findViewById(R.id.linearLayout2)).setVisibility(View.VISIBLE);
        	
        	if (mHandler == null) {
	        	mHandler = new Handler();
	        	mUpdateTimeTask = new Runnable() {
	        		
	        		public void run() {
	        			final long start = mStartTime;
	        			long millis = SystemClock.elapsedRealtime() - start;
	        			int seconds = (int) (millis / 1000);
	        			int minutes = seconds / 60;
	        			seconds = seconds % 60;
	        			int totalSeconds = minutes * 60 + seconds;
	        			
	        			if (seconds == 0 || minutes >= 59) {
	        				
	        				if (minutes == 59 && seconds == 0) {
	        					mExamTimeTextView.setTextColor(Color.RED);
	        		        }
	        		        else if (minutes == 60) {
	        		            handInExamAndShowResult();
	        		        }
	        				
	        				mExamTimeTextView.setText(getTimeLeftString(totalSeconds));
	        			}
	        			
	        			mHandler.postAtTime(this, start + ((totalSeconds + 1) * 1000));
	        		}
	    		};
	    		mHandler.post(mUpdateTimeTask);
        	}
        }
        
        mOrientationManager = new OrientationManager(this);
    }
	
	private void initializeQuestionSelectButtons() {
		mQuestionsOverviewRelativeLayout = (RelativeLayout) findViewById(R.id.questionsOverviewRelativeLayout);
		mMainQuestionsRelativeLayout = (RelativeLayout) findViewById(R.id.mainQuestionsRelativeLayout);
		mAdditionalQuestionsRelativeLayout = (RelativeLayout) findViewById(R.id.additionalQuestionsRelativeLayout);
		
		mNumMainQuestions = 9;
		mNumAdditionalQuestions = 1;
		
		// Hide question selection button for 'Grundstoff'
		for (int i = mNumMainQuestions;i < 20;i++) {
			mMainQuestionsRelativeLayout.getChildAt(i).setVisibility(View.GONE);
		}
		
		// Hide question selection button for 'Zusatzstoff'
		for (int i = mNumAdditionalQuestions;i < 30;i++) {
			mAdditionalQuestionsRelativeLayout.getChildAt(i).setVisibility(View.GONE);
		}
		
		// Adjust overlay height
		if (mNumMainQuestions <= 20 && mNumAdditionalQuestions <= 20) {
			mQuestionsOverviewRelativeLayout.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 123.0f, getResources().getDisplayMetrics());
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (outState != null) {
			int timeLeft = 3600 - (int) ((SystemClock.elapsedRealtime() - mStartTime) / 1000);
			outState.putInt(EXTRA_TIME_LEFT, timeLeft);
		}
	}
	
	@Override
    protected void onResume() {
    	super.onResume();
    	if (!isOfficialLayout()) {
    		mOrientationManager.startListening(this);
    	}
    }
    
    @Override
    protected void onPause() {
    	if (!isOfficialLayout()) {
    		mOrientationManager.stopListening();
    	}
    	super.onPause();
    }
	
    @Override
	protected void onDestroy() {
		if (mHandler != null) {
			mHandler.removeCallbacks(mUpdateTimeTask);
			mHandler = null;
		}
		mOrientationManager = null;
		super.onDestroy();
	}
    
    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event) {
    	
    	QuestionSheetAdapter adapter = ((QuestionSheetAdapter) mGallery.getAdapter());
    	
    	if (KeyEvent.KEYCODE_BACK == keyCode && mIsExam) {
    		abortExam();
    		
    		return true;
    	}
    	else if (KeyEvent.KEYCODE_BACK == keyCode && !FahrschulePreferences.getInstance().isInstantSolutionMode() && adapter.isAnAnswerGiven() && !mHideSolutionButton) {
    		setResult(RESULT_OK);
    		
    		QuestionModel.clearAndSetQuestionModels(adapter.getQuestionModelsWithGivenAnswers());
    		Intent i = new Intent(this, LearningResult.class);
    		startActivityForResult(i, 0);
    		finish();
    		
    		return true;
    	}
    	else if (KeyEvent.KEYCODE_BACK == keyCode && !mHideSolutionButton) {
    		Intent i = new Intent(this, BuyFullVersion.class);
			startActivity(i);
			finish();
    	}
    	
    	return super.onKeyDown(keyCode, event);
    }
    
    /**
     * Help method for aborting an exam
     */
    private void abortExam() {
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
	                case DialogInterface.BUTTON_POSITIVE:
	                	int timeLeft = 3600 - (int) ((SystemClock.elapsedRealtime() - mStartTime) / 1000);
	                	int points = 0;
	                	for (QuestionModel model : mQuestionModels) {
	            			model.hasSolutionBeenShown = true;
	            			if (!model.hasAnsweredCorrectly()) {
	            				points += model.question.points;
	            			}
	            		}
	                	
	                	FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(QuestionSheet.this);
	                	db.setExamStatistics(ExamState.CANCELED_EXAM, timeLeft, points, mQuestionModels, mGallery.getSelectedItemPosition());
	                	db.close();
	                	
	                	setResult(Exam.RESULT_EXAM_ABORTED);
	                	finish();
	                    break;
	                case DialogInterface.BUTTON_NEGATIVE:
	                    // Do nothing
	                    break;
                }
            }
        };
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.abort_exam_dialogbox).setPositiveButton(R.string.yes, dialogClickListener)
            .setNegativeButton(R.string.no, dialogClickListener).setTitle(R.string.abort_exam).show();
    }
    
    /**
     * OnClick method which is called when the user click on one of the two arrows at the bottom of the question sheet view.
     * @param v the Button clicked
     */
    public void changeQuestion(View v) {
    	
    	QuestionSheetAdapter adapter = ((QuestionSheetAdapter)mGallery.getAdapter());
		
		if (FahrschulePreferences.getInstance().isInstantSolutionMode() && !adapter.getItem(mGallery.getSelectedItemPosition()).hasSolutionBeenShown && !mIsExam) {
			solutionButtonClick(mSolutionButton);
			return;
		}
    	
    	switch (v.getId()) {
	    	case R.id.questionSheetLeftImageButton:
	    		if (mGallery.getSelectedItemPosition() == 0 && mGallery.getCount() > 2) {
	    			mGallery.setSelection(mGallery.getCount() - 1, true);
	    			return;
	    		}
	    		mGallery.onScroll(null, null, -50.0f, 0.0f);
				mGallery.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null);
	    		break;
	    	case R.id.questionSheetRightImageButton:
	    	case R.id.nextQuestionButton:
	    		if (mGallery.getSelectedItemPosition() == mGallery.getCount() - 1 && mGallery.getCount() > 2) {
	    			mGallery.setSelection(0, true);
	    			return;
	    		}
	    		mGallery.onScroll(null, null, 50.0f, 0.0f);
				mGallery.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
	    		break;
    	}
    }
    
    /**
     * Helpful method which returns a human readable string with the amount of time left on a exam
     * @param seconds seconds passed since start of the exam. Will subtract the number from 3600 (60 minutes)
     * @return a human readable string. >60 seconds left, returns minutes left. Returns seconds left otherwise
     */
    private String getTimeLeftString(int seconds) {
    	long timeLeft = 3600 - seconds;
        return timeLeft <= 60 ? String.format("%d", (int)timeLeft) : String.format("%d", (int)Math.ceil((double)timeLeft / (double)60.0));
    }
    
    /**
     * This method will update the TextView containing the amount of not yet answered questions.
     * If will also hide the TextView completely if all questions have been answered.
     * This method will only be called in official exam mode.
     */
    private void updateQuestionsLeftAndQuestionSelection() {
    	QuestionModel model = ((QuestionSheetAdapter) mGallery.getAdapter()).getItem(mGallery.getSelectedItemPosition());
    	
    	QuestionSelectButton btn = getQuestionSelectButton(mGallery.getSelectedItemPosition());
    	btn.setAnswered(model.isAnAnswerGiven());
    	
    	int numberOfQuestionsNotAnswered = numberOfQuestionsNotAnswered();
		for (int i=0;i < mGallery.getChildCount();i++) {
			View v = mGallery.getChildAt(i);
			if (v != null) {
				LinearLayout questionsLeftLinearLayout = (LinearLayout) v.findViewById(R.id.questionsLeftLinearLayout); 
				TextView questionsLeft = (TextView) v.findViewById(R.id.questionsLeftTextView);
				if (numberOfQuestionsNotAnswered > 0) {
					questionsLeft.setText(getString(R.string.questions_left, numberOfQuestionsNotAnswered));
					
					if (questionsLeftLinearLayout.getVisibility() == View.GONE) {
						questionsLeftLinearLayout.setVisibility(View.VISIBLE);
					}
				}
				else {
					questionsLeftLinearLayout.setVisibility(View.GONE);
				}
			}
			numberOfQuestionsNotAnswered();
		}
    }
    
    /**
     * Calculates the number of questions the user has not answered. Used for exams.
     * @return number of questions not answered
     */
    private int numberOfQuestionsNotAnswered() {
    	int numAnsweredQuestions = 0;
        for (QuestionModel model : mQuestionModels) {
        	if (model.givenAnswers.size() > 0)
        		numAnsweredQuestions++;
        }
        
        return mQuestionModels.size() - numAnsweredQuestions;
    }
    
    /**
     * This method wraps up the exam process, stores the results in the database and then passes the question models on to the exam result view.
     */
    private void handInExamAndShowResult() {
    	
    	int timeLeft = 3600 - (int) ((SystemClock.elapsedRealtime() - mStartTime) / 1000);
    	int points = 0;
    	for (QuestionModel model : mQuestionModels) {
			model.hasSolutionBeenShown = true;
			if (!model.hasAnsweredCorrectly()) {
				points += model.question.points;
			}
		}
    	
    	FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
    	db.setExamStatistics(ExamState.FINISHED_EXAM, timeLeft, points, mQuestionModels);
    	db.close();
    	
    	setResult(RESULT_OK);
    	
    	Intent i = new Intent(this, ExamResult.class);
    	i.putExtra(ExamResult.EXTRA_EXAM_TIME, timeLeft);
		startActivityForResult(i, Exam.REQUEST_CODE);
		finish();
    }
    
    /**
     * OnClick method for the show solution / hand in exam button
     * @param v the button view
     */
    public void solutionButtonClick(View v) {
    	
    	if (mIsExam) {
    		int numNotAnsweredQuestions = numberOfQuestionsNotAnswered();
    		if (numNotAnsweredQuestions != 0) {
    			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	            @Override
    	            public void onClick(DialogInterface dialog, int which) {
    	                switch (which) {
    	                case DialogInterface.BUTTON_POSITIVE:
    	                	handInExamAndShowResult();
    	                    break;
    	                case DialogInterface.BUTTON_NEGATIVE:
    	                    // Do nothing
    	                    break;
    	                }
    	            }
    	        };
    	        
    	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	        String dialogText = getResources().getQuantityString(R.plurals.hand_in_exam_dialogbox, numNotAnsweredQuestions, numNotAnsweredQuestions);
    	        builder.setMessage(dialogText).setPositiveButton(R.string.yes, dialogClickListener)
    	            .setNegativeButton(R.string.no, dialogClickListener).setTitle(R.string.hand_in_exam).show();
    		}
    		else {
    			handInExamAndShowResult();
    		}
    	}
    	else {
    		setResult(RESULT_OK);
    		
    		v.setEnabled(false);
        	
        	QuestionSheetAdapter adapter = ((QuestionSheetAdapter)mGallery.getAdapter());
        	
        	QuestionModel model = adapter.getItem(mGallery.getSelectedItemPosition());
        	if (model.isAnAnswerGiven()) {
        		FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
        		db.setLearnStatistics(model.question.id, model.hasAnsweredCorrectly() ? StatisticState.CORRECT_ANSWERED : StatisticState.FAULTY_ANSWERED);
        		db.close();
        	}
    		adapter.showSolution(mCurrentQuestionView, mGallery.getSelectedItemPosition());
    	}
    }

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (mQuestionModels.get(arg2).question.number.contains("-M")) {
			setTitle(String.format("%d / %d | %d Pkt. | M", arg2 + 1, mQuestionModels.size(), mQuestionModels.get(arg2).question.points));
		}
		else {
			setTitle(String.format("%d / %d | %d Pkt.", arg2 + 1, mQuestionModels.size(), mQuestionModels.get(arg2).question.points));
		}
		mCurrentQuestionView = arg1;
		
		if (mQuestionModels.size() == 2) {
			mLeftArrowButton.setEnabled(arg2 != 0);
	    	mRightArrowButton.setEnabled(arg2 != mQuestionModels.size() - 1);
		}
		
		if (!mIsExam) {
			QuestionSheetAdapter adapter = ((QuestionSheetAdapter)mGallery.getAdapter());
			mSolutionButton.setEnabled(!adapter.getItem(arg2).hasSolutionBeenShown);
		}
		
		if (isOfficialLayout()) {
			selectQuestionSelectButton(arg2);
		}
		
		if (mCurrentEditText != null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mCurrentEditText.getWindowToken(), 0);
			mCurrentEditText = null;
		}
		
		if (arg1 != null) {
			mCurrentEditText = (EditText) arg1.findViewById(R.id.questionNumberEditText);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) { }
	
	public QuestionSelectButton getQuestionSelectButton(int index) {
		if (index >= mNumMainQuestions) {
			return (QuestionSelectButton) mAdditionalQuestionsRelativeLayout.getChildAt(index - mNumMainQuestions);
		}
		else {
			return (QuestionSelectButton) mMainQuestionsRelativeLayout.getChildAt(index);
		}
	}
	
	public void selectQuestionSelectButton(int index) {
		for (int i = 0;i < mMainQuestionsRelativeLayout.getChildCount();i++) {
			((QuestionSelectButton) mMainQuestionsRelativeLayout.getChildAt(i)).setSelected(false);
		}
	
		for (int i = 0;i < mAdditionalQuestionsRelativeLayout.getChildCount();i++) {
			((QuestionSelectButton) mAdditionalQuestionsRelativeLayout.getChildAt(i)).setSelected(false);
		}
		
		getQuestionSelectButton(index).setSelected(true);
		
		if (index >= mNumMainQuestions && mAdditionalQuestionsRelativeLayout.getVisibility() == View.GONE) {
			toggleQuestionTab(false);
		}
		else if (index < mNumMainQuestions && mMainQuestionsRelativeLayout.getVisibility() == View.GONE) {
			toggleQuestionTab(true);
		}
	}

	@Override
	public boolean didFling(int direction, int position) {
		QuestionSheetAdapter adapter = ((QuestionSheetAdapter)mGallery.getAdapter());
		
		if (FahrschulePreferences.getInstance().isInstantSolutionMode() && !adapter.getItem(position).hasSolutionBeenShown && direction == 1 && !mIsExam) {
			solutionButtonClick(mSolutionButton);
			return true;
		}
		else if (position == 0 && direction == -1 && mGallery.getCount() > 2) {
			mGallery.setSelection(mGallery.getCount() - 1, true);
			return true;
		}
		else if (position == mGallery.getCount() - 1 && direction == 1 && mGallery.getCount() > 2) {
			mGallery.setSelection(0, true);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isAllowedToFling(int direction) {
		QuestionSheetAdapter adapter = ((QuestionSheetAdapter)mGallery.getAdapter());
		
		return !(FahrschulePreferences.getInstance().isInstantSolutionMode() &&
				!adapter.getItem(mGallery.getSelectedItemPosition()).hasSolutionBeenShown && direction == 1 && !mIsExam);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		QuestionModel model = ((QuestionSheetAdapter) mGallery.getAdapter()).getItem(mGallery.getSelectedItemPosition());
		if (buttonView.getId() == R.id.markedQuestionCheckBox) {
			FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
			db.tagQuestion((Integer)buttonView.getTag(), isChecked);
			db.close();
		}
		else if (model.givenAnswers.remove(buttonView.getTag()) == null) {
			model.givenAnswers.put((Integer) buttonView.getTag(), "");
		}
		
		if (isOfficialLayout()) {
			updateQuestionsLeftAndQuestionSelection();
		}
	}

	@Override
	public void afterTextChanged(Editable s) { }

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		QuestionModel model = ((QuestionSheetAdapter) mGallery.getAdapter()).getItem(mGallery.getSelectedItemPosition());
		
		int key = (mCurrentEditText != null && mCurrentEditText.getId() == R.id.questionNumberEditText) ? model.question.answers.get(0).id : -model.question.answers.get(0).id;
		model.givenAnswers.remove(key);
		
		if (!s.toString().equals("")) {
			model.givenAnswers.put(key, s.toString());
		}
		
		if (isOfficialLayout()) {
			updateQuestionsLeftAndQuestionSelection();
		}
	}

	@Override
	public void onOrientationChange(Orientation orientation) {
		if (isOfficialLayout()) return;
		
		QuestionModel model = ((QuestionSheetAdapter) mGallery.getAdapter()).getItem(mGallery.getSelectedItemPosition());
		
		if (orientation.isLandscape() && !model.question.image.equals("")) {
			Intent i = new Intent(this, QuestionImage.class);
			i.putExtra(QuestionImage.EXTRA_IMAGE_FILENAME, model.question.image);
			i.putExtra(QuestionImage.EXTRA_QUESTION_TEXT, model.question.text);
			i.putExtra(QuestionImage.EXTRA_EXIT_WITH_ORIENTATION, true);
			i.putExtra(QuestionImage.EXTRA_OFFICIAL_LAYOUT, false);
			startActivity(i);
			overridePendingTransition(0, 0);
		}
	}
	
	public void selectQuestion(View v) {
		Integer tag = Integer.valueOf((String) v.getTag());
		int index = tag >= 20 ? mNumMainQuestions + (tag - 20) : tag;
		
		selectQuestionSelectButton(index);
		mGallery.setSelection(index);
	}
	
	public void switchQuestionTab(View v) {
		switch (v.getId()) {
			case R.id.mainGroupButton:
				toggleQuestionTab(true);
				selectQuestion(mMainQuestionsRelativeLayout.findViewById(R.id.mainButton1));
				break;
			case R.id.additionalGroupButton:
				toggleQuestionTab(false);
				selectQuestion(mAdditionalQuestionsRelativeLayout.findViewById(R.id.additionalButton1));
				break;
		}
	}
	
	private void toggleQuestionTab(boolean showMain) {
		Button mainBtn = (Button) findViewById(R.id.mainGroupButton);
		Button additionalBtn = (Button) findViewById(R.id.additionalGroupButton);
		if (showMain) {
			mMainQuestionsRelativeLayout.setVisibility(View.VISIBLE);
			mAdditionalQuestionsRelativeLayout.setVisibility(View.GONE);
			
			mainBtn.setBackgroundResource(R.drawable.question_tab_selected);
			mainBtn.setTextColor(Color.WHITE);
			additionalBtn.setBackgroundResource(R.drawable.question_tab);
			additionalBtn.setTextColor(Color.BLACK);
		}
		else {
			mMainQuestionsRelativeLayout.setVisibility(View.GONE);
			mAdditionalQuestionsRelativeLayout.setVisibility(View.VISIBLE);
			
			mainBtn.setBackgroundResource(R.drawable.question_tab);
			mainBtn.setTextColor(Color.BLACK);
			additionalBtn.setBackgroundResource(R.drawable.question_tab_selected);
			additionalBtn.setTextColor(Color.WHITE);
		}
	}
	
	public void toggleQuestionOverview(View v) {
		if (mIsAnimating) return;
		mIsAnimating = true;
		
		switch (v.getId()) {
			case R.id.hideQuestionsOverviewButton: {
				Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_down);
				anim.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) { }
					
					@Override
					public void onAnimationRepeat(Animation animation) { }
					
					@Override
					public void onAnimationEnd(Animation animation) {
						mQuestionsOverviewRelativeLayout.setVisibility(View.GONE);
						mIsAnimating = false;
					}
				});
				mQuestionsOverviewRelativeLayout.startAnimation(anim);
			}
				break;
			case R.id.showQuestionsOverviewButton: {
				Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_up);
				anim.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) { }
					
					@Override
					public void onAnimationRepeat(Animation animation) { }
					
					@Override
					public void onAnimationEnd(Animation animation) {
						mIsAnimating = false;
					}
				});
				mQuestionsOverviewRelativeLayout.setVisibility(View.VISIBLE);
				mQuestionsOverviewRelativeLayout.startAnimation(anim);
			}
				break;
		}
	}
	
	public void tagOfficialExamQuestion(View v) {
		int index = mGallery.getSelectedItemPosition();
		
		boolean isOfficialExamTagged = !mQuestionModels.get(index).isOfficialExamTagged;
		mQuestionModels.get(index).isOfficialExamTagged = isOfficialExamTagged;
		getQuestionSelectButton(index).setTagged(isOfficialExamTagged);
		
		// Change button text to indicate if question is tagged or not
		((Button) v).setText(isOfficialExamTagged ? R.string.official_untag : R.string.official_tag);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			mCurrentEditText = (EditText) v;
		}
	}
	
	private boolean isOfficialLayout() {
		return (mIsExam || mIsOldOfficialExam) && FahrschulePreferences.getInstance().isOfficialExamLayout();
	}
}
