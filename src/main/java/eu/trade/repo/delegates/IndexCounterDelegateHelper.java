package eu.trade.repo.delegates;

import java.util.List;
import java.util.Map;

import eu.trade.repo.index.model.WordCounterDTO;
import eu.trade.repo.index.model.IndexOperation.IndexOperationType;

public interface IndexCounterDelegateHelper {

	void incrementIndexCounterWordObjectPage(List<Map<String, ?>> paramList);

	void decrementIndexCounterWordObjectPage(List<Integer> wordObjectCounterIdList);

	List<Integer> obtainIndexCounterWordObjectIdListFromWordObjectIds(List<Integer> wordObjectIdList);
	
	
	void deleteIndexObjectTokenCounters(Integer objectId, IndexOperationType operationType);
	
	void insertIndexObjectTokenCounters(Integer objectId, IndexOperationType operationType);
	
	void updateIndexRepoCounters(Integer repositoryId, Integer objectId, int increment);

	void updateIndexRepoCounters(Integer repositoryId, List<Integer> objectTypePropIdList, int increment);
	
	void writeObjectCountersByRepository(Integer repositoryId, List<Integer> objTypePropList);

	void writeObjectCountersByWord(List<Integer> wordIdList, List<Integer> objectTypePropertyIdList, Map<Integer, Map<Integer, WordCounterDTO>> existingWordCounterMap);

}
