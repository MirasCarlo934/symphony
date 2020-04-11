package symphony.bm.bmlogicdevices.entities;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import symphony.bm.bmlogicdevices.adaptors.RegistryAdaptor;
import symphony.bm.bmlogicdevices.mongodb.MongoDBManager;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

@Repository
public class SymphonyRegistry {
    private Logger LOG;
    private HashMap<String, Room> rooms = new HashMap<>();
    private HashMap<String, Device> devices = new HashMap<>();
    private List<RegistryAdaptor> adaptors;
    private MongoDBManager mongo;

    private String devicesCollectionName;

    public SymphonyRegistry(@Value("${log.logic}") String logDomain,
                            @Value("${mongo.collection.devices}") String devicesCollectionName,
                            @Value("${mongo.collection.rooms}") String roomsCollectionName,
                            List<RegistryAdaptor> adaptors, MongoDBManager mongoDBManager) {
        LOG = LoggerFactory.getLogger(logDomain + ".registry");
        this.adaptors = adaptors;
        this.mongo = mongoDBManager;
        this.devicesCollectionName = devicesCollectionName;

        MongoCollection<Document> devicesCollection = mongo.getCollection(devicesCollectionName);
        MongoCollection<Document> roomsCollection = mongo.getCollection(roomsCollectionName);
        FindIterable<Document> roomsDocs = roomsCollection.find();
        FindIterable<Document> devicesDocs = devicesCollection.find();

        for (Document roomDoc : roomsDocs) {
            String rid = roomDoc.getString("RID");
            String name = roomDoc.getString("name");
            rooms.put(rid, new Room(rid, name, adaptors));
            LOG.debug("Room " + rid + " retrieved from DB");
        }

        for (Document devDoc : devicesDocs) {
            String cid = devDoc.getString("CID");
            String pid = devDoc.getString("PID");
            String name = devDoc.getString("name");
            Vector<DeviceProperty> properties = new Vector<>();
            Document roomDoc = (Document) devDoc.get("room");
            Document propertiesDoc = (Document) devDoc.get("properties");
            Room room = rooms.get(roomDoc.getString("RID"));
            Set<String> propIndices = propertiesDoc.keySet();
            for (String index : propIndices) { // get properties
                Document propDoc = (Document) propertiesDoc.get(index);
                DevicePropertyMode mode = DevicePropertyMode.valueOf(propDoc.getString("mode"));
                properties.add(new DeviceProperty(propDoc.getInteger("index"),
                        propDoc.getString("name"), propDoc.getString("type"), mode,
                        propDoc.getInteger("minValue"), propDoc.getInteger("maxValue")));
            }

            Device device = new Device(cid, pid, name, room, properties, adaptors);
            devices.put(cid, device);
            room.addDevice(device);
            LOG.debug("Device " + cid + " retrieved from DB");
        }
        LOG.info(devices.size() + " devices and " + rooms.size() + " rooms retrieved from DB");
    }

    public Device createDeviceObject(String cid, String pid, String name, Room room, List<DeviceProperty> properties) {
        LOG.debug("Creating new device " + cid + " in SymphonyEnvironment...");
        Device device = new Device(cid, pid, name, room, properties, adaptors);
        device.registerDevice();
        devices.put(cid, device);
        return device;
    }

    public Room createRoomObject(String rid, String name) {
        LOG.debug("Creating new room " + rid + " in SymphonyEnvironment...");
        Room room = new Room(rid, name, adaptors);
        room.createRoom();
        rooms.put(rid, room);
        return room;
    }

    public void reloadDevicePropertyFromDB(String cid, int prop_index) {
        Document devDoc = mongo.getCollection(devicesCollectionName).find(eq("CID", cid)).first();
        devices.get(cid).reloadDevicePropertyFromDB(prop_index, devDoc);
    }

    public boolean containsDeviceObject(String cid) {
        return devices.containsKey(cid);
    }

    public Device getDeviceObject(String cid) {
        return devices.get(cid);
    }
    
    public Collection<Device> getAllDeviceObjects() {
        return devices.values();
    }

    public Room getRoomObject(String rid) {
        return rooms.get(rid);
    }

    public void deleteDeviceObject(String cid) {
        Device device = devices.remove(cid);
        device.unregisterDevice();
    }
}
