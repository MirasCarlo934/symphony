package bm.main;

import bm.cir.CIRManager;
import bm.comms.Sender;
import bm.context.adaptors.AdaptorManager;
import bm.comms.InboundTrafficManager;
import bm.main.controller.Controller;
import bm.main.engines.AbstEngine;
import bm.main.repositories.DeviceRepository;
import bm.main.repositories.ProductRepository;
import bm.main.repositories.RoomRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

import java.util.List;

@SpringBootApplication
@ComponentScan({"ui"})
@ImportResource({"file:configuration/spring/core-config.xml"})
/**
 * The main class of the Maestro application. This is responsible for the initialization of important objects and
 * starting the application.
 */
public class Maestro {
	private static ApplicationContext applicationContext;
	private static final int build = 1;
	private static final Logger LOG = LogManager.getLogger("MAIN.Maestro");
	private Initializables inits;
	private DeviceRepository dr;
	private RoomRepository rr;
	private ProductRepository pr;
	private AdaptorManager am;
	private CIRManager cirm;
	private List<AbstEngine> engines;
	private List<Sender> senders;

	private InboundTrafficManager itm;
	
	private Controller controller;

	public static void main(String[] args) {
		Maestro maestro = new Maestro();
		maestro.setup(args);
		maestro.start();
	}
	
	/**
	 * Can be called in order to signify a fatal error during startup which can lead to runtime errors and 
	 * possible termination of the Maestro's functionality. This method terminates the VM.
	 * 
	 * @param e The exception that causes the fatal error
	 * @param status The error status of the VM
	 */
	public static void errorStartup(Exception e, int status) {
		LOG.fatal("Fatal error occurred! VM shutting down...", e);
		System.exit(status);
	}
	
	 /**
     * Retrieves all objects to be initialized for Symphony to run. Objects are retrieved from the 
     * Spring IoC Container.
     */
    public void setup(String[] args) {
    		LOG.info("Starting Maestro... ");
    		ApplicationContext context = SpringApplication.run(Maestro.class, args);
    		Maestro.applicationContext = context;

    		try {
        		//comm layer
                senders = (List<Sender>) context.getBean("Senders");
				itm = (InboundTrafficManager) context.getBean("InboundTrafficManager");

                //controller layer
                controller = (Controller) context.getBean("Controller");
                dr = (DeviceRepository) context.getBean("Devices");
                rr = (RoomRepository) context.getBean("Rooms");
                pr = (ProductRepository) context.getBean("Products");
                am = (AdaptorManager) context.getBean("AdaptorManager");
                cirm = (CIRManager) context.getBean("CIRs");

                //engine layer
                engines = (List<AbstEngine>) context.getBean("Engines");

                //misc initializables
                inits = (Initializables) context.getBean("Initializables");
            } catch (Exception e) {
                Exception ex = new Exception("VM failed to initialize!", e);
                errorStartup(ex, 000);
            }
    }
    
    /**
     * Starts Maestro. Maestro is started by initializing objects from bottom-to-top layers, starting from the engine
	 * layer to the controller layer then, finally, the comm layer.
     */
    public void start() {
    	/*
    		NOTE: Order of initialization is VERY IMPORTANT. DO NOT TOUCH!!!
    	 */
		//initializes engines
		while(!engines.isEmpty()) {
			AbstEngine engine = engines.remove(0);
			Thread t = new Thread(engine, engine.getName());
			t.start();
		}
		
		//initializes repositories
		LOG.debug("Initializing repositories...");
		try {
			//repositories must be initialized in THIS order
	    		rr.initialize();
	    		pr.initialize();
	    		dr.initialize();
		} catch(Exception e) {
			LOG.fatal("A repository has not initialized! BusinessMachine cannot start!", e);
			errorStartup(e, 0);
		}
		
		//initializes senders
		while(!senders.isEmpty()) {
			Sender sender = senders.remove(0);
			Thread t = new Thread(sender, sender.getName());
			t.start();
		}

		//run runnables on separate threads
        Thread t1 = new Thread(itm, itm.getClass().getSimpleName());
		Thread t3 = new Thread(controller, controller.getClass().getSimpleName());
		Thread t4 = new Thread(cirm, cirm.getClass().getSimpleName());
		t1.start();
		t3.start();
		t4.start();

		//sets repositories for adaptor manager
		am.setRepositories(pr, dr, rr);

		//initializes other Initializable objects
		try {
			inits.initializeAll();
		} catch (Exception e) {
			LOG.fatal("An initializable cannot be initialized!", e);
		}

		//updates Environment
		LOG.info("BusinessMachine started!");
	}

	//TASK: Avoid this!!!
    public static ApplicationContext getApplicationContext() {
    		return applicationContext;
    }
}