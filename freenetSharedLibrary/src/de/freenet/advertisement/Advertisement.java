package de.freenet.advertisement;

import java.io.Serializable;
import java.net.URL;

public class Advertisement extends Object implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public Advertisement() {
		adType = "";
		webContent = "";
		width = 0;
		height = 0;
		duration = 0;
		frequency = 0;
		frequencyCounter = 0;
	}
	
	public String adType;
	public URL url;
	public String webContent;
	public int width;
	public int height;
	public int duration;
	public int frequency;
	public int frequencyCounter;
}