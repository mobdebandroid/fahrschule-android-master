package de.freenet.advertisement;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.freenet.advertisement.AdManager.AdvertisementType;

import android.util.Log;

public class AdHandler extends DefaultHandler {
	
	private static final String TAG = "AdHandler";
	private static final String INTERSTITIAL_TAG = "mob_interstitial";
	private static final String BANNER1_TAG = "mob_banner1";
	private static final String BANNER2_TAG = "mob_banner2";
	
	private StringBuilder mCurrentData;
	private boolean parseThisTag;
	
	private Hashtable<AdvertisementType, Advertisement> mParsedAds;
	private Advertisement mCurrentAd;

	public Hashtable<AdvertisementType, Advertisement> getParsedData() {
		
		return mParsedAds;
	}
	
    @Override
    public void startDocument() throws SAXException {
    	mParsedAds = new Hashtable<AdvertisementType, Advertisement>();
    	mCurrentAd = new Advertisement();
    	mCurrentData = new StringBuilder();
    	parseThisTag = false;
    }
    
    @Override
    public void endDocument() throws SAXException {
    	mCurrentAd = null;
    	mCurrentData = null;
    }
    
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

    	if (localName.equals("width")) {
    		parseThisTag = true;
    	}
    	else if (localName.equals("height")) {
    		parseThisTag = true;
    	}
    	else if (localName.equals("d_number")) {
    		parseThisTag = true;
    	}
    	else if (localName.equals("f_number")) {
    		parseThisTag = true;
    	}
    	else if (localName.equals("html")) {
    		parseThisTag = true;
    	}
    }
    
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    	
    	String data = "";
    	if (parseThisTag) {
    		data = mCurrentData.toString();
    		mCurrentData = new StringBuilder();
    	}
    	
    	if (localName.equals("width")) {
    		parseThisTag = false;
    		mCurrentAd.width = Integer.parseInt(data.trim());
    	}
    	else if (localName.equals("height")) {
    		parseThisTag = false;
    		mCurrentAd.height = Integer.parseInt(data.trim());
    	}
    	else if (localName.equals("d_number")) {
    		parseThisTag = false;
    		mCurrentAd.duration = Integer.parseInt(data.trim());
    	}
    	else if (localName.equals("f_number")) {
    		parseThisTag = false;
    		mCurrentAd.frequency = Integer.parseInt(data.trim());
    	}
    	else if (localName.equals("html")) {
    		parseThisTag = false;
    		
    		Random rand = new Random(new Date().getTime());
    		data = data.trim().replace("[insert_random_number]", String.valueOf(((long)Integer.MAX_VALUE * 2) % rand.nextInt(Integer.MAX_VALUE)));
    		try {
				mCurrentAd.url = new URL(data);
			} catch (MalformedURLException e) {
				Log.e(TAG, e.getMessage());
			}
    	}
    	else if (localName.startsWith("mob_")) {
    		mCurrentAd.adType = localName;
    		
    		AdvertisementType type = null;
    		if (localName.equals(INTERSTITIAL_TAG))
    			type = AdvertisementType.INTERSTITIAL;
    		else if (localName.equals(BANNER1_TAG))
    			type = AdvertisementType.BANNER1;
    		else if (localName.equals(BANNER2_TAG))
    			type = AdvertisementType.BANNER2;
    		
    		if (type != null)
    			mParsedAds.put(type, mCurrentAd);
    		
    		mCurrentAd = new Advertisement();
    	}
    }
    
    @Override
    public void characters(char ch[], int start, int length) {
    	if (parseThisTag) {
    		mCurrentData.append(ch, start, length);
    	}
    }
}
