package bm.jeep.vo.device;

import bm.comms.Protocol;
import org.json.JSONObject;

import bm.jeep.vo.JEEPRequest;

public class ReqPOOP extends JEEPRequest {
	public int propIndex;
	public Object propValue;

	public ReqPOOP(JSONObject json, Protocol protocol, String propIndexParam, String propValParam) {
		super(json, protocol);
		assignVariablesFromJSON(json, propIndexParam, propValParam);
	}
	
	public ReqPOOP(JEEPRequest request, String propIndexParam, String propValParam) {
		super(request.getJSON(), request.getProtocol());
		assignVariablesFromJSON(request.getJSON(), propIndexParam, propValParam);
	}
	
	public ReqPOOP(String rid, String cid, String rty, Protocol protocol, String propIndexParam, String propValParam,
			int propIndex, Object propVal) {
		super(rid, cid, rty, protocol);
		json.put(propIndexParam, propIndex);
		json.put(propValParam, propVal);
		assignVariablesFromJSON(json, propIndexParam, propValParam);
	}
	
	private void assignVariablesFromJSON(JSONObject json, String propIndexParam, String propValParam) {
		propIndex = json.getInt(propIndexParam);
		propValue = json.get(propValParam);
	}
}