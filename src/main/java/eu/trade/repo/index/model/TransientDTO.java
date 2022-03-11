package eu.trade.repo.index.model;

public class TransientDTO {

	private String word;
	private int position;
	private int frequency;
	private int step;
	private Integer documentId;
	private Integer propertyId;
	private Integer objPropTypeId;
	
	
	public TransientDTO(String word, int position, int frequency, int step, Integer objectId, Integer propertyId, Integer objPropTypeId) {
		super();
		this.word = word;
		this.position = position;
		this.frequency = frequency;
		this.step = step;
		this.documentId = objectId;
		this.propertyId = propertyId;
		this.objPropTypeId = objPropTypeId;
	}
	
	public TransientDTO(String word, int position, int frequency, int step, Integer objectId) {
		this(word, position, frequency, step, objectId, null, null);
	}
	
	public TransientDTO(String word, int position, int frequency, int step) {
		this(word, position, frequency, step, null, null, null);
	}
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public int getFrequency() {
		return frequency;
	}
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	public int getStep() {
		return step;
	}
	public void setStep(int step) {
		this.step = step;
	}

	public Integer getDocumentId() {
		return documentId;
	}

	public void setDocumentId(Integer documentId) {
		this.documentId = documentId;
	}
	
	/**
	 * @return null if propertyId is 0 or null; the propertyId value otherwise.
	 */
	public Integer getPropertyId() {
		return (Integer.valueOf(0).equals(propertyId))? null : propertyId;
	}

	public void setPropertyId(Integer propertyId) {
		this.propertyId = propertyId;
	}
	
	/**
	 * @return null if propertyId is 0 or null; the propertyId value otherwise.
	 */
	public Integer getObjPropTypeId() {
		return (Integer.valueOf(0).equals(objPropTypeId))? null : objPropTypeId;
	}

	public void setObjPropTypeId(Integer objPropTypeId) {
		this.objPropTypeId = objPropTypeId;
	}
}
