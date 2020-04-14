package symphony.bm.comms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:bm.properties")
@ImportResource("classpath:bm-comms.config.xml")
public class BmCommsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BmCommsApplication.class, args);
    }
    
}
