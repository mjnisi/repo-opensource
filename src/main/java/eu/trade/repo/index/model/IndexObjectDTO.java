package eu.trade.repo.index.model;

import java.math.BigInteger;

public class IndexObjectDTO {

	private Integer objectId;
	private Integer repositoryId;
	private String fileName;
	private BigInteger fileSize;
	
	public IndexObjectDTO(Integer repositoryId, Integer objectId){
		this.objectId = objectId;
		this.repositoryId = repositoryId;
	}

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
	
}
