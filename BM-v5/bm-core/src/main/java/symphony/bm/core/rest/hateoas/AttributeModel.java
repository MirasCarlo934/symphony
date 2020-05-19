package symphony.bm.core.rest.hateoas;

import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.hateoas.RepresentationModel;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.attribute.AttributeDataType;
import symphony.bm.core.iot.attribute.AttributeMode;
import symphony.bm.core.rest.AttributeController;
import symphony.bm.core.rest.ThingController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

public class AttributeModel extends RepresentationModel<AttributeModel> {
    @Getter public final String aid;
    @Getter public final String name;
    @Getter public final String thing;
    @Getter public final AttributeMode mode;
    @Getter public final AttributeDataType dataType;
    @Getter public final Object value;

    @SneakyThrows
    public AttributeModel(Attribute attribute, boolean expanded) {
        this.aid = attribute.getAid();
        this.name = attribute.getName();
        this.thing = attribute.getThing();
        this.mode = attribute.getMode();
        this.dataType = attribute.getDataType();
        this.value = attribute.getValue();
        if (expanded) {
            this.add(linkTo(methodOn(AttributeController.class).get(attribute.getThing(), aid)).withSelfRel()
                    .andAffordance(afford(methodOn(AttributeController.class).add(attribute.getThing(), aid, null)))
                    .andAffordance(afford(methodOn(AttributeController.class).update(attribute.getThing(), aid, null)))
                    .andAffordance(afford(methodOn(AttributeController.class).delete(attribute.getThing(), aid)))
            );
        } else {
            this.add(linkTo(methodOn(AttributeController.class).get(attribute.getThing(), aid)).withSelfRel());
        }
        this.add(linkTo(methodOn(ThingController.class).get(attribute.getThing(), true))
                .withRel("parent"));
    }
}
