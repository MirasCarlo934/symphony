package symphony.bm.bm_comms.jeep.vo;

import symphony.bm.bm_comms.Protocol;

public class JeepErrorResponse extends JeepResponse {
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
	public JeepErrorResponse(String rid, String cid, String rty, Protocol protocol, String message) {
		super(rid, cid, rty, protocol, false);
		this.message = message;
		addParameter("errormsg", message);
	}

	/**
	 * The default, most intuitive, and most logical constructor
	 */
	public JeepErrorResponse(JeepRequest request, String message) {
		super(request, false);
		this.message = message;
		addParameter("errormsg", message);
	}

	public JeepErrorResponse(String message, Protocol protocol) {
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
