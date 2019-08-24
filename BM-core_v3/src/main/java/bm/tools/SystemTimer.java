package bm.tools;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public class SystemTimer extends Timer {
	private Logger LOG;
	private Vector<TimerTask> scheduledTasks = new Vector<TimerTask>(1,1);
	private int refreshRate;

	public SystemTimer(String logDomain, int refreshRate) {
		super("SystemTimer");
		LOG = Logger.getLogger(logDomain + ".SystemTimer");
		this.refreshRate = refreshRate;
	}
	
	@Override
	public void schedule(TimerTask task, long delay, long period) {
		scheduledTasks.addElement(task);
		super.schedule(task, delay, period);
	}
	
	/**
	 * The SystemTimer schedules a TimerTask that executes immediately and at an interval of the SystemTimer's
	 * standard refresh rate afterwards. 
	 * 
	 * @param timerTask The TimerTask to be scheduled
	 */
	public void schedule(TimerTask timerTask) {
		scheduledTasks.addElement(timerTask);
		super.schedule(timerTask, 0, refreshRate);
	}
	
	public TimerTask[] getScheduledTasks() {
		return (TimerTask[]) scheduledTasks.toArray();
	}
	
	public void printAllTasks(Priority logPriority) {
		String s = "Printing all scheduled tasks: \n"
				+ "Scheduled Tasks:";
		for(int i = 0; i < scheduledTasks.size(); i++) {
			TimerTask task = scheduledTasks.get(i);
			s += "\n-" + task.getClass().getSimpleName();
		}
		LOG.log(logPriority, s);
	}
	
	public int getRefreshRate() {
		return refreshRate;
	}
}
