package de.freenet.webservices;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class UrlShortener {
	
	private static final String TAG = "UrlShortener";

	public static String shortenWithGooGl(String url) {
		
		String shortUrl = "";
		
		JSONObject postObject = new JSONObject();
		try {
			postObject.put("longUrl", url);
		} catch (JSONException e) {
			return shortUrl;
		}
		
		try {
			HttpClient client = new DefaultHttpClient();
			
	        HttpPost httpPost = new HttpPost("https://www.googleapis.com/urlshortener/v1/url");
	        httpPost.addHeader("Content-Type", "application/json");
	        StringEntity se = new StringEntity(postObject.toString());
	        httpPost.setEntity(se);
	        
	        HttpResponse responseGet = client.execute(httpPost);
	        HttpEntity responseEntity = responseGet.getEntity();
	        if (responseEntity != null) {
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            BufferedOutputStream bos = new BufferedOutputStream(baos, 8192);
	            responseEntity.writeTo(bos);
	            bos.flush();
	            bos.close();
	            
	            JSONObject result = new JSONObject(new String(baos.toByteArray()));
	            shortUrl = result.getString("id");
	        }
	        
	        client = null;
		
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		}
        
		return shortUrl;
	}
}
