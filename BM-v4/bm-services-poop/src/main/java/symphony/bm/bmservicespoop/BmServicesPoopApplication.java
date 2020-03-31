package symphony.bm.bmservicespoop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import symphony.bm.bmservicespoop.adaptors.POOPAdaptor;
import symphony.bm.bmservicespoop.mongodb.MongoDB_POOPAdaptor;

import java.util.List;
import java.util.Vector;

@SpringBootApplication
@PropertySource("file:resources/bm.properties")
@ImportResource("file:resources/bm-services-poop.config.xml")
public class BmServicesPoopApplication {

    public static void main(String[] args) {
        SpringApplication.run(BmServicesPoopApplication.class, args);
    }

}
