package symphony.bm.cache.devices.entities.deviceproperty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

//@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class DevicePropertyType {
    private static String[] binaryMapParams = {"data", "ui"};
    private static String[] enumMapParams = {"data", "ui", "values"};
    private static String[] boundedNumberMapParams = {"data", "ui", "minValue", "maxValue"};
    private static String[] numberMapParams = {"data", "ui"};
    private static String[] stringMapParams = {"data"};
    
    @Getter DataType data;
    @Getter DevicePropertyInterface ui;
    @Getter Number minValue;
    @Getter Number maxValue;
    @Getter List<String> values;
    
    @JsonCreator
    public DevicePropertyType(@JsonProperty("data") DataType data, @JsonProperty("ui") DevicePropertyInterface ui,
                              @JsonProperty("minValue") Number minValue, @JsonProperty("maxValue") Number maxValue,
                              @JsonProperty("values") List<String> values) {
        this.data = data;
        this.ui = ui;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.values = values;
    }
    
    public boolean checkIfValueIsValid(String value) {
        return data.checkIfValueIsValid(value, this);
    }
    
    @Builder()
    public static DevicePropertyType parseDevicePropertyType(Map<String, Object> map) throws IllegalArgumentException,
            NullPointerException, ClassCastException {
        String data = (String) map.get("data");
        DataType dataType;
        DevicePropertyInterface ui;
        switch (data) {
            case "binary":
                dataType = DataType.binary;
                for (String param : binaryMapParams) {
                    if (!map.containsKey(param)) {
                        throw new NullPointerException("'" + param + "' must exist when declaring a " + data + " property type!");
                    }
                }
                ui = DevicePropertyInterface.valueOf((String) map.get("ui"));
                if (!dataType.getValidUI().contains(ui)) {
                    throw new IllegalArgumentException(ui + " not a valid UI for " + dataType);
                }
                return new DevicePropertyType(dataType, ui, 0, 1, null);
            case "enumeration":
                dataType = DataType.enumeration;
                for (String param : enumMapParams) {
                    if (!map.containsKey(param)) {
                        throw new NullPointerException("'" + param + "' must exist when declaring a " + data + " property type!");
                    }
                }
                ui = DevicePropertyInterface.valueOf((String) map.get("ui"));
                if (!dataType.getValidUI().contains(ui)) {
                    throw new IllegalArgumentException(ui + " not a valid UI for " + dataType);
                }
                return new DevicePropertyType(dataType, ui, null, null,
                        (List<String>) map.get("values"));
            case "boundednumber":
                dataType = DataType.boundednumber;
                for (String param : boundedNumberMapParams) {
                    if (!map.containsKey(param)) {
                        throw new NullPointerException("'" + param + "' must exist when declaring a " + data + " property type!");
                    }
                }
                ui = DevicePropertyInterface.valueOf((String) map.get("ui"));
                if (!dataType.getValidUI().contains(ui)) {
                    throw new IllegalArgumentException(ui + " not a valid UI for " + dataType);
                }
                return new DevicePropertyType(dataType, ui, (Number) map.get("minValue"),
                        (Number) map.get("maxValue"), null);
            case "number":
                dataType = DataType.number;
                for (String param : numberMapParams) {
                    if (!map.containsKey(param)) {
                        throw new NullPointerException("'" + param + "' must exist when declaring a " + data + " property type!");
                    }
                }
                ui = DevicePropertyInterface.valueOf((String) map.get("ui"));
                if (!dataType.getValidUI().contains(ui)) {
                    throw new IllegalArgumentException(ui + " not a valid UI for " + dataType);
                }
                return new DevicePropertyType(dataType, ui, null, null, null);
            case "string":
                dataType = DataType.string;
                for (String param : stringMapParams) {
                    if (!map.containsKey(param)) {
                        throw new NullPointerException("'" + param + "' must exist when declaring a " + data + " property type!");
                    }
                }
                ui = DevicePropertyInterface.valueOf((String) map.get("ui"));
                if (!dataType.getValidUI().contains(ui)) {
                    throw new IllegalArgumentException(ui + " not a valid UI for " + dataType);
                }
                return new DevicePropertyType(dataType, DevicePropertyInterface.field, null, null, null);
            default:
                throw new IllegalArgumentException("Invalid property data type");
        }
    }
}
