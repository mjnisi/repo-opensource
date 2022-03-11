package eu.trade.repo.index.model;

/**
 * Index operations available. Each index operation is classified using an operation type and an operation action.
 * 
 * @see IndexOperationType, IndexOperationAction
 * 
 */
public enum IndexOperation {

	/**
	 * Index cmis object metadata
	 */
	METADATA_INDEX (1, IndexOperationType.METADATA, IndexOperationAction.INDEX),
	/**
	 * Delete cmis object metadata index
	 */
	METADATA_INDEX_DELETE (2, IndexOperationType.METADATA, IndexOperationAction.DELETE),
	/**
	 * Index cmis object document content
	 */
	CONTENT_INDEX (3, IndexOperationType.CONTENT, IndexOperationAction.INDEX),
	/**
	 * Delete cmis object document content index
	 */
	CONTENT_INDEX_DELETE (4, IndexOperationType.CONTENT, IndexOperationAction.DELETE);


	private final int code;   
	private final IndexOperationType type;
	private final IndexOperationAction action;

	IndexOperation(int code, IndexOperationType type, IndexOperationAction action) {
		this.code = code;
		this.type = type;
		this.action = action;
	}

	public int getCode() {
		return code;
	}

	public IndexOperationType getType() {
		return type;
	}

	public IndexOperationAction getAction(){
		return action;
	}


	/**
	 * Type of index operation based on which part of the cmis object is to be indexed.
	 */
	public enum IndexOperationType{
		METADATA, CONTENT
    }

	/**
	 * Actions that can be performed with a cmis object index part.
	 */
	public enum IndexOperationAction{
		INDEX, DELETE
    }
}
