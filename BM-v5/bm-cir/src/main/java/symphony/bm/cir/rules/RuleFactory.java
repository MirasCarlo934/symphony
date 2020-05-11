package symphony.bm.cir.rules;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;
import symphony.bm.cir.messaging.ThingChannelFilter;
import symphony.bm.cir.rules.namespaces.Namespace;
import symphony.bm.core.activitylisteners.ActivityListenerManager;
import symphony.bm.core.iot.Thing;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

@Component
@Slf4j
public class RuleFactory {
    private final MongoOperations mongo;
    private final SubscribableChannel mainThingsChannel;
    private final ObjectMapper objectMapper;
    private final ActivityListenerManager activityListenerManager;
    private final HashMap<String, ThingChannelFilter> channelFilters = new HashMap<>();
    
    @Getter private List<Rule> rules = new Vector<>();
    
    public RuleFactory(MongoOperations mongo, ObjectMapper objectMapper,
                       @Qualifier("mainThingsChannel") SubscribableChannel mainThingsChannel,
                       ActivityListenerManager activityListenerManager) {
        this.mongo = mongo;
        this.objectMapper = objectMapper;
        this.mainThingsChannel = mainThingsChannel;
        this.activityListenerManager = activityListenerManager;
        
        loadRulesFromDB();
    }
    
    public void loadRulesFromDB() {
        log.debug("Loading rules from DB...");
        rules = mongo.findAll(Rule.class);
        
        for (Rule rule : rules) {
            rule.setActivityListenerManager(activityListenerManager);
            rule.setObjectMapper(objectMapper);
            subscribeRule(rule);
//            rule.setMqttAdapter(getMqttAdapter());
//            inbound.subscribe(rule);
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
            subscribeRule(rule);
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
            unsubscribeRule(rule);
            rules.removeIf(r -> r.getRid().equals(rid));
            mongo.remove(rule);
            log.info("Rule " + rid + " removed");
            printRuleCount();
        }
        return rule != null;
    }
    
    private void subscribeRule(Rule rule) {
        for (Namespace namespace : rule.getNamespaces()) {
            ThingChannelFilter channelFilter = channelFilters.get(namespace.getThingURL());
            if (channelFilter == null) {
                channelFilter = new ThingChannelFilter(namespace.getThingURL());
                mainThingsChannel.subscribe(channelFilter);
                channelFilter.getThingChannel().subscribe(rule);
                channelFilters.put(namespace.getThingURL(), channelFilter);
            } else {
                channelFilter.getThingChannel().subscribe(rule);
            }
        }
    }
    
    private void unsubscribeRule(Rule rule) {
        for (Namespace namespace : rule.getNamespaces()) {
            channelFilters.get(namespace.getThingURL()).getThingChannel().unsubscribe(rule);
        }
    }
    
    public void printRuleCount() {
        log.info(rules.size() + " rules currently exist");
    }
}
