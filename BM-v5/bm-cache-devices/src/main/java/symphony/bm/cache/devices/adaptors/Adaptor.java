package symphony.bm.cache.devices.adaptors;

import symphony.bm.cache.devices.entities.Device;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;
import symphony.bm.cache.devices.entities.Room;

public interface Adaptor {
    void deviceCreated(Device device) throws Exception;
    void deviceDeleted(Device device) throws Exception;
    void deviceUpdated(Device device) throws Exception;
    
    void roomCreated(Room room) throws Exception;
    void roomDeleted(Room room) throws Exception;
    
    void propertyUpdated(DeviceProperty property);
}