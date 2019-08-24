//package oh.modules.extensions;
//
//import org.json.JSONObject;
//
//import bm.comms.http.HTTPException;
//import bm.comms.http.HTTPSender;
//import bm.context.devices.Device;
//import bm.jeep.vo.JEEPRequest;
//import bm.main.modules.AbstModuleExtension;
//import bm.main.repositories.DeviceRepository;
//import oh.main.initializables.OH_Initializer;
//
//public class OH_DeviceChangeStateExtension extends AbstModuleExtension {
//	private DeviceRepository dr;
//	private HTTPSender hs;
//	private String ohIP;
//
//	public OH_DeviceChangeStateExtension(String logDomain, String errorLogDomain, String name, String[] params,
//			DeviceRepository deviceRepository, HTTPSender httpSender, String ohIP) {
//		super(logDomain, errorLogDomain, name, params);
//		this.dr = deviceRepository;
//		this.hs = httpSender;
//		this.ohIP = ohIP;
//	}
//
//	@Override
//	protected void processRequest(JEEPRequest request) {
//		Device dev = dr.getDevice(request.getCID());
//		if(dev == null) {
//			dev = dr.getDevice(request.getRID()); //for registration requests
//		}
//
//		LOG.debug("Updating state of device " + dev.getSSID() + "to " + dev.isActive() + "...");
//		String itemName;
//		if(dev.getPropvals().length > 1) {
//			itemName = dev.getSSID();
//		} else {
//			itemName = dev.getPropvals()[0].getOH_ID();
//		}
//		try {
//			if(dev.isActive()) {
//				OH_Initializer.addItems(LOG, hs, ohIP, dev.convertToItemsJSON(), true);
//				if(dev.getPropvals().length == 1) {
//					OH_Initializer.addItems(LOG, hs, ohIP, dev.getPropvals()[0].convertToItemsJSON(), true);
//				}
//			} else {
//				JSONObject json = new JSONObject();
//				json.put("type", "String");
//				json.put("name", itemName);
//				json.put("label", dev.getName() + " [inactive]");
//				json.put("groupNames", new String[]{dev.getParentRoom().getSSID()});
//				json.put("category", dev.getProduct().getOHIcon());
//				OH_Initializer.addItems(LOG, hs, ohIP, new JSONObject[]{json}, true);
//			}
//			LOG.debug("Device state change success!");
//		} catch(HTTPException e) {
//			LOG.error("Cannot updateRules state of device " + dev.getSSID() + "!", e);
//		}
//	}
//
//	@Override
//	protected boolean additionalRequestChecking(JEEPRequest request) {
//		return true;
//	}
//}
