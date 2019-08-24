package bm.jeep.vo;

import bm.comms.Protocol;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class JEEPRequest extends JEEPMessage{
	
	public JEEPRequest(String rid, String cid, String rty, Protocol protocol) {
		super(rid, cid, rty, protocol);
	}

	public JEEPRequest(JSONObject json, Protocol protocol) {
		super(json, protocol);
	}
	
	public JEEPRequest(JEEPRequest request) {
		super(request.json, request.protocol);
	}
}
