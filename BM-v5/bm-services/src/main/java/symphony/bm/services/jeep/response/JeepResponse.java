package symphony.bm.services.jeep;

import lombok.Value;

public abstract class JeepResponse extends JeepMessage {
    boolean success;
    
    public JeepResponse(JeepMessage message, boolean success) {
        super(message.getMRN(), message.getMSN(), message.getCID());
        this.success = true;
    }
}
