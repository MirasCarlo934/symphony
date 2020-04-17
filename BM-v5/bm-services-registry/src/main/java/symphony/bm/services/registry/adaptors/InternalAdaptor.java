package symphony.bm.services.registry.adaptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symphony.bm.cache.devices.adaptors.Adaptor;
import symphony.bm.cache.devices.entities.Device;
import symphony.bm.cache.devices.entities.Room;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;
import symphony.bm.services.registry.exceptions.RequestProcessingException;

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

public class InternalAdaptor implements Adaptor {
    private final Logger LOG = LoggerFactory.getLogger(InternalAdaptor.class);
    private Queue<Device> devicesToUpdate = new LinkedBlockingQueue<>();
    
    private String bmURL;
    private String devicesCachePort;
    
    public InternalAdaptor(String bmURL, String devicesCachePort) {
        this.bmURL = bmURL;
        this.devicesCachePort = devicesCachePort;
    }
    
    @Override
    public void deviceCreated(Device device) throws Exception {
        LOG.info("Posting new device in devices cache...");
        ObjectMapper objectMapper = new ObjectMapper();
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost postDevice = new HttpPost(bmURL + ":" + devicesCachePort + "/rooms/"
                + device.getRID() + "/devices/" + device.getCID());
        postDevice.setEntity(new StringEntity(objectMapper.writeValueAsString(device), ContentType.APPLICATION_JSON));
        HttpResponse response = httpClient.execute(postDevice);
        JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
        LOG.debug("Response: " + json.toString());
        if (json.isEmpty()) {
            throw new RequestProcessingException("Unable to register device " + device.getCID() + " in devices cache");
        }
        LOG.info("New device posted...");
    }
    
    @Override
    public void deviceDeleted(Device device) throws Exception {
        LOG.info("Deleting device in devices cache...");
        ObjectMapper objectMapper = new ObjectMapper();
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpDelete deleteDevice = new HttpDelete(bmURL + ":" + devicesCachePort + "/devices/" + device.getCID());
        HttpResponse response = httpClient.execute(deleteDevice);
        JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
        LOG.debug("Response: " + json.toString());
        if (json.isEmpty()) {
            throw new RequestProcessingException("Unable to delete device " + device.getCID() + " in devices cache");
        }
        LOG.info("Device deleted");
    }
    
    @Override
    public void deviceUpdatedDetails(Device device) throws Exception {
        LOG.info("Patching device in devices cache...");
        ObjectMapper objectMapper = new ObjectMapper();
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPatch patchDevice = new HttpPatch(bmURL + ":" + devicesCachePort + "/devices/" + device.getCID());
        patchDevice.setEntity(new StringEntity(objectMapper.writeValueAsString(device), ContentType.APPLICATION_JSON));
        HttpResponse response = httpClient.execute(patchDevice);
        JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
        LOG.debug("Response: " + json.toString());
        if (json.isEmpty()) {
            throw new RequestProcessingException("Unable to patch device " + device.getCID() + " in devices cache");
        }
        LOG.info("Device patched");
    }

    @Override
    public void deviceTransferredRoom(Device device, Room from, Room to) throws Exception {
        deviceUpdatedDetails(device);
    }

    @Override
    public void roomCreated(Room room) throws Exception {
        LOG.info("Posting new room in devices cache...");
        ObjectMapper objectMapper = new ObjectMapper();
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost postRoom = new HttpPost(bmURL + ":" + devicesCachePort + "/rooms/" + room.getRID());
        postRoom.setEntity(new StringEntity(objectMapper.writeValueAsString(room), ContentType.APPLICATION_JSON));
        HttpResponse response = httpClient.execute(postRoom);
        JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
        LOG.debug("Response: " + json.toString());
        if (json.isEmpty()) {
            throw new RequestProcessingException("Unable to create room " + room.getRID() + " in devices cache");
        }
        LOG.info("New room posted...");
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
    public void devicePropertyUpdatedValue(DeviceProperty property) {
    
    }
}
