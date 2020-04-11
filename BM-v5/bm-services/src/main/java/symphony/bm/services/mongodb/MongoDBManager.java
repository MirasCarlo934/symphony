package symphony.bm.services.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public class MongoDBManager {
    private Logger LOG;
    private MongoClient mongoClient;

    public MongoDBManager(@Value("${log.db}") String logDomain, @Value("${mongo.serverURI}") String uri) {
        LOG = LoggerFactory.getLogger(logDomain + ".mongo");
        LOG.info("Connecting to mongoDB...");
        mongoClient = new MongoClient(new MongoClientURI(uri));
        LOG.info("Connected to mongoDB!");
    }

    public MongoClient getClient() {
        return mongoClient;
    }
}
