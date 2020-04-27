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
import symphony.bm.core.rest.hateoas.ThingModel;
import symphony.bm.generics.exceptions.RestControllerProcessingException;
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
    private final GroupController groupController;
    
    @GetMapping
    public List<ThingModel> getThingList() {
        List<ThingModel> thingModels = new Vector<>();
        for (Thing thing : superGroup.getContainedThings()) {
            thingModels.add(new ThingModel(thing));
        }
        return thingModels;
    }

    @GetMapping("/{uid}")
    public ThingModel get(@PathVariable String uid) throws RestControllerProcessingException {
        Thing thing = superGroup.getThingRecursively(uid);
        if (thing == null) {
            throw new RestControllerProcessingException("Thing " + uid + " does not exist", HttpStatus.NOT_FOUND);
        }
        return new ThingModel(superGroup.getThingRecursively(uid));
    }

    @DeleteMapping("/{uid}")
    public ResponseEntity<MicroserviceMessage> delete(@PathVariable String uid) throws RestControllerProcessingException {
        Thing thing = superGroup.getThingRecursively(uid);
        if (thing == null) {
            throw new RestControllerProcessingException("Thing " + uid + " does not exist", HttpStatus.NOT_FOUND);
        }

        log.debug("Deleting thing " + uid + " ...");
        List<String> parentGIDs = thing.getCopyOfParentGroups();
        if (parentGIDs.isEmpty()) {
            superGroup.removeThing(thing);
        }
        for (String parentGID : parentGIDs) {
            Group group = superGroup.getGroupRecursively(parentGID);
            log.info("Removing thing from group " + group.getGid() + "(" + group.getName() + ")");
            group.removeThing(thing);
        }
        thing.delete();

        return buildSuccessResponseEntity("Thing " + uid + " deleted", HttpStatus.OK);
    }

    @PostMapping("/{uid}")
    public ResponseEntity<MicroserviceMessage> add(@PathVariable String uid, @RequestBody Thing thing)
            throws RestControllerProcessingException {
        if (!uid.equals(thing.getUid())) {
            throw new RestControllerProcessingException("UID specified in path (" + uid + ") is not the same with UID of thing ("
                    + thing.getUid() + ") in request body", HttpStatus.CONFLICT);
        }

        Thing current = superGroup.getThingRecursively(uid);
        if (current != null) {
            throw new RestControllerProcessingException("Thing " + uid + " already exists", HttpStatus.NOT_FOUND);
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
                group = groupController.createDefaultGroup(parentGID);
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

    @PatchMapping("/{uid}")
    public ResponseEntity<MicroserviceMessage> update(@PathVariable String uid, @RequestBody ThingUpdateForm form)
            throws RestControllerProcessingException {
        Thing thing = superGroup.getThingRecursively(uid);
        if (thing == null) {
            throw new RestControllerProcessingException("Thing " + uid + " does not exist", HttpStatus.NOT_FOUND);
        }

        log.debug("Updating thing " + uid + "...");
        boolean changed = false;
        if (form.getParentGroups() != null && !thing.hasSameParentGroups(form.getParentGroups())) {
            List<String> groupsToAdd = new Vector<>();
            List<String> groupsToRemove = new Vector<>(thing.getCopyOfParentGroups());
            form.getParentGroups().forEach(parentGID -> {
                if (!thing.hasGroup(parentGID)) {
                    groupsToAdd.add(parentGID);
                } else {
                    groupsToRemove.remove(parentGID);
                }
            });
            ThingGroupForm groupForm = new ThingGroupForm();
            groupForm.setParentGroups(groupsToAdd);
            addGroup(uid, groupForm);
            groupForm.setParentGroups(groupsToRemove);
            removeGroup(uid, groupForm);
            changed = true;
        }

        boolean updated = thing.update(form);
        changed = changed || updated;

        if (changed) {
            return buildSuccessResponseEntity("Thing " + uid + " updated", HttpStatus.OK);
        } else {
            return buildSuccessResponseEntity("Nothing to update", HttpStatus.OK);
        }
    }

    @PostMapping("/{uid}/addgroup")
    public ResponseEntity<MicroserviceMessage> addGroup(@PathVariable String uid, @RequestBody ThingGroupForm form)
            throws RestControllerProcessingException {
        List<String> groups = form.getParentGroups();
        Thing thing = superGroup.getThingRecursively(uid);
        if (thing == null) {
            throw new RestControllerProcessingException("Thing " + uid + " does not exist", HttpStatus.NOT_FOUND);
        }

        log.debug("Adding thing " + thing.getUid() + " to groups " + groups);
        if (groups == null || groups.isEmpty()) {
            log.info("Adding thing " + thing.getUid() + " to Super Group...");
            superGroup.addThing(thing);
        }
        for (String GID : groups) {
            Group group = superGroup.getGroupRecursively(GID);
            if (group == null) {
                group = groupController.createDefaultGroup(GID);
            }
            log.info("Adding thing to group " + GID + "...");
            group.addThing(thing);
        }

        return buildSuccessResponseEntity("Thing " + thing.getUid() + " added to groups " + groups, HttpStatus.OK);
    }

    @PostMapping("/{uid}/removegroup")
    public ResponseEntity<MicroserviceMessage> removeGroup(@PathVariable String uid, @RequestBody ThingGroupForm form)
            throws RestControllerProcessingException {
        List<String> groups = form.getParentGroups();
        Thing thing = superGroup.getThingRecursively(uid);
        if (thing == null) {
            throw new RestControllerProcessingException("Thing " + uid + " does not exist", HttpStatus.NOT_FOUND);
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

//    private ResponseEntity<MicroserviceMessage> buildErrorResponseEntity(String msg, HttpStatus status) {
//        log.error(msg);
//        return new ResponseEntity<>(new MicroserviceUnsuccessfulMesage(msg), status);
//    }
}
