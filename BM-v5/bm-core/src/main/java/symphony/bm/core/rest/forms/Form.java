package symphony.bm.core.rest.forms;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Iterator;
import java.util.Map;

public abstract class Form {
    public Map<String, Object> transformToMap() {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.convertValue(this, new TypeReference<Map<String, Object>>() {});
        map.entrySet().removeIf(entry -> entry.getValue() == null);
        return map;
    }
}
