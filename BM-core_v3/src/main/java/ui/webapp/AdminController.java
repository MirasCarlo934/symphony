package ui.webapp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import bm.context.products.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import bm.context.properties.PropertyMode;
import bm.context.properties.PropertyType;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.DeleteDBEReq;
import bm.main.engines.requests.DBEngine.InsertDBEReq;
import bm.main.engines.requests.DBEngine.UpdateDBEReq;
import bm.main.repositories.ProductRepository;
import bm.tools.IDGenerator;

@Controller
@RequestMapping("/admin")
public class AdminController extends AbstController {
	@Autowired
	private IDGenerator idg;
	@Autowired
	private ProductRepository pr;
	@Autowired
	@Qualifier("DBManager")
	private DBEngine dbe;
	private String pvalTable;
	private String productTable;
	private String comproplistTable;

	public AdminController(@Value("${log.domain.ui}") String logDomain,
			@Value("${table.propertyvalues}") String pvalTable, 
			@Value("${table.products}") String productTable, 
			@Value("${table.productproperties}") String comproplistTable) {
		super(logDomain, AdminController.class.getSimpleName());
		this.pvalTable = pvalTable;
		this.productTable = productTable;
		this.comproplistTable = comproplistTable;
//		idg = (IDGenerator)config.getApplicationContext().getBean(idgStr);
//		pr = (ProductRepository)config.getApplicationContext().getBean(prStr);
//		dbe = (DBEngine)config.getApplicationContext().getBean(dbeStr);
	}
	
	/**
	 * Displays the product management page
	 * 
	 * @param model The current MVC model, supplied by Spring MVC
	 * @return
	 */
	@RequestMapping("/products")
	public String products(Model model) {
		Product[] prods = pr.getAllProducts();
		PropertyType[] ptypes = pr.getAllPropertyTypes();
//		Vector<PropertyType> ptypes = new Vector<PropertyType>(10,1);
		String prodsJSArray = "[";
		String ptypesJSArray = "[";
		
		//sort products and ptypes by ascending order
		Vector<Product> sortedProds = new Vector<Product>(prods.length);
		int[] prodSSIDs = new int[prods.length];
			for(int i = 0; i < prods.length; i++) {
				prodSSIDs[i] = Integer.parseInt(prods[i].getSSID());
			}
			Arrays.sort(prodSSIDs);
			for(int i = 0; i < prodSSIDs.length; i++) {
				int ssid = prodSSIDs[i];
				Product prod = null;
				for(int j = 0; j < prods.length; j++) {
					if(Integer.parseInt(prods[j].getSSID()) == ssid) prod = prods[j];
				}
				sortedProds.add(prod);
				prodsJSArray += prod.convertToJavascript() + ",";
			}
		Vector<PropertyType> sortedPtypes = new Vector<PropertyType>(ptypes.length);
		String[] ptypeSSIDs = new String[ptypes.length];
			for(int i = 0; i < ptypes.length; i++) {
				ptypeSSIDs[i] = ptypes[i].getSSID();
			}
			Arrays.sort(ptypeSSIDs);
			for(int i = 0; i < ptypeSSIDs.length; i++) {
				String ssid = ptypeSSIDs[i];
				PropertyType ptype = null;
				for(int j = 0; j < ptypes.length; j++) {
					if(ptypes[j].getSSID().equals(ssid)) ptype = ptypes[j];
				}
				sortedPtypes.add(ptype);
				ptypesJSArray += ptype.convertToJavascript() + ",";
			}
		
		prodsJSArray = prodsJSArray.substring(0, prodsJSArray.length() - 1) + "];";
		ptypesJSArray = ptypesJSArray.substring(0, ptypesJSArray.length() - 1) + "];";
		model.addAttribute("products", sortedProds.toArray(new Product[prods.length]));
		model.addAttribute("productsJS", prodsJSArray);
		model.addAttribute("propertyTypes", sortedPtypes.toArray(new PropertyType[ptypes.length]));
		model.addAttribute("propertyTypesJS", ptypesJSArray);
		return "admin/products";
	}
	
	@RequestMapping("editProduct")
	public String editProduct(@RequestParam(value="ssid", required=true) String ssid, 
			@RequestParam(value="newSSID", required=true) String newSSID, 
			@RequestParam(value="name", required=true) String name, 
			@RequestParam(value="description", required=true) String description, 
			@RequestParam(value="oh_icon", required=true) String oh_icon, 
			@RequestParam HashMap<String, String> params, Model model) {
		LOG.debug("Product modification requested!");
		Product prod = pr.getProduct(ssid);
		if(prod == null) {
			LOG.error("Product ID " + ssid + " does not exist");
			return notify(null, "Product ID " + ssid + " does not exist", model);
		}

		LOG.info("Modifying product " + ssid + " (" + prod.getName() + ")...");
		if(newSSID.length() == 0 || newSSID == null) {
			newSSID = ssid;
		} else if(pr.getProduct(newSSID) != null) {
			LOG.error("Specified SSID already exists!");
			return notify(null, "Specified SSID already exists!", model);
		}
		if(name.length() == 0 || name == null) {
			name = prod.getName();
		} else LOG.info("Changing product name to " + name);
		if(description.length() == 0 || description == null) {
			description = prod.getDescription();
		} else LOG.info("Changing product description to " + description);
		if(oh_icon.length() == 0 || oh_icon == null) {
			oh_icon = prod.getIconImg();
		} else LOG.info("Changing product OpenHAB icon to " + oh_icon);
		
		//updateRules product object
		prod.setSSID(newSSID);
		prod.setName(name);
		prod.setDescription(description);
		prod.setIconImg(oh_icon);
		
		//updateRules DB
		HashMap<String, Object> vals = new HashMap<String, Object>(4);
		HashMap<String, Object> args = new HashMap<String, Object>(1);
		vals.put("SSID", newSSID);
		vals.put("NAME", name);
		vals.put("DESCRIPTION", description);
		vals.put("OH_ICON", oh_icon);
		args.put("SSID", ssid);
		UpdateDBEReq dber1 = new UpdateDBEReq(idg.generateERQSRequestID(), dbe, productTable, vals, args);
		try {
			dbe.putRequest(dber1, Thread.currentThread(), true);
		} catch (EngineException e) {
			LOG.error("Cannot updateRules product!", e);
			return notify(null, "Cannot updateRules product! Check BM logs for more info.", model);
		}
		LOG.info("Product modified!");
		return notify(null, "Product modified!", model);
	}
	
	@RequestMapping("createProduct")
	public String createProduct(@RequestParam HashMap<String, String> requestParams, Model model) {
		final class PropertyParams {
			PropertyType proptype;
			String propname;
			PropertyMode propmode;
			private PropertyParams(String proptype, String propname, String propmode) {
				this.proptype = pr.getPropertyType(proptype);
				this.propmode = PropertyMode.parseFromString(propmode);
				this.propname = propname;
			}
		}
		
		LOG.debug("Create product requested!");
		String ssid = requestParams.get("newSSID");
		String name = requestParams.get("name");
		String description = requestParams.get("description");
		String oh_icon = requestParams.get("oh_icon");
		Vector<PropertyParams> props = new Vector<PropertyParams>(1,1);
		//parameter checking
		String error = null;
		if(ssid == null || ssid.equals("")) error = "SSID";
		else if(name == null || name.equals("")) error = "Name";
		else if(description == null || description.equals("")) error = "Description";
		else if(oh_icon == null || oh_icon.equals("")) error = "OpenHAB Icon";
		if(error != null) {
			error += " is empty!";
			LOG.error(error);
			model.addAttribute("status", false);
			return notifyError(error, model);
		}
		
		int propsfields = 0;
		Iterator<String> params = requestParams.keySet().iterator();
		while(params.hasNext()) {
			String param = params.next();
			if(param.contains("propname") || param.contains("proptype") || param.contains("propmode")) {
				int max = Integer.parseInt(param.substring(8));
				if(max > propsfields) propsfields = max;
			}
		}
		for(int i = 1; i <= propsfields; i++) {
			String pname = requestParams.get("propname" + i);
			String ptype = requestParams.get("proptype" + i);
			String pmode = requestParams.get("propmode" + i);
			if((pname == null || pname.equals("")) && (ptype != null)) {
				LOG.error("Empty property name!");
				model.addAttribute("status", false);
				return notifyError("Empty property name!", model);
			} else if((pname == null || pname.equals("")) && (ptype == null)) continue;
			else {
				props.add(new PropertyParams(ptype, pname, pmode));
			}
		}
		
		LOG.debug("Inserting new product " + ssid + " (" + name + ") to DB...");
		HashMap<String, Object> prodVals = new HashMap<String, Object>(4);
		Vector<InsertDBEReq> compropReqs = new Vector<InsertDBEReq>(props.size());
		prodVals.put("SSID", ssid);
		prodVals.put("NAME", name);
		prodVals.put("DESCRIPTION", description);
		prodVals.put("OH_ICON", oh_icon);
		InsertDBEReq dber1 = new InsertDBEReq(idg.generateERQSRequestID(), dbe, productTable, prodVals);
		for(int i = 0; i < props.size(); i++) {
			PropertyParams p = props.get(i);
			HashMap<String, Object> compropVals = new HashMap<String, Object>(4);
			compropVals.put("COM_TYPE", ssid);
			compropVals.put("DISP_NAME", p.propname);
			compropVals.put("SSID", idg.generateIntID(4, new String[0]));
			compropVals.put("INDEX", i + 1);
			compropVals.put("BINDING", null);
			compropVals.put("PROP_TYPE", p.proptype.getSSID());
			compropVals.put("PROP_MODE", p.propmode.toString());
			InsertDBEReq dber = new InsertDBEReq(idg.generateERQSRequestID(), dbe, comproplistTable, compropVals);
			compropReqs.add(dber);
		}
		try {
			dbe.putRequest(dber1, Thread.currentThread(), true);
			for(int i = 0; i < compropReqs.size(); i++) {
				dbe.putRequest(compropReqs.get(i), Thread.currentThread(), true);
			}
		} catch(EngineException e) {
			LOG.error("Error in persisting new product!", e);
			model.addAttribute("status", false);
			return notifyError("Error in persisting product to DB! Check BM logs for more info.", model);
		}
		
		LOG.debug("Adding new product to ProductRepository...");
		pr.retrieveProductsFromDB();
		
		model.addAttribute("status", true);
		LOG.info("Product " + ssid + " (" + name + ") created!");
		return notify(null, "Product created!", model);
	}
	
	@RequestMapping("editPropertyTypessss")
	public String test(@RequestParam(value="ssid", required=true) String ssid, 
			@RequestParam(value="newSSID", required=true) String newSSID, 
			@RequestParam(value="name", required=true) String name, 
			@RequestParam(value="description", required=true) String description, 
			@RequestParam(value="oh_icon", required=true) String oh_icon, 
			Model model) {
		LOG.fatal("DEAD");
		return "home";
	}
	
	@RequestMapping("editPropertyType")
	public String editPropertyType(@RequestParam(value="ssid", required=true) String ssid, 
			@RequestParam(value="newSSID", required=true) String newSSID, 
			@RequestParam(value="name", required=true) String name, 
			@RequestParam(value="description", required=true) String description, 
			@RequestParam(value="oh_icon", required=true) String oh_icon, 
			@RequestParam(value="min", required=true) String min, 
			@RequestParam(value="max", required=true) String max, Model model) {
		LOG.debug("Property type modification requested!");
		PropertyType ptype = pr.getPropertyType(ssid);
		if(ptype == null) {
			LOG.error("Property type " + ssid + " does not exist");
			return notify(null, "Product ID " + ssid + " does not exist", model);
		}
		try {
			Integer.parseInt(min);
			Integer.parseInt(max);
		} catch(NumberFormatException e) {
			LOG.error("Min/max specified is not an integer!");
			return notifyError("Min/max specified is not an integer!", model);
		}

		LOG.info("Modifying property type " + ssid + " (" + ptype.getName() + ")...");
		if(newSSID.length() == 0 || newSSID == null) {
			newSSID = ssid;
		} else if(pr.getPropertyType(newSSID) != null) {
			LOG.error("Specified SSID already exists!");
			return notify(null, "Specified SSID already exists!", model);
		}
		if(name.length() == 0 || name == null) {
			name = ptype.getName();
		} else LOG.info("Changing property type name to " + name);
		if(description.length() == 0 || description == null) {
			description = ptype.getDescription();
		} else LOG.info("Changing property type description to " + description);
		if(oh_icon.length() == 0 || oh_icon == null) {
			oh_icon = ptype.getOHIcon();
		} else LOG.info("Changing property type OpenHAB icon to " + oh_icon);
		if(min.length() == 0 || min == null) {
			min = String.valueOf(ptype.getMin());
		} else LOG.info("Changing property type min value to " + min);
		if(max.length() == 0 || max == null) {
			max = String.valueOf(ptype.getMax());
		} else LOG.info("Changing property type max value to " + max);
		
		//updateRules product object
		ptype.setSSID(newSSID);
		ptype.setName(name);
		ptype.setDescription(description);
		ptype.setOHIcon(oh_icon);
		ptype.setMin(Integer.parseInt(min));
		ptype.setMax(Integer.parseInt(max));
		
		//updateRules DB
		HashMap<String, Object> vals = new HashMap<String, Object>(4);
		HashMap<String, Object> args = new HashMap<String, Object>(1);
		vals.put("SSID", newSSID);
		vals.put("NAME", name);
		vals.put("DESCRIPTION", description);
		vals.put("OH_ITEM", oh_icon);
		vals.put("MINIM", Integer.parseInt(min));
		vals.put("MAXIM", Integer.parseInt(max));
		args.put("SSID", ssid);
		UpdateDBEReq dber1 = new UpdateDBEReq(idg.generateERQSRequestID(), dbe, pvalTable, vals, args);
		try {
			dbe.putRequest(dber1, Thread.currentThread(), true);
		} catch (EngineException e) {
			LOG.error("Cannot updateRules property type!", e);
			return notify(null, "Cannot updateRules property type! Check BM logs for more info.", model);
		}
		LOG.info("Property type modified!");
		return notify(null, "Property type modified!", model);
	}
	
	@RequestMapping("createPropertyType")
	public String createPropertyType(@RequestParam(value="ssid", required=true) String ssid, 
			@RequestParam(value="name", required=true) String name, 
			@RequestParam(value="description", required=true) String description, 
			@RequestParam(value="oh_icon", required=true) String oh_icon, 
			@RequestParam(value="min", required=true) String min, 
			@RequestParam(value="max", required=true) String max, Model model) {
		LOG.debug("Create property type requested!");
		if(pr.getPropertyType(ssid) != null) {
			LOG.error("SSID already exists!");
			model.addAttribute("status", false);
			return notify(null, "Product ID " + ssid + " does not exist", model);
		}
		
		//checks for errors in parameters
		int minim;
		int maxim;
		String error = null;
		if(ssid == null || ssid.equals("")) error = "SSID";
		else if(name == null || name.equals("")) error = "Name";
		else if(description == null || description.equals("")) error = "Description";
		else if(oh_icon == null || oh_icon.equals("")) error = "OpenHAB Icon";
		if(error != null) {
			error += " is empty!";
			LOG.error(error);
			model.addAttribute("status", false);
			return notifyError(error, model);
		}
		try {
			minim = Integer.parseInt(min);
			maxim = Integer.parseInt(max);
		} catch(NumberFormatException e) {
			LOG.error("Min/max specified is not an integer!");
			model.addAttribute("status", false);
			return notifyError("Min/max specified is not an integer!", model);
		}
		
		LOG.debug("Persisting new property type " + ssid + " '" + name + "'" );
		PropertyType ptype = new PropertyType(ssid, name, description, oh_icon, minim, maxim);
		pr.addPropertyType(ptype);
		HashMap<String, Object> values = new HashMap<String, Object>(6);
		values.put("SSID", ssid);
		values.put("NAME", name);
		values.put("DESCRIPTION", description);
		values.put("OH_ITEM", oh_icon);
		values.put("MINIM", minim);
		values.put("MAXIM", maxim);
		InsertDBEReq dber1 = new InsertDBEReq(idg.generateERQSRequestID(), dbe, pvalTable, values);
		try {
			dbe.putRequest(dber1, Thread.currentThread(), false);
		} catch (EngineException e) {
			LOG.error("Cannot persist property type to database!", e);
			model.addAttribute("status", false);
			return notifyError("Cannot persist property type to database! Check BM logs for more info.", model);
		}
		
		model.addAttribute("status", true);
		LOG.info("New property type " + ssid + " '" + name + "' created!");
		return notify(null, "Property type created!", model);
	}
	
	@RequestMapping("deletePropertyType")
	public String deletePropertyType(@RequestParam(value="ssid", required=true) String ssid, Model model) {
		LOG.debug("Delete property type requested!");
		if(pr.getPropertyType(ssid) == null) {
			LOG.error("Invalid SSID!");
			model.addAttribute("status", false);
			return notifyError("Invalid SSID!", model);
		}
		
		PropertyType ptype = pr.getPropertyType(ssid);
		HashMap<String, Object> args = new HashMap<String, Object>(1);
		args.put("SSID", ssid);
		DeleteDBEReq dber1 = new DeleteDBEReq(idg.generateERQSRequestID(), dbe, pvalTable, args);
		try {
			dbe.putRequest(dber1, Thread.currentThread(), true);
			pr.deletePropertyType(ssid);
		} catch (EngineException e) {
			LOG.error("Cannot delete property type " + ssid, e);
			model.addAttribute("status", false);
			return notifyError("Cannot delete property type " + ssid + ". Check BM logs for more info.", model);
		}
		model.addAttribute("status", true);
		LOG.info("Property type " + ssid + " (" + ptype.getName() + ") deleted!");
		return notify(null, "Property type deleted!", model);
	}
}
