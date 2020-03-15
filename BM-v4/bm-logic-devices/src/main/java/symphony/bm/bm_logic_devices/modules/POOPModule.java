package symphony.bm.bm_logic_devices.modules;

import bm.context.adaptors.exceptions.AdaptorException;
import bm.context.devices.Device;
import bm.context.properties.Property;
import bm.jeep.JEEPManager;
import bm.jeep.exceptions.SecondaryMessageCheckingException;
import bm.jeep.vo.JEEPRequest;
import bm.jeep.vo.JEEPResponse;
import bm.jeep.vo.device.ReqPOOP;
import bm.main.modules.exceptions.RequestProcessingException;
import bm.main.repositories.DeviceRepository;

/**
 * The heart of the BM's functionality, the POOPModule executes the <b>P</b>roperty-<b>O</b>riented <b>O</b>rchestration 
 * <b>P</b>rocedure based on the JEEP request sent by devices every time their properties change value. The POOP is
 * executed by this module by:
 * 	<ol>
 * 		<li>Updating the property value of the requesting component</li>
 * 		<li>Checking the <b>C</b>omponent <b>I</b>nteracton <b>R</b>ules for other device properties that will be
 * 			changed based on the requesting component's property change</li>
 * 		<li>Changing the property values of other components based on the CIR</li>
 * 	</ol>
 * 
 * @author carlomiras
 *
 */

public class POOPModule extends Module {
	private String propIDParam;
	private String propValParam;
	private JEEPManager jm;

	public POOPModule(String logDomain, String errorLogDomain, String RTY, String propIDParam,
					  String propValParam, DeviceRepository dr, JEEPManager jeepManager) {
		super(logDomain, errorLogDomain, "POOPModule", RTY, new String[]{propIDParam, propValParam}, 
				null, /*mp, */dr);
		this.propIDParam = propIDParam;
		this.propValParam = propValParam;
		this.jm = jeepManager;
	}

	/**
	 * Updates the system of the property change of the requesting component and also the property
	 * changes of all the affected components according to CIR.
	 * 
	 * @param request The Request to be processed. <b>Must be</b> a <i>ReqPOOP</i> object.
	 */
	@Override
	protected void processRequest(JEEPRequest request) throws RequestProcessingException {
		ReqPOOP poop = new ReqPOOP(request, propIDParam, propValParam);
		Device d = dr.getDevice(poop.getCID());
		
		LOG.info("Changing property " + poop.propIndex + " of device " + d.getSSID() + " to "
                + poop.propValue + "...");
		if(d.getProperty(poop.propIndex).getValue().toString().equals(poop.propValue.toString())) {
			LOG.info("Property is already set to " + poop.propValue + "!");
			jm.sendPOOPResponse(d.getProperty(poop.propIndex), poop);
		}
		else {
			LOG.debug("Updating property in system...");
			try {
				Property prop = d.getProperty(poop.propIndex);
                prop.setValue(poop.propValue);
				prop.update(logDomain, false);
				jm.sendPOOPResponse(prop, poop);
			} catch (AdaptorException e) {
				throw new RequestProcessingException("Cannot change property " + poop.propIndex + " of device " +
						poop.getCID(), e);
			}
		}
		LOG.info("Property changed. POOP processing complete!");
	}
	
	@Override
	protected void processResponse(JEEPResponse response) {

	}

	@Override
	public void processNonResponse(JEEPRequest request) {
		ReqPOOP poop = (ReqPOOP) request;
		Device device = dr.getDevice(request.getCID());
		LOG.warn("Device " + request.getCID() + " has not responded to a request to change its property "
				+ device.getProperty(poop.propIndex) + " (" + device.getProperty(poop.propIndex).getDisplayName()
				+ ") to " + poop.propValue + ". Device may not have changed its property!");
	}

	@Override
	protected boolean additionalResponseChecking(JEEPResponse response) {
		return true;
	}

	/**
	 * Checks if request follows the following requirements:
	 * <ol>
	 * 	<li>Device has the specified property</li>
     * 	<li>Property can accept the specified property value</li>
	 * </ol>
	 */
	@Override
	protected boolean additionalRequestChecking(JEEPRequest request) throws SecondaryMessageCheckingException {
		ReqPOOP poop = new ReqPOOP(request, propIDParam, propValParam);
		
		Device d = dr.getDevice(poop.getCID());
		if(d.getProperty(poop.propIndex) != null) { //checks if property exists in the device;
			Property prop = d.getProperty(poop.propIndex);
			if(!prop.checkValueValidity(poop.propValue)) {
				throw new SecondaryMessageCheckingException("Invalid value! Check valid data types for given " +
						"property!");
			}
		}
		else {
			throw new SecondaryMessageCheckingException("Property does not exist in the specified device!");
		}
		
		return true;
	}
}
