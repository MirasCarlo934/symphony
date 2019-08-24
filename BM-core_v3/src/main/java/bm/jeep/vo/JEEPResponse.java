package bm.jeep.vo;

import bm.comms.Protocol;
import org.json.JSONObject;

public class JEEPResponse extends JEEPMessage {
	private boolean success;

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
	 */
	public JEEPResponse(String rid, String cid, String rty, Protocol protocol, boolean success) {
		super(rid, cid, rty, protocol);
		json.put("RID", rid);
		json.put("CID", cid);
		json.put("RTY", rty);
		json.put("success", success);
		this.success = success;
	}
	
	/**
	 * Constructor for inbound JEEPResponse.
	 * 
	 * @param json
	 * @param protocol
	 */
	public JEEPResponse(JSONObject json, Protocol protocol) {
		super(json, protocol);
		this.success = json.getBoolean("success");
	}
	
	public JEEPResponse(JSONObject json, Protocol protocol, boolean success) {
		super(json, protocol);
		json.put("success", success);
		this.success = success;
	}
	
	/**
	 * The default, most intuitive, and most logical constructor
	 */
	public JEEPResponse(JEEPMessage message, boolean success) {
		super(message.getJSON(), message.getProtocol());
		this.success = success;
	}
	
	/**
	 * Returns whether or not the request was successfully processed
	 * @return <b><i>True</i></b> if successful, <b><i>false</i></b> otherwise
	 */
	public boolean isSuccessful() {
		return success;
	}
}
