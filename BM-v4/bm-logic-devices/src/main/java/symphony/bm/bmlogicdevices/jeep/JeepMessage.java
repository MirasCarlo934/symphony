package symphony.bm.bmlogicdevices.jeep;

import org.json.JSONObject;

public class JeepMessage extends JSONObject {

    public JeepMessage(String messageString) {
        super(messageString);
    }

    public String getMRN() {
        return getString("MRN");
    }

    public String getMSN() {
        return getString("MSN");
    }

    public String getCID() {
        return getString("CID");
    }
}
