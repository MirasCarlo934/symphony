package symphony.bm.mqtt.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.*;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Thing;
import symphony.bm.generics.exceptions.RestControllerProcessingException;
import symphony.bm.generics.messages.MicroserviceMessage;
import symphony.bm.generics.messages.MicroserviceSuccessfulMessage;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class RestMicroserviceController {
    private final MessageChannel outbound;
    private final ObjectMapper objectMapper;
    
    public RestMicroserviceController(@Qualifier("mqttOutboundChannel") MessageChannel outbound,
                                      ObjectMapper objectMapper) {
        this.outbound = outbound;
        this.objectMapper = objectMapper;
    }
    
    @PostMapping("things/{uid}")
    public MicroserviceMessage thing(@PathVariable String uid, @RequestBody Thing thing)
            throws RestControllerProcessingException {
        Map<String, Object> headers = new HashMap<>();
        headers.put("mqtt_topic", "things/" + uid);
        headers.put("mqtt_retained", false);
        headers.put("mqtt_qos", 2);
        try {
            publish(new GenericMessage<>(objectMapper.writeValueAsString(thing), headers));
        } catch (JsonProcessingException e) {
            throw new RestControllerProcessingException("Thing " + uid + " cannot be published",
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
        return new MicroserviceSuccessfulMessage("Thing " + uid + " published");
    }
    
    @DeleteMapping("things/{uid}")
    public MicroserviceMessage deleteThing(@PathVariable String uid) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("mqtt_topic", "things/" + uid);
        headers.put("mqtt_retained", true);
        headers.put("mqtt_qos", 2);
        publish(new GenericMessage<>("", headers));
        return new MicroserviceSuccessfulMessage("Thing " + uid + " retained message deleted");
    }
    
    @PostMapping("things/{uid}/{field}")
    public MicroserviceMessage thingField(@PathVariable String uid, @PathVariable String field,
                                          @RequestBody String value) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("mqtt_topic", "things/" + uid + "/" + field);
//        headers.put("mqtt_retained", true);
        headers.put("mqtt_qos", 2);
        publish(new GenericMessage<>(value, headers));
        return new MicroserviceSuccessfulMessage("Thing " + uid + " " + field + " published");
    }
    
    @PostMapping("things/{uid}/attributes/{aid}")
    public MicroserviceMessage attribute(@PathVariable String uid, @PathVariable String aid,
                                          @RequestBody Attribute attribute) throws RestControllerProcessingException {
        Map<String, Object> headers = new HashMap<>();
        headers.put("mqtt_topic", "things/" + uid + "/attributes/" + aid);
//        headers.put("mqtt_retained", true);
        headers.put("mqtt_qos", 2);
        try {
            publish(new GenericMessage<>(objectMapper.writeValueAsString(attribute), headers));
            return new MicroserviceSuccessfulMessage("Thing " + uid + " published");
        } catch (JsonProcessingException e) {
            throw new RestControllerProcessingException("Attribute " + uid + "/" + aid + " cannot be published",
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
    
    @DeleteMapping("things/{uid}/attributes/{aid}")
    public MicroserviceMessage deleteAttribute(@PathVariable String uid, @PathVariable String aid) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("mqtt_topic", "things/" + uid + "/attributes/" + aid);
//        headers.put("mqtt_retained", true);
        headers.put("mqtt_qos", 2);
        publish(new GenericMessage<>("", headers));
        return new MicroserviceSuccessfulMessage("Attribute " + uid + "/" + aid + " retained message deleted");
    }
    
    @PostMapping("things/{uid}/attributes/{aid}/{field}")
    public MicroserviceMessage attributeField(@PathVariable String uid, @PathVariable String aid,
                                              @PathVariable String field, @RequestBody String value)
            throws RestControllerProcessingException {
        Map<String, Object> headers = new HashMap<>();
        headers.put("mqtt_topic", "things/" + uid + "/attributes/" + aid + "/" + field);
//        headers.put("mqtt_retained", true);
        headers.put("mqtt_qos", 2);
        publish(new GenericMessage<>(value, headers));
        return new MicroserviceSuccessfulMessage("Attribute " + uid + "/" + aid + " " + field + " published");
    }
    
    private void publish(Message<String> message) {
        log.debug("Publishing to topic " + message.getHeaders().get("mqtt_topic"));
        log.debug("Message: " + message.getPayload());
        outbound.send(message);
    }
}
