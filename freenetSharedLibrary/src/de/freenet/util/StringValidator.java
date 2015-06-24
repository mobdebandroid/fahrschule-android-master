package de.freenet.util;

import java.util.regex.Pattern;

import android.text.TextUtils;

public class StringValidator {

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	
	public static boolean validateEmail(String email) {
		return validatePattern(email, EMAIL_PATTERN);
	} 
	
	private static boolean validatePattern(String value, String pattern) {
		if (TextUtils.isEmpty(value)) return false;
		
		return Pattern.compile(pattern).matcher(value).matches();
	}
	
}
