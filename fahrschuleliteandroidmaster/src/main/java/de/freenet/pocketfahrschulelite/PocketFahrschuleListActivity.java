package de.freenet.pocketfahrschulelite;

import java.util.ArrayList;

import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.objects.Question;
import de.freenet.pocketfahrschulelite.objects.QuestionModel;
import de.freenet.pocketfahrschulelite.objects.LearnStatistic.StatisticState;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class PocketFahrschuleListActivity extends ListActivity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    }
    
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
    }
    
    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
    }
    
    @Override
    public void setContentView(View view, LayoutParams params) {
        super.setContentView(view, params);
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
    }
    
    @Override
    public void setTitle(CharSequence title) {
    	TextView tv = (TextView) findViewById(R.id.titleTextView);
    	tv.setText(title);
    }
    
    @Override
    public void setTitle(int title) {
    	TextView tv = (TextView) findViewById(R.id.titleTextView);
    	tv.setText(title);
    }
    
    public static void showPollingDialog(final Context context, final Object object) {
    	showPollingDialog(context, object, R.array.polling_choices);
    }
    
    public static void showPollingDialog(final Context context, final Object object, int itemsId) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
    	builder.setTitle(R.string.polling);
    	builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) { }
		});
    	builder.setItems(itemsId, new DialogInterface.OnClickListener() {
    		
    		@Override
    	    public void onClick(DialogInterface dialog, int item) {
    			ArrayList<Question> questions = new ArrayList<Question>();
    			FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(context);
    			switch(item) {
	    			case 0:
	    				questions.addAll(db.getQuestions(object));
	    				break;
	    			case 1:
	    				StatisticState[] s1 = { StatisticState.FAULTY_ANSWERED };
	    				questions.addAll(db.getQuestions(object, s1));
	    				break;
	    			case 2:
	    				StatisticState[] s2 = { StatisticState.NOT_ANSWERED };
	    				questions.addAll(db.getQuestions(object, s2));
	    				break;
	    			case 3:
	    				StatisticState[] s3 = { StatisticState.FAULTY_ANSWERED, StatisticState.NOT_ANSWERED };
	    				questions.addAll(db.getQuestions(object, s3));
	    				break;
    			}
    			db.close();
    			
    			if (questions.size() == 0) {
    				Toast.makeText(context, R.string.no_questions_available, Toast.LENGTH_SHORT).show();
    				return;
    			}
    				
    			QuestionModel.createModelsForQuestions(questions);
    			Intent i = new Intent(context, QuestionSheet.class);
    			((Activity) context).startActivityForResult(i, 0);
    		}
    	});
    	
    	AlertDialog alert = builder.create();
    	alert.show();
    }
}
