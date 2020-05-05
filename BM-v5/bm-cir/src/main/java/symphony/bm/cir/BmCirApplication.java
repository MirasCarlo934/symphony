package symphony.bm.cir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@PropertySource({
        "file:${bm.properties.home}/bm.properties"
})
@ImportResource({
        "file:${bm.config.home}/bm-cir.config.xml"
})
@SpringBootApplication(scanBasePackages = {
        "symphony.bm.cir",
        "symphony.bm.generics"
})
public class BmCirApplication {

    public static void main(String[] args) {
        SpringApplication.run(BmCirApplication.class, args);
    }

}
