package bm.context.properties;

public enum PropertyMode {
	I, O, IO, Null;
	
//	private String string;
//	
//	private PropertyMode(String string) {
//		this.string = string;
//	}

	/**
	 * Parses a string and returns the equivalent PropertyMode enum.
	 * @param str
	 * @return The PropertyMode enum, or PropertyMode.Null if string is not "I", "O", or "IO".
	 */
	public static PropertyMode parseFromString(String str) {
		if(str.equals("I"))
			return I;
		else if(str.equals("O"))
			return O;
		else if(str.equals("IO"))
			return IO;
		else
			return Null;
	}
}
