package symphony.bm.cir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@PropertySource({
        "file:${bm.properties.home}/bm.properties"
})
@SpringBootApplication
public class BmCirApplication {

    public static void main(String[] args) {
        SpringApplication.run(BmCirApplication.class, args);
    }

}
