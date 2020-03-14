package symphony.bm.bm_comms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import symphony.bm.bm_comms.mqtt.MQTTClient;

@SpringBootApplication
@PropertySource("classpath:bm-comms.properties")
@ImportResource({"classpath:bm-comms.config.xml"})
public class BMCommsApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(BMCommsApplication.class, args);
        MQTTClient c = (MQTTClient) context.getBean("MQTT.client");
        System.out.println("HEKHEK");
    }

}
