package symphony.bm.generics.jeep.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public class RegisterResponse extends JeepResponse {
    @JsonProperty("CID") @Getter(onMethod_ = {@JsonProperty("CID")}) String CID;
    
    public RegisterResponse(String CID) {
        super(true, "Device " + CID + " registered");
        this.CID = CID;
    }
}
