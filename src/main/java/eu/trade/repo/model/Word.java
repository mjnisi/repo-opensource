package eu.trade.repo.model;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;


@Entity
@NamedQueries({
	@NamedQuery(name="Words.byWordAndRepo", query="select word from Word word where word.repository.id = :repositoryId and word.word in (:words)")	
})
@Table(name="index_word")
public class Word implements DBEntity {
	private static final long serialVersionUID = 1L;

	//MODEL
	@Id
	@GeneratedValue(generator="sq_index_word")
	@SequenceGenerator(sequenceName= "sq_index_word", name="sq_index_word")
	private Integer id;

	@Column(name="word")
	@Size(max=100)
	private String word;

	@ManyToOne
	@JoinColumn(name="repository_id")
	private Repository repository;

	@OneToMany(mappedBy="word", cascade=CascadeType.REMOVE, fetch=FetchType.LAZY)
	private Set<WordObject> wordObjects = new LinkedHashSet<WordObject>();


	public Word() {
		super();
		repository = new Repository();
	}
	public Word(Integer repositoryId, String word) {
		super();
		repository = new Repository();
		repository.setId(repositoryId);
		this.word = word;
	}

	//GETTERS/SETTERS
	@Override
	public void setId(Integer id) {		this.id = id;	}
	public void setWord(String word) 	{	this.word = word;	}
	public void setRepository(Repository repository){	this.repository = repository;	}
	public void setWordObjects(Set<WordObject> wordObjects) {	this.wordObjects = wordObjects;	}

	@Override
	public Integer getId() {	return id;	}
	public String  getWord() 	{	return word;	}
	public Repository getRepository() 	{	return repository; }
	public Set<WordObject> getWordObjects() 		{	return wordObjects;	}

	//ADDS/REMOVES

	public WordObject addWordObject(WordObject wordObject) {
		wordObject.setWord(this);
		getWordObjects().add(wordObject);
		return wordObject;
	}
	public WordObject removeWordObject(WordObject wordObject) {
		getWordObjects().remove(wordObject);
		wordObject.setWord(null);
		return wordObject;
	}

}
