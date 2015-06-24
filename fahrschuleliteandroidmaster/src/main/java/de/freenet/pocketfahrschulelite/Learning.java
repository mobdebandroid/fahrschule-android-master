package de.freenet.pocketfahrschulelite;

import de.freenet.pocketfahrschulelite.classes.Utils;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import de.freenet.pocketfahrschulelite.objects.LearnStatistic.StatisticState;
import de.freenet.pocketfahrschulelite.widget.KanisterView;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

public class Learning extends Activity implements OnSharedPreferenceChangeListener {
	
	public static final int REQUEST_CODE = 100;
	
	private KanisterView mKanisterView;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.learning);
     
        mKanisterView = (KanisterView) findViewById(R.id.learningKanisterImageView);
        loadValuesToKanisterView();
        
        FahrschulePreferences.getInstance().registerOnSharedPreferenceChangeListener(this);
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
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
    		mKanisterView.clearValues();
    		loadValuesToKanisterView();
			mKanisterView.invalidate();
    	}
    }
    
    public void clickStartLearning(View v) {
    	Intent i = new Intent(this, QuestionCatalogNavigation.class);
    	startActivityForResult(i, REQUEST_CODE);
    }
    
    private void loadValuesToKanisterView() {
    	FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
        
        int correct = db.countQuestions(StatisticState.CORRECT_ANSWERED);
        int faulty = db.countQuestions(StatisticState.FAULTY_ANSWERED);
        
        mKanisterView.addValues(StatisticState.CORRECT_ANSWERED, correct);
        mKanisterView.addValues(StatisticState.FAULTY_ANSWERED, faulty);
        mKanisterView.addValues(StatisticState.NOT_ANSWERED, db.countQuestions(null) - correct - faulty);
        
        db.close();
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("licenseClass")) {
			mKanisterView.clearValues();
			loadValuesToKanisterView();
			mKanisterView.invalidate();
		}
	}
}
