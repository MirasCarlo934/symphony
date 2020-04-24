package symphony.bm.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.hateoas.config.EnableHypermediaSupport;

@SpringBootApplication
@ImportResource({
        "file:${bm.config.home}/bm-core.config.xml"
})
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL_FORMS)
public class BmCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(BmCoreApplication.class, args);
    }

    @Bean
    MongoMappingContext springDataMongoMappingContext() {
        return new MongoMappingContext();
    }
}
