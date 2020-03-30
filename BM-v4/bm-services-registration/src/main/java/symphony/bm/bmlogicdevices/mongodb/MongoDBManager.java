package symphony.bm.bmlogicdevices.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBManager {
    private Logger LOG;
    private MongoClient client;
    private MongoDatabase database;

    public MongoDBManager(String logDomain, String logName, String uri, String database) {
        LOG = LoggerFactory.getLogger(logDomain + "." + logName);
        LOG.info("Connecting to " + uri);
        client = new MongoClient(uri);
        this.database = client.getDatabase(database);
        LOG.info("Connected. Using database " + database);
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }
}
