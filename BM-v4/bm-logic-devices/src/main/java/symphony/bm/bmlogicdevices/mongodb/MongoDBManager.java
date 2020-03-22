package symphony.bm.bmlogicdevices.mongodb;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class MongoDBManager {
    private Logger LOG;
    private MongoClient mongoClient;
    private MongoDatabase db;

    public MongoDBManager(String logDomain, String logName, String uri, String database) {
        LOG = LoggerFactory.getLogger(logDomain + "." + logName);
        LOG.info("Connecting to mongoDB...");
        mongoClient = new MongoClient(new MongoClientURI(uri));
        LOG.info("Using database " + database);
        db = mongoClient.getDatabase(database);
        LOG.info("Connected to mongoDB...");
    }

    public void insert(String collectionName, Document document) {
        LOG.trace("Inserting document " + document.toString() + " to " + db.getName() + "." + collectionName + "...");
        MongoCollection<Document> c = db.getCollection(collectionName);
        c.insertOne(document);
        LOG.trace("Document " + document.toString() + " inserted to " + db.getName() + "." + collectionName);
    }

    public FindIterable<Document> find(String collectionName, Bson filter) {
        LOG.trace("Finding " + filter.toString() + " from " + db.getName() + "." + collectionName + "...");
        MongoCollection<Document> collection = db.getCollection(collectionName);
        return collection.find(filter);
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        LOG.trace("Getting " + db + "." + collectionName);
        return db.getCollection(collectionName);
    }
}
