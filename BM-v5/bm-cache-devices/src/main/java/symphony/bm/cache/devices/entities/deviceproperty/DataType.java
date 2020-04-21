package symphony.bm.cache.devices.entities.deviceproperty;

import java.util.Arrays;
import java.util.List;

public enum DataType {
    binary {
        @Override
        boolean checkIfValueIsValid(String value, DevicePropertyType devicePropertyType) {
            int v;
            try {
                v = Integer.parseInt(value);
            } catch (Exception e) {
                return false;
            }
            return (v == 1) || (v == 0);
        }
    
        @Override
        List<DevicePropertyInterface> getValidUI() {
            return Arrays.asList(DevicePropertyInterface.toggle, DevicePropertyInterface.push);
        }
    },
    boundednumber {
        @Override
        boolean checkIfValueIsValid(String value, DevicePropertyType devicePropertyType) {
            double v;
            try {
                v = Double.parseDouble(value);
            } catch (Exception e) {
                return false;
            }
            return (v <= devicePropertyType.getMaxValue().doubleValue()) &&
                    (v >= devicePropertyType.getMinValue().doubleValue());
        }
    
        @Override
        List<DevicePropertyInterface> getValidUI() {
            return Arrays.asList(DevicePropertyInterface.slider, DevicePropertyInterface.field);
        }
    },
    number {
        @Override
        boolean checkIfValueIsValid(String value, DevicePropertyType devicePropertyType) {
            try {
                Double.parseDouble(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        
        @Override
        List<DevicePropertyInterface> getValidUI() {
            return Arrays.asList(DevicePropertyInterface.field);
        }
    },
    enumeration {
        @Override
        boolean checkIfValueIsValid(String value, DevicePropertyType devicePropertyType) {
            return devicePropertyType.getValues().contains(value);
        }
    
        @Override
        List<DevicePropertyInterface> getValidUI() {
            return Arrays.asList(DevicePropertyInterface.field, DevicePropertyInterface.menu);
        }
    },
    string {
        @Override
        boolean checkIfValueIsValid(String value, DevicePropertyType devicePropertyType) {
            return true;
        }
    
        @Override
        List<DevicePropertyInterface> getValidUI() {
            return Arrays.asList(DevicePropertyInterface.field);
        }
    };
    
    abstract boolean checkIfValueIsValid(String value, DevicePropertyType devicePropertyType);
    abstract List<DevicePropertyInterface> getValidUI();
}
