package symphony.bm.services.jeep.request.register;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.Value;
import symphony.bm.services.entities.DevicePropertyMode;
import symphony.bm.services.jeep.JeepMessage;

import java.util.*;

@Value
public class RegisterRequest extends JeepMessage {
    Object product;
    Object room;
    String name;
    
    public RegisterRequest(@NonNull String MRN, @NonNull String MSN, @NonNull String CID, @NonNull String name,
                           @NonNull Object product, Object room) throws ClassCastException, NullPointerException {
        super(MRN, MSN, CID);
        this.name = name;
        
        if (!product.getClass().equals(String.class)) {
            this.product = new Vector<RegisterRequestPropertyDefinition>();
            for (Map<String, Object> propDef : (List<Map<String, Object>>) product) {
                ((Vector<RegisterRequestPropertyDefinition>) this.product).add(new RegisterRequestPropertyDefinition(propDef));
            }
        } else {
            this.product = product;
        }
        if (!room.getClass().equals(String.class)) {
            this.room = new RoomDefinition((Map<String, Object>) room);
        } else {
            this.room = room;
        }
    }
    
    @Value
    public class RoomDefinition {
        @NonNull String RID;
        @NonNull String name;
        
        RoomDefinition(Map<String, Object> map) throws ClassCastException, NullPointerException {
            if (!map.containsKey("RID")) throw new NullPointerException("'room.RID' does not exist!");
            if (!map.containsKey("name")) throw new NullPointerException("'room.name' does not exist!");
            this.RID = (String) map.get("RID");
            this.name = (String) map.get("name");
        }
    }
}
