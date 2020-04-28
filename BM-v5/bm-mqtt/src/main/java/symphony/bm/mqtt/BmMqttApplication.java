package symphony.bm.mqtt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@PropertySource({
        "file:${bm.properties.home}/bm.properties"
})
@ImportResource({
        "file:${bm.config.home}/bm-mqtt.config.xml"
})
@SpringBootApplication
public class BmMqttApplication {

    public static void main(String[] args) {
        SpringApplication.run(BmMqttApplication.class, args);
    }

}
