package eu.trade.repo.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


@Embeddable
public class ObjectDescendantId implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="object_id")
	private CMISObject object;
	
	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="descendant_id")
	private CMISObject descendant;
	
	public CMISObject getObject() {
		return object;
	}
	public void setObject(CMISObject object) {
		this.object = object;
	}

	public CMISObject getDescendant() {
		return descendant;
	}
	public void setDescendant(CMISObject descendant) {
		this.descendant = descendant;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return object.getId() + " - " + descendant.getId();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (object.getId() == null ? 0 : object.getId().hashCode());
		result = prime * result + (descendant.getId() == null ? 0 : descendant.getId().hashCode());
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
		if ( !(obj instanceof ObjectDescendantId) ) {
			return false;
		}

		ObjectDescendantId other = (ObjectDescendantId) obj;
		if (object.getId() == null) {
			return false;
		}
		if (descendant.getId() == null) {
			return false;
		}
		if (!object.getId().equals(other.object.getId())) {
			return false;
		}
		return descendant.getId().equals(other.descendant.getId());
	}
	
}
