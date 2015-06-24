package de.freenet.pocketfahrschulelite.objects;

import java.io.Serializable;

import android.database.Cursor;

public class Answer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2295305352219189562L;
	
	public int id;
	public boolean correct;
	public int question_id;
	public String text;
	public float number;
	
	public Answer() {
		super();
		this.id = 0;
		this.correct = false;
		this.question_id = 0;
		this.text = "";
		this.number = 0.0f;
	}
	
	public Answer(Cursor c) {
		this();
		this.id = c.getInt(c.getColumnIndex("Z_PK"));
		this.correct = c.getInt(c.getColumnIndex("ZCORRECT")) == 1;
		this.question_id = c.getInt(c.getColumnIndex("ZWHATQUESTION"));
		this.text = c.getString(c.getColumnIndex("ZTEXT"));
		this.number = c.getFloat(c.getColumnIndex("ZCORRECTNUMBER"));
	}
}
