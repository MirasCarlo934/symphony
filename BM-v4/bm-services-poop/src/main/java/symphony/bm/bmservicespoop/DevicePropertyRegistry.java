package symphony.bm.bmservicespoop;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import symphony.bm.bmservicespoop.adaptors.POOPAdaptor;
import symphony.bm.bmservicespoop.entities.DeviceProperty;
import symphony.bm.bmservicespoop.mongodb.MongoDBManager;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Repository
public class DevicePropertyRegistry {
    private Logger LOG;
    private MongoDBManager mongo;
    private List<POOPAdaptor> adaptors;

    private String devicesDBname;
    private String devicesCollectionName;

    // key: cid-index
    private HashMap<String, DeviceProperty> deviceProperties = new HashMap<>();

    public DevicePropertyRegistry(@Value("${log.poop}") String logDomain,
                                  @Value("${mongo.database.devices}") String devicesDBname,
                                  @Value("${mongo.collection.devices}") String devicesCollectionName,
                                  MongoDBManager mongoDBManager, List<POOPAdaptor> adaptors) {
        LOG = LoggerFactory.getLogger(logDomain + "." + DevicePropertyRegistry.class.getSimpleName());
        this.mongo = mongoDBManager;
        this.devicesDBname = devicesDBname;
        this.devicesCollectionName = devicesCollectionName;
        this.adaptors = adaptors;

        updateRegistry();
    }

    public void updateRegistry() {
        LOG.info("Updating device property registry...");
        MongoCollection<Document> devicesCollection = mongo.getClient().getDatabase(devicesDBname).getCollection(devicesCollectionName);
        FindIterable<Document> devicesDoc = devicesCollection.find();
        deviceProperties.clear();
        int n = 0;

        for (Document deviceDoc : devicesDoc) {
            String cid = deviceDoc.getString("CID");
            Document propsDoc = deviceDoc.get("properties", Document.class);
            Set<String> indices = propsDoc.keySet();
            for (String index : indices) {
                DeviceProperty prop = new DeviceProperty(cid, propsDoc.get(index, Document.class), adaptors);
                deviceProperties.put(prop.getID(), prop);
                n++;
            }
        }

        LOG.info("Device property registry updated");
    }

    public HashMap<String, Integer> getAllDevicePropertyValues() {
        HashMap<String, Integer> values = new HashMap<>();
        for (DeviceProperty prop : deviceProperties.values()) {
            values.put(prop.getID(), prop.getValue());
        }
        return values;
    }

    public boolean containsDeviceProperty(String cid, int propIndex) {
        return deviceProperties.containsKey(cid + "-" + propIndex);
    }

    public DeviceProperty getDeviceProperty(String cid, int propIndex) {
        return deviceProperties.get(cid + "-" + propIndex);
    }
}
