package symphony.bm.bm_comms.jeep;

import org.json.JSONException;
import org.json.JSONObject;
import symphony.bm.bm_comms.Protocol;

public class JeepMessage extends JSONObject {
	private Protocol protocol;

	public JeepMessage(RawMessage rawMsg) {
		super(rawMsg.getMessageString());
		this.protocol = rawMsg.getProtocol();
	}
	public JeepMessage(String s, Protocol protocol) {
		super(s);
		this.protocol = protocol;
	}
	public JeepMessage(String mrn, String cid, String msn, Protocol protocol) {
		super();
		put("MRN", mrn);
		put("MSN", msn);
		put("CID", cid);
		this.protocol = protocol;
	}
	public JeepMessage(boolean success, String msg, Protocol protocol) {
		put("success", success);
		put("msg", msg);
		this.protocol = protocol;
	}

	public void send() {
		protocol.getSender().send(this);
	}

	public void sendAsError() {
		protocol.getSender().sendErrorMessage(this);
	}

	public String getMRN() {
		return getString("MRN");
	}

	public String getCID() {
		return getString("CID");
	}

	public String getMSN() {
		return getString("MSN");
	}
	
	/**
	 * Returns the Sender object for this JeepMessage
	 * 
	 * @return The Sender object
	 */
	public Protocol getProtocol() {
		return protocol;
	}
}
