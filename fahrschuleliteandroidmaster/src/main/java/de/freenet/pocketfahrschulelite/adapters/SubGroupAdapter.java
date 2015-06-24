package de.freenet.pocketfahrschulelite.adapters;

import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.objects.SubGroup;
import de.freenet.pocketfahrschulelite.widget.MainGroupCellView;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

public class SubGroupAdapter extends FahrschuleAdapter<SubGroup> {
	
	private ColorStateList mBlackColorStateList;

	public SubGroupAdapter(Context context, List<SubGroup> objects) {
		super(context);
		clearAndSetObject(objects);
		
		try {
			mBlackColorStateList = ColorStateList.createFromXml(mContext.getResources(), mContext.getResources().getXml(R.color.textcolor_black_white));
		} catch (NotFoundException e) {
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		MainGroupCellView v = null;
		try {
			v = (MainGroupCellView) convertView;
		} catch (ClassCastException e) { }
		
		if (v == null) {
			v = new MainGroupCellView(mContext);
			v.setImageHidden(true);
			v.setSectionHeaderHidden(true);
			v.setProgressViewHidden(true);
		}
		
		SubGroup object = getItem(position);
		if (object != null) {
			v.setText(object.number + " " + object.name);
			
			if (position == 0) {
				v.getTextView().setTextColor(mBlackColorStateList);
				v.setCellBackground(R.drawable.list_selector); //list_cell_background);
			}
			else {
				v.getTextView().setTextColor(Color.LTGRAY);
				v.setCellBackground(R.drawable.list_selector); //bg_fragenkatalog);
			}
		}
		
		return v;
	}
}
