package bm.context.products;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import bm.context.properties.Property;
import bm.context.properties.PropertyMode;
import bm.jeep.JEEPManager;
import org.apache.log4j.Logger;

import bm.context.properties.PropertyType;
import bm.tools.IDGenerator;

/**
 * The ProductFactory <i>constructs</i> the products in the Symphony environment by retrieving their individual
 * properties from the database.
 */
public class ProductFactory {
	protected Logger LOG;
	protected String logDomain;
	protected static HashMap<String, PropertyType> propertyTypes = new HashMap<String, PropertyType>(6);
	private JEEPManager jm;

	private IDGenerator idg;

	public ProductFactory(String logDomain, JEEPManager jeepManager, IDGenerator idGenerator) {
		this.LOG = Logger.getLogger(logDomain + "." + ProductFactory.class.getSimpleName());
		this.logDomain = logDomain;
		this.jm = jeepManager;
		this.idg = idGenerator;
	}

    /**
     * Creates a new Product object.
     *
     * @param SSID The product ID
     * @param name The product name
     * @param description The product description
     * @param iconImg The product icon image (openhab icon image name)
     * @param productsRS The
     * @return
     */
	public Product createProductObject(String SSID, String name, String description, String iconImg,
                                       ResultSet productsRS) {
	    LOG.debug("Constructing product " + SSID + " (" + name + ")");
	    Product prod = new Product(logDomain, SSID, name, description, iconImg, propertyTypes, jm, idg);
        try {
            productsRS.beforeFirst();
            while(productsRS.next()) {
                String prod_ssid =  productsRS.getString("prod_ssid");
                try{
                    if(prod_ssid.equals(SSID)) {
                        PropertyType prop_type = propertyTypes.get(productsRS.getString("prop_type"));
                        String prop_dispname = productsRS.getString("prop_dispname");
                        PropertyMode prop_mode = PropertyMode.parseFromString(productsRS.getString(
                                "prop_mode"));
                        int prop_index = productsRS.getInt("prop_index");
                        Property prop = new Property(prop_type, prop_index, prop_dispname, prop_mode, jm);
                        LOG.debug("Adding property \"" + prop_dispname + "\" (index: " + prop_index +
                                ") to product \"" + name + "\" (SSID: " + SSID + ")!");
                        prod.addProperty(prop);
                    }
                } catch(NullPointerException e) {
                    throw new IllegalArgumentException("Product not yet initialized!", e);
                }
            }
        } catch (SQLException e) {
            LOG.error("Could not retrieve properties for product \"" + name + "\" (SSID: " + SSID + ")!");
        }

        return prod;
    }

    public NoProduct createNoProductObject(String name, String description, String iconImg) {
	    return new NoProduct(logDomain, name, description, iconImg, idg);
    }

	public void setPropertyTypes(HashMap<String, PropertyType> propertyTypes) {
		ProductFactory.propertyTypes = propertyTypes;
	}
}
