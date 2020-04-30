package symphony.bm.core.iot.attribute;

import java.util.HashMap;
import java.util.List;

public enum AttributeDataTypeEnum {
    binary {
        @Override
        public boolean checkValueIfValid(Object value, HashMap<String, Object> constraints) throws Exception {
            int i = Integer.parseInt(value.toString());
            if (i < 0 || i > 1) {
                throw new Exception("Binary attribute value must only be 1 or 0");
            }
            return true;
        }
    }, number {
        @Override
        public boolean checkValueIfValid(Object value, HashMap<String, Object> constraints) throws Exception {
            double d = Double.parseDouble(value.toString());
            double min = (double) constraints.get("min");
            double max = (double) constraints.get("max");
            if (d < min || d > max) {
                throw new Exception("Attribute value must only be from " + min + " to " + max
                        + " as set in constraints");
            }
            return true;
        }
    }, enumeration {
        @Override
        public boolean checkValueIfValid(Object value, HashMap<String, Object> constraints) throws Exception {
            List<String> enumValues = (List<String>) constraints.get("values");
            String s = value.toString();
            if (!enumValues.contains(s)) {
                throw new Exception("Attribute value must only be [" + String.join(",", enumValues) + "]");
            }
            return true;
        }
    }, string {
        @Override
        public boolean checkValueIfValid(Object value, HashMap<String, Object> constraints) throws Exception {
            return true;
        }
    };

    abstract boolean checkValueIfValid(Object value, HashMap<String, Object> constraints) throws Exception;
}
