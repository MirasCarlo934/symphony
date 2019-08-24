package bm.context;

import org.json.JSONObject;

public interface OHItemmable {
	/**
	 * Converts the object to a specific JSON format which can be sent to the REST API of OpenHAB to register this 
	 * object as an item.
	 * 
	 * @return A JSONObject[] array that contains the item representation of this object
	 */
	abstract JSONObject[] convertToItemsJSON();
	
	/**
	 * Converts the object to a specific String format which can be written into the sitemap file of OpenHAB to 
	 * register this object as a sitemap element.
	 * 
	 * @return A String that contains the sitemap element representation of this object
	 */
	abstract String convertToSitemapString();
}
