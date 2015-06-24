package de.freenet.pocketfahrschulelite.objects;

public class LearnStatistic {

	public static enum StatisticState {
		STATE_LESS (0),
		CORRECT_ANSWERED (1),
		FAULTY_ANSWERED (2),
		NOT_ANSWERED (4);
		
		private final int mId;
		private StatisticState (int id) {
	        mId = id;
	    }
		
		public int getId() { return mId; }
	}
	
	public static StatisticState parseStatisticState(Integer state) {
		if (state == null) return StatisticState.NOT_ANSWERED;
		
		switch (state) {
			case 1:
				return StatisticState.CORRECT_ANSWERED;
			case 2:
				return StatisticState.FAULTY_ANSWERED;
			case 4:
				return StatisticState.NOT_ANSWERED;
		}
		
		return StatisticState.STATE_LESS;
	}
}