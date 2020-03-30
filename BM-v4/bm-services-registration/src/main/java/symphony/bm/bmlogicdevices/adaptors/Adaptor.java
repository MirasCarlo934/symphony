package symphony.bm.bmlogicdevices.adaptors;

import symphony.bm.bmlogicdevices.entities.Device;
import symphony.bm.bmlogicdevices.entities.DeviceProperty;
import symphony.bm.bmlogicdevices.entities.Room;

public interface Adaptor {
    void deviceRegistered(Device device);
    void deviceUnregistered(Device device);
    void deviceUpdated(Device device);
    void roomCreated(Room room);
    void propertyValueUpdated(Device device, DeviceProperty property);
}