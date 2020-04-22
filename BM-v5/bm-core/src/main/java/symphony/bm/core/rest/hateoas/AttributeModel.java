package symphony.bm.core.rest.hateoas;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.hateoas.RepresentationModel;
import symphony.bm.core.iot.attribute.Attribute;
import symphony.bm.core.iot.attribute.AttributeDataType;
import symphony.bm.core.iot.attribute.AttributeMode;
import symphony.bm.core.rest.ThingController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class AttributeModel extends RepresentationModel<AttributeModel> {
    public final String name;
    public final AttributeMode mode;
    public final AttributeDataType dataType;
    public final String value;

    public AttributeModel(Attribute attribute, String UID, int index) {
        this.name = attribute.getName();
        this.mode = attribute.getMode();
        this.dataType = attribute.getDataType();
        this.value = attribute.getValue();
        this.add(linkTo(methodOn(ThingController.class).getAttribute(UID, index)).withSelfRel());
        this.add(linkTo(methodOn(ThingController.class).getThing(UID))
                .withRel("parent"));
    }
}
