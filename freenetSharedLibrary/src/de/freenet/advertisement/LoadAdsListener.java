package de.freenet.advertisement;

import de.freenet.advertisement.AdManager.AdvertisementType;

public interface LoadAdsListener {
	
	/**
	 * Function is called when the advertisement XML is fully downloaded and parsed into an internal structure.
	 */
	public void adsLoadedFromServer();
	
	/**
	 * Informs what kind of advertisement will be shown. 
	 * @param type the type of advertisement
	 */
	public void willShowAd(AdvertisementType type);
	
	/**
	 * This method is called when there is time to show a banner. The banner is provides as an AdWebView.
	 * In this method, the view manipulation should take place.
	 * @param webView The AdWebView containing the actual banner.
	 * @param width width of the banner
	 * @param height height of the banner
	 */
	public void timeToShowBanner(AdWebView webView, int width, int height);
}
