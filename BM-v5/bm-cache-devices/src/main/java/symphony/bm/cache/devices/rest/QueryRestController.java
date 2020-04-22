package symphony.bm.cache.devices.rest;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import symphony.bm.cache.devices.entities.Device;
import symphony.bm.cache.devices.entities.SuperRoom;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

@RestController
@RequestMapping("/query")
@AllArgsConstructor
public class QueryRestController {
    private static final Logger LOG = LoggerFactory.getLogger(QueryRestController.class);

    private final SuperRoom superRoom;

    @PostMapping("/propertylist")
    public List<DeviceProperty> getDeviceProperties(@RequestBody HashMap<String, List<Integer>> requestBody) {
        LOG.info("Getting device properties...");
        List<DeviceProperty> response = new Vector<>();
        int n = 0;
        for (String cid : requestBody.keySet()) {
            Device d = superRoom.getDevice(cid);
            for (int prop_index : requestBody.get(cid)) {
                response.add(d.getProperty(prop_index));
                n++;
                LOG.info(cid + "." + prop_index + " retrieved");
            }
        }
        LOG.info(n + " properties retrieved");
        return response;
    }

}
