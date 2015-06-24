package de.freenet.pocketfahrschulelite.adapters;

import java.util.List;

import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.objects.Question;
import de.freenet.pocketfahrschulelite.widget.MainGroupCellView;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

public class QuestionAdapter extends FahrschuleAdapter<Question> {

	public QuestionAdapter(Context context, List<Question> objects) {
		super(context);
		clearAndSetObject(objects);
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
		
		Question object = getItem(position);
		if (object != null) {
			v.getTextView().setTypeface(null, Typeface.NORMAL);
			CharSequence text = TextUtils.ellipsize(object.text, new TextPaint(), 410.0f, TextUtils.TruncateAt.END);
			v.getTextView().setText(Html.fromHtml(String.format("<html><body><b>%s</b> %s</body></html>", object.number, text)));
			
			switch (object.state) {
				case CORRECT_ANSWERED:
					v.setLearningStatisticImageViewHidden(false);
					v.setLearningStatisticImage(R.drawable.icon_richtig);
					break;
				case FAULTY_ANSWERED:
					v.setLearningStatisticImageViewHidden(false);
					v.setLearningStatisticImage(R.drawable.icon_falsch);
					break;
				default:
					v.setLearningStatisticImageViewHidden(true);
			}
		}
		
		return v;
	}
}
