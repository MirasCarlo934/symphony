package symphony.bm.bmlogicdevices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("file:resources/bm-logic-devices.properties")
@ImportResource({"file:resources/bm-logic-devices.config.xml"})
public class BMLogicDevicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(BMLogicDevicesApplication.class, args);
    }

}
