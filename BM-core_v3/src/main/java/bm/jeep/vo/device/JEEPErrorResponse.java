package bm.jeep.vo.device;

import bm.comms.Protocol;

import bm.jeep.vo.JEEPRequest;
import bm.jeep.vo.JEEPResponse;

public class JEEPErrorResponse extends JEEPResponse {
	private boolean complete = true;
	private String message;

	/**
	 * The constructor in case papa requests for it
	 * @param rid
	 * @param cid
	 * @param rty
	 
	public AbstResponse(String rid, String cid, String rty, boolean success) {
		this.rid = rid;
		this.cid = cid;
		this.rty = rty;
		this.success = success;
		json.put("RID", rid);
		json.put("RTY", rty);
		json.put("success", success);
	}*/
	
	/**
	 * Not the default, most intuitive, and most logical constructor
	 * @param rid
	 * @param cid
	 */
	public JEEPErrorResponse(String rid, String cid, String rty, Protocol protocol, String message) {
		super(rid, cid, rty, protocol, false);
		this.message = message;
		addParameter("errormsg", message);
	}
	
	/**
	 * The default, most intuitive, and most logical constructor
	 */
	public JEEPErrorResponse(JEEPRequest request, String message) {
		super(request, false);
		this.message = message;
		addParameter("errormsg", message);
	}
	
	public JEEPErrorResponse(String message, Protocol protocol) {
		super(null, null, null, protocol, false);
		this.message = message;
		addParameter("errormsg", message);
		complete = false;
	}
	
	/**
	 * Returns the error message contained by this DeviceJEEPErrorResponse.
	 * 
	 * @return The error message
	 */
	public String getMessage() {
		return message;
	}
	
	public boolean isComplete() {
		return complete;
	}
}
