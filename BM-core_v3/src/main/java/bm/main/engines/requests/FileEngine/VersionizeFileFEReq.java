package bm.main.engines.requests.FileEngine;

import bm.main.engines.FileEngine;

public class VersionizeFileFEReq extends FileEngineRequest {

	public VersionizeFileFEReq(String id, FileEngine engine) {
		super(id, engine, FileEngineRequestType.versionize);
	}
}
