package eu.trade.repo.index.triggers;

import java.math.BigInteger;

public class IndexChangeData {
	private final Integer objectId;
	private StreamChangeType changeType;
	private BigInteger fileSize;

	public IndexChangeData(Integer objectId){
		this.objectId = objectId;
	}
	
	public IndexChangeData(Integer objectId, StreamChangeType changeType, BigInteger fileSize){
		this.objectId = objectId;
		this.changeType = changeType;
		this.fileSize = fileSize;
	}

	public StreamChangeType getChangeType() {
		return changeType;
	}

	public BigInteger getFileSize() {
		return fileSize;
	}

	

	public Integer getObjectId() {
		return objectId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((objectId == null) ? 0 : objectId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IndexChangeData other = (IndexChangeData) obj;
		if (objectId == null) {
			return other.objectId == null;
		} else return objectId.equals(other.objectId);
	}

	

	
}
