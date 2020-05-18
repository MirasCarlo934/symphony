package symphony.bm.core.iot.attribute;

import java.util.List;
import java.util.Map;

public enum AttributeDataTypeEnum {
    binary {
        @Override
        boolean checkValuesForEquality(Object value1, Object value2) {
            return Integer.valueOf(value1.toString()).equals(Integer.valueOf(value2.toString()));
        }
        @Override
        boolean checkConstraintsIfValid(Map<String, Object> constraints) throws Exception {
            try {
                if (constraints.containsKey("min") && Double.parseDouble(constraints.get("min").toString()) != 0d) {
                    throw new IllegalArgumentException("Binary data type min must always be 0");
                }
                if (constraints.containsKey("max") && Double.parseDouble(constraints.get("max").toString()) != 1d) {
                    throw new IllegalArgumentException("Binary data type max must always be 1");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Data type constraint min/max is not a number");
            }
            return true;
        }
        @Override
        Object checkValueIfValid(Object value, Map<String, Object> constraints) throws Exception {
            try {
                int i = Integer.parseInt(value.toString());
                if (i < 0 || i > 1) {
                    throw new Exception("Binary attribute value must only be 1 or 0");
                }
                return i;
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Binary attribute value must only be 1 or 0");
            }
        }
        @Override
        Object getDefaultValue(Map<String, Object> constraints) {
            return 0;
        }
    },
    number {
        @Override
        boolean checkValuesForEquality(Object value1, Object value2) {
            return Double.valueOf(value1.toString()).equals(Double.valueOf(value2.toString()));
        }
        @Override
        boolean checkConstraintsIfValid(Map<String, Object> constraints) throws Exception {
            try {
                double min = Double.parseDouble(constraints.get("min").toString());
                double max = Double.parseDouble(constraints.get("max").toString());
                if (min >= max) {
                    throw new IllegalArgumentException("Number data type min must always be less than max");
                }
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Value is not a number");
            } catch (NullPointerException | ClassCastException e) {
                // no min/max value constraint
            }
            return true;
        }
        @Override
        Object checkValueIfValid(Object value, Map<String, Object> constraints) throws Exception {
            double d = Double.parseDouble(value.toString());
            try {
                double min = Double.parseDouble(constraints.get("min").toString());
                if (d < min) {
                    throw new Exception("Attribute value must be greater than " + min
                            + " as set in constraints");
                }
                double max = Double.parseDouble(constraints.get("max").toString());
                if (d > max) {
                    throw new Exception("Attribute value must be less than " + max
                            + " as set in constraints");
                }
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Value is not a number");
            } catch (NullPointerException | ClassCastException e) {
                // no min/max value constraint
            }
            return d;
        }
        @Override
        Object getDefaultValue(Map<String, Object> constraints) {
            try {
                return Double.parseDouble(constraints.get("min").toString());
            } catch (Exception e) {
                return 0;
            }
        }
    },
    enumeration {
        @Override
        boolean checkValuesForEquality(Object value1, Object value2) {
            return value1.toString().equals(value2.toString());
        }
        @Override
        boolean checkConstraintsIfValid(Map<String, Object> constraints) throws Exception {
            try {
                List<String> values = (List<String>) constraints.get("values");
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Enumeration data type has no declared values under constraints");
            }
            return true;
        }
        @Override
        Object checkValueIfValid(Object value, Map<String, Object> constraints) throws Exception {
            List<String> enumValues = (List<String>) constraints.get("values");
            String s = value.toString();
            if (!enumValues.contains(s)) {
                throw new Exception("Attribute value must only be [" + String.join(",", enumValues) + "]");
            }
            return value;
        }
        @Override
        Object getDefaultValue(Map<String, Object> constraints) {
            List<String> enumValues = (List<String>) constraints.get("values");
            return enumValues.get(0);
        }
    },
    string {
        @Override
        boolean checkValuesForEquality(Object value1, Object value2) {
            return value1.toString().equals(value2.toString());
        }
        @Override
        boolean checkConstraintsIfValid(Map<String, Object> constraints) throws Exception {
            return true;
        }
        @Override
        Object checkValueIfValid(Object value, Map<String, Object> constraints) throws Exception {
            return value;
        }
        @Override
        Object getDefaultValue(Map<String, Object> constraints) {
            return "";
        }
    };

    abstract boolean checkValuesForEquality(Object value1, Object value2);
    abstract boolean checkConstraintsIfValid(Map<String, Object> constraints) throws Exception;
    abstract Object checkValueIfValid(Object value, Map<String, Object> constraints) throws Exception;
    abstract Object getDefaultValue(Map<String, Object> constraints);
}
