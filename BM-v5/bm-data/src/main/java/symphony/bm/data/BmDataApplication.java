package symphony.bm.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@PropertySource({
        "file:${bm.properties.home}/bm.properties"
})
@ImportResource({
        "file:${bm.config.home}/bm-data.config.xml"
})
@SpringBootApplication(scanBasePackages = {
        "symphony.bm.data",
        "symphony.bm.generics"
})
public class BmDataApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BmDataApplication.class, args);
    }
    
}
