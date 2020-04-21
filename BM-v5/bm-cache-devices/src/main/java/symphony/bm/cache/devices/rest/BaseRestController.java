package symphony.bm.cache.devices.rest;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import symphony.bm.cache.devices.entities.SuperRoom;
import symphony.bm.cache.devices.entities.Device;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;
import symphony.bm.cache.devices.entities.Room;
import symphony.bm.cache.devices.rest.messages.MicroserviceMessage;
import symphony.bm.cache.devices.rest.messages.MicroserviceSuccessfulMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

@RestController
@AllArgsConstructor
public class BaseRestController {
    private static String msgToAneya = "Luv u bb <3";
    private static final Logger LOG = LoggerFactory.getLogger(BaseRestController.class);
    private final SuperRoom superRoom;
    
    @GetMapping("/")
    public SuperRoom getAll() {
        LOG.info("Returning all entities in the Symphony Network...");
        return superRoom;
    }
    
    @RequestMapping("/aneya")
    public String aneya() {
        return msgToAneya;
    }
    
    @RequestMapping("/aneya/set")
    public String aneya(@RequestParam("msg") String msg) {
        msgToAneya = msg;
        return "Message set!";
    }
}
