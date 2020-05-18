package symphony.bm.core.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.SuperGroup;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.rest.forms.group.GroupGroupForm;
import symphony.bm.core.rest.forms.group.GroupUpdateForm;
import symphony.bm.core.rest.forms.thing.ThingGroupForm;
import symphony.bm.core.rest.hateoas.BasicGroupModel;
import symphony.bm.core.rest.hateoas.GroupModel;
import symphony.bm.generics.exceptions.RestControllerProcessingException;
import symphony.bm.generics.messages.MicroserviceMessage;
import symphony.bm.generics.messages.MicroserviceSuccessfulMessage;
import symphony.bm.generics.messages.MicroserviceUnsuccessfulMessage;

import java.util.List;
import java.util.Vector;

@RestController
@CrossOrigin
@RequestMapping("/groups")
@AllArgsConstructor
@Slf4j
public class GroupController {
    private final SuperGroup superGroup;

    @GetMapping
    public List<GroupModel> getAll() {
        List<GroupModel> groupModels = new Vector<>();
        superGroup.getContainedGroups().forEach( group -> groupModels.add(new GroupModel(group)) );
        return groupModels;
    }

    @GetMapping("/{gid}")
    public GroupModel get(@PathVariable String gid) throws RestControllerProcessingException {
        Group group = superGroup.getGroupRecursively(gid);
        if (group == null) {
            throw new RestControllerProcessingException("Group " + gid + " does not exist", HttpStatus.NOT_FOUND);

        }
        return new GroupModel(group);
    }

    @DeleteMapping("/{gid}")
    public ResponseEntity<MicroserviceMessage> delete(@PathVariable String gid)
            throws RestControllerProcessingException {
        Group group = superGroup.getGroupRecursively(gid);
        if (group != null) {
            for (Thing thing : group.getCopyOfThingList()) {
                log.debug("Thing " + thing.getUid() + " removed from group " + gid);
                group.removeThing(thing);
                if (thing.hasNoGroup()) {
                    superGroup.addThing(thing);
                }
            }
            for (Group subgroup : group.getCopyOfGroupList()) {
                log.debug("Group " + subgroup.getGid() + " removed from group " + gid);
                group.removeGroup(subgroup);
                if (subgroup.hasNoGroup()) {
                    superGroup.addGroup(subgroup);
                }
            }
            for (String parentGID : new Vector<>(group.getParentGroups())) {
                log.debug("Group " + gid + " removed from group " + parentGID);
                Group parent = superGroup.getGroupRecursively(parentGID);
                parent.removeGroup(group);
            }
            group.delete();
            return successResponseEntity("Group " + gid + " removed", HttpStatus.OK);
        } else {
            throw new RestControllerProcessingException("Group does not exist", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{gid}")
    public ResponseEntity<MicroserviceMessage> add(@PathVariable String gid, @RequestBody Group group)
            throws RestControllerProcessingException {
        if (!group.getGid().equals(gid)) {
            throw new RestControllerProcessingException("GID specified in path (" + gid + ") is not the same with " +
                    "GID of group (" + group.getGid() + ") in request body", HttpStatus.CONFLICT);
        }

        if (superGroup.getGroupRecursively(gid) != null) {
            throw new RestControllerProcessingException("Group " + gid + " already exists", HttpStatus.CONFLICT);
        }

        log.debug("Adding group " + gid + "...");
        for (String parentGID : group.getParentGroups()) {
            Group parent = superGroup.getGroupRecursively(parentGID);
            if (parent == null) {
                parent = createDefaultGroup(parentGID);
            }
            parent.addGroup(group);
        }
        group.create();

        return successResponseEntity("Group " + gid + " added", HttpStatus.CREATED);
    }

    @PatchMapping("/{gid}")
    public ResponseEntity<MicroserviceMessage> update(@PathVariable String gid, @RequestBody GroupUpdateForm form)
            throws RestControllerProcessingException {
        Group group = superGroup.getGroupRecursively(gid);
        if (group == null) {
            throw new RestControllerProcessingException("Group does not exist", HttpStatus.NOT_FOUND);
        }

        log.debug("Updating group " + gid + "...");
        boolean changed = false;
        if (form.getParentGroups() != null) {
            changed = updateGroups(group, form.getParentGroups());
        }

        boolean updated = group.update(form);
        changed = changed || updated;

        if (changed) {
            return successResponseEntity("Group " + gid + " updated", HttpStatus.OK);
        } else {
            return successResponseEntity("Nothing to update", HttpStatus.OK);
        }
    }

    @PutMapping(value = "/{gid}/{field}", consumes = "text/plain")
    public ResponseEntity<MicroserviceMessage> updateField(@PathVariable String gid, @PathVariable String field,
                                                           @RequestBody String value)
            throws RestControllerProcessingException {
        Group group = superGroup.getGroupRecursively(gid);
        if (group == null) {
            throw new RestControllerProcessingException("Group does not exist", HttpStatus.NOT_FOUND);
        }

        log.debug("Updating " + field + " of " + gid + " ...");
        boolean changed = false;
        try {
            changed = group.update(field, value);
        } catch (Exception e) {
            throw new RestControllerProcessingException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
        }

        if (changed) {
            return successResponseEntity("Group " + gid + " " + field + " updated", HttpStatus.OK);
        } else {
            return successResponseEntity("Nothing to update", HttpStatus.OK);
        }
    }

    @PutMapping(value = "/{gid}/{field}", consumes = "application/json")
    public ResponseEntity<MicroserviceMessage> updateField(@PathVariable String gid, @PathVariable String field,
                                                           @RequestBody Object value)
            throws RestControllerProcessingException {
        Group group = superGroup.getGroupRecursively(gid);
        if (group == null) {
            throw new RestControllerProcessingException("Group does not exist", HttpStatus.NOT_FOUND);
        }

        boolean changed = false;
        if (field.equals("parentGroups")) {
            try {
                List<String> parentGroups = (List<String>) value;
                changed = updateGroups(group, parentGroups);
            } catch (ClassCastException e) {
                throw new RestControllerProcessingException("Invalid data sent (must be JSON string array)",
                        HttpStatus.BAD_REQUEST);
            }
        } else {
            try {
                changed = group.update(field, value);
            } catch (Exception e) {
                throw new RestControllerProcessingException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
            }
        }

        if (changed) {
            return successResponseEntity("Group " + gid + " " + field + " updated", HttpStatus.OK);
        } else {
            return successResponseEntity("Nothing to update", HttpStatus.OK);
        }
    }

    @PostMapping("/{gid}/addgroup")
    public ResponseEntity<MicroserviceMessage> addGroup(@PathVariable String gid, @RequestBody GroupGroupForm form)
            throws RestControllerProcessingException {
        List<String> groups = form.getParentGroups();
        Group group = superGroup.getGroupRecursively(gid);
        if (group == null) {
            throw new RestControllerProcessingException("Group " + gid + " does not exist", HttpStatus.NOT_FOUND);
        }
        
        log.debug("Adding group " + group.getGid() + " to groups " + groups);
        if (groups == null || groups.isEmpty()) {
            log.info("Adding group " + gid + " to Super Group");
            superGroup.addGroup(group);
        }
        for (String parentGID : groups) {
            Group parentGroup = superGroup.getGroupRecursively(parentGID);
            if (parentGroup == null) {
                parentGroup = createDefaultGroup(parentGID);
            } else if (group.getGroupRecursively(parentGID) != null) {
                throw new RestControllerProcessingException("Adding group " + gid + " under group " + parentGID
                        + " will result to a circular grouping structure (child will contain parent)",
                        HttpStatus.CONFLICT);
            }
            log.info("Adding group " + gid + " to group " + parentGID);
            parentGroup.addGroup(group);
        }
        
        return successResponseEntity("Group " + gid + " added to groups " + groups, HttpStatus.OK);
    }
    
    @PostMapping("/{gid}/removegroup")
    public ResponseEntity<MicroserviceMessage> removeGroup(@PathVariable String gid, @RequestBody GroupGroupForm form) {
        List<String> groups = form.getParentGroups();
        Group group = superGroup.getGroupRecursively(gid);
        if (group == null) {
            String warn = "Group does not exist";
            log.warn(warn);
            return new ResponseEntity<>(new MicroserviceUnsuccessfulMessage(warn), HttpStatus.BAD_REQUEST);
        }
    
        log.debug("Removing group " + gid + " from groups " + groups);
        for (String GID : groups) {
            Group parentGroup = superGroup.getGroupRecursively(GID);
            if (parentGroup != null) {
                log.info("Removing group from group " + GID);
                parentGroup.removeGroup(group);
            }
        }
    
        if (group.hasNoGroup()) {
            superGroup.addGroup(group);
        }
    
        return successResponseEntity("Group " + gid + " removed from groups " + groups, HttpStatus.OK);
    }

    private boolean updateGroups(Group group, List<String> parentGIDs) throws RestControllerProcessingException {
        if (!group.hasSameParentGroups(parentGIDs)) {
            List<String> groupsToAdd = new Vector<>();
            List<String> groupsToRemove = new Vector<>(group.getParentGroups());
            parentGIDs.forEach(parentGID -> {
                if (!group.hasGroup(parentGID)) {
                    groupsToAdd.add(parentGID);
                } else {
                    groupsToRemove.remove(parentGID);
                }
            });
            GroupGroupForm groupForm = new GroupGroupForm();
            groupForm.setParentGroups(groupsToAdd);
            addGroup(group.getGid(), groupForm);
            groupForm.setParentGroups(groupsToRemove);
            removeGroup(group.getGid(), groupForm);
            return true;
        }
        return false;
    }
    
    public Group createDefaultGroup(String GID) {
        log.info("Group " + GID + " does not exist. Creating new group " + GID + " with the same name");
        Group group = new Group(GID, GID);
        superGroup.addGroup(group);
        group.create();
        return group;
    }

    private ResponseEntity<MicroserviceMessage> successResponseEntity(String msg, HttpStatus status) {
        log.info(msg);
        return new ResponseEntity<>(new MicroserviceSuccessfulMessage(msg), status);
    }
}
