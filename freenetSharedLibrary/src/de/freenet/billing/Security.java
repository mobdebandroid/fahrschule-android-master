// Copyright 2010 Google Inc. All Rights Reserved.

package de.freenet.billing;

import de.freenet.billing.Consts.PurchaseState;
import de.freenet.util.Base64;
import de.freenet.util.Base64DecoderException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.text.TextUtils;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Security-related methods. For a secure implementation, all of this code
 * should be implemented on a server that communicates with the
 * application on the device. For the sake of simplicity and clarity of this
 * example, this code is included here and is executed on the device. If you
 * must verify the purchases on the phone, you should obfuscate this code to
 * make it harder for an attacker to replace the code with stubs that treat all
 * purchases as verified.
 */
public class Security {
    private static final String TAG = "Security";

    private static final String KEY_FACTORY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * This keeps track of the nonces that we generated and sent to the
     * server.  We need to keep track of these until we get back the purchase
     * state and send a confirmation message back to Android Market. If we are
     * killed and lose this list of nonces, it is not fatal. Android Market will
     * send us a new "notify" message and we will re-generate a new nonce.
     * This has to be "static" so that the {@link BillingReceiver} can
     * check if a nonce exists.
     */
    private static HashSet<Long> sKnownNonces = new HashSet<Long>();

    /**
     * A class to hold the verified purchase information.
     */
    public static class VerifiedPurchase {
        public PurchaseState purchaseState;
        public String notificationId;
        public String productId;
        public String orderId;
        public String purchaseToken;
        public long purchaseTime;
        public String developerPayload;

        public VerifiedPurchase(PurchaseState purchaseState, String notificationId, String productId, String orderId,
        		long purchaseTime, String purchaseToken, String developerPayload) {
            this.purchaseState = purchaseState;
            this.notificationId = notificationId;
            this.productId = productId;
            this.orderId = orderId;
            this.purchaseTime = purchaseTime;
            this.purchaseToken = purchaseToken;
            this.developerPayload = developerPayload;
        }
    }

    /** Generates a nonce (a random number used once). */
    public static long generateNonce() {
        long nonce = RANDOM.nextLong();
        sKnownNonces.add(nonce);
        return nonce;
    }

    public static void removeNonce(long nonce) {
        sKnownNonces.remove(nonce);
    }

    public static boolean isNonceKnown(long nonce) {
        return sKnownNonces.contains(nonce);
    }

    /**
     * Verifies that the data was signed with the given signature, and returns
     * the list of verified purchases. The data is in JSON format and contains
     * a nonce (number used once) that we generated and that was signed
     * (as part of the whole data string) with a private key. The data also
     * contains the {@link PurchaseState} and product ID of the purchase.
     * In the general case, there can be an array of purchase transactions
     * because there may be delays in processing the purchase on the backend
     * and then several purchases can be batched together.
     * @param signedData the signed JSON string (signed, not encrypted)
     * @param signature the signature for the data, signed with the private key
     */
    public static ArrayList<VerifiedPurchase> verifyPurchase(Context context, String signedData, String signature) {
        if (signedData == null) {
            Log.e(TAG, "data is null");
            return null;
        }
        if (Consts.DEBUG) {
            Log.i(TAG, "signedData: " + signedData);
        }
        boolean verified = false;
        if (!TextUtils.isEmpty(signature)) {
            /**
             * Compute your public key (that you got from the Android Market publisher site).
             *
             * Instead of just storing the entire literal string here embedded in the
             * program,  construct the key at runtime from pieces or
             * use bit manipulation (for example, XOR with some other string) to hide
             * the actual key.  The key itself is not secret information, but we don't
             * want to make it easy for an adversary to replace the public key with one
             * of their own and then fake messages from the server.
             *
             * Generally, encryption keys / passwords should only be kept in memory
             * long enough to perform the operation they need to perform.
             */
        	
        	ComponentName cn = new ComponentName(context, BillingService.class);
            byte[] pubKey = null;
        	try {
    			ServiceInfo si = context.getPackageManager().getServiceInfo(cn, PackageManager.GET_META_DATA);
    			String publicKey = si.metaData.getString("publicKey");
    			pubKey = Base64.decode(publicKey);
    			for (int i = 0;i < pubKey.length;i++) {
    				pubKey[i] -= 128; 
    			}
    		} catch (Exception e) {
    			Log.e(TAG, "Can not get public key");
    		}
        	
        	if (pubKey == null) return null;
        	
        	byte[] stencil = { 30,40,-61,11,110,4,77,-58,-98,76,56,-31,55,-92,-25,-128,24,-31,-9,-82,-126,-99,21,109,
            		-51,-50,8,-98,119,-63,123,-5,-65,12,107,64,112,-125,55,75,102,28,44,-78,97,-106,122,114,30,-102,
            		65,-82,-54,-111,85,-58,-14,-26,64,-101,-87,-112,47,72,50,-57,59,57,109,-89,24,4,-24,-6,34,29,60,
            		126,124,28,82,-73,40,64,-70,84,27,-94,-119,71,116,44,15,33,122,-89,-38,-14,91,-68,36,-82,9,12,-102,
            		-79,5,115,-14,-24,124,103,14,60,29,80,62,-32,-103,-18,113,91,-105,-125,70,-125,-54,-50,72,-122,64,
            		39,-43,55,113,-127,28,69,-34,-25,-108,55,-73,-91,-35,0,75,-20,-86,-50,40,-94,-77,93,-68,-70,123,49,
            		-20,-25,95,-117,-36,-92,-28,37,-33,-13,5,114,12,115,99,66,6,73,33,7,62,58,83,42,58,127,55,-109,-120,
            		6,-11,-77,-30,32,35,69,82,-9,5,-104,-83,-111,18,-48,-105,-119,67,35,58,110,-65,-102,67,71,-19,-92,
            		89,-63,6,109,13,-2,20,-85,106,67,-44,85,-86,113,115,103,1,118,42,-1,50,-2,-37,-92,122,-84,60,-103,19,
            		55,66,121,-63,57,-74,-13,-115,81,49,-47,-53,-19,-65,90,16,125,-74,-93,-98,-71,-57,-128,91,-68,-28,
            		-37,10,-64,-28,-125,70,93,-89,-82,14,93,105,18,-101,-116,-7,75,61,-56,74,-127,26,94,125,-90,15,-93,
            		51,-107,34,-82,37,114,13,-123,99,-73,3,-86,-50,23,123,-49,-13,65,37,84,-85,-5,-94,98,-60,-104,-120,
            		120,-10,42,-100,-105,47,-57,23,116,-105,13,-67,-65,98,113,125,4,87,-100,59,-55,-110,-1,-28,-84,-57,
            		47,-35,8,106,21,18,-50,66,-22,-91,43,104,125,-40,83,62,103,-109,52,-102,-2,-81,-4,-74,78,104,1,-49,
            		-89,-78,-73,-87,58,105,67,48,-108,-65,19,108,71,-62,-5 };
        	
            byte[] decryptedPubKey = new byte[pubKey.length];
            for (int i = 0;i < pubKey.length;i++) {
            	decryptedPubKey[i] = (byte) (pubKey[i] ^ stencil[i]);
            }
            
            PublicKey key = Security.generatePublicKey(new String(decryptedPubKey));
            verified = Security.verify(key, signedData, signature);
            if (!verified) {
                Log.w(TAG, "signature does not match data.");
                return null;
            }
        }

        JSONObject jObject;
        JSONArray jTransactionsArray = null;
        int numTransactions = 0;
        long nonce = 0L;
        try {
            jObject = new JSONObject(signedData);

            // The nonce might be null if the user backed out of the buy page.
            nonce = jObject.optLong("nonce");
            jTransactionsArray = jObject.optJSONArray("orders");
            if (jTransactionsArray != null) {
                numTransactions = jTransactionsArray.length();
            }
        } catch (JSONException e) {
            return null;
        }

        if (!Security.isNonceKnown(nonce)) {
            Log.w(TAG, "Nonce not found: " + nonce);
            return null;
        }

        ArrayList<VerifiedPurchase> purchases = new ArrayList<VerifiedPurchase>();
        try {
            for (int i = 0; i < numTransactions; i++) {
                JSONObject jElement = jTransactionsArray.getJSONObject(i);
                int response = jElement.getInt("purchaseState");
                PurchaseState purchaseState = PurchaseState.valueOf(response);
                String productId = jElement.getString("productId");
                //String packageName = jElement.getString("packageName");
                long purchaseTime = jElement.getLong("purchaseTime");
                String orderId = jElement.optString("orderId", "");
                String purchaseToken = jElement.optString("purchaseToken", "");
                String notifyId = null;
                if (jElement.has("notificationId")) {
                    notifyId = jElement.getString("notificationId");
                }
                String developerPayload = jElement.optString("developerPayload", null);

                // If the purchase state is PURCHASED, then we require a
                // verified nonce.
                if (purchaseState == PurchaseState.PURCHASED && !verified) {
                    continue;
                }
                purchases.add(new VerifiedPurchase(purchaseState, notifyId, productId,
                        orderId, purchaseTime, purchaseToken, developerPayload));
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON exception: ", e);
            return null;
        }
        removeNonce(nonce);
        return purchases;
    }

    /**
     * Generates a PublicKey instance from a string containing the
     * Base64-encoded public key.
     *
     * @param encodedPublicKey Base64-encoded public key
     * @throws IllegalArgumentException if encodedPublicKey is invalid
     */
    public static PublicKey generatePublicKey(String encodedPublicKey) {
        try {
            byte[] decodedKey = Base64.decode(encodedPublicKey);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            Log.e(TAG, "Invalid key specification.");
            throw new IllegalArgumentException(e);
        } catch (Base64DecoderException e) {
            Log.e(TAG, "Base64 decoding failed.");
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Verifies that the signature from the server matches the computed
     * signature on the data.  Returns true if the data is correctly signed.
     *
     * @param publicKey public key associated with the developer account
     * @param signedData signed data from server
     * @param signature server signature
     * @return true if the data and signature match
     */
    public static boolean verify(PublicKey publicKey, String signedData, String signature) {
        if (Consts.DEBUG) {
            Log.i(TAG, "signature: " + signature);
        }
        Signature sig;
        try {
            sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(publicKey);
            sig.update(signedData.getBytes());
            if (!sig.verify(Base64.decode(signature))) {
                Log.e(TAG, "Signature verification failed.");
                return false;
            }
            return true;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "NoSuchAlgorithmException.");
        } catch (InvalidKeyException e) {
            Log.e(TAG, "Invalid key specification.");
        } catch (SignatureException e) {
            Log.e(TAG, "Signature exception.");
        } catch (Base64DecoderException e) {
            Log.e(TAG, "Base64 decoding failed.");
        }
        return false;
    }
}
