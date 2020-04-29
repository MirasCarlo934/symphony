package symphony.bm.cir.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import symphony.bm.cir.rules.RulesManager;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.rest.forms.attribute.AttributeUpdateForm;
import symphony.bm.core.rest.forms.thing.ThingUpdateForm;
import symphony.bm.generics.messages.MicroserviceMessage;
import symphony.bm.generics.messages.MicroserviceSuccessfulMessage;

@RestController
@Slf4j
@AllArgsConstructor
public class RulesController {
    private final RulesManager rulesManager;

    @PostMapping("/attributes/{aid}")
    public ResponseEntity<MicroserviceMessage> attributeUpdated(@PathVariable String aid,
                                                                @RequestBody Attribute attribute) {
        Attribute tracking = rulesManager.getAttribute(aid);
        if (tracking == null) {
            String debug = "Attribute " + aid + " not being tracked";
            log.debug(debug);
            return new ResponseEntity<>(new MicroserviceSuccessfulMessage(debug), HttpStatus.OK);
        }

        AttributeUpdateForm form = new AttributeUpdateForm(attribute.getName(), attribute.getMode(),
                attribute.getDataType(), attribute.getValue());
        tracking.update(form);
        return successResponseEntity("Attribute " + aid + " updated", HttpStatus.OK);
    }

    @DeleteMapping("/attributes/{aid}")
    public ResponseEntity<MicroserviceMessage> attributeDeleted(@PathVariable String aid) {
        if (rulesManager.getAttribute(aid) == null) {
            String debug = "Attribute " + aid + " not being tracked";
            log.debug(debug);
            return new ResponseEntity<>(new MicroserviceSuccessfulMessage(debug), HttpStatus.OK);
        }

        rulesManager.untrackAttribute(aid);
        return successResponseEntity("Attribute " + aid + " untracked", HttpStatus.OK);
    }

    @PostMapping("/things/{uid}")
    public ResponseEntity<MicroserviceMessage> thingUpdated(@PathVariable String uid, @RequestBody Thing thing) {
        Thing tracking = rulesManager.getThing(uid);
        if (tracking == null) {
            String debug = "Thing " + uid + " not being tracked";
            log.debug(debug);
            return new ResponseEntity<>(new MicroserviceSuccessfulMessage(debug), HttpStatus.OK);
        }

        ThingUpdateForm form = new ThingUpdateForm(thing.getName(), thing.getParentGroups(), null);
        tracking.update(form);
        return successResponseEntity("Thing " + uid + " updated", HttpStatus.OK);
    }

    @DeleteMapping("/things/{uid}")
    public ResponseEntity<MicroserviceMessage> thingDeleted(@PathVariable String uid) {
        if (rulesManager.getThing(uid) == null) {
            String debug = "Thing " + uid + " not being tracked";
            log.debug(debug);
            return new ResponseEntity<>(new MicroserviceSuccessfulMessage(debug), HttpStatus.OK);
        }

        rulesManager.untrackThing(uid);
        return successResponseEntity("Thing " + uid + " untracked", HttpStatus.OK);
    }

    private ResponseEntity<MicroserviceMessage> successResponseEntity(String msg, HttpStatus status) {
        log.info(msg);
        return new ResponseEntity<>(new MicroserviceSuccessfulMessage(msg), status);
    }
}
