package bm.main.controller;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

public class ControllerThreadFactory implements ThreadFactory {
	private static final Logger LOG = Logger.getLogger("controller.ThreadFactory");
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
