package eu.trade.repo.index.impl;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import eu.trade.repo.index.IndexEvent;
import eu.trade.repo.index.IndexTask;
import eu.trade.repo.index.model.IndexOperation;


/**
 * Fires indexing tasks.
 * 
 * It is used by cmis services to fire ATOMIC_INDEX operations after cmis object stream or properties have been changed.
 * 
 */
public class IndexImpl extends AbstractIndex implements ApplicationListener<IndexEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(IndexImpl.class);

	@Override
	public void executeOperation(Integer repositoryId, Integer objectId, String fileName, BigInteger fileSize, boolean updateObjectInfo, IndexOperation operation) {
		LOG.debug("Firing indexing operation {} for cmis object: {} ( file name: {})", operation, objectId, fileName);
		executeIfAllowed(repositoryId, objectId, fileName, fileSize, updateObjectInfo, operation, true, ATOMIC_INDEX);
	}

	/**
	 * Listen to indexSynchronizer, to capture the indexEvent thrown at the end of any indexTask execution.
	 * It gets tasks in the waiting queue and activate them.
	 */
	@Override
	public void onApplicationEvent(IndexEvent event) {
		IndexTask task = null;
		if( event.getSource() instanceof IndexTask ){
			task = (IndexTask)event.getSource();
			LOG.debug("End task event thrown by executing index task for object {} ({}) listened", task.getObjectId(), task.getFileName());
			executeIfAllowed(getIndexSynchronizer().popWaitingTask(task.getOperation().getType(), task.getObjectId()), true);
		}
	}
}