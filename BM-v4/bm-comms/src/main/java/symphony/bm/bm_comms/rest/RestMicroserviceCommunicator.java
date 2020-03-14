package symphony.bm.bm_comms.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import symphony.bm.bm_comms.jeep.vo.JeepMessage;
import symphony.bm.bm_comms.jeep.vo.JeepRequest;

@RestController
public class RestMicroserviceCommunicator {
    private Logger LOG;
    private String name = RestMicroserviceCommunicator.class.getSimpleName();

    public RestMicroserviceCommunicator(@Value("${log.comms}")String logDomain) {
        LOG = LoggerFactory.getLogger(logDomain + "." + name);
        LOG.info(name + " started!");
    }

    @RequestMapping("/test")
    public TestTxt test(@RequestParam(value = "txt", defaultValue = "Hello, World!") String txt) {
        TestTxt obj = new TestTxt(txt);
        LOG.info(txt);
        return obj;
    }

    /**
     * Forwards a JEEP message to the logic layer
     * @param message the JEEP message to be forwarded
     */
    public void forwardJeepMessage(JeepMessage message) {

    }

    /**
     * Checks if the RTY corresponds to an existing request module in the logic layer
     * @param rty the RTY to be checked
     */
    public boolean checkRTY(String rty) {
        return true;
    }

    /**
     * Checks if the device with the corresponding CID exists
     * @param cid the RTY to be checked
     */
    public boolean checkDevice(String cid) {
        return true;
    }

    class TestTxt {
        private String txt;

        public TestTxt(String txt) {
            this.txt = txt;
        }

        public String getTxt() {
            return txt;
        }
    }
}
