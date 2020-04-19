package symphony.bm.generics.jeep.request;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import symphony.bm.generics.jeep.JeepMessage;

@Value @NonFinal
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class JeepRequest extends JeepMessage {
    String MSN;
    String CID;
    
    public JeepRequest(String MRN, String MSN, String CID) {
        super(MRN);
        this.MSN = MSN;
        this.CID = CID;
    }
}
