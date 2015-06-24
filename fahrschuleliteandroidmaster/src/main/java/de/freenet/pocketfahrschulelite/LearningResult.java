package de.freenet.pocketfahrschulelite;

import java.util.ArrayList;

import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.objects.QuestionModel;
import de.freenet.pocketfahrschulelite.objects.LearnStatistic.StatisticState;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LearningResult extends PocketFahrschuleActivity {
	
	private ArrayList<QuestionModel> mQuestionModels;

	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mQuestionModels = new ArrayList<QuestionModel>(QuestionModel.getQuestionModels());
        if (mQuestionModels == null || mQuestionModels.size() == 0) finish();
        
        for (QuestionModel model : mQuestionModels) {
			model.hasSolutionBeenShown = true;
		}
        
        setContentView(R.layout.learning_result);
        setTitle(R.string.results);
        
        TextView tv = (TextView) findViewById(android.R.id.text1);
        tv.setText(getResources().getQuantityString(R.plurals.learn_result, mQuestionModels.size(), mQuestionModels.size()));
        
        int numCorrect = 0;
        FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
        for (QuestionModel model : mQuestionModels) {
        	if (model.hasAnsweredCorrectly()) {
        		numCorrect++;
        		db.setLearnStatistics(model.question.id, StatisticState.CORRECT_ANSWERED);
        	}
        	else {
        		db.setLearnStatistics(model.question.id, StatisticState.FAULTY_ANSWERED);
        	}
        }
        db.close();
        
        tv = (TextView) findViewById(R.id.learningResultCorrectTextView);
        tv.setText(String.valueOf(numCorrect));
        
        tv = (TextView) findViewById(R.id.learningResultFaultyTextView);
        tv.setText(String.valueOf(mQuestionModels.size() - numCorrect));
        
        if ((mQuestionModels.size() - numCorrect) == 0) {
        	Button btn = (Button) this.findViewById(R.id.button2);
        	btn.setEnabled(false);
        	btn.setTextColor(Color.GRAY);
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