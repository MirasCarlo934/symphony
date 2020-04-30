package symphony.bm.mqtt;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import symphony.bm.generics.messages.MicroserviceMessage;

@EqualsAndHashCode(callSuper = true)
@Value
@AllArgsConstructor
public class MqttMessage extends Throwable {
    String topic;
    MicroserviceMessage msg;
}
