package symphony.bm.core.activitylisteners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import symphony.bm.core.iot.Attribute;
import symphony.bm.core.iot.Group;
import symphony.bm.core.iot.Thing;
import symphony.bm.generics.messages.MicroserviceMessage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

@Slf4j
public class CirMicroserviceListener implements ActivityListener {
    private final String bmURL;
    private final String bmCirPort;
    private final ObjectMapper objectMapper;

    public CirMicroserviceListener(ObjectMapper objectMapper, String bmURL, String bmCirPort) {
        this.bmURL = bmURL;
        this.bmCirPort = bmCirPort;
        this.objectMapper = objectMapper;
    }

    @Override
    public void thingCreated(Thing thing) {

    }

    @Override
    public void thingUpdated(Thing thing, Map<String, Object> updatedFields) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(bmURL + ":" + bmCirPort + "/things/" + thing.getUid());
        try {
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(thing)));
        } catch (UnsupportedEncodingException e) {
            log.error("UnsupportedEncodingException:", e);
            return;
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException:", e);
            return;
        }

        HttpResponse response;
        try {
            response = httpClient.execute(request);
            MicroserviceMessage msg = objectMapper.readValue(EntityUtils.toString(response.getEntity()),
                    MicroserviceMessage.class);
            log.debug(msg.getMessage());
        } catch (IOException e) {
            log.error("IOException:", e);
            return;
        }
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
    public void groupUpdated(Group group, Map<String, Object> updatedFields) {

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

    @Override
    public void attributeUpdated(Attribute attribute, Map<String, Object> updatedFields) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(bmURL + ":" + bmCirPort + "/attributes/" + attribute.getAid());
        try {
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(attribute), ContentType.APPLICATION_JSON));
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException:", e);
            return;
        }

        HttpResponse response;
        try {
            response = httpClient.execute(request);
            MicroserviceMessage msg = objectMapper.readValue(EntityUtils.toString(response.getEntity()),
                    MicroserviceMessage.class);
            log.debug(msg.getMessage());
        } catch (IOException e) {
            log.error("IOException:", e);
            return;
        }
    }

    @Override
    public void attributeUpdatedValue(Attribute attribute, Object value) {

    }

    @Override
    public void attributeAddedToThing(Attribute attribute, Thing thing) {

    }

    @Override
    public void attributeRemovedFromThing(Attribute attribute, Thing thing) {

    }
}
