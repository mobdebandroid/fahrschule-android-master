package de.freenet.pocketfahrschulelite;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import de.freenet.pocketfahrschulelite.adapters.SubGroupAdapter;
import de.freenet.pocketfahrschulelite.classes.ApplicationStoreHelper;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.objects.MainGroup;

public class SubGroupList extends ActionBarActivity {//PocketFahrschuleListActivity {
	
	public static final String EXTRA_MAINGROUP_ID = "SubGroupList_Extra_MainGroup_Id";
	public static final String EXTRA_MAINGROUP_TITLE = "SubGroupList_Extra_MainGroup_Title";

	private MainGroup mMainGroup;
	private SubGroupAdapter subAdapter;
	private ListView lv;

	private Toolbar toolbar;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getIntent().getExtras() == null || !getIntent().getExtras().containsKey(EXTRA_MAINGROUP_ID))
        	finish();
        
        mMainGroup = new MainGroup();
        mMainGroup.id = getIntent().getExtras().getInt(EXTRA_MAINGROUP_ID);
        
        setContentView(R.layout.sub_group_list);

		toolbar = (Toolbar) findViewById(R.id.app_bar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setTitle(TextUtils.ellipsize(getIntent().getExtras().getString(EXTRA_MAINGROUP_TITLE), new TextPaint(), 180.0f, TextUtils.TruncateAt.END));
        
        FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
		lv = (ListView) findViewById(R.id.list);
		subAdapter = new SubGroupAdapter(this, db.getSubGroups(mMainGroup.id));
		lv.setAdapter(subAdapter);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (position != 0) return;

					Intent i = new Intent(SubGroupList.this, QuestionList.class);
					i.putExtra(QuestionList.EXTRA_SUBGROUP_ID, subAdapter.getItem(position).id);//(SubGroupAdapter) getListAdapter()).getItem(position).id);
					i.putExtra(QuestionList.EXTRA_SUBGROUP_TITLE, subAdapter.getItem(position).name);//((SubGroupAdapter) getListAdapter()).getItem(position).name);
					startActivity(i);
			}
		});

//        setListAdapter(new SubGroupAdapter(this, db.getSubGroups(mMainGroup.id)));
        db.close();
        
        showBanner();
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
	        	PocketFahrschuleListActivity.showPollingDialog(this, mMainGroup);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
//    @Override
//    protected void onListItem (ListView l, View v, int position, long id) {
//    	if (position != 0) return;
//
//    	Intent i = new Intent(this, QuestionList.class);
//    	i.putExtra(QuestionList.EXTRA_SUBGROUP_ID, subAdapter.getItem(position).id);//(SubGroupAdapter) getListAdapter()).getItem(position).id);
//    	i.putExtra(QuestionList.EXTRA_SUBGROUP_TITLE, subAdapter.getItem(position).name);//((SubGroupAdapter) getListAdapter()).getItem(position).name);
//    	startActivity(i);
//    }
    
    public void bannerClicked(View v) {
    	ApplicationStoreHelper.openFullVersionStorePage(this);
    }
    
    private void showBanner() {
    	new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				final Button btn = (Button) findViewById(R.id.bannerButton);
				
				TranslateAnimation anim = new TranslateAnimation(0.0f, 0.0f, 50.0f, 0.0f);
				anim.setDuration(250);
				anim.setFillAfter(true);
				anim.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationEnd(Animation animation) {
						int height = (int)(50 * getResources().getDisplayMetrics().density);
						
						RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(btn.getLayoutParams());
						lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
						lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
						lp.topMargin = height;
						btn.setLayoutParams(lp);
						
						lv.getLayoutParams().height = lv.getHeight() - height; //getListView().getLayoutParams().height = getListView().getHeight() - height;
					}

					@Override
					public void onAnimationRepeat(Animation animation) { }

					@Override
					public void onAnimationStart(Animation animation) {
						btn.setVisibility(View.VISIBLE);
					}
					
				});
				btn.startAnimation(anim);
			}
			
        }, 1000);
    }
}