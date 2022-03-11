package eu.trade.repo.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


@Embeddable
public class ObjectAncestorId implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="object_id")
	private CMISObject object;
	
	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="ancestor_id")
	private CMISObject ancestor;
	
	public CMISObject getObject() {
		return object;
	}
	public void setObject(CMISObject object) {
		this.object = object;
	}

	public CMISObject getAncestor() {
		return ancestor;
	}
	public void setAncestor(CMISObject ancestor) {
		this.ancestor = ancestor;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return object.getId() + " - " + ancestor.getId();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (object.getId() == null ? 0 : object.getId().hashCode());
		result = prime * result + (ancestor.getId() == null ? 0 : ancestor.getId().hashCode());
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
		if ( !(obj instanceof ObjectAncestorId) ) {
			return false;
		}

		ObjectAncestorId other = (ObjectAncestorId) obj;
		if (object.getId() == null) {
			return false;
		}
		if (ancestor.getId() == null) {
			return false;
		}
		if (!object.getId().equals(other.object.getId())) {
			return false;
		}
		return ancestor.getId().equals(other.ancestor.getId());
	}
	
}
