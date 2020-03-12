package symphony.bm.bm_comms.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import symphony.bm.bm_comms.jeep.vo.JeepMessage;
import symphony.bm.bm_comms.jeep.vo.JeepRequest;

@RestController
public class RestMicroserviceCommunicator {

    @RequestMapping
    public TestTxt test(@RequestParam(value = "txt", defaultValue = "Hello, World!") String txt) {
        TestTxt obj = new TestTxt(txt);
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

    }

    /**
     * Checks if the device with the corresponding CID exists
     * @param cid the RTY to be checked
     */
    public boolean checkDevice(String cid) {

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
