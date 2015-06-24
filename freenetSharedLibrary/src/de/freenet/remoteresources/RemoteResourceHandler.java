package de.freenet.remoteresources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class RemoteResourceHandler extends DefaultHandler {

	private static final String TAG = "RemoteResourceHandler";
	
	private static Hashtable<String, String> mResources;
	
	private boolean inResources;
	private String currentKey;
	private StringBuilder currentValue;
	
	/**
	 * Tries to parse the input stream as a standard Android resource XML 
	 * @param inputStream The input stream to be parsed.
	 */
	public static void parseInputStream(InputStream inputStream) {
		
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
        	SAXParser sp;
		
			sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
        
			xr.setContentHandler(new RemoteResourceHandler());
        
			xr.parse(new InputSource(inputStream));
			
		} catch (ParserConfigurationException e) {
			Log.e(TAG, e.getMessage());
		} catch (SAXException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	/**
	 * Gets the resource parsed by the function parseInputStream(InputStream inputStream). That function also needs to
	 * be executed before this one.
	 * @return Returns a hashtable representation of the parsed XML file.
	 * @throws RemoteResourceException Exception thrown if parseInputStream(InputStream inputStream) was not executed
	 * before this function or if parsing of the XML file failed.
	 */
	public static Hashtable<String, String> getResources() throws RemoteResourceException {
		
		if (mResources == null)
			throw new RemoteResourceException("No XML parsed. Resources is null.");
		
		return mResources;
	}
	
	@Override
    public void startDocument() throws SAXException {
		mResources = new Hashtable<String, String>();
		inResources = false;
		currentValue = new StringBuilder();
    }
    
    @Override
    public void endDocument() throws SAXException {
    }
    
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {

    	if (localName.equals("resources")) {
    		inResources = true;
    	}
    	else if (inResources) {
    		currentKey = atts.getValue("name");
    	}
    }
    
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    	
    	if (localName.equals("resources")) {
    		inResources = false;
    	}
    	else if (inResources && currentKey != null && !currentKey.equals("")) {
    		mResources.put(currentKey, currentValue.toString().trim());
    		currentKey = "";
    		currentValue = new StringBuilder();
    	}
    }
    
    @Override
    public void characters(char ch[], int start, int length) {
    	if (inResources) {
    		currentValue.append(ch, start, length);
    	}
    }
}
