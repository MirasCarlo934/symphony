package symphony.bm.generics.jeep.response;


public class JeepSuccessResponse extends JeepResponse {
    public JeepSuccessResponse(String message) {
        super(true, message);
    }
}
