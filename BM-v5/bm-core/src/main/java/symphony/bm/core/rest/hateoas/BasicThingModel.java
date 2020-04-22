package symphony.bm.core.rest.hateoas;

import org.springframework.hateoas.RepresentationModel;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.rest.RestApiController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class BasicThingModel extends RepresentationModel<BasicThingModel> {
    public final String UID;
    public final String parentGID;
    public final String name;

    public BasicThingModel(Thing thing) {
        this.UID = thing.getUID();
        this.parentGID = thing.getParentGID();
        this.name = thing.getName();
        this.add(linkTo(methodOn(RestApiController.class).getThing(UID)).withSelfRel());
        this.add(linkTo(methodOn(RestApiController.class).getGroup(thing.getParentGID()))
                .withRel("parent"));
    }
}
