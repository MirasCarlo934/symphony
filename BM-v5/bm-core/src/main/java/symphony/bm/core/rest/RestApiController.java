package symphony.bm.core.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.IotContext;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.rest.hateoas.AttributeModel;
import symphony.bm.core.rest.hateoas.GroupModel;
import symphony.bm.core.rest.hateoas.ThingModel;

import javax.servlet.http.HttpServletRequest;

@RestController
@AllArgsConstructor
@Slf4j
public class RestApiController {
    private final IotContext iotContext;

    @GetMapping("/things/{uid}")
    public ThingModel getThing(@PathVariable String uid) {
        return new ThingModel(iotContext.getThing(uid));
    }

    @GetMapping("/things/{uid}/{index}")
    public AttributeModel getAttribute(@PathVariable String uid, @PathVariable int index) {
        return new AttributeModel(iotContext.getThing(uid).getAttributes().get(index), uid, index);
    }

    @GetMapping("/groups")
    public GroupModel getSuperGroup() {
        return new GroupModel(iotContext.getSuperGroup());
    }

    @GetMapping("/groups/{gid}")
    public GroupModel getGroup(@PathVariable String gid) {
        return new GroupModel(iotContext.getGroup(gid));
    }
}
