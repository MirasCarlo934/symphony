package symphony.bm.bmservicespoop.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBManager {
    private Logger LOG;
    private MongoClient mongoClient;

    public MongoDBManager(String logDomain, String logName, String uri) {
        LOG = LoggerFactory.getLogger(logDomain + "." + logName);
        LOG.info("Connecting to mongoDB...");
        mongoClient = new MongoClient(new MongoClientURI(uri));
        LOG.info("Connected to mongoDB!");
    }

    public MongoClient getClient() {
        return mongoClient;
    }
}
