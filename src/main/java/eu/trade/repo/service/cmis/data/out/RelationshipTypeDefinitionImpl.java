package eu.trade.repo.service.cmis.data.out;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.definitions.RelationshipTypeDefinition;

import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeRelationship;
import eu.trade.repo.model.ObjectTypeRelationship.RelationshipType;

/**
 * {@link RelationshipTypeDefinition} implementation.
 * 
 * @author porrjai
 */
public class RelationshipTypeDefinitionImpl extends AbstractTypeDefinitionImpl implements RelationshipTypeDefinition {

	private static final long serialVersionUID = 1L;

	private final List<String> allowedSourceTypeIds = new ArrayList<>();
	private final List<String> allowedTargetTypeIds = new ArrayList<>();

	/**
	 * New instance
	 * 
	 * @param objectType
	 * @param addPropertyDefinitions
	 */
	public RelationshipTypeDefinitionImpl(ObjectType objectType, boolean addPropertyDefinitions) {
		super(objectType, addPropertyDefinitions);
		for (ObjectTypeRelationship objectTypeRelationship : objectType.getObjectTypeRelationships()) {
			if (objectTypeRelationship.getType().equals(RelationshipType.SOURCE)) {
				allowedSourceTypeIds.add(objectTypeRelationship.getReferencedObjectType().getCmisId());
			}
			else {
				allowedTargetTypeIds.add(objectTypeRelationship.getReferencedObjectType().getCmisId());
			}
		}
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.definitions.RelationshipTypeDefinition#getAllowedSourceTypeIds()
	 */
	@Override
	public List<String> getAllowedSourceTypeIds() {
		return allowedSourceTypeIds;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.definitions.RelationshipTypeDefinition#getAllowedTargetTypeIds()
	 */
	@Override
	public List<String> getAllowedTargetTypeIds() {
		return allowedTargetTypeIds;
	}
}
