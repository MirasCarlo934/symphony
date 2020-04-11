//package symphony.bm.services.mongodb;
//
//import com.mongodb.client.MongoCollection;
//import org.bson.Document;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
////import symphony.bm.services.adaptors.Adaptor;
//import symphony.bm.services.cir.rule.Rule;
////import symphony.bm.services.entities.DeviceProperty;
//
//import static com.mongodb.client.model.Filters.eq;
//
//public class MongoDB_Adaptor implements Adaptor {
//    private Logger LOG;
//    private MongoDBManager mongo;
//
//    private String devicesCollectionName;
//    private String devicesDBname;
//    private String rulesCollectionName;
//    private String cirDBname;
//
//    public MongoDB_Adaptor(String logDomain, String devicesCollectionName, String devicesDBname,
//                           String rulesCollectionName, String cirDBname,
//                           MongoDBManager mongoDBmanager) {
//        LOG = LoggerFactory.getLogger(logDomain + ".mongoDB");
//        this.mongo = mongoDBmanager;
//        this.devicesCollectionName = devicesCollectionName;
//        this.devicesDBname = devicesDBname;
//        this.rulesCollectionName = rulesCollectionName;
//        this.cirDBname = cirDBname;
//    }
//
//    @Override
//    public void propertyValueUpdated(DeviceProperty property) {
//        LOG.debug("Updating " + property.getID() + " in mongoDB...");
//        MongoCollection<Document> devicesCollection = mongo.getClient().getDatabase(devicesDBname).getCollection(devicesCollectionName);
//        Document update = new Document("$set", new Document("properties." + property.getIndex() + ".value",
//                property.getValue()));
//        devicesCollection.findOneAndUpdate(eq("CID", property.getDeviceCID()), update);
//        LOG.debug(property.getID() + " updated in mongoDB...");
//    }
//
//    @Override
//    public void ruleCreated(Rule rule) {
//        LOG.debug("Persisting " + rule.getID() + "(" + rule.getName() + ") in mongoDB...");
//        MongoCollection<Document> rulesCollection = mongo.getClient().getDatabase(cirDBname).getCollection(rulesCollectionName);
//        rulesCollection.insertOne(rule.getDocument());
//        LOG.debug(rule.getID() + "(" + rule.getName() + ") persisted in mongoDB...");
//    }
//}
