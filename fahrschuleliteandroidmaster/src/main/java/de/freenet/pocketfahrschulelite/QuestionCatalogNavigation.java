package de.freenet.pocketfahrschulelite;

import de.freenet.pocketfahrschulelite.classes.ApplicationStoreHelper;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.main_fragments.ExamFragment;
import de.freenet.pocketfahrschulelite.main_fragments.LearningFragment;
import de.freenet.pocketfahrschulelite.question_fragments.QuestionCatalogFragment;
import de.freenet.pocketfahrschulelite.question_fragments.TaggedQuestionFragment;
import de.freenet.pocketfahrschulelite.sliding.SlidingTabLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class QuestionCatalogNavigation extends ActionBarActivity {


    //	Toolbar with new tabs
    private Toolbar toolbar;
    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);    -   old
        setContentView(R.layout.main_navigation);

        //	init toolbar
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //	init viewpager and sliding tabs
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new QuestPagerAdapter(getSupportFragmentManager()));
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);

//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);    -   old
        
        // Set title
//        TextView tv = (TextView) findViewById(R.id.titleTextView);    -   old
//    	tv.setText(R.string.question_catalog);
        
        // Removes the ugly fading edge
        FrameLayout topFrame = (FrameLayout) findViewById(android.R.id.content);
        topFrame.setForeground(null);
        
        // Setup the tab host
//        TabHost tabHost = getTabHost();  // The activity TabHost
//        TabHost.TabSpec spec;  // Reusable TabSpec for each tab
//        Intent intent;  // Reusable Intent for each tab
//        View tabIndicatorView; // Reusable View for each tab
//
//        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//        intent = new Intent().setClass(this, QuestionCatalog.class);
//        tabIndicatorView = inflater.inflate(R.layout.tab_bar_indicator, null);
//        ((ImageView)tabIndicatorView.findViewById(R.id.indicatorIcon)).setImageResource(R.drawable.ic_menu_katalog);
//        ((TextView)tabIndicatorView.findViewById(R.id.indicatorTextView)).setText(R.string.question_catalog);
//        spec = tabHost.newTabSpec("catalog").setIndicator(tabIndicatorView).setContent(intent);
//        tabHost.addTab(spec);
//
//        intent = new Intent().setClass(this, TaggedQuestions.class);
//        tabIndicatorView = inflater.inflate(R.layout.tab_bar_indicator, null);
//        ((ImageView)tabIndicatorView.findViewById(R.id.indicatorIcon)).setImageResource(R.drawable.ic_menu_markiert);
//        ((TextView)tabIndicatorView.findViewById(R.id.indicatorTextView)).setText(R.string.tagged_questions);
//        spec = tabHost.newTabSpec("tagged").setIndicator(tabIndicatorView).setContent(intent);
//        tabHost.addTab(spec);
//
//        tabHost.setCurrentTab(0);
//
//        setResult(RESULT_OK);
        showBanner();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	// Sets "Markierte Fragen" as active or inactive depending on the amount of tagged questions. 
    	FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
        boolean hasTaggedQuestions = db.countTaggedQuestions() != 0;
        db.close();
        
//        getTabWidget().getChildAt(1).findViewById(R.id.indicatorIcon).setEnabled(hasTaggedQuestions);
//        getTabWidget().getChildAt(1).setClickable(hasTaggedQuestions);
//        getTabWidget().getChildAt(1).setFocusable(hasTaggedQuestions);
//
//        if (!hasTaggedQuestions && getTabHost().getCurrentTab() != 0) {
//        	getTabHost().setCurrentTab(0);
//        }
    }
    
    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event) {
    	
    	// Bind the magnifying glass button to show the search view
    	if (KeyEvent.KEYCODE_SEARCH == keyCode) {
    		Intent i = new Intent(this, SearchQuestion.class);
    		startActivity(i);
    		return true;
    	}
    	
    	return super.onKeyDown(keyCode, event);
    }
    
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
						
						LinearLayout layout = (LinearLayout) findViewById(R.id.mainNavigationLinearLayout);
						layout.getLayoutParams().height = layout.getHeight() - height;
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

    public class QuestPagerAdapter extends FragmentStatePagerAdapter {
        private static final int ITEMS = 2;

        public QuestPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return ITEMS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = null;
            switch (position) {
                case 0:			//	Fragenkatalog
                    title = getResources().getString(R.string.question_catalog);
                    break;
                case 1:			//	Markierte Fragen
                    title = getResources().getString(R.string.tagged_questions);
                    break;
            }
            return title;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:			//	Lerner
                    fragment = new QuestionCatalogFragment();
                    break;
                case 1:			//	Pr?fung
                    fragment = new TaggedQuestionFragment();
                    break;
            }

            return fragment;
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.learning, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        int id = item.getItemId();
//
//        switch (id) {
//            case R.id.menu_polling:
//                break;
//            case R.id.menu_search:
//                break;
//
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

}
