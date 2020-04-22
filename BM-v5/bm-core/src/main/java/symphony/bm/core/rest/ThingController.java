package symphony.bm.core.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import symphony.bm.core.iot.IotContext;
import symphony.bm.core.rest.hateoas.AttributeModel;
import symphony.bm.core.rest.hateoas.ThingModel;

@RestController
@RequestMapping("/things")
@AllArgsConstructor
@Slf4j
public class ThingController {
    private final IotContext iotContext;

    @GetMapping("/{uid}")
    public ThingModel getThing(@PathVariable String uid) {
        return new ThingModel(iotContext.getThing(uid));
    }

    @GetMapping("/{uid}/{index}")
    public AttributeModel getAttribute(@PathVariable String uid, @PathVariable int index) {
        return new AttributeModel(iotContext.getThing(uid).getAttributes().get(index), uid, index);
    }
}
