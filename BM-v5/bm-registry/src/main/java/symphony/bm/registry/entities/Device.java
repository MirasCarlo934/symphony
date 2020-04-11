package symphony.bm.registry.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import symphony.bm.registry.adaptors.Adaptor;

import java.util.HashMap;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

@Document(collection = "registry")
public class Device extends Entity {
    @Getter private String CID;
    @Getter private String RID;
    @Getter private String name;
    @Getter private HashMap<Integer, DeviceProperty> properties = new HashMap<>();
    
    @Transient @Setter @Getter(onMethod_ = {@JsonIgnore}) private Room room;

    public Device(String CID, String RID, String name, HashMap<Integer, DeviceProperty> properties) {
        this.CID = CID;
        this.name = name;
        this.RID = RID;
        this.properties = properties;
    }

//    void reloadDevicePropertyFromDB(int prop_index, Document deviceDoc) {
//        properties.get(prop_index).reloadFromDB(deviceDoc);
//    }

    public void registerDevice() {
        for (Adaptor adaptor : adaptors) {
            adaptor.deviceCreated(this);
        }
    }

    public void unregisterDevice() {
        for (Adaptor adaptor : adaptors) {
            adaptor.deviceDeleted(this);
        }
    }

    public void updateDetails(String name, String rid) {
        if (name != null)
            this.name = name;
        if (rid != null)
            this.RID = rid;
        for (Adaptor adaptor : adaptors) {
            adaptor.deviceUpdated(this);
        }
    }

    public boolean hasPropertyIndex(int index) {
        return properties.containsKey(index);
    }
    
    public void putProperty(DeviceProperty property) {
        properties.put(property.getIndex(), property);
    }
    
    public DeviceProperty getProperty(int index) {
        return properties.get(index);
    }
    
    @Override
    void setAdaptorsToChildren(List<Adaptor> adaptors) {
        for (DeviceProperty p : properties.values()) {
            p.setAdaptors(adaptors);
        }
    }
    
    @Override
    void setSelfToChildren() {
        for (DeviceProperty p : properties.values()) {
            p.setDevice(this);
        }
    }
}
