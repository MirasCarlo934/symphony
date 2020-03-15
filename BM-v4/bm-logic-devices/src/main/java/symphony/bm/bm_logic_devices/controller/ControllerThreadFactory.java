package symphony.bm.bm_logic_devices.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ControllerThreadFactory implements ThreadFactory {
	private static final Logger LOG = LogManager.getLogger("controller.ThreadFactory");
	private AtomicInteger threads = new AtomicInteger(1);
	
	/*public ControllerThreadFactory() {
		System.out.println("HAHA");
	}*/

	@Override
	public Thread newThread(Runnable r) {
		LOG.trace("Thread " + threads + " created!");
		Thread t = new Thread(r, String.valueOf(threads.getAndIncrement()));
		return t;
	}
}
