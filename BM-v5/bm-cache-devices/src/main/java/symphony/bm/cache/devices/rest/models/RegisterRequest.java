package symphony.bm.cache.devices.rest.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.json.JSONObject;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class RegisterRequest {
    @Getter private final String CID;
    @Getter private final String name;
    @Getter private final String room;
    
    public RegisterRequest(String CID, String name, String room) {
        this.CID = CID;
        this.name = name;
        this.room = room;
    }
    
    @Builder
    public static RegisterRequest parseRegisterRequest(JSONObject json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json.toString(), BasicRegisterRequest.class);
        } catch (JsonProcessingException e) {
            System.out.println(e);
            return objectMapper.readValue(json.toString(), ProductRegisterRequest.class);
        }
    }
}
