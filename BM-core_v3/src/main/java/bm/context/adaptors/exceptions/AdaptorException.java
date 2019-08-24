package bm.context.adaptors.exceptions;

public class AdaptorException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6321948809839903096L;
	private String adaptorName;

	public AdaptorException(String message, String adaptorName) {
		super(message);
		this.adaptorName = adaptorName;
	}

	public AdaptorException(Throwable cause, String adaptorName) {
		super(cause);
		this.adaptorName = adaptorName;
	}

	public AdaptorException(String message, Throwable cause, String adaptorName) {
		super(message, cause);
		this.adaptorName = adaptorName;
	}

	/**
	 * Returns the name of the adaptor that threw this AdaptorException
	 * @return the adaptor name
	 */
	public String getAdaptorName() {
		return adaptorName;
	}
}
