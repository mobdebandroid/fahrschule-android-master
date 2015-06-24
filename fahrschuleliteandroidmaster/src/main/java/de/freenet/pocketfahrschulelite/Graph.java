package de.freenet.pocketfahrschulelite;

import de.freenet.pocketfahrschulelite.adapters.GraphAdapter;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.widget.GraphCellView;
import de.freenet.pocketfahrschulelite.widget.HorizontalListView;
import de.freenet.pocketfahrschulelite.widget.HorizontalListView.GraphListener;
import de.freenet.view.OrientationListener;
import de.freenet.view.OrientationManager;
import de.freenet.view.OrientationManager.Orientation;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class Graph extends Activity implements OrientationListener, GraphListener {
	
	private OrientationManager mOrientationManager;
	private HorizontalListView mHorizontalListView;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.graph);
        
        mOrientationManager = new OrientationManager(this);
        
        mHorizontalListView = (HorizontalListView) findViewById(android.R.id.list);
        mHorizontalListView.setGraphListener(this);
        
        FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(this);
        mHorizontalListView.setAdapter(new GraphAdapter(this, db.getExamStatistics(-1)));
        db.close();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mOrientationManager.startListening(this);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	mOrientationManager.stopListening();
    }

	@Override
	public void onOrientationChange(Orientation orientation) {
		if (orientation.isPortrait()) {
			finish();
			overridePendingTransition(0, 0);
		}
	}

	@Override
	public void viewAdded(View view) {
		animateViews();
	}

	@Override
	public void viewRemoved(View view) {
		animateViews();
	}
	
	private void animateViews() {
		
		GraphAdapter adapter = (GraphAdapter) mHorizontalListView.getAdapter();
		
		int points = adapter.getPassLimit() * 2;
		for (int i = 0;i < mHorizontalListView.getChildCount();i++) {
			GraphCellView cell = (GraphCellView) mHorizontalListView.getChildAt(i);
			int currentPoint = Integer.valueOf(cell.getPointsTextView().getText().toString());
			if (currentPoint > points)
				points = currentPoint;
		}
		
		if (adapter.getHighestPoint() != points) {
			adapter.setHighestPoints(points);
			
			for (int i = 0;i < mHorizontalListView.getChildCount();i++) {
				GraphCellView cell = (GraphCellView) mHorizontalListView.getChildAt(i);
				adapter.startTranslateAnimation(cell);
			}
		}
	}
}
