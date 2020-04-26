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
import symphony.bm.core.rest.hateoas.GroupModel;
import symphony.bm.generics.exceptions.RestControllerProcessingException;
import symphony.bm.generics.messages.MicroserviceMessage;
import symphony.bm.generics.messages.MicroserviceSuccessfulMessage;
import symphony.bm.generics.messages.MicroserviceUnsuccessfulMesage;

import java.util.List;

@RestController
@RequestMapping("/groups")
@AllArgsConstructor
@Slf4j
public class GroupController {
    private final SuperGroup superGroup;

    @GetMapping
    public GroupModel getSuperGroup() {
        return new GroupModel(superGroup);
    }

    @GetMapping("/{gid}")
    public GroupModel get(@PathVariable String gid) throws RestControllerProcessingException {
        Group group = superGroup.getGroupRecursively(gid);
        if (group != null) {
            return new GroupModel(group);
        } else {
            throw new RestControllerProcessingException("Group does not exist", HttpStatus.NOT_FOUND);
        }
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
            for (String parentGID : group.getCopyOfParentGroups()) {
                log.debug("Group " + gid + " removed from group " + parentGID);
                Group parent = superGroup.getGroupRecursively(parentGID);
                parent.removeGroup(group);
            }
            group.delete();
            return buildSuccessResponseEntity("Group " + gid + " removed", HttpStatus.OK);
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
        for (String parentGID : group.getCopyOfParentGroups()) {
            Group parent = superGroup.getGroupRecursively(parentGID);
            if (parent == null) {
                parent = createDefaultGroup(parentGID);
            }
            parent.addGroup(group);
        }
        group.create();

        return buildSuccessResponseEntity("Group " + gid + " added", HttpStatus.CREATED);
    }
    
//    @PatchMapping("/{gid}")
//    public ResponseEntity<MicroserviceMessage> update(@PathVariable String gid, @RequestBody GroupUpdateForm form) {
//
//    }
    
    @PostMapping("/{gid}/addgroup")
    public ResponseEntity<MicroserviceMessage> addGroup(@PathVariable String gid, @RequestBody GroupGroupForm form) {
        List<String> groups = form.getParentGroups();
        Group group = superGroup.getGroupRecursively(gid);
        if (group == null) {
            String warn = "Group does not exist";
            log.warn(warn);
            return new ResponseEntity<>(new MicroserviceUnsuccessfulMesage(warn), HttpStatus.BAD_REQUEST);
        }
        
        log.debug("Adding group " + group.getGid() + " to groups " + groups);
        if (groups == null || groups.isEmpty()) {
            log.info("Adding group " + gid + " to Super Group");
            superGroup.addGroup(group);
        }
        for (String GID : groups) {
            Group parentGroup = superGroup.getGroupRecursively(GID);
            if (parentGroup == null) {
                parentGroup = createDefaultGroup(GID);
            }
            log.info("Adding group " + gid + " to group " + GID);
            parentGroup.addGroup(group);
        }
        
        return buildSuccessResponseEntity("Group " + gid + " added to groups " + groups, HttpStatus.OK);
    }
    
    @PostMapping("/{gid}/removegroup")
    public ResponseEntity<MicroserviceMessage> removeGroup(@PathVariable String gid, @RequestBody GroupGroupForm form) {
        List<String> groups = form.getParentGroups();
        Group group = superGroup.getGroupRecursively(gid);
        if (group == null) {
            String warn = "Group does not exist";
            log.warn(warn);
            return new ResponseEntity<>(new MicroserviceUnsuccessfulMesage(warn), HttpStatus.BAD_REQUEST);
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
    
        return buildSuccessResponseEntity("Group " + gid + " removed from groups " + groups, HttpStatus.OK);
    }
    
    public Group createDefaultGroup(String GID) {
        log.info("Group " + GID + " does not exist. Creating new group " + GID + " with the same name");
        Group group = new Group(GID, GID);
        superGroup.addGroup(group);
        group.create();
        return group;
    }

    private ResponseEntity<MicroserviceMessage> buildSuccessResponseEntity(String msg, HttpStatus status) {
        log.info(msg);
        return new ResponseEntity<>(new MicroserviceSuccessfulMessage(msg), status);
    }
}
