package symphony.bm.services.jeep.request.register;

import lombok.NonNull;
import lombok.Value;
import symphony.bm.services.entities.DevicePropertyMode;

import java.util.Map;

@Value
public class RegisterRequestPropertyDefinition {
    private static final String[] params = {"name", "index", "type", "mode", "minValue", "maxValue"};
    
    @NonNull String name;
    @NonNull int index;
    @NonNull String type;
    @NonNull DevicePropertyMode mode;
    @NonNull double minValue;
    @NonNull double maxValue;
    
    RegisterRequestPropertyDefinition(Map<String, Object> map) {
        for (String param : params) {
            if (!map.containsKey(param)) throw new NullPointerException(param + " does not exist in a property definition");
        }
        this.name = (String) map.get("name");
        this.index = (Integer) map.get("index");
        this.type = (String) map.get("type");
        this.mode = DevicePropertyMode.valueOf((String) map.get("mode"));
        if (map.get("minValue").getClass().equals(Integer.class))
            this.minValue = (Integer) map.get("minValue");
        else
            this.minValue = (Double) map.get("minValue");
        if (map.get("maxValue").getClass().equals(Integer.class))
            this.maxValue = (Integer) map.get("maxValue");
        else
            this.maxValue = (Double) map.get("maxValue");
    }
}
