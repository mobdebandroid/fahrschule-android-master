package de.freenet.c2dm;

public class MessageConstants {

	public static final String REGISTRATION_INTENT_ACTION = "com.google.android.c2dm.intent.REGISTRATION";
	public static final String RECEIVE_INTENT_ACTION = "com.google.android.c2dm.intent.RECEIVE";
	public static final String REGISTER_INTENT_ACTION = "com.google.android.c2dm.intent.REGISTER";
	
	public static final String REGISTER_GOOGLE_INTENT_ACTION = "de.freenet.c2dm.intent.GOOGLE_REGISTER";
	public static final String SUBSCRIPTION_INTENT_ACTION = "de.freenet.c2dm.intent.SUBSCRIPTION";
	
	public static final String EXTRA_PUSHALOT_REGISTER_PAYLOAD = "de.freenet.c2dm.extra.REGISTER_PAYLOAD";
	
	public static final String EXTRA_SUBSCRIPTION_CID = "de.freenet.c2dm.extra.SUBSCRIPTION_CID";
	public static final String EXTRA_SUBSCRIPTION_ACTIVATE = "de.freenet.c2dm.extra.SUBSCRIPTION_ACTIVATE";
	
	public static final boolean DEBUG = true;
	
	public static enum RequestType { REGISTRATION, SUBSCRIPTION, GOOGLE_REGISTRATION, FETCH_SUBSCRIPTIONS };
}
