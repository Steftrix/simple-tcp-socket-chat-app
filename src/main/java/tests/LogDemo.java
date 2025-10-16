package tests;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class LogDemo {
    private static final Logger logger = LoggerFactory.getLogger(LogDemo.class);

    public static void main(String[] args) {
        logger.trace("Trace message");
        logger.debug("Debug message");
        logger.info("Info message");
        logger.warn("Warning message");
        logger.error("Error message");
        }
    }
