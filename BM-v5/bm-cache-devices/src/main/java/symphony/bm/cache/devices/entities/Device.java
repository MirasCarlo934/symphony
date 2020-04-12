package symphony.bm.cache.devices.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import symphony.bm.cache.devices.adaptors.Adaptor;
import symphony.bm.cache.devices.adaptors.AdaptorManager;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;

import java.util.List;

public class Device extends Entity {
    @JsonProperty("CID") @NonNull @Getter(onMethod_ = {@JsonProperty("CID")}) private String CID;
    @JsonProperty("RID") @NonNull @Setter(AccessLevel.PACKAGE) @Getter(onMethod_ = {@JsonProperty("RID")}) private String RID;
    @NonNull @Getter private String name;
    @NonNull @Getter private List<DeviceProperty> properties;
    
    @Transient @JsonIgnore @Getter(onMethod_ = {@JsonIgnore}) private Room room = null;

    public Device(@JsonProperty("CID") String CID, @JsonProperty("RID") String RID, @JsonProperty("name") String name,
                  @JsonProperty("properties") List<DeviceProperty> properties) {
        this.CID = CID;
        this.name = name;
        this.RID = RID;
        this.properties = properties;
    }

//    public void registerDeviceInAdaptors() throws Exception {
//        if (!registered) {
//            registered = true;
//            for (Adaptor adaptor : adaptorManager) {
//                adaptor.deviceCreated(this);
//            }
//        }
//    }
//
//    public void unregisterDeviceInAdaptors() throws Exception {
//        for (Adaptor adaptor : adaptorManager) {
//            adaptor.deviceDeleted(this);
//        }
//    }

    public boolean hasPropertyIndex(int index) {
        try {
            properties.get(index);
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }
    
    public void putProperty(DeviceProperty property) {
        properties.add(property);
    }
    
    public DeviceProperty getProperty(int index) {
        return properties.get(index);
    }
    
    @JsonIgnore
    public Room getFirstAncestorRoom() {
        return room.getFirstAncestorRoom();
    }
    
    @JsonIgnore
    public void setName(String name) throws Exception {
        this.name = name;
        adaptorManager.deviceUpdatedDetails(this);
    }
    
    @JsonIgnore
    void setRoom(Room room) throws Exception {
        this.room = room;
        this.RID = room.getRID();
    }
    
    @Override
    protected void setAdaptorManagerToChildren(AdaptorManager adaptors) {
        for (DeviceProperty p : properties) {
            p.setAdaptorManager(adaptors);
        }
    }
    
    @Override
    protected void setSelfToChildren() {
        for (DeviceProperty p : properties) {
            p.setDevice(this);
        }
    }
}
