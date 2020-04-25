package symphony.bm.core.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import symphony.bm.core.iot.SuperGroup;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.iot.attribute.Attribute;
import symphony.bm.core.rest.forms.attribute.AttributeUpdateForm;
import symphony.bm.core.rest.hateoas.AttributeModel;
import symphony.bm.generics.exceptions.RestControllerProcessingException;
import symphony.bm.generics.messages.MicroserviceMessage;
import symphony.bm.generics.messages.MicroserviceSuccessfulMessage;
import symphony.bm.generics.messages.MicroserviceUnsuccessfulMesage;

import java.util.List;
import java.util.Vector;

@RestController
@RequestMapping("/things/{uid}/attributes")
@AllArgsConstructor
@Slf4j
public class AttributeController {
    private final SuperGroup superGroup;

    @GetMapping("/")
    public List<AttributeModel> getAll(@PathVariable String uid) throws RestControllerProcessingException {
        Thing thing = superGroup.getThingRecursively(uid);
        if (thing != null) {
            List<Attribute> attributes = thing.getCopyOfAttributeList();
            List<AttributeModel> attributeModels = new Vector<>();
            attributes.forEach( attribute -> attributeModels.add(new AttributeModel(attribute, uid)));
            return attributeModels;
        } else {
            throw new RestControllerProcessingException("Thing " + uid + " does not exist", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{aid}")
    public AttributeModel get(@PathVariable String uid, @PathVariable String aid)
            throws RestControllerProcessingException {
        Thing thing = superGroup.getThingRecursively(uid);
        if (thing != null) {
            Attribute attribute = thing.getAttribute(aid);
            if (attribute != null) {
                return new AttributeModel(attribute, uid);
            } else {
                throw new RestControllerProcessingException("Attribute " + thing.getUid() + "/" + aid
                        + " does not exist", HttpStatus.NOT_FOUND);
            }
        } else {
            throw new RestControllerProcessingException("Thing " + uid + " does not exist", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{aid}")
    public ResponseEntity<MicroserviceMessage> delete(@PathVariable String uid, @PathVariable String aid)
            throws RestControllerProcessingException {
        Thing thing = superGroup.getThingRecursively(uid);
        if (thing != null) {
            Attribute attribute = thing.getAttribute(aid);
            if (attribute != null) {
                thing.deleteAttribute(aid);
                attribute.delete();
                return buildSuccessResponseEntity("Attribute " + thing.getUid() + "/" + aid + " deleted",
                        HttpStatus.OK);
            } else {
                throw new RestControllerProcessingException("Attribute " + thing.getUid() + "/" + aid
                        + " does not exist", HttpStatus.NOT_FOUND);
            }
        } else {
            throw new RestControllerProcessingException("Thing " + uid + "does not exist", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{aid}")
    public ResponseEntity<MicroserviceMessage> add(@PathVariable String uid, @PathVariable String aid,
                                                   @RequestBody Attribute attribute)
            throws RestControllerProcessingException {
        if (!uid.equals(attribute.getThing())) {
            throw new RestControllerProcessingException("UID specified in path (" + uid + ") is not the same with " +
                    "specified UID of attribute (" + attribute.getThing() + ") in request body", HttpStatus.CONFLICT);
        }
        if (!aid.equals(attribute.getAid())) {
            throw new RestControllerProcessingException("AID specified in path (" + aid + ") is not the same with " +
                    "AID of attribute (" + attribute.getAid() + ") in request body", HttpStatus.CONFLICT);
        }

        Thing thing = superGroup.getThingRecursively(uid);
        if (thing != null) {
            Attribute a = thing.getAttribute(aid);
            if (a == null) {
                log.info("Adding attribute " + attribute.getAid() + " to thing " + uid);
                thing.addAttribute(attribute);
                return buildSuccessResponseEntity("Attribute " + thing.getUid() + "/" + aid + " added",
                        HttpStatus.OK);
            } else {
                String warn = "Attribute " + thing.getUid() + "/" + aid + " already exists. Attribute will not be " +
                        "added to context.";
                log.warn(warn);
                return new ResponseEntity<>(new MicroserviceUnsuccessfulMesage(warn), HttpStatus.CONFLICT);
            }
        } else {
            throw new RestControllerProcessingException("Thing " + uid + "does not exist", HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{aid}")
    public ResponseEntity<MicroserviceMessage> update(@PathVariable String uid, @PathVariable String aid,
                                                      @RequestBody AttributeUpdateForm form)
            throws RestControllerProcessingException {
        Thing thing = superGroup.getThingRecursively(uid);
        if (thing != null) {
            Attribute attribute = thing.getAttribute(aid);
            if (attribute != null) {
                log.debug("Updating attribute " + uid + '/' + aid + "...");
                attribute.update(form);
                return buildSuccessResponseEntity("Attribute " + thing.getUid() + "/" + aid + " updated",
                        HttpStatus.OK);
            } else {
                throw new RestControllerProcessingException("Attribute " + thing.getUid() + "/" + aid
                        + " does not exist", HttpStatus.NOT_FOUND);
            }
        } else {
            throw new RestControllerProcessingException("Thing " + uid + "does not exist", HttpStatus.NOT_FOUND);
        }
    }

    private ResponseEntity<MicroserviceMessage> buildSuccessResponseEntity(String msg, HttpStatus status) {
        log.info(msg);
        return new ResponseEntity<>(new MicroserviceSuccessfulMessage(msg), status);
    }
}
