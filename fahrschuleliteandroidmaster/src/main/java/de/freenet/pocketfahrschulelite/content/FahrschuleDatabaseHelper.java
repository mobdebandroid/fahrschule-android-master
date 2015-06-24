package de.freenet.pocketfahrschulelite.content;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import de.freenet.pocketfahrschulelite.objects.ExamResultObject;
import de.freenet.pocketfahrschulelite.R;
import de.freenet.pocketfahrschulelite.classes.StringCrypto;
import de.freenet.pocketfahrschulelite.classes.Utils;
import de.freenet.pocketfahrschulelite.objects.Answer;
import de.freenet.pocketfahrschulelite.objects.ExamStatistic;
import de.freenet.pocketfahrschulelite.objects.MainGroup;
import de.freenet.pocketfahrschulelite.objects.Question;
import de.freenet.pocketfahrschulelite.objects.QuestionModel;
import de.freenet.pocketfahrschulelite.objects.SubGroup;
import de.freenet.pocketfahrschulelite.objects.ExamStatistic.ExamState;
import de.freenet.pocketfahrschulelite.objects.LearnStatistic.StatisticState;
import de.freenet.pocketfahrschulelite.objects.MainGroup.SectionType;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

public class FahrschuleDatabaseHelper extends SQLiteOpenHelper {
	
	public final static String TAG = "FahrschuleDatabaseHelper";
	
	// The Android's default system path of your application database.
    private final static String DB_PATH = "/data/data/de.freenet.pocketfahrschulelite/databases/";
    
    private final static String DB_NAME = "PocketFahrschuleDB";
    private final static int DB_VERSION = 5;
    
    private final static String ASSET_DB_FILENAME = "fahrschule.sqlite";
    
    private final static byte[] ANSWER_SALT = { 114, -115, 68, 8, 119, -39, 51, -9, 31, -111, -103, -41, -47, 102, 104, 64 };
 
    private SQLiteDatabase mDataBase;
 
    private Context mContext = null;
    
    private StringCrypto mStringCrypto;

	public FahrschuleDatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		mContext = context;
		
		mStringCrypto = new StringCrypto(ANSWER_SALT);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "Database onUpgrade()");
		
		if (oldVersion < 2 && newVersion == 2) {
			executeSqlScript(db, R.raw.new_questions);
		}
		
		switch (oldVersion) {
			case 1:
			case 2:
			case 3:
			case 4:
				db.execSQL("ALTER TABLE ZEXAMQUESTION ADD COLUMN ZTAGGED INTEGER");
				db.execSQL("UPDATE ZEXAMQUESTION SET ZTAGGED = 0");
				break;
		}
	}
	
	private void executeSqlScript(SQLiteDatabase db, int rawResId) {
		try {
			InputStream is = mContext.getResources().openRawResource(rawResId);
		    DataInputStream in = new DataInputStream(is);
		    BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF8"));
		    String strLine = null;
		    db.beginTransaction();
		    while ((strLine = br.readLine()) != null) {
		        db.execSQL(strLine);
		    }
		    in.close();
		    db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG, "SQLException: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		} finally {
			db.endTransaction();
		}
	}
	
	@Override
	public synchronized void close() {
		if (mDataBase != null) {
			mDataBase.close();
			mDataBase = null;
		}
		
		super.close();
	}
	
	/**
     * Creates a empty database on the system and overwrite it with our own database.
     * */
    public void createDataBase() throws IOException {
 
    	boolean dbExist = checkDataBase();
 
    	if (!dbExist) {
 
    		// By calling this method an empty database will be created into the default system path
    		// of your application so we'll be able to overwrite that database with our own.
        	getReadableDatabase().close();
        	
        	try {
    			copyDataBase();
    		} catch (IOException e) {
        		throw new Error("Error copying database");
        	}
    	}
    	
    	// Database upgrade fix due to version not being set in previous Pocket Fahrschule versions.
    	SQLiteDatabase db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
    	if (db.getVersion() == 0) {
    		Log.d(TAG, "Version is 0");
    		db.close();
    		
    		db = getWritableDatabase();
    		onUpgrade(db, 1, DB_VERSION);
    		db.close();
    		
    		db = null;
    	}
    	if (db != null) {
    		db.close();
    	}
    }
    
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {
 
    	SQLiteDatabase checkDB = null;
 
    	try {
    		String path = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
 
    	} catch(SQLiteException e) {
    		// Database does't exist yet.
    	}
 
    	if (checkDB != null) {
    		checkDB.close();
    	}
 
    	return checkDB != null ? true : false;
    }
    
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
    private void copyDataBase() throws IOException {
    	
    	Log.i(TAG, "Copying database");
 
    	//Open your local db as the input stream
    	InputStream is = mContext.getAssets().open(ASSET_DB_FILENAME);
 
    	// Path to the just created empty db
    	String outFilename = DB_PATH + DB_NAME;
 
    	//Open the empty db as the output stream
    	FileOutputStream fos = new FileOutputStream(outFilename);
 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = is.read(buffer)) > 0) {
    		fos.write(buffer, 0, length);
    	}
 
    	//Close the streams
    	fos.flush();
    	fos.close();
    	is.close();
    	
    	SQLiteDatabase db = getWritableDatabase();
    	onUpgrade(db, 1, 2);
    	db.close();
    }
    
    private void openDataBase() throws SQLException {
    	if (mDataBase != null) return;
    	
    	// Open the database
    	mDataBase = getReadableDatabase();
    }
    
    private void openWritableDataBase() throws SQLException {
    	if (mDataBase != null && !mDataBase.isReadOnly()) return;
    	else if (mDataBase != null && mDataBase.isReadOnly()) close();
    	
    	// Open the database
    	mDataBase = getWritableDatabase();
    }
    
    public List<MainGroup> getMainGroups() {
    	
    	if (mContext == null) return Arrays.asList(new MainGroup[0]);
    	
    	openDataBase();
    	Cursor c = mDataBase.rawQuery("SELECT t1.Z_PK AS Z_PK, t1.ZNAME AS ZNAME, t1.ZIMAGE AS ZIMAGE, COUNT(t3.Z_PK) AS questionsTotal, " +
    								  "t1.ZBASEMATERIAL AS ZBASEMATERIAL, COUNT(t4.Z_PK) AS questionsCorrect, COUNT(t5.Z_PK) AS questionsFaulty " +
    			"FROM ZMAINGROUP AS t1 " +
    			"JOIN ZSUBGROUP AS t2 " +
    			"ON t1.Z_PK = t2.ZCONTAINEDIN " +
    			"LEFT JOIN ZQUESTION AS t3 " +
    			"ON t2.Z_PK = t3.ZCONTAINEDIN " +
    			"AND t3.ZLICENSECLASSFLAG & " + FahrschulePreferences.getInstance().getCurrentLicenseClassString() + " > 0 " +
    			"LEFT JOIN ZLEARNINGSTATISTIC AS t4 " +
    			"ON t3.Z_PK = t4.ZWHATQUESTION " +
    			"AND t4.ZSTATE = 1 " +
    			"AND t4.ZLICENSECLASS = " + FahrschulePreferences.getInstance().getCurrentLicenseClassString() + " " +
    			"LEFT JOIN ZLEARNINGSTATISTIC AS t5 " +
    			"ON t3.Z_PK = t5.ZWHATQUESTION " +
    			"AND t5.ZSTATE = 2 " +
    			"AND t5.ZLICENSECLASS = " + FahrschulePreferences.getInstance().getCurrentLicenseClassString() + " " +
    			"GROUP BY t1.Z_PK " +
    			"ORDER BY t1.Z_PK", null);
    	
    	MainGroup[] result = new MainGroup[c.getCount()];
    	int sectionId = -1;
    	while (c.moveToNext()) {
    		result[c.getPosition()] = new MainGroup(c);
    		if (sectionId != c.getInt(c.getColumnIndex("ZBASEMATERIAL"))) {
    			sectionId = c.getInt(c.getColumnIndex("ZBASEMATERIAL"));
    			switch (sectionId) {
    				case 1:
    					result[c.getPosition()].sectionType = SectionType.BASE_MATERIAL;
    					break;
    				case 0:
    					result[c.getPosition()].sectionType = SectionType.ADDITIONAL_MATERIAL;
    					break;
    			}
    		}
    	}
    		
    	c.close();
    	
    	
    	return Arrays.asList(result);
    }
    
    public List<SubGroup> getSubGroups(int mainGroupId) {
    	
    	if (mContext == null) return Arrays.asList(new SubGroup[0]);
    	
    	openDataBase();
    	String[] val = { FahrschulePreferences.getInstance().getCurrentLicenseClassString(), String.valueOf(mainGroupId) };
    	Cursor c = mDataBase.rawQuery("SELECT t1.Z_PK AS Z_PK, t1.ZNAME AS ZNAME, t1.ZNUMBER AS ZNUMBER " +
    			"FROM ZSUBGROUP AS t1 " +
    			"LEFT JOIN ZQUESTION AS t2 " +
    			"ON t2.ZCONTAINEDIN = t1.Z_PK " +
    			"AND t2.ZLICENSECLASSFLAG & ? > 0 " +
    			"WHERE t1.ZCONTAINEDIN = ? " +
    			"GROUP BY t1.Z_PK " +
    			"ORDER BY t1.Z_PK", val);
    	
    	SubGroup[] result = new SubGroup[c.getCount()];
    	while (c.moveToNext()) {
    		result[c.getPosition()] = new SubGroup(c);
    	}
    	c.close();
    	
    	return Arrays.asList(result);
    }
    
    @SuppressWarnings("unchecked")
	public List<Question> getQuestions(Object object) {
    	
    	if (mContext == null) return Arrays.asList(new Question[0]);
    	
    	String joinClause = "";
    	String whereClause = "";
    	if (object instanceof List<?>) {
			return (List<Question>) object;
    	}
    	else if (object instanceof MainGroup) {
    		joinClause = "JOIN ZSUBGROUP AS t2 " +
	    				 "ON t2.Z_PK = t1.ZCONTAINEDIN " +
	    				 "AND t2.ZCONTAINEDIN = " + ((MainGroup) object).id + " ";
    	}
    	else if (object instanceof SubGroup) {
    		whereClause = "AND t1.ZCONTAINEDIN = " + ((SubGroup) object).id + " ";
    	}
    	else if (object instanceof String) {
    		String str = ((String) object);
    		if (str.equals(""))
    			return Arrays.asList(new Question[0]);
    		whereClause = "AND t1.ZTEXT LIKE '%" + str + "%' ";
    	}
    	
    	openDataBase();
    	String[] val = { FahrschulePreferences.getInstance().getCurrentLicenseClassString() };
    	Cursor c = mDataBase.rawQuery("SELECT t1.Z_PK AS Z_PK, t1.ZPOINTS AS ZPOINTS, t1.ZPREFIX AS ZPREFIX, t1.ZTEXT AS ZTEXT, " +
    								  "t1.ZNUMBER AS ZNUMBER, t1.ZTYPE AS ZTYPE, t1.ZIMAGE AS ZIMAGE, t3.ZSTATE AS ZSTATE, t1.ZDELETED AS ZDELETED " +
    			"FROM ZQUESTION AS t1 " +
    			"LEFT JOIN ZLEARNINGSTATISTIC AS t3 " +
    			"ON t3.ZWHATQUESTION = t1.Z_PK " +
    			"AND t3.ZLICENSECLASS = " + FahrschulePreferences.getInstance().getCurrentLicenseClassString() + " " +
    			joinClause +
    			"WHERE t1.ZLICENSECLASSFLAG & ? > 0 " +
    			"AND t1.ZDELETED = 0 " +
    			whereClause +
    			"ORDER BY t1.Z_PK", val);
    	
    	Question[] result = new Question[c.getCount()];
    	while (c.moveToNext()) {
    		int questionId = c.getInt(c.getColumnIndex("Z_PK"));
    		result[c.getPosition()] = new Question(c, getAnswers(questionId));
    	}
    	c.close();

    	return Arrays.asList(result);
    }
    
	public List<Question> getQuestions(Object object, StatisticState[] state) {

    	if (state == null || state.length == 0 || mContext == null) return Arrays.asList(new Question[0]);
    	
    	openDataBase();
    	
    	String joinClause = "";
    	String whereClause = "";
    	List<StatisticState> states = Arrays.asList(state);
    	String[] val = { FahrschulePreferences.getInstance().getCurrentLicenseClassString() };
    	
    	if (object instanceof List<?>) {
    		joinClause = "JOIN ZQUESTIONTAGS AS t3 " +
    					 "ON t3.ZWHATQUESTION = t1.Z_PK " +
    					 "AND t3.ZLICENSECLASS = " + val[0] + " ";
    	}
    	else if (object instanceof MainGroup) {
    		joinClause = "JOIN ZSUBGROUP AS t3 " +
	    				 "ON t3.Z_PK = t1.ZCONTAINEDIN " +
	    				 "AND t3.ZCONTAINEDIN = " + ((MainGroup) object).id + " ";
    	}
    	else if (object instanceof SubGroup) {
    		whereClause = "AND t1.ZCONTAINEDIN = " + ((SubGroup) object).id + " ";
    	}
    	else if (object instanceof String) {
    		String str = ((String) object);
    		if (str.equals(""))
    			return Arrays.asList(new Question[0]);
    		whereClause = "AND t1.ZTEXT LIKE '%" + str + "%' ";
    	}
    	
    	String query = "";
    	
    	if (states.size() == 1 && states.get(0) != StatisticState.NOT_ANSWERED) {
    		query = "SELECT t1.Z_PK AS Z_PK, t1.ZPOINTS AS ZPOINTS, t1.ZPREFIX AS ZPREFIX, t1.ZTEXT AS ZTEXT, t1.ZNUMBER AS ZNUMBER, " +
					"t1.ZTYPE AS ZTYPE, t1.ZIMAGE AS ZIMAGE, t2.ZSTATE AS ZSTATE, t1.ZDELETED AS ZDELETED " +
				"FROM ZQUESTION AS t1 " +
				"JOIN ZLEARNINGSTATISTIC AS t2 " +
				"ON t2.ZWHATQUESTION = t1.Z_PK " +
				"AND t2.ZSTATE = " + state[0].getId() + " " +
				joinClause +
				"WHERE t2.ZLICENSECLASS = ? " +
				"AND t1.ZDELETED = 0 " + whereClause;
    	}
    	else if (states.size() == 1) {
    		query = "SELECT t1.Z_PK AS Z_PK, t1.ZPOINTS AS ZPOINTS, t1.ZPREFIX AS ZPREFIX, t1.ZTEXT AS ZTEXT, " +
    					"t1.ZNUMBER AS ZNUMBER, t1.ZTYPE AS ZTYPE, t1.ZIMAGE AS ZIMAGE, t4.ZSTATE AS ZSTATE, t1.ZDELETED AS ZDELETED " +
					"FROM ZQUESTION AS t1 " +
					"LEFT JOIN ZLEARNINGSTATISTIC AS t4 " +
	    			"ON t4.ZWHATQUESTION = t1.Z_PK " +
	    			"AND t4.ZLICENSECLASS = " + FahrschulePreferences.getInstance().getCurrentLicenseClassString() + " " +
					joinClause +
					"WHERE t1.Z_PK NOT IN (SELECT ZWHATQUESTION " +
										  "FROM ZLEARNINGSTATISTIC " +
										  "WHERE ZLICENSECLASS = " + val[0] + ") " +
					"AND t1.ZLICENSECLASSFLAG & ? > 0 " +
					"AND t1.ZDELETED = 0 " + whereClause;
    	}
    	else if (states.size() == 2 && states.contains(StatisticState.FAULTY_ANSWERED) && states.contains(StatisticState.NOT_ANSWERED)) {
    		query = "SELECT t1.Z_PK AS Z_PK, t1.ZPOINTS AS ZPOINTS, t1.ZPREFIX AS ZPREFIX, t1.ZTEXT AS ZTEXT, " +
						"t1.ZNUMBER AS ZNUMBER, t1.ZTYPE AS ZTYPE, t1.ZIMAGE AS ZIMAGE, t4.ZSTATE AS ZSTATE, t1.ZDELETED AS ZDELETED " +
					"FROM ZQUESTION AS t1 " +
					"LEFT JOIN ZLEARNINGSTATISTIC AS t4 " +
	    			"ON t4.ZWHATQUESTION = t1.Z_PK " +
	    			"AND t4.ZLICENSECLASS = " + FahrschulePreferences.getInstance().getCurrentLicenseClassString() + " " +
					joinClause +
					"WHERE t1.Z_PK NOT IN (SELECT ZWHATQUESTION FROM ZLEARNINGSTATISTIC " +
									   "WHERE ZSTATE = " + StatisticState.CORRECT_ANSWERED.getId() + " " +
									   "AND ZLICENSECLASS = " + val[0] + ") " +
					"AND t1.ZLICENSECLASSFLAG & ? > 0 " +
					"AND t1.ZDELETED = 0 " + whereClause;
    	}
    	
    	Cursor c = mDataBase.rawQuery(query, val);
    	
    	Question[] result = new Question[c.getCount()];
    	while (c.moveToNext()) {
    		int questionId = c.getInt(c.getColumnIndex("Z_PK"));
    		result[c.getPosition()] = new Question(c, getAnswers(questionId));
    	}
    	c.close();

    	return Arrays.asList(result);
    }
	
	public List<Question> getExamQuestions() {
		
		if (mContext == null) return Arrays.asList(new Question[0]);
		
		openDataBase();
		
		
		/*Z_PK | ZNUMBER |
		+------+---------+
		| 3    | 1.1.3   |
		| 7    | 1.1.7   |
		| 34   | 2.2.37  |
		| 35   | 2.3.2   |
		| 36   | 2.5.4   |
		| 37   | 3.1.22  |
		| 38   | 4.8.14  |
		| 39   | 6.1.1   |
		| 59   | 6.3.16  |
		| 60   | 11.21.6 |*/


		//@"6.1.1", @"1.1.7", @"2.3.2", @"1.1.3", @"2.2.37", @"3.1.22", @"6.3.16", @"2.5.4", @"4.8.14", @"11.21.6"
		// 3, 7, 34, 35, 36, 37, 38, 39, 59, 60
		// 3, 7, 89, 99, 155, 224, 283, 411, 463, 1112
		Cursor c = mDataBase.rawQuery("SELECT t1.Z_PK AS Z_PK, t1.ZPOINTS AS ZPOINTS, t1.ZPREFIX AS ZPREFIX, t1.ZTEXT AS ZTEXT, t4.ZBASEMATERIAL AS ZBASEMATERIAL, " +
				  "t1.ZNUMBER AS ZNUMBER, t1.ZTYPE AS ZTYPE, t1.ZIMAGE AS ZIMAGE, t3.ZSTATE AS ZSTATE, t1.ZDELETED AS ZDELETED " +
				  "FROM ZQUESTION AS t1 " +
				  "JOIN ZSUBGROUP AS t2 " +
				  "ON t1.ZCONTAINEDIN = t2.Z_PK " +
				  "JOIN ZMAINGROUP AS t4 " +
				  "ON t2.ZCONTAINEDIN = t4.Z_PK " +
				  "LEFT JOIN ZLEARNINGSTATISTIC AS t3 " +
				  "ON t3.ZWHATQUESTION = t1.Z_PK " +
				  "AND t3.ZLICENSECLASS = " + FahrschulePreferences.getInstance().getCurrentLicenseClassString() + " " +
				  "WHERE t1.Z_PK IN(3, 7, 34, 35, 36, 37, 38, 39, 59, 60)", null);
		
		Question[] result = new Question[c.getCount()];
		while (c.moveToNext()) {
    		int questionId = c.getInt(c.getColumnIndex("Z_PK"));
    		result[c.getPosition()] = new Question(c, getAnswers(questionId));
    	}
		c.close();
		
		return Arrays.asList(result);
	}
	
	public void createModelsFromExam(int examId, boolean isOldExam) {
		
		openDataBase();
		
		String[] selectionArgs = { String.valueOf(examId) };
		Cursor c = mDataBase.rawQuery("SELECT t1.Z_PK AS Z_PK, t1.ZPOINTS AS ZPOINTS, t1.ZPREFIX AS ZPREFIX, t1.ZTEXT AS ZTEXT, t2.Z_PK AS Z_PK_T2, " +
    								  "t1.ZNUMBER AS ZNUMBER, t1.ZTYPE AS ZTYPE, t1.ZIMAGE AS ZIMAGE, t2.ZGIVENNUMBERANSWER AS ZGIVENNUMBERANSWER, t3.ZSTATE AS ZSTATE, " +
    								  "t1.ZDELETED AS ZDELETED, t2.ZTAGGED AS ZTAGGED " +
				"FROM ZQUESTION AS t1 " +
				"JOIN ZEXAMQUESTION AS t2 " +
				"ON t1.Z_PK = t2.ZWHATQUESTION " +
				"LEFT JOIN ZLEARNINGSTATISTIC AS t3 " +
    			"ON t3.ZWHATQUESTION = t1.Z_PK " +
    			"AND t3.ZLICENSECLASS = " + FahrschulePreferences.getInstance().getCurrentLicenseClassString() + " " +
				"WHERE t2.ZWHATEXAM = ? " +
				"ORDER BY t2.ZORDER ASC", selectionArgs);
		
		Integer[] examQuestionIds = new Integer[c.getCount()];
		String[] givenNumberAnswer = new String[c.getCount()];
		Boolean[] isOfficialExamTagged = new Boolean[c.getCount()];
		Question[] result = new Question[c.getCount()];
		while (c.moveToNext()) {
			int questionId = c.getInt(c.getColumnIndex("Z_PK"));
    		result[c.getPosition()] = new Question(c, getAnswers(questionId));
    		
    		examQuestionIds[c.getPosition()] = c.getInt(c.getColumnIndex("Z_PK_T2"));
    		givenNumberAnswer[c.getPosition()] = c.getString(c.getColumnIndex("ZGIVENNUMBERANSWER"));
    		givenNumberAnswer[c.getPosition()] = givenNumberAnswer[c.getPosition()] == null || givenNumberAnswer[c.getPosition()].equals("-1") ?
    			"" : givenNumberAnswer[c.getPosition()];
    		isOfficialExamTagged[c.getPosition()] = c.getInt(c.getColumnIndex("ZTAGGED")) == 1;
    	}
		c.close();
		
		QuestionModel.createModelsForQuestions(Arrays.asList(result));
		
		for (int i = 0;i < QuestionModel.getQuestionModels().size();i++) {
			QuestionModel model = QuestionModel.getQuestionModels().get(i);
			model.hasSolutionBeenShown = isOldExam;
			model.isOfficialExamTagged = isOfficialExamTagged[i];
			if (model.question.type == Question.Type.CHOICE) {
				model.givenAnswers.putAll(getGivenAnswers(examQuestionIds[i]));
			}
			else if (model.question.type == Question.Type.NUMBER && !givenNumberAnswer[i].equals("")) {
				model.givenAnswers.put(model.question.answers.get(0).id, givenNumberAnswer[i]);
			}
		}
	}
	
	public List<ExamResultObject> arrangeQuestionsForOfficialExamLayoutResultsView(final List<QuestionModel> models) {
		openDataBase();
		
		ArrayList<Integer> ids = new ArrayList<Integer>(models.size());
		for (QuestionModel model : models) {
			ids.add(model.question.id);
		}
		
		Cursor c = mDataBase.rawQuery("SELECT t3.*, t1.Z_PK AS questionId, t1.ZPOINTS " +
			"FROM ZQUESTION t1 " +
			"JOIN ZSUBGROUP t2 " +
			"ON t2.Z_PK = t1.ZCONTAINEDIN " +
			"JOIN ZMAINGROUP t3 " +
			"ON t3.Z_PK = t2.ZCONTAINEDIN " +
			"WHERE t1.Z_PK IN (" + TextUtils.join(",", ids) + ") " +
			"ORDER BY t3.Z_PK, t1.Z_PK", null);
	
		ArrayList<ExamResultObject> result = new ArrayList<ExamResultObject>(models.size());
		ArrayList<QuestionModel> pool = new ArrayList<QuestionModel>(models);
		while (c.moveToNext()) {
			int questionId = c.getInt(c.getColumnIndex("questionId"));
			for (QuestionModel m : pool) {
				if (m.question.id == questionId) {
					result.add(new ExamResultObject(c, m.hasAnsweredCorrectly()));
					pool.remove(m);
					break;
				}
			}
		}
		
		c.close();
		
		return result;
	}
	
	private HashMap<Integer, String> getGivenAnswers(int examQuestionId) {
		
		if (mContext == null) Collections.emptyMap();
		
		openDataBase();
    	String[] val = { String.valueOf(examQuestionId) };
    	Cursor c = mDataBase.rawQuery("SELECT t1.Z_PK AS Z_PK " +
    			"FROM ZANSWER AS t1 " +
    			"JOIN Z_1WHATEXAMQUESTIONS AS t2 " +
    			"ON t1.Z_PK = t2.Z_1GIVENANSWERS " +
    			"WHERE t2.Z_2WHATEXAMQUESTIONS = ?", val);
    	
    	HashMap<Integer, String> answers = new HashMap<Integer, String>();
    	while (c.moveToNext()) {
    		answers.put(c.getInt(c.getColumnIndex("Z_PK")), "");
    	}
    	c.close();
    	
		return answers;
	}
	
	public void removeCancelledExam() {
		openWritableDataBase();
		
		String[] columns = { "Z_PK" };
		String[] val = { FahrschulePreferences.getInstance().getCurrentLicenseClassString(), 
				FahrschulePreferences.getInstance().getCurrentTeachingTypeString(), String.valueOf(ExamState.CANCELED_EXAM.getId()) };
		Cursor c = mDataBase.query("ZEXAMSTATISTIC", columns, "ZLICENSECLASS = ? AND ZTEACHINGTYPE = ? AND ZSTATE = ?", val, null, null, null);
		
		while (c.moveToNext()) {
			int examId = c.getInt(c.getColumnIndex("Z_PK"));
			String[] examVal = { String.valueOf(examId) };
			
			Cursor c2 = mDataBase.query("ZEXAMQUESTION", null, "ZWHATEXAM = ?", examVal, null, null, null);
			while (c2.moveToNext()) {
				String[] whereArgs = { c2.getString(c2.getColumnIndex("Z_PK")) };
				mDataBase.delete("Z_1WHATEXAMQUESTIONS", "Z_2WHATEXAMQUESTIONS = ?", whereArgs);
			}
			c2.close();
			
			mDataBase.delete("ZEXAMQUESTION", "ZWHATEXAM = ?", examVal);
		}
		c.close();
		
		mDataBase.delete("ZEXAMSTATISTIC", "ZLICENSECLASS = ? AND ZTEACHINGTYPE = ? AND ZSTATE = ?", val);
	}
    
	public int countExams(ExamState state) {
		if (state == null || mContext == null) return 0;
		
		openDataBase();
    	
    	String[] columns = { "COUNT(Z_PK) AS num" };
    	Cursor c = null;
		String[] val = { FahrschulePreferences.getInstance().getCurrentLicenseClassString(), 
				FahrschulePreferences.getInstance().getCurrentTeachingTypeString(), String.valueOf(state.getId()) };
		c = mDataBase.query("ZEXAMSTATISTIC", columns, "ZLICENSECLASS = ? AND ZTEACHINGTYPE = ? AND ZSTATE = ?", val, null, null, null);
    	
		int num = 0;
		if (c.moveToFirst()) {
			num = c.getInt(c.getColumnIndex("num"));
		}
    	c.close();

    	return num;
	}
	
	public int countTaggedQuestions() {
		if (mContext == null) return 0;
    	
    	openDataBase();
    	
    	String[] val = { FahrschulePreferences.getInstance().getCurrentLicenseClassString() };
    	String[] columns = { "COUNT(Z_PK) AS num" };
    	Cursor c = mDataBase.query("ZQUESTIONTAGS", columns, "ZLICENSECLASS = ?", val, null, null, null);
    	
    	int num = 0;
    	while (c.moveToNext()) {
    		num = c.getInt(c.getColumnIndex("num"));
    	}
    	c.close();

    	return num;
	}
	
    public int countQuestions(StatisticState state) {
    	if (mContext == null) return 0;
    	
    	openDataBase();
    	
    	String[] columns = { "COUNT(Z_PK) AS num" };
    	Cursor c = null;
    	if (state != null) {
    		String[] val = { FahrschulePreferences.getInstance().getCurrentLicenseClassString(), String.valueOf(state.getId()) };
    		c = mDataBase.query("ZLEARNINGSTATISTIC", columns, "ZLICENSECLASS = ? AND ZSTATE = ?", val, null, null, null);
    	}
    	else {
    		String[] val = { FahrschulePreferences.getInstance().getCurrentLicenseClassString() };
    		c = mDataBase.query("ZQUESTION", columns, "ZLICENSECLASSFLAG & ? > 0 AND ZDELETED = 0", val, null, null, null);
    	}
    	
    	int num = 0;
    	while (c.moveToNext()) {
    		num = c.getInt(c.getColumnIndex("num"));
    	}
    	c.close();

    	return num;
    }
    
    public List<Answer> getAnswers(int questionId) {
    	if (mContext == null) return Arrays.asList(new Answer[0]);
    	
    	openDataBase();
    	String[] val = { String.valueOf(questionId) };
    	Cursor c = mDataBase.rawQuery("SELECT Z_PK, ZCORRECT, ZWHATQUESTION, ZTEXT, ZCORRECTNUMBER FROM ZANSWER WHERE ZWHATQUESTION = ? ORDER BY Z_PK", val);
    	
    	Answer[] result = new Answer[c.getCount()];
    	while (c.moveToNext()) {
    		result[c.getPosition()] = new Answer(c);
    		result[c.getPosition()].text = mStringCrypto.decrypt(result[c.getPosition()].text);
    	}
    	c.close();
    	
    	List<Answer> answers = Arrays.asList(result);
    	Collections.shuffle(answers);
    	
    	return answers;
    }
    
    public void tagQuestion(int questionId, boolean tag) {
    	openWritableDataBase();
    	String[] val = { String.valueOf(questionId), FahrschulePreferences.getInstance().getCurrentLicenseClassString() };
    	Cursor c = mDataBase.rawQuery("SELECT Z_PK FROM ZQUESTIONTAGS WHERE ZWHATQUESTION = ? AND ZLICENSECLASS = ?", val);
    	
    	if (!tag && c.getCount() == 0 || tag && c.getCount() > 0)
    		return;
    	else if (!tag && c.getCount() > 0) {
    		mDataBase.delete("ZQUESTIONTAGS", "ZWHATQUESTION = ? AND ZLICENSECLASS = ?", val);
    	}
    	else {
    		ContentValues values = new ContentValues();
        	values.put("Z_ENT", 7);
        	values.put("Z_OPT", 1);
        	values.put("ZLICENSECLASS", Integer.valueOf(FahrschulePreferences.getInstance().getCurrentLicenseClassString()));
        	values.put("ZWHATQUESTION", questionId);
        	
        	mDataBase.insert("ZQUESTIONTAGS", null, values);
    	}
    	
    	c.close();
    }
    
    public boolean isQuestionTagged(int questionId) {
    	openDataBase();
    	String[] val = { String.valueOf(questionId), FahrschulePreferences.getInstance().getCurrentLicenseClassString() };
    	Cursor c = mDataBase.rawQuery("SELECT Z_PK FROM ZQUESTIONTAGS WHERE ZWHATQUESTION = ? AND ZLICENSECLASS = ?", val);
    	
    	boolean isTagged = c.getCount() > 0;
    	
    	c.close();
    	
    	return isTagged;
    }
    
    public void clearLearnStatistics() {
    	openWritableDataBase();
    	String[] val = { FahrschulePreferences.getInstance().getCurrentLicenseClassString() };
    	mDataBase.delete("ZLEARNINGSTATISTIC", "ZLICENSECLASS = ?", val);
    }
    
    public void setLearnStatistics(int questionId, StatisticState state) {
    	if (FahrschulePreferences.getInstance().isGuestMode()) return;
    	
    	openWritableDataBase();
    	
    	String[] val = { String.valueOf(questionId), FahrschulePreferences.getInstance().getCurrentLicenseClassString() };
    	Cursor c = mDataBase.rawQuery("SELECT * FROM ZLEARNINGSTATISTIC WHERE ZWHATQUESTION = ? AND ZLICENSECLASS = ?", val);
    	
    	ContentValues values = new ContentValues();
    	values.put("Z_ENT", 4);
    	values.put("Z_OPT", 1);
    	values.put("ZLICENSECLASS", Integer.valueOf(FahrschulePreferences.getInstance().getCurrentLicenseClassString()));
    	values.put("ZSTATE", state.getId());
    	values.put("ZWHATQUESTION", questionId);
    	values.put("ZDATE", String.valueOf(new Date().getTime()));
    	
    	if (c.getCount() == 0) {
    		mDataBase.insert("ZLEARNINGSTATISTIC", null, values);
    	}
    	else {
    		mDataBase.update("ZLEARNINGSTATISTIC", values, "ZWHATQUESTION = ? AND ZLICENSECLASS = ?", val);
    	}
    	
    	c.close();
    }
    
    private boolean hasPassedExam(List<QuestionModel> models, int points) {
		
		boolean passed = true;
		
		JSONObject examSheet = null;
		try {
			examSheet = new JSONObject(Utils.getContentFromFile(mContext.getAssets().open("exam_sheet.json")));
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		} catch (JSONException e) {
			Log.e(TAG, "JSONException: " + e.getMessage());
		}
		
		if (examSheet == null) return false;
		
		try {
			int maxPoints = examSheet.getJSONObject(FahrschulePreferences.getInstance().getCurrentLicenseClassString())
					.getJSONObject(FahrschulePreferences.getInstance().getCurrentTeachingTypeString()).getInt("MaxPoints");
			
			if (points > maxPoints) {
				passed = false;
			}
			else if (points == maxPoints && points == 10) {
				
				 int numFalse = 0;
				for (QuestionModel model : models) {
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
	}
    
    public void setExamStatistics(ExamState state, int secondsLeft, int points, List<QuestionModel> models, int currentQuestionIndex) {
    	
    	if (FahrschulePreferences.getInstance().isGuestMode()) return;

    	openWritableDataBase();
    	
    	mDataBase.beginTransaction();
    	try {
    		ContentValues values = new ContentValues();
        	values.put("Z_ENT", 3);
        	values.put("Z_OPT", 1);
        	values.put("ZTEACHINGTYPE", FahrschulePreferences.getInstance().getCurrentTeachingTypeString());
        	values.put("ZINDEX",currentQuestionIndex);
        	values.put("ZTIMELEFT", secondsLeft);
        	values.put("ZFAULTYPOINTS", points);
        	values.put("ZLICENSECLASS", Integer.valueOf(FahrschulePreferences.getInstance().getCurrentLicenseClassString()));
        	values.put("ZSTATE", state.getId());
        	values.put("ZDATE", String.valueOf(new Date().getTime()));
        	values.put("ZPASSED", hasPassedExam(models, points));
        	
        	long rowId = mDataBase.insert("ZEXAMSTATISTIC", null, values);
        	
        	if (rowId == -1) return;
        	
        	for (QuestionModel model : models) {
        		if (model.question.type == Question.Type.CHOICE) {
        			values.clear();
        	    	values.put("Z_ENT", 2);
        	    	values.put("Z_OPT", 1);
        	    	values.put("ZORDER", model.index);
        	    	values.put("ZTAGGED", model.isOfficialExamTagged);
        	    	values.put("ZWHATEXAM", rowId);
        	    	values.put("ZWHATQUESTION", model.question.id);
        	    	values.put("ZGIVENNUMBERANSWER", -1.0f);
        	    	
        	    	long answerRowId = mDataBase.insert("ZEXAMQUESTION", null, values);
        	    	
        	    	for (Answer answer : model.question.answers) {
        	    		
        	    		if (model.givenAnswers.containsKey(answer.id)) {
        	    			values.clear();
                	    	values.put("Z_1GIVENANSWERS", answer.id);
                	    	values.put("Z_2WHATEXAMQUESTIONS", answerRowId);
                	    	
                	    	mDataBase.insert("Z_1WHATEXAMQUESTIONS", null, values);
        	    		}
        	    	}
        		}
        		else if (model.question.type == Question.Type.NUMBER) {
        			values.clear();
        	    	values.put("Z_ENT", 2);
        	    	values.put("Z_OPT", 1);
        	    	values.put("ZORDER", model.index);
        	    	values.put("ZTAGGED", model.isOfficialExamTagged);
        	    	values.put("ZWHATEXAM", rowId);
        	    	values.put("ZWHATQUESTION", model.question.id);
        	    	
        	    	String numberAnswer = model.givenAnswers.get(model.question.answers.get(0).id);
        	    	if (numberAnswer != null)
        	    		values.put("ZGIVENNUMBERANSWER", numberAnswer);
        	    	else
        	    		values.put("ZGIVENNUMBERANSWER", -1.0f);
        	    	
        	    	mDataBase.insert("ZEXAMQUESTION", null, values);
        		}
        	}
        	mDataBase.setTransactionSuccessful();
    	} finally {
    		mDataBase.endTransaction();
    	}
    }
    
    public void setExamStatistics(ExamState state, int secondsLeft, int points, List<QuestionModel> models) {
    	setExamStatistics(state, secondsLeft, points, models, 0);
    }
    
    public List<ExamStatistic> getExamStatistics(int limit)
    {	
    	return getExamStatistics(limit, ExamState.FINISHED_EXAM);
    }

    public List<ExamStatistic> getExamStatistics(int limit, ExamState state) {
    	if (mContext == null) return Arrays.asList(new ExamStatistic[0]);
    	
    	openDataBase();
    	
    	String[] val = { String.valueOf(state.getId()), FahrschulePreferences.getInstance().getCurrentLicenseClassString(),
    			FahrschulePreferences.getInstance().getCurrentTeachingTypeString() };
    	String limitStr = limit > 0 ? String.valueOf(limit) : null;
    	Cursor c = mDataBase.query("ZEXAMSTATISTIC", null, "ZSTATE = ? AND ZLICENSECLASS = ? AND ZTEACHINGTYPE = ?", val, null, null, "Z_PK DESC", limitStr);
    	
    	ExamStatistic[] result = new ExamStatistic[c.getCount()];
    	while (c.moveToNext()) {
    		result[c.getPosition()] = new ExamStatistic(c);
    	}
    	c.close();
    	
    	return Arrays.asList(result);
    }
    
    public List<Question> searchQuestions(String searchString) {
    	if (mContext == null) return Arrays.asList(new Question[0]);
    	
    	openDataBase();
    	String[] val = { FahrschulePreferences.getInstance().getCurrentLicenseClassString() };
    	Cursor c = mDataBase.rawQuery("SELECT t1.Z_PK AS Z_PK, t1.ZPOINTS AS ZPOINTS, t1.ZPREFIX AS ZPREFIX, t1.ZTEXT AS ZTEXT, " +
    								  "t1.ZNUMBER AS ZNUMBER, t1.ZTYPE AS ZTYPE, t1.ZIMAGE AS ZIMAGE, t2.ZSTATE AS ZSTATE, t1.ZDELETED AS ZDELETED " +
    			"FROM ZQUESTION AS t1 " +
    			"LEFT JOIN ZLEARNINGSTATISTIC AS t2 " +
    			"ON t2.ZWHATQUESTION = t1.Z_PK " +
    			"AND t2.ZLICENSECLASS = " + FahrschulePreferences.getInstance().getCurrentLicenseClassString() + " " +
    			"WHERE t1.ZLICENSECLASSFLAG & ? > 0 " +
    			"AND t1.ZTEXT LIKE '%" + searchString + "%' " +
    			"AND t1.ZDELETED = 0 " +
    			"ORDER BY t1.Z_PK", val);
    	
    	Question[] result = new Question[c.getCount()];
    	while (c.moveToNext()) {
    		int questionId = c.getInt(c.getColumnIndex("Z_PK"));
    		result[c.getPosition()] = new Question(c, getAnswers(questionId));
    	}
    	c.close();
    	
    	return Arrays.asList(result);
    }
    
    public List<Question> getTaggedQuestions() {
    	if (mContext == null) return Arrays.asList(new Question[0]);
    	
    	openDataBase();
    	String[] val = { FahrschulePreferences.getInstance().getCurrentLicenseClassString() };
    	Cursor c = mDataBase.rawQuery("SELECT t1.Z_PK AS Z_PK, t1.ZPOINTS AS ZPOINTS, t1.ZPREFIX AS ZPREFIX, t1.ZTEXT AS ZTEXT, " +
    								  "t1.ZNUMBER AS ZNUMBER, t1.ZTYPE AS ZTYPE, t1.ZIMAGE AS ZIMAGE, t3.ZSTATE AS ZSTATE, t1.ZDELETED AS ZDELETED " +
    			"FROM ZQUESTION AS t1 " +
    			"JOIN ZQUESTIONTAGS AS t2 " +
    			"ON t2.ZWHATQUESTION = t1.Z_PK " +
    			"LEFT JOIN ZLEARNINGSTATISTIC AS t3 " +
    			"ON t3.ZWHATQUESTION = t1.Z_PK " +
    			"AND t3.ZLICENSECLASS = " + FahrschulePreferences.getInstance().getCurrentLicenseClassString() + " " +
    			"WHERE t2.ZLICENSECLASS = ? " +
    			"AND t1.ZDELETED = 0 " +
    			"ORDER BY t1.Z_PK", val);
    	
    	Question[] result = new Question[c.getCount()];
    	while (c.moveToNext()) {
    		int questionId = c.getInt(c.getColumnIndex("Z_PK"));
    		result[c.getPosition()] = new Question(c, getAnswers(questionId));
    	}
    	c.close();
    	
    	return Arrays.asList(result);
    }
}
