package symphony.bm.services.jeep;

import lombok.Value;

@Value
public class JeepResponse extends JeepMessage {
    boolean success;
    String msg;
    
    public JeepResponse(JeepMessage message) {
        super(message.getMRN(), message.getMSN(), message.getCID());
        this.success = true;
        this.msg = null;
    }
    public JeepResponse(JeepMessage message, String errorMsg) {
        super(message.getMRN(), message.getMSN(), message.getCID());
        this.success = false;
        this.msg = errorMsg;
    }
}
