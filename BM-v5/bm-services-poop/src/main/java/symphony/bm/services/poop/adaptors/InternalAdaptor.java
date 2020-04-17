package symphony.bm.services.poop.adaptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symphony.bm.cache.devices.adaptors.Adaptor;
import symphony.bm.cache.devices.entities.Device;
import symphony.bm.cache.devices.entities.Room;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;

@RequiredArgsConstructor
public class InternalAdaptor implements Adaptor {
    private final Logger LOG = LoggerFactory.getLogger(InternalAdaptor.class);
    
    private final String bmURL;
    private final String devicesCachePort;
    
    @Override
    public void deviceCreated(Device device) throws Exception {
    
    }
    
    @Override
    public void deviceDeleted(Device device) throws Exception {
    
    }
    
    @Override
    public void deviceUpdatedDetails(Device device) throws Exception {
    
    }
    
    @Override
    public void deviceTransferredRoom(Device device, Room from, Room to) throws Exception {
    
    }
    
    @Override
    public void roomCreated(Room room) throws Exception {
    
    }
    
    @Override
    public void roomDeleted(Room room) throws Exception {
    
    }
    
    @Override
    public void roomUpdatedDetails(Room room) throws Exception {
    
    }
    
    @Override
    public void roomTransferredRoom(Room room, Room from, Room to) throws Exception {
    
    }
    
    @Override
    public void devicePropertyUpdatedDetails(DeviceProperty property) throws Exception {
    
    }
    
    @Override
    public void devicePropertyUpdatedValue(DeviceProperty property) throws Exception {
        LOG.info("Updating " + property.getID() + " in devices cache...");
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPatch patch = new HttpPatch(bmURL + ":" + devicesCachePort + "/devices/" + property.getCID()
                + "/properties/" + property.getIndex());
        patch.setEntity(new StringEntity(new ObjectMapper().writeValueAsString(property),
                ContentType.APPLICATION_JSON));
        HttpResponse response = httpClient.execute(patch);
        LOG.info("Property updated");
    }
}
