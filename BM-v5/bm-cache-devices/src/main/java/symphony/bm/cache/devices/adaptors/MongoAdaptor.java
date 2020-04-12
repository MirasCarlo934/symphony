package symphony.bm.cache.devices.adaptors;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import symphony.bm.cache.devices.entities.Device;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;
import symphony.bm.cache.devices.entities.Room;

import java.util.HashMap;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

@AllArgsConstructor
public class MongoAdaptor implements Adaptor {
    private static final Logger LOG = LoggerFactory.getLogger(MongoAdaptor.class);
    private final MongoOperations mongoOperations;
    
    @Override
    public void deviceCreated(Device device) throws Exception {
        LOG.info("Inserting device " + device.getCID() + " in MongoDB...");
        updateRoom(device.getRoom());
        LOG.info("Device " + device.getCID() + " inserted in MongoDB");
    }
    
    @Override
    public void deviceDeleted(Device device) throws Exception {
        LOG.info("Deleting device " + device.getCID() + " in MongoDB...");
        updateRoom(device.getRoom());
        LOG.info("Device " + device.getCID() + " deleted in MongoDB");
    }
    
    @Override
    public void deviceUpdatedDetails(Device device) {
        LOG.info("Updating device " + device.getCID() + " in MongoDB...");
        updateRoom(device.getRoom());
        LOG.info("Device " + device.getCID() + " updated in MongoDB");
    }

    @Override
    public void deviceTransferredRoom(Device device, Room from, Room to) throws Exception {
        deviceUpdatedDetails(device);
    }

    @Override
    public void roomCreated(Room room) throws Exception {
        LOG.info("Inserting room " + room.getRID() + " in MongoDB...");
        updateRoom(room);
        LOG.info("Room " + room.getRID() + " inserted in MongoDB");
    }
    
    @Override
    public void roomDeleted(Room room) throws Exception {
        LOG.info("Deleting room " + room.getRID() + " in MongoDB...");
        updateRoom(room);
        LOG.info("Room " + room.getRID() + " deleted in MongoDB");
    }

    @Override
    public void roomUpdatedDetails(Room room) throws Exception {
        LOG.info("Updating room " + room.getRID() + " in MongoDB...");
        updateRoom(room);
        LOG.info("Room " + room.getRID() + " updated in MongoDB");
    }

    @Override
    public void roomTransferredRoom(Room room, Room from, Room to) throws Exception {
        roomUpdatedDetails(room);
    }

    @Override
    public void devicePropertyUpdated(DeviceProperty property) {
        LOG.info("Updating property " + property.getDevice().getCID() + "." + property.getIndex() + " in MongoDB...");
        mongoOperations.updateFirst(query(where("properties")), update(String.valueOf(property.getIndex()), property),
                HashMap.class);
        LOG.info("Property " + property.getDevice().getCID() + "." + property.getIndex() + " updated in MongoDB");
    }
    
    private void updateRoom(Room room) {
        Room ancestor = room.getFirstAncestorRoom();
        mongoOperations.save(ancestor);
//        if (ancestor == room) {
//            LOG.error("Saving room...");
//            mongoOperations.save(room);
//        } else {
//            LOG.error("Updating room...");
//            mongoOperations.updateFirst(query(where("RID").is(ancestor.getRID())), new Update().set("rooms", ancestor.getRooms()),
//                    Room.class);
//        }
    }
}
