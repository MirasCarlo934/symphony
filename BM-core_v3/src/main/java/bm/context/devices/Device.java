package bm.context.devices;

import java.util.HashMap;
import java.util.Iterator;

import bm.comms.Protocol;
import bm.context.properties.Property;
import bm.jeep.JEEPManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import bm.context.SymphonyObject;
import bm.context.adaptors.AbstAdaptor;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.products.Product;
import bm.context.rooms.Room;

/**
 * The Device object 
 * 
 * @author Carlo Miras
 *
 */
public class Device extends SymphonyObject {
	private final Logger LOG;
    private HashMap<Integer, Property> properties = new HashMap<Integer, Property>(1);
    private Product product;
    private String MAC;
    private String name;
    private String topic;
	private Protocol protocol;
    private boolean active;
    private String icon;
	private JEEPManager jm;
	
	public Device(String logDomain, String SSID, String MAC, String name, String topic, Protocol protocol, Room room,
                  boolean active, Product product, int index, JEEPManager jeepManager) {
		super(SSID, room, index);
		LOG = LogManager.getLogger(logDomain + ".DEV:" + SSID);
		this.setMAC((MAC));
		this.setName((name));
		this.setTopic((topic));
		this.setProduct((product));
		this.protocol = protocol;
		this.jm = jeepManager;
		this.active = active;
		this.icon = product.getIconImg();
		
		//clones properties of products and sets this device as the owner of the properties given to it
		Iterator<Property> props = product.getProperties().values().iterator();
		while(props.hasNext()) {
            Property prop = props.next().clone();
            prop.setDevice(this);
            properties.put(prop.getIndex(), prop);
//            System.out.println("Device" + getSSID() + ": " + prop.getSSID() + "-" + prop.getDevice().getSSID());
		}
	}
	
	/**
	 * Persists this component and its properties to the DB, OH, and all the various peripheral systems plugged in
	 * to this specific component. 
	 */
	@Override
	protected void createInAdaptor(AbstAdaptor adaptor, String callerLogDomain, boolean waitUntilCreated)
			throws AdaptorException {
		LOG.debug("Creating device " + SSID + " in " + adaptor.getName() + "...");
		adaptor.deviceCreated(this, waitUntilCreated);
        LOG.debug("Device " + SSID + " created in " + adaptor.getName() + "!");
	}

	@Override
    public void create(String callerLogDomain, boolean waitUntilCreated) throws AdaptorException {
	    super.create(callerLogDomain, waitUntilCreated);
        LOG.debug("Creating properties of device " + SSID + " in Environment...");
        Iterator<Property> props = properties.values().iterator();
        while(props.hasNext()) {
            Property prop = props.next();
            prop.create(callerLogDomain, waitUntilCreated);
            LOG.debug("Property " + prop.getSSID() + " of device " + SSID + " created!");
        }
        LOG.debug("Device " + SSID + " created!");
    }
	
	/**
	 * Deletes this component and its properties from the DB, OH, and all the various peripheral systems plugged in
	 * to this specific component. 
	 */
	@Override
	protected void deleteInAdaptor(AbstAdaptor adaptor, String callerLogDomain, boolean waitUntilDeleted)
			throws AdaptorException {
		LOG.debug("Deleting device " + SSID + " from " + adaptor.getName() + "...");
		adaptor.deviceDeleted(this, waitUntilDeleted);
		
		Iterator<Property> props = properties.values().iterator();
		LOG.debug("Deleting properties of device " + SSID + " from " + adaptor.getName() + "...");
		while(props.hasNext()) {
			Property prop = props.next();
			adaptor.propertyDeleted(prop, waitUntilDeleted);
			LOG.debug("Property " + prop.getSSID() + " of device " + SSID + " deleted!");
		}
		parentRoom.removeSmarthomeObject(this);
		LOG.debug("Device " + SSID + " deleted!");
	}
	
	/**
	 * Updates the device in all of the plugged adaptors.
	 */
	@Override
	protected void updateInAdaptor(AbstAdaptor adaptor, String callerLogDomain, boolean waitUntilUpdated)
			throws AdaptorException {
		LOG.debug("Updating device " + SSID + " in " + adaptor.getName() + "...");
		adaptor.deviceCredentialsUpdated(this, waitUntilUpdated);
		LOG.debug("Device " + SSID + " updated!");
	}
	
//	/**
//	 * Updates the component in all of the plugged adaptors. <br><br>
//	 * 
//	 * <b><i>NOTE:</i></b> Set the credentials in this component object <b>FIRST</b> before calling this method.
//	 */
//	@Override
//	public void updateRules(String parentLogDomain, boolean waitUntilUpdated) throws AdaptorException {
//		Logger LOG = getLogger(parentLogDomain);
//		LOG.debug("Updating component " + SSID + "...");
//		for(int i = 0; i < adaptors.length; i++) {
//			AbstAdaptor a = adaptors[i];
//			a.deviceCredentialsUpdated(this, waitUntilUpdated);
//		}
//		LOG.debug("Component " + SSID + " updated!");
//	}
	
//	@Override
//	public void updateRules(AbstAdaptor[] exceptions, String parentLogDomain, boolean waitUntilUpdated)
//			throws AdaptorException {
//		Logger LOG = getLogger(parentLogDomain);
//		LOG.debug("Updating component " + SSID + "...");
//		for(int i = 0; i < adaptors.length; i++) {
//			AbstAdaptor a = adaptors[i];
//			if(!a.equals(exceptions))
//				a.deviceCredentialsUpdated(this, waitUntilUpdated);
//		}
//		LOG.debug("Component " + SSID + " updated!");
//	}
	
//	@Override
//	public void updateExcept(Class[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
//			throws AdaptorException {
//		Logger LOG = getLogger(parentLogDomain);
//		LOG.debug("Updating component " + SSID + "...");
//		List<Class> excepts = Arrays.asList(exceptions);
//		for(int i = 0; i < adaptors.length; i++) {
//			AbstAdaptor a = adaptors[i];
//			if(!excepts.contains(a.getClass()))
//				a.deviceCredentialsUpdated(this, waitUntilUpdated);
//		}
//		LOG.debug("Component " + SSID + " updated!");
//	}
	
//	/**
//	 * Sends the credentials of this Device object to the actual device as a JEEPResponse. This is
//	 * typically only used by a Module object currently processing a JEEPRequest.
//     * @param request The JEEPRequest that needs responding to
//	 */
//	public void sendCredentials(JEEPRequest request) {
//		LOG.debug("Sending credentials to actual device...");
//		jm.sendRegistrationResponse(this, request);
////		LOG.debug("Credentials sent!");
//	}

	/**
	 * Sends the credentials of this Device object to the actual device as a JEEPRequest.
	 */
	public void sendCredentials() {
		LOG.debug("Sending credentials to actual device...");
		jm.sendRegistrationRequest(this);
//		LOG.debug("Credentials sent!");
	}

//    /**
//     * Sends a deactivation request to the actualdevice informing it that it is considered inactive in the
//     * Environment. <i>This method is ONLY called by the <b>ResponseManager</b> after the device fails to
//     * respond to a request sent to it.</i>
//     *
//     * @param message The deactivation message
//     */
//	public void sendDeactivationMessage(String message) {
//	    LOG.debug("Sending deactivation message to actual device...");
//	    jm.sendDeactivationRequest(this, message);
//    }

//	/**
//	 * Sends a detachment response to the device that sent a detachment request. This is
//	 *
//	 * typically only used by a Module object currently processing a JEEPRequest.
//	 * @param request The detachment request that needs responding to
//	 * @param success <b><i>true</i></b> if the request was processed successfully,
//	 *                <b><i>false</i></b> if not
//	 */
//	public void sendDetachmentResponse(JEEPRequest request, boolean success) {
//		LOG.debug("Sending detachment response to actual device...");
//		jm.sendDetachmentResponse(this, success, request);
//	}

	/**
	 * Sends a detachment JEEP message to the actual device stating the grounds of detachment.
	 * This method is called to inform the actual device that it will be detached from the Environment.
	 * Normally, the device will have to return an acknowledgement response but failure to do so
	 * will still detach it from the Environment. <i>Once detachment is undergone, Maestro will remove
	 * the device from its records no matter the response (or lack thereof).</i>
	 *
	 * @param message The detachment message
	 */
	public void detachDevice(String message) {
		LOG.debug("Sending detachment message to actual device...");
		jm.sendDetachmentRequest(this, message);
	}

//	/**
//	 * Sends a detachment message to the actual device.
//	 * @param message The message/grounds for detachment. Set to <b><i>null</i></b> if <i>isRequest</i> is set
//	 *                to false.
//	 * @param isRequest <b><i>True</i></b> if the credentials will be sent to the device as a request,
//	 *      			<b><i>false</i></b> if not. <i>NOTE: As a general rule of thumb, only Module objects set
//	 *      			this to false.</i>
//	 */
//	public void detachDevice(boolean success, JEEPRequest request) {
//		LOG.debug("Sending detachment message to actual device...");
//		jm.detachDevice(this, message, isRequest);
//		LOG.debug("Detachment message sent!");
//	}
	
//	/**
//	 * Publishes the values of this component's properties to the MQTT server. The values are returned in the form
//	 * of a POOP response JSON.
//	 *
//	 * @param poopRTY The RTY string for the POOP request
//	 * @param parentLogDomain The log domain of the object that called this method
//	 */
//	public void publishPropertyValues(Sender sender, String poopRTY, String parentLogDomain) {
//		Logger LOG = getLogger(parentLogDomain);
//		LOG.debug("Sending property states of " + SSID + " to " + topic + "...");
//		Iterator<Property> props = properties.values().iterator();
//		while(props.hasNext()) {
//			Property prop = props.next();
//			LOG.trace(prop.getSSID() + " = " + prop.getValue());
//			ResPOOP poop = new ResPOOP("POOP", SSID, poopRTY, protocol, prop.getSSID(), prop.getValue());
////			sender.send(poop);
//		}
//	}
	
	/**
	 * Converts a component into JSON format. Also includes the component's properties in the 
	 * conversion
	 * 
	 * @return An array of <i>JSONObjects</i> containing the <i>AbstComponent's</i> JSON 
	 * 		representation
	 */
//	@Override
//	public JSONObject[] convertToItemsJSON() {
//		if(getPropvals().length > 1) { //creates a group item if component has >1 properties
//			JSONObject json = new JSONObject();
//			json.put("type", "Group");
//			json.put("name", getSSID());
//			json.put("label", getName());
//			json.put("category", product.getOHIcon());
//			if(parentRoom != null)
//				json.put("groupNames", new String[]{getParentRoom().getSSID()});
//			return new JSONObject[]{json};
//		} else { //lets the single property define the component in registry
//			return new JSONObject[0];
//		}
//	}
//
//	@Override
//	public String convertToSitemapString() {
//		String itemType;
//		if(getPropvals().length > 1) {
//			itemType = "Group";
//		} else {
//			Property p = getPropvals()[0];
//			itemType = p.getOHItemType();
//		}
//		return itemType + " item=" + SSID + " [label=\"" + name + "\"] [icon=\"" + product.getOHIcon() + "\"]";
//	}


	@Override
	public String convertToJavascript() {
//		String s = "var d_" + SSID + " = new Device(\"" + SSID + "\", \"" + name + "\", \""
//				+ name + "\", ";		modified by cels due to duplicate name parameter Oct 24 2019
		String s = "var d_" + SSID + " = new Device(\"" + SSID + "\", \"" + name + "\", ";
		if(parentRoom != null) {
			s += "\"" + parentRoom.getName() + "\", ";
		}
		s += "[";
		Iterator<Property> props = properties.values().iterator();

		while(props.hasNext()) { //convert each property to format ex. {id:"0006",label:"Detected",io:"I"}
			Property prop = props.next();
			if(!(prop.getPropType().getSSID().equals("INN"))) {
				s += "{id:\"" + prop.getSSID() + "\","
						+ "label:\"" + prop.getDisplayName() + "\","
						+ "io:\"" + prop.getMode().toString() + "\",";
				if(!prop.getPropType().getSSID().equals("STR")) {
					s += "min:" + prop.getPropType().getMin() + ","
							+ "max:" + prop.getPropType().getMax() + ",";
				}
				s = s.substring(0, s.length() - 1) + "},"; //to chomp the last comma and add closing curly bracket
			}
		}
		s = s.substring(0, s.length() - 1) + "]);"; //to chomp the last comma and add closing var characters
		return s;
	}


	@Override
	public JSONObject[] convertToItemsJSON() {
		if(getProperties().length > 1) { //creates a group item if component has >1 properties
			JSONObject json = new JSONObject();
			json.put("type", "Group");
			json.put("name", getSSID());
			json.put("label", getName());
			json.put("category", icon);
			if(getParentRoom() != null)
				json.put("groupNames", new String[]{getParentRoom().getSSID()});
			return new JSONObject[]{json};
		} else { //lets the single property define the component in registry
			return new JSONObject[0];
		}
	}

	@Override
	public String convertToSitemapString() {
		String itemType;
		if(getProperties().length > 1) {
			itemType = "Group";
		} else {
			Property p = getProperties()[0];
			itemType = p.getOHItemType();
		}
		return itemType + " item=" + SSID + " [label=\"" + getName() + "\"] [icon=\"" +
				product.getIconImg() + "\"]";
	}
	
	/**
	 * Checks if the property with the specified PID exists in this component
	 * 
	 * @param pid The property ID
	 * @return <b>true</b> if the property exists, <b>false</b> otherwise
	 */
	public boolean containsProperty(int pid) {
		return properties.containsKey(pid);
	}
	
	/**
	 * Returns the property object with the specified property index.
	 * 
	 * @param index The index of the property
	 * @return The property, <b><i>null</i></b> if the property does not exist
	 */
	public Property getProperty(int index) {
		return properties.get(index);
	}

	/**
	 * Returns the property object with the specified property SSID.
	 *
	 * @param ssid The index of the property
	 * @return The property, <b><i>null</i></b> if the property does not exist
	 */
	public Property getProperty(String ssid) {
		properties.forEach((key,value) -> LOG.info(" ====test==== key="+key + " SSID " + value.getSSID()+" finding:"+ssid));
		for(Property prop : properties.values()) {
			LOG.info(" ====test====  prop.getSSID=" + prop.getSSID()+" finding:"+ssid);
			if(prop.getSSID().equals(ssid)) {
				LOG.info(" ====test==== found SSID=" + prop.getSSID()+" finding:"+ssid);
				return prop;
			}
		}
		return null;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

//    /**
//     * Sets this Device object active in the Environment. Also sends a deactivation request to the actual
//     * device if parameter active is set to false.
//     * @param active
//     * @param callerLogDomain
//     * @param waitUntilUpdated
//     * @throws AdaptorException
//     */
//	public void setActive(boolean active, String callerLogDomain, boolean waitUntilUpdated)
//            throws AdaptorException {
//	    this.active = active;
//	    update(callerLogDomain, waitUntilUpdated);
//	    sendCredentials();
//    }

	/**
	 * Sets the active property of this Device object.
	 * 
	 * @param active <i><b>true</b></i> if the device is active, <i><b>false</b></i> if not
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
//	/**
//	 * Sets the active state of this Component and persists it to the DB.
//	 *
//	 * @param active <b>true</b> if the Component is active, <b>false</b> if not
//	 * @param waitUntilActivated
//	 * @throws EngineException thrown when persistence fails
//	 * @throws InterruptedException thrown when Thread.wait() fails
//	 */
//	public void setActive(boolean active, /*String parentLogDomain, */boolean waitUntilActivated) throws AdaptorException {
////		Logger LOG = getLogger(parentLogDomain);
////		LOG.debug("Setting active state to " + active);
//		this.active = active;
//		for(int i = 0; i < adaptors.size(); i++) {
//			adaptors.get(i).deviceStateUpdated(this, waitUntilActivated);
//		}
//	}
	
//	@Override
//	public void setIndex(int index) throws AdaptorException {
//		for(int i = 0; i < adaptors.length; i++) {
//			adaptors[i].deviceCredentialsUpdated(this, true);
//		}
//	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMAC() {
		return MAC;
	}

	public void setMAC(String mAC) {
		MAC = mAC;
	}

	public String getTopic() {
		return topic;
	}

    /**
     * Returns the sender for the protocol in which this device communicates in.
     * @return The Sender object.
     */
    public Protocol getProtocol() {
        return protocol;
    }

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Property[] getProperties() {
		return properties.values().toArray(new Property[properties.size()]);
	}

	public void setProperties(HashMap<Integer, Property> properties) {
    	for(Property p : properties.values()) {
    		p.setDevice(this);
		}
		this.properties = properties;
	}

	public void addProperty(Property property) {
    	property.setDevice(this);
    	properties.put(property.getIndex(), property);
	}

	@Override
	public void addAdaptor(AbstAdaptor adaptor) {
		super.addAdaptor(adaptor);
		for (Property property : properties.values()) {
            property.addAdaptor(adaptor);
		}
	}

	@Override
    public void setAdaptors(AbstAdaptor[] adaptors) {
        super.setAdaptors(adaptors);
        for(Property property : properties.values()) {
            property.setAdaptors(adaptors);
        }
    }

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}
}
