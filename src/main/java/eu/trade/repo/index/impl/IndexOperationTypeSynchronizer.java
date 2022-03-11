package eu.trade.repo.index.impl;

import java.util.concurrent.ConcurrentHashMap;

import eu.trade.repo.index.IndexTask;


/**
 * The IndexOperationTypeSynchronizer is an object to register which index tasks of a concrete IndexOperationType are to be executed by the index.
 * 
 * Internally, it has two maps: one is to register the tasks ready to be added to the task executor of the index (executingMap)
 * and the other is to register the received tasks related to a cmis object having already a task in the executingMap (waitingMap).
 * 
 * The waitingMap records only the last received task for a cmis object having already a task in the executingMap. 
 * That is, for a cmis object, it can be at most one task in the executingMap an another in the waitingMap. 
 *  
 */
public class IndexOperationTypeSynchronizer {

	private final ConcurrentHashMap<Integer, Integer> indexExecutingMap;
	private final ConcurrentHashMap<Integer, IndexTask> indexWaitingMap;

	public IndexOperationTypeSynchronizer(){
		indexExecutingMap = new ConcurrentHashMap<Integer, Integer>();
		indexWaitingMap = new ConcurrentHashMap<Integer, IndexTask>();
	}

	public boolean isOtherTaskWaiting(Integer objectId){
		return (null != indexWaitingMap.get(objectId) );
	}

	public boolean isIndexIdle(){
		return indexExecutingMap.isEmpty() && indexWaitingMap.isEmpty();
	}

	public void doOnTaskFinished( IndexTask stoppedTask ){
		indexExecutingMap.remove(stoppedTask.getObjectId());
	}

	/**
	 * Depending on the objectId of the task, the waiting parameter and the content of the map of tasks to be executed (executingMap),
	 * the new task is registered in the map of tasks to be executed, or in the map of waiting tasks or in neither of both.
	 * 
	 * - executingMap does not have another task for the same objectId: the new task is registered in this map and the return value is true.
	 * - executingMap has another task for the same objectId: the return value is false
	 *  	-> if the waiting parameter is equals true: the new task is registered in the waiting tasks map
	 * 		-> if the waiting parameter is equals false: the new task is not registered.
	 * 
	 */
	public boolean putInQueue(IndexTask task, boolean waiting ){
		Integer oldHashCode = indexExecutingMap.putIfAbsent( task.getObjectId(), Integer.valueOf(task.hashCode()) );

		if( oldHashCode != null && !oldHashCode.equals(Integer.valueOf(task.hashCode())) ){
			if( waiting ){
				indexWaitingMap.put( task.getObjectId(), task );
			}
			return false;
		}
		return true;
	}

	public IndexTask popWaitingTask(Integer objectId) {
		return indexWaitingMap.remove(objectId);
	}

}
