package symphony.bm.core.rest.hateoas;

import org.springframework.hateoas.RepresentationModel;
import symphony.bm.core.iot.Group;
import symphony.bm.core.rest.RestApiController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class BasicGroupModel extends RepresentationModel<BasicGroupModel> {
    public final String GID;
    public final String parentGID;
    public final String name;

    public BasicGroupModel(Group group) {
        this.GID = group.getGID();
        this.parentGID = group.getParentGID();
        this.name = group.getName();
        if (!GID.equals("")) {
            this.add(linkTo(methodOn(RestApiController.class).getGroup(GID)).withSelfRel());
            this.add(linkTo(methodOn(RestApiController.class).getGroup(parentGID)).withRel("parent"));
        } else {
            this.add(linkTo(methodOn(RestApiController.class).getSuperGroup()).withSelfRel());
        }
    }
}
