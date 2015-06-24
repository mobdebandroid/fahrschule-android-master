package de.freenet.pocketfahrschulelite;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import de.freenet.pocketfahrschulelite.app.FahrschuleApplication;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import de.freenet.pocketfahrschulelite.main_fragments.ExamFragment;
import de.freenet.pocketfahrschulelite.main_fragments.ExtrasFragment;
import de.freenet.pocketfahrschulelite.main_fragments.LearningFragment;
import de.freenet.pocketfahrschulelite.objects.ExamStatistic.ExamState;
import de.freenet.pocketfahrschulelite.sliding.SlidingTabLayout;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainNavigation extends ActionBarActivity implements OnSharedPreferenceChangeListener {

	private TextView mExamIndicationBadgeTextView;
	private String HOCKEY_BETA_APP_ID = "87b41fa4d74997be6a2cedd8225e197e";
	private String HOCKEY_LIVE_APP_ID = "dff4b6e29f8a6d33f1b8eec48a12e973";

	//	Toolbar with new tabs
    private Toolbar toolbar;
	private SlidingTabLayout mSlidingTabLayout;
	private ViewPager mViewPager;

	static final String LOG_TAG = "SlidingTabsBasicFragment";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);	-	old

        setContentView(R.layout.main_navigation);

		//	init toolbar
        toolbar = (Toolbar) findViewById(R.id.app_bar);
		setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

		//	init viewpager and sliding tabs
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mViewPager.setAdapter(new FarcshPagerAdapter(getSupportFragmentManager()));
		mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
		mSlidingTabLayout.setViewPager(mViewPager);

//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);	-	old
        
        // Removes the ugly fading edge
        FrameLayout topFrame = (FrameLayout) findViewById(android.R.id.content);
        topFrame.setForeground(null);
        
        // Setup the tab host
//        TabHost tabHost = this.getComponentName().getTabHost();  // The activity TabHost
//        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
//        Intent intent;  // Reusable Intent for each tab
//        View tabIndicatorView; // Reusable View for each tab
//
//        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//        intent = new Intent().setClass(this, Learning.class);
//        tabIndicatorView = inflater.inflate(R.layout.tab_bar_indicator, null);
//        ((ImageView)tabIndicatorView.findViewById(R.id.indicatorIcon)).setImageResource(R.drawable.ic_menu_lernen);
//        ((TextView)tabIndicatorView.findViewById(R.id.indicatorTextView)).setText(R.string.learning);
//        spec = tabHost.newTabSpec("learning").setIndicator(tabIndicatorView).setContent(intent);
//        tabHost.addTab(spec);
//
//        intent = new Intent().setClass(this, Exam.class);
//        tabIndicatorView = inflater.inflate(R.layout.tab_bar_indicator, null);
//
//        mExamIndicationBadgeTextView = (TextView) tabIndicatorView.findViewById(R.id.indicatiorBadgeTextView);
//
//        FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
//        if (db.countExams(ExamState.CANCELED_EXAM) > 0) {
//        	mExamIndicationBadgeTextView.setVisibility(View.VISIBLE);
//        }
//        db.close();
//
//        ((ImageView)tabIndicatorView.findViewById(R.id.indicatorIcon)).setImageResource(R.drawable.ic_menu_pruefung);
//        ((TextView)tabIndicatorView.findViewById(R.id.indicatorTextView)).setText(R.string.exam);
//        spec = tabHost.newTabSpec("exam").setIndicator(tabIndicatorView).setContent(intent);
//        tabHost.addTab(spec);
//
//        intent = new Intent().setClass(this, Extras.class);
//        tabIndicatorView = inflater.inflate(R.layout.tab_bar_indicator, null);
//        ((ImageView)tabIndicatorView.findViewById(R.id.indicatorIcon)).setImageResource(R.drawable.ic_menu_extras);
//        ((TextView)tabIndicatorView.findViewById(R.id.indicatorTextView)).setText(R.string.extras);
//        spec = tabHost.newTabSpec("extras").setIndicator(tabIndicatorView).setContent(intent);
//        tabHost.addTab(spec);
//
//        intent = new Intent().setClass(this, Settings.class);
//        tabIndicatorView = inflater.inflate(R.layout.tab_bar_indicator, null);
//        ((ImageView)tabIndicatorView.findViewById(R.id.indicatorIcon)).setImageResource(R.drawable.ic_menu_einstellungen);
//        ((TextView)tabIndicatorView.findViewById(R.id.indicatorTextView)).setText(R.string.settings);
//        spec = tabHost.newTabSpec("settings").setIndicator(tabIndicatorView).setContent(intent);
//        tabHost.addTab(spec);
//
//        tabHost.setCurrentTab(0);
//
        FahrschulePreferences.getInstance().registerOnSharedPreferenceChangeListener(this);

        // Hockey implementation
        if (FahrschuleApplication.DEBUG) {
        	UpdateManager.register(this, HOCKEY_BETA_APP_ID);
        }
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	// Hockey crash reporting
    	CrashManager.register(this, FahrschuleApplication.DEBUG ? HOCKEY_BETA_APP_ID : HOCKEY_LIVE_APP_ID);
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("licenseClass") || key.startsWith("teachingType")) {
			FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
			if (db.countExams(ExamState.CANCELED_EXAM) > 0) {
	        	mExamIndicationBadgeTextView.setVisibility(View.VISIBLE);
	        }
	        else {
	        	mExamIndicationBadgeTextView.setVisibility(View.GONE);
	        }
			db.close();
		}
	}

	/**
	 * Adapter for set fragments in view pager
	 */
	public class FarcshPagerAdapter extends FragmentStatePagerAdapter {
		private static final int ITEMS = 4;	//	-	Counts of tabs

		public FarcshPagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		/**
		 * Counts of tabs
		 * @return ITEMS
		 */
		@Override
		public int getCount() {
			return ITEMS;
		}

		/**
		 * Get page title of tab
		 * @param position of tabs
		 * @return title
		 */
		@Override
		public CharSequence getPageTitle(int position) {
			String title = null;
			switch (position) {
				case 0:			//	Lernen
					title = getResources().getString(R.string.learning);
					break;
				case 1:			//	Pr?fung
					title = getResources().getString(R.string.exam);
					break;
				case 2:			//	Extras
					title = getResources().getString(R.string.extras);
					break;
				case 3:			//	Einstellungen
					title = getResources().getString(R.string.settings);
					break;
			}
			return title;
		}

		/**
		 * Get fragment from view pager
		 * @param position tab
		 * @return fragment
		 */
		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			switch (position) {
				case 0:			//	Lerner
					fragment = new LearningFragment();
					break;
				case 1:			//	Pr?fung
					fragment = new ExamFragment();
					break;
				case 2:			//	Extras
					fragment = new ExtrasFragment();
					break;
				case 3:			//	Einstellungen
					fragment = new LearningFragment();
					break;
			}

 			return fragment;
		}
	}

}



