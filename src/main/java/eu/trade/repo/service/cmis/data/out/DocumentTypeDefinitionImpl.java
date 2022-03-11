package eu.trade.repo.service.cmis.data.out;

import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;

import eu.trade.repo.model.ObjectType;

/**
 * {@link DocumentTypeDefinition} implementation.
 * 
 * @author porrjai
 */
public class DocumentTypeDefinitionImpl extends AbstractTypeDefinitionImpl implements DocumentTypeDefinition {

	private static final long serialVersionUID = 1L;

	/**
	 * New instance
	 * 
	 * @param objectType
	 * @param addPropertyDefinitions
	 */
	public DocumentTypeDefinitionImpl(ObjectType objectType, boolean addPropertyDefinitions) {
		super(objectType, addPropertyDefinitions);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition#isVersionable()
	 */
	@Override
	public Boolean isVersionable() {
		return getObjectType().isVersionable();
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition#getContentStreamAllowed()
	 */
	@Override
	public ContentStreamAllowed getContentStreamAllowed() {
		return getObjectType().getContentStreamAllowed();
	}
}
