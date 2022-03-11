package eu.trade.repo.model.view;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ObjectParentId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name="object_id", insertable=false, updatable=false)
	private Integer objectId;

	@Column(name="parent_id", insertable=false, updatable=false)
	private Integer parentId;

	/**
	 * @return the objectId
	 */
	public Integer getObjectId() {
		return objectId;
	}

	/**
	 * @param objectId the objectId to set
	 */
	public void setObjectId(Integer objectId) {
		this.objectId = objectId;
	}

	/**
	 * @return the parentId
	 */
	public Integer getParentId() {
		return parentId;
	}

	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}


	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return objectId + " - " + parentId;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (objectId == null ? 0 : objectId.hashCode());
		result = prime * result + (parentId == null ? 0 : parentId.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ( !(obj instanceof ObjectParentId) ) {
			return false;
		}

		ObjectParentId other = (ObjectParentId) obj;
		if (objectId == null) {
			return false;
		}
		if (parentId == null) {
			return false;
		}
		if (!objectId.equals(other.objectId)) {
			return false;
		}
		return parentId.equals(other.parentId);
	}
}
