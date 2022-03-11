package eu.trade.repo.index.triggers;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Utility class to register and retrieve cmis:object IDs which have changed properties, changed streams or have been removed.
 *
 */
public class ChangeTrackerMap{

	private ConcurrentHashMap<Integer, IndexChangeData> changedPropertyMap;
	private ConcurrentHashMap<Integer, IndexChangeData> removedObjectMap;
	private ConcurrentHashMap<Integer, IndexChangeData> streamMap;

	public ChangeTrackerMap(){
		reset();
	}

	public void registerPropertyChange(Integer objectId){
		changedPropertyMap.put(objectId, new IndexChangeData(objectId));
	}

	public void registerCMISObjectRemoval(Integer objectId){
		removedObjectMap.put(objectId, new IndexChangeData(objectId));
	}

	public void registerStreamChange(Integer objectId, StreamChangeType changeType, BigInteger fileSize){
		streamMap.put(objectId, new IndexChangeData(objectId, changeType, fileSize));
	}

	public Set<IndexChangeData> getCMISObjectsHavingMetadataChanged(){
		Set<IndexChangeData> values = new HashSet<IndexChangeData>(changedPropertyMap.values());
		values.removeAll(removedObjectMap.values());
		return values;
	}

	public Set<IndexChangeData> getCMISObjectsHavingStreamChangedOrRemoved(){
		Set<IndexChangeData> keys = new HashSet<IndexChangeData>(streamMap.values());
		keys.removeAll(removedObjectMap.values());
		return keys;
	}

	public Set<IndexChangeData> getCMISObjectsRemoved(){
		return new HashSet<IndexChangeData>(removedObjectMap.values());
	}

	public Set<Integer> getChangedObjectIds(){
		Set<Integer> objectIdSet = new HashSet<Integer>(changedPropertyMap.keySet());
		objectIdSet.addAll(removedObjectMap.keySet());
		objectIdSet.addAll(streamMap.keySet());
		return objectIdSet;
	}

	
	public final void reset() {
		changedPropertyMap = new ConcurrentHashMap<Integer, IndexChangeData>();
		removedObjectMap = new ConcurrentHashMap<Integer, IndexChangeData>();
		streamMap = new ConcurrentHashMap<Integer, IndexChangeData>();
	}
}
