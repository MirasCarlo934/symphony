package bm.context;

public interface HTMLTransformable {
	
	/**
	 * Converts this Object into Javascript code. Code format is Object-specific.
	 * 
	 * @return A String containing the equivalent Javascript code for this Object.
	 */
	public String convertToJavascript();
}
