package symphony.bm.bm_comms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
//@PropertySource("file:resources/bm.properties")
//@ImportResource("file:resources/bm-comms.config.xml")
@PropertySource("classpath:bm-comms.config.xml")
@PropertySource("classpath:bm.properties")
public class BMCommsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BMCommsApplication.class, args);
    }
    
}

