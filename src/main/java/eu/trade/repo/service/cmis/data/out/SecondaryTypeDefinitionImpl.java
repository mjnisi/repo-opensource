package eu.trade.repo.service.cmis.data.out;

import org.apache.chemistry.opencmis.commons.definitions.SecondaryTypeDefinition;

import eu.trade.repo.model.ObjectType;

public class SecondaryTypeDefinitionImpl extends AbstractTypeDefinitionImpl implements SecondaryTypeDefinition {

	private static final long serialVersionUID = 1L;

	/**
	 * New instance
	 * 
	 * @param objectType
	 * @param addPropertyDefinitions
	 */
	public SecondaryTypeDefinitionImpl(ObjectType objectType, boolean addPropertyDefinitions) {
		super(objectType, addPropertyDefinitions);
	}
}