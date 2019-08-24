package bm.jeep.vo.device;

import bm.comms.Protocol;
import bm.jeep.vo.JEEPRequest;
import org.json.JSONObject;

public class OutboundGenericRequest extends JEEPRequest {
    private String message;

    public OutboundGenericRequest(String rid, String cid, String rty, Protocol protocol,
                                  String message, String messageParam) {
        super(rid, cid, rty, protocol);
        addParameter(messageParam, message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
