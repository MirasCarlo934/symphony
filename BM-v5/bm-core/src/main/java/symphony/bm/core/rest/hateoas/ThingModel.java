package symphony.bm.core.rest.hateoas;

import org.springframework.hateoas.RepresentationModel;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.rest.GroupController;
import symphony.bm.core.rest.ThingController;

import java.util.List;
import java.util.Vector;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ThingModel extends RepresentationModel<ThingModel> {
    public final String UID;
    public final List<String> parentGroups;
    public final String name;
    public final List<AttributeModel> attributes = new Vector<>();

    public ThingModel(Thing thing) {
        this.UID = thing.getUID();
        this.parentGroups = thing.getParentGroups();
        this.name = thing.getName();
        for (int i = 0; i < thing.getAttributes().size(); i++) {
            AttributeModel attributeModel = new AttributeModel(thing.getAttributes().get(i), UID, i);
            attributes.add(attributeModel);
        }
        
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
