package symphony.bm.core.rest.hateoas;

import org.springframework.hateoas.RepresentationModel;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.rest.GroupController;
import symphony.bm.core.rest.ThingController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class BasicThingModel extends RepresentationModel<BasicThingModel> {
    public final String UID;
    public final List<String> parentGroups;
    public final String name;

    public BasicThingModel(Thing thing) {
        this.UID = thing.getUID();
        this.parentGroups = thing.getParentGroups();
        this.name = thing.getName();
    
        this.add(linkTo(methodOn(ThingController.class).getThing(UID)).withSelfRel());
        if (thing.getParentGroups().isEmpty()) {
            this.add(linkTo(methodOn(GroupController.class).getSuperGroup()).withRel("parent"));
        } else {
            for (String parentGID : thing.getParentGroups()) {
                if (parentGID == null || parentGID.equals("")) {
                    this.add(linkTo(methodOn(GroupController.class).getSuperGroup()).withRel("parent"));
                } else {
                    this.add(linkTo(methodOn(GroupController.class).getGroup(parentGID))
                            .withRel("parent." + parentGID));
                }
            }
        }
    }
}
