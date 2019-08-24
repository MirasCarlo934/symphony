//package oh.modules.extensions;
//
//import java.util.Arrays;
//import java.util.Vector;
//
//import org.json.JSONObject;
//
//import bm.comms.http.HTTPException;
//import bm.comms.http.HTTPSender;
//import bm.context.adaptors.exceptions.AdaptorException;
//import bm.context.devices.Device;
//import bm.jeep.vo.JEEPRequest;
//import bm.jeep.vo.device.InboundRegistrationRequest;
//import bm.main.modules.AbstModuleExtension;
//import bm.main.repositories.DeviceRepository;
//import oh.main.initializables.OH_Initializer;
//
//public class OH_RegistrationExtension extends AbstModuleExtension {
//	private String ohIP;
//	private String nameParam;
//	private String roomIDParam;
//	private String propsParam;
//	private DeviceRepository dr;
//	private HTTPSender hs;
//
//	public OH_RegistrationExtension(String logDomain, String errorLogDomain, String name, String[] params,
//			DeviceRepository deviceRepository, HTTPSender httpSender, String nameParam, String roomIDParam,
//			String propsParam, String ohIP) {
//		super(logDomain, errorLogDomain, name, params);
//		this.ohIP = ohIP;
//		this.nameParam = nameParam;
//		this.roomIDParam = roomIDParam;
//		this.propsParam = propsParam;
//		this.dr = deviceRepository;
//		this.hs = httpSender;
//	}
//
//	@Override
//	protected void processRequest(JEEPRequest request) {
//		InboundRegistrationRequest reg = new InboundRegistrationRequest(request, nameParam, roomIDParam, propsParam);
//		Device dev = dr.getDevice(reg.mac);
//		Vector<JSONObject> items = new Vector<JSONObject>(dev.getPropvals().length + 1);
//
//		//adds a device to the item registry, updates if it already exists
//		LOG.debug("Adding device " + dev.getSSID() + " to OpenHAB item registry...");
//		if(dev.getPropvals().length > 1) { //1-property components are persisted thru their sole property!!!
//			items.addAll(Arrays.asList(dev.convertToItemsJSON()));
//			LOG.debug("Adding properties of " + dev.getSSID() + " to OpenHAB item registry...");
//			for(int i = 0; i < dev.getPropvals().length; i++) {
//				items.addAll(Arrays.asList(dev.getPropvals()[i].convertToItemsJSON()));
//			}
//		} else {
//			items.addAll(Arrays.asList(dev.getPropvals()[0].convertToItemsJSON()));
//		}
//
//		try {
//			OH_Initializer.addItems(LOG, hs, ohIP, items.toArray(new JSONObject[items.size()]), true);
//			LOG.debug("Device added successfully!");
//		} catch (HTTPException e) {
//			LOG.error("Cannot add device to OpenHAB!", e);
//		}
//	}
//
//	@Override
//	protected boolean additionalRequestChecking(JEEPRequest request) {
//		return true;
//	}
//}
