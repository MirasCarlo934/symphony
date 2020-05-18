package symphony.bm.core.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.SuperGroup;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.rest.forms.thing.ThingGroupForm;
import symphony.bm.core.rest.forms.thing.ThingUpdateForm;
import symphony.bm.core.rest.hateoas.ThingModel;
import symphony.bm.generics.exceptions.RestControllerProcessingException;
import symphony.bm.generics.messages.MicroserviceMessage;
import symphony.bm.generics.messages.MicroserviceSuccessfulMessage;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Vector;

@RestController
@CrossOrigin
@RequestMapping("/things")
@AllArgsConstructor
@Slf4j
public class ThingController {
    private final SuperGroup superGroup;
    private final GroupController groupController;
    private final AttributeController attributeController;
    private final ObjectMapper objectMapper;
    
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
        List<String> parentGIDs = thing.getParentGroups();
        if (parentGIDs.isEmpty()) {
            superGroup.removeThing(thing);
        }
        for (String parentGID : new Vector<>(parentGIDs)) {
            Group group = superGroup.getGroupRecursively(parentGID);
            log.info("Removing thing from group " + group.getGid() + "(" + group.getName() + ")");
            group.removeThing(thing);
        }
        thing.delete();

        return successResponseEntity("Thing " + uid + " deleted", HttpStatus.OK);
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
        List<String> groups = thing.getParentGroups();
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

        return successResponseEntity("Thing added", HttpStatus.CREATED);
    }

    @PatchMapping("/{uid}")
    public ResponseEntity<MicroserviceMessage> update(@PathVariable String uid, @RequestBody ThingUpdateForm form)
            throws RestControllerProcessingException {
        Thing thing = superGroup.getThingRecursively(uid);
        if (thing == null) {
            throw new RestControllerProcessingException("Thing " + uid + " does not exist", HttpStatus.NOT_FOUND);
        }

        log.debug("Updating thing " + uid + "...");
        boolean changed = false;
        if (form.getParentGroups() != null) {
            changed = updateGroups(thing, form.getParentGroups());
        }

        boolean updated = thing.update(form);
        changed = changed || updated;

        if (changed) {
            return successResponseEntity("Thing " + uid + " updated", HttpStatus.OK);
        } else {
            return successResponseEntity("Nothing to update", HttpStatus.OK);
        }
    }
    
    @PutMapping(value = "/{uid}/{field}", consumes = "text/plain")
    public ResponseEntity<MicroserviceMessage> updateField(@PathVariable String uid, @PathVariable String field,
                                                           @RequestBody String value)
            throws RestControllerProcessingException {
        Thing thing = superGroup.getThingRecursively(uid);
        if (thing == null) {
            throw new RestControllerProcessingException("Thing " + uid + " does not exist", HttpStatus.NOT_FOUND);
        }
    
        log.debug("Updating " + field + " of " + uid + " ...");
        boolean changed = false;
        try {
            changed = thing.update(field, value);
        } catch (Exception e) {
            throw new RestControllerProcessingException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    
        if (changed) {
            return successResponseEntity("Thing " + uid + " " + field + " updated", HttpStatus.OK);
        } else {
            return successResponseEntity("Nothing to update", HttpStatus.OK);
        }
    }
    
    @PutMapping(value = "/{uid}/{field}", consumes = "application/json")
    public ResponseEntity<MicroserviceMessage> updateField(@PathVariable String uid, @PathVariable String field,
                                                           @RequestBody Object value)
            throws RestControllerProcessingException {
        Thing thing = superGroup.getThingRecursively(uid);
        if (thing == null) {
            throw new RestControllerProcessingException("Thing " + uid + " does not exist", HttpStatus.NOT_FOUND);
        }

        boolean changed = false;
        if (field.equals("parentGroups")) {
            try {
                List<String> parentGroups = (List<String>) value;
                changed = updateGroups(thing, parentGroups);
            } catch (ClassCastException e) {
                throw new RestControllerProcessingException("Invalid data sent (must be JSON string array)",
                        HttpStatus.BAD_REQUEST);
            }
        } else {
            try {
                changed = thing.update(field, value);
            } catch (Exception e) {
                throw new RestControllerProcessingException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
            }
        }
        
        if (changed) {
            return successResponseEntity("Thing " + uid + " " + field + " updated", HttpStatus.OK);
        } else {
            return successResponseEntity("Nothing to update", HttpStatus.OK);
        }
    }

    @PutMapping("/{uid}")
    public ResponseEntity<MicroserviceMessage> put(@PathVariable String uid, @RequestBody Thing thing)
            throws RestControllerProcessingException {
        if (!uid.equals(thing.getUid())) {
            throw new RestControllerProcessingException("UID specified in path (" + uid + ") is not the same with UID of thing ("
                    + thing.getUid() + ") in request body", HttpStatus.CONFLICT);
        }

        Thing current = superGroup.getThingRecursively(uid);
        if (current == null) {
            log.debug("Thing " + uid + " does not exist yet");
            return add(uid, thing);
        }

        log.debug("Updating thing " + uid + "...");
        boolean changed = false;
        ThingUpdateForm form = new ThingUpdateForm(thing.getName(), thing.getParentGroups(),
                thing.getCopyOfAttributeList());
        if (form.getAttributes() != null && !form.getAttributes().isEmpty()) {
            List<Attribute> attributesToRemove = new Vector<>(current.getCopyOfAttributeList());
            for (Attribute attribute : form.getAttributes()) {
                attributesToRemove.removeIf(a -> a.getAid().equals(attribute.getAid()));
                attributeController.put(uid, attribute.getAid(), attribute);
            }
            for (Attribute attribute : attributesToRemove) {
                attributeController.delete(uid, attribute.getAid());
            }
            changed = true;
        }

        if (changed) {
            update(uid, form);
            return successResponseEntity("Thing " + uid + " updated", HttpStatus.OK);
        } else {
            return update(uid, form);
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

        return successResponseEntity("Thing " + thing.getUid() + " added to groups " + groups, HttpStatus.OK);
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

        return successResponseEntity("Thing " + thing.getUid() + " removed from groups " + groups, HttpStatus.OK);
    }
    
    private boolean updateGroups(Thing thing, List<String> parentGIDs) throws RestControllerProcessingException {
        if (!thing.hasSameParentGroups(parentGIDs)) {
            List<String> groupsToAdd = new Vector<>();
            List<String> groupsToRemove = new Vector<>(thing.getParentGroups());
            parentGIDs.forEach(parentGID -> {
                if (!thing.hasGroup(parentGID)) {
                    groupsToAdd.add(parentGID);
                } else {
                    groupsToRemove.remove(parentGID);
                }
            });
            ThingGroupForm groupForm = new ThingGroupForm(groupsToAdd);
            addGroup(thing.getUid(), groupForm);
            groupForm.setParentGroups(groupsToRemove);
            removeGroup(thing.getUid(), groupForm);
            return true;
        }
        return false;
    }

    private ResponseEntity<MicroserviceMessage> successResponseEntity(String msg, HttpStatus status) {
        log.info(msg);
        return new ResponseEntity<>(new MicroserviceSuccessfulMessage(msg), status);
    }

//    private ResponseEntity<MicroserviceMessage> buildErrorResponseEntity(String msg, HttpStatus status) {
//        log.error(msg);
//        return new ResponseEntity<>(new MicroserviceUnsuccessfulMesage(msg), status);
//    }
}
