package symphony.bm.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Thing;
import symphony.bm.generics.messages.MicroserviceUnsuccessfulMessage;
import symphony.bm.mqtt.iot.MinifiedAttribute;
import symphony.bm.mqtt.iot.MinifiedThing;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class MqttController implements MessageHandler {
    private String bmURL;
    private String bmCorePort;
    private MessageChannel outbound;
    private ObjectMapper objectMapper;

    public MqttController(@Value("${bm.url}") String bmURL, @Value("${bm.port.core}") String bmCorePort,
                          @Qualifier("mqttOutboundChannel") MessageChannel outbound, ObjectMapper objectMapper) {
        this.bmURL = bmURL;
        this.bmCorePort = bmCorePort;
        this.outbound = outbound;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
        String payload = (String) message.getPayload();
        StringBuilder thingUrlBuilder = new StringBuilder("things");
        HttpClient httpClient = HttpClientBuilder.create().build();

        log.debug("Message received from topic " + topic);
        log.debug("Message: " + payload);

        List<String> topicLevels = new ArrayList<>(Arrays.asList(topic.split("/")));
        topicLevels.remove(0);
        topicLevels.forEach( level -> thingUrlBuilder.append("/").append(level));
        
        if (topicLevels.contains("attributes")) {
            try {
                MinifiedAttribute minAttr = objectMapper.readValue(payload, MinifiedAttribute.class);
                payload = objectMapper.writeValueAsString(minAttr.unminify());
            } catch (JsonProcessingException e) {
                try {
                    objectMapper.readValue(payload, Attribute.class);
                } catch (JsonProcessingException e1) {
                    String msg = "Invalid Attribute data sent";
                    throw new MessagingException(msg, e1);
                }
            }
        } else {
            try {
                MinifiedThing minThing = objectMapper.readValue(payload, MinifiedThing.class);
                payload = objectMapper.writeValueAsString(minThing.unminify());
            } catch (JsonProcessingException e) {
                try {
                    objectMapper.readValue(payload, Thing.class);
                } catch (JsonProcessingException e1) {
                    String msg = "Invalid Thing data sent";
                    throw new MessagingException(msg, e1);
                }
            }
        }

        String resourceUrl = bmURL + ":" + bmCorePort + "/" + thingUrlBuilder.toString();
        HttpPut request = new HttpPut(resourceUrl);
        request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
        HttpResponse response;
        try {
            log.debug("Requesting resource " + resourceUrl + "...");
            response = httpClient.execute(request);
            JsonNode jsonRsp = new ObjectMapper().readTree(EntityUtils.toString(response.getEntity()));
            log.debug("Response from " + resourceUrl + ": " + jsonRsp.toString());
//            Map<String, Object> headers = new HashMap<>();
//            headers.put("mqtt_topic", thingUrlBuilder.toString());
//            publish(new GenericMessage<>(jsonRsp.toString(), headers));
        } catch (IOException e) {
            String msg = "Resource not currently available";
            throw new MessagingException(msg, e);
        }
    }

//    private void publish(Message<String> message) {
//        log.debug("Publishing to topic " + message.getHeaders().get("mqtt_topic"));
//        log.debug("Message: " + message.getPayload());
//        outbound.send(message);
//    }

}
