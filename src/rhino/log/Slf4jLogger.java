package src.rhino.log;

import org.slf4j.spi.LocationAwareLogger;

public class Slf4jLogger implements Logger {

    private static final String ADAPTER_FQCN = Slf4jLogger.class.getName();

    private org.slf4j.Logger logger;
    
    private boolean isLocationAware;
    
    public Slf4jLogger(String loggerName) {
        logger = org.slf4j.LoggerFactory.getLogger(loggerName);
        if (logger instanceof LocationAwareLogger) {
            try {
                ((LocationAwareLogger)logger).log(null, ADAPTER_FQCN,
                        LocationAwareLogger.DEBUG_INT, "init slf4j logger", null, null);
                isLocationAware = true;
            } catch(Throwable t) {
                isLocationAware = false;
            }
        }
    }

    @Override
    public void debug(String message) {
        if (isLocationAware) {
            ((LocationAwareLogger)logger).log(null, ADAPTER_FQCN,
                    LocationAwareLogger.DEBUG_INT, message, null, null);
        } else {
            logger.debug(message);
        }
    }

    @Override
    public void info(String message) {
        if (isLocationAware) {
            ((LocationAwareLogger)logger).log(null, ADAPTER_FQCN,
                    LocationAwareLogger.INFO_INT, message, null, null);
        } else {
            logger.info(message);
        }
    }

    @Override
    public void warn(String message) {
        warn(message, null);
    }

    @Override
    public void warn(String message, Throwable t) {
        if (isLocationAware) {
            ((LocationAwareLogger)logger).log(null, ADAPTER_FQCN,
                    LocationAwareLogger.WARN_INT, message, null, t);
        } else {
            logger.warn(message, t);
        }
    }

    @Override
    public void error(String message) {
        error(message, null);
    }

    @Override
    public void error(String message, Throwable t) {
        if (isLocationAware) {
            ((LocationAwareLogger)logger).log(null, ADAPTER_FQCN,
                    LocationAwareLogger.ERROR_INT, message, null, t);
        } else {
            logger.error(message, t);
        }
    }

}
