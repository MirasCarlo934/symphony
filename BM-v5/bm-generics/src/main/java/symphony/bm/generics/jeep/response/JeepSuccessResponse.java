package symphony.bm.generics.jeep.response;


public class JeepSuccessResponse extends JeepResponse {
    public JeepSuccessResponse(String MRN, String message) {
        super(MRN, true, message);
    }
}
