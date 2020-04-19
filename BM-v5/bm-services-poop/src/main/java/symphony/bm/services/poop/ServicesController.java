package symphony.bm.services.poop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import symphony.bm.cache.devices.adaptors.AdaptorManager;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;
import symphony.bm.cache.devices.entities.deviceproperty.DevicePropertyType;
import symphony.bm.cache.rules.vo.Rule;
import symphony.bm.generics.exceptions.RequestProcessingException;
import symphony.bm.generics.jeep.JeepMessage;
import symphony.bm.generics.jeep.request.JeepRequest;
import symphony.bm.services.poop.jeep.POOPRequest;
import symphony.bm.services.poop.jeep.POOPSuccessResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

@RestController
public class ServicesController {
    private final Logger LOG = LoggerFactory.getLogger(ServicesController.class);
    private AdaptorManager adaptorManager;
    
    private String bmURL;
    private String devicesCachePort;
    private String rulesCachePort;
    private String poopMSN;
    
    public ServicesController(@Value("${http.url.bm}") String bmURL,
                              @Value("${microservices.cache.devices.port}") String devicesCachePort,
                              @Value("${microservices.cache.rules.port}") String rulesCachePort,
                              @Value("${services.poop.msn}") String poopMSN,
                              @Qualifier("bmsp.adaptorManager") AdaptorManager adaptorManager) {
        this.bmURL = bmURL;
        this.devicesCachePort = devicesCachePort;
        this.rulesCachePort = rulesCachePort;
        this.poopMSN = poopMSN;
        this.adaptorManager = adaptorManager;
    }
    
    @PatchMapping("/poop")
    public Object poop(@RequestBody POOPRequest request) throws RequestProcessingException {
        List<JeepMessage> messages = new Vector<>();
        DeviceProperty property;
        LOG.info("POOP requested by device " + request.getCID() + " property "
                + request.getPropIndex());
        try {
            property = getDeviceProperty(request.getCID(), request.getPropIndex());
        } catch (NullPointerException e) {
            throw new RequestProcessingException(request.getCID() + "." + request.getPropIndex() + " does not exist");
        } catch (IOException e) {
            throw new RequestProcessingException("Error in processing request", e);
        }
        
        // check if value supplied is valid
        DevicePropertyType propType = property.getType();
        if (!propType.checkIfValueIsValid(request.getPropValue())) {
            throw new RequestProcessingException("'" + request.getPropValue() + "' is invalid for the specified " +
                    "property " + property.getID());
        }
        
        // set the property value in registry
        try {
            property.setValue(request.getPropValue());
            messages.add(new POOPSuccessResponse(request.getMRN()));
        } catch (Exception e) {
            throw new RequestProcessingException("Unable to set value of + " + property.getID() + " to "
                    + request.getPropValue(), e);
        }
        
        // check rules
        List<Rule> rulesTriggerable;
        try {
            rulesTriggerable = getRulesTriggerable(property);
            for (Rule rule : rulesTriggerable) {
                LOG.info("Checking if rule " + rule.getRuleID() + " (" + rule.getRuleName() + ") is triggered...");
                List<DeviceProperty> triggerProperties = getDeviceProperties(rule.getTriggerProperties());
                if (rule.isTriggered(triggerProperties)) {
                    LOG.info("Rule "+ rule.getRuleID() + " (" + rule.getRuleName() + ") triggered!");
                    List<DeviceProperty> actionProperties = getDeviceProperties(rule.getActionProperties());
                    for (DeviceProperty action : actionProperties) {
                        try {
                            String actionValue = rule.getPropertyActionValue(action.getCID(), action.getIndex());
                            action.setValue(actionValue);
                            messages.add(new POOPRequest(generateRandomMRN(), poopMSN, action.getCID(),
                                    action.getIndex(), action.getValue()));
                            LOG.info(action.getID() + " set value to " + actionValue + " (Rule "
                                    + rule.getRuleID() + ")");
                        } catch (Exception e) {
                            throw new RequestProcessingException("Unable to set value to " + action.getID()
                                    + " from rule " + rule.getRuleID());
                        }
                    }
                } else {
                    LOG.info("Rule "+ rule.getRuleID() + " (" + rule.getRuleName() + ") not triggered");
                }
            }
        } catch (IOException e) {
            throw new RequestProcessingException("Unable to process rules", e);
        }
        return messages;
    }
    
    private List<Rule> getRulesTriggerable(DeviceProperty property) throws IOException {
        String cid = property.getCID();
        ObjectMapper mapper = new ObjectMapper();
        int prop_index = property.getIndex();
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(bmURL + ":" + rulesCachePort + "/" + cid + "/" + prop_index);
        
        HttpResponse response = httpClient.execute(get);
        String responseStr = EntityUtils.toString(response.getEntity());
        Rule[] rules = mapper.readValue(responseStr, Rule[].class);
        return Arrays.asList(rules);
    }
    
    private DeviceProperty getDeviceProperty(String CID, int index) throws NullPointerException, IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(bmURL + ":" + devicesCachePort + "/devices/" + CID + "/properties/" + index);
        HttpResponse response = httpClient.execute(get);
        String json = EntityUtils.toString(response.getEntity());
        if (json.isEmpty()) {
            throw new NullPointerException("Empty response from devices cache");
        }
        return injectAdaptorsInDeviceProperty(new ObjectMapper().readValue(json, DeviceProperty.class));
    }
    
    private List<DeviceProperty> getDeviceProperties(HashMap<String, List<Integer>> propertiesToGet) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        ObjectMapper mapper = new ObjectMapper();
        HttpPost post = new HttpPost(bmURL + ":" + devicesCachePort + "/query/propertylist");
        post.setEntity(new StringEntity(mapper.writeValueAsString(propertiesToGet), ContentType.APPLICATION_JSON));
        
        HttpResponse response = httpClient.execute(post);
        String responseStr = EntityUtils.toString(response.getEntity());
        DeviceProperty[] properties = mapper.readValue(responseStr, DeviceProperty[].class);
        for (DeviceProperty property : properties) {
            injectAdaptorsInDeviceProperty(property);
        }
        return Arrays.asList(properties);
    }
    
    private DeviceProperty injectAdaptorsInDeviceProperty(DeviceProperty property) {
        property.setAdaptorManager(adaptorManager);
        return property;
    }
    
    private String generateRandomMRN() {
        return RandomStringUtils.random(8, false, true);
    }
}
