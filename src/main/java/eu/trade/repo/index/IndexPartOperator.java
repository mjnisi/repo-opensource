package eu.trade.repo.index;

import eu.trade.repo.index.model.IndexOperation.IndexOperationType;


/**
 * Responsible for processing a cmis object part (content or metadata). The index part supported is defined by getOperationTypeSupported method.
 * 
 * @author abascis
 *
 */
public interface IndexPartOperator{


	/**
	 * Indicates which IndexOperationType is supported by this operator.
	 * @return
	 */
	IndexOperationType getOperationTypeSupported();

	/**
	 * Method to drop an index part for a cmis object.
	 * 
	 * @param indexTask containing all of the necessary data to perform the action
	 */
	void deleteIndexPart(IndexTask indexTask);

	/**
	 * Method to index or re-index a part of a cmis object.
	 * 
	 * @param indexTask containing all of the necessary data to perform the action
	 */

	void createIndexPart(IndexTask indexTask);



}
