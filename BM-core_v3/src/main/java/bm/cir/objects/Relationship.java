package bm.cir.objects;

public enum Relationship {
	AND, OR, NONE;
	
	/**
	 * Parses a CIR argument relationship from the specified string
	 * 
	 * @param str The string where the CIR argument relationship will be parsed from
	 * @return A Relationship enum representing the CIR argument relationship. The Relationship enum includes
	 * 		<i>NONE</i> which signifies that the CIR argument does not have any relationshp	with other CIR arguments
	 * 		(standalone argument).
	 */
	public static Relationship parseString(String str) {
		if(str.equalsIgnoreCase("AND"))
			return AND;
		else if(str.equalsIgnoreCase("OR"))
			return OR;
		else
			return NONE;
	}
}
