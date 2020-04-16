package symphony.bm.services.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;
import symphony.bm.cache.devices.adaptors.Adaptor;
import symphony.bm.cache.devices.adaptors.AdaptorManager;
import symphony.bm.cache.devices.entities.Device;
import symphony.bm.cache.devices.entities.Room;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;
import symphony.bm.cache.devices.entities.deviceproperty.DevicePropertyMode;
import symphony.bm.cache.devices.entities.deviceproperty.DevicePropertyType;
import symphony.bm.services.registry.exceptions.RequestProcessingException;
import symphony.bm.services.registry.jeep.request.UnregisterRequest;
import symphony.bm.services.registry.jeep.response.JeepResponse;
import symphony.bm.services.registry.jeep.request.RegisterRequest;
import symphony.bm.services.registry.jeep.response.JeepSuccessResponse;
import symphony.bm.services.registry.models.Product;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@RestController
public class ServicesController {
    private final Logger LOG = LoggerFactory.getLogger(ServicesController.class);
    private MongoOperations mongo;
    private HttpClient httpClient = HttpClientBuilder.create().build();
    private AdaptorManager adaptorManager;
    
    private String bmURL;
    private String devicesCachePort;
    private String getRoomsPath;

    public ServicesController(@Value("${http.url.bm}") String bmURL,
                              @Value("${microservices.cache.devices.port}") String devicesCachePort,
                              @Value("${microservices.cache.devices.path.rooms}") String getRoomsPath,
                              MongoTemplate mongoTemplate,
                              @Qualifier("bmsr.adaptorManager") AdaptorManager adaptorManager) {
        this.mongo = mongoTemplate;
        this.bmURL = bmURL;
        this.devicesCachePort = devicesCachePort;
        this.getRoomsPath = getRoomsPath;
        this.adaptorManager = adaptorManager;
    }
    
    @PutMapping("/registry")
    public JeepResponse register(@RequestBody RegisterRequest request) throws RequestProcessingException {
        RequestLogFormat format = new RequestLogFormat(request.getMRN(), request.getMSN());
        LOG.info(format.format("Register requested"));
        Object product = request.getProduct();
        Object room = request.getRoom();
        Product productObj;
        Room roomObj;
        Device device;
        boolean newRoom = false;
        
        if (request.getCID().isEmpty()) {
            device = null;
        } else {
            try {
                device = getDeviceObject(request.getCID());
            } catch (Exception e) {
                throw new RequestProcessingException("Error in checking if device already exists", e);
            }
        }
    
        try {
            if (room.getClass().equals(String.class)) {
                roomObj = getRoomObject((String) room);
            } else {
                Map<String, Object> roomMap = (Map<String, Object>) room;
                try {
                    roomObj = getRoomObject((String) roomMap.get("RID"));
                } catch (RequestProcessingException e) {
                    roomObj = createRoomObject((String) roomMap.get("RID"), (String) roomMap.get("name"));
                    newRoom = true;
                }
            }
        } catch (IOException e) {
            throw new RequestProcessingException("IOException in getting room", e);
        }
        
        if (device == null) { // create new device
            if (product.getClass().equals(String.class)) {
                String pid = (String) product;
                LOG.info(format.format("Getting product " + pid + " from DB..."));
                productObj = mongo.findOne(query(where("PID").is(pid)), Product.class);
            } else {
                ObjectMapper objectMapper = new ObjectMapper();
                Vector<DeviceProperty> props = new Vector<>();
                List<Map<String, Object>> propList = (List<Map<String, Object>>) request.getProduct();
                int index = 0;
                for (Map<String, Object> map : propList) {
//                    int index = (Integer) map.get("index");
                    String name = (String) map.get("name");
                    DevicePropertyMode mode = DevicePropertyMode.valueOf((String) map.get("mode"));
                    DeviceProperty prop = new DeviceProperty(index, request.getCID(), name,
                            DevicePropertyType.builder().map((Map<String, Object>) map.get("type")).build(), mode);
                    props.add(prop);
                    index++;
                }
                productObj = new Product(props);
            }
            if (newRoom) { // create new room
                LOG.info(format.format("Creating new room..."));
                try {
                    adaptorManager.roomCreated(roomObj);
                } catch (Exception e) {
                    throw new RequestProcessingException("Unable to create new room", e);
                }
            }
            try {
                device = createDeviceObject(request.getCID(), roomObj.getRID(), request.getName(),
                        productObj.getProperties());
                adaptorManager.deviceCreated(device);
            } catch (Exception e) {
                throw new RequestProcessingException("Unable to register device", e);
            }
            LOG.info(format.format("Device registered successfully"));
            return new JeepSuccessResponse("Device registered");
        } else { // update device
            LOG.warn(format.format("Device " + request.getCID() + " already exists. Updating..."));
            if (newRoom) { // create new room
                LOG.info(format.format("Creating new room..."));
                try {
                    adaptorManager.roomCreated(roomObj);
                } catch (Exception e) {
                    throw new RequestProcessingException("Unable to create new room", e);
                }
            }
            try {
                // NOTE: DO NOT CHANGE THE ORDER! SET NAME FIRST BEFORE CHANGE ROOM!
                if (!device.getName().equals(request.getName())) {
                    LOG.info(format.format("Updating name of device " + device.getCID() + " from "
                            + device.getName() + " to " + request.getName()));
                    device.setName(request.getName());
                }
                if (!device.getRID().equals(roomObj.getRID())) {
                    LOG.info(format.format("Device " + device.getCID() + " transferring from room "
                            + device.getRID() + " to " + roomObj.getRID()));
                    Room currentRoom = getRoomObject(device.getRID());
                    currentRoom.transferDevice(device.getCID(), roomObj);
                }
                LOG.info(format.format("Device " + request.getCID() + " updated"));
            } catch (Exception e) {
                throw new RequestProcessingException("Unable to update device", e);
            }
            return new JeepSuccessResponse("Device updated");
        }
    }
    
    @DeleteMapping("/registry/{cid}")
    public JeepResponse unregister(@PathVariable String cid) throws RequestProcessingException {
//        RequestLogFormat format = new RequestLogFormat(request.getMRN(), request.getMSN());
        LOG.info("Unregister requested for device " + cid);
        try {
            Device device = getDeviceObject(cid);
            adaptorManager.deviceDeleted(device);
        } catch (Exception e) {
            throw new RequestProcessingException("Unable to get device", e);
        }
        return new JeepSuccessResponse("Device unregistered");
    }
    
//    @DeleteMapping("/registry")
//    public JeepResponse unregister(@RequestBody UnregisterRequest request) throws RequestProcessingException {
//        RequestLogFormat format = new RequestLogFormat(request.getMRN(), request.getMSN());
//        LOG.info(format.format("Unregister requested"));
//        try {
//            Device device = getDeviceObject(request.getCID());
//            adaptorManager.deviceDeleted(device);
//        } catch (Exception e) {
//            throw new RequestProcessingException("Unable to get device", e);
//        }
//        return new JeepSuccessResponse("Device deleted");
//    }
    
    private Room getRoomObject(String rid) throws RequestProcessingException, IOException {
        HttpGet getRoom = new HttpGet(bmURL + ":" + devicesCachePort + "/rooms/" + rid);
        HttpResponse response = httpClient.execute(getRoom);
        String json = EntityUtils.toString(response.getEntity());
//        LOG.error("Room: " + json);
        if (json.isEmpty()) {
            throw new RequestProcessingException("Room with RID: " + rid + " does not exist");
        } else {
            return createRoomObject(json);
        }
    }
    
    private Device getDeviceObject(String cid) throws Exception {
        HttpGet getDevice = new HttpGet(bmURL + ":" + devicesCachePort + "/devices/" + cid);
        HttpResponse response = httpClient.execute(getDevice);
        String json = EntityUtils.toString(response.getEntity());
        if (json.isEmpty()) {
            return null;
        }
//        LOG.error("Device: " + json);
        return createDeviceObject(json);
    }
    
    private Room createRoomObject(String rid, String name) {
        Room room = new Room(rid, name);
        room.setAdaptorManager(adaptorManager);
        return room;
    }
    
    private Room createRoomObject(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Room room = mapper.readValue(json, Room.class);
        room.setAdaptorManager(adaptorManager);
        return room;
    }
    
    private Device createDeviceObject(String cid, String rid, String name, List<DeviceProperty> properties) {
        Device device = new Device(cid, rid, name, properties);
        device.setAdaptorManager(adaptorManager);
        return device;
    }
    
    private Device createDeviceObject(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Device device = mapper.readValue(json, Device.class);
        device.setAdaptorManager(adaptorManager);
        return device;
    }
    
    private class RequestLogFormat {
        private final String mrn;
        private final String msn;
    
        RequestLogFormat(String mrn, String msn) {
            this.mrn = mrn;
            this.msn = msn;
        }
        
        String format(String msg) {
            return mrn + "(" + msn + "): " + msg;
        }
    }
}
