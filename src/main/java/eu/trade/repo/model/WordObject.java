package eu.trade.repo.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;



@Entity
@Table(name="index_word_object")
public class WordObject implements DBEntity {
	private static final long serialVersionUID = 1L;

	//MODEL
	@Id
	@GeneratedValue(generator="sq_index_word_object")
	@SequenceGenerator(sequenceName= "sq_index_word_object", name="sq_index_word_object")
	private Integer id;


	@Column(name="frequency")
	private Integer frequency;

	@OneToMany(mappedBy="id.wordObject", cascade=CascadeType.REMOVE, orphanRemoval=true, fetch=FetchType.LAZY)
	private List<WordPosition> positions;


	@ManyToOne
	@JoinColumn(name="word_id", referencedColumnName="id")
	private Word word;

	@ManyToOne
	@JoinColumn(name="object_id", referencedColumnName="id")
	private CMISObject object;


	public WordObject() {
		super();
		frequency = Integer.valueOf(0);
	}


	//GETTERS/SETTERS
	@Override
	public Integer getId() 		{	return this.id;	}
	public void setFrequency(Integer frequency) 	{	this.frequency = frequency;	}
	public void setPositions(List<WordPosition> positions) 	{	this.positions = positions;	}
	public void setWord(Word dictionary){	this.word = dictionary;	}
	public void setObject(CMISObject document){	this.object = document;	}

	@Override
	public void setId(Integer id) {	this.id = id;	}
	public Integer  getFrequency() 	{	return frequency;	}
	public List<WordPosition> getPositions(){	 return positions;	}
	public Word getWord() 	{	return word;	}
	public CMISObject getObject() 	{	return object;	}

	//ADDS/REMOVES
	public WordPosition addPosition(WordPosition position) {
		getPositions().add(position);
		position.getId().setWordObject(this);

		return position;
	}

	public WordPosition removePosition(WordPosition position) {
		getPositions().remove(position);
		position.getId().setWordObject(null);

		return position;
	}


}
