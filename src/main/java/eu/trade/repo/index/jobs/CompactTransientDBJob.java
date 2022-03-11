package eu.trade.repo.index.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.delegates.DBTransientDelegate;
import eu.trade.repo.index.IndexSynchronizer;


/**
 * Background job to shutdown the transient index database when there are no index task execution pending. 
 * When the database is closed, then the database is also fully compacted.
 * 
 */
public class CompactTransientDBJob {
	private static final Logger LOG = LoggerFactory.getLogger(CompactTransientDBJob.class);

	@Autowired
	private DBTransientDelegate dbTransientDelegate;

	@Autowired
	private IndexSynchronizer indexSynchronizer;

	public void execute() {
		LOG.info("Starting the compact transient database service...");
		if ( indexSynchronizer.isIndexIdle() ){
			LOG.warn("Transient database is going to shutdown to be compacted.");
			dbTransientDelegate.shutdown();
		}
		LOG.info("Compact transient database service Finished.");
	}

}
