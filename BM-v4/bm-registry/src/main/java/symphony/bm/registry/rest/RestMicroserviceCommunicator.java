package symphony.bm.registry.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import symphony.bm.registry.entities.*;
import symphony.bm.registry.rest.messages.*;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

@RestController
public class RestMicroserviceCommunicator {
    private static final Logger LOG = LoggerFactory.getLogger(RestMicroserviceCommunicator.class);
    private SuperRoom superRoom;

    public RestMicroserviceCommunicator(SuperRoom superRoom) {
        this.superRoom = superRoom;
    }
    
    @GetMapping("/")
    public SuperRoom getAll() {
        LOG.info("Returning all entities in the Symphony Network...");
        return superRoom;
    }
    
    @GetMapping("/devices/{cid}")
    public Device getDevice(@PathVariable String cid) {
        LOG.info("Getting device " + cid);
        Device d = superRoom.getDevice(cid);
        if (d == null) {
            LOG.warn("No device found!");
        }
        return d;
    }
    
    @GetMapping("/devices/{cid}/{prop_index}")
    public DeviceProperty getDeviceProperty(@PathVariable String cid, @PathVariable int prop_index) {
        LOG.info("Getting device property " + cid + "." + prop_index);
        Device d = getDevice(cid);
        if (d == null) {
            return null;
        }
        DeviceProperty p = d.getProperty(prop_index);
        if (p == null) {
            LOG.warn("No property " + prop_index + " found in device " + cid + "!");
        }
        return p;
    }

    @PostMapping("/query/propertylist")
    public HashMap<String, List<DeviceProperty>> getDeviceProperties(@RequestBody HashMap<String, List<Integer>> requestBody) {
        LOG.info("Getting device properties...");
        HashMap<String, List<DeviceProperty>> response = new HashMap<>();
        int n = 0;
        for (String cid : requestBody.keySet()) {
            response.put(cid, new Vector<>());
            Device d = superRoom.getDevice(cid);
            for (int prop_index : requestBody.get(cid)) {
                response.get(cid).add(d.getProperty(prop_index));
                n++;
                LOG.info(cid + "." + prop_index + " retrieved");
            }
        }
        LOG.info(n + " properties retrieved");
        return response;
    }
    
    @GetMapping("/rooms/{rid}")
    public Room getRoom(@PathVariable String rid) {
        LOG.info("Getting room " + rid);
        Room r = superRoom.getRoom(rid);
        if (r == null) {
            LOG.warn("No room found!");
        }
        return r;
    }
    
    @PutMapping("/devices/{cid}")
    public MicroserviceMessage putDevice(@RequestBody Device device, @PathVariable String cid) {
        return new MicroserviceSuccessfulMessage();
    }
    
    @PutMapping("/devices/{cid}/{prop_index}")
    public MicroserviceMessage patchDeviceProperty(@PathVariable String cid, @PathVariable int prop_index,
                                                   @RequestBody DeviceProperty deviceProperty) {
        LOG.info("Patching device property " + cid + "." + prop_index);
        DeviceProperty p = getDeviceProperty(cid, prop_index);
        if (p == null) {
            return null;
        }
        p.replace(deviceProperty);
        return new MicroserviceSuccessfulMessage();
    }

//    @RequestMapping("internal/reload")
//    public boolean reload(@RequestParam(value = "cid", required = false) String cid,
//                          @RequestParam(value = "propindex", required = false) Integer prop_index) {
//        if (cid != null) {
//            if (prop_index != null) {
//                LOG.info("Reloading device " + cid + " property " + prop_index + " from DB...");
//                registry.reloadDevicePropertyFromDB(cid, prop_index);
//                Device d = registry.getDeviceObject(cid);
//                LOG.error(String.valueOf(d.getPropertyValue(prop_index)));
//                LOG.info("Device reloaded successfully");
//            }
//        }
//        return true;
//    }
//
//    private String receiveJeepMessage(String msgStr, AbstService service) {
//        LOG.debug("Message arrived: " + msgStr);
//        JeepMessage msg = new JeepMessage(msgStr);
//        try {
//            JeepMessage m = service.processMessage(msg);
//            return m.toString();
//        }
//        catch (MessageParameterCheckingException e) {
//            LOG.error("Unable to process message!", e);
//            JeepResponse error = new JeepResponse(msg, e.getMessage());
//            return error.toString();
//        }
//    }
}
