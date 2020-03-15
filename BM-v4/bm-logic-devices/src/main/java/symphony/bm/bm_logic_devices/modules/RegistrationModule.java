package symphony.bm.bm_logic_devices.modules;

import bm.comms.Protocol;
import bm.comms.mqtt.MQTTPublisher;
import bm.context.adaptors.AdaptorManager;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.products.Product;
import bm.context.properties.Property;
import bm.context.properties.PropertyMode;
import bm.context.rooms.Room;
import bm.jeep.JEEPManager;
import bm.jeep.exceptions.SecondaryMessageCheckingException;
import bm.jeep.vo.JEEPRequest;
import bm.jeep.vo.JEEPResponse;
import bm.jeep.vo.device.InboundRegistrationRequest;
import bm.main.modules.exceptions.RequestProcessingException;
import bm.main.repositories.DeviceRepository;
import bm.main.repositories.ProductRepository;
import bm.main.repositories.RoomRepository;
import bm.tools.IDGenerator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class RegistrationModule extends Module {
	private String nameParam;
	private String roomIDParam;
	private String propsParam;
	private String proplistParam; //only if CID is "0000"
	private String iconParam; //only if CID is "0000"

	private String plistIDParam;
	private String plistNameParam;
	private String plistModeParam;
	private String plistIndexParam;

	private ProductRepository pr;
	private RoomRepository rr;
	private IDGenerator idg;
	private MQTTPublisher mp;
	private AdaptorManager am;
	private JEEPManager jm;
	private HashMap<String, Protocol> protocols;
	
	public RegistrationModule(String logDomain, String errorLogDomain, String RTY,
							  String nameParam, String roomIDParam, String propsParam, String proplistParam,
							  String iconParam, String plistIDParam, String plistNameParam, String plistModeParam,
							  String plistIndexParam,
							  MQTTPublisher mqttPublisher, DeviceRepository deviceRepository,
							  ProductRepository productRepository, RoomRepository roomRepository,
							  AdaptorManager adaptorManager, JEEPManager jeepManager,
							  Protocol[] protocols, IDGenerator idg) {
		super(logDomain, errorLogDomain, RegistrationModule.class.getSimpleName(), RTY,
				new String[]{nameParam, roomIDParam}, null, deviceRepository);
		this.pr = productRepository;
		this.rr = roomRepository;
		this.nameParam = nameParam;
		this.roomIDParam = roomIDParam;
		this.propsParam = propsParam;
		this.proplistParam = proplistParam;
		this.iconParam = iconParam;
		this.plistIDParam = plistIDParam;
		this.plistNameParam = plistNameParam;
		this.plistModeParam = plistModeParam;
		this.plistIndexParam = plistIndexParam;
		this.idg = idg;
		this.mp = mqttPublisher;
		this.am = adaptorManager;
		this.jm = jeepManager;
		this.protocols = new HashMap<String, Protocol>(protocols.length);
		for(Protocol protocol : protocols) {
		    this.protocols.put(protocol.getProtocolName(), protocol);
        }
	}

	/**
	 * Registers component into system.
	 */
	@Override
	protected void processRequest(JEEPRequest request) throws RequestProcessingException {
		InboundRegistrationRequest reg = new InboundRegistrationRequest(request, nameParam, roomIDParam, propsParam,
				proplistParam, iconParam);
		if(request.getJSON().has("exists")) {
			if(checkCredentialChanges(reg)) {
				updateDevice(reg);
			} else {
				returnExistingComponent(reg);
			}
			return;
		}
		
		LOG.info("Registering device " + reg.getMAC() + " to Environment...");
		String ssid = idg.generateCID();
		String topic = ssid + "_topic";
		Product product = pr.getProduct(reg.getCID());
		Room parentRoom = rr.getRoom(reg.getRoomID());
		Protocol protocol = reg.getProtocol();
		LOG.debug("Creating Device object...");
		Device d = product.createDevice(ssid, reg.getMAC(), reg.getName(), topic, protocol, parentRoom, true,
				parentRoom.getHighestIndex() + 1);
		d.setAdaptors(am.getAdaptorsLinkedToProduct(product.getSSID()));

		if(reg.isProductless()) {
			LOG.debug("Device is productless! Retrieving properties and device icon from registration request...");
			LOG.fatal(reg.getProplist());
			HashMap<Integer, Property> props = new HashMap<Integer, Property>(reg.getProplist().length());
			for(int i = 0; i < reg.getProplist().length(); i++) {
				JSONObject json = (JSONObject) reg.getProplist().get(i);
				Property prop = new Property(pr.getPropertyType(json.getString(plistIDParam)),
						json.getInt(plistIndexParam), json.getString(plistNameParam),
						PropertyMode.parseFromString(json.getString(plistModeParam)), jm);
				prop.setAdaptors(am.getUniversalAdaptors());
				props.put(prop.getIndex(), prop);
			}
			d.setProperties(props);
			LOG.debug("Properties retrieved!");
			d.setIcon(reg.getIcon());
			LOG.debug("Icon retrieved!");
		}
		if(reg.getPropvals() != null) {
			JSONObject props = reg.getPropvals();
			Iterator<String> propIndices = props.keys();
			while(propIndices.hasNext()) {
				int propIndex = Integer.parseInt(propIndices.next());
				LOG.fatal(propIndex);
				Property prop = d.getProperty(propIndex);
				LOG.debug("Setting property " + propIndex + " (" + prop.getDisplayName() + ")");
				prop.setValue(props.get(String.valueOf(propIndex)));
			}
		}
		try {
			d.create(logDomain, true);
			dr.addDevice(d);
			for(Property p : d.getProperties()) {
				p.update(logDomain, true);
			}
		} catch (AdaptorException e) {
//			LOG.error("Device couldn't be added to Environment!", e);
			throw new RequestProcessingException("Device couldn't be added to Environment!", e);
		}

		LOG.info("Sending device credentials to actual device...");
		jm.sendRegistrationResponse(d, request);
		LOG.info("Registration complete!");
//		return true;
	}

	@Override
	protected void processResponse(JEEPResponse response) {
//		return true;
	}

	@Override
	public void processNonResponse(JEEPRequest request) {
		Device d = dr.getDevice(request.getCID());
		LOG.warn("Actual device with MAC " + d.getMAC() + " failed to respond to its registration message to " +
				"the Environment. The device will be removed from Maestro.");
		try {
			d.delete(logDomain, true);
			jm.sendDetachmentResponse(d, true, request);
			dr.removeDevice(d.getSSID());
			LOG.info("Device " + d.getMAC() + " deleted from Maestro records!");
		} catch (AdaptorException e) {
			LOG.error("Device " + d.getMAC() + " failed to be deleted from the " + e.getAdaptorName()
					+ " adaptor!");
		}
	}

	private void returnExistingComponent(InboundRegistrationRequest request) {
		Device d = dr.getDevice(request.getMAC());
		LOG.info("Device already exists in system as " + d.getSSID() + "! "
				+ "Returning existing credentials and property states.");
		jm.sendRegistrationResponse(d, request);
		for(Property prop : d.getProperties()) {
			prop.sendValueToDevice(logDomain);
		}

		LOG.info("Activating device " + d.getSSID() + "...");
		try {
			d.setActive(true);
			d.update(logDomain, true);
			LOG.info("Device activated!");
		} catch (AdaptorException e) {
			error("Cannot activate device" + d.getSSID() + "!", e, request.getProtocol());
		}
	}
	
	private void updateDevice(InboundRegistrationRequest request) {
		Device c = dr.getDevice(request.getMAC());
		LOG.info("Updating device " + c.getSSID() + " credentials...");
		try {
			c.setName(request.getName());
			c.setRoom(rr.getRoom(request.getRoomID()));
			c.update(logDomain, true);
			c.setActive(true);
            c.update(logDomain, true);
			LOG.info("Device updated!");
		} catch (AdaptorException e) {
			error("Cannot updateRules device " + c.getSSID() + " credentials!", e, request.getProtocol());
		}
	}
	
	/**
	 * After confirming that the registering component already exists, another check is made to see if the registration 
	 * request contains different credentials from persisted data. Changes signify component credential updateRules. No
	 * changes signify component activation. <br><br>
	 * 
	 * <b><i>NOTE:</b></i> The request parameter <i>properties</i> will not be checked here.
	 * 
	 * @param request The registration request sent by the registering component
	 * @return <b>true</b> if the request contains changes in credentials, <b>false</b> otherwise
	 */
	private boolean checkCredentialChanges(InboundRegistrationRequest request) {
		Device c = dr.getDevice(request.getMAC());
		String[] reqCreds = new String[]{request.getName(), request.getRoomID()};
		String[] comCreds = new String[] {c.getName(), c.getParentRoom().getSSID()};
		
		for(int i = 0; i < reqCreds.length; i++) {
			if(!reqCreds[i].equals(comCreds[i])) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Checks for the following deficiencies in the request (done in a step-by-step manner):
	 * <ol>
	 * 	<li>CID already exists</li>
	 * 	<li>Invalid product ID</li>
	 * 	<li>Invalid room ID</li>
	 * 	<li>Invalid set properties block <i>(<b>Optional:</b> a register request does not have to include a set
	 * 		properties block)</i>
	 * 		<ul>
	 * 			<li>B_Property does not exist in the requested component product</li>
	 * 			<li>Invalid property value</li>
	 * 		</ul>
	 * 	</li>
	 * 
	 * </ol>
	 */
	@Override
	protected boolean additionalRequestChecking(JEEPRequest request) throws SecondaryMessageCheckingException {
		LOG.trace("Additional secondary request parameter checking...");
		InboundRegistrationRequest reg = new InboundRegistrationRequest(request, nameParam, roomIDParam, propsParam,
				proplistParam, iconParam);
		
		LOG.trace("Checking productID validity...");
		if(!pr.containsProduct(reg.getCID())) {
			throw new SecondaryMessageCheckingException("Request contains invalid product ID! (" + reg.getCID() + ")");
		} else if(reg.isProductless()) {
			List<String> params = Arrays.asList((reg.getParameters()));
			LOG.debug("============ proplistParam=" + proplistParam + " iconParam="+iconParam);
			for(int i = 0; i < params.size(); i++) {
				LOG.debug("============ params["+i+"]="+params.get(i));
			}
			if(params.contains(proplistParam) && params.contains(iconParam)) {
				//check if proplist parameter is valid
				JSONArray proplist = (JSONArray) reg.getParameter(proplistParam);
				Vector<Integer> indexes = new Vector<Integer>(proplist.length());
				for(int i = 0; i < proplist.length(); i++) {
					JSONObject json = proplist.getJSONObject(i);
					String[] jsonParams = {plistIDParam, plistNameParam, plistModeParam, plistIndexParam};
					for(String param : jsonParams) {
						try {
							json.get(param);
						} catch (JSONException e) {
							throw new SecondaryMessageCheckingException("'proplist' parameter lacks parameters.");
						}
					}
					if(!pr.containsPropertyType(json.getString(plistIDParam))) {
						throw new SecondaryMessageCheckingException("'proplist' parameter contains an invalid " +
								"property type (" + json.getString(plistIDParam) + ")");
					}
					if(PropertyMode.parseFromString(json.getString(plistModeParam)) == PropertyMode.Null) {
						throw new SecondaryMessageCheckingException("'proplist' parameter contains an invalid " +
								"property mode (" +json.getString(plistModeParam) + ")");
					}
					if(indexes.contains(json.getInt(plistIndexParam))) {
						throw new SecondaryMessageCheckingException("'proplist' parameter contains a duplicate " +
								"index (" + json.getInt(plistIndexParam) + ")");
					} else {
						indexes.add(json.getInt(plistIndexParam));
					}
				}
				indexes.sort(Comparator.naturalOrder());
				int i = 0;
				for(int a : indexes) {
					LOG.fatal("INDEX: " + a);
                    if(a != i) {
                        throw new SecondaryMessageCheckingException("'proplist' parameter skipped an index " +
                                "(" + i + ")");
                    }
                    i++;
                }
			} else {
				throw new SecondaryMessageCheckingException("Request lacks parameters for productless " +
						"registration!");
			}
		}
		LOG.trace("Checking roomID validity...");
		if(!rr.containsRoom(reg.getRoomID())) {
			throw new SecondaryMessageCheckingException("Request contains invalid room ID! (" + reg.getRoomID() + ")");
//			ResError error = new ResError(reg, "Request contains invalid room ID!");
//			error(error);
//			return false;
		}
		
		LOG.trace("Checking set property block validity...");
		Product prod = pr.getProduct(reg.getCID());
		if(reg.getPropvals() != null) { //it's okay if the request does not have a set properties block
			JSONObject props = reg.getPropvals();
			Iterator<String> propIndices = props.keys();
			while(propIndices.hasNext()) {
				int propIndex = Integer.parseInt(propIndices.next());
				if(prod.containsProperty(propIndex)) {
					if(!prod.getProperty(propIndex).checkValueValidity(props.get(String.valueOf(propIndex)))) {
						throw new SecondaryMessageCheckingException("Invalid property value for property index ("
								+ propIndex + ")");
//						ResError error = new ResError(request, "Invalid property value for PID " +
//								propID);
//						error(error);
//						return false;
					}
				} else {
					throw new SecondaryMessageCheckingException("Invalid propery index \"" + propIndex + "\"!");
				}
			}
		}
		
		LOG.trace("Checking MAC validity...");
		if(dr.containsDevice(reg.getMAC())) {
			LOG.warn("Request contains a preexisting MAC address!");
			request.getJSON().put("exists", true);
			return true;
		}
		
		return true;
	}

	@Override
	protected boolean additionalResponseChecking(JEEPResponse response) {
		return true;
	}
}
