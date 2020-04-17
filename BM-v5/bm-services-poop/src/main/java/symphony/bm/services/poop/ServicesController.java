package symphony.bm.services.poop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import symphony.bm.cache.devices.adaptors.AdaptorManager;
import symphony.bm.cache.devices.entities.deviceproperty.DataType;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;
import symphony.bm.cache.devices.entities.deviceproperty.DevicePropertyType;
import symphony.bm.cache.devices.exceptions.RequestProcessingException;
import symphony.bm.services.poop.jeep.POOPRequest;
import symphony.bm.services.poop.jeep.POOPSuccessResponse;

import java.io.IOException;

@RestController
public class ServicesController {
    private final Logger LOG = LoggerFactory.getLogger(ServicesController.class);
    private AdaptorManager adaptorManager;
    
    private String bmURL;
    private String devicesCachePort;
    
    public ServicesController(@Value("${http.url.bm}") String bmURL,
                              @Value("${microservices.cache.devices.port}") String devicesCachePort,
                              @Qualifier("bmsp.adaptorManager") AdaptorManager adaptorManager) {
        this.bmURL = bmURL;
        this.devicesCachePort = devicesCachePort;
        this.adaptorManager = adaptorManager;
    }
    
    @PatchMapping("/poop")
    public Object poop(@RequestBody POOPRequest request) throws RequestProcessingException {
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
        
        DevicePropertyType propType = property.getType();
        switch(propType.getData()) {
            case binary:
                try {
                    int value = Integer.parseInt(request.getPropValue());
                    if (value < 0 || value > 1) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    throw new RequestProcessingException("Binary property value must only either be 1 or 0");
                }
                break;
            case number:
                double min = propType.getMinValue().doubleValue();
                double max = propType.getMaxValue().doubleValue();
                try {
                    double value = Double.parseDouble(request.getPropValue());
                    if (value < min || value > max) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    throw new RequestProcessingException("Number property value must only be from " + min + " to "
                            + max);
                }
                break;
            case enumeration:
                String value = request.getPropValue();
                if (!propType.getValues().contains(value)) {
                    String values = String.join(",", propType.getValues());
                    throw new RequestProcessingException("Enumeration property value must only be [" + values + "]");
                }
        }
        
        try {
            property.setValue(request.getPropValue());
        } catch (Exception e) {
            throw new RequestProcessingException("Unable to set value to " + request.getCID() + "."
                    + request.getPropIndex(), e);
        }
        return new POOPSuccessResponse();
    }
    
    private DeviceProperty getDeviceProperty(String CID, String index) throws NullPointerException, IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(bmURL + ":" + devicesCachePort + "/devices/" + CID + "/properties/" + index);
        HttpResponse response = httpClient.execute(get);
        String json = EntityUtils.toString(response.getEntity());
        if (json.isEmpty()) {
            throw new NullPointerException("Empty response from devices cache");
        }
        return injectAdaptorsInDeviceProperty(new ObjectMapper().readValue(json, DeviceProperty.class));
    }
    
    private DeviceProperty injectAdaptorsInDeviceProperty(DeviceProperty property) {
        property.setAdaptorManager(adaptorManager);
        return property;
    }
}
