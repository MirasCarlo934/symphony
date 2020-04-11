package symphony.bm.cache.devices.entities.deviceproperty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import symphony.bm.cache.devices.adaptors.Adaptor;
import symphony.bm.cache.devices.entities.Device;
import symphony.bm.cache.devices.entities.Entity;

import java.util.List;

//@RequiredArgsConstructor
public class DeviceProperty extends Entity {
    @NonNull @Getter private int index;
    @NonNull @Getter private String name;
    @NonNull @Getter private DevicePropertyType type;
    @NonNull @Getter private DevicePropertyMode mode;
    @JsonIgnore @Getter private String value = null;
    
    @Transient @JsonIgnore @Setter(onMethod_ = {@JsonIgnore}) @Getter(onMethod_ = {@JsonIgnore}) private Device device;
    
    public DeviceProperty(@JsonProperty("index") int index, @JsonProperty("name") String name,
                          @JsonProperty("type") DevicePropertyType type,
                          @JsonProperty("mode") DevicePropertyMode mode) {
        this.index = index;
        this.name = name;
        this.mode = mode;
        this.type = type;
    }
    
    @Override
    protected void setSelfToChildren() {
    
    }
    
    
    @Override
    protected void setAdaptorsToChildren(List<Adaptor> adaptors) {
    
    }
}