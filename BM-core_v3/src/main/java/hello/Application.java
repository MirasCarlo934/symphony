package hello;

import autovalue.shaded.com.google$.common.collect.$Iterators;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@SpringBootApplication
@RestController
public class Application {

    private static Logger LOG;
    TestMqtt testMqtt;

    public Application() {
        try {
            LOG.info("start MQTT instantiation");
            testMqtt = new TestMqtt("tcp://192.168.0.116:1883", "testMqttClient");
            testMqtt.connect();
            LOG.info("MQTT  instantiated!!!");
        } catch (MqttException e) {
            LOG.error("Error in connecting to MQTT");
            e.printStackTrace();
        }
    }

    @RequestMapping("/test")
    public String test() {
        return "<h1>Test Hello Docker World</h1>";
    }
    @RequestMapping("/hello")
    public String hello() {
        return "<h1>Hello Docker World</h1>";
    }
    @RequestMapping(value = "/mqtt", params = "msg", method = GET)
    @ResponseBody
    public String mqtt(@RequestParam String msg) {
        testMqtt.send(msg);
        LOG.info("MQTT message published!!!");
        return "message:" + msg + " sent to MQTT topic /BM";
    }
    @GetMapping("/")
    public ResponseEntity<String> multiValue(
            @RequestHeader MultiValueMap<String, String> headers) {
        if (headers != null) {
            headers.forEach((key, value) -> {
                LOG.info(String.format(
                        "Header '%s' = %s", key, value.stream().collect(Collectors.joining("|"))));
            });
            Iterator<String> it = headers.keySet().iterator();
            String s = "<h1>test of nginx reverse proxy using docker</h1>";
            while(it.hasNext()){
                String theKey = (String)it.next();
                s = s +theKey + " = " + headers.getFirst(theKey) +"<br>";
            }
            //return new ResponseEntity<String>(String.format("Listed %d headers", headers.size()), HttpStatus.OK);
            return new ResponseEntity<String>(s, HttpStatus.OK);
        } else {
            LOG.info("header is null");
            return new ResponseEntity<String>("No header", HttpStatus.OK);
        }

    }
    public static void main(String[] args) {
        LOG = Logger.getLogger("APPLICATION");
        SpringApplication.run(Application.class, args);
    }

}