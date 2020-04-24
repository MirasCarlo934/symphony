package symphony.bm.core.rest;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.SuperGroup;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.rest.forms.thing.ThingGroupForm;
import symphony.bm.core.rest.forms.thing.ThingUpdateForm;
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
        for (Thing thing : superGroup.getContainedThings()) {
            thingModels.add(new ThingModel(thing));
        }
        return thingModels;
    }

    @GetMapping("/{uid}")
    public ThingModel get(@PathVariable String uid) {
        return new ThingModel(superGroup.getThingRecursively(uid));
    }

    @GetMapping("/{uid}/attributes/{index}")
    public AttributeModel getAttribute(@PathVariable String uid, @PathVariable int index) {
        return new AttributeModel(superGroup.getThingRecursively(uid).getAttributes().get(index), uid, index);
    }

    @DeleteMapping("/{uid}")
    public ResponseEntity<MicroserviceMessage> delete(@PathVariable String uid) {
        Thing thing = superGroup.getThingRecursively(uid);
        if (thing == null) {
            String warn = "Thing does not exist";
            log.warn(warn);
            return new ResponseEntity<>(new MicroserviceUnsuccessfulMesage(warn), HttpStatus.BAD_REQUEST);
        }

        log.debug("Deleting thing " + uid + " ...");
        List<String> groups = thing.getCopyOfParentGroups();
        if (groups.isEmpty()) {
            superGroup.removeThing(thing);
        }
        for (String parentGID : groups) {
            Group group = superGroup.getGroupRecursively(parentGID);
            log.info("Removing thing from group " + group.getGid() + "(" + group.getName() + ")");
            group.removeThing(thing);
        }
        thing.delete();

        return buildSuccessResponseEntity("Thing " + uid + " deleted", HttpStatus.OK);
    }

    @PostMapping("/{uid}")
    public ResponseEntity<MicroserviceMessage> add(@PathVariable String uid, @RequestBody Thing thing) {
        if (!uid.equals(thing.getUid())) {
            return buildErrorResponseEntity("UID specified in path (" + uid + ") is not the same with UID of thing ("
                    + thing.getUid() + ") in request body", HttpStatus.CONFLICT);
        }

        Thing current = superGroup.getThingRecursively(uid);
        if (current != null) {
            String warn = "Thing already exists. Thing will not be added to context";
            log.warn(warn);
            return new ResponseEntity<>(new MicroserviceUnsuccessfulMesage(warn), HttpStatus.BAD_REQUEST);
        }

        log.debug("Adding thing " + uid + " ...");
        List<Group> groupList = new Vector<>();

        // check if the group/s specified exist/s
        List<String> groups = thing.getCopyOfParentGroups();
        if (groups.isEmpty()) {
            groupList.add(superGroup);
        }
        for (String parentGID : groups) {
            Group group = superGroup.getGroupRecursively(parentGID);
            if (group == null) {
                return buildErrorResponseEntity("Group " + parentGID + " does not exist",
                        HttpStatus.BAD_REQUEST);
            }
            groupList.add(group);
        }
        for (Group group : groupList) {
            log.info("Adding thing " + uid + " to group " + group.getGid() + "(" + group.getName() + ")");
            group.addThing(thing);
        }
        thing.create();

        return buildSuccessResponseEntity("Thing added", HttpStatus.CREATED);
    }

//    @PutMapping("/{uid}")
//    public ResponseEntity<MicroserviceMessage> replace(@PathVariable String uid, @RequestBody Thing thing) {
//        if (!uid.equals(thing.getUid())) {
//            return buildErrorResponseEntity("UID specified in path (" + uid + ") is not the same with UID of thing ("
//                    + thing.getUid() + ") in request body", HttpStatus.CONFLICT);
//        }
//
//        Thing current = superGroup.getThingRecursively(uid);
//        if (current == null) {
//            log.info("Thing does not exist yet");
//            return add(uid, thing);
//        }
//
//        log.info("Replacing thing " + uid + "...");
//        delete(uid);
//        add(uid, thing);
//
//        return buildSuccessResponseEntity("Thing replaced", HttpStatus.OK);
//    }

    @SneakyThrows
    @PatchMapping("/{uid}")
    public ResponseEntity<MicroserviceMessage> update(@PathVariable String uid,
                                                      @RequestBody ThingUpdateForm form) {
        Thing thing = superGroup.getThingRecursively(uid);
        if (thing == null) {
            return buildErrorResponseEntity("Thing does not exist", HttpStatus.NOT_FOUND);
        }

        boolean changed = false;
        if (form.getParentGroups() != null && !thing.isAlreadyGroupedIn(form.getParentGroups())) {
            ThingGroupForm groupForm = new ThingGroupForm();
            groupForm.setParentGroups(thing.getCopyOfParentGroups());
            removeGroup(uid, groupForm);
            groupForm.setParentGroups(form.getParentGroups());
            addGroup(uid, groupForm);
            changed = true;
        }

        log.info("Updating thing...");
        changed = changed || thing.update(form);

        if (changed) {
            return buildSuccessResponseEntity("Thing updated", HttpStatus.OK);
        } else {
            return buildSuccessResponseEntity("Nothing to update", HttpStatus.OK);
        }
    }

    @PostMapping("/{uid}/addgroup")
    public ResponseEntity<MicroserviceMessage> addGroup(@PathVariable String uid, @RequestBody ThingGroupForm form) {
        List<String> groups = form.getParentGroups();
        Thing thing = superGroup.getThingRecursively(uid);
        if (thing == null) {
            String warn = "Thing does not exist";
            log.warn(warn);
            return new ResponseEntity<>(new MicroserviceUnsuccessfulMesage(warn), HttpStatus.BAD_REQUEST);
        }

        log.debug("Adding thing " + thing.getUid() + " to groups " + groups);
        if (groups.isEmpty()) {
            log.info("Adding thing " + thing.getUid() + " to Super Group");
            superGroup.addThing(thing);
        }
        for (String GID : groups) {
            Group group = superGroup.getGroupRecursively(GID);
            if (group == null) {
                log.info("Group " + GID + " does not exist. Creating new group " + GID + " with the same name");
                group = new Group(GID, GID);
                superGroup.addGroup(group);
                group.create();
            }
            log.info("Adding thing to group " + GID);
            group.addThing(thing);
        }

        return buildSuccessResponseEntity("Thing " + thing.getUid() + " added to groups " + groups, HttpStatus.OK);
    }

    @PostMapping("/{uid}/removegroup")
    public ResponseEntity<MicroserviceMessage> removeGroup(@PathVariable String uid, @RequestBody ThingGroupForm form) {
        List<String> groups = form.getParentGroups();
        Thing thing = superGroup.getThingRecursively(uid);
        if (thing == null) {
            String warn = "Thing does not exist";
            log.warn(warn);
            return new ResponseEntity<>(new MicroserviceUnsuccessfulMesage(warn), HttpStatus.BAD_REQUEST);
        }

        log.debug("Removing thing " + thing.getUid() + " from groups " + groups);
        for (String GID : groups) {
            Group group = superGroup.getGroupRecursively(GID);
            if (group != null) {
                log.info("Removing thing from group " + GID);
                group.removeThing(thing);
            }
        }

        if (thing.hasNoGroup()) {
            superGroup.addThing(thing);
        }

        return buildSuccessResponseEntity("Thing " + thing.getUid() + " removed from groups " + groups, HttpStatus.OK);
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
