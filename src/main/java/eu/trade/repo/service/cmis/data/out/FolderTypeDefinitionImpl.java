package eu.trade.repo.service.cmis.data.out;

import org.apache.chemistry.opencmis.commons.definitions.FolderTypeDefinition;

import eu.trade.repo.model.ObjectType;

/**
 * {@link FolderTypeDefinition} implementation.
 * 
 * @author porrjai
 */
public class FolderTypeDefinitionImpl extends AbstractTypeDefinitionImpl implements FolderTypeDefinition {

	private static final long serialVersionUID = 1L;

	/**
	 * New instance
	 * 
	 * @param objectType
	 * @param addPropertyDefinitions
	 */
	public FolderTypeDefinitionImpl(ObjectType objectType, boolean addPropertyDefinitions) {
		super(objectType, addPropertyDefinitions);
	}
}
