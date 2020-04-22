package symphony.bm.core.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import symphony.bm.core.iot.IotContext;
import symphony.bm.core.rest.hateoas.GroupModel;

@RestController
@RequestMapping("/groups")
@AllArgsConstructor
@Slf4j
public class GroupController {
    private final IotContext iotContext;

    @GetMapping("/")
    public GroupModel getSuperGroup() {
        return new GroupModel(iotContext.getSuperGroup());
    }

    @GetMapping("/{gid}")
    public GroupModel getGroup(@PathVariable String gid) {
        return new GroupModel(iotContext.getGroup(gid));
    }
}
