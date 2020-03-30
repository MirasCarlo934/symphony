package symphony.bm.bmservicespoop.cir;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symphony.bm.bmservicespoop.mongodb.MongoDBManager;

import java.util.Vector;

public class CIRManager {
    private Logger LOG;
    private MongoDatabase cirDB;
    private String rulesCollectionName;
    private Vector<Rule> rules = new Vector<>();

    public CIRManager(String logDomain, String logName, MongoDBManager mongoDBManager, String databaseName,
                      String rulesCollectionName) {
        LOG = LoggerFactory.getLogger(logDomain + "." + logName);
        cirDB = mongoDBManager.getClient().getDatabase(databaseName);
        this.rulesCollectionName = rulesCollectionName;

        getRulesFromDB();
    }

    public void getRulesFromDB() {
        LOG.info("Getting rules from DB...");
        MongoCollection<Document> rulesCollection = cirDB.getCollection(rulesCollectionName);
        FindIterable<Document> ruleDocs = rulesCollection.find();
        int n = 0;
        while (ruleDocs.cursor().hasNext()) {
            Rule rule = new Rule(ruleDocs.cursor().next());
            LOG.info("Rule " + rule.getRuleID() + "(" + rule.getRuleName() + ") found.");
            if (!containsRule(rule.getRuleID())) {
                rules.add(rule);
                n++;
            } else
                LOG.info("Rule " + rule.getRuleID() + "(" + rule.getRuleName() + ") already exists in rules list.");
        }
        LOG.info(n + " new rules retrieved from DB. Total rules: " + rules.size());
    }

    private boolean containsRule(String ruleID) {
        for (Rule rule : rules) {
            if (rule.getRuleID().equals(ruleID)) return true;
        }
        return false;
    }
}
