package de.freenet.pocketfahrschulelite.widget;

import java.util.HashMap;

import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.objects.LearnStatistic.StatisticState;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class KanisterView extends View {
	
	private HashMap<StatisticState, Integer> mValues;
	private int mTotal;
	private Resources mResources;

	public KanisterView(Context context) {
		super(context);
		initKanisterView(context);
	}

	public KanisterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initKanisterView(context);
    }

    public KanisterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initKanisterView(context);
    }
    
    private void initKanisterView(Context context) {
    	mValues = new HashMap<StatisticState, Integer>();
    	mResources = context.getResources();
    	mTotal = 0;
    }
    
    public Integer addValues(StatisticState state, int value) {
    	mTotal += value;
    	return mValues.put(state, value);
    }
    
    public Integer removeValue(StatisticState state) {
    	Integer value = mValues.remove(state);
    	
    	if (value != null)
    		mTotal -= value;
    	
    	return value; 
    }
    
    public void clearValues() {
    	mValues.clear();
    	mTotal = 0;
    }
    
    @Override
    protected void onDraw (Canvas canvas) {
    	
    	if (mTotal <= 0) return;
    	
    	final float scale = mResources.getDisplayMetrics().density;
    	final int height = (int)(146 * scale);
    	
    	final float correct = mValues.containsKey(StatisticState.CORRECT_ANSWERED) ? (float)mValues.get(StatisticState.CORRECT_ANSWERED) / (float)mTotal : 0.0f;
    	final float faulty = mValues.containsKey(StatisticState.FAULTY_ANSWERED) ? (float)mValues.get(StatisticState.FAULTY_ANSWERED) / (float)mTotal : 0.0f;
    	
    	int currentHeight = (int)(scale * 80) + (int)Math.ceil(height * (1.0f - correct - faulty));
    	
    	// Draw unanswered part of canister
    	Rect rect = new Rect(0, 0, (int)(scale * 320), currentHeight);
    	Bitmap bmp = BitmapFactory.decodeResource(mResources, R.drawable.kanister_blau);
    	canvas.drawBitmap(bmp, rect, rect, null);
    	
    	// Draw correct answered part of canister
    	if (correct > 0.0f) {
    		rect = new Rect(0, currentHeight, (int)(scale * 320), currentHeight + (int)Math.ceil(height * correct));
    		canvas.drawBitmap(BitmapFactory.decodeResource(mResources, R.drawable.kanister_gruen), rect, rect, null);
    		
    		currentHeight += (int)Math.ceil(height * correct);
    	}
    	
    	// Draw faulty answered part of canister
    	if (faulty > 0.0f) {
    		rect = new Rect(0, currentHeight, (int)(scale * 320), currentHeight + (int)Math.ceil(height * faulty));
    		canvas.drawBitmap(BitmapFactory.decodeResource(mResources, R.drawable.kanister_rot), rect, rect, null);
    		
    		currentHeight += (int)Math.ceil(height * faulty);
    	}
    	
    	// Draw bottom of canister
    	rect = new Rect(0, currentHeight, (int)(scale * 320), (int)(scale * 250));
		canvas.drawBitmap(bmp, rect, rect, null);
		bmp.recycle();
		bmp = null;
		
		/**
		 * Draw lines must be done after canister is drawn so the lines are shown over the canister
		 */
		
		Paint linePaint = new Paint();
    	linePaint.setColor(Color.WHITE);
    	linePaint.setAntiAlias(true);
    	
    	Paint textPaint = new Paint();
    	textPaint.setTextSize(26.0f * scale);
    	textPaint.setTypeface(Typeface.DEFAULT_BOLD);
    	textPaint.setColor(Color.parseColor("#9cb3e0"));
    	textPaint.setAntiAlias(true);
    	
		currentHeight = (int)(scale * 80) + (int)Math.ceil(height * (1.0f - correct - faulty));
		float dy = 20.0f * scale;
		
		int correctNum = mValues.containsKey(StatisticState.CORRECT_ANSWERED) ? mValues.get(StatisticState.CORRECT_ANSWERED) : 0;
		int faultyNum = mValues.containsKey(StatisticState.FAULTY_ANSWERED) ? mValues.get(StatisticState.FAULTY_ANSWERED) : 0;
		
		// Draw line with text from unanswered part of canister
    	float y = (int)(scale * 80) + (int)Math.ceil((height * (1.0f - correct - faulty)) / 2.0f);
		canvas.drawLine(225.0f * scale, y, 270.0f * scale, y - dy, linePaint);
		canvas.drawLine(270.0f * scale, y - dy, 310.0f * scale, y - dy, linePaint);
		canvas.drawText(String.valueOf(mTotal - correctNum - faultyNum), 270.0f * scale, y - dy - 2.0f * scale, textPaint);
		
		if (correctNum > 0) {
			// Draw line with text from correct answered part of canister
			y = currentHeight + (int)Math.ceil((height * correct) / 2.0f);
			canvas.drawLine(225.0f * scale, y, 270.0f * scale, y + dy, linePaint);
			canvas.drawLine(270.0f * scale, y + dy, 310.0f * scale, y + dy, linePaint);
			canvas.drawText(String.valueOf(correctNum), 270.0f * scale, y + dy - 2.0f * scale, textPaint);
			currentHeight += (int)Math.ceil(height * correct);
			
			float startX = (270.0f + 13.0f * String.valueOf(correctNum).length()) * scale;
			RectF dst = new RectF(startX, y - 10.0f * scale, startX + 10.0f * scale, y + 2.0f * scale);
			canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon_richtig), null, dst, null);
		}
		
		if (faultyNum > 0) {
			// Draw line with text from faulty answered part of canister
			y = currentHeight + (int)Math.ceil((height * faulty) / 2.0f);
			canvas.drawLine(120.0f * scale, y, 55.0f * scale, y - dy, linePaint);
			canvas.drawLine(55.0f * scale, y - dy, 15.0f * scale, y - dy, linePaint);
			canvas.drawText(String.valueOf(faultyNum), 15.0f * scale, y - dy - 2.0f * scale, textPaint);
			
			float startX = (18.0f + 13.0f * String.valueOf(faultyNum).length()) * scale;
			RectF dst = new RectF(startX, y - 2 * dy - 10.0f * scale, startX + 10.0f * scale, y - 2 * dy + 2.0f * scale);
			canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon_falsch), null, dst, null);
		}
    }
}
