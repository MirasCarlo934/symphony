package symphony.bm.generics.jeep;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value @NonFinal
@AllArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class JeepMessage {
    String MRN;
    String MSN;
    String CID;
}
