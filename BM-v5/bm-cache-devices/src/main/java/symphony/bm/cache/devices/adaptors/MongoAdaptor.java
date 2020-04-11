package symphony.bm.cache.devices.adaptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import symphony.bm.cache.devices.entities.Device;
import symphony.bm.cache.devices.entities.SuperRoom;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;
import symphony.bm.cache.devices.entities.Room;

import java.util.HashMap;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

public class MongoAdaptor implements Adaptor {
    private static final Logger LOG = LoggerFactory.getLogger(MongoAdaptor.class);
    private MongoOperations mongo;
    
    public MongoAdaptor(MongoOperations mongoOperations) {
        this.mongo = mongoOperations;
    }
    
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
    public void deviceUpdated(Device device) {
        LOG.info("Updating device " + device.getCID() + " in MongoDB...");
        updateRoom(device.getRoom());
        LOG.info("Device " + device.getCID() + " updated in MongoDB");
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
    public void propertyUpdated(DeviceProperty property) {
        LOG.info("Updating property " + property.getDevice().getCID() + "." + property.getIndex() + " in MongoDB...");
        mongo.updateFirst(query(where("properties")), update(String.valueOf(property.getIndex()), property),
                HashMap.class);
        LOG.info("Property " + property.getDevice().getCID() + "." + property.getIndex() + " updated in MongoDB");
    }
    
    private void updateRoom(Room room) {
        Room ancestor = room.getFirstAncestorRoom();
        if (ancestor == room) {
            mongo.save(room);
        } else {
            mongo.updateFirst(query(where("RID").is(ancestor.getRID())), new Update().set("rooms", ancestor.getRooms()),
                    Room.class);
        }
    }
    
//    private Room getFirstAncestorRoom(Device device) {
//        return getFirstAncestorRoom(device.getRoom());
//    }
//
//    private Room getFirstAncestorRoom(Room room) {
//        if (room.getParentRoom().getClass().equals(SuperRoom.class)) {
//            return room;
//        } else {
//            return getFirstAncestorRoom(room.getParentRoom());
//        }
//    }
}
