package symphony.bm.cir.messaging;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.*;

@Slf4j
public class ThingChannelFilter implements MessageHandler {
    @Getter private final SubscribableChannel thingChannel;
    private final String thingURL;
    private Message<?> retained;
    
    public ThingChannelFilter(String thingURL) {
//        RetainablePublishSubscribeChannel thingChannel = new RetainablePublishSubscribeChannel();
        PublishSubscribeChannel thingChannel = new PublishSubscribeChannel();
        thingChannel.setComponentName(thingURL);
        this.thingChannel = thingChannel;
        this.thingURL = thingURL;
    }
    
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);
        assert topic != null;
        if (topic.contains("things/" + thingURL)) {
            thingChannel.send(message);
        }
    }
    
//    private class RetainablePublishSubscribeChannel extends PublishSubscribeChannel {
//        @Setter private Message<?> retainedMessage;
//
//        @Override
//        public boolean send(Message<?> message) {
//            Boolean retained = message.getHeaders().get("mqtt_receivedRetained", Boolean.class);
//            assert retained != null;
//            if (retained) {
//                this.retainedMessage = message;
//            }
//            return super.send(message);
//        }
//
//        @Override
//        public boolean subscribe(MessageHandler handler) {
//            if (retainedMessage != null) {
//                handler.handleMessage(retainedMessage);
//            }
//            return super.subscribe(handler);
//        }
//    }
}
