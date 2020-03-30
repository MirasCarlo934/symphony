package symphony.bm.bmservicespoop.cir;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import symphony.bm.bmservicespoop.cir.rule.Rule;
import symphony.bm.bmservicespoop.mongodb.MongoDBManager;

import java.util.List;
import java.util.Vector;

@Repository
public class CIRManager {
    private Logger LOG;
    private MongoDatabase cirDB;
    private Vector<Rule> rules = new Vector<>();

    private String rulesCollectionName;

    public CIRManager(@Value("${log.poop}") String logDomain, @Value("${mongo.database.cir}") String cirDBname,
                      @Value("${mongo.collection.rules}") String rulesCollectionName,
                      MongoDBManager mongoDBManager) {
        LOG = LoggerFactory.getLogger(logDomain + "." + CIRManager.class.getSimpleName());
        cirDB = mongoDBManager.getClient().getDatabase(cirDBname);
        this.rulesCollectionName = rulesCollectionName;

        getRulesFromDB();
    }

    public void getRulesFromDB() {
        LOG.info("Getting rules from DB...");
        MongoCollection<Document> rulesCollection = cirDB.getCollection(rulesCollectionName);
        FindIterable<Document> ruleDocs = rulesCollection.find();
        int n = 0;
        for (Document ruleDoc : ruleDocs) {
            Rule rule = new Rule(ruleDoc);
            LOG.info("Rule " + rule.getID() + "(" + rule.getName() + ") found.");
            if (!containsRule(rule.getID())) {
                rules.add(rule);
                n++;
            } else {
                LOG.info("Rule " + rule.getID() + "(" + rule.getName() + ") already exists in rules list.");
            }
        }
        LOG.info(n + " new rules retrieved from DB. Total rules: " + rules.size());
    }

    public List<Rule> getRulesTriggered(String cid, int propIndex) {
        Vector<Rule> rulesTriggered = new Vector<>();
        for (Rule rule : rules) {
            if (rule.hasTriggerDeviceProperty(cid, propIndex)) {
                rulesTriggered.add(rule);
            }
        }
        return rulesTriggered;
    }

    private boolean containsRule(String ruleID) {
        for (Rule rule : rules) {
            if (rule.getID().equals(ruleID)) return true;
        }
        return false;
    }
}
