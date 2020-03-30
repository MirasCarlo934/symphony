package symphony.bm.bmservicespoop.jeep;

import org.json.JSONObject;

public class JeepMessage extends JSONObject {

    public JeepMessage(String messageString) {
        super(messageString);
    }
    public JeepMessage(String mrn, String msn, String cid) {
        put("MRN", mrn);
        put("MSN", msn);
        put("CID", cid);
    }
    public JeepMessage(String mrn, String msn, String cid, boolean success) {
        this(mrn, msn, cid);
        put("success", success);
    }
    public JeepMessage(String mrn, String msn, String cid, boolean success, String msg) {
        this(mrn, msn, cid, success);
        put("msg", msg);
    }
    public JeepMessage(boolean success, String msg) {
        put("success", success);
        put("msg", msg);
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
