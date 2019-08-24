package bm.jeep.vo.device;

import bm.comms.Protocol;
import bm.jeep.vo.JEEPRequest;
import bm.jeep.vo.JEEPResponse;

public class OutboundRegistrationResponse extends JEEPResponse {
	public String id; //ssid of the newly registered component
	public String topic; //topic of the newly registered component

	public OutboundRegistrationResponse(String rid, String cid, String rty, Protocol protocol,
                                        String idParam, String topicParam, String id, String topic) {
		super(rid, cid, rty, protocol, true);
		this.id = id;
		this.topic = topic;
		addParameter(idParam, id);
		addParameter(topicParam, topic);
	}

	public String getID() {
		return id;
	}

	public String getTopic() {
		return topic;
	}
}
