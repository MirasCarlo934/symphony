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
public class BmServicesPoopApplication {

    @Autowired
    private MongoDB_POOPAdaptor poop;

    public static void main(String[] args) {
        SpringApplication.run(BmServicesPoopApplication.class, args);
    }

    @Bean()
    public List<POOPAdaptor> getAdaptors() {
        Vector<POOPAdaptor> adaptors = new Vector<>();
        adaptors.add(poop);
        return adaptors;
    }
}
