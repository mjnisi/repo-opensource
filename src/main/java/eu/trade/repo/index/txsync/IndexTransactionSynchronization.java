package eu.trade.repo.index.txsync;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronization;

import eu.trade.repo.index.Index;
import eu.trade.repo.index.model.IndexOperation;
import eu.trade.repo.index.triggers.ChangeTrackerMap;
import eu.trade.repo.index.triggers.IndexChangeData;
import eu.trade.repo.index.triggers.IndexTriggersDelegate;
import eu.trade.repo.index.triggers.StreamChangeType;

public class IndexTransactionSynchronization implements TransactionSynchronization{

	@Autowired
	private Index index;
	
	@Autowired
	private IndexTriggersDelegate indexTriggersDelegate;
	
	/* TransactionSynchronization  */
	@Override
	public void afterCommit() {		
		ChangeTrackerMap tracker = indexTriggersDelegate.getChangeTrackerMap();
		try {
			Map<Integer, Integer> repositoryMap = indexTriggersDelegate.getRepositoryByObjectMap(tracker.getChangedObjectIds());
			
			
			Set<IndexChangeData> objectMetadataChangedList = tracker.getCMISObjectsHavingMetadataChanged();
			Integer repositoryId = null;
			for (IndexChangeData change : objectMetadataChangedList) {
				repositoryId = repositoryMap.get(change.getObjectId());
				if( null == repositoryId ){
					continue;
				}
				index.executeOperation(repositoryMap.get(change.getObjectId()), change.getObjectId(), null, null, true, IndexOperation.METADATA_INDEX);
			}
			
			Set<IndexChangeData> objectStreamChangedList = tracker.getCMISObjectsHavingStreamChangedOrRemoved();
			for (IndexChangeData change : objectStreamChangedList) {
				repositoryId = repositoryMap.get(change.getObjectId());
				if( null == repositoryId ){
					continue;
				}
				if( StreamChangeType.DELETE.equals(change.getChangeType()) ){
					index.executeOperation(repositoryMap.get(change.getObjectId()), change.getObjectId(), null, change.getFileSize(), true, IndexOperation.CONTENT_INDEX_DELETE);
				}else{
					index.executeOperation(repositoryMap.get(change.getObjectId()), change.getObjectId(),  null, change.getFileSize(), true, IndexOperation.CONTENT_INDEX);
				}
			}
			Set<IndexChangeData> objectRemovedList = tracker.getCMISObjectsRemoved();
			for (IndexChangeData change : objectRemovedList) {
				repositoryId = repositoryMap.get(change.getObjectId());
				if( null == repositoryId ){
					continue;
				}
				index.executeOperation(repositoryMap.get(change.getObjectId()), change.getObjectId(), null, null, false, IndexOperation.METADATA_INDEX_DELETE);
				index.executeOperation(repositoryMap.get(change.getObjectId()), change.getObjectId(), null, null, false, IndexOperation.CONTENT_INDEX_DELETE);
			}
			
		} finally {			
			//ensuring that the tracker list is reseted no matter the code run with / without any error
			tracker.reset();			
		}
	}

	@Override
	public void suspend() {}
	@Override
	public void resume() {}
	@Override
	public void flush() {}
	@Override
	public void beforeCommit(boolean readOnly) {}
	@Override
	public void beforeCompletion() {}
	@Override
	public void afterCompletion(int status) {}


}
