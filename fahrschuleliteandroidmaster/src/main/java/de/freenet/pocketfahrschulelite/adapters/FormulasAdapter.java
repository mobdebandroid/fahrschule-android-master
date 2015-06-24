package de.freenet.pocketfahrschulelite.adapters;

import de.freenet.pocketfahrschulelite.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FormulasAdapter extends BaseAdapter {

	private String[] mFormulaTitles;
	private String[] mFormulaDescriptions;
	private LayoutInflater mLayoutInflater;
	
	public FormulasAdapter(Context context) {
		super();
		mFormulaTitles = context.getResources().getStringArray(R.array.formula_titles);
		mFormulaDescriptions = context.getResources().getStringArray(R.array.formula_descriptions);
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return mFormulaTitles.length;
	}

	@Override
	public String getItem(int position) {
		return mFormulaTitles[position];
	}
	
	public String getItemDescription(int position) {
		return mFormulaDescriptions[position];
	}

	@Override
	public long getItemId(int position) {
		return mFormulaTitles[position].hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = null;
		switch (position) {
			case 0:
				v = mLayoutInflater.inflate(R.layout.formula_1, null);
				break;
			case 1:
				v = mLayoutInflater.inflate(R.layout.formula_2, null);
				break;
			case 2:
				v = mLayoutInflater.inflate(R.layout.formula_3, null);
				break;
			case 3:
				v = mLayoutInflater.inflate(R.layout.formula_4, null);
				break;
			case 4:
				v = mLayoutInflater.inflate(R.layout.formula_5, null);
				break;
			case 5:
				v = mLayoutInflater.inflate(R.layout.formula_6, null);
				break;
			default:
				v = mLayoutInflater.inflate(R.layout.formula_1, null);
		}
		
		return v;
	}

}
