package bm.jeep.vo.device;

import bm.comms.Protocol;
import org.json.JSONObject;

import bm.jeep.vo.JEEPRequest;

public class ReqDeleteRoom extends JEEPRequest {
	public String roomID;

	public ReqDeleteRoom(JSONObject json, Protocol protocol, String roomIDParam) {
		super(json, protocol);
		this.roomID = json.getString(roomIDParam);
	}

//	public ReqDeleteRoom(String RID, String CID, String RTY, String roomID) {
//		super(RID, CID, RTY);
//		this.roomID = roomID;
//	}
	
	public ReqDeleteRoom(JEEPRequest request, String roomIDParam) {
		super(request.getJSON(), request.getProtocol());
		this.roomID = request.getString(roomIDParam);
	}
}
