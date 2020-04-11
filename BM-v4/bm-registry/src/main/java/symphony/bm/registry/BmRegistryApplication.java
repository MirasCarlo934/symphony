package symphony.bm.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@SpringBootApplication
@PropertySource("file:resources/bm.properties")
public class BmRegistryApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BmRegistryApplication.class, args);
    }
    
    @Bean
    MongoMappingContext springDataMongoMappingContext() {
        return new MongoMappingContext();
    }
}
