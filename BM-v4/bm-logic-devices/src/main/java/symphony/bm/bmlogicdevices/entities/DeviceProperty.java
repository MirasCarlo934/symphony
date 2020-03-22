package symphony.bm.bmlogicdevices.entities;

public class DeviceProperty {
    private int index;
    private String name;
    private String type;
    private int minValue;
    private int maxValue;
    private int value;

    public DeviceProperty(int index, String name, String type, int minValue, int maxValue) {
        this.index = index;
        this.name = name;
        this.type = type;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
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
}
