package symphony.bm.generics.jeep.request;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;
import symphony.bm.generics.jeep.JeepMessage;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class JeepRequest extends JeepMessage {
    @Getter private final String MSN;
    @Getter private final String CID;
    
    public JeepRequest(String MRN, String MSN, String CID) {
        super(MRN);
        this.MSN = MSN;
        this.CID = CID;
    }
}
