package symphony.bm.cache.devices.entities.deviceproperty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("property-storage")
@Value
@AllArgsConstructor
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class DevicePropertyValueSnapshot {
    String CID;
    int index;
    String value;
    Date timestamp;
}
