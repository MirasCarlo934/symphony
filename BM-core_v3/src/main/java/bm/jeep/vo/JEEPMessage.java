package bm.jeep.vo;

import bm.comms.Protocol;
import org.json.JSONException;
import org.json.JSONObject;

import bm.comms.Sender;

public class JEEPMessage {
	protected String rid;
	protected String cid;
	protected String rty;
	protected Protocol protocol;
	protected JSONObject json = new JSONObject();

	public JEEPMessage(JSONObject json, Protocol protocol) {
		this.rid = json.getString("RID");
		this.rty = json.getString("RTY");
		this.cid = json.getString("CID");
		this.json = json;
		this.protocol = protocol;
	}
	
	public JEEPMessage(String rid, String cid, String rty, Protocol protocol) {
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
	 * Returns all the parameter keys of this JEEPMessage.
	 *
	 * @return A string array containing the parameter keys
	 */
	public String[] getParameters() {
		String[] params = json.keySet().toArray(new String[0]);
		return params;
	}

	/**
	 * Sends this JEEPMessage to the protocol assigned to it during instantiation.
	 */
	public void send() {
		protocol.getSender().send(this);
	}
	
	/**
	 * Returns the <i>Request ID</i> of this JEEPMessage
	 * 
	 * @return The RID
	 */
	public String getRID() {
		return rid;
	}
	
	/**
	 * Returns the <i>Component ID</i> of this JEEPMessage
	 * 
	 * @return The CID
	 */
	public String getCID() {
		return cid;
	}
	
	/**
	 * Returns the <i>Request Type</i> of this JEEPMessage
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
	 * Returns the Sender object for this JEEPMessage
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
