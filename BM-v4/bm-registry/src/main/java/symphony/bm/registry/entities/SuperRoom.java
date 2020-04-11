package symphony.bm.registry.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import symphony.bm.registry.adaptors.Adaptor;

import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
public class SuperRoom extends Room {
    private static final Logger LOG = LoggerFactory.getLogger(SuperRoom.class);
    private MongoOperations mongo;

    public SuperRoom(List<Adaptor> adaptors, MongoTemplate mongoTemplate) {
        super("_super", "Super Room");
        this.mongo = mongoTemplate;

        reloadAllEntities();
        setAdaptors(adaptors);
        setSelfToChildren();
//        MongoCollection<Document> devicesCollection = mongo.getCollection(devicesCollectionName);
//        MongoCollection<Document> roomsCollection = mongo.getCollection(roomsCollectionName);
//        FindIterable<Document> roomsDocs = roomsCollection.find();
//        FindIterable<Document> devicesDocs = devicesCollection.find();
//
//        for (Document roomDoc : roomsDocs) {
//            String rid = roomDoc.getString("RID");
//            String name = roomDoc.getString("name");
//            rooms.put(rid, new Room(rid, name, adaptors));
//            LOG.debug("Room " + rid + " retrieved from DB");
//        }
//
//        for (Document devDoc : devicesDocs) {
//            String cid = devDoc.getString("CID");
//            String pid = devDoc.getString("PID");
//            String name = devDoc.getString("name");
//            Vector<DeviceProperty> properties = new Vector<>();
//            Document roomDoc = (Document) devDoc.get("room");
//            Document propertiesDoc = (Document) devDoc.get("properties");
//            Room room = rooms.get(roomDoc.getString("RID"));
//            Set<String> propIndices = propertiesDoc.keySet();
//            for (String index : propIndices) { // get properties
//                Document propDoc = (Document) propertiesDoc.get(index);
//                DevicePropertyMode mode = DevicePropertyMode.valueOf(propDoc.getString("mode"));
//                properties.add(new DeviceProperty(propDoc.getInteger("index"),
//                        propDoc.getString("name"), propDoc.getString("type"), mode,
//                        propDoc.getInteger("minValue"), propDoc.getInteger("maxValue")));
//            }
//
//            Device device = new Device(cid, pid, name, room, properties, adaptors);
//            devices.put(cid, device);
//            room.addDevice(device);
//            LOG.debug("Device " + cid + " retrieved from DB");
//        }
    }
    
    public void reloadAllEntities() {
        LOG.info("Reloading all entities in super room from DB...");
        List<Room> roomsList = mongo.find(query(where("_class").is(Room.class.getName())), Room.class);
        List<Device> devicesList = mongo.find(query(where("_class").is(Device.class.getName())), Device.class);
    
        for (Room room : roomsList) {
            rooms.put(room.getRid(), room);
        }
        for (Device device : devicesList) {
            devices.put(device.getCID(), device);
        }

        LOG.info(countAllDevices() + " devices and " + countAllRooms() + " rooms reloaded from DB");
    }

//    public Device createDeviceObject(String cid, String pid, String name, Room room, List<DeviceProperty> properties) {
//        LOG.debug("Creating new device " + cid + " in SymphonyEnvironment...");
//        Device device = new Device(cid, pid, name, room, properties, adaptors);
//        device.registerDevice();
//        devices.put(cid, device);
//        return device;
//    }
//
//    public Room createRoomObject(String rid, String name) {
//        LOG.debug("Creating new room " + rid + " in SymphonyEnvironment...");
//        Room room = new Room(rid, name, adaptors);
//        room.createRoom();
//        rooms.put(rid, room);
//        return room;
//    }

//    public void reloadDevicePropertyFromDB(String cid, int prop_index) {
//        Document devDoc = mongo.getCollection(devicesCollectionName).find(eq("CID", cid)).first();
//        devices.get(cid).reloadDevicePropertyFromDB(prop_index, devDoc);
//    }
//
//    public boolean containsDeviceObject(String cid) {
//        return devices.containsKey(cid);
//    }
//
//    public Device getDeviceObject(String cid) {
//        return devices.get(cid);
//    }
//
//    public Collection<Device> getAllDeviceObjects() {
//        return devices.values();
//    }
//
//    public Room getRoomObject(String rid) {
//        return rooms.get(rid);
//    }
//
//    public void deleteDeviceObject(String cid) {
//        Device device = devices.remove(cid);
//        device.unregisterDevice();
//    }
}
