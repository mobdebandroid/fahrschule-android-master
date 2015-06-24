package de.freenet.pocketfahrschulelite.objects;

import android.database.Cursor;

public class ExamResultObject {

	public final int mainGroupId;
	public final String mainGroupName;
	public final int questionId;
	public final int points;
	public final boolean answeredCorrectly;
	
	public ExamResultObject(Cursor c, boolean answeredCorrectly) {
		this.mainGroupId = c.getInt(c.getColumnIndex("Z_PK"));
		this.mainGroupName = c.getString(c.getColumnIndex("ZNAME"));
		this.questionId = c.getInt(c.getColumnIndex("questionId"));
		this.points = c.getInt(c.getColumnIndex("ZPOINTS"));
		this.answeredCorrectly = answeredCorrectly;
	}
}
