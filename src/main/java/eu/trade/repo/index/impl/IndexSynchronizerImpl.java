package eu.trade.repo.index.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import eu.trade.repo.index.IndexEvent;
import eu.trade.repo.index.IndexSynchronizer;
import eu.trade.repo.index.IndexTask;
import eu.trade.repo.index.model.IndexOperation.IndexOperationType;


/**
 * The IndexSynchronizer is an object to register which index tasks are to be executed by the index, regardless of its index operation type.
 * 
 * Internally, it uses two IndexOperationTypeSynchronizer objects: one to deal with METADATA index operation type tasks and the other with CONTENT tasks.
 * 
 * @author abascis
 */
public class IndexSynchronizerImpl implements IndexSynchronizer, ApplicationEventPublisherAware{

	private final IndexOperationTypeSynchronizer metadataSynchronizer;
	private final IndexOperationTypeSynchronizer contentSynchronizer;
	private ApplicationEventPublisher publisher;

	public IndexSynchronizerImpl(){
		metadataSynchronizer = new IndexOperationTypeSynchronizer();
		contentSynchronizer = new IndexOperationTypeSynchronizer();
	}
	

	@Override
	public boolean isIndexIdle(){
		return metadataSynchronizer.isIndexIdle() && contentSynchronizer.isIndexIdle();
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
	@Override
	public boolean putInQueue(IndexTask task, boolean waiting ){
		return getSynchronizer(task).putInQueue(task, waiting);
	}

	
	@Override
	public boolean isOtherTaskWaiting(IndexOperationType operationType, Integer objectId){
		return getSynchronizer(operationType).isOtherTaskWaiting(objectId);
	}
	
	@Override
	public IndexTask popWaitingTask(IndexOperationType operationType, Integer objectId) {
		return getSynchronizer(operationType).popWaitingTask(objectId);
	}
	
	
	@Override
	public void doOnTaskFinished( IndexTask stoppedTask ){
		getSynchronizer(stoppedTask).doOnTaskFinished(stoppedTask);
		publisher.publishEvent(new IndexEvent(stoppedTask));
	}
	
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.publisher = applicationEventPublisher;
	}

	private IndexOperationTypeSynchronizer getSynchronizer(IndexTask task){
		return getSynchronizer(task.getOperation().getType());
	}
	
	private IndexOperationTypeSynchronizer getSynchronizer(IndexOperationType operationType){
		return (IndexOperationType.CONTENT.equals(operationType))? contentSynchronizer : metadataSynchronizer;
	}
}
