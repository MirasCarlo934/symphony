package symphony.bm.bm_logic_devices.engines.exceptions;

import bm.main.engines.AbstEngine;

public class EngineException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8493070133289144632L;
	private String engine;
	
	/**
	 * Creates an instance of an EngineException used for internal ERQS error handling. 
	 * 
	 * @param engine the Engine that threw this EngineException
	 * @param cause the Exception encountered by the Engine that caused this EngineException
	 */
	public EngineException(AbstEngine engine, Throwable cause) {
		super(cause);
		this.engine = engine.getClass().getSimpleName();
	}

	/**
	 * Creates an instance of an EngineException used for internal ERQS error handling. 
	 * 
	 * @param engine the Engine that threw this EngineException
	 * @param message the error message
	 */
	public EngineException(AbstEngine engine, String message) {
		super(message);
		this.engine = engine.getClass().getSimpleName();
	}
	
	/**
	 * Creates an instance of an EngineException used for internal ERQS error handling. 
	 * 
	 * @param engine the Engine that threw this EngineException
	 * @param message the error message
	 * @param cause the Exception encountered by the Engine that caused this EngineException
	 */
	public EngineException(AbstEngine engine, String message, Exception cause) {
		super(message, cause);
		this.engine = engine.getClass().getSimpleName();
	}
	
	/**
	 * Returns the name of the Engine that threw this EngineException
	 * 
	 * @return the String name of the Engine
	 */
	public String getEngine() {
		return engine;
	}
}
