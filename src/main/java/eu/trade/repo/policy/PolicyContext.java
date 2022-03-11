package eu.trade.repo.policy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;

import eu.trade.repo.model.CMISObject;

public class PolicyContext implements CmisExtensionElement {
	
	private static final long serialVersionUID = -2061376353108705583L;

	private CMISObject policyObject;
	private Set<CMISObject> appliedObjects;
	
	public enum PolicyState {BEFORE, AFTER}

    private PolicyState policyState;
	private Object returnValue;
	
	
	public PolicyContext() {
		super();
	}
	
	public CMISObject getPolicyObject() {
		return policyObject;
	}
	public void setPolicyObject(CMISObject policyObject) {
		this.policyObject = policyObject;
	}
	public Set<CMISObject> getAppliedObjects() {
		return appliedObjects;
	}
	public void setAppliedObjects(Set<CMISObject> appliedObjects) {
		this.appliedObjects = appliedObjects;
	}
	public PolicyState getPolicyState() {
		return policyState;
	}
	public void setPolicyState(PolicyState policyState) {
		this.policyState = policyState;
	}
	public Object getReturnValue() {
		return returnValue;
	}
	public void setReturnValue(Object returnValue) {
		this.returnValue = returnValue;
	}


	/**
	 * ExtensionData interface
	 */
	@Override
	public String getName() {
		return null;
	}

	/**
	 * ExtensionData interface
	 */
	@Override
	public String getNamespace() {
		return null;
	}

	/**
	 * ExtensionData interface
	 */
	@Override
	public String getValue() {
		return null;
	}

	/**
	 * ExtensionData interface
	 */
	@Override
	public Map<String, String> getAttributes() {
		return null;
	}

	/**
	 * ExtensionData interface
	 */
	@Override
	public List<CmisExtensionElement> getChildren() {
		return null;
	}
	
	
	
}
