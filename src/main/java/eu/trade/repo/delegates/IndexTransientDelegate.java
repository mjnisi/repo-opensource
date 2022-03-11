package eu.trade.repo.delegates;

import java.util.List;

import eu.trade.repo.index.model.TransientDTO;

/**
 * Manages operations on the transient index
 */
public interface IndexTransientDelegate {

	List<String> obtainWordPageByObjectId(Integer objectId, int start, int pageSize);

	List<TransientDTO> obtainWordObjectPageByObjectId(Integer objectId, List<String> wordList);

	List<TransientDTO> obtainWordPositionPageByObjectId(Integer objectId, List<String> wordList, int start, int pageSize);

	List<Integer> obtainObjectTypePropertyIdListFromTransientByObjectId(Integer objectId);
	
	int deleteTransientContentIndexByObjectId(Integer objectId, int deletePageSize);

}
