package eu.trade.repo.index.impl;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskRejectedException;

import eu.trade.repo.index.Index;
import eu.trade.repo.index.IndexExecutorSelector;
import eu.trade.repo.index.IndexSynchronizer;
import eu.trade.repo.index.IndexTask;
import eu.trade.repo.index.IndexTaskServiceFactory;
import eu.trade.repo.index.model.IndexOperation;


/**
 * Coordinates the process of register an index task. That is:
 *  - if necessary, creates the index task from the data received.
 *  - check with synchronizer whether it can be executed
 *  - choose using index executor selector, the task executor to be used to execute the index task
 *  - register the task in the executor.
 *  
 * @author abascis
 *
 */
public abstract class AbstractIndex implements Index{

	private static final Logger LOG = LoggerFactory.getLogger(AbstractIndex.class);

	public static final String ATOMIC_INDEX = "AtomicIndex"; 
	
	
	@Autowired
	private IndexTaskServiceFactory taskFactory;
	
	@Autowired
	private IndexSynchronizer indexSynchronizer;
	
	@Autowired
	private IndexExecutorSelector indexExecutorSelector;
	

	protected void executeIfAllowed(IndexTask indexTask, boolean waiting) {
		if (indexTask != null) {
			boolean executionAllowed = indexSynchronizer.putInQueue( indexTask, waiting );
			if ( executionAllowed ) {
				try {
					indexExecutorSelector.getTaskExecutor(indexTask).execute( indexTask );
					
				} catch (TaskRejectedException e) {
					LOG.warn("Index task rejected. This exception is ommitted since the content will be indexed by the 'Terminate unfinished indexes service'. Cause: " + e.getLocalizedMessage());
					indexSynchronizer.doOnTaskFinished(indexTask);
				}
			}else{
				LOG.info(">> index process not allowed for objectId {} at this moment (other index process is scheduled for that object) ", indexTask.getObjectId());
			}
		}
	}

	protected void executeIfAllowed(Integer repositoryId, Integer objectId, String fileName, BigInteger fileSize, boolean updateObjectInfo, IndexOperation operation, boolean waiting, String owner) {
		IndexTask indexTask = createTask(repositoryId, objectId, fileName, fileSize, updateObjectInfo, operation, owner);
		executeIfAllowed(indexTask, waiting);
	}
	

	protected IndexTask createTask(Integer repositoryId, Integer objectId, String fileName, BigInteger fileSize, boolean updateObjectInfo, IndexOperation operation, String owner) {
		IndexTask indexTask = taskFactory.getIndexService();
		indexTask.setRepositoryId(repositoryId);
		indexTask.setObjectId(objectId);
		indexTask.setOperation(operation);
		indexTask.setFileName(fileName);
		indexTask.setFileSize(fileSize);
		indexTask.setUpdateObjectIndexInfo(updateObjectInfo);
		indexTask.setOwner(owner);
		return indexTask;
	}

	/**
	 * @return the indexSynchronizer
	 */
	protected IndexSynchronizer getIndexSynchronizer() {
		return indexSynchronizer;
	}

}