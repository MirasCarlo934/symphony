package symphony.bm.cir.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import symphony.bm.cir.rules.Rule;
import symphony.bm.cir.rules.RuleFactory;
import symphony.bm.generics.exceptions.RestControllerProcessingException;
import symphony.bm.generics.messages.MicroserviceMessage;
import symphony.bm.generics.messages.MicroserviceSuccessfulMessage;

import java.util.List;

@RestController
@CrossOrigin
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
    
    @DeleteMapping("/{rid}")
    public ResponseEntity<MicroserviceMessage> deleteRule(@PathVariable String rid)
            throws RestControllerProcessingException {
        if (ruleFactory.deleteRule(rid)) {
            return successResponseEntity("Rule " + rid + " deleted", HttpStatus.OK);
        } else {
            throw new RestControllerProcessingException("Rule " + rid + " does not exist", HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping("/{rid}")
    public ResponseEntity<MicroserviceMessage> addRule(@PathVariable String rid, @RequestBody Rule rule)
            throws RestControllerProcessingException {
        if (ruleFactory.getRule(rid) != null) {
            throw new RestControllerProcessingException("Rule " + rid + " already exists", HttpStatus.CONFLICT);
        }
        if (!rule.getRid().equals(rid)) {
            throw new RestControllerProcessingException("RID specified in path (" + rid + ") is not the same with RID of rule ("
                    + rule.getRid() + ") in request body", HttpStatus.CONFLICT);
        }
        ruleFactory.addRule(rule);
        
        return successResponseEntity("Rule " + rid + " created", HttpStatus.CREATED);
    }
    
    @PutMapping("/{rid}")
    public ResponseEntity<MicroserviceMessage> putRule(@PathVariable String rid, @RequestBody Rule rule)
            throws RestControllerProcessingException{
        if (ruleFactory.getRule(rid) == null) {
            return addRule(rid, rule);
        } else {
            if (!rule.getRid().equals(rid)) {
                throw new RestControllerProcessingException("RID specified in path (" + rid + ") is not the same with RID of rule ("
                        + rule.getRid() + ") in request body", HttpStatus.CONFLICT);
            }
            ruleFactory.deleteRule(rid);
            ruleFactory.addRule(rule);
            return successResponseEntity("Rule " + rid + " updated", HttpStatus.OK);
        }
    }
    
    private ResponseEntity<MicroserviceMessage> successResponseEntity(String msg, HttpStatus status) {
        log.info(msg);
        return new ResponseEntity<>(new MicroserviceSuccessfulMessage(msg), status);
    }
}
