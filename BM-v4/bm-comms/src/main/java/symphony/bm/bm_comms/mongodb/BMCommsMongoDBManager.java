package symphony.bm.bm_comms.mongodb;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class BMCommsMongoDBManager {
    private Logger LOG;
    private MongoClient mongoClient;
    private MongoDatabase db;
    private MongoCollection<Document> devicesCollection;

    public BMCommsMongoDBManager(String logDomain, String logName, String uri, String bmCommsDatabase,
                                 String devicesCollectionName) {
        LOG = LoggerFactory.getLogger(logDomain + "." + logName);
        LOG.info("Connecting to mongoDB...");
        mongoClient = new MongoClient(new MongoClientURI(uri));
        LOG.info("Using BM-comms database " + bmCommsDatabase);
        db = mongoClient.getDatabase(bmCommsDatabase);
        devicesCollection = db.getCollection(devicesCollectionName);
        LOG.info("Connected to mongoDB...");
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        LOG.trace("Getting " + db + "." + collectionName);
        return db.getCollection(collectionName);
    }

    public MongoCollection<Document> getDevicesCollection() {
        return devicesCollection;
    }

//    public void insert(String collection, DBObject dbObject) {
//        LOG.trace("Inserting DBObject " + dbObject.toString() + " to " + db.getName() + "." + collection + "...");
//        DBCollection c = db.getCollection(collection);
//        c.insert(dbObject);
//        LOG.trace("DBObject " + dbObject.toString() + " inserted to " + db.getName() + "." + collection);
//    }
//
//    public DBCursor query(String collection, DBObject query) {
//        LOG.trace("Querying " + query.toString() + " from " + db.getName() + "." + collection);
//        DBCollection c = db.getCollection(collection);
//        return c.find(query);
//    }
}
