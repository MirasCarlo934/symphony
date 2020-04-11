package symphony.bm.cache.devices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@SpringBootApplication
@PropertySource("classpath:bm.properties")
@ImportResource("classpath:bm-cache-devices.config.xml")
public class BMCacheDevicesApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BMCacheDevicesApplication.class, args);
    }
    
    @Bean
    MongoMappingContext springDataMongoMappingContext() {
        return new MongoMappingContext();
    }
}
