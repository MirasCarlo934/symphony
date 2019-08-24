package bm.jeep.vo.device;

import bm.comms.Protocol;
import bm.jeep.vo.JEEPRequest;
import org.json.JSONObject;

public class OutboundRegistrationRequest extends JEEPRequest {
    private String id; //ssid of the newly registered component
    private String topic; //topic of the newly registered component

    public OutboundRegistrationRequest(JEEPRequest request, String idParam, String topicParam, String id,
                                        String topic) {
        super(request);
        this.id = id;
        this.topic = topic;
        addParameter(idParam, id);
        addParameter(topicParam, topic);
    }

    public OutboundRegistrationRequest(String MAC, String CID, String RTY, Protocol protocol, String idParam,
                                        String topicParam, String id, String topic) {
        super(MAC, CID, RTY, protocol);
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
