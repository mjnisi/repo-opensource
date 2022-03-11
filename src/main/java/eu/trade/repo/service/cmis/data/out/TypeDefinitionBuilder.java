package eu.trade.repo.service.cmis.data.out;

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;

import eu.trade.repo.model.ObjectType;

/**
 * Helper class to build a type definition based on an {@link ObjectType}.
 * 
 * @author porrjai
 */
public final class TypeDefinitionBuilder {

	private TypeDefinitionBuilder() {
	}

	public static TypeDefinition build(ObjectType objectType, boolean addPropertyDefinitions) {
		BaseTypeId baseTypeId = BaseTypeId.fromValue(objectType.getBase().getCmisId());
		switch (baseTypeId) {
			case CMIS_DOCUMENT :
				return new DocumentTypeDefinitionImpl(objectType, addPropertyDefinitions);
			case CMIS_FOLDER :
				return new FolderTypeDefinitionImpl(objectType, addPropertyDefinitions);
			case CMIS_POLICY :
				return new PolicyTypeDefinitionImpl(objectType, addPropertyDefinitions);
			case CMIS_RELATIONSHIP :
				return new RelationshipTypeDefinitionImpl(objectType, addPropertyDefinitions);
			case CMIS_ITEM:
				return new ItemTypeDefinitionImpl(objectType, addPropertyDefinitions);
			case CMIS_SECONDARY:
				return new SecondaryTypeDefinitionImpl(objectType, addPropertyDefinitions);
		}
		throw new IllegalArgumentException("Unknown base type: " + baseTypeId);
	}


}
