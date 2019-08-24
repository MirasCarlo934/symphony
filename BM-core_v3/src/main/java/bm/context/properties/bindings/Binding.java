package bm.context.properties.bindings;

public class Binding {
	private String service;
	private String function;

	/**
	 * Creates a Binding object to be used to identify a peripheral service binding for a specific 
	 * property
	 * 
	 * @param service The name of the peripheral service that is bound with the property that owns this 
	 * 		binding
	 * @param function The name of the function that will be performed on the peripheral service by the 
	 * 		property that owns this binding
	 */
	public Binding(String service, String function) {
		this.service = service;
		this.function = function;
	}
	
	/**
	 * Parses a string into a binding. A binding will be parsed from a string with the following format:
	 * <br><br>
	 * <b>[service]:[function]</b>
	 * 
	 * @param str The string to be parsed
	 */
	public static Binding parseBinding(String str) {
		if(str == null) {
			return null;
		}
		String service = str.split(":")[0];
		String function = str.split(":")[1];
		return new Binding(service, function);
	}

	/**
	 * @return the name of the peripheral service that is bound with the property that owns this binding
	 */
	public String getService() {
		return service;
	}
	
	/**
	 * @return the name of the function that will be performed on the peripheral service by the property 
	 * 		that owns this binding
	 */
	public String getFunction() {
		return function;
	}
}
