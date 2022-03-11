package eu.trade.repo.index;

import java.math.BigInteger;

import eu.trade.repo.index.model.IndexOperation;


/**
 * Entry point to execute indexing tasks.
 * 
 */
public interface Index {

	/**
	 * Triggers the indexation of a cmis object part (metadata or content)
	 * 
	 * @see IndexOperation
	 * 
	 * @param repositoryId repository the cmis object belongs
	 * @param objectId cmis object being indexed
	 * @param fileName if the cmis object has a stream, its file name
	 * @param fileSize if the cmis object has a stream, its file size
	 * @param updateObjectInfo whether to update or not the cmis object index state after having cleaned the index
	 * @param operation index operation to perform
	 */
	void executeOperation(Integer repositoryId, Integer objectId, String fileName, BigInteger fileSize, boolean updateObjectInfo, IndexOperation operation);

}
