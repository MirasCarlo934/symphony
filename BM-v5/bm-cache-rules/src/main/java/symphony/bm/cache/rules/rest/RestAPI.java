package symphony.bm.cache.rules.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import symphony.bm.cache.rules.RulesManager;
import symphony.bm.cache.rules.vo.Rule;

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
}
