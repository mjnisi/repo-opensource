package eu.trade.repo.index;

import java.math.BigInteger;

import eu.trade.repo.index.impl.IndexOperationTypeSynchronizer;
import eu.trade.repo.index.model.IndexOperation;


/**
 * Stores information needed to index a part (content or metadata) of a cmis object.
 * 
 * Attention!! Implementations of this interface should not override the default implementation of hashcode() method (different values for different instances of the class)
 * The hashcode value is used by the IndexSynchronizer while deciding whether an IndexTask instance is to be put in the executingMap or in the waitingMap.
 * 
 * @see IndexOperationTypeSynchronizer
 *  
 * @author abascis
 *
 */
public interface IndexTask extends Runnable{

	/**
	 * Internal ID of the cmis object to be indexed
	 * @return
	 */
	Integer getObjectId();

	void setObjectId(Integer objectId);

	/**
	 * Internal ID of the repository the cmis object belongs to
	 * @return
	 */
	Integer getRepositoryId();

	void setRepositoryId(Integer repositoryId);

	/**
	 * Name of the file (stream) related to the cmis object to be indexed, if necessary
	 * @return
	 */
	String getFileName();

	void setFileName(String fileName);

	/**
	 * Size of the file (stream) related to the cmis object to be indexed, if necessary
	 * @return
	 */
	BigInteger getFileSize();

	void setFileSize(BigInteger fileSize);

	/**
	 * Index operation to perform
	 * @return
	 */
	IndexOperation getOperation();

	void setOperation(IndexOperation operation);

	/**
	 * Who triggers the index task. It could be ATOMIC_INDEX or the execution of one of the background jobs.
	 * @return
	 */
	String getOwner();

	void setOwner(String owner);

	/**
	 * Used for synchronization between tasks.
	 * @return true if there is other task waiting for the same cmis object.
	 */
	boolean hasToStop();

	/**
	 * Whether the state of the object being indexed has to be updated after the cleaning step of the indexing process is reached
	 * steps: [ clean: NONE, createIndex: INDEXED | PARTIALLY_INDEXED | NOT_INDEXABLE | ERROR ] 
	 * @return
	 */
	boolean isUpdateObjectIndexInfo();

	void setUpdateObjectIndexInfo(boolean updateObjectIndexInfo);
}
