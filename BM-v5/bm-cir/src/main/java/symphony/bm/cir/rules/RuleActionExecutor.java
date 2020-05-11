package symphony.bm.cir.rules;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import symphony.bm.core.activitylisteners.ActivityListener;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.Thing;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Slf4j
public class RuleActionExecutor implements ActivityListener {
    private final MessageChannel outbound;
    private final ObjectMapper objectMapper;

    @Override
    public void thingCreated(Thing thing) {

    }

    @Override
    public void thingUpdated(Thing thing, String fieldName, Object fieldValue) {

    }

    @Override
    public void thingAddedToGroup(Thing thing, Group group) {

    }

    @Override
    public void thingRemovedFromGroup(Thing thing, Group group) {

    }

    @Override
    public void thingDeleted(Thing thing) {

    }

    @Override
    public void groupCreated(Group group) {

    }

    @Override
    public void groupUpdated(Group group, String fieldName, Object fieldValue) {

    }

    @Override
    public void groupAddedToGroup(Group group, Group parent) {

    }

    @Override
    public void groupRemovedFromGroup(Group group, Group parent) {

    }

    @Override
    public void groupDeleted(Group group) {

    }

    @SneakyThrows
    @Override
    public void attributeUpdated(Attribute attribute, String fieldName, Object fieldValue) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("mqtt_topic", "BM/" + attribute.getThing() + "/attributes/" + attribute.getAid() + "/" + fieldName);
        Message<String> message = new GenericMessage<>(fieldValue.toString(), headers);
        outbound.send(message);
        log.debug("Attribute " + attribute.getThing() + "/" + attribute.getAid() + " state sent to core");
    }

    @Override
    public void attributeAddedToThing(Attribute attribute, Thing thing) {

    }

    @Override
    public void attributeRemovedFromThing(Attribute attribute, Thing thing) {

    }

}
