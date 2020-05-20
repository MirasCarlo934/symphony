package symphony.bm.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import symphony.bm.core.iot.Thing;
import symphony.bm.core.rest.resources.Resource;

import java.util.*;

@Component
@Slf4j
public class MqttQueryController implements MessageHandler {
    private final String bmURL;
    private final String bmCorePort;
    private final MessageChannel outbound;

    public MqttQueryController(@Value("${bm.url}") String bmURL, @Value("${bm.port.core}") String bmCorePort,
                                @Qualifier("mqttOutboundChannel") MessageChannel outbound) {
        this.bmURL = bmURL;
        this.bmCorePort = bmCorePort;
        this.outbound = outbound;
    }

    @SneakyThrows
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
        String payload = (String) message.getPayload();
        RestTemplate restTemplate = new RestTemplate();

        log.debug("Message received from topic " + topic);
        log.debug("Message: " + payload);

        List<String> topicLevels = new ArrayList<>(Arrays.asList(topic.split("/")));
        topicLevels.remove(0);

        if (topicLevels.size() == 1 && topicLevels.get(0).equals("resource")) {
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(
                        bmURL + ":" + bmCorePort + "/" + payload + "?restful=false",
                        String.class
                );

                //assuming response is 2xx successful
                String resource = response.getBody();
                Map<String, Object> headers = new HashMap<>();
                headers.put("mqtt_topic", payload);
                headers.put("mqtt_retained", false);
                headers.put("mqtt_qos", 2);
                outbound.send(new GenericMessage<>(resource, headers));
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    Map<String, Object> headers = new HashMap<>();
                    headers.put("mqtt_topic", payload);
                    headers.put("mqtt_retained", false);
                    headers.put("mqtt_qos", 2);
                    outbound.send(new GenericMessage<>("", headers));
                }
                // throw other exceptions to be handled by error controller
            }
        }
    }
}
