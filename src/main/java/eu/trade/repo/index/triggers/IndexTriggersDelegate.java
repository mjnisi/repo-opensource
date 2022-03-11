package eu.trade.repo.index.triggers;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import eu.trade.repo.delegates.IndexDelegate;

/**
 * Utility class to initialize and register changes in ChangeTrackerMap
 */
public class IndexTriggersDelegate {

	private static final String TRACKER_MAP_KEY = "indexTrackerMap";

	@Autowired
	private IndexDelegate indexDelegate;

	public void initChangeTrackerMap(){
		if ( null == TransactionSynchronizationManager.getResource(IndexTriggersDelegate.TRACKER_MAP_KEY) ){
			TransactionSynchronizationManager.bindResource(IndexTriggersDelegate.TRACKER_MAP_KEY, new ChangeTrackerMap());
		}
	}

	/**
	 * Retrieves the ChangeTrackerMap bounded to the current thread.
	 * @return ChangeTrackerMap or null if not bounded
	 */
	public ChangeTrackerMap getChangeTrackerMap(){
		return (ChangeTrackerMap)TransactionSynchronizationManager.getResource(IndexTriggersDelegate.TRACKER_MAP_KEY);
	}

	public void registerPropertyChange(Integer objectId){
		ChangeTrackerMap tracker = getChangeTrackerMap();
		if( null != tracker ){
			tracker.registerPropertyChange(objectId);
		}
	}

	public  void registerCMISObjectRemoval(Integer objectId){
		ChangeTrackerMap tracker = getChangeTrackerMap();
		if( null != tracker ){
			tracker.registerCMISObjectRemoval(objectId);
		}
	}

	public  void registerStreamChange(Integer objectId, StreamChangeType changeType, BigInteger fileSize){
		ChangeTrackerMap tracker = getChangeTrackerMap();
		if( null != tracker ){
			tracker.registerStreamChange(objectId, changeType, fileSize);
		}
	}

	public Map<Integer,Integer> getRepositoryByObjectMap(Set<Integer> changedObjectIds){
		return indexDelegate.obtainRepositoryByObjectList(changedObjectIds);
	}

}
