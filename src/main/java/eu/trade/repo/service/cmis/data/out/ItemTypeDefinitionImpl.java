package eu.trade.repo.service.cmis.data.out;

import org.apache.chemistry.opencmis.commons.definitions.ItemTypeDefinition;

import eu.trade.repo.model.ObjectType;

public class ItemTypeDefinitionImpl extends AbstractTypeDefinitionImpl implements ItemTypeDefinition {

	private static final long serialVersionUID = 1L;

	/**
	 * New instance
	 * 
	 * @param objectType
	 * @param addPropertyDefinitions
	 */
	public ItemTypeDefinitionImpl(ObjectType objectType, boolean addPropertyDefinitions) {
		super(objectType, addPropertyDefinitions);
	}
}