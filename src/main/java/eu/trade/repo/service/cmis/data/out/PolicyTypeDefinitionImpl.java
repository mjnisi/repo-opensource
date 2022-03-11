package eu.trade.repo.service.cmis.data.out;

import org.apache.chemistry.opencmis.commons.definitions.PolicyTypeDefinition;

import eu.trade.repo.model.ObjectType;

/**
 * {@link PolicyTypeDefinition} implementation.
 * 
 * @author porrjai
 */
public class PolicyTypeDefinitionImpl extends AbstractTypeDefinitionImpl implements PolicyTypeDefinition {

	private static final long serialVersionUID = 1L;

	/**
	 * New instance
	 * 
	 * @param objectType
	 * @param addPropertyDefinitions
	 */
	public PolicyTypeDefinitionImpl(ObjectType objectType, boolean addPropertyDefinitions) {
		super(objectType, addPropertyDefinitions);
	}
}
