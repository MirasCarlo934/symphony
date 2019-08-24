package bm.main;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import bm.main.interfaces.Initializable;

/**
 * The Initializables object contains all Initializable objects in the VM. It is configured through Spring.
 * @author carlomiras
 *
 */
public class Initializables {
	private Logger LOG;
	private List<Initializable> initializables;

	public Initializables(String logDomain, List<Initializable> initializables/*,
			List<Initializable> multiModules*/) {
		LOG = Logger.getLogger(logDomain + "." + Initializables.class.getSimpleName());
		this.initializables = initializables;
	}
	
	public void initializeAll() throws Exception {
		Iterator<Initializable> inits = initializables.iterator();
		while(inits.hasNext()) {
			Initializable init = inits.next();
			LOG.debug("Initializing " + init.getClass().getName());
			init.initialize();
		}
	}

	public List<Initializable> getInitializables() {
		return initializables;
	}
}
