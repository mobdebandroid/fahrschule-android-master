package de.freenet.pocketfahrschulelite.objects;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.freenet.pocketfahrschulelite.objects.LearnStatistic.StatisticState;

import android.database.Cursor;

public class Question implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2067662361972781071L;
	
	public enum Type { CHOICE, NUMBER };
	
	public int id;
	public String number;
	public String prefix;
	public String text;
	public String image;
	public int points;
	public Type type;
	public List<Answer> answers;
	public StatisticState state;
	public boolean deleted;
	
	public Question() {
		super();
		this.id = 0;
		this.number = "";
		this.prefix = "";
		this.text = "";
		this.image = "";
		this.points = 0;
		this.type = Type.CHOICE;
		this.answers = Arrays.asList(new Answer[0]);
		this.state = StatisticState.STATE_LESS;
		this.deleted = false;
	}
	
	public Question(Cursor c) {
		this();
		this.id = c.getInt(c.getColumnIndex("Z_PK"));
		this.number = c.getString(c.getColumnIndex("ZNUMBER"));
		this.prefix = c.getString(c.getColumnIndex("ZPREFIX"));
		this.text = c.getString(c.getColumnIndex("ZTEXT"));
		this.image = c.getString(c.getColumnIndex("ZIMAGE"));
		this.points = c.getInt(c.getColumnIndex("ZPOINTS"));
		this.type = c.getString(c.getColumnIndex("ZTYPE")).equals("choice") ? Type.CHOICE : Type.NUMBER;
		this.state = LearnStatistic.parseStatisticState(c.getInt(c.getColumnIndex("ZSTATE")));
		this.deleted = c.getInt(c.getColumnIndex("ZDELETED")) == 1;
	}
	
	public Question(Cursor c, List<Answer> answers) {
		this(c);
		this.answers = answers;
	}
	
	@Override
	public boolean equals(Object object) {
		
		if (object instanceof Question) {
			return ((Question) object).id == this.id;
		}
		
		return false;
	}
	
	public boolean hasAnsweredCorrectly(HashMap<Integer, String> givenAnswers) {
		
		boolean correct = true;
		if (answers.size() == 0) return !correct;
		
		if (this.type == Type.CHOICE) {
			for (Answer answer : answers) {
				if (givenAnswers.containsKey(answer.id) && !answer.correct || answer.correct && !givenAnswers.containsKey(answer.id)) {
					correct = false;
					break;
				}
			}
		}
		else if (this.type == Type.NUMBER) {
			
			Answer answer = answers.get(0);
			if (this.number.equals("11.11.1")) {
				if (givenAnswers.containsKey(answer.id) && givenAnswers.containsKey(-answer.id)) {
					String str = givenAnswers.get(answer.id);
					String str2 = givenAnswers.get(-answer.id);
					if (!String.valueOf(answer.number).equals(String.format("%s.%s", str, str2))) {
						correct = false;
					}
				}
				else {
					correct = false;
				}
			}
			else {
				if (givenAnswers.containsKey(answer.id)) {
					String str = givenAnswers.get(answer.id);
					str = str.contains(".") ? str : str + ".0";
					if (!String.valueOf(answer.number).equals(str)) {
						correct = false;
					}
				}
				else {
					correct = false;
				}
			}
		}
		
		return correct;
	}
}
