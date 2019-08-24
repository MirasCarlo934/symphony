package bm.context.properties;

public enum PropertyValueType {
	digital, analog, analogHue, percent, string, innate;
	
	/**
	 * Parses a specified String and returns a PropertyValueType if the String is equal
	 * to a PropertyValueType.
	 * @param str The String to be parsed
	 * @return a PropertyValueType. <b><i>Null</i></b> if str is not a valid PropertyValueType
	 */
	public static PropertyValueType parsePropValTypeFromString(String str) {
		PropertyValueType pvt = null;
		if(str.equalsIgnoreCase("D")) //digital
			pvt = digital;
		else if(str.equalsIgnoreCase("A1")) //analog
			pvt = analog;
		else if(str.equalsIgnoreCase("A2")) //percent
			pvt = percent;
		else if(str.equalsIgnoreCase("A3")) //analogHue
			pvt = analogHue;
		else if(str.equalsIgnoreCase("STR")) //string
			pvt = string;
		
		return pvt;
	}
}
