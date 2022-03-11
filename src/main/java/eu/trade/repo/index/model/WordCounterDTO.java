package eu.trade.repo.index.model;

public class WordCounterDTO {
	
	private Integer wordId;
	private Integer objectTypePropertyId;
	private int num;
	
	public WordCounterDTO(){}
	
	public WordCounterDTO(Integer wordId, Integer objectTypePropertyId) {
		super();
		this.wordId = wordId;
		this.objectTypePropertyId = objectTypePropertyId;
	}
	
	public Integer getWordId() {
		return wordId;
	}
	public void setWordId(Integer wordId) {
		this.wordId = wordId;
	}
	public Integer getObjectTypePropertyId() {
		return objectTypePropertyId;
	}
	public void setObjectTypePropertyId(Integer objectTypePropertyId) {
		this.objectTypePropertyId = objectTypePropertyId;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	
	
	
}
