package symphony.bm.cache.devices.entities.deviceproperty;

public enum DataType {
    binary {
        @Override
        boolean checkIfValueIsValid(String value, DevicePropertyType devicePropertyType) {
            int v = Integer.parseInt(value);
            return (v == 1) || (v == 0);
        }
    },
    number {
        @Override
        boolean checkIfValueIsValid(String value, DevicePropertyType devicePropertyType) {
            double v = Double.parseDouble(value);
            return (v <= devicePropertyType.getMaxValue().doubleValue()) &&
                    (v >= devicePropertyType.getMinValue().doubleValue());
        }
    },
    enumeration {
        @Override
        boolean checkIfValueIsValid(String value, DevicePropertyType devicePropertyType) {
            return devicePropertyType.getValues().contains(value);
        }
    },
    string {
        @Override
        boolean checkIfValueIsValid(String value, DevicePropertyType devicePropertyType) {
            return true;
        }
    };
    
    abstract boolean checkIfValueIsValid(String value, DevicePropertyType devicePropertyType);
}
