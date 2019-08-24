package bm.main.controller;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool extends ThreadPoolExecutor {
	private int rrn = 1;

	/*public ThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	public ThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
	}

	public ThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
	}*/

	public ThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime,
			ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, 
				new ArrayBlockingQueue<Runnable>(10), threadFactory, handler);
	}

	/**
	 * Renames thread to include processRequest number
	 */
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		String s = t.getName();
		if(s.contains(":")) {
			s = s.substring(0, s.indexOf(":")) + ":Process" + rrn;
		} else {
			s = "Thread" + s + ":Process" + rrn;
		}
		t.setName(s);
		rrn++;
	}
}
