package symphony.bm.cache.devices.adaptors;

import lombok.AllArgsConstructor;
import symphony.bm.cache.devices.entities.Device;
import symphony.bm.cache.devices.entities.Room;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;

import java.util.List;

@AllArgsConstructor
public class AdaptorManager implements Adaptor {
    private final List<Adaptor> adaptors;

    @Override
    public void deviceCreated(Device device) throws Exception {
        for(Adaptor adaptor: adaptors) {
            adaptor.deviceCreated(device);
        }
    }

    @Override
    public void deviceDeleted(Device device) throws Exception {
        for(Adaptor adaptor: adaptors) {
            adaptor.deviceDeleted(device);
        }
    }

    @Override
    public void deviceUpdatedDetails(Device device) throws Exception {
        for(Adaptor adaptor: adaptors) {
            adaptor.deviceUpdatedDetails(device);
        }
    }

    @Override
    public void deviceTransferredRoom(Device device, Room from, Room to) throws Exception {
        for(Adaptor adaptor: adaptors) {
            adaptor.deviceTransferredRoom(device, from, to);
        }
    }

    @Override
    public void roomCreated(Room room) throws Exception {
        for(Adaptor adaptor: adaptors) {
            adaptor.roomCreated(room);
        }
    }

    @Override
    public void roomDeleted(Room room) throws Exception {
        for(Adaptor adaptor: adaptors) {
            adaptor.roomDeleted(room);
        }
    }

    @Override
    public void roomUpdatedDetails(Room room) throws Exception {
        for(Adaptor adaptor: adaptors) {
            adaptor.roomUpdatedDetails(room);
        }
    }

    @Override
    public void roomTransferredRoom(Room room, Room from, Room to) throws Exception {
        for(Adaptor adaptor: adaptors) {
            adaptor.roomTransferredRoom(room, from, to);
        }
    }
    
    @Override
    public void devicePropertyUpdatedDetails(DeviceProperty property) {
        for(Adaptor adaptor: adaptors) {
            adaptor.devicePropertyUpdatedDetails(property);
        }
    }
    
    @Override
    public void devicePropertyUpdatedValue(DeviceProperty property) {
        for(Adaptor adaptor: adaptors) {
            adaptor.devicePropertyUpdatedValue(property);
        }
    }
}
