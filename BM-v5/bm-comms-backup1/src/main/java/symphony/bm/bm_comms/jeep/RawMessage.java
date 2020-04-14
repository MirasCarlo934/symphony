package symphony.bm.bm_comms.jeep;

import org.json.JSONObject;
import symphony.bm.bm_comms.Protocol;

public class RawMessage {
    private String msg;
    private Protocol protocol;
    private JSONObject checkingJSON = new JSONObject();

    public RawMessage(String msg, Protocol protocol) {
        this.msg = msg;
        this.protocol = protocol;
    }

    public JSONObject getCheckingJSON() { return checkingJSON; }

    public String getMessageString() {
        return msg;
    }

    public Protocol getProtocol() {
        return protocol;
    }
}
