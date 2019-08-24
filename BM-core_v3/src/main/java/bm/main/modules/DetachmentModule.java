package bm.main.modules;

import bm.cir.CIRManager;
import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.properties.Property;
import bm.jeep.JEEPManager;
import bm.jeep.exceptions.SecondaryMessageCheckingException;
import bm.jeep.vo.JEEPRequest;
import bm.jeep.vo.JEEPResponse;
import bm.main.modules.exceptions.RequestProcessingException;
import bm.main.repositories.DeviceRepository;
import bm.tools.IDGenerator;

/**
 * The DetachmentModule basically unregisters or "detaches" the requesting component from the Symphony system. The 
 * component's records are removed from the database, OpenHAB, and other peripheral systems linked to the component.
 * 
 * @author carlomiras
 *
 */
public class DetachmentModule extends Module {
	private JEEPManager jm;
	private IDGenerator idg;
	private CIRManager cirm;

	/**
	 * Creates a DetachmentModule
	 * 
	 * @param logDomain the log4j domain that this module will use
	 * @param errorLogDomain the log4j domain where errors will be logged to
	 * @param RTY the JEEP request type that this module handles
	 */
	public DetachmentModule(String logDomain, String errorLogDomain, String RTY, JEEPManager jeepManager,
							CIRManager cirManager, DeviceRepository dr, IDGenerator idGenerator) {
		super(logDomain, errorLogDomain, "DetachmentModule", RTY, new String[0], null, dr);
		this.jm = jeepManager;
		this.idg = idGenerator;
		this.cirm = cirManager;
	}
	
//	/**
//	 * Creates a DetachmentModule
//	 *
//	 * @param logDomain the log4j domain that this module will use
//	 * @param errorLogDomain the log4j domain where errors will be logged to
//	 * @param RTY the JEEP request type that this module handles
//	 */
//	public DetachmentModule(String logDomain, String errorLogDomain, String RTY, /*MQTTPublisher mp, */
//			DeviceRepository dr, AbstModuleExtension[] extensions) {
//		super(logDomain, errorLogDomain, "DetachmentModule", RTY, new String[0], null, dr, extensions);
//	}

	@Override
	protected void processRequest(JEEPRequest request) throws RequestProcessingException{
		String cid = request.getCID();
		Device d = dr.getDevice(cid);
		LOG.info("Detaching device " + cid + " from system...");
		try {
			for(Property p : d.getProperties()) {
				cirm.removeRulesTriggered(p);
			}
			d.delete(logDomain, true);
			jm.sendDetachmentResponse(d, true, request);
			dr.removeDevice(d.getSSID());
			idg.removeCID(d.getSSID());
		} catch (AdaptorException e) {
//			error(e, request.getProtocol());
//			return false;
			throw new RequestProcessingException("Failed to detach device in " + e.getAdaptorName() + "!",
					e);
		}
		
		LOG.info("Detachment complete!");
//		return true;
	}

	@Override
	protected void processResponse(JEEPResponse response) {
	}

	@Override
	public void processNonResponse(JEEPRequest request) {

	}

	/*
	This method was overridden from superclass Module to allow attached ModuleExtensions to process first before
	detaching.
	 */
	@Override
	public void run() {
		LOG.debug(name + " request processing started!");

		try {
			if(checkSecondaryRequestParameters(request)) {
				LOG.trace("Request valid! Proceeding to request processing...");
				for(int i = 0; i < extensions.length; i++) {
					AbstModuleExtension ext = extensions[i];
					ext.processRequest(request);
				}
				try {
					processRequest(request);
					LOG.info("Request processing finished!");
				} catch(RequestProcessingException e) {
					error("Request processing failed!", e, request.getProtocol());
				}
			}
		} catch (SecondaryMessageCheckingException e) {
			LOG.error("Secondary request params didn't check out. See also the additional request params"
					+ " checking.");
		}
	}

	@Override
	protected boolean additionalRequestChecking(JEEPRequest request) {
		return true;
	}

	@Override
	protected boolean additionalResponseChecking(JEEPResponse response) {
		return true;
	}
}
