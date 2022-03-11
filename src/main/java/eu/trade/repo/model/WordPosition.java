package eu.trade.repo.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;



@Entity
@Table(name="index_word_position")
public class WordPosition implements Serializable {
	private static final long serialVersionUID = 1L;

	//MODEL
	@EmbeddedId
	private WordPositionId id;

	@Column
	private Integer step;

	public WordPosition() {
		super();
		this.id = new WordPositionId();
	}

	public WordPosition(Integer id) {
		super();
		this.id = new WordPositionId(id);
	}

	//GETTERS/SETTERS
	public void setStep(Integer step) 	{	this.step = step;	}
	public void setId(WordPositionId id) 	{	this.id = id;	}

	public Integer  getStep() 	{	return step;	}
	public WordPositionId  getId() 	{	return id;	}

}
