package bm.main.interfaces;

/**
 * The interface implemented by all objects that has to undergo additional initialization processes after environment 
 * setup and before VM operation start.
 * @author carlomiras
 *
 */
public interface Initializable {

	/**
	 * Initializes the object during startup phase. {@link bm.main.Maestro Maestro} calls this method during startup.
	 * @throws Exception
	 */
	void initialize() throws Exception;
}
