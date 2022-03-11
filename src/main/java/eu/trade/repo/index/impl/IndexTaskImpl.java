package eu.trade.repo.index.impl;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.index.IndexOperatorFactory;
import eu.trade.repo.index.IndexSynchronizer;
import eu.trade.repo.index.IndexTask;
import eu.trade.repo.index.model.IndexOperation;


/**
 * Stores information needed to index a part (content or metadata) of a cmis object.
 * 
 * It is also the thread representing the index task. It instantiates the index operator needed to deal with the index operation type the task defines.
 *  
 * @author abascis
 *
 */
public class IndexTaskImpl implements Runnable, IndexTask{

	private static final Logger LOG = LoggerFactory.getLogger(IndexTaskImpl.class);

	@Autowired
	private IndexOperatorFactory operatorFactory;
	@Autowired
	private IndexSynchronizer indexSynchronizer;

	private Integer objectId;
	private Integer repositoryId;
	private IndexOperation operation;
	private String fileName;
	private BigInteger fileSize;
	private boolean updateObjectIndexInfo = true;
	private String owner;


	public IndexTaskImpl(){}

	public IndexTaskImpl(Integer repositoryId, Integer objectId, IndexOperation operation, IndexSynchronizer indexSynchronizer){
		this(repositoryId, objectId, operation, null, null, indexSynchronizer);
	}

	public IndexTaskImpl(Integer repositoryId, Integer objectId, IndexOperation operation, String fileName, BigInteger fileSize, IndexSynchronizer indexSynchronizer){
		this.repositoryId = repositoryId;
		this.objectId = objectId;
		this.operation = operation;
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.indexSynchronizer = indexSynchronizer;
	}

	public IndexTaskImpl(Integer repositoryId, Integer objectId, IndexOperation operation, String fileName, BigInteger fileSize, boolean updateObjectInfo, IndexSynchronizer indexSynchronizer){
		this.repositoryId = repositoryId;
		this.objectId = objectId;
		this.operation = operation;
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.updateObjectIndexInfo = updateObjectInfo;
		this.indexSynchronizer = indexSynchronizer;
	}


	public void run() {
		doIndex();
	}


	private void doIndex(){
		try{
			switch(operation.getAction()){
			case INDEX:
				operatorFactory.getOperator(operation.getType()).createIndexPart(this);
				break;
				
			default:
				operatorFactory.getOperator(operation.getType()).deleteIndexPart(this);
			}

		}catch(Exception e){
			LOG.warn(e.getLocalizedMessage(), e);
		}finally{
			indexSynchronizer.doOnTaskFinished(this);
		}
	}
	
	
	public boolean hasToStop(){
		return indexSynchronizer.isOtherTaskWaiting(this.operation.getType(), this.objectId);
	}

	/* Getters / Setters */

	public Integer getObjectId() {
		return objectId;
	}

	public void setObjectId(Integer objectId) {
		this.objectId = objectId;
	}

	public Integer getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(Integer repositoryId) {
		this.repositoryId = repositoryId;
	}

	public IndexOperation getOperation() {
		return operation;
	}

	public void setOperation(IndexOperation operation) {
		this.operation = operation;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public BigInteger getFileSize() {
		return fileSize;
	}

	public void setFileSize(BigInteger fileSize) {
		this.fileSize = fileSize;
	}

	public boolean isUpdateObjectIndexInfo() {
		return updateObjectIndexInfo;
	}

	public void setUpdateObjectIndexInfo(boolean updateObjectIndexInfo) {
		this.updateObjectIndexInfo = updateObjectIndexInfo;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

}
