package de.freenet.pocketfahrschulelite.objects;

import java.util.Date;

import de.freenet.pocketfahrschulelite.app.FahrschuleApplication;
import de.freenet.pocketfahrschulelite.classes.Utils;
import de.freenet.pocketfahrschulelite.content.FahrschuleDatabaseHelper;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences.LicenseClass;
import de.freenet.pocketfahrschulelite.content.FahrschulePreferences.TeachingType;

import android.database.Cursor;

public class ExamStatistic {
	
	public static final String TAG = "ExamStatistic";
	
	public int id;
	public int selectedQuestionIndex;
	public int secondsLeft;
	public int points;
	public LicenseClass licenseClass;
	public TeachingType teachingType;
	public ExamState state;
	public Date date;
	public boolean passed;
	public boolean showDate;
	
	public static enum ExamState {
		FINISHED_EXAM (0),
		CANCELED_EXAM (1),
		STATELESS_EXAM (2);
		
		private final int mId;
		private ExamState (int id) {
	        mId = id;
	    }
		
		public int getId() { return mId; } 
	}
	
	public ExamStatistic() {
		id = 0;
		selectedQuestionIndex = 0;
		secondsLeft = 0;
		points = 0;
		licenseClass = LicenseClass.B;
		teachingType = TeachingType.FIRST_TIME_LICENSE;
		state = ExamState.STATELESS_EXAM;
		date = new Date();
		passed = false;
		showDate = false;
	}
	
	public ExamStatistic(Cursor c) {
		this();
		id = c.getInt(c.getColumnIndex("Z_PK"));
		selectedQuestionIndex = c.getInt(c.getColumnIndex("ZINDEX"));
		secondsLeft = c.getInt(c.getColumnIndex("ZTIMELEFT"));
		points = c.getInt(c.getColumnIndex("ZFAULTYPOINTS"));
		passed = c.getInt(c.getColumnIndex("ZPASSED")) == 1;
		try {
			date = new Date(Long.valueOf(c.getString(c.getColumnIndex("ZDATE"))));
		}
		catch (NumberFormatException e) {
			date = new Date();
		}
		
		licenseClass = Utils.getLicenseClassFromId(c.getInt(c.getColumnIndex("ZLICENSECLASS")));
		
		switch(c.getInt(c.getColumnIndex("ZTEACHINGTYPE"))) {
			case 1:
				teachingType = TeachingType.FIRST_TIME_LICENSE;
				break;
			case 2:
				teachingType = TeachingType.ADDITIONAL_LICENSE;
				break;
			case 0:
			default:
				teachingType = TeachingType.FIRST_TIME_LICENSE;
		}
		
		switch(c.getInt(c.getColumnIndex("ZSTATE"))) {
			case 0:
				state = ExamState.FINISHED_EXAM;
				break;
			case 1:
				state = ExamState.CANCELED_EXAM;
				break;
			case 2:
			default:
				state = ExamState.STATELESS_EXAM;
		}
	}
	
	public void createModelsFromExam() {
		FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(FahrschuleApplication.getAppContext());
		db.createModelsFromExam(id, state == ExamState.FINISHED_EXAM);
		db.close();
	}
	
	/**
	 * Is needed because of the special case where you can fail a test with 10 points if you achieved those 10 points by answering two 5 point questions incorrectly.
	 * @return true if the user has passed the exam, false otherwise
	 */
	/*public boolean hasPassedExam() {
		
		boolean passed = true;
		
		JSONObject examSheet = null;
		try {
			examSheet = new JSONObject(Utils.getContentFromFile(FahrschuleApplication.getAppContext().getAssets().open("exam_sheet.json")));
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		} catch (JSONException e) {
			Log.e(TAG, "JSONException: " + e.getMessage());
		}
		
		if (examSheet == null) return false;
		
		try {
			int maxPoints = examSheet.getJSONObject(String.valueOf(licenseClass.getId())).getJSONObject(String.valueOf(teachingType.getId())).getInt("MaxPoints");
			
			if (points > maxPoints) {
				passed = false;
			}
			else if (points == maxPoints && points == 10) {
				
				FahrschuleDatabaseHelper db = new FahrschuleDatabaseHelper(FahrschuleApplication.getAppContext());
				db.createModelsFromExam(id, state == ExamState.FINISHED_EXAM);
				db.close();
				
				 int numFalse = 0;
				for (QuestionModel model : QuestionModel.getQuestionModels()) {
					if (model.question.points == 5 && !model.hasAnsweredCorrectly()) {
		                numFalse++;
		            }
				}
				
		        return numFalse != 2;
			}
			
		} catch (JSONException e) {
			// Can't find value in JSONObject. Default to not passed exam.
			passed = false;
		}
		
		return passed;
	}*/
}
