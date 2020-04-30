package symphony.bm.mqtt.iot;

import symphony.bm.core.iot.attribute.AttributeDataTypeEnum;

public enum MinifiedAttributeDataTypeEnum implements Minified<AttributeDataTypeEnum> {
    bin {
        @Override
        public AttributeDataTypeEnum unminify() {
            return AttributeDataTypeEnum.binary;
        }
    }, num {
        @Override
        public AttributeDataTypeEnum unminify() {
            return AttributeDataTypeEnum.number;
        }
    }, enumrtn {
        @Override
        public AttributeDataTypeEnum unminify() {
            return AttributeDataTypeEnum.enumeration;
        }
    }, str {
        @Override
        public AttributeDataTypeEnum unminify() {
            return AttributeDataTypeEnum.string;
        }
    };
}
