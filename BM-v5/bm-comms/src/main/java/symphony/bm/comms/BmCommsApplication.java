package symphony.bm.comms;

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
        "file:${bm.resources.home}/bm-comms.config.xml"
})
public class BmCommsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BmCommsApplication.class, args);
    }
    
}
