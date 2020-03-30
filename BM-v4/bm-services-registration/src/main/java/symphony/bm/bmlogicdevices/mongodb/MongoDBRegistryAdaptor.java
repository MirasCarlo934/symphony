package symphony.bm.bmlogicdevices.mongodb;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symphony.bm.bmlogicdevices.adaptors.RegistryAdaptor;
import symphony.bm.bmlogicdevices.entities.Device;
import symphony.bm.bmlogicdevices.entities.DeviceProperty;
import symphony.bm.bmlogicdevices.entities.Room;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class MongoDBRegistryAdaptor implements RegistryAdaptor {
    private Logger LOG;
    private MongoDBManager mongo;
    private String devicesCollection;
    private String roomsCollection;
    private String productsCollection;

    public MongoDBRegistryAdaptor(String logDomain, String adaptorName, MongoDBManager mongoDBManager,
                                  String devicesCollectionName, String roomsCollectionName, String productsCollectionName) {
        LOG = LoggerFactory.getLogger(logDomain + "." + adaptorName);
        this.mongo = mongoDBManager;
        this.devicesCollection = devicesCollectionName;
        this.roomsCollection = roomsCollectionName;
        this.productsCollection = productsCollectionName;
    }

    @Override
    public void deviceRegistered(Device device) {
        LOG.info("Registering device " + device.getCID() + " to MongoDB...");
        try {
            MongoCollection<Document> devices = mongo.getCollection(devicesCollection);
            Room room = device.getRoom();
            List<DeviceProperty> properties = device.getPropertiesList();

            Document roomDoc = new Document()
                    .append("RID", room.getRID())
                    .append("name", room.getName());
            Document propDoc = new Document();
            for (DeviceProperty prop : properties) {
                int index = prop.getIndex();
                propDoc.append(String.valueOf(index), new Document()
                        .append("index", index)
                        .append("name", prop.getName())
                        .append("type", prop.getType())
                        .append("mode", prop.getMode().toString())
                        .append("minValue", prop.getMinValue())
                        .append("maxValue", prop.getMaxValue())
                        .append("value", 0));
            }
            Document deviceDoc = new Document("CID", device.getCID())
                    .append("PID", device.getPID())
                    .append("name", device.getName())
                    .append("room", roomDoc)
                    .append("properties", propDoc);
            devices.insertOne(deviceDoc);
            LOG.info("Device " + device.getCID() + " registered to MongoDB");
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    @Override
    public void roomCreated(Room room) {
        LOG.info("Adding room " + room.getRID() + " to MongoDB...");
        MongoCollection<Document> rooms = mongo.getCollection(roomsCollection);

        Document roomDoc = new Document()
                .append("RID", room.getRID())
                .append("name", room.getName());
        rooms.insertOne(roomDoc);
        LOG.info("Room " + room.getRID() + " added to MongoDB");
    }

    @Override
    public void deviceUnregistered(Device device) {
        LOG.info("Deleting device " + device.getCID() + " from MongoDB...");
        MongoCollection<Document> devices = mongo.getCollection(devicesCollection);
        devices.deleteOne(eq("CID", device.getCID()));
        LOG.info("Device " + device.getCID() + " deleted from MongoDB");
    }

    @Override
    public void propertyValueUpdated(Device device, DeviceProperty property) {

    }

    @Override
    public void deviceUpdated(Device device) {
        LOG.info("Updating device " + device.getCID() + " in MongoDB...");
        MongoCollection<Document> devices = mongo.getCollection(devicesCollection);
        Document room = device.getRoom().convertToDocument();
        Bson update = combine(set("name", device.getName()), set("room", room));
        devices.findOneAndUpdate(eq("CID", device.getCID()), update);
        LOG.info("Device " + device.getCID() + " updated in MongoDB");
    }
}
