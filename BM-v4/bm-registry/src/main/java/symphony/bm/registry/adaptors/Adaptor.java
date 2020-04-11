package symphony.bm.registry.adaptors;

import symphony.bm.registry.entities.Device;
import symphony.bm.registry.entities.DeviceProperty;
import symphony.bm.registry.entities.Room;

public interface Adaptor {
    void deviceCreated(Device device);
    void deviceDeleted(Device device);
    void deviceUpdated(Device device);
    
    void roomCreated(Room room);
    
    void propertyUpdated(DeviceProperty property);
}