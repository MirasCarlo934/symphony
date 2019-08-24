package bm.cir.objects;

public enum Conditional {
	IF, WHEN;
	
	public static Conditional parseConditional(String str) throws IllegalArgumentException {
		if(str.equalsIgnoreCase(IF.toString())) {
			return IF;
		}
		else if(str.contentEquals(WHEN.toString())) {
			return WHEN;
		}
		else {
			throw new IllegalArgumentException("String specified is not a CIRS conditional!");
		}
	}
}
