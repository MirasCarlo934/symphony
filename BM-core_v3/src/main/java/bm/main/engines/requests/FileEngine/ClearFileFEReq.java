package bm.main.engines.requests.FileEngine;

import bm.main.engines.FileEngine;

/**
 * A FileEngine request that instructs the FileEngine to clear the contents of the file it is handling.
 * 
 * @author carlomiras
 *
 */
public class ClearFileFEReq extends FileEngineRequest {

	/**
	 * Creates a FileEngine request that instructs the FileEngine to clear the contents of the file it is handling.
	 * 
	 * @param id the ID of this EngineRequest
	 */
	public ClearFileFEReq(String id, FileEngine engine) {
		super(id, engine, FileEngineRequestType.clear);
	}
}
