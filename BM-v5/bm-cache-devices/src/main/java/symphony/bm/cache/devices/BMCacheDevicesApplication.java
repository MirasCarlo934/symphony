package symphony.bm.cache.devices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@SpringBootApplication
@PropertySources({
        @PropertySource("file:${bm.resources.home}/bm.properties")
})
@ImportResource(locations = {
        "file:${bm.resources.home}/bm-cache-devices.config.xml"
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