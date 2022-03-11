package eu.trade.repo.delegates;

import java.util.List;

import eu.trade.repo.index.model.IndexOperation;
import eu.trade.repo.index.model.IndexOperation.IndexOperationType;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.IndexingState;
import eu.trade.repo.model.Property;

/**
 * Responsible for managing information related to cmis object indexes in the CMIS database
 */
public interface IndexLinkedDelegate {

	/**
	 * Checks whether the object still exists.
	 * @param objectId
	 * @return
	 */
	CMISObject getCmisObject(Integer objectId);

	/**
	 * Updates the appropriated indexing state and indexing tries fields in cmis object depending on the index operation type
	 * @param objectId
	 * @param state
	 * @param indexOperation
	 */
	void updateObjectIndexingState(Integer objectId, IndexingState state, IndexOperationType operationType);

	/**
	 * Retrieves a index part (metadata or content) indexing state of a cmis object
	 * @param objectId
	 * @param operationType
	 * @return
	 */
	IndexingState getObjectIndexingState(Integer objectId, IndexOperationType operationType);

	/**
	 * Retrieves the metadata (properties of type ='string') of a cmis object
	 * @param objectId
	 * @return
	 */
	List<Property> obtainPropertyListByObjectId(Integer objectId);
}
