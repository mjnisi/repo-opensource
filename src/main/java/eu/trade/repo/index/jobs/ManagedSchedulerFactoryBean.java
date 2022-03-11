package eu.trade.repo.index.jobs;

import org.apache.commons.configuration.Configuration;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.SchedulingException;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import eu.trade.repo.util.Constants;


/**
 * {@link FactoryBean} that creates and configures a Quartz {@link org.quartz.Scheduler},
 * manages its lifecycle as part of the Spring application context, and exposes the
 * Scheduler as bean reference for dependency injection.
 * 
 * It starts up depending on configuration:
 * 
 * index.background.enabled = true
 * 
 */
public class ManagedSchedulerFactoryBean extends SchedulerFactoryBean{
	private static final Logger LOG = LoggerFactory.getLogger(ManagedSchedulerFactoryBean.class);

	private boolean initialized;
	@Autowired
	private Configuration indexConfig;

	public ManagedSchedulerFactoryBean(){
		super();
		setAutoStartup(false);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (indexConfig.getBoolean(Constants.PROPNAME_INDEX_BACKGROUND_ENABLED, Constants.DEFAULT_BACKGROUND_ENABLED)) {
			setAutoStartup(true);
			afterPropertiesSetInternal();
		}
	}

	@Override
	public void start() throws SchedulingException {

		if( !initialized ){
			try{
				afterPropertiesSetInternal();
			}catch(Exception e){
				LOG.error("Error initializing schedulerFactoryBean: {}", e.getMessage(), e);
				return;
			}
		}
		if( initialized ){
			super.start();
		}
	}

	@Override
	public void stop() throws SchedulingException {
		if( initialized ){
			super.stop();
		}
	}

	/**
	 * Shut down the Quartz scheduler on bean factory shutdown,
	 * stopping all scheduled jobs.
	 */
	public void destroy() throws SchedulerException {
		if( initialized ){
			super.destroy();
		}
	}

	private void afterPropertiesSetInternal() throws Exception{
		super.afterPropertiesSet();
		initialized = true;
	}
}
