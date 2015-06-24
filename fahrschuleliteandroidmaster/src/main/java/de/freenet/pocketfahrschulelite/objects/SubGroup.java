package de.freenet.pocketfahrschulelite.objects;

import android.database.Cursor;

public class SubGroup {

	public int id;
	public String name;
	public String number;
	
	public SubGroup() {
		super();
		this.id = 0;
		this.name = "";
		this.number = "";
	}
	
	public SubGroup(Cursor c) {
		this();
		this.id = c.getInt(c.getColumnIndex("Z_PK"));
		this.name = c.getString(c.getColumnIndex("ZNAME"));
		this.number = c.getString(c.getColumnIndex("ZNUMBER"));
	}
}
