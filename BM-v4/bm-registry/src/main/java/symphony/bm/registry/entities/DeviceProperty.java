package symphony.bm.registry.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import symphony.bm.registry.adaptors.Adaptor;

import java.util.List;

public class DeviceProperty extends Entity{
    @Getter private int index;
    @Getter private String name;
    @Getter private String type;
    @Getter private DevicePropertyMode mode;
    @Getter private double minValue;
    @Getter private double maxValue;
    @Getter private String value;
    
    @Transient @Setter @Getter(onMethod_ = {@JsonIgnore}) private Device device;
    
    public DeviceProperty(int index, String name, String type, DevicePropertyMode mode, double minValue, double maxValue) {
        this.index = index;
        this.name = name;
        this.type = type;
        this.mode = mode;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

//    void reloadFromDB(Document deviceDoc) {
//        Document propDoc = deviceDoc.get("properties", Document.class).get(String.valueOf(index), Document.class);
//        name = propDoc.getString("name");
//        value = propDoc.getString("value");
//    }
    
    public boolean isIntegerType() {
        return minValue != maxValue;
    }
    
    public void replace(DeviceProperty property) {
        device.putProperty(property);
        for (Adaptor adaptor : adaptors) {
            adaptor.propertyUpdated(property);
        }
    }
    
    @Override
    void setSelfToChildren() {
    
    }
    
    
    @Override
    void setAdaptorsToChildren(List<Adaptor> adaptors) {
    
    }
}
