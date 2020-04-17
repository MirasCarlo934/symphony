package symphony.bm.services.poop.jeep;

public class POOPFailResponse extends POOPResponse {
    public POOPFailResponse(String errorMsg) {
        super(false, errorMsg);
    }
}
