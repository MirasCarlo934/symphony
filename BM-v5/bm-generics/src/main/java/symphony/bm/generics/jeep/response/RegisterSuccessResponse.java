package symphony.bm.generics.jeep.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public class RegisterSuccessResponse extends JeepResponse {
    @JsonProperty("CID") String CID;
    
    public RegisterSuccessResponse(String MRN, String CID) {
        super(MRN, true, "Device " + CID + " registered");
        this.CID = CID;
    }
}
