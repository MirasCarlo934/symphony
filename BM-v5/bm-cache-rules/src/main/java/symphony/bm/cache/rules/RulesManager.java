package symphony.bm.cache.rules;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import symphony.bm.cache.rules.vo.Rule;

import java.util.List;
import java.util.Vector;

import static org.springframework.data.mongodb.core.query.Query.query;

@Component
public class RulesManager {
    private static final Logger LOG = LoggerFactory.getLogger(RulesManager.class);
    private MongoOperations mongo;
    
    @Getter private List<Rule> rulesList = new Vector<>();
    
    public RulesManager(MongoOperations mongo) {
        this.mongo = mongo;
        
        loadRulesFromDB();
    }
    
//    public void upsertRule(Rule rule) {
//        for (Rule r : rulesList) {
//            if (r.getRuleID().equals(rule.getRuleID())) {
//                rulesList.remove(r);
//                break;
//            }
//        }
//        rulesList.add(rule);
//        saveRuleInDB(rule);
//    }
    
    public void addRule(Rule rule) {
        rulesList.add(rule);
        saveRuleInDB(rule);
    }
    
    public Rule getRule(String rule_id) {
        for (Rule r : rulesList) {
            if (r.getRuleID().equals(rule_id)) {
                return r;
            }
        }
        return null;
    }
    
    public Rule deleteRule(Rule rule) {
        boolean b = rulesList.remove(rule);
        if (b) {
            deleteRuleInDB(rule);
            return rule;
        } else {
            return null;
        }
    }
    
    public Rule deleteRule(String rule_id) {
        for (Rule r : rulesList) {
            if (r.getRuleID().equals(rule_id)) {
                return deleteRule(r);
            }
        }
        return null;
    }
    
    public List<Rule> getRulesTriggerable(String cid, int prop_index) {
        List<Rule> rules = new Vector<>();
        for (Rule rule : rulesList) {
            if (rule.isTriggerable(cid, prop_index)) {
                rules.add(rule);
            }
        }
        return rules;
    }
    
    public void loadRulesFromDB() {
        rulesList = mongo.findAll(Rule.class);
        
        printRulesListSize();
    }
    
    public void printRulesListSize() {
        LOG.info(rulesList.size() + " rules in cache");
    }
    
    private void saveRuleInDB(Rule rule) {
        mongo.save(rule);
    }
    
    private void deleteRuleInDB(Rule rule) {
        mongo.remove(rule);
    }
}
