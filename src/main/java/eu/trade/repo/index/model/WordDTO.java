package eu.trade.repo.index.model;

public class WordDTO {

	private Integer id;
	private Integer repositoryId;
	private String word;
	
	public WordDTO(){}
	
	public WordDTO(Integer id, Integer repositoryId, String word) {
		super();
		this.id = id;
		this.repositoryId = repositoryId;
		this.word = word;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getRepositoryId() {
		return repositoryId;
	}
	public void setRepositoryId(Integer repositoryId) {
		this.repositoryId = repositoryId;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	
	
}
