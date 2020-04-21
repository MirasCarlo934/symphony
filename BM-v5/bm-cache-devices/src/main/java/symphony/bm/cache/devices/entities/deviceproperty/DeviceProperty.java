package symphony.bm.cache.devices.entities.deviceproperty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import symphony.bm.cache.devices.adaptors.AdaptorManager;
import symphony.bm.cache.devices.entities.Device;
import symphony.bm.cache.devices.entities.Entity;

//@RequiredArgsConstructor
public class DeviceProperty extends Entity {
    @Getter private int index;
    @JsonProperty("CID") @NonNull @Getter(onMethod_ = {@JsonProperty("CID")}) private final String CID;
    @NonNull @Getter private String name;
    @NonNull @Getter private DevicePropertyType type;
    @NonNull @Getter private DevicePropertyMode mode;
    @Getter private String value;
    
    @Transient @JsonIgnore @Setter(onMethod_ = {@JsonIgnore}) @Getter(onMethod_ = {@JsonIgnore}) private Device device;
    
    public DeviceProperty(@JsonProperty("index") int index, @JsonProperty("CID") String CID,
                          @JsonProperty("name") String name, @JsonProperty("type") DevicePropertyType type,
                          @JsonProperty("mode") DevicePropertyMode mode, @JsonProperty("value") String value) {
        this.index = index;
        this.CID = CID;
        this.name = name;
        this.mode = mode;
        this.type = type;
        
        if (value == null) {
            this.value = "";
        } else {
            this.value = value;
        }
    }
    
    @JsonIgnore
    public String getID() {
        return CID + "." + index;
    }
    
    public void setName(String name) throws Exception {
        this.name = name;
        adaptorManager.devicePropertyUpdatedDetails(this);
    }
    
    public void setValue(String value) throws Exception {
        this.value = value;
        adaptorManager.devicePropertyUpdatedValue(this);
    }
    
    @Override
    protected void setSelfToChildren() {
    
    }

    @Override
    protected void setAdaptorManagerToChildren(AdaptorManager adaptors) {

    }
}
