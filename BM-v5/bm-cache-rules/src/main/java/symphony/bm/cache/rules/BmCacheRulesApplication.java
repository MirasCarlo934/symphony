package symphony.bm.cache.rules;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication(scanBasePackages = {"symphony.bm.cache.rules", "symphony.bm.generics"})
@PropertySources({
        @PropertySource("file:${bm.resources.home}/bm.properties")
})
@ImportResource(locations = {
        "file:${bm.config.home}/bm-cache-rules.config.xml"
})
public class BmCacheRulesApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BmCacheRulesApplication.class, args);
    }
    
}
