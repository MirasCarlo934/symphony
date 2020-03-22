package symphony.bm.bmlogicdevices.mongodb;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symphony.bm.bmlogicdevices.adaptors.Adaptor;
import symphony.bm.bmlogicdevices.entities.Device;
import symphony.bm.bmlogicdevices.entities.Room;

import java.util.List;
import java.util.Vector;

import static com.mongodb.client.model.Filters.eq;

public class MongoDBAdaptor implements Adaptor {
    private Logger LOG;
    private MongoDBManager mongo;
    private String devicesCollection;
    private String roomsCollection;
    private String productsCollection;

    public MongoDBAdaptor(String logDomain, String adaptorName, MongoDBManager mongoDBManager,
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
        MongoCollection<Document> devices = mongo.getCollection(devicesCollection);
        MongoCollection<Document> rooms = mongo.getCollection(roomsCollection);
        MongoCollection<Document> products = mongo.getCollection(productsCollection);
        Document room = rooms.find(eq("RID", device.getRoom().getRID())).first();
        Document product = products.find(eq("PID", device.getPID())).first();

        Document roomDoc = new Document()
                .append("RID", room.getString("RID"))
                .append("name", room.getString("name"));
        Document propDoc = new Document();
        List<Document> productProps = product.getList("properties", Document.class);
        for (Document prop : productProps) {
            int index = prop.getInteger("index");
            propDoc.append(String.valueOf(index), new Document()
                    .append("index", index)
                    .append("name", prop.getString("name"))
                    .append("type", prop.get("type"))
                    .append("value", 0));
        }
        Document deviceDoc = new Document("CID", device.getCID())
                .append("PID", device.getPID())
                .append("name", device.getName())
                .append("room", roomDoc)
                .append("properties", propDoc);
        devices.insertOne(deviceDoc);
        LOG.info("Device " + device.getCID() + " registered to MongoDB");
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
}
