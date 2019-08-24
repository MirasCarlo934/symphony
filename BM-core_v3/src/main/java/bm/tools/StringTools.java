package bm.tools;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class StringTools {
	private static final Logger logger = Logger.getLogger(StringTools.class);
	
	/**
	 * Returns the specified string enclosed in double quotes. This saves the hassle of doing the enclosure manually.
	 * 
	 * @param str the string needed to be enclosed
	 * @return
	 */
	public static String encloseInQuotes(String str) {
		return '"' + str + '"';
	}

	/**
	 * Returns the String representation of a JSONObject that uses single quotes instead of double quotes. This is useful especially for
	 * openhab bindings where double quotes cannot be used.
	 * 
	 * @param json the JSONObject
	 * @return the String representation of the JSONObject
	 */
	public static String toSingleQuotedJSONString(JSONObject json) {
		String str = "{";
		Iterator<String> keys = json.keys();
		while(keys.hasNext()) {
			String key = keys.next();
			str += "'" + key + "':"; //key
			str += "'" + json.get(key).toString() + "',"; //value
		}
		str = str.substring(0, str.length() - 1) + "}";
		return str;
	}
	
	/**
	 * Injects additional Strings into a main String. This is extremely useful when building a String that includes a
	 * mixture of constant Strings and multiple variables. <i>This method simplifies that processRequest.</i><br><br>
	 * 
	 * A classic example of a String built with mixed constant Strings and variables: <br>
	 * <i>String s = "Hey there, " + name + ". Today is " + date + " and good day!";</i><br><br>
	 * 
	 * With the given variables "Carlo", and "6/15/2016" respectively, this results to:<br>
	 * <i>Hey there, Carlo. Today is 6/15/2016 and good day!</i><br><br>
	 * 
	 * This example perfectly illustrates the difficulty of constructing a String that includes constant Strings and
	 * variables. <b><i>This method simplifies this processRequest by allowing the user to build the template String first and then
	 * placing a constant field String all over the template String where the variables will be injected into.</b></i><br><br>
	 * 
	 * Example:<br>
	 * <i>injectStrings("Hey there, %s. Today is %s and I good day!", new String[]{name, date}, "%s");</i><br><br>
	 * 
	 * This results to:<br>
	 * <i>Hey there, Carlo. Today is 6/15/2016 and good day!</i><br><br>
	 * 
	 * <b>With this particular call on the method, the method replaces all the "%s" Strings in the main String with the
	 * variables provided in the second field respectively.</b>
	 * 
	 * @param str the template String where additional Strings will be injected.
	 * @param strs the array of Strings to be injected into the main String. These Strings <b>MUST NOT</b> contain a
	 * 		sequence similar to the <b>field_str</b>
	 * @param field_str the String that recurs within the document which will be replaced by the new Strings. 
	 * 		<b><i>The length of this String must not be less than 2 and also must not contain "$" and "\".</b></i>
	 * @return
	 * @throws StringInjectionException when the provided field String has a length of less than 2, if the size of the
	 * 		provided String array to be injected is not equal with the amount of field Strings in the main String or
	 * 		if the <b>field_str</b> contains an invalid character.
	 */
	public static String injectStrings(String str, String[] strs, String field_str) {
		try {
			if(field_str.length() < 2) {
				throw new StringInjectionException("field_str is has a length of less than 2");
			/*} else if(str.(field_str) + 1 != strs.length) {
				logger.debug(strs.length);
				logger.debug(str.lastIndexOf(field_str));
				throw new StringInjectionException("Size of String array to be injected is not equal with the amount of"
						+ " field Strings in the main String!");*/
			} else if(field_str.contains("$") || field_str.contains("\\")) {
				throw new StringInjectionException("Field string contains an invalid character!");
			}
			
			for(int i = 0; i < strs.length; i++) {
				String inject = strs[i];
				str = str.replaceFirst(field_str, inject);
			}
		} catch(StringInjectionException e) {
			logger.error(e);
		}
		
		return str;
	}
	
	/**
	 * Extends original injectStrings() by accommodating varying field strings. This is extremely useful when building a String that includes a
	 * mixture of constant Strings and multiple variables. <i>This method simplifies that processRequest.</i><br><br>
	 * 
	 * A classic example of a String built with mixed constant Strings and variables: <br>
	 * <i>String s = "Hey there, " + name + ". Today is " + date + " and good day!";</i><br><br>
	 * 
	 * With the given variables "Carlo", and "6/15/2016" respectively, this results to:<br>
	 * <i>Hey there, Carlo. Today is 6/15/2016 and good day!</i><br><br>
	 * 
	 * This example perfectly illustrates the difficulty of constructing a String that includes constant Strings and
	 * variables. <b><i>This method simplifies this processRequest by allowing the user to build the template String first and then
	 * placing variable field Strings all over the template String where the variables will be injected into.</b></i><br><br>
	 * 
	 * Example:<br>
	 * <i>injectStrings("Hey there, {name}. Today is {date} and good day!", new String[]{name, date}, "%s");</i><br><br>
	 * 
	 * This results to:<br>
	 * <i>Hey there, Carlo. Today is 6/15/2016 and good day!</i><br><br>
	 * 
	 * <b>With this particular call on the method, the method replaces all the "%s" Strings in the main String with the
	 * variables provided in the second field respectively.</b>
	 * 
	 * @param str the template String where additional Strings will be injected. The varying field Strings must
	 * 		be enclosed in curly brackets <b>{ }</b>.
	 * @param values the HashMap of Strings to be injected into the main String. The key Strings will be used to 
	 * 		map the value Strings to be injected to <b>str</b>.
	 * @param encloser The characters that enclose the key String. The first element of this array must be the
	 * 		first enclosing character/s while the second element must be the final enclosing character/s.
	 * 		<b>There must only be two elements in this array.</b>
	 * @return
	 * @throws StringInjectionException when the provided field String has a length of less than 2, if the size of the
	 * 		provided String array to be injected is not equal with the amount of field Strings in the main String or
	 * 		if the <b>field_str</b> contains an invalid character.
	 */
	public static String injectStrings(String str, HashMap<String, String> values, 
			String[] encloser) {
		String s = str;
		String[] keys = values.keySet().toArray(new String[0]);
		
		for(int i = 0; i < values.size(); i++) {
			String key = keys[i];
			String value = values.get(key);
			s = s.replace(encloser[0] + key + encloser[1], value);
		}
		
		return s;
	}
	
	/**
	 * Capitalizes the first letter of the specified String.
	 * 
	 * @param str the String to be capitalized, must have a length of more than one character otherwise <b>null</b> will
	 * 		be returned
	 * @return the <b>str</b> with its first letter capitalized
	 */
	public static String capitalizeFirstLetter(String str) {
		String s = null;
		if(str.length() > 1) {
			s = str.substring(0, 1).toUpperCase() + str.substring(1);
		}
		return s;
	}
	
	public static String generateRandomString(int length) {
		final String chars = "/=ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String str = "";
		Random rnd = new Random();
		while(str.length() < length) {
			String c = String.valueOf(chars.charAt(rnd.nextInt(chars.length() - 1)));
			if(rnd.nextBoolean()) c.toLowerCase();
			str += c;
		}
		
		return str;
	}
	
	public static class StringInjectionException extends Exception {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -4884447064580215813L;

		public StringInjectionException() {
			super();
		}
		
		public StringInjectionException(String message) {
			super(message);
		}
	}
}
