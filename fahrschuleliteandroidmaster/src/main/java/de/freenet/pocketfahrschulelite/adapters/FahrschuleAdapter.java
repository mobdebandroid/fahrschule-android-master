package de.freenet.pocketfahrschulelite.adapters;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class FahrschuleAdapter<T> extends BaseAdapter {
	
	protected final Context mContext;
	protected LinkedList<T> mListItems;
	
	public FahrschuleAdapter(Context context) {
		super();
		mContext = context;
		mListItems = new LinkedList<T>();
	}

	@Override
	public int getCount() {
		return mListItems.size();
	}

	@Override
	public T getItem(int position) {
		if (position < 0) return null;
		
		return mListItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		if (position < 0) return -1;
		
		return mListItems.get(position).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return null;
	}
	
	public void clear() {
		mListItems.clear();
	}
	
	public void addFirst(T object) {
		mListItems.addFirst(object);
	}
	
	public void addLast(T object) {
		mListItems.addLast(object);
	}
	
	public void clearAndSetObject(List<T> objects) {
		mListItems.clear();
		mListItems.addAll(objects);
	}
	
	public LinkedList<T> getItems() {
		return mListItems;
	}
}
