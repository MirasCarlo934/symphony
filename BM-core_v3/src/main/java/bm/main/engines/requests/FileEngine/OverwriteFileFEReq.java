package bm.main.engines.requests.FileEngine;

import bm.main.engines.FileEngine;

/**
 * A FileEngine request that instructs the FileEngine to write a specified array of lines over the existing contents 
 * of the file that is handled by the FileEngine
 * @author carlomiras
 *
 */
public class OverwriteFileFEReq extends FileEngineRequest {
	private String[] lines;

	/**
	 * Creates a FileEngine request that writes a specified array of lines over the existing contents of the 
	 * file that is handled by the FileEngine
	 * 
	 * @param id the ID for this engine request
	 * @param lines The String[] array that contains all the lines to be overwritten over the existing contents of
	 * 		the FileEngine file
	 */
	public OverwriteFileFEReq(String id, FileEngine engine, String[] lines) {
		super(id, engine, FileEngineRequestType.overwrite);
		this.lines = lines;
	}
	
	/**
	 * Returns all the lines to be overwritten over the existing contents of the FileEngine file
	 * 
	 * @return A String[] array that contains all the lines
	 */
	public String[] getLines() {
		return lines;
	}
}
