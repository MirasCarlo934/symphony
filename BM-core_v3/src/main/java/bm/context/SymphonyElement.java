package bm.context;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import bm.context.adaptors.AbstAdaptor;
import bm.context.adaptors.exceptions.AdaptorException;

public abstract class SymphonyElement {
//	private static final Logger LOG = Logger.getLogger(SmarthomeElement.class);
	protected String SSID;
	/**
	 * The index of this SmarthomeElement in its container
	 */
	private int index;
//	protected DBAdaptor mainDBAdaptor;
//	protected OHAdaptor mainOHAdaptor;
//	protected AbstAdaptor[] additionalAdaptors;
	protected Vector<AbstAdaptor> adaptors = new Vector<AbstAdaptor>(1,1);

	public SymphonyElement(String SSID, /*AbstAdaptor[] adaptors, */int index) {
		this.SSID = SSID;
//		this.adaptors = adaptors;
		this.index = index;
//		this.mainDBAdaptor = dba;
//		this.mainOHAdaptor = oha;
//		this.additionalAdaptors = additionalAdaptors;
//		Vector<AbstAdaptor> a;
//		if(additionalAdaptors == null)
//			a = new Vector<AbstAdaptor>(2);
//		else {
//			a = new Vector<AbstAdaptor>(additionalAdaptors.length + 2);
//			a.addAll(Arrays.asList(additionalAdaptors));
//		}
//		a.add(dba);
//		a.add(oha);
//		this.adaptors = a.toArray(new AbstAdaptor[a.size()]);
	}
	
	public String getSSID() {
		return SSID;
	}

	public void setSSID(String SSID) {
		this.SSID = SSID;
	}

	/**
	 * Returns the index of this SymphonyElement in its container.
	 *
	 * @return The index
	 */
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

//	public DBAdaptor getMainDBAdaptor() {
//		return mainDBAdaptor;
//	}
//	
//	public OHAdaptor getMainOHAdaptor() {
//		return mainOHAdaptor;
//	}

	public void addAdaptor(AbstAdaptor adaptor) {
		adaptors.add(adaptor);
	}

	public void setAdaptors(AbstAdaptor[] adaptors) {
	    this.adaptors.addAll(Arrays.asList(adaptors));
    }

	public AbstAdaptor[] getAdaptors() {
		return adaptors.toArray(new AbstAdaptor[adaptors.size()]);
	}
	
//	public AbstAdaptor[] getAdditionalAdaptors() {
//		return additionalAdaptors;
//	}

	/**
	 * Persists this object. The objective of this method is to use all the injected adaptors
	 * to persist this object to various external systems and services. <br><br>
	 * 
	 * <i><b>Note:</b> This method does NOT guarantee that this object will be persisted into the BusinessMachine
	 * repositories. That is mainly done in Modules.
	 * 
	 * @param callerLogDomain the log domain of the object that called this method
	 * @param waitUntilCreated
	 * @throws AdaptorException if an adaptor fails to persist this object
	 */
	protected abstract void createInAdaptor(AbstAdaptor adaptor, String callerLogDomain, boolean waitUntilCreated)
			throws AdaptorException;
	
	public synchronized void create(String callerLogDomain, boolean waitUntilCreated) throws AdaptorException {
		for(int i = 0; i < adaptors.size(); i++) {
			createInAdaptor(adaptors.get(i), callerLogDomain, waitUntilCreated);
		}
	}
	
//	/**
//	 * Persists this object. The common objective of this method is to use all the injected adaptors
//	 * to persist this object to various external systems and services, <b>with the exception of some
//	 * specified adaptors</b>. <br><br>
//	 *
//	 * <i><b>Note:</b> This method does NOT guarantee that this object will be persisted into the BusinessMachine
//	 * repositories. That is mainly done in Modules.
//	 *
//	 * @param exceptions the adaptors where this object <b>WILL NOT</b> be created in
//	 * @param callerLogDomain the log domain of the object that called this method
//	 * @param waitUntilCreated
//	 * @throws AdaptorException if an adaptor fails to persist this object
//	 */
//	public synchronized void createExcept(AbstAdaptor[] exceptions, String callerLogDomain,
//			boolean waitUntilCreated) throws AdaptorException {
//		List<AbstAdaptor> excepts = Arrays.asList(exceptions);
//		for(int i = 0; i < adaptors.size(); i++) {
//			AbstAdaptor adaptor = adaptors.get(i);
//			if(!excepts.contains(adaptor)) {
//				createInAdaptor(adaptor, callerLogDomain, waitUntilCreated);
//			}
//		}
//	}
//
//	/**
//	 * Persists this object. The common objective of this method is to use all the injected adaptors
//	 * to persist this object to various external systems and services, <b>with the exception of some
//	 * specified adaptors</b>. <br><br>
//	 *
//	 * <i><b>Note:</b> This method does NOT guarantee that this object will be persisted into the BusinessMachine
//	 * repositories. That is mainly done in Modules.
//	 *
//	 * @param exceptions the adaptor names where this object <b>WILL NOT</b> be created in
//	 * @param callerLogDomain the log domain of the object that called this method
//	 * @param waitUntilCreated
//	 * @throws AdaptorException if an adaptor fails to persist this object
//	 */
//	public synchronized void createExcept(String[] exceptions, String callerLogDomain,
//			boolean waitUntilCreated) throws AdaptorException {
//		List<String> excepts = Arrays.asList(exceptions);
//		for(int i = 0; i < adaptors.size(); i++) {
//			AbstAdaptor adaptor = adaptors.get(i);
//			if(!excepts.contains(adaptor.getName())) {
//				createInAdaptor(adaptor, callerLogDomain, waitUntilCreated);
//			}
//		}
//	}
	
	/**
	 * Persists this object to all plugged adaptors <b>EXCEPT</b> the DBAdaptor. This method is invoked mainly by 
	 * repositories that wish to updateRules the records of the persisted objects in all the peripheral systems.
	 * 
	 * @param parentLogDomain the log domain of the object that called this method
	 * @throws AdaptorException if an adaptor fails to persist this object
	 */
//	public abstract void persistExceptDB(String parentLogDomain) throws AdaptorException;
	
	/**
	 * Deletes this object. The common objective of this method is to use all the injected adaptors 
	 * to delete this object to various external systems and services. <br><br>
	 * 
	 * <i><b>Note:</b> This method does NOT guarantee that this object will be deleted from the BusinessMachine
	 * repositories. That is mainly done in Modules.
	 * 
	 * @param callerLogDomain the log domain of the object that called this method
	 * @param waitUntilDeleted
	 * @throws AdaptorException if an adaptor fails to persist this object
	 */
	protected abstract void deleteInAdaptor(AbstAdaptor adaptor, String callerLogDomain, boolean waitUntilDeleted)
			throws AdaptorException;
	
	public synchronized void delete(String callerLogDomain, boolean waitUntilDeleted) throws AdaptorException {
		for(int i = 0; i < adaptors.size(); i++) {
			deleteInAdaptor(adaptors.get(i), callerLogDomain, waitUntilDeleted);
		}
	}
	
//	/**
//	 * Persists this object. The common objective of this method is to use all the injected adaptors
//	 * to persist this object to various external systems and services, <b>with the exception of some
//	 * specified adaptors</b>. <br><br>
//	 *
//	 * <i><b>Note:</b> This method does NOT guarantee that this object will be persisted into the BusinessMachine
//	 * repositories. That is mainly done in Modules.
//	 *
//	 * @param exceptions the adaptors where this object <b>WILL NOT</b> be created in
//	 * @param callerLogDomain the log domain of the object that called this method
//	 * @param waitUntilDeleted
//	 * @throws AdaptorException if an adaptor fails to persist this object
//	 */
//	public void deleteExcept(AbstAdaptor[] exceptions, String callerLogDomain,
//			boolean waitUntilDeleted) throws AdaptorException {
//		List<AbstAdaptor> excepts = Arrays.asList(exceptions);
//		for(int i = 0; i < adaptors.size(); i++) {
//			AbstAdaptor adaptor = adaptors.get(i);
//			if(!excepts.contains(adaptor)) {
//				deleteInAdaptor(adaptor, callerLogDomain, waitUntilDeleted);
//			}
//		}
//	}
//
//	/**
//	 * Persists this object. The common objective of this method is to use all the injected adaptors
//	 * to persist this object to various external systems and services, <b>with the exception of some
//	 * specified adaptors</b>. <br><br>
//	 *
//	 * <i><b>Note:</b> This method does NOT guarantee that this object will be persisted into the BusinessMachine
//	 * repositories. That is mainly done in Modules.
//	 *
//	 * @param exceptions the adaptor names where this object <b>WILL NOT</b> be created in
//	 * @param callerLogDomain the log domain of the object that called this method
//	 * @param waitUntilDeleted
//	 * @throws AdaptorException if an adaptor fails to persist this object
//	 */
//	public void deleteExcept(String[] exceptions, String callerLogDomain,
//			boolean waitUntilDeleted) throws AdaptorException {
//		List<String> excepts = Arrays.asList(exceptions);
//		for(int i = 0; i < adaptors.size(); i++) {
//			AbstAdaptor adaptor = adaptors.get(i);
//			if(!excepts.contains(adaptor.getName())) {
//				deleteInAdaptor(adaptor, callerLogDomain, waitUntilDeleted);
//			}
//		}
//	}
	
//	/**
//	 * Updates this object. The common objective of this method is to use all the injected adaptors 
//	 * to updateRules this object to various external systems and services. <br><br>
//	 * 
//	 * <i><b>Note:</b> This method does NOT guarantee that this object will be updated from the BusinessMachine
//	 * repositories. That is mainly done in Modules.
//	 * 
//	 * @param parentLogDomain the log domain of the object that called this method
//	 * @throws AdaptorException if an adaptor fails to updateRules this object
//	 */
	//FIXME this method should updateRules one by one based on the supplied adaptors by the other updateRules methods
	/**
	 * Updates this object to a single adaptor.
	 * 
	 * @param adaptor the adaptor where this component will be updated in
	 * @param callerLogDomain the log domain of the object that called this method
	 * @param waitUntilUpdated
	 * @throws AdaptorException if an adaptor fails to persist this object
	 */
	protected abstract void updateInAdaptor(AbstAdaptor adaptor, String callerLogDomain, boolean waitUntilUpdated)
			throws AdaptorException;
	
	/**
	 * Updates this object to all adaptors plugged to this SymphonyElement.
	 * 
	 * @param callerLogDomain the log domain of the object that called this method
	 * @param waitUntilUpdated
	 * @throws AdaptorException if an adaptor fails to updateRules this object
	 */
	public synchronized void update(String callerLogDomain, boolean waitUntilUpdated) throws AdaptorException {
		for(int i = 0; i < adaptors.size(); i++) {
			updateInAdaptor(adaptors.get(i), callerLogDomain, waitUntilUpdated);
		}
	}
	
//	/**
//	 * Updates this object to all plugged adaptors with the exception of some specified adaptors.
//	 *
//	 * @param exceptions the adaptors where this object <b>WILL NOT</b> be updated to
//	 * @param parentLogDomain the log domain of the object that called this method
//	 * @param waitUntilUpdated
//	 * @throws AdaptorException if an adaptor fails to updateRules this object
//	 */
//	public void updateExcept(AbstAdaptor[] exceptions, String parentLogDomain, boolean waitUntilUpdated)
//			throws AdaptorException {
//		List<AbstAdaptor> excepts = Arrays.asList(exceptions);
//		for(int i = 0; i < adaptors.size(); i++) {
//			AbstAdaptor adaptor = adaptors.get(i);
//			if(!excepts.contains(adaptor)) {
//				updateInAdaptor(adaptor, parentLogDomain, waitUntilUpdated);
//			}
//		}
//	}
	
//	/**
//	 * Updates this object to all plugged adaptors with the exception of some specified adaptors.
//	 *
//	 * @param exceptions the adaptor names where this object <b>WILL NOT</b> be updated to
//	 * @param callerLogDomain the log domain of the object that called this method
//	 * @param waitUntilUpdated
//	 * @throws AdaptorException if an adaptor fails to updateRules this object
//	 */
//	public void updateExcept(String[] exceptions, String callerLogDomain, boolean waitUntilUpdated)
//			throws AdaptorException {
//		List<String> excepts = Arrays.asList(exceptions);
//		for(int i = 0; i < adaptors.size(); i++) {
//			AbstAdaptor adaptor = adaptors.get(i);
//			if(!excepts.contains(adaptor.getName())) {
//				updateInAdaptor(adaptor, callerLogDomain, waitUntilUpdated);
//			}
//		}
//	}
	
//	/**
//	 * Updates this object to all plugged adaptors with the exception of the specified adaptor classes. <b><i>NOTE:</b> 
//	 * ALL adaptors that have classes that are the same with the specified adaptor classes exceptions will become exceptions!
//	 * </i>
//	 * 
//	 * @param exceptions the adaptor classes where this object <b>WILL NOT</b> be persisted to
//	 * @param parentLogDomain the log domain of the object that called this method
//	 * @param waitUntilUpdated
//	 * @throws AdaptorException if an adaptor fails to persist this object
//	 */
//	public abstract void updateExcept(Class[] exceptions, String parentLogDomain, boolean waitUntilUpdated) 
//			throws AdaptorException;
}
