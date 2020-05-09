package symphony.bm.cir.rules;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;
import symphony.bm.core.activitylisteners.ActivityListenerManager;

import java.util.List;
import java.util.Vector;

@Component
@Slf4j
public class RuleFactory {
    private final MongoOperations mongo;
    private final SubscribableChannel inbound;
    private final ObjectMapper objectMapper;
    private final ActivityListenerManager activityListenerManager;
    
    @Getter private List<Rule> rules = new Vector<>();
    
    public RuleFactory(MongoOperations mongo, ObjectMapper objectMapper,
                       @Qualifier("mqttThingsChannel") SubscribableChannel inbound,
                       ActivityListenerManager activityListenerManager) {
        this.mongo = mongo;
        this.inbound = inbound;
        this.objectMapper = objectMapper;
        this.activityListenerManager = activityListenerManager;
        
        loadRulesFromDB();
    }
    
    public void loadRulesFromDB() {
        log.debug("Loading rules from DB...");
        rules = mongo.findAll(Rule.class);
        
        for (Rule rule : rules) {
            rule.setActivityListenerManager(activityListenerManager);
            rule.setObjectMapper(objectMapper);
            inbound.subscribe(rule);
        }
        
        printRuleCount();
    }
    
    public Rule getRule(String rid) {
        for (Rule rule : rules) {
            if (rule.getRid().equals(rid)) {
                return rule;
            }
        }
        return null;
    }
    
    public boolean addRule(Rule rule) {
        if (getRule(rule.getRid()) == null) {
            rule.setActivityListenerManager(activityListenerManager);
            rule.setObjectMapper(objectMapper);
            inbound.subscribe(rule);
            rules.add(rule);
            mongo.save(rule);
            log.info("Rule " + rule.getRid() + " added");
            printRuleCount();
            return true;
        }
        return false;
    }
    
    public boolean deleteRule(String rid) {
        Rule rule = getRule(rid);
        if (rule != null) {
            rules.removeIf(r -> r.getRid().equals(rid));
            mongo.remove(rule);
            log.info("Rule " + rid + " removed");
            printRuleCount();
        }
        return rule != null;
    }
    
    public void printRuleCount() {
        log.info(rules.size() + " rules currently exist");
    }
}
