package de.freenet.pocketfahrschulelite.widget;

import java.util.ArrayList;
import java.util.Date;

import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.objects.BarChartItem;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;

/**
 * This view draws horizontal bars for the exam view.
 */
public class BarChartView extends View {
	
	private int mPassLimit;
	private int mHighestPoints;
	private ArrayList<BarChartItem> mItems;
	private Resources mResources;
	
	public BarChartView(Context context) {
		super(context);
		initBarChartView(context);
	}

	public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBarChartView(context);
    }

    public BarChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initBarChartView(context);
    }
    
    private void initBarChartView(Context context) {
    	mPassLimit = 10;
    	mHighestPoints = mPassLimit * 2;
    	mItems = new ArrayList<BarChartItem>();
    	mResources = context.getResources();
    }
    
    /**
     * Sets the maximal number of points a user can reach to pass the exam.
     * This value will only be used to calculate the minimum max value that the bar can represent. 
     * @param points the number of points.
     */
    public void setPassLimit(int points) {
    	mPassLimit = points;
    	mHighestPoints = mPassLimit * 2;
    }
    
    /**
     * @return the number of bar items added to the view.
     */
    public int size() {
    	return mItems.size();
    }
    
    /**
     * Clears the view from all bar items
     */
    public void clearValues() {
    	mItems.clear();
    	mHighestPoints = mPassLimit * 2;
    }
    
    /**
     * Adds a new bar to the bar view
     * @param point the bar value
     * @param passed true if the bar should be green, red otherwise
     * @param date the date connected to the value. Will be shown next to the bar.
     * @return always true
     */
    public boolean addValues(int point, boolean passed, Date date) {
    	mHighestPoints = point > mHighestPoints ? point : mHighestPoints;
    	return mItems.add(new BarChartItem(point, passed, date));
    }
    
    @Override
    protected void onDraw (Canvas canvas) {
    	final float scale = mResources.getDisplayMetrics().density;
    	Drawable redBar = mResources.getDrawable(R.drawable.red_bar);
    	Drawable greenBar = mResources.getDrawable(R.drawable.green_bar);
    	Drawable thumb = mResources.getDrawable(R.drawable.daumen);
    	
    	Paint dateTextPaint = new Paint();
    	dateTextPaint.setTextSize(14.0f * scale);
    	dateTextPaint.setTypeface(Typeface.DEFAULT);
    	dateTextPaint.setColor(Color.parseColor("#D6DCE8"));
    	dateTextPaint.setAntiAlias(true);
    	
    	Paint pointsTextPaint = new Paint();
    	pointsTextPaint.setTextSize(15.0f * scale);
    	pointsTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
    	pointsTextPaint.setColor(Color.parseColor("#9CB3E0"));
    	pointsTextPaint.setAntiAlias(true);
    	
    	float delta_x = (172.0f / (float) mHighestPoints) * scale;
    	
    	for (int i = 0;i < mItems.size();i++) {
    		BarChartItem item = mItems.get(i);

        	float y = (14.0f + 24.0f * i) * scale;
        	int y_bar = (int) ((4.0f + 24.0f * i) * scale);
        	int left = (int) (74 * scale);
        	int right = left + (int) (delta_x * item.value);
        	
        	// Draw date
    		canvas.drawText(DateFormat.format("dd. MMM yy", item.date).toString(), 0.0f, y, dateTextPaint);
    		
    		// Draw bar
    		if (item.value == 0) {
    			y_bar -= 2 * scale;
    			thumb.setBounds(left, y_bar, left + (int) (12 * scale), y_bar + (int) (15 * scale));
    			thumb.draw(canvas);
    			right = left + (int) (15 * scale);
    		}
    		else if (item.passed) {
    			greenBar.setBounds(left, y_bar, left + (int) (delta_x * item.value), y_bar + (int) (12 * scale));
    			greenBar.draw(canvas);
    		}
    		else {
    			redBar.setBounds(left, y_bar, left + (int) (delta_x * item.value), y_bar + (int) (12 * scale));
            	redBar.draw(canvas);
    		}
        	
        	// Draw points
        	canvas.drawText(String.valueOf(item.value), right + 10 * scale, y, pointsTextPaint);
    	}
    }

}
