package symphony.bm.core.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import symphony.bm.core.iot.SuperGroup;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.rest.hateoas.AttributeModel;
import symphony.bm.core.rest.hateoas.ThingModel;

@RestController
@RequestMapping("/things")
@AllArgsConstructor
@Slf4j
public class ThingController {
    private final SuperGroup superGroup;

    @GetMapping("/{uid}")
    public ThingModel getThing(@PathVariable String uid) {
        return new ThingModel(superGroup.getThing(uid));
    }

    @GetMapping("/{uid}/{index}")
    public AttributeModel getAttribute(@PathVariable String uid, @PathVariable int index) {
        return new AttributeModel(superGroup.getThing(uid).getAttributes().get(index), uid, index);
    }

//    @PutMapping("/{uid}")
//    public ResponseEntity<String> updateThing(@PathVariable String uid, @RequestBody Thing thing) {
//        if (!uid.equals(thing.getUID())) {
//            return new ResponseEntity<>("UID specified in path(" + uid + ") is not the same with UID of thing ("
//                    + thing.getUID() + ") in request body", HttpStatus.CONFLICT);
//        }
//        try {
//            superGroup.replaceThing(thing);
//        } catch (NullPointerException e) {
//            return new ResponseEntity<>("Thing not found", HttpStatus.NOT_FOUND);
//        }
//        return new ResponseEntity<>("Thing updated", HttpStatus.OK);
//    }
}
