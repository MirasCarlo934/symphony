package bm.main.engines.requests.FileEngine;

import bm.main.engines.FileEngine;

/**
 * A FileEngine request that instructs the FileEngine to insert a specified string into the file handled by the 
 * FileEngine.
 * @author carlomiras
 *
 */
public class InsertToFileFEReq extends FileEngineRequest {
	private String line;
	private String lineMarker;
	private boolean afterLineMarker;

	/**
	 * Creates a FileEngine request that is used to insert a specified string into the file handled by the 
	 * FileEngine. The string is inserted either before or after the line that contains the specified line marker.
	 * 
	 * @param id The ID for this ERQS request
	 * @param line The line of string to be inserted into the file
	 * @param lineMarker A string which will be used to see where the string will be inserted to. The first line 
	 * 		that contains this lineMarker will be chosen as the <i>reference line</i> where 
	 * 		the string will be inserted to.
	 * @param afterLineMarker <b><i>True</i></b> if the string will be inserted <b>after</b> the reference 
	 * 		line, <b><i>false</b></i> if the string will be inserted <b>before</b> the reference line
	 * @param afterLineMarker
	 */
	public InsertToFileFEReq(String id, FileEngine engine, String line, String lineMarker, boolean afterLineMarker) {
		super(id, engine, FileEngineRequestType.insert);
		this.line = line;
		this.lineMarker = (lineMarker);
		this.afterLineMarker = (afterLineMarker);
	}

	/**
	 * @return the line to be inserted into the file
	 */
	public String getLine() {
		return line;
	}

	/**
	 * @return the lineMarker which will be used to see where the string will be inserted into
	 */
	public String getLineMarker() {
		return lineMarker;
	}

	/**
	 * @return the afterLineMarker if the string will be inserted after the line marker or before
	 */
	public boolean isAfterLineMarker() {
		return afterLineMarker;
	}
}
