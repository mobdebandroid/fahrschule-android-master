package de.freenet.pocketfahrschulelite.adapters;

import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.objects.MainGroup;
import de.freenet.pocketfahrschulelite.objects.MainGroup.SectionType;
import de.freenet.pocketfahrschulelite.widget.MainGroupCellView;

public class MainGroupAdapter extends FahrschuleAdapter<MainGroup> {
	
	private ColorStateList mBlackColorStateList;
	
	public MainGroupAdapter(Context context, List<MainGroup> objects) {
		super(context);
		clearAndSetObject(objects);
		
		try {
			mBlackColorStateList = ColorStateList.createFromXml(mContext.getResources(), mContext.getResources().getXml(R.color.textcolor_black_white));
		} catch (NotFoundException e) {
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		MainGroupCellView v = null;
		try {
			v = (MainGroupCellView) convertView;
		} catch (ClassCastException e) { }
		
		if (v == null) {
			v = new MainGroupCellView(mContext);
		}
		
		MainGroup object = getItem(position);
		if (object != null) {
			v.setText(object.name);
			
			int resId = mContext.getResources().getIdentifier(object.imageFilename , "drawable", mContext.getPackageName());
			v.setImage(resId);
			v.setProgressBarProgress(object.questionsCorrect, object.questionsFaulty, object.questionsTotal);
			
			if (position == 0 || position == 1 || position == 5) {
				v.setProgressViewHidden(false);
				v.getTextView().setTextColor(mBlackColorStateList);
				v.getImageView().setImageAlpha(255);
//				v.setCellBackground(R.drawable.list_cell_background);
			}
			else {
				v.setProgressViewHidden(true);
				v.getTextView().setTextColor(Color.LTGRAY);
				v.getImageView().setImageAlpha(153);
//				v.setCellBackground(R.drawable.bg_fragenkatalog);
			}
			
			if (object.sectionType == SectionType.BASE_MATERIAL) {
				v.setSectionHeaderHidden(false);
				v.setSectionHeaderText(mContext.getString(R.string.base_material));
			}
			else if (object.sectionType == SectionType.ADDITIONAL_MATERIAL) {
				v.setSectionHeaderHidden(false);
				v.setSectionHeaderText(mContext.getString(R.string.additional_material));
			}
			else {
				v.setSectionHeaderHidden(true);
			}
		}
		
		return v;
	}
}
