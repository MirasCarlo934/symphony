package symphony.bm.bmlogicdevices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("file:resources/bm-services-registration.properties")
@ImportResource({"file:resources/bm-services-registration.config.xml"})
public class BMServicesRegistration {

    public static void main(String[] args) {
        SpringApplication.run(BMServicesRegistration.class, args);
    }

}
