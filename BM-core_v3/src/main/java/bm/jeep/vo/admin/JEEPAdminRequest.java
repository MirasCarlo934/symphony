package bm.jeep.vo.admin;

import bm.comms.Protocol;
import org.json.JSONObject;

import bm.jeep.vo.JEEPRequest;

public class JEEPAdminRequest extends JEEPRequest {
	private String pwd;

	public JEEPAdminRequest(JSONObject json, Protocol protocol) {
		super(json, protocol);
		this.pwd = json.getString("pwd");
	}
	
	/**
	 * Returns the pwd of this JEEPAdminRequest.
	 * <br/><br/>
	 * <b>NOTE:</b> The pwd is an encrypted string.
	 * 
	 * @return The encrypted pwd string
	 */
	public String getPwd() {
		return pwd;
	}

//	public JEEPAdminRequest(JEEPRequest request) {
//		super(request);
//	}
}
