package de.freenet.pocketfahrschulelite.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuestionModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4385440698087498742L;
	
	private static ArrayList<QuestionModel> mQuestionModels;
	
	public Question question;
	public HashMap<Integer, String> givenAnswers;
	public int index;
	public boolean hasSolutionBeenShown;
	public boolean isOfficialExamTagged;
	
	public QuestionModel() {
		super();
		givenAnswers = new HashMap<Integer, String>();
		index = 0;
		hasSolutionBeenShown = false;
		isOfficialExamTagged = false;
	}
	
	public QuestionModel(Question question, int index) {
		this();
		this.question = question;
		this.index = index;
	}
	
	public boolean hasAnsweredCorrectly() {
		return question.hasAnsweredCorrectly(givenAnswers);
	}
	
	public boolean isAnAnswerGiven() {
		return givenAnswers.size() > 0;
	}
	
	public static void clearAndSetQuestionModels(List<QuestionModel> models) {
		
		if (mQuestionModels == null) {
			mQuestionModels = new ArrayList<QuestionModel>();
		}
		else {
			mQuestionModels.clear();
		}
		
		mQuestionModels.addAll(models);
	}
	
	public static ArrayList<QuestionModel> getQuestionModels() {
		return mQuestionModels != null ? mQuestionModels : new ArrayList<QuestionModel>();
	}
	
	public static ArrayList<QuestionModel> createModelsForQuestions(List<Question> questions) {
		
		if (mQuestionModels == null) {
			mQuestionModels = new ArrayList<QuestionModel>();
		}
		else {
			mQuestionModels.clear();
		}
		
		for (int i = 0;i < questions.size();i++) {
			mQuestionModels.add(new QuestionModel(questions.get(i), i));
		}
		
		return mQuestionModels;
	}
}
