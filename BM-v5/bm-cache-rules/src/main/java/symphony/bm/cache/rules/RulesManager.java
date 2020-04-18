package symphony.bm.cache.rules;

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
    
    private List<Rule> rulesList = new Vector<>();
    
    public RulesManager(MongoOperations mongo) {
        this.mongo = mongo;
        
        loadRulesFromDB();
    }
    
    public void loadRulesFromDB() {
        rulesList = mongo.findAll(Rule.class);
        
        printRulesListSize();
    }
    
    public void printRulesListSize() {
        LOG.info(rulesList.size() + " rules in cache");
    }
}
