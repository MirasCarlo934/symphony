package symphony.bm.core.rest.hateoas;

import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.hateoas.RepresentationModel;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.rest.GroupController;
import symphony.bm.core.rest.ThingController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class BasicThingModel extends RepresentationModel<BasicThingModel> {
    @Getter public final String uid;
    @Getter public final String name;
    @Getter public final boolean active;

    @SneakyThrows
    public BasicThingModel(Thing thing) {
        this.uid = thing.getUid();
        this.name = thing.getName();
        this.active = thing.isActive();
    
        this.add(linkTo(methodOn(ThingController.class).get(uid)).withSelfRel());
    }
}
