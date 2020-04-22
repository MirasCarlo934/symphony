package symphony.bm.core.rest;

import jdk.nashorn.internal.objects.annotations.Getter;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.IotContext;
import symphony.bm.core.iot.Thing;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@AllArgsConstructor
public class HateoasModelManager {
    private final IotContext iotContext;

    public EntityModel<Thing> getThingEntityModel(String UID) {
        Thing thing = iotContext.getThing(UID);
        if (thing != null) {
            Link self = linkTo(methodOn(RestApiController.class).getThing(UID)).withSelfRel();
            Link parent = linkTo(methodOn(RestApiController.class).getGroup(thing.getParentGID()))
                    .withRel(IanaLinkRelations.PREV);
            return new EntityModel<>(thing, self, parent);
        }
        return null;
    }

    public EntityModel<Group> getGroupEntityModel(String GID) {
        Group group = iotContext.getGroup(GID);
        if (group != null) {
            Link self = linkTo(methodOn(RestApiController.class).getGroup(GID)).withSelfRel();
            Link parent = linkTo(methodOn(RestApiController.class).getGroup(group.getParentGID()))
                    .withRel(IanaLinkRelations.PREV);
            return new EntityModel<>(group, self, parent);
        }
        return null;
    }
}
