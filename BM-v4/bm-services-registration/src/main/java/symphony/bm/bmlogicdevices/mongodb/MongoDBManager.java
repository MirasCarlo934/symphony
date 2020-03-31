package symphony.bm.bmlogicdevices.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class MongoDBManager {
    private Logger LOG;
    private MongoClient client;
    private MongoDatabase database;

    public MongoDBManager(@Value("${log.db}") String logDomain, @Value("${mongo.serverURI}") String uri,
                          @Value("${mongo.database.devices}") String database) {
        LOG = LoggerFactory.getLogger(logDomain + ".mongo");
        LOG.info("Connecting to " + uri);
        client = new MongoClient(new MongoClientURI(uri));
        this.database = client.getDatabase(database);
        LOG.info("Connected. Using database " + database);
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }
}
