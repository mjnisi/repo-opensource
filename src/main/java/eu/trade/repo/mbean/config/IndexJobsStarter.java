package eu.trade.repo.mbean.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import eu.trade.repo.index.jobs.ManagedSchedulerFactoryBean;

/**
 * MBean which allows clients to change or retrieve the logging level for a
 * Log4j Logger at runtime.
 * 
 */
@Component
@ManagedResource(objectName = IndexJobsStarter.MBEAN_NAME, 
description = "Allows clients to turn on / turn off the background jobs used by the full content index at runtime")
public class IndexJobsStarter {

	private static final Logger LOG = LoggerFactory.getLogger(IndexJobsStarter.class);

	public static final String MBEAN_NAME = "trade.repo:type=config,name=IndexJobsConfiguration";

	@Autowired
	private ManagedSchedulerFactoryBean schedulerFactoryBean;

	@ManagedOperation(description = "Turns ON the index background jobs")
	public void start() {
		try{
			schedulerFactoryBean.start();
			LOG.info("SchedulerFactoryBean INITIALIZED");
		}catch(Exception e){
			LOG.error(e.getLocalizedMessage(), e);
		}
	}

	@ManagedOperation(description = "Turns OFF the index background jobs")
	public void stop() {
		try{
			schedulerFactoryBean.stop();
			LOG.info("SchedulerFactoryBean STOPPED");
		}catch(Exception e){
			LOG.error(e.getLocalizedMessage(), e);
		}
	}


}
