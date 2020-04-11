package symphony.bm.services.jeep.request.register;

import lombok.NonNull;
import lombok.Value;

import java.util.Map;

@Value
public class RegisterRequestRoomDefinition {
    private static final String fieldName = "room";
    private static final String[] params = {"RID", "name"};
    
    @NonNull String RID;
    @NonNull String name;
    
    RegisterRequestRoomDefinition(Map<String, Object> map) throws ClassCastException, NullPointerException {
        for (String param : params) {
            if (!map.containsKey(param)) throw new NullPointerException(param + " does not exist");
        }
        this.RID = (String) map.get("RID");
        this.name = (String) map.get("name");
    }
}
