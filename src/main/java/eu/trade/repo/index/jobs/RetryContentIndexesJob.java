package eu.trade.repo.index.jobs;

import java.math.BigInteger;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.delegates.IndexDelegate;
import eu.trade.repo.index.impl.IndexConfigHolder;
import eu.trade.repo.index.jobs.JobIndexExecutorsHelper.IndexExecutorPool;
import eu.trade.repo.index.model.IndexObjectDTO;
import eu.trade.repo.index.model.IndexOperation;


/**
 * Background job to retry content indexes in error or not done. Fires index tasks.
 * 
 * @see JobIndexExecutorsHelper
 */
public class RetryContentIndexesJob extends AbstractIndexBackgroundJob {

	private static final Logger LOG = LoggerFactory.getLogger(RetryContentIndexesJob.class);

	@Autowired
	private IndexDelegate indexDelegate;

	@Autowired
	private JobIndexExecutorsHelper executorsHelper;

	@Autowired
	private IndexConfigHolder indexConfigHolder;



	/**
	 * Tries to re-index a list of documents containing those that are pending to be indexed (indexing_state_content = NONE) and those for which indexation process 
	 * has raised an error (indexing_state_content = ERROR) and they have not reached de MAX_ATTEMPTS number of indexing attempts yet.
	 * 
	 * The list of objects is shrunk to meet the 'index.pool.queueCapacity' limit.
	 * 
	 * The execution of this job is skipped if the remaining capacity of the task executor pool is below the percentage indicated by the 
	 * 'index.background.retryInErrorIndexes.remainingQueueCapacityThreshold' property.
	 *
	 * WARNING!!! If the task executor's implementation used by this class is not org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
	 * or the 'index.pool.queueCapacity' property is not informed the maximum size of the document list is Integer.MAX_VALUE.
	 * 
	 * WARNING!!! If the task executor's implementation used by this class is not org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
	 * the job execution is never skipped.
	 * 
	 */

	public void execute() {
		LOG.info("Starting the retry content indexes in states 'error' or 'not indexed yet' service...");

		searchAndRegisterDocumentsToIndex(IndexExecutorPool.INDEX_POOL_LARGE_FILES);
		searchAndRegisterDocumentsToIndex(IndexExecutorPool.INDEX_POOL_SMALL_FILES);

		LOG.info("Retry content indexes in states 'error' or 'not indexed yet' Finished.");
	}


	private void searchAndRegisterDocumentsToIndex( IndexExecutorPool pool ){

		String poolName = pool.getTargetTasksDescription();
		if(!executorsHelper.poolIsFull(pool)) {
			LOG.info("Starting processing {}..", poolName);

			int minSize = (IndexExecutorPool.INDEX_POOL_LARGE_FILES == pool)? indexConfigHolder.getQueueSelectionLimitSize() : -1;
			int maxSize = (IndexExecutorPool.INDEX_POOL_LARGE_FILES == pool)? -1: indexConfigHolder.getQueueSelectionLimitSize();

			List<IndexObjectDTO> objectList = indexDelegate.obtainContentIndexesInErrorOrUnfinishedState(
					indexConfigHolder.getMaxIndexAttempts(), 
					executorsHelper.getPoolRemainingCapacity(pool),
					minSize,
					maxSize);
			LOG.info("Found {} {} in 'error' or 'not indexed yet' indexing state", (null != objectList)? objectList.size() : 0, poolName); 
			registerIndexTasksForErrorAndUnfinishedIndexList( objectList );

		}else{
			LOG.info("Index thread pool capacity for {} is above the thresold configured", poolName);
		}
	}

	private void registerIndexTasksForErrorAndUnfinishedIndexList(List<IndexObjectDTO> objectList){
		if( null != objectList ){
			for (IndexObjectDTO obj : objectList) {
				try {
					LOG.debug("Content indexing process begin for objectId: {}", obj.getObjectId());
					executeOperation(obj.getRepositoryId(), obj.getObjectId(), obj.getFileName(), obj.getFileSize(), true, IndexOperation.CONTENT_INDEX);

				} catch(Exception e) {
					LOG.warn("Exception trying to index the content of object id:" + ((null != obj)? obj.getObjectId() : "obj is null"), e);
				}
			}
		}
	}


	@Override
	public void executeOperation(Integer repositoryId, Integer objectId, String fileName, BigInteger fileSize, boolean updateObjectInfo, IndexOperation operation) {
		LOG.debug("Firing indexing operation {} for cmis object: {} ( file name: {})", operation, objectId, fileName);
		executeIfAllowed(repositoryId, objectId, fileName, fileSize, updateObjectInfo, operation, false, getOwner(OWNER_JOB_RETRY_CONTENT_INDEX_IN_ERROR));
	}

}
