package eu.trade.repo.web.admin.bean;


public class IndexStatusSummary {
	private int stateId;
	private String stateName;
	private String stateDescription;
	private Long numObjects;
	
	public IndexStatusSummary(int indexStateId, String indexStateName, String indexStateDescription, Long numObjects) {
		super();
		this.stateId = indexStateId;
		this.stateName = indexStateName;
		this.stateDescription = indexStateDescription;
		this.numObjects = numObjects;
	}

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public String getStateDescription() {
		return stateDescription;
	}

	public void setStateDescription(String stateDescription) {
		this.stateDescription = stateDescription;
	}

	public Long getNumObjects() {
		return numObjects;
	}

	public void setNumObjects(Long numObjects) {
		this.numObjects = numObjects;
	}

	public int getStateId() {
		return stateId;
	}

	public void setStateId(int stateId) {
		this.stateId = stateId;
	}
	
}
