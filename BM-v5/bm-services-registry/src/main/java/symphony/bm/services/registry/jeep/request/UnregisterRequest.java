package symphony.bm.services.registry.jeep.request;

import symphony.bm.services.registry.jeep.JeepMessage;

public class UnregisterRequest extends JeepMessage {
    public UnregisterRequest(String MRN, String MSN, String CID) {
        super(MRN, MSN, CID);
    }
}
