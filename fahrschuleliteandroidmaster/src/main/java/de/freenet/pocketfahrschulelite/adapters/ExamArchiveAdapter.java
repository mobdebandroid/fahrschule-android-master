package de.freenet.pocketfahrschulelite.adapters;

import java.util.List;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.objects.ExamStatistic;

public class ExamArchiveAdapter extends FahrschuleAdapter<ExamStatistic> {

	public ExamArchiveAdapter(Context context, List<ExamStatistic> objects) {
		super(context);
		clearAndSetObject(objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;
		if (v == null) {
			LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = li.inflate(R.layout.exam_archive_list_item, null);
		}
		
		ExamStatistic examStat = getItem(position);
		if (examStat != null) {
			ImageView iv = (ImageView) v.findViewById(android.R.id.icon);
			if (examStat.passed) {
				iv.setImageResource(R.drawable.icon_richtig);
			}
			else {
				iv.setImageResource(R.drawable.icon_falsch);
			}
			
			TextView tv = (TextView) v.findViewById(android.R.id.text1);
			tv.setText(String.format("%s - %d %s", DateFormat.format("dd. MMM yyyy kk:mm", examStat.date).toString(), examStat.points, mContext.getString(R.string.points)));
		}
		
		return v;
	}
}
