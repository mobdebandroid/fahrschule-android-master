package de.freenet.pocketfahrschulelite.classes;

import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

public class StringCrypto {
	
	private static final String TAG = "StringCrypto";
	private static final String HEX = "0123456789ABCDEF";
	private static final String KEYGEN_ALGORITHM = "PBEWITHSHAAND256BITAES-CBC-BC";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final byte[] IV =
        { 99, 69, -27, 122, 44, -69, -21, 19, -123, 67, 12, -39, -125, 99, 8, -118 };
	
	private Cipher mEncryptor;
    private Cipher mDecryptor;
	
	public StringCrypto(byte[] salt) {
		try {
			KeySpec keySpec = new PBEKeySpec("de.freenet.pocketfahrschule".toCharArray(), salt, 1024, 256);
			SecretKeyFactory factory;
		
			factory = SecretKeyFactory.getInstance(KEYGEN_ALGORITHM);
		
			SecretKeySpec secretkeySpec = new SecretKeySpec(factory.generateSecret(keySpec).getEncoded(), "AES");
		
			mEncryptor = Cipher.getInstance(CIPHER_ALGORITHM);
			mEncryptor.init(Cipher.ENCRYPT_MODE, secretkeySpec, new IvParameterSpec(IV));
			mDecryptor = Cipher.getInstance(CIPHER_ALGORITHM);
			mDecryptor.init(Cipher.DECRYPT_MODE, secretkeySpec, new IvParameterSpec(IV));
		} catch (GeneralSecurityException e) {
			Log.e(TAG, "GeneralSecurityException: " + e.getMessage());
		}
	}

	public String encrypt(String value) {		
	    String result = "";
		try {
			result = byteToHex(mEncryptor.doFinal(value.getBytes()));
		} catch (Exception e) {
			Log.e(TAG, "GeneralSecurityException: " + e.getMessage());
		}
		return result;
	}
	
	public String decrypt(String value) {
		String result = "";
		try {
			result = new String(mDecryptor.doFinal(hexToByte(value)));
		}
		catch (Exception e) {
			Log.e(TAG, "GeneralSecurityException: " + e.getMessage());
		}
		return result;
	}
	
	public static String byteToHex(byte[] buf) {
		if (buf == null) return "";
		
		StringBuffer result = new StringBuffer(2 * buf.length);
		for (int i = 0; i < buf.length; i++) {
			result.append(HEX.charAt((buf[i] >> 4) & 0x0f)).append(HEX.charAt(buf[i] & 0x0f));
		}
		return result.toString();
	}
	
	public static byte[] hexToByte(String value) {
		int len = value.length() / 2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(value.substring(2 * i, 2 * i + 2), 16).byteValue();
		return result;
	}
}
