package symphony.bm.services.registry.jeep.request;

import symphony.bm.generics.jeep.request.JeepRequest;

public class UnregisterRequest extends JeepRequest {
    public UnregisterRequest(String MRN, String MSN, String CID) {
        super(MRN, MSN, CID);
    }
}
