package symphony.bm.cir.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import symphony.bm.cir.rules.Rule;
import symphony.bm.cir.rules.RuleFactory;
import symphony.bm.generics.exceptions.RestControllerProcessingException;

import java.util.List;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/rules")
public class RulesController {
    private final RuleFactory ruleFactory;

    @GetMapping
    public List<Rule> getAllRules() {
        return ruleFactory.getRules();
    }
    
    @GetMapping("/{rid}")
    public Rule getRule(@PathVariable String rid) throws RestControllerProcessingException {
        Rule rule = ruleFactory.getRule(rid);
        if (rule == null) {
            throw new RestControllerProcessingException("Rule " + rid + " does not exist", HttpStatus.NOT_FOUND);
        }
        
        return rule;
    }
}
