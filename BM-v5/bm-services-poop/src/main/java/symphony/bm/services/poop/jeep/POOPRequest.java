package symphony.bm.services.poop.jeep;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import symphony.bm.generics.jeep.JeepMessage;

@Value
public class POOPRequest extends JeepMessage {
    @JsonProperty("prop-index") @Getter(onMethod_ = {@JsonProperty("prop-index")}) String propIndex;
    @JsonProperty("prop-value") @Getter(onMethod_ = {@JsonProperty("prop-value")}) String propValue;
    
    public POOPRequest(@NonNull String MRN, @NonNull String MSN, @NonNull String CID,
                       @NonNull @JsonProperty("prop-index") String propIndex,
                       @NonNull @JsonProperty("prop-value") String propValue) {
        super(MRN, MSN, CID);
        this.propIndex = propIndex;
        this.propValue = propValue;
    }
}
