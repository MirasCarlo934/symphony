package symphony.bm.core.rest.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.RepresentationModel;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.iot.attribute.Attribute;
import symphony.bm.core.rest.GroupController;
import symphony.bm.core.rest.ThingController;

import java.util.List;
import java.util.Vector;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ThingModel extends RepresentationModel<ThingModel> {
    public final String UID;
    public final String parentGID;
    public final String name;
    public final List<AttributeModel> attributes = new Vector<>();

    public ThingModel(Thing thing) {
        this.UID = thing.getUID();
        this.parentGID = thing.getParentGID();
        this.name = thing.getName();
        for (int i = 0; i < thing.getAttributes().size(); i++) {
            AttributeModel attributeModel = new AttributeModel(thing.getAttributes().get(i), UID, i);
            attributes.add(attributeModel);
        }
        this.add(linkTo(methodOn(ThingController.class).getThing(UID)).withSelfRel());
        this.add(linkTo(methodOn(GroupController.class).getGroup(thing.getParentGID()))
                .withRel("parent"));
    }
}
