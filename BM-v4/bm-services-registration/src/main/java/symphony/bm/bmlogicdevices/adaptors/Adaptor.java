package symphony.bm.bmlogicdevices.adaptors;

import symphony.bm.bmlogicdevices.entities.Device;
import symphony.bm.bmlogicdevices.entities.Room;

public interface Adaptor {
    void deviceRegistered(Device device);
    void deviceUnregistered(Device device);
    void roomCreated(Room room);
}
