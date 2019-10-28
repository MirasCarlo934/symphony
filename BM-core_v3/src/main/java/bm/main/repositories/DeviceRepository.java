package bm.main.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import bm.comms.Protocol;
import bm.context.properties.Property;
import bm.context.properties.PropertyMode;
import bm.context.properties.PropertyType;
import bm.jeep.JEEPManager;
import org.apache.log4j.Logger;

import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.main.engines.DBEngine;
import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.DBEngine.RawDBEReq;
import bm.main.interfaces.Initializable;
import bm.tools.IDGenerator;

/**
 * The DeviceRepository is the container for all the devices that are registered within the Symphony Environment.
 * Only one DeviceRepository object must exist in the Symphony Environment.
 */
public class DeviceRepository /*extends AbstRepository*/ implements Initializable {
	private Logger LOG;
	private String logDomain;
	private HashMap<String, String> rooms = new HashMap<String, String>(1,1);
	private HashMap<String, Device> devices = new HashMap<String, Device>(1);
	private HashMap<String, String> registeredMACs = new HashMap<String, String>(1,1); //registered MAC and corresponding SSID
	private IDGenerator idg;
	private DBEngine mainDBE;
	private String deviceQuery;
	private ProductRepository pr;
	private RoomRepository rr;
	private HashMap<String, Protocol> protocols;
	private JEEPManager jm;

	/**
	 * Constructs the DeviceRepository. The DeviceRepository is initialized by {@link bm.main.Maestro Maestro} in
	 * the startup phase.
	 * @param logDomain
	 * @param dbm
	 * @param deviceQuery
	 * @param pr
	 * @param rr
	 * @param idGenerator
	 */
	public DeviceRepository(String logDomain, DBEngine dbm, String deviceQuery,
			ProductRepository pr, RoomRepository rr, IDGenerator idGenerator) {
		this.LOG = Logger.getLogger(logDomain + "." + DeviceRepository.class.getSimpleName());
		this.logDomain = logDomain;
		this.deviceQuery = deviceQuery;
		this.mainDBE = dbm;
		this.pr = pr;
		this.rr = rr;
		this.idg = idGenerator;
	}
	
	@Override
	public void initialize() throws Exception {
		retrieveDevicesFromDB();
		updateDevicesInEnvironment();
	}
	
	/**
	 * Retrieves all registered devices from the Symphony Database. This method is ONLY USUALLY called by the
	 * {@link bm.main.Maestro Maestro} in the startup phase.
	 */
	public void retrieveDevicesFromDB() {
		try {
			LOG.info("Retrieving devices from DB...");
			RawDBEReq dber1 = new RawDBEReq(idg.generateERQSRequestID(), mainDBE, deviceQuery);
			Object o;
			try {
				o = mainDBE.putRequest(dber1, Thread.currentThread(), true);
			} catch (EngineException e) {
				LOG.error("Error in retrieving components from DB!", e);
				return;
			}
			ResultSet devs_rs = (ResultSet) o;
			
			while(devs_rs.next()) {
				String SSID = devs_rs.getString("SSID");
				String topic = devs_rs.getString("topic");
				String MAC = devs_rs.getString("MAC");
				String room = devs_rs.getString("room");
				String prod_id = devs_rs.getString("product");
				String name = devs_rs.getString("name");
				boolean active = devs_rs.getBoolean("active");
				int index = devs_rs.getInt("index");
				String protocol = devs_rs.getString("protocol");
				
				int prop_index = devs_rs.getInt("prop_index");
				String prop_dispname = devs_rs.getString("prop_name");
				PropertyType ptype = pr.getPropertyType(devs_rs.getString("prop_type"));
				PropertyMode pmode = PropertyMode.parseFromString(devs_rs.getString("prop_mode"));
				Object prop_val = devs_rs.getString("prop_value");
				String prop_id = SSID + "_" + prop_index;
				if(devices.containsKey(SSID)) {
				    Device dev = getDevice(SSID);
				    if(dev.containsProperty(prop_index)) {
                        Property prop = dev.getProperty(prop_index);
                        LOG.debug("Setting property: " + prop_id + " (" + prop.getDisplayName() + ") of device: " + SSID +
                                " with value: " + prop_val);
                        prop.setValue(prop_val);
                    } else { //only productless devices get to this point
                        LOG.debug("Setting property: " + prop_id + " of device: " + SSID +
                                " with value: " + prop_val);
				        Property prop = new Property(ptype, prop_index, prop_dispname, pmode, jm);
				        prop.setValue(prop_val);
                        dev.addProperty(prop);
                    }
				} else {
				    if(protocols.containsKey(protocol)) {
                        LOG.debug("Adding device " + SSID + " (" + name + ") to repository!");
						Device d = pr.getProduct(prod_id).createDevice(SSID, MAC, name, topic,
								protocols.get(protocol), rr.getRoom(room), active, index);
                        if(prod_id.equals("0000")) {
							d.addProperty(new Property(ptype, prop_index, prop_dispname, pmode, jm));
						}
                        LOG.debug("Setting property: " + prop_id + " of device: " + SSID +
                                " with value: " + prop_val);
                        d.getProperty(prop_index).setValue(prop_val);
                        devices.put(SSID, d);
                        registeredMACs.put(MAC, SSID);
                    } else {
				        LOG.warn("Device " + SSID + " specifies a non-supported protocol! Device " +
                                "will not be managed by Maestro.");
                    }
				}
			}
			devs_rs.close();
			LOG.info(devices.size() + " devices retrieved!");
		} catch (SQLException e) {
			LOG.error("Cannot populate DeviceRepository!", e);
		}
	}

	/**
	 * Calls an update to all the adaptors connected to the devices and the devices' properties. This method is ONLY
	 * USUALLY called by the {@link bm.main.Maestro Maestro} in the startup phase.
	 */
    public void updateDevicesInEnvironment() {
        LOG.debug("Updating devices in Symphony Environment...");
        Iterator<Device> devices = this.devices.values().iterator();
        while(devices.hasNext()) {
            Device device = devices.next();
            try {
                device.update(logDomain, false);
				for (Property prop : device.getProperties()) {
					prop.update(logDomain, false);
				}
            } catch (AdaptorException e) {
                LOG.error("Cannot updateRules device " + device.getSSID() + " in environment!", e);
            }
        }
        LOG.debug("Devices updated in Symphony Environment!");
    }

//	/**
//	 * Creates a new Device object in the Environment. This method sends a registration device request to the created
//	 * device.
//	 *
//	 * @param mac The MAC address of the new Device object
//	 * @param name The name of the new Device object
//	 * @param roomID The ID of the parent room of the new Device object
//	 * @param prodID The ID of the product of the new Device object
//	 * @param protocol The protocol of the new Device object
//	 * @param active <b><i>true</i></b> if the device is already active upon registration, <i><b>false</i></b>
//	 *               if not
//	 * @return The Device object created
//	 * @throws AdaptorException
//	 * @throws IllegalArgumentException
//	 */
//	public Device createDevice(String mac, String name, String roomID, String prodID, Protocol protocol,
//							   boolean active)
//			throws AdaptorException, IllegalArgumentException {
//		Room room = rr.getRoom(roomID);
//		Product product = pr.getProduct(prodID);
//		String id = idg.generateCID();
//		if(room == null) {
//			throw new IllegalArgumentException("Room with ID " + roomID + " doesn't exist!");
//		}
//		if(product == null) {
//			throw new IllegalArgumentException("Product with ID " + prodID + " doesn't exist!");
//		}
//		Device device = new Device(logDomain, id, mac, name, id + "_topic", protocol, room,
//				active, product, room.getHighestIndex() + 1, jm);
//		LOG.debug("Creating device in Maestro...");
//		device.create(logDomain, true);
//		device.sendCredentials();
//		devices.put(device.getSSID(), device);
//		registeredMACs.put(device.getMAC(), device.getSSID());
//		LOG.info("Device created!");
//		return device;
//	}

//	/**
//	 * Adds a new Device object in the Environment. <i>This method is called mainly by Module objects as a
//	 * response to a JEEPRequest. For the non-Module version, see
//	 * {@link #createDevice(String, String, String, String, Protocol, boolean)}</i>
//	 *
//	 * @param device The Device object to be added
//	 * @param request The JEEPRequest that needs responding to
//	 * @return
//	 * @throws AdaptorException
//	 */
//	public Device addDevice(Device device, JEEPRequest request) throws AdaptorException {
//		device.create(logDomain, true);
//		jm.sendRegistrationResponse(device, request);
//		devices.put(device.getSSID(), device);
//		registeredMACs.put(device.getMAC(), device.getSSID());
//		return device;
//	}

	/**
	 * Adds a device object to the repository. <b>NOTE:</b> Device is added ONLY to the repository. To integrate
	 * device to the environment completely, {@link Device#create(String, boolean)} must be called.
	 * @param device The {@link Device} to be added
	 */
	public void addDevice(Device device) {
		devices.put(device.getSSID(), device);
		registeredMACs.put(device.getMAC(), device.getSSID());
	}
	
//	/**
//	 * Detaches the device with the specified identifier from Maestro and the environment. This method sends a
//	 * detachment request to the device for it to recognize that it is being detached from the Environment.
//	 *
//	 * @param identifier The SSID or MAC address of the device to be removed
//	 * @param detachmentMessage A message detailing the reason why the specified device is being
//	 *                          removed from the Environment
//	 * @return The device that was removed, <i>null</i> if the SSID specified does not pertain to an
//	 * 		existing device
//	 */
//	public Device detachDevice(String identifier, String detachmentMessage) throws AdaptorException {
//		if(registeredMACs.containsKey(identifier))
//			identifier = registeredMACs.get(identifier);
//		Device d = devices.get(identifier);
//		if(d != null) {
//			d.delete(logDomain, true);
//			d.detachDevice(detachmentMessage);
//			registeredMACs.remove(d.getMAC());
//			devices.remove(identifier);
//			LOG.info("Device " + d.getSSID() + " detached!");
//		}
//		return d;
//	}

//	/**
//	 * Detaches the device with the specified identifier from Maestro and the Environment. <i>This method is called
//	 * mainly by Module objects as a response to a JEEPRequest. For the non-Module version, see
//	 * {@link #detachDevice(String, String)}</i>
//	 *
//	 * @param identifier
//	 * @param request
//	 * @return
//	 * @throws AdaptorException
//	 */
//	public Device detachDevice(String identifier, JEEPRequest request) throws AdaptorException {
//		if(registeredMACs.containsKey(identifier))
//			identifier = registeredMACs.get(identifier);
//		Device d = devices.get(identifier);
//		if(d != null) {
//			d.delete(logDomain, true);
//			jm.sendDetachmentResponse(d, true, request);
//			registeredMACs.remove(d.getMAC());
//			devices.remove(identifier);
//			LOG.info("Device " + d.getSSID() + " detached!");
//		}
//		return d;
//	}

	/**
	 * Removes a device object from the repository. <b>NOTE:</b> Device is removed ONLY from the repository. To
	 * delete device from the environment completely, {@link Device#delete(String, boolean)} must be called.
	 * @param s The SSID or MAC address of the device to be removed
	 */
	public Device removeDevice(String s) {
		if(devices.containsKey(s)) {
			Device d = devices.remove(s);
			registeredMACs.remove(d.getMAC());
			return d;
		} else if (registeredMACs.containsKey(s)) {
			return devices.remove(registeredMACs.remove(s));
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the device object with the specified SSID or MAC.
	 * @param s The SSID or MAC to specify
	 * @return the device object with the specified SSID or MAC; <i>null</i> if nonexistent
	 */
	public Device getDevice(String s) {
		if(devices.containsKey(s)) {
			return devices.get(s);
		} else if (registeredMACs.containsKey(s)) {
			return devices.get(registeredMACs.get(s));
		} else {
			return null;
		}
	}
	
	/**
	 * Returns all the devices registered in the repository
	 * @return an array containing all the device objects in the repository
	 */
	public Device[] getAllDevices() {
		LOG.info(" ====test==== ");
//		devices.forEach((key,value) -> LOG.info(" ====test==== "+key + " = " + value));
        devices.forEach((key,value) -> {
            LOG.info(" ====test==== "+key + " = " + value);
            Device d = (Device)value;
            LOG.info(" ====test cels==== device name=" +d.getName());
        });
        LOG.info(" ====test cels==== devices.values()=" +devices.values());
		return devices.values().toArray(new Device[devices.size()]);
	}
	
	/**
	 * Returns all devices that belong to the product with the specified ID.
	 * @param prodID The product ID to specify
	 * @return an array containing the devices
	 */
	public Device[] getAllDevicesUnderProductSSID(String prodID) {
		Device[] devs = getAllDevices();
		Vector<Device> d = new Vector<Device>(1,1);
		
		for(int i = 0; i < devs.length; i++) {
			Device dev = devs[i];
			if(dev.getProduct().getSSID().equals(prodID)) {
				d.add(dev);
			}
		}
		
		return d.toArray(new Device[d.size()]);
	}
	
	/**
	 * Checks if a device with the specified SSID or MAC already exists in the repository.
	 * @param str The SSID or MAC address to be checked
	 * @return <b><i>true</i></b> if the device exists, <b><i>false</i></b> if not
	 */
	public boolean containsDevice(String str) {
		if(devices.containsKey(str) || registeredMACs.containsKey(str)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Sets all the protocols supported by the Symphony Environment. This method is ONLY used by the Spring IoC
	 * Container in the startup phase.
	 * @param protocols
	 */
    public void setProtocols(Protocol[] protocols) {
        this.protocols = new HashMap<String, Protocol>(protocols.length, 1);
        for(Protocol protocol : protocols) {
            this.protocols.put(protocol.getProtocolName(), protocol);
        }
    }
}