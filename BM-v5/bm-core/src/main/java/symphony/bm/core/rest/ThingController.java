package symphony.bm.core.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.SuperGroup;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.rest.hateoas.AttributeModel;
import symphony.bm.core.rest.hateoas.ThingModel;
import symphony.bm.generics.messages.MicroserviceMessage;
import symphony.bm.generics.messages.MicroserviceSuccessfulMessage;
import symphony.bm.generics.messages.MicroserviceUnsuccessfulMesage;

import java.util.List;
import java.util.Vector;

@RestController
@RequestMapping("/things")
@AllArgsConstructor
@Slf4j
public class ThingController {
    private final SuperGroup superGroup;
    
    @GetMapping
    public List<ThingModel> getThingList() {
        List<ThingModel> thingModels = new Vector<>();
        for (Thing thing : superGroup.getThingList()) {
            thingModels.add(new ThingModel(thing));
        }
        return thingModels;
    }

    @GetMapping("/{uid}")
    public ThingModel getThing(@PathVariable String uid) {
        return new ThingModel(superGroup.getThing(uid));
    }

    @GetMapping("/{uid}/{index}")
    public AttributeModel getAttribute(@PathVariable String uid, @PathVariable int index) {
        return new AttributeModel(superGroup.getThing(uid).getAttributes().get(index), uid, index);
    }

    @PutMapping("/{uid}")
    public ResponseEntity<MicroserviceMessage> updateThing(@PathVariable String uid, @RequestBody Thing thing) {
        if (!uid.equals(thing.getUID())) {
            return buildErrorResponseEntity("UID specified in path (" + uid + ") is not the same with UID of thing ("
                    + thing.getUID() + ") in request body", HttpStatus.CONFLICT);
        }
        
        Thing current = superGroup.getThing(uid);
        if (current == null) {
            log.info("Thing does not exist yet. Adding thing...");
            for (String parentGID : thing.getParentGroups()) {
                Group group = superGroup.getGroup(parentGID);
                if (group == null) {
                    return buildErrorResponseEntity("Group " + parentGID + " does not exist",
                            HttpStatus.BAD_REQUEST);
                }
                log.info("Adding thing to group " + parentGID + "(" + group.getName() + ")");
                group.getThings().add(thing);
            }
            return buildSuccessResponseEntity("Thing added", HttpStatus.CREATED);
        }
        
        return buildSuccessResponseEntity("Thing updated", HttpStatus.OK);
    }
    
    private ResponseEntity<MicroserviceMessage> buildSuccessResponseEntity(String msg, HttpStatus status) {
        log.info(msg);
        return new ResponseEntity<>(new MicroserviceSuccessfulMessage(msg), status);
    }
    
    private ResponseEntity<MicroserviceMessage> buildErrorResponseEntity(String msg, HttpStatus status) {
        log.error(msg);
        return new ResponseEntity<>(new MicroserviceUnsuccessfulMesage(msg), status);
    }
}
