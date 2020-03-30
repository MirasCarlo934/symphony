package symphony.bm.bmservicespoop.jeep;

public class JeepResponse extends JeepMessage {
    public JeepResponse(JeepMessage message) {
        super(message.getMRN(), message.getMSN(), message.getCID(), true);
    }
    public JeepResponse(JeepMessage message, String errorMsg) {
        super(message.getMRN(), message.getMSN(), message.getCID(), false);
        put("error", errorMsg);
    }
}
