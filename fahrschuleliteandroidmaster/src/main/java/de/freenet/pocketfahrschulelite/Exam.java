package de.freenet.pocketfahrschulelite;

import java.util.List;

import de.freenet.pocketfahrschulelite.classes.Utils;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import de.freenet.pocketfahrschulelite.objects.ExamStatistic;
import de.freenet.pocketfahrschulelite.objects.QuestionModel;
import de.freenet.pocketfahrschulelite.objects.ExamStatistic.ExamState;
import de.freenet.pocketfahrschulelite.widget.BarChartView;
import de.freenet.tracking.TrackingManager;
import de.freenet.view.OrientationListener;
import de.freenet.view.OrientationManager;
import de.freenet.view.OrientationManager.Orientation;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Exam extends Activity implements OnSharedPreferenceChangeListener, OrientationListener {
	
	private BarChartView mBarChartView;
	private TextView mExamIndicationBadgeTextView;
	private OrientationManager mOrientationManager;
	
	public static final int REQUEST_CODE = 200;
	public static final int RESULT_EXAM_ABORTED = 1;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exam);
        
        if (getParent() instanceof MainNavigation) {
//        	mExamIndicationBadgeTextView = (TextView) ((MainNavigation) getParent()).getTabHost().getTabWidget().getChildTabViewAt(1).findViewById(R.id.indicatiorBadgeTextView);
		}
        
        mBarChartView = (BarChartView) findViewById(R.id.examBarChartView);
        loadBarChartView();
        
        FahrschulePreferences.getInstance().registerOnSharedPreferenceChangeListener(this);
        
        mOrientationManager = new OrientationManager(this);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mOrientationManager.startListening(this);
    	
    	TrackingManager.getInstance().sendStatistics(this, FahrschulePreferences.getInstance().getTrackingUrl("C2"));
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	mOrientationManager.stopListening();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	FahrschulePreferences.getInstance().unregisterOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		Utils.showQuitApplicationDialog(this);
    	    
    	    return true;
    	}
    	
    	return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.exam, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	MenuItem item = menu.findItem(R.id.menu_exam_archive);
    	if (item == null) return true;
    	
    	item.setEnabled(mBarChartView.size() > 0);
    	
		return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
        // Handle item selection
        switch (item.getItemId()) {
	        case R.id.menu_exam_archive:
	        	TrackingManager.getInstance().sendStatistics(this, FahrschulePreferences.getInstance().getTrackingUrl("C6"));
	        	Intent i = new Intent(this, ExamArchive.class);
	        	startActivity(i);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (requestCode == REQUEST_CODE) {
    		
    		switch (resultCode) {
	    		case RESULT_OK:
	    			mBarChartView.clearValues();
	        		loadBarChartView();
	        		mBarChartView.invalidate();
	        		if (mExamIndicationBadgeTextView != null) {
	    				mExamIndicationBadgeTextView.setVisibility(View.GONE);
	    			}
	    			break;
	    		case RESULT_EXAM_ABORTED:
	    			if (mExamIndicationBadgeTextView != null) {
	    				mExamIndicationBadgeTextView.setVisibility(View.VISIBLE);
	    			}
	    			break;
    		}
    	}
    }
    
    /**
     * OnClick method for starting an exam. If a cancel exam is available the user will be asked if this exam should be used, otherwise
     * a new exam will be used
     * @param v the Button view
     */
    public void clickStartExam(View v) {
    	FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
    	
    	// An old exam is saved. Ask user if it should be resumed
    	if (db.countExams(ExamState.CANCELED_EXAM) > 0) {
    		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(Exam.this);
                	Intent i = null;
                	
                    switch (which) {
    	                case DialogInterface.BUTTON_POSITIVE:
    	                	// Start saved exam
    	                	List<ExamStatistic> stats = db.getExamStatistics(1, ExamState.CANCELED_EXAM);
    	                	
    	                	if (stats.size() > 0) {
    	                		stats.get(0).createModelsFromExam();
    	                		
    	                		i = new Intent(Exam.this, QuestionSheet.class);
        	            		i.putExtra(QuestionSheet.EXTRA_IS_EXAM, true);
        	            		i.putExtra(QuestionSheet.EXTRA_TIME_LEFT, stats.get(0).secondsLeft);
        	            		i.putExtra(QuestionSheet.EXTRA_INDEX, stats.get(0).selectedQuestionIndex);
        	            		startActivityForResult(i, REQUEST_CODE);
    	                	}
    	                    break;
    	                case DialogInterface.BUTTON_NEGATIVE:
    	                	// Start new exam
    	                	QuestionModel.createModelsForQuestions(db.getExamQuestions());
    	            		
    	            		i = new Intent(Exam.this, QuestionSheet.class);
    	            		i.putExtra(QuestionSheet.EXTRA_IS_EXAM, true);
    	            		startActivityForResult(i, REQUEST_CODE);
    	                	
    	                    break;
                    }
                    db.removeCancelledExam();
                    db.close();
                }
            };
            
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.resume_exam_dialogbox).setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).setTitle(R.string.exam).show();
    	}
    	else {
    		QuestionModel.createModelsForQuestions(db.getExamQuestions());
    		
    		Intent i = new Intent(this, QuestionSheet.class);
    		i.putExtra(QuestionSheet.EXTRA_IS_EXAM, true);
    		startActivityForResult(i, REQUEST_CODE);
    	}
    	db.close();
    }
    
    /**
     * Prepares the bar chart in the exam view. If no previous exams exists a placeholder image is shown.
     */
    private void loadBarChartView() {
    	FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
        List<ExamStatistic> exams = db.getExamStatistics(6);
        db.close();
        
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.examBarChartRelativeLayout);
        ImageView iv = (ImageView) this.findViewById(R.id.examPlaceholderImageView);
        if (exams.size() == 0) {
        	iv.setVisibility(View.VISIBLE);
        	rl.setVisibility(View.GONE);
        }
        else {
        	iv.setVisibility(View.GONE);
        	rl.setVisibility(View.VISIBLE);
        }
        
        mBarChartView.clearValues();
        for (ExamStatistic exam : exams) {
        	mBarChartView.addValues(exam.points, exam.passed, exam.date);
        }
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("licenseClass") || key.startsWith("teachingType")) {
			loadBarChartView();
		}
	}

	@Override
	public void onOrientationChange(Orientation orientation) {
		if (orientation.isLandscape() && mBarChartView.size() > 0) {
			TrackingManager.getInstance().sendStatistics(this, FahrschulePreferences.getInstance().getTrackingUrl("C7"));
			Intent i = new Intent(this, Graph.class);
			startActivity(i);
			overridePendingTransition(0, 0);
		}
	}
}
