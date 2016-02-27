package se.kth.id2203.jbstore.system;

import org.slf4j.LoggerFactory;

public class Log {

    private final org.slf4j.Logger logger;

    public Log(String name) {
        logger = LoggerFactory.getLogger(name);
    }

    public void info(String event, long time, String msg) {
        logger.info("{}({}):\t{}", event, time, msg);
    }
}
