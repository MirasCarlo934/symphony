package symphony.bm.generics.jeep.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import symphony.bm.generics.jeep.JeepMessage;

public abstract class JeepResponse extends JeepMessage {
    @Getter final boolean success;
    @Getter String message;
    
    public JeepResponse(String MRN, boolean success, String message) {
        super(MRN);
        this.success = success;
        this.message = message;
    }
}
