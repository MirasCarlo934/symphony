package symphony.bm.generics.jeep;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class JeepMessage {
    @Getter private final String MRN;
    
    public JeepMessage(String MRN) {
        this.MRN = MRN;
    }
}
