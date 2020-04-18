package symphony.bm.services.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootApplication
@PropertySources({
        @PropertySource("file:${bm.resources.home}/bm.properties")
})
@ImportResource(locations = {
        "file:${bm.config.home}/bm-services-registry.config.xml"
})
public class BMServicesRegistryApplication {
    
    private ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    public static void main(String[] args) {
        SpringApplication.run(BMServicesRegistryApplication.class, args);
    }
    
    @Bean
    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }
}
