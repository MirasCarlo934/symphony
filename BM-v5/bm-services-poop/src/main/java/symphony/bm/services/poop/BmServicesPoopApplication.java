package symphony.bm.services.poop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication
@PropertySources({
        @PropertySource("file:${bm.resources.home}/bm.properties")
})
@ImportResource(locations = {
        "file:${bm.config.home}/bm-services-poop.config.xml"
})
public class BmServicesPoopApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BmServicesPoopApplication.class, args);
    }
    
}
