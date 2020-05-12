package symphony.bm.data.iot;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;
import symphony.bm.core.iot.Thing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@Component
@Slf4j
public class ResourceContainer {
    private final MongoOperations mongo;
    private Map<String, Thing> things = new HashMap<>();
    
    public ResourceContainer(MongoOperations mongo) {
        this.mongo = mongo;
    }
    
    public void addThing(Thing thing) {
        things.put(thing.getUid(), thing);
    }
}
