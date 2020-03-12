package symphony.bm.bm_comms.jeep.vo;

import org.json.JSONObject;
import symphony.bm.bm_comms.Protocol;

public class JeepRequest extends JeepMessage {
	
	public JeepRequest(String rid, String cid, String rty, Protocol protocol) {
		super(rid, cid, rty, protocol);
	}

	public JeepRequest(JSONObject json, Protocol protocol) {
		super(json, protocol);
	}
	
	public JeepRequest(JeepRequest request) {
		super(request.json, request.protocol);
	}
}
