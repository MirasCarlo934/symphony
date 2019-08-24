package bm.main.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import bm.context.products.*;
import bm.context.properties.PropertyType;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.RawDBEReq;
import bm.main.interfaces.Initializable;
import bm.tools.IDGenerator;

/**
 * The ProductRepository is the container for all the products and property types that exist in the Symphony
 * Environment. Only one ProductRepository object must exist in the Symphony Environment.
 */
public class ProductRepository /*extends AbstRepository */ implements Initializable {
	private Logger LOG;
	private ProductFactory mainProdFactory; //the main product factory
	private String getProductsQuery;
	private String getPropertyTypesQuery;
	private IDGenerator idg;
	private DBEngine dbe;
	private HashMap<String, Product> products = new HashMap<String, Product>(5);
	private HashMap<String, PropertyType> propertyTypes = new HashMap<String, PropertyType>(6);

	/**
	 * Constructs the ProductRepository. The ProductRepository is initialized by {@link bm.main.Maestro Maestro} in
	 * the startup phase.
	 * @param logDomain The log4j log domain to be used
	 * @param productFactory The product factory
	 * @param dbe The DBEngine
	 * @param getProductsQuery The SQL query to be used to retrieve the products from the Symphony database
	 * @param getPropertyTypesQuery The SQL query to be used to retrieve the property types from the Symphony database
	 * @param idg The IDGenerator
	 */
	public ProductRepository(String logDomain, ProductFactory productFactory, DBEngine dbe, String getProductsQuery, 
			String getPropertyTypesQuery, IDGenerator idg) {
		this.LOG = Logger.getLogger(logDomain + "." + ProductRepository.class.getSimpleName());
		this.mainProdFactory = productFactory;
		this.dbe = dbe;
		this.getProductsQuery = getProductsQuery;
		this.getPropertyTypesQuery = getPropertyTypesQuery;
		this.idg = idg;
	}

	/**
	 * @see Initializable#initialize()
	 */
	@Override
	public void initialize() throws Exception {
		retrievePropertyTypesFromDB();
		mainProdFactory.setPropertyTypes(propertyTypes);
		retrieveProductsFromDB();
	}

	/**
	 * Retrieves all products from the Symphony Database. This method is ONLY USUALLY called by the
	 * {@link bm.main.Maestro Maestro} in the startup phase.
	 */
	//TASK recode this to be simpler, use individual SelectDBEReq for each table instead
	public void retrieveProductsFromDB() {
		LOG.info("Populating products from DB...");
		RawDBEReq request = new RawDBEReq(idg.generateERQSRequestID(), dbe, getProductsQuery);
		Object o;
		try {
			o = dbe.putRequest(request, Thread.currentThread(), true);
		} catch (EngineException e1) {
			LOG.fatal("Cannot retrieve products from DB!", e1);
			return;
		}
		Object o2;
		try {
			o2 = dbe.putRequest(request, Thread.currentThread(), true);
		} catch (EngineException e1) {
			LOG.fatal("Cannot retrieve products from DB!", e1);
			return;
		}
		
		ResultSet rs = (ResultSet) o; //for product retrieval
		ResultSet productRS = (ResultSet) o2; //for property retrieval in each product

		try {
			String SSID;
			String name;
			String description;
			String icon;
			while(rs.next()) {
				SSID = rs.getString("prod_ssid");
				name = rs.getString("prod_name");
				description = rs.getString("prod_desc");
				icon = rs.getString("icon");
				if(SSID.equals("0000")) {
					products.put(SSID, mainProdFactory.createNoProductObject(name, description, icon));
				} else if(!products.containsKey(SSID)) {
					Product prod;
//					if(specialProducts.containsKey(SSID)) {
//						prod = specialProducts.get(SSID).createProductObject(SSID, name, description,
//								icon, productRS);
//					} else {
					prod = mainProdFactory.createProductObject(SSID, name, description, icon, productRS);
//					}
					products.put(SSID, prod);
				}
				LOG.debug("Product " + SSID + " (" + name + ") added to repository!");
			}
			LOG.debug(products.size() + " products added!");
			rs.close();
		} catch (SQLException e) {
			LOG.fatal("Cannot retrieve products from DB!", e);
		}
	}

	/**
	 * Retrieves all property types from the Symphony Database. This method is ONLY USUALLY called by the
	 * {@link bm.main.Maestro Maestro} in the startup phase.
	 */
	public void retrievePropertyTypesFromDB() {
		LOG.info("Retrieving property types from DB...");
		RawDBEReq request = new RawDBEReq(idg.generateERQSRequestID(), dbe, getPropertyTypesQuery);
		Object o;
		try {
			o = dbe.putRequest(request, Thread.currentThread(), true);
		} catch (EngineException e1) {
			LOG.fatal("Cannot retrieve products from DB!", e1);
			return;
		}
		
		ResultSet rs = (ResultSet) o;
		try {
			while(rs.next()) {
				String ssid = rs.getString("ssid");
				String name = rs.getString("name");
				int min = rs.getInt("minim");
				int max = rs.getInt("maxim");
				String description = rs.getString("description");
				String oh_item = rs.getString("oh_item");
				String prop_transformable_type = rs.getString("prop_type");
				String pval_transformable = rs.getString("prop_value");
				String pval_command = rs.getString("oh_command");
				
				if(!propertyTypes.containsKey(ssid)) {
					LOG.debug("Adding property type " + ssid + " (" + name + ") to repository!");
					PropertyType propType = new PropertyType(ssid, name, description, oh_item, min, max);
					propertyTypes.put(ssid, propType);
				}
				
				if(ssid.equals(prop_transformable_type) || propertyTypes.containsKey(prop_transformable_type)) {
					propertyTypes.get(prop_transformable_type).linkPropValueToOHCommand(pval_transformable, 
							pval_command);
				}
			}
			LOG.debug(propertyTypes.size() + " property types added!");
			rs.close();
		} catch (SQLException e) {
			LOG.fatal("Cannot retrieve property types from DB!", e);
		}
	}

	/**
	 * Adds a product object to the repository. <b>NOTE:</b> Product is added ONLY to the repository and does not
	 * handle the persistence of a product to the Symphony database.
	 * @param product The {@link Product} to be added
	 */
	public void addProduct(Product product) {
		products.put(product.getSSID(), product);
	}

	/**
	 * Checks if a product with the specified ID already exists in the repository.
	 * @param prodID the product ID to specify
	 * @return <b><i>true</i></b> if the product exists, <b><i>false</i></b> if not
	 */
	public boolean containsProduct(String prodID) {
		return products.containsKey(prodID);
	}

	/**
	 * Returns all the products in the repository
	 * @return an array containing all the product objects in the repository
	 */
	public Product[] getAllProducts() {
		return products.values().toArray(new Product[products.size()]);
	}

	/**
	 * Adds a property type object to the repository. <b>NOTE:</b> Property type is added ONLY to the repository and
	 * does not handle the persistence of a property type to the Symphony database.
	 * @param ptype The {@link PropertyType} to be added
	 */
	public void addPropertyType(PropertyType ptype) {
		propertyTypes.put(ptype.getSSID(), ptype);
	}

	/**
	 * Deletes the specified property type from the repository. <b>NOTE:</b> Property type is removed ONLY from the
	 * repository and does not handle the deletion of a property type from the Symphony database.
	 * @param ptypeID The property type ID to specify
	 * @return The deleted property type
	 */
	public PropertyType deletePropertyType(String ptypeID) {
		return propertyTypes.remove(ptypeID);
	}

	/**
	 * Checks if a property type with the specified ID already exists in the repository.
	 * @param ptypeID the property type ID to specify
	 * @return <b><i>true</i></b> if the property type exists, <b><i>false</i></b> if not
	 */
	public boolean containsPropertyType(String ptypeID) {
		return propertyTypes.containsKey(ptypeID);
	}

	/**
	 * Returns all the property types in the repository
	 * @return an array containing all the property type objects in the repository
	 */
	public PropertyType[] getAllPropertyTypes() {
		return propertyTypes.values().toArray(new PropertyType[propertyTypes.size()]);
	}
	
	/**
	 * Returns the product associated with the specified prodID
	 * @param prodID The product SSID
	 * @return The product object, <b><i>null</i></b> if product with specified prodID does not exist.
	 */
	public Product getProduct(String prodID) {
		return products.get(prodID);
	}
	
	/**
	 * Returns the property type associated with the specified ptypeID
	 * @param ptypeID The property type SSID
	 * @return The property type object, <b><i>null</i></b> if property type with specified ptypeID does not exist.
	 */
	public PropertyType getPropertyType(String ptypeID) {
		return propertyTypes.get(ptypeID);
	}
}
