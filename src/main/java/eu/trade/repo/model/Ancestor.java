package eu.trade.repo.model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table (name="ancestors_view")
public class Ancestor {

	@EmbeddedId
	private ObjectAncestorId objectAncestorId;
	
	public ObjectAncestorId getObjectAncestorId() {
		return objectAncestorId;
	}
	public void setObjectAncestorId(ObjectAncestorId objectAncestorId) {
		this.objectAncestorId = objectAncestorId;
	}
	
	public Ancestor() {}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (objectAncestorId == null ? 0 : objectAncestorId.hashCode());
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
		if ( !(obj instanceof Ancestor) ) {
			return false;
		}

		Ancestor other = (Ancestor) obj;
		if (objectAncestorId == null) {
			return false;
		}
		return objectAncestorId.equals(other.objectAncestorId);
	}

}