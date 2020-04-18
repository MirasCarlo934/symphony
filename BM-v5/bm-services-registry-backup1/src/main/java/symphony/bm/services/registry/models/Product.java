package symphony.bm.services.registry.models;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;

import java.util.List;

@Document(collection = "products")
@Value
@AllArgsConstructor(onConstructor_ = {@PersistenceConstructor})
public class Product {
    String name;
    String description;
    List<DeviceProperty> properties;
    
    public Product(List<DeviceProperty> properties) {
        this.name = null;
        this.description = null;
        this.properties = properties;
    }
}
