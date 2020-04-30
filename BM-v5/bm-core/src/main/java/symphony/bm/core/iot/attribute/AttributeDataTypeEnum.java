package symphony.bm.core.iot.attribute;

import java.util.List;
import java.util.Map;

public enum AttributeDataTypeEnum {
    binary {
        @Override
        public boolean checkValueIfValid(Object value, Map<String, Object> constraints) throws Exception {
            int i = Integer.parseInt(value.toString());
            if (i < 0 || i > 1) {
                throw new Exception("Binary attribute value must only be 1 or 0");
            }
            return true;
        }
    }, number {
        @Override
        public boolean checkValueIfValid(Object value, Map<String, Object> constraints) throws Exception {
            double d = Double.parseDouble(value.toString());
            try {
                double min = (double) constraints.get("min");
                if (d < min) {
                    throw new Exception("Attribute value must be greater than " + min
                            + " as set in constraints");
                }
                double max = (double) constraints.get("max");
                if (d > max) {
                    throw new Exception("Attribute value must be less than " + max
                            + " as set in constraints");
                }
            } catch (NullPointerException e) {
                // no min/max value constraint
            }
            return true;
        }
    }, enumeration {
        @Override
        public boolean checkValueIfValid(Object value, Map<String, Object> constraints) throws Exception {
            List<String> enumValues = (List<String>) constraints.get("values");
            String s = value.toString();
            if (!enumValues.contains(s)) {
                throw new Exception("Attribute value must only be [" + String.join(",", enumValues) + "]");
            }
            return true;
        }
    }, string {
        @Override
        public boolean checkValueIfValid(Object value, Map<String, Object> constraints) throws Exception {
            return true;
        }
    };

    abstract boolean checkValueIfValid(Object value, Map<String, Object> constraints) throws Exception;
}
