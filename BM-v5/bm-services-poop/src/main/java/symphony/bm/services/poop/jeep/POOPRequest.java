package symphony.bm.services.poop.jeep;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

@Value
@AllArgsConstructor
public class POOPRequest {
    @JsonProperty("MRN") @Getter(onMethod_ = {@JsonProperty("MRN")}) String MRN;
    @JsonProperty("MSN") @Getter(onMethod_ = {@JsonProperty("MSN")}) String MSN;
    @JsonProperty("CID") @Getter(onMethod_ = {@JsonProperty("CID")}) String CID;
    @JsonProperty("prop-index") @Getter(onMethod_ = {@JsonProperty("prop-index")}) String propIndex;
    @JsonProperty("prop-value") @Getter(onMethod_ = {@JsonProperty("prop-value")}) String propValue;
}
