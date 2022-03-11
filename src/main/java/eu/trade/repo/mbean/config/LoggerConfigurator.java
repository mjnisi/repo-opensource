package eu.trade.repo.mbean.config;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.Log4jLoggerAdapter;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 * MBean which allows clients to change or retrieve the logging level for a
 * Log4j Logger at runtime.
 * 
 */
@Component
@ManagedResource(objectName = LoggerConfigurator.MBEAN_NAME, 
description = "Allows clients to set the Log4j Logger level at runtime")
public class LoggerConfigurator {

	private static final Logger LOG = LoggerFactory.getLogger(LoggerConfigurator.class);

	public static final String MBEAN_NAME = "trade.repo:type=config,name=LoggingConfiguration";

	@ManagedOperation(description = "Returns the Logger LEVEL for the given logger name")
	@ManagedOperationParameters({ @ManagedOperationParameter(description = "The Logger Name", name = "loggerName") })
	public String getLoggerLevel(String loggerName) {

		Level loggerLevel = null;
		
		org.apache.log4j.Logger logger = getLogger(loggerName);
		if( null == logger ){
			LOG.info("Get logger level operation available only for log4j implementation");
		}else{
			loggerLevel = logger.getEffectiveLevel();
		}

		return (null == loggerLevel)? new StringBuilder("The logger ").append(loggerName).append(" has not level").toString(): loggerLevel.toString();
	}

	@ManagedOperation(description = "Set Logger Level")
	@ManagedOperationParameters({
		@ManagedOperationParameter(description = "The Logger Name", name = "loggerName"),
		@ManagedOperationParameter(description = "The Level to which the Logger must be set", name = "loggerLevel") })
	public void setLoggerLevel(String loggerName, String loggerLevel) {

		org.apache.log4j.Logger logger = getLogger(loggerName);
		
		if( null != logger ){
			logger.setLevel(Level.toLevel(loggerLevel, Level.INFO));
			LOG.info("Set logger {} to level {}", loggerName, logger.getLevel());
		}else{
			LOG.info("Set logger level operation available only for log4j implementation");
		}

	}


	private org.apache.log4j.Logger getLogger(String loggerName){
		org.apache.log4j.Logger logger = null;
		if( "ROOT".equals(loggerName) ){
			logger = org.apache.log4j.Logger.getRootLogger();
		}else {
			Logger slf4logger = LoggerFactory.getLogger(loggerName);
			if (slf4logger instanceof Log4jLoggerAdapter) {		
				logger = org.apache.log4j.Logger.getLogger(loggerName);
			}
		}
		return logger;
	}
}
