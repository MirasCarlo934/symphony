package symphony.bm.bm_comms.jeep.vo;

import org.json.JSONException;
import org.json.JSONObject;
import symphony.bm.bm_comms.Protocol;

public class JeepMessage {
	protected String rid;
	protected String cid;
	protected String rty;
	protected Protocol protocol;
	protected JSONObject json = new JSONObject();

	public JeepMessage(JSONObject json, Protocol protocol) {
		this.rid = json.getString("RID");
		this.rty = json.getString("RTY");
		this.cid = json.getString("CID");
		this.json = json;
		this.protocol = protocol;
	}
	
	public JeepMessage(String rid, String cid, String rty, Protocol protocol) {
		this.rid = rid;
		this.cid = cid;
		this.rty = rty;
		this.json.put("RID", rid);
		this.json.put("CID", cid);
		this.json.put("RTY", rty);
		this.protocol = protocol;
	}

	/**
	 * Returns the value attached to the parameter name specified.
	 *
	 * @param paramName The parameter name
	 * @return The value attached to the parameter, <b><i>null</i></b> if <b>paramName</b> does not exist
	 */
	public Object getParameter(String paramName) {
		Object o;
		try {
			o = json.get(paramName);
		} catch(JSONException e) {
			o = null;
		}
		return o;
	}

	/**
	 * Returns the string value attached to the parameter name specified.
	 *
	 * @param paramName The parameter name
	 * @return The value attached to the parameter, <b><i>null</i></b> if <b>paramName</b> does not exist or if it is not a string
	 */
	public String getString(String paramName) {
		return (String) getParameter(paramName);
	}

	/**
	 * Returns all the parameter keys of this JeepMessage.
	 *
	 * @return A string array containing the parameter keys
	 */
	public String[] getParameters() {
		String[] params = json.keySet().toArray(new String[0]);
		return params;
	}

	/**
	 * Sends this JeepMessage to the protocol assigned to it during instantiation.
	 */
	public void send() {
		protocol.getSender().send(this);
	}
	
	/**
	 * Returns the <i>Request ID</i> of this JeepMessage
	 * 
	 * @return The RID
	 */
	public String getRID() {
		return rid;
	}
	
	/**
	 * Returns the <i>Component ID</i> of this JeepMessage
	 * 
	 * @return The CID
	 */
	public String getCID() {
		return cid;
	}
	
	/**
	 * Returns the <i>Request Type</i> of this JeepMessage
	 * 
	 * @return The RTY
	 */
	public String getRTY() {
		return rty;
	}
	
	public JSONObject getJSON() {
		return json;
	}

	protected void addParameter(String name, Object value) {
		json.put(name, value);
	}
	
	/**
	 * Returns the Sender object for this JeepMessage
	 * 
	 * @return The Sender object
	 */
	public Protocol getProtocol() {
		return protocol;
	}
	
	@Override
	public String toString() {
		return json.toString();
	}
}
