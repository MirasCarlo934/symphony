package symphony.bm.cache.rules.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import symphony.bm.cache.devices.rest.messages.MicroserviceMessage;
import symphony.bm.cache.devices.rest.messages.MicroserviceSuccessfulMessage;
import symphony.bm.cache.rules.RulesManager;
import symphony.bm.cache.rules.vo.Rule;
import symphony.bm.generics.jeep.JeepMessage;
import symphony.bm.generics.jeep.response.JeepSuccessResponse;

import java.util.List;

@RestController
public class RestAPI {
    private final static Logger LOG = LoggerFactory.getLogger(RestAPI.class);
    
    private final RulesManager rulesManager;
    
    public RestAPI(@Autowired RulesManager rulesManager) {
        this.rulesManager = rulesManager;
    }
    
    @GetMapping("/")
    public List<Rule> getAllRules() {
        LOG.info("Get all rules requested");
        return rulesManager.getRulesList();
    }
    
    @GetMapping("/{cid}/{prop_index}")
    public List<Rule> getRulesTriggerable(@PathVariable String cid, @PathVariable int prop_index) {
        LOG.info("Getting rules triggerable by " + cid + "." + prop_index + " ...");
        List<Rule> rules = rulesManager.getRulesTriggerable(cid, prop_index);
        LOG.info(rules.size() + " rules triggerable");
        return rules;
    }
    
    @PutMapping("/rules/{rule_id}")
    public MicroserviceMessage putRule(@PathVariable String rule_id, @RequestBody Rule rule) {
        LOG.info("Upserting rule " + rule_id + "...");
        Rule r = rulesManager.getRule(rule_id);
        
        if (r != null) {
            LOG.info("Updating rule " + rule_id + "...");
            rulesManager.deleteRule(r);
            rulesManager.addRule(rule);
            LOG.info("Rule " + rule_id + " updated");
        } else {
            LOG.info("Adding rule " + rule_id + "...");
            rulesManager.addRule(rule);
            rulesManager.printRulesListSize();
            LOG.info("Rule " + rule_id + " added");
        }
        return new MicroserviceSuccessfulMessage();
    }
    
    @DeleteMapping("/rules/{rule_id}")
    public MicroserviceMessage deleteRule(@PathVariable String rule_id) {
        LOG.info("Deleting rule " + rule_id + "...");
        Rule rule = rulesManager.deleteRule(rule_id);
        if (rule != null) {
            LOG.info("Rule " + rule_id + " deleted");
        } else {
            LOG.info("Rule " + rule_id + " does not exist");
        }
        return new MicroserviceSuccessfulMessage();
    }
}
