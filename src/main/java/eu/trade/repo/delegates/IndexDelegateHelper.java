package eu.trade.repo.delegates;

import java.util.List;
import java.util.Map;

import eu.trade.repo.index.model.IWordObjectExtractor;
import eu.trade.repo.index.model.IndexOperation.IndexOperationType;
import eu.trade.repo.index.model.TransientDTO;
import eu.trade.repo.index.model.WordDTO;

public interface IndexDelegateHelper {


	Map<String, ? extends IWordObjectExtractor> writeWordObjectPage(Integer repositoryId, Integer objectId, List<TransientDTO> partialWordObjectList, final Map<String, WordDTO> wordMap, IndexOperationType operationType);

	<T extends IWordObjectExtractor> void writeWordPositionPage(Integer repositoryId, Integer documentId, List<TransientDTO> partialWordPositionList, Map<String, T> wordObjectMap);

	int deletePermanentIndexPartPage(List<Integer> wordObjectIdList);

	void writeWords(Integer repositoryId, Integer documentId, List<String> wordList);

}
