package de.freenet.pocketfahrschulelite;

import de.freenet.pocketfahrschulelite.adapters.MainGroupAdapter;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import de.freenet.tracking.TrackingManager;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;

public class QuestionCatalog extends ListActivity {
	
	private boolean mFirstStart;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView listView = new ListView(this);
        listView.setId(android.R.id.list);
        setContentView(listView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        mFirstStart = true;
        
        FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
        setListAdapter(new MainGroupAdapter(this, db.getMainGroups()));
        db.close();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	if (!mFirstStart) {
    		FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
    		((MainGroupAdapter) getListAdapter()).clearAndSetObject(db.getMainGroups());
    		db.close();
    		((MainGroupAdapter) getListAdapter()).notifyDataSetChanged();
    	}
    	mFirstStart = false;
    	
    	TrackingManager.getInstance().sendStatistics(this, FahrschulePreferences.getInstance().getTrackingUrl("B2"));
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
	        	PocketFahrschuleListActivity.showPollingDialog(this, null);
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
    protected void onListItemClick (ListView l, View v, int position, long id) {
    	if (position == 0 || position == 1 || position == 5) {
	    	Intent i = new Intent(this, SubGroupList.class);
	    	i.putExtra(SubGroupList.EXTRA_MAINGROUP_ID, ((MainGroupAdapter) getListAdapter()).getItem(position).id);
	    	i.putExtra(SubGroupList.EXTRA_MAINGROUP_TITLE, ((MainGroupAdapter) getListAdapter()).getItem(position).name);
	    	startActivity(i);
    	}
    }
}
