package symphony.bm.core.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import symphony.bm.core.iot.SuperGroup;
import symphony.bm.core.rest.hateoas.GroupModel;

@RestController
@RequestMapping("/groups")
@AllArgsConstructor
@Slf4j
public class GroupController {
    private final SuperGroup superGroup;

    @GetMapping
    public GroupModel getSuperGroup() {
        return new GroupModel(superGroup);
    }

    @GetMapping("/{gid}")
    public GroupModel get(@PathVariable String gid) {
        return new GroupModel(superGroup.getGroupRecursively(gid));
    }
}
