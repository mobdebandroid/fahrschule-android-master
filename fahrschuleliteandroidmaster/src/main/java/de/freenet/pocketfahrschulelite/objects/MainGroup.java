package de.freenet.pocketfahrschulelite.objects;

import android.database.Cursor;

public class MainGroup {
	
	public enum SectionType {BASE_MATERIAL, ADDITIONAL_MATERIAL, NO_SECTION};

	public int id;
	public String name;
	public String imageFilename;
	public SectionType sectionType;
	public int questionsTotal;
	public int questionsFaulty;
	public int questionsCorrect;
	
	public MainGroup() {
		super();
		this.id = 0;
		this.name = "";
		this.imageFilename = "";
		this.sectionType = SectionType.NO_SECTION;
		this.questionsTotal = 0;
		this.questionsFaulty = 0;
		this.questionsCorrect = 0;
	}
	
	public MainGroup(Cursor c) {
		this();
		this.id = c.getInt(c.getColumnIndex("Z_PK"));
		this.name = c.getString(c.getColumnIndex("ZNAME"));
		this.imageFilename = c.getString(c.getColumnIndex("ZIMAGE"));
		this.questionsTotal = c.getInt(c.getColumnIndex("questionsTotal"));
		this.questionsFaulty = c.getInt(c.getColumnIndex("questionsFaulty"));
		this.questionsCorrect = c.getInt(c.getColumnIndex("questionsCorrect"));
	}
}
