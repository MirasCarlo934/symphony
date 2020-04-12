package symphony.bm.cache.devices.adaptors;

import symphony.bm.cache.devices.entities.Device;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;
import symphony.bm.cache.devices.entities.Room;

public interface Adaptor {
    void deviceCreated(Device device) throws Exception;
    void deviceDeleted(Device device) throws Exception;
    void deviceUpdatedDetails(Device device) throws Exception;
    void deviceTransferredRoom(Device device, Room from, Room to) throws Exception;
    
    void roomCreated(Room room) throws Exception;
    void roomDeleted(Room room) throws Exception;
    void roomUpdatedDetails(Room room) throws Exception;
    void roomTransferredRoom(Room room, Room from, Room to) throws Exception;
    
    void devicePropertyUpdated(DeviceProperty property);
}