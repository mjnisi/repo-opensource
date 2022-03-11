package eu.trade.repo.model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table (name="descendants_view")
public class Descendant {

	@EmbeddedId
	private ObjectDescendantId objectDescendantId;
	
	public ObjectDescendantId getObjectDescendantId() {
		return objectDescendantId;
	}
	public void setObjectDescendantId(ObjectDescendantId objectDesdendantId) {
		this.objectDescendantId = objectDesdendantId;
	}
	
	public Descendant() {}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (objectDescendantId == null ? 0 : objectDescendantId.hashCode());
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
		if ( !(obj instanceof Descendant) ) {
			return false;
		}

		Descendant other = (Descendant) obj;
		if (objectDescendantId == null) {
			return false;
		}
		return objectDescendantId.equals(other.objectDescendantId);
	}

}