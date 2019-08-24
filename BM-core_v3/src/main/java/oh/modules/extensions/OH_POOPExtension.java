package oh.modules.extensions;

import java.util.HashMap;

import bm.comms.http.HTTPException;
import bm.comms.http.HTTPSender;
import bm.comms.http.messages.PutHTTPReq;
import bm.context.devices.Device;
import bm.context.properties.Property;
import bm.jeep.vo.JEEPRequest;
import bm.jeep.vo.device.ReqPOOP;
import bm.main.modules.AbstModuleExtension;
import bm.main.repositories.DeviceRepository;

public class OH_POOPExtension extends AbstModuleExtension {
	private DeviceRepository dr;
	private HTTPSender hs;
	private String ohIP;
	private String propValParam;
	private String propIDParam;

	public OH_POOPExtension(String logDomain, String errorLogDomain, String name, String[] params, 
			String propIDParam, String propValParam, DeviceRepository dr, HTTPSender httpSender, 
			String ohIP/*, MQTTPublisher mp*/) {
		super(logDomain, errorLogDomain, name, params);
		this.dr = dr;
		this.hs = httpSender;
		this.ohIP = ohIP;
		this.propIDParam = propIDParam;
		this.propValParam = propValParam;
	}

	@Override
	protected void process(JEEPRequest request) {
		ReqPOOP poop = new ReqPOOP(request, propIDParam, propValParam);
		Device dev = dr.getDevice(poop.getCID());
		Property p = dev.getProperty(poop.propIndex);
		
		mainLOG.debug("Updating property " + p.getDevice().getSSID() + "_" + p.getSSID() + " state in OpenHAB...");
		HashMap<String, String> parameters = new HashMap<String, String>(1,1);
		HashMap<String, String> headers = new HashMap<String, String>(2, 1);
		headers.put("Content-Type", "text/plain");
		headers.put("Accept", "application/json");
		parameters.put("null", p.transformValueToOHCommand());
		
		PutHTTPReq req = new PutHTTPReq(/*idg.generateMixedCharID(10), he, */ohIP + "/rest/items/" +
				p.getSSID() + "/state", headers, parameters, new int[]{200, 202});
		try {
			hs.sendHTTPRequest(req, false);
		} catch (HTTPException e) {
			mainLOG.error("Cannot updateRules property!", e);
		}
		mainLOG.debug("Property " + p.getDevice().getSSID() + "_" + p.getSSID() + " updated!");
	}

	@Override
	protected boolean additionalRequestChecking(JEEPRequest request) {
		return true;
	}
}
