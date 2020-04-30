package symphony.bm.mqtt.iot;

import symphony.bm.core.iot.attribute.AttributeMode;

public enum MinifiedAttributeMode implements Minified<AttributeMode> {
    ctrl {
        @Override
        public AttributeMode unminify() {
            return AttributeMode.controllable;
        }
    }, in {
        @Override
        public AttributeMode unminify() {
            return AttributeMode.input;
        }
    }
}
