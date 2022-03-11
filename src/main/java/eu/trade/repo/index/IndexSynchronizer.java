package eu.trade.repo.index;

import eu.trade.repo.index.model.IndexOperation.IndexOperationType;

/**
 * Responsible for coordinating the index tasks.
 * 
 * The complete index of a cmis object consists of the metadata index part and, if it has a related stream, the content index part.
 * Each of these index parts are independent, but in order to avoid index corruption and to improve performance, only a index task
 * of each type can be executed at a time.
 * 
 * That is, the first task is going to be executed. The second task of the same type is going to be reserved waiting for the first one
 * to finish. If in the meanwhile, a third task of the same type arrives, it is going to be reserved overriding the second one. This way
 * only the most recent tasks will be executed.
 * 
 * 
 * @author abascis
 *
 */
public interface IndexSynchronizer {

	/**
	 * Add the task to the appropriate queue (executing or waiting) based on whether there are other tasks executing or not.
	 * If waiting=false, the task is never added to the waiting queue.
	 * 
	 * Return true if the task can be executed (and was added to the executing queue) or false otherwise.
	 * 
	 * @param objectId
	 * @param task
	 * @return
	 */
	boolean putInQueue( IndexTask task, boolean waiting );

	/**
	 * Method used to clean synchronization data between index tasks over the same cmis object.
	 * 
	 * @param stoppedTask
	 */
	void doOnTaskFinished( IndexTask stoppedTask );

	/**
	 * Method used by index tasks to know if they have to stop its execution.
	 * 
	 * @param objectId
	 * @return
	 */
	boolean isOtherTaskWaiting( IndexOperationType operationType, Integer objectId );
	
	/**
	 * Method used to know if there are no index tasks executing or waiting.
	 * 
	 * @return
	 */
	boolean isIndexIdle();

	/**
	 * If there is a waiting task related to the given objectId, it is removed from the waiting queue and returned.
	 * If not, the return value is null
	 * 
	 * @param objectId
	 * @return
	 */
	IndexTask popWaitingTask( IndexOperationType operationType, Integer objectId );

}
