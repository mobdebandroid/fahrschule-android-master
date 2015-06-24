package de.freenet.pocketfahrschulelite.adapters;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.classes.Utils;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LicenseClassAdapter extends BaseAdapter {
	
	private Context mContext;
	private JSONArray mItems;
	private float mScale;
	private ColorStateList mDarkBlueColorStateList;
	private ColorStateList mBlackColorStateList;
	private ColorStateList mGrayColorStateList;
	
	public LicenseClassAdapter(Context context) {
		mContext = context;
		try {
			mItems = new JSONArray(Utils.getContentFromFile(context.getAssets().open("license_classes.json")));
		} catch (JSONException e) {
			mItems = new JSONArray();
		} catch (IOException e) {
			mItems = new JSONArray();
		}
		
		mScale = mContext.getResources().getDisplayMetrics().density;
		
		try {
			mDarkBlueColorStateList = ColorStateList.createFromXml(mContext.getResources(), mContext.getResources().getXml(R.color.textcolor_darkblue_white));
			mBlackColorStateList = ColorStateList.createFromXml(mContext.getResources(), mContext.getResources().getXml(R.color.textcolor_black_white));
			mGrayColorStateList = ColorStateList.createFromXml(mContext.getResources(), mContext.getResources().getXml(R.color.textcolor_gray_white));
		} catch (NotFoundException e) {
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}
	}

	@Override
	public int getCount() {
		return mItems.length();
	}

	@Override
	public JSONObject getItem(int position) {
		return mItems.optJSONObject(position);
	}

	@Override
	public long getItemId(int position) {
		return mItems.optJSONObject(position) != null ? Long.valueOf(mItems.optJSONObject(position).hashCode()) : 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;
		if (v == null) {
			LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = li.inflate(R.layout.settings_list_license_class_item, null);
			
			LinearLayout ll = (LinearLayout) v.findViewById(android.R.id.widget_frame);
			TextView tv = new TextView(mContext);
			tv.setMaxWidth((int)(30 * mScale));
			tv.setMinWidth((int)(30 * mScale));
			tv.setId(android.R.id.text1);
			tv.setTypeface(Typeface.DEFAULT_BOLD);
			tv.setTextSize(17.0f);
			
			ImageView iv = new ImageView(mContext);
			iv.setId(android.R.id.icon);
			iv.setImageResource(R.drawable.haken_klassen);
			
			ll.addView(tv);
			ll.addView(iv);
		}
		
		JSONObject object = mItems.optJSONObject(position);
		if (object != null) {
			TextView tv1 = (TextView) v.findViewById(android.R.id.title);
			tv1.setText(object.optString("title"));
			
			TextView tv2 = (TextView) v.findViewById(android.R.id.summary);
			tv2.setText(object.optString("desc"));
			
			TextView tv3 = (TextView) v.findViewById(android.R.id.text1);
			String licenseClassStr = object.optString("class");
			tv3.setTextSize(licenseClassStr.equals("MOFA") ? 10.0f : 17.0f);
			tv3.setText(licenseClassStr);
			
			ImageView iv = (ImageView) v.findViewById(R.id.settingsImageView);
			int resId = mContext.getResources().getIdentifier(object.optString("image"), "drawable", mContext.getPackageName());
			iv.setImageResource(resId);
			
			RelativeLayout rl = (RelativeLayout) v.findViewById(R.id.linearLayout1);
			if (licenseClassStr.equals("B")) {
				tv1.setTextColor(mBlackColorStateList);
				tv2.setTextColor(mGrayColorStateList);
				tv3.setTextColor(mDarkBlueColorStateList);
				rl.setBackgroundResource(R.drawable.list_cell_background);
				iv.setAlpha(255);
			}
			else {
				tv1.setTextColor(Color.LTGRAY);
				tv2.setTextColor(Color.LTGRAY);
				tv3.setTextColor(Color.LTGRAY);
				rl.setBackgroundResource(R.drawable.bg_fragenkatalog);
				iv.setAlpha(153);
			}
			
			iv = (ImageView) v.findViewById(android.R.id.icon);
			iv.setVisibility(FahrschulePreferences.getInstance().getCurrentLicenseClassString().equals(String.valueOf(object.optInt("id"))) ? View.VISIBLE : View.GONE);
		}
		
		return v;
	}

}
