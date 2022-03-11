package eu.trade.repo.index.model;




public class WordObjectDTO implements IWordObjectExtractor{

	private Integer id;
	private Integer wordId;
	private Integer objectId;
	private Integer propertyId;
	private Integer objPropTypeId;
	private Integer frequency;

	public WordObjectDTO(){}

	public WordObjectDTO(Integer id, Integer wordId, Integer objectId, Integer frequency) {
		super();
		this.id = id;
		this.wordId = wordId;
		this.objectId = objectId;
		this.frequency = frequency;
	}

	public WordObjectDTO(Integer id, Integer wordId, Integer objectId, Integer propertyId, Integer objPropTypeId, Integer frequency) {
		super();
		this.id = id;
		this.wordId = wordId;
		this.objectId = objectId;
		this.propertyId = propertyId;
		this.objPropTypeId = objPropTypeId;
		this.frequency = frequency;
	}

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getWordId() {
		return wordId;
	}
	public void setWordId(Integer wordId) {
		this.wordId = wordId;
	}
	public Integer getObjectId() {
		return objectId;
	}
	public void setObjectId(Integer objectId) {
		this.objectId = objectId;
	}
	public Integer getPropertyId() {
		return propertyId;
	}
	public void setPropertyId(Integer propertyId) {
		this.propertyId = propertyId;
	}
	public Integer getObjPropTypeId() {
		return objPropTypeId;
	}
	public void setObjPropTypeId(Integer objPropTypeId) {
		this.objPropTypeId = objPropTypeId;
	}
	public Integer getFrequency() {
		return frequency;
	}
	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}

	@Override
	public Integer getIdFromTransientItem(TransientDTO item) {
		return id;
	}
}
