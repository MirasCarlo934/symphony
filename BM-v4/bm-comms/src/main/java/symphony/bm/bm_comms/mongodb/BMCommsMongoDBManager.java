package symphony.bm.bm_comms.mongodb;

import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class BMCommsMongoDBManager {
    private Logger LOG;
    private MongoClient mongoClient;
    private DB db;

    public BMCommsMongoDBManager(String logDomain, String logName, String uri, String bmCommsDatabase) {
        LOG = LoggerFactory.getLogger(logDomain + "." + logName);
        try {
            LOG.info("Connecting to mongoDB...");
            mongoClient = new MongoClient(new MongoClientURI(uri));
            LOG.info("Using BM-comms database " + bmCommsDatabase);
            db = mongoClient.getDB(bmCommsDatabase);
            LOG.info("Connected to mongoDB...");
        } catch (UnknownHostException e) {
            LOG.error("Unable to connect to mongoDB!", e);
        }
    }

    public void insert(String collection, DBObject dbObject) {
        LOG.trace("Inserting DBObject " + dbObject.toString() + " to " + db.getName() + "." + collection + "...");
        DBCollection c = db.getCollection(collection);
        c.insert(dbObject);
        LOG.trace("DBObject " + dbObject.toString() + " inserted to " + db.getName() + "." + collection);
    }

    public DBCursor query(String collection, DBObject query) {
        LOG.trace("Querying " + query.toString() + " from " + db.getName() + "." + collection);
        DBCollection c = db.getCollection(collection);
        return c.find(query);
    }
}
