package symphony.bm.bm_logic_devices.engines.requests.FileEngine;

import bm.main.engines.FileEngine;

/**
 * A FileEngine request that instructs the FileEngine to delete a specified string from the file handled by the
 * FileEngine.
 * @author carlomiras
 *
 */
public class DeleteStringFEReq extends FileEngineRequest {
	private String string;

	/**
	 * Creates a FileEngine request that is used to delete a specified string from the file handled by the
	 * FileEngine. <i><b>NOTE:</b> Recurring specified strings will all be deleted from the file!</i>
	 * 
	 * @param id The ID for this ERQS request
	 * @param string The string to be deleted from the file
	 */
	public DeleteStringFEReq(String id, FileEngine engine, String string) {
		super(id, engine, FileEngineRequestType.delete);
		this.string = string;
	}

	/**
	 * @return the string to be deleted from the file
	 */
	public String getString() {
		return string;
	}
}
