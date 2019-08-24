package bm.context.properties;

import java.util.HashMap;

import org.json.JSONObject;

import bm.context.HTMLTransformable;
import bm.context.OHItemmable;

/**
 * The PropertyType of a B_Property is described in the PVALCAT table in the DB
 * @author carlomiras
 */
public class PropertyType implements OHItemmable, HTMLTransformable {
	private String SSID;
	private String name;
	private String description;
	private String OHIcon;
	private int min;
	private int max;
	private HashMap<String, String> valueOHCommandTransform = new HashMap<String, String>(1);
	
	public PropertyType(String SSID, String name, String description, /*PropertyMode mode, */String OHItem, 
			int min, int max) {
		this.SSID = SSID;
		this.OHIcon = OHItem;
		this.min = min;
		this.max = max;
		this.name = name;
		this.description = description;
	}
	
	/**
	 * Checks if the supplied value is valid for this property type
	 * @param value The value to check
	 * @return <b>true</b> if valid, <b>false</b> if not
	 */
	public boolean checkValueTypeValidity(Object value) {
		if(min < 0 || max < 0) {
			return true;
		} else {
			//checks if value precedes with '%' char
			float val;
			if(value.toString().startsWith("%")) { //for dimmers from OH
				try {
					val = (max / 100) * Integer.parseInt(value.toString().substring(1));
					return true;
				} catch(NumberFormatException e) {
					return false;
				}
			}
			
			//checks if value is a number
			try {
				val = Float.parseFloat(value.toString());
				//checks if value is within min and max range
				if(min <= val && val <= max) {
					return true;
				} else {
					return false;
				}
			} catch(NumberFormatException e) {
				return false;
			}
		}
	}

	@Override
	public JSONObject[] convertToItemsJSON() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String convertToSitemapString() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Links a valid property value to an OpenHAB item command.
	 * <br><br>
	 * During property value updateRules, the <i>OHAdaptor</i> will transform the updated value into an OpenHAB item command
	 * to control the item in OpenHAB. (eg. 1 will be transformed to "ON" to control a Switch item)
	 * 
	 * @param propValue the property value that can be transformed
	 * @param OHCommand the OpenHAB command to which the value will be transformed to
	 */
	public void linkPropValueToOHCommand(String propValue, String OHCommand) {
		valueOHCommandTransform.put(propValue, OHCommand);
	}
	
	public String transformPropValueToOHCommand(String propValue) {
		String command = valueOHCommandTransform.get(propValue);
		if(command == null) {
			return propValue;
		} else {
			return command;
		}
	}
	
	public String convertToJavascript() {
		return "new PropertyType('" + SSID + "', '" + name + "', '" + description + "', '" + OHIcon + "'," + min + 
				"," + max + ")";
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getSSID() {
		return SSID;
	}
	
	public String getOHIcon() {
		return OHIcon;
	}
	
	public int getMin() {
		return min;
	}
	
	public int getMax() {
		return max;
	}
	
	/**
	 * @param sSID the sSID to set
	 */
	public void setSSID(String sSID) {
		SSID = sSID;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param oH_icon the oH_icon to set
	 */
	public void setOHIcon(String OHIcon) {
		this.OHIcon = OHIcon;
	}
	
	public void setMin(int min) {
		this.min = min;
	}
	
	public void setMax(int max) {
		this.max = max;
	}
}
