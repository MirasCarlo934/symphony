package bm.jeep.vo.device;

import bm.comms.Protocol;
import org.json.JSONException;
import org.json.JSONObject;

import bm.jeep.vo.JEEPRequest;

public class ReqRequest extends JEEPRequest {
	private JSONObject json;

	public ReqRequest(JSONObject json, Protocol protocol) {
		super(json, protocol);
		this.json = json;
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
		return (String)getParameter(paramName);
	}
	
	public String[] getParameters() {
		String[] params = json.keySet().toArray(new String[0]);
		return params;
	}
	
	public JSONObject getJSON() {
		return json;
	}
}
