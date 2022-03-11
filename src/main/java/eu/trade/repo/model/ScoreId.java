package eu.trade.repo.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


@Embeddable
public class ScoreId implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="object_id")
	private CMISObject object;

	@Column(name="word_id", insertable=false, updatable=false)
	private Integer wordId;
	
	public CMISObject getObject() {
		return object;
	}
	public void setObject(CMISObject object) {
		this.object = object;
	}

	public Integer getWordId() {
		return wordId;
	}
	public void setWordId(Integer wordId) {
		this.wordId = wordId;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return object.getId() + " - " + wordId;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (object.getId() == null ? 0 : object.getId().hashCode());
		result = prime * result + (wordId == null ? 0 : wordId.hashCode());
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
		if ( !(obj instanceof ScoreId) ) {
			return false;
		}

		ScoreId other = (ScoreId) obj;
		if (object.getId() == null) {
			return false;
		}
		if (wordId == null) {
			return false;
		}
		if (!object.getId().equals(other.object.getId())) {
			return false;
		}
		return wordId.equals(other.wordId);
	}
	
}
