package eu.trade.repo.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class WordPositionId implements Serializable {
	private static final long serialVersionUID = 1L;

	//MODEL
	@Column(name="position", nullable=false)
	private Integer position;

	@ManyToOne
	@JoinColumn(name="word_object_id", referencedColumnName="id")
	private WordObject wordObject;

	public WordPositionId() {
		super();
		wordObject = new WordObject();
	}

	public WordPositionId(Integer id) {
		super();
		wordObject = new WordObject();
		this.position = id;
	}


	//GETTERS/SETTERS
	public void setPosition(Integer position) {		this.position = position;	}
	public void setWordObject(WordObject wordObject) 	 {	this.wordObject = wordObject;	}

	public Integer getPosition() {	return position;	}
	public WordObject  getWordObject() 		{	return wordObject;	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return wordObject.getId() + " - " + position;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (wordObject.getId() == null ? 0 : wordObject.getId().hashCode());
		result = prime * result + (position == null ? 0 : position.hashCode());
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
		if ( !(obj instanceof WordPositionId) ) {
			return false;
		}

		WordPositionId other = (WordPositionId) obj;
		if (wordObject.getId() == null) {
			return false;
		}
		if (position == null) {
			return false;
		}
		if (!wordObject.getId().equals(other.wordObject.getId())) {
			return false;
		}
		return position.equals(other.position);
	}
}
