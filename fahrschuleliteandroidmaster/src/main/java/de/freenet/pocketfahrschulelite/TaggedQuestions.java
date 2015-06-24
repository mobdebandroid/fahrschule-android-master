package de.freenet.pocketfahrschulelite;

import de.freenet.pocketfahrschulelite.adapters.QuestionAdapter;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import de.freenet.pocketfahrschulelite.objects.QuestionModel;
import de.freenet.tracking.TrackingManager;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class TaggedQuestions extends ListActivity implements OnItemClickListener {
	
        private TextView mTextView;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tagged_question);
        
        mTextView = (TextView) findViewById(android.R.id.text1);
        
        getListView().setOnItemClickListener(this);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
        setListAdapter(new QuestionAdapter(this, db.getTaggedQuestions()));
        db.close();
        
        if (getListAdapter().getCount() == 0) {
        	mTextView.setVisibility(View.VISIBLE);
        }
        else {
        	mTextView.setVisibility(View.GONE);
        }
        
        TrackingManager.getInstance().sendStatistics(this, FahrschulePreferences.getInstance().getTrackingUrl("B7"));
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.learning, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
        // Handle item selection
        switch (item.getItemId()) {
	        case R.id.menu_polling:
	        	FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
	        	PocketFahrschuleListActivity.showPollingDialog(this, db.getTaggedQuestions(), R.array.polling_choices_2);
	        	db.close();
	            return true;
	        case R.id.menu_search:
	        	Intent i = new Intent(this, SearchQuestion.class);
	        	startActivity(i);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		QuestionModel.createModelsForQuestions(((QuestionAdapter) getListAdapter()).getItems());
		
		Intent i = new Intent(this, QuestionSheet.class);
		i.putExtra(QuestionSheet.EXTRA_INDEX, arg2);
		startActivity(i);
	}
}
