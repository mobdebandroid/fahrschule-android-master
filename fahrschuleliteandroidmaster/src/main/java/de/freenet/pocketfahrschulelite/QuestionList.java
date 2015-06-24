package de.freenet.pocketfahrschulelite;

import de.freenet.pocketfahrschulelite.adapters.QuestionAdapter;
import de.freenet.pocketfahrschulelite.adapters.SubGroupAdapter;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.objects.QuestionModel;
import de.freenet.pocketfahrschulelite.objects.SubGroup;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class QuestionList extends ActionBarActivity {//PocketFahrschuleListActivity implements OnItemClickListener {
	
	public static final String EXTRA_SUBGROUP_ID = "QuestionList_Extra_SubGroup_Id";
	public static final String EXTRA_SUBGROUP_TITLE  = "QuestionList_Extra_SubGroup_Title";
	
	private SubGroup mSubGroup;
	private Toolbar toolbar;

    private ListView lv;
    private QuestionAdapter questionAdapter;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getIntent().getExtras() == null || !getIntent().getExtras().containsKey(EXTRA_SUBGROUP_ID))
        	finish();
        
        mSubGroup = new SubGroup();
        mSubGroup.id = getIntent().getExtras().getInt(EXTRA_SUBGROUP_ID);

        setContentView(R.layout.question_list);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lv = (ListView) findViewById(R.id.list);

//        ListView listView = new ListView(this);
//        listView.setId(android.R.id.list);
//        listView.setBackgroundResource(R.drawable.bg_mit_gitter);
//        setContentView(listView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
//        setTitle(TextUtils.ellipsize(getIntent().getExtras().getString(EXTRA_SUBGROUP_TITLE), new TextPaint(), 180.0f, TextUtils.TruncateAt.END));
        
        FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
        questionAdapter = new QuestionAdapter(this, db.getQuestions(mSubGroup));
        lv.setAdapter(questionAdapter);
//        setListAdapter();
        db.close();
        
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QuestionModel.createModelsForQuestions(questionAdapter.getItems()); //getListAdapter()).getItems());
                Intent i = new Intent(QuestionList.this, QuestionSheet.class);
                i.putExtra(QuestionSheet.EXTRA_INDEX, position);
                startActivityForResult(i, 0);
            }
        });

         //getListView().setOnItemClickListener(this);
    }
    
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_OK) {
    		FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
            questionAdapter = new QuestionAdapter(this, db.getQuestions(mSubGroup));
            lv.setAdapter(questionAdapter);
            //setListAdapter(new QuestionAdapter(this, db.getQuestions(mSubGroup)));
            db.close();
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.learning, menu);
        menu.removeItem(R.id.menu_search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
        // Handle item selection
        switch (item.getItemId()) {
	        case R.id.menu_polling:
	        	PocketFahrschuleListActivity.showPollingDialog(this, mSubGroup, R.array.polling_choices_2);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }

//	@Override
//	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//		QuestionModel.createModelsForQuestions(((QuestionAdapter) getListAdapter()).getItems());
//		Intent i = new Intent(this, QuestionSheet.class);
//		i.putExtra(QuestionSheet.EXTRA_INDEX, arg2);
//		startActivityForResult(i, 0);
//	}
	
}
