package symphony.bm.cache.devices.entities.deviceproperty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Calendar;
import java.util.Date;

@Document("property-storage")
@Value
@AllArgsConstructor(onConstructor_ = {@PersistenceConstructor})
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class DevicePropertyValueSnapshot {
    @JsonProperty("CID") String CID;
    int index;
    String value;
    Date timestamp;

    public DevicePropertyValueSnapshot(DeviceProperty property) {
        this.CID = property.getCID();
        this.index = property.getIndex();
        this.value = property.getValue();
        this.timestamp = Calendar.getInstance().getTime();
    }

    public ValueAndTimestamp getValueAndTimestampOnly() {
        return new ValueAndTimestamp(value, timestamp);
    }

    @Value
    @AllArgsConstructor
    public static class ValueAndTimestamp {
        String value;
        Date timestamp;
    }
}
