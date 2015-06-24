package de.freenet.pocketfahrschulelite;

import de.freenet.pocketfahrschulelite.adapters.FormulasAdapter;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.TextView;

public class Formulas extends PocketFahrschuleActivity implements OnItemSelectedListener {

	private Gallery mGallery;
	private TextView mTitleTextView;
	private TextView mDescriptionTextView;
	private ImageButton mLeftArrowButton;
	private ImageButton mRightArrowButton;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.formulas);
        
        mLeftArrowButton = (ImageButton) findViewById(R.id.formulasSheetLeftImageButton);
        mRightArrowButton = (ImageButton) findViewById(R.id.formulasSheetRightImageButton);
        
        mTitleTextView = (TextView) findViewById(R.id.formulasTitleTextView);
        mDescriptionTextView = (TextView) findViewById(R.id.formulasDescTextView);
        
        mGallery = (Gallery) findViewById(R.id.formulasGallery);
        mGallery.setAdapter(new FormulasAdapter(this));
        mGallery.setOnItemSelectedListener(this);
        mGallery.setSelection(FahrschulePreferences.getInstance().getFormulaIndex());
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	FahrschulePreferences.getInstance().setFormulaIndex(mGallery.getSelectedItemPosition());
    }
    
    public void changeFormula(View v) {
    	switch (v.getId()) {
	    	case R.id.formulasSheetLeftImageButton:
	    		if (mGallery.getSelectedItemPosition() == 0)
	    			return;
	    		
	    		mGallery.onScroll(null, null, -50.0f, 0.0f);
				mGallery.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null);
	    		break;
	    	case R.id.formulasSheetRightImageButton:
	    		if (mGallery.getSelectedItemPosition() == mGallery.getCount() - 1)
	    			return;
	    		
	    		mGallery.onScroll(null, null, 50.0f, 0.0f);
				mGallery.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
	    		break;
    	}
    }

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		FormulasAdapter adapter = ((FormulasAdapter) mGallery.getAdapter());
		mTitleTextView.setText(adapter.getItem(arg2));
		mDescriptionTextView.setText(adapter.getItemDescription(arg2));
		
		setTitle(String.format("%s | %d / %d", getString(R.string.formulary), arg2 + 1, mGallery.getCount()));
		
		mLeftArrowButton.setEnabled(arg2 != 0);
    	mRightArrowButton.setEnabled(arg2 != mGallery.getCount() - 1);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) { }
    
    
}