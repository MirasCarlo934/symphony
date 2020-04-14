package symphony.bm.cache.devices.entities.deviceproperty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

//@AllArgsConstructor(onConstructor_ = {@JsonCreator})
public class DevicePropertyType {
    private static String[] binaryMapParams = {"data", "ui"};
    private static String[] enumMapParams = {"data", "ui", "values"};
    private static String[] numberMapParams = {"data", "ui", "minValue", "maxValue"};
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
    
    @Builder()
    public static DevicePropertyType parseDevicePropertyType(Map<String, Object> map) throws IllegalArgumentException,
            NullPointerException, ClassCastException {
        String data = (String) map.get("data");
        DevicePropertyInterface ui;
        switch (data) {
            case "binary":
                for (String param : binaryMapParams) {
                    if (!map.containsKey(param)) {
                        throw new NullPointerException("'" + param + "' must exist when declaring a " + data + " property type!");
                    }
                }
                ui = DevicePropertyInterface.valueOf((String) map.get("ui"));
                return new DevicePropertyType(DataType.binary, ui, null, null, null);
            case "enumeration":
                for (String param : enumMapParams) {
                    if (!map.containsKey(param)) {
                        throw new NullPointerException("'" + param + "' must exist when declaring a " + data + " property type!");
                    }
                }
                ui = DevicePropertyInterface.valueOf((String) map.get("ui"));
                return new DevicePropertyType(DataType.enumeration, ui, null, null,
                        (List<String>) map.get("values"));
            case "number":
                for (String param : numberMapParams) {
                    if (!map.containsKey(param)) {
                        throw new NullPointerException("'" + param + "' must exist when declaring a " + data + " property type!");
                    }
                }
                ui = DevicePropertyInterface.valueOf((String) map.get("ui"));
                return new DevicePropertyType(DataType.number, ui, (Number) map.get("minValue"),
                        (Number) map.get("maxValue"), null);
            case "string":
                for (String param : stringMapParams) {
                    if (!map.containsKey(param)) {
                        throw new NullPointerException("'" + param + "' must exist when declaring a " + data + " property type!");
                    }
                }
                return new DevicePropertyType(DataType.string, DevicePropertyInterface.field, null, null, null);
            default:
                throw new IllegalArgumentException("Invalid property data type");
        }
    }
}
