package symphony.bm.core.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import symphony.bm.core.iot.SuperGroup;
import symphony.bm.core.rest.hateoas.GroupModel;

@RestController
@CrossOrigin
@AllArgsConstructor
@Slf4j
public class BaseController {
    private final SuperGroup superGroup;

    @GetMapping
    public Object getSuperGroup(@RequestParam(value = "restful", required = false, defaultValue = "true") Boolean restful) {
        if (restful) {
            return new GroupModel(superGroup);
        } else {
            return superGroup;
        }
    }
}
