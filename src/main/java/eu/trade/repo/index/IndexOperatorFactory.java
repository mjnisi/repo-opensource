package eu.trade.repo.index;

import eu.trade.repo.index.model.IndexOperation.IndexOperationType;


/**
 * Responsible for retrieving the operator that will process the cmis object to index it
 * 
 * @see IndexOperationType, IndexPartOperator
 * 
 * @author abascis
 *
 */
public interface IndexOperatorFactory {
	
	/**
	 * Obtains the appropriated operator depending on the index operation type to be performed.
	 * 
	 * @param operationType
	 * @return
	 */
	IndexPartOperator getOperator(IndexOperationType operationType);
}
