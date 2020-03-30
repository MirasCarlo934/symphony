package symphony.bm.bmservicespoop.mongodb;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import symphony.bm.bmservicespoop.adaptors.POOPAdaptor;
import symphony.bm.bmservicespoop.entities.DeviceProperty;

import static com.mongodb.client.model.Filters.eq;

@Component
public class MongoDB_POOPAdaptor implements POOPAdaptor {
    private Logger LOG;
    private MongoDBManager mongo;

    private String devicesCollectionName;
    private String devicesDBname;

    public MongoDB_POOPAdaptor(@Value("${log.adaptor}") String logDomain,
                               @Value("${mongo.collection.devices}") String devicesCollectionName,
                               @Value("${mongo.database.devices}") String devicesDBname,
                               MongoDBManager mongo) {
        LOG = LoggerFactory.getLogger(logDomain + ".mongoDB");
        this.mongo = mongo;
        this.devicesCollectionName = devicesCollectionName;
        this.devicesDBname = devicesDBname;
    }

    @Override
    public void updatePropertyValue(DeviceProperty property) {
        LOG.debug("Updating " + property.getID() + " in mongoDB...");
        MongoCollection<Document> devicesCollection = mongo.getClient().getDatabase(devicesDBname).getCollection(devicesCollectionName);
        Document update = new Document("$set", new Document("properties." + property.getIndex() + ".value",
                property.getValue()));
        devicesCollection.findOneAndUpdate(eq("CID", property.getDeviceCID()), update);
        LOG.debug(property.getID() + " updated in mongoDB...");
    }
}
