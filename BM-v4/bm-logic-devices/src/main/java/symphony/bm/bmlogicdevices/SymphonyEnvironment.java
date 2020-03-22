package symphony.bm.bmlogicdevices;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import symphony.bm.bmlogicdevices.adaptors.Adaptor;
import symphony.bm.bmlogicdevices.entities.Device;
import symphony.bm.bmlogicdevices.entities.DeviceProperty;
import symphony.bm.bmlogicdevices.entities.Room;
import symphony.bm.bmlogicdevices.mongodb.MongoDBManager;

import java.util.HashMap;
import java.util.List;

public class SymphonyEnvironment {
    private HashMap<String, Room> rooms = new HashMap<>();
    private HashMap<String, Device> devices = new HashMap<>();
    private List<Adaptor> adaptors;
    private MongoDBManager mongo;
//    private String devicesCollectionName;
    private String roomsCollectionName;

    public SymphonyEnvironment(List<Adaptor> adaptors, MongoDBManager mongoDBManager, String roomsCollectionName) {
        this.adaptors = adaptors;
        this.mongo = mongoDBManager;
        this.roomsCollectionName = roomsCollectionName;

        MongoCollection<Document> roomsCollection = mongo.getCollection(roomsCollectionName);
    }

    public Device createDeviceObject(String cid, String pid, String name, Room room, DeviceProperty[] properties) {
        Device device = new Device(cid, pid, name, room, properties, adaptors);
        device.registerDevice();
        devices.put(cid, device);
        return device;
    }

    public Room createRoomObject(String rid, String name) {
        Room room = new Room(rid, name, adaptors);
        room.createRoom();
        rooms.put(rid, room);
        return room;
    }

    public Room getRoomObject(String rid) {
        return rooms.get(rid);
    }

    public Device getDeviceObject(String cid) {
        return devices.get(cid);
    }
}
