package symphony.bm.cache.devices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@SpringBootApplication(scanBasePackages = {"symphony.bm.cache.devices", "symphony.bm.generics"})
@PropertySources({
        @PropertySource("file:${bm.resources.home}/bm.properties")
})
@ImportResource(locations = {
        "file:${bm.config.home}/bm-cache-devices.config.xml"
})
public class BMCacheDevicesApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BMCacheDevicesApplication.class, args);
    }
    
    @Bean
    MongoMappingContext springDataMongoMappingContext() {
        return new MongoMappingContext();
    }
}
