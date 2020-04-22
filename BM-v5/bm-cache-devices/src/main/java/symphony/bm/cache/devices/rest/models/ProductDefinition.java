package symphony.bm.cache.devices.rest.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import symphony.bm.cache.devices.entities.deviceproperty.DeviceProperty;

import java.util.List;

@Document(collection = "products")
@Value
@AllArgsConstructor(onConstructor_ = {@PersistenceConstructor})
public class ProductDefinition {
    String name;
    String description;
    List<DeviceProperty> properties;
    
    @JsonCreator
    public ProductDefinition(@NonNull @JsonProperty("properties") List<DeviceProperty> properties) {
        this.name = null;
        this.description = null;
        this.properties = properties;
    }
}
