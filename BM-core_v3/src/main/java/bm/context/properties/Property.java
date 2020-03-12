package bm.context.properties;

import bm.jeep.JEEPManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import bm.context.HTMLTransformable;
import bm.context.OHItemmable;
import bm.context.SymphonyElement;
import bm.context.adaptors.AbstAdaptor;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.tools.IDGenerator;

/**
 * A Java-object representation of a real-world device property. 
 * @author carlomiras
 *
 */
public class Property extends SymphonyElement implements OHItemmable, HTMLTransformable {
	private Device parentDevice;
    private String loggerName;
//    private String genericName;
    private String displayName;
//    private String systemName; //[prop_type]-[prop_mode]-[cpl_SSID]
    private PropertyMode mode;
    private PropertyType propType;
    private Object value = "0";
    private Object previousValue = "0";

	private JEEPManager jm;

	//TASK add index to constructor params
	public Property(PropertyType propType, int index, /*String genericName, */String dispname,
					/*String ohItemType,*/ PropertyMode mode, /*PropertyValueType propValType,*/
					JEEPManager jeepManager) {
		super(IDGenerator.generateIntID(4, new String[0]) + "_" + index, index);
		this.displayName = (dispname);
//		this.genericName = propType.getName() + "-" + mode.toString();
//		this.setSystemName(genericName, index);
		this.mode = (mode);
		this.propType = propType;
		this.jm = jeepManager;
	}

	@Override
	public Property clone() {
		Property p = new Property(propType, getIndex(), displayName, mode, jm);
		p.setAdaptors(p.getAdaptors());
		return p;
	}
	
	/**
	 * Checks if the specified value is valid for this property
	 * @param value The value to be checked
	 * @return <b><i>true</i></b> if value is valid, <b><i>false</i></b> otherwise
	 */
//	public abstract boolean checkValueValidity(Object value);
	public boolean checkValueValidity(Object value) {
		return propType.checkValueTypeValidity(value);
	}
	
	/**
	 * Transforms the value of this property into a String which OpenHAB can recognize as a command 
	 * for the item that represents this property. 
	 * 
	 * @return the transformed value
	 */
//	public abstract String transformValueToOHCommand();
	public String transformValueToOHCommand() {
//		System.out.println("COMMAND: " + propType.transformPropValueToOHCommand(value.toXML()));
		return propType.transformPropValueToOHCommand(value.toString());
	}
	
	/**
	 * Persists this property to the DB, OH, and all the various peripheral systems plugged in to this specific
	 * property.
	 */
	@Override
	protected void createInAdaptor(AbstAdaptor adaptor, String callerLogDomain, boolean waitUntilCreated)
			throws AdaptorException {
		final Logger LOG = getLogger(callerLogDomain);
		if(!propType.getOHIcon().equals("none")) {
			LOG.debug("Creating property " + getSSID() + " in " + adaptor.getName() + "...");
			adaptor.propertyCreated(this, waitUntilCreated);
			LOG.debug("Property " + getSSID() + " created!");
		}
	}
	
//	@Override
//	protected void create(String parentLogDomain, boolean waitUntilCreated)
//			throws AdaptorException {
//		final Logger LOG = getLogger(parentLogDomain);
//		List<AbstAdaptor> except = Arrays.asList(exceptions);
//		if(!propType.getOHIcon().equals("none")) {
//			LOG.debug("Persisting property " + getStandardID() + "...");
//			for(int i = 0; i < adaptors.length; i++) {
//				if(!except.contains(adaptors[i])) {
//					adaptors[i].propertyCreated(this, waitUntilPersisted);
//				}
//			}
//			LOG.debug("B_Property " + getStandardID() + " persisted!");
//		}
//	}

	@Override
	protected void deleteInAdaptor(AbstAdaptor adaptor, String callerLogDomain, boolean waitUntilDeleted)
			throws AdaptorException {
		Logger LOG = getLogger(callerLogDomain);
		if(!propType.getOHIcon().equals("none")) {
			LOG.debug("Deleting property " + getSSID() + " from " + adaptor.getName() + "...");
			adaptor.propertyDeleted(this, waitUntilDeleted);
			LOG.debug("Property " + getSSID() + " deleted!");
		}
	}

	@Override
	protected synchronized void updateInAdaptor(AbstAdaptor adaptor, String callerLogDomain, boolean waitUntilUpdated)
            throws AdaptorException {
		if(!propType.getOHIcon().equals("none")) {
			Logger LOG = getLogger(callerLogDomain);
			LOG.debug("Updating value of property " + getSSID() + " in " + adaptor.getName() + 
					"...");
			adaptor.propertyValueUpdated(this, waitUntilUpdated);
			LOG.debug("Property " + getSSID() + " updated!");
		}
	}

//	/**
//	 * Updates this property in the Environment.
//	 * @param callerLogDomain the log domain of the object that called this method
//	 * @param waitUntilUpdated
//	 * @throws AdaptorException
//	 */
//	@Override
//    public void update(String callerLogDomain, boolean waitUntilUpdated) throws AdaptorException {
//        super.update(callerLogDomain, waitUntilUpdated);
//    }

	/**
	 * Sends a POOP request declaring the value of this property to its device.
	 * @param callerLogDomain The log domain of the object that called this method
	 */
	public void sendValueToDevice(String callerLogDomain) {
	    Logger LOG = getLogger(callerLogDomain);
	    LOG.debug("Sending property value to device...");
		jm.sendPOOPRequest(this);
        LOG.debug("Property value sent!");
    }

	@Override
	public JSONObject[] convertToItemsJSON() {
		JSONObject json = new JSONObject();
		json.put("name", getSSID());
		json.put("type", propType.getOHIcon());
		if(parentDevice.getProperties().length > 1) {
			json.put("groupNames", new String[]{parentDevice.getSSID()});
			json.put("label", displayName);
		} else {
			json.put("groupNames", new String[]{parentDevice.getParentRoom().getSSID()});
			json.put("label", parentDevice.getName());
			json.put("category", parentDevice.getProduct().getIconImg());
		}
		return new JSONObject[]{json};
	}

	@Override
	public String convertToSitemapString() {
		return null;
	}

	/**
	 * Returns a javascript object named "Property" with the format.
	 * "new B_Property('[propType ID]', '[display name]', '[mode]', '[property value]')";
	 */
	@Override
	public String convertToJavascript() {
		String str = "new Property('" + propType.getSSID() + "', '" + displayName + "', '" + mode.toString() + "', '"
				+ value.toString() + "')";
		return str;
	}

	/**
	 * Returns the value of this <i>Property</i>.
	 * @return The value of this <i>Property</i>.
	 */
	public Object getValue() {
		return value;
	}

    /**
     * Returns the previous value of this property
     * @return The previous value
     */
	public Object getPreviousValue() {
	    return previousValue;
    }

	/**
	 * Sets the value of this property.
	 * @param value The value of the Property to be set
	 */
	public void setValue(Object value) {
	    this.previousValue = this.value;
		this.value = value;
	}
	
//	/**
//	 * Sets the value of this property in this object and calls the external application adaptors to
//	 * handle this property value change. This method must only be called when the value change did
//	 * not come from a JEEP request (ie. CIR)
//	 * @param value The value of the property to be set
//	 * @param parentLogDomain The log4j logging domain used by the Object that invokes this method
//	 * @param waitUntilUpdated <b><i>true</i></b> if thread must be set to wait until the adaptor/s have
//	 * 			completed processing, <b><i>false</i></b> if thread will not be set to wait.
//	 * @throws AdaptorException
//	 */
//	public void setValue(Object value, String parentLogDomain, boolean waitUntilUpdated)
//			throws AdaptorException {
//		setValue(value, null, parentLogDomain, waitUntilUpdated);
//		this.value = value;
//	}
//
//	/**
//	 * Sets the value of this property in this object and calls the external application adaptors to
//	 * handle this property value change.
//	 * @param value The value of the property to be set
//	 * @param cid The CID of the component that sent the request to change the property value
//	 * @param parentLogDomain The log4j logging domain used by the Object that invokes this method
//	 * @param waitUntilUpdated <b><i>true</i></b> if thread must be set to wait until the adaptor/s have
//	 * 			completed processing, <b><i>false</i></b> if thread will not be set to wait.
//	 * @throws AdaptorException
//	 */
//	public void setValue(Object value, String cid, String parentLogDomain, boolean waitUntilUpdated)
//			throws AdaptorException {
//		Logger LOG = getLogger(parentLogDomain);
//		LOG.debug("Setting value of property " + getSSID() + " to " + value + "...");
//		setValue(value);
//		if(cid == null)
//			update(parentLogDomain, waitUntilUpdated);
//		else
//			updateExcept(new String[]{cid}, parentLogDomain, waitUntilUpdated);
//	}

//	/**
//	 * Sets the value of this property. Persists this property's value to all adaptors with exceptions.
//	 * 
//	 * @param value The value of the property to be set
//	 * @param exceptions the adaptor classes where this object <b>WILL NOT</b> be persisted to
//	 * @param parentLogDomain The log4j logging domain used by the Object that invokes this method
//	 * @throws Exception 
//	 * @throws HTTPException 
//	 */
//	public void setValue(Object value, Class[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
//			throws AdaptorException {
//		Logger LOG = getLogger(parentLogDomain);
//		LOG.debug("Setting value of property " + getStandardID() + " to " + value + "...");
//		setValue(value);
//		updateExcept(exceptions, parentLogDomain, waitUntilUpdated);
//	}
	
	/**
	 * Returns the display name of this B_Property. Used primarily in OpenHAB and other UI.
	 * 
	 * @return the display name of this B_Property
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Returns the mode of this B_Property. Usually denotes if the property is an input or output.
	 * 
	 * @return the PropertyMode that represents the mode of this property
	 */
	public PropertyMode getMode() {
		return mode;
	}


//	/**
//	 * Returns the system name of this B_Property. <br><br>
//	 *
//	 * <b>Construction:</b><br>
//	 * [<i>genericName</i>]-[<i>SSID</i>]
//	 *
//	 * @return the system name of this B_Property
//	 */
//	public String getSystemName() {
//		return systemName;
//	}

//	public String getCommonName() {
//	    return parentDevice.getSSID() + "-" + SSID;
//    }
//
//	/**
//	 * @param systemName the systemName to set
//	 * @param index the index set in table COMPROPLIST
//	 */
//	private void setSystemName(String systemName, String index) {
//		this.systemName = systemName + "-" + index;
//	}

	/**
	 * Returns the data type of the value held by this B_Property.
	 * 
	 * @return the PropertyValueType that represents the data type of the value held by this B_Property
	 */
//	public PropertyValueType getPropValType() {
//		return propValType;
//	}

	/**
	 * Returns the property type of this B_Property denoted in PROPCAT table
	 * 
	 * @return the property type of this B_Property
	 */
	public PropertyType getPropType() {
		return propType;
	}

	/**
	 * Returns the component that owns this property
	 * 
	 * @return the AbstComponent object that owns this property
	 */
	public Device getDevice() {
		return parentDevice;
	}
	
	public void setDevice(Device device) {
		this.parentDevice = device;
		setSSID(device.getSSID() + "_" + getIndex());
		loggerName = getSSID();
//		loggerName = getSSID();
	}
	
//	/**
//	 * Returns the standard ID for this property which is defined as <b>[CID]_[SSID]</b>. This is 
//	 * commonly used in OpenHAB.
//	 * 
//	 * @return the standard ID of this property
//	 */
//	public String getSSID() {
//		return parentDevice.getSSID() + "_" + SSID;
//	}
	
	public String getOHItemType() {
		return propType.getOHIcon();
	}
	
	protected Logger getLogger(String parentLogDomain) {
		return LogManager.getLogger(parentLogDomain + "." + loggerName);
	}
}
