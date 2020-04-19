package symphony.bm.services.poop.jeep;

import symphony.bm.generics.jeep.response.JeepResponse;

public class POOPSuccessResponse extends JeepResponse {
    public POOPSuccessResponse(String MRN) {
        super(MRN, true, "POOP successful");
    }
}
