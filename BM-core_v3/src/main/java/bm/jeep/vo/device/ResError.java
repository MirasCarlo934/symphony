package bm.jeep.vo.device;

import bm.comms.Protocol;
import bm.jeep.vo.JEEPRequest;

public class ResError extends JEEPErrorResponse {

	public ResError(String rid, String cid, String rty, Protocol protocol, String message) {
		super(rid, cid, rty, protocol, message);
	}
	
	public ResError(JEEPRequest request, String message) {
		super(request, message);
	}
	
	/**
	 * Creates an instance of ResError SOLELY for the use of internal ERQS error handling.
	 * @param source the source of the error, preferrably the name of the object that issued
	 * 		the error
	 * @param message the error message
	 
	public ResError(String source, String message) {
		super(source, "BM", "N/A", false);
		super.addParameter("message", message);
		this.message = message;
	}*/
}
