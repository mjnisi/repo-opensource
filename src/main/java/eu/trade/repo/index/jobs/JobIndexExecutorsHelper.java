package eu.trade.repo.index.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import eu.trade.repo.index.impl.IndexConfigHolder;


/**
 * Helper to deal with task executors capacity.
 * 
 * WARNING!! The behaviour of this class depends on whether the implementation chosen for the taskExecutor is 
 * org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor or other.
 */
public class JobIndexExecutorsHelper {

	private static final Logger LOG = LoggerFactory.getLogger(JobIndexExecutorsHelper.class);

	public enum IndexExecutorPool{
		INDEX_POOL_METADATA ("cmis object's metadata"),
		INDEX_POOL_SMALL_FILES ("small documents"),
		INDEX_POOL_LARGE_FILES ("large documents");
		
		private final String targetTasksDescription;
		IndexExecutorPool(String targetTasksDescription){
			this.targetTasksDescription = targetTasksDescription;
		}
		public String getTargetTasksDescription() {
			return targetTasksDescription;
		}
	}

	@Autowired
	@Qualifier("taskExecutorSmallTasks")
	private TaskExecutor taskExecutor;

	@Autowired
	@Qualifier("taskExecutorLargeTasks")
	private TaskExecutor taskExecutorLarge;

	@Autowired
	@Qualifier("taskExecutorMetadataTasks")
	private TaskExecutor taskExecutorMetadata;

	@Autowired
	private IndexConfigHolder indexConfigHolder;


	public boolean poolIsFull( IndexExecutorPool pool ){
		boolean isFull = false;
		TaskExecutor taskExec = getTaskExecutor( pool );

		if( taskExec instanceof ThreadPoolTaskExecutor ){
			ThreadPoolTaskExecutor tbEx = (ThreadPoolTaskExecutor)taskExec;
			isFull = tbEx.getThreadPoolExecutor().getQueue().remainingCapacity() < getPoolCapacity(pool) * indexConfigHolder.getRemainingPoolQueueThreshold();
			if( LOG.isDebugEnabled() ){
				LOG.debug("Pool queue remaining capacity: {}", tbEx.getThreadPoolExecutor().getQueue().remainingCapacity());
				LOG.debug("Pool queue used capacity: {}", tbEx.getThreadPoolExecutor().getQueue().size());
			}
		}
		LOG.info("Pool is over thresold? {}", isFull);
		return isFull;
	}

	public int getPoolCapacity( IndexExecutorPool pool ){
		int capacity = 0;
		switch (pool) {
		case INDEX_POOL_METADATA:
			capacity = indexConfigHolder.getQueueMetadataTasksCapacity();
			break;

		case INDEX_POOL_LARGE_FILES:
			capacity = indexConfigHolder.getQueueLargeTasksCapacity();
			break;

		default:
			capacity = indexConfigHolder.getQueueSmallTasksCapacity();
			break;
		}
		return capacity;
	}

	public int getPoolRemainingCapacity( IndexExecutorPool pool ){
		TaskExecutor taskExec = getTaskExecutor( pool );
		int remainingCapacity = 0;
		if( taskExec instanceof ThreadPoolTaskExecutor ){
			ThreadPoolTaskExecutor tbEx = (ThreadPoolTaskExecutor)taskExec;
			remainingCapacity = tbEx.getThreadPoolExecutor().getQueue().remainingCapacity();
		}else{
			remainingCapacity = getPoolCapacity( pool );
		}
		return remainingCapacity;
	}

	private TaskExecutor getTaskExecutor(IndexExecutorPool pool){
		TaskExecutor executor = null;
		switch (pool) {
		case INDEX_POOL_METADATA:
			executor = taskExecutorMetadata;
			break;

		case INDEX_POOL_LARGE_FILES:
			executor = taskExecutorLarge;
			break;

		default:
			executor = taskExecutor;
			break;
		}
		return executor;
	}
}
