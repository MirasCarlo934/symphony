package symphony.bm.bmservicespoop.entities;

import org.bson.Document;
import symphony.bm.bmservicespoop.adaptors.POOPAdaptor;

import java.util.List;

public class DeviceProperty {
    private String deviceCID;
    private int index;
    private String name;
    private String type;
    private DevicePropertyMode mode;
    private int minValue;
    private int maxValue;
    private int value;

    private List<POOPAdaptor> adaptors;

    public DeviceProperty(String deviceCID, int index, String name, String type, DevicePropertyMode mode, int minValue,
                          int maxValue, List<POOPAdaptor> adaptors) {
        this.deviceCID = deviceCID;
        this.index = index;
        this.name = name;
        this.type = type;
        this.mode = mode;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.adaptors = adaptors;
    }

    public DeviceProperty(String deviceCID, Document doc, List<POOPAdaptor> adaptors) {
        this(deviceCID, doc.getInteger("index"), doc.getString("name"), doc.getString("type"),
                DevicePropertyMode.valueOf(doc.getString("mode")), doc.getInteger("minValue"),
                doc.getInteger("maxValue"), adaptors);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        for (POOPAdaptor adaptor : adaptors) {
            adaptor.updatePropertyValue(this);
        }
    }

    boolean checkValue(int value) {
        return (value < maxValue && value > minValue);
    }

    public String getID() {
        return deviceCID + "-" + index;
    }

    public String getDeviceCID() {
        return deviceCID;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public DevicePropertyMode getMode() {
        return mode;
    }
}
