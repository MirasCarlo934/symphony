package symphony.bm.cir;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

import java.util.UUID;

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
//    private final MqttPahoClientFactory mqttClientFactory;
//    private final MessageChannel errorChannel;
//
//    public BmCirApplication(MqttPahoClientFactory mqttClientFactory,
//                            @Qualifier("errorChannel") MessageChannel errorChannel) {
//        this.mqttClientFactory = mqttClientFactory;
//        this.errorChannel = errorChannel;
//    }
    
    public static void main(String[] args) {
        SpringApplication.run(BmCirApplication.class, args);
    }
    
    
//    @Bean
//    @Scope("prototype")
//    public MqttPahoMessageDrivenChannelAdapter mqttAdapter() {
//        MqttPahoMessageDrivenChannelAdapter adapter =
//                new MqttPahoMessageDrivenChannelAdapter(UUID.randomUUID().toString(), mqttClientFactory,
//                "defaultNobodyListensToThisTopicHahah");
//        adapter.setCompletionTimeout(5000);
//        adapter.setConverter(new DefaultPahoMessageConverter());
//        adapter.setQos(2);
//        adapter.setErrorChannel(errorChannel);
//        return adapter;
//    }
}
