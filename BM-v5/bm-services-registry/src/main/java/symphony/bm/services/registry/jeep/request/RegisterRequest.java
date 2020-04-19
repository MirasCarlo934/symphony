package symphony.bm.services.registry.jeep.request;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symphony.bm.cache.devices.entities.deviceproperty.DevicePropertyType;
import symphony.bm.generics.jeep.request.JeepRequest;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Value
public class RegisterRequest extends JeepRequest {
    static String[] propertyFields = {"name", "mode", "type"};
    static String[] roomFields = {"name", "RID"};
    static Logger LOG = LoggerFactory.getLogger(RegisterRequest.class);
    
    Object product;
    Object room;
    String name;
    
    public RegisterRequest(@NonNull String MRN, @NonNull String MSN, @NonNull String CID, @NonNull String name,
                           @NonNull Object product, Object room) throws Exception {
        super(MRN, MSN, CID);
        this.name = name;
        this.product = product;
        this.room = room;
        if (!product.getClass().equals(String.class)) {
            checkProduct(product);
        }
        if (!room.getClass().equals(String.class)) {
            checkRoom(room);
        }
    }
    
    private void checkProduct(Object product) throws Exception {
        List<Map<String, Object>> props;
        try {
            props = (List<Map<String, Object>>) product;
        } catch (ClassCastException e) {
            throw new ClassCastException("'product' parameter not properly constructed");
        }
        for (Map<String, Object> prop : props) {
            for (String field : propertyFields) {
                if (!prop.containsKey(field)) {
                    throw new Exception("a '" + field + "' field does not exist in the properties array");
                }
            }
            if (!Map.class.isAssignableFrom(prop.get("type").getClass())) {
                throw new Exception("a 'type' field in the properties array is not properly constructed");
            }

            Map<String, Object> type;
            try {
                type = (Map<String, Object>) prop.get("type");
            } catch (ClassCastException e) {
                throw new ClassCastException("a 'type' field in the properties array is not properly constructed");
            }
            try {
                DevicePropertyType.builder().map(type).build();
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new Exception("a 'type' field in the properties array does not have appropriate parameters", e);
            }
        }
    }
    
    private void checkRoom(Object room) throws Exception {
        Map<String, Object> map;
        try {
            map = (Map<String, Object>) room;
        } catch (ClassCastException e) {
            throw new Exception("'room' parameter not properly constructed", e);
        }
        for (String field : roomFields) {
            if (!map.containsKey(field)) {
                throw new Exception("room." + field + " does not exist");
            }
        }
    }
}
