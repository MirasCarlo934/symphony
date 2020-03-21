package symphony.bm.bmlogicdevices.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadRejectionHandler implements RejectedExecutionHandler {
    private static final Logger LOG = LogManager.getLogger("controller.ThreadRejectionHandler");

    public ThreadRejectionHandler() {

    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        LOG.error("Failed to processRequest the received request due to system overload!");
    }
}
