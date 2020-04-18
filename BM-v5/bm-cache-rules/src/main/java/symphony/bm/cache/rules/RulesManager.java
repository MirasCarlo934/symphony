package symphony.bm.cache.rules;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;
import symphony.bm.cache.rules.vo.Rule;

import java.util.List;
import java.util.Vector;

@Component
public class RulesManager {
    private static final Logger LOG = LoggerFactory.getLogger(RulesManager.class);
    private MongoOperations mongo;
    
    @Getter private List<Rule> rulesList = new Vector<>();
    
    public RulesManager(MongoOperations mongo) {
        this.mongo = mongo;
        
        loadRulesFromDB();
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
}
