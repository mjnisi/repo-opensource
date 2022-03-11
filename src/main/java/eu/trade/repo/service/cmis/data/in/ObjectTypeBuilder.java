package eu.trade.repo.service.cmis.data.in;

import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.commons.definitions.Choice;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDateTimeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDecimalDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyIntegerDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyStringDefinition;
import org.apache.chemistry.opencmis.commons.definitions.RelationshipTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;

import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.ObjectTypeRelationship;
import eu.trade.repo.model.ObjectTypeRelationship.RelationshipType;
import eu.trade.repo.model.Repository;
import eu.trade.repo.util.Constants;

/**
 * Utility class to build the ObjectType
 * 
 * @author martjoe
 *
 */
public final class ObjectTypeBuilder {

	private ObjectTypeBuilder() {
		
	}
	
	/**
	 * Builds an instance of ObjectType with the information coming form the 
	 * chemistry binding.
	 * 
	 * NOTE: The respositoryId is needed because the properties collection is invoking
	 * the hashCode() that uses the repositoryId.
	 * 
	 * @param type CMIS type
	 * @param repositoryId target repository id
	 * @return
	 */
	public static ObjectType build(TypeDefinition type, String repositoryId) {
		ObjectType objectType = new ObjectType();
		objectType.setCmisId(type.getId());
		objectType.setLocalName(type.getLocalName());
		objectType.setLocalNamespace(type.getLocalNamespace());
		objectType.setQueryName(type.getQueryName());
		objectType.setDisplayName(type.getDisplayName());
		
		//to be hydratated later on the service layer
		if(type.getBaseTypeId() != null) {
			objectType.setBase(new ObjectType(type.getBaseTypeId().value()));
		}
		
		//this supposes to be a root type: when the parent is empty or the id == parent
		//for us a root type doesn't have a parent
		if(type.getParentTypeId() == null || type.getParentTypeId().equals(objectType.getCmisId())) {
			//no-op
			objectType.setParent(null);
		} else {
			objectType.setParent(new ObjectType(type.getParentTypeId()));
		}
		
		objectType.setDescription(type.getDescription());
		objectType.setCreatable(type.isCreatable());
		objectType.setFileable(type.isFileable());
		objectType.setQueryable(type.isQueryable());
		objectType.setControllablePolicy(type.isControllablePolicy());
		objectType.setControllableAcl(type.isControllableAcl());
		objectType.setFulltextIndexed(type.isFulltextIndexed());
		objectType.setIncludedInSupertypeQuery(type.isIncludedInSupertypeQuery());
		
		if(type instanceof DocumentTypeDefinition) {
			buildDocumentType(type, objectType);
		} else if(type instanceof RelationshipTypeDefinition) {
			buildRelationshipType(type, objectType);
		}
		
		//link to the repository
		Repository repository = new Repository();
		repository.setCmisId(repositoryId);
        objectType.setRepository(repository);
		
        //properties
		buildProperties(type, objectType);
		
		return objectType;
	}

	
	/**
	 * Builds the document type attributes
	 * @param type
	 * @param objectType
	 */
	private static void buildDocumentType(TypeDefinition type, ObjectType objectType) {
		DocumentTypeDefinition docType = (DocumentTypeDefinition)type;
		objectType.setVersionable(docType.isVersionable());
		objectType.setContentStreamAllowed(docType.getContentStreamAllowed());
	}

	/**
	 * Builds the relationship type attributes 
	 * @param type
	 * @param objectType
	 */
	private static void buildRelationshipType(TypeDefinition type, ObjectType objectType) {
		RelationshipTypeDefinition relationshipType = (RelationshipTypeDefinition)type;
		for(String id:relationshipType.getAllowedSourceTypeIds()) {
			ObjectTypeRelationship otr = new ObjectTypeRelationship();
			//to be hydratated later on the service layer
			otr.setReferencedObjectType(new ObjectType(id));
			otr.setType(RelationshipType.SOURCE);
			objectType.addObjectTypeRelationship(otr);
		}
		for(String id:relationshipType.getAllowedTargetTypeIds()) {
			ObjectTypeRelationship otr = new ObjectTypeRelationship();
			//to be hydratated later on the service layer
			otr.setReferencedObjectType(new ObjectType(id));
			otr.setType(RelationshipType.TARGET);
			objectType.addObjectTypeRelationship(otr);
		}
	}

	/**
	 * Build the properties
	 * 
	 * @param type
	 * @param objectType
	 */
	private static void buildProperties(TypeDefinition type, ObjectType objectType) {
		
		for(PropertyDefinition propertyDefinition: type.getPropertyDefinitions().values()) {
			//ignore the inherited property definitions
			if(propertyDefinition.isInherited() == null || !propertyDefinition.isInherited()) {
				ObjectTypeProperty property = new ObjectTypeProperty();
				property.setCmisId(propertyDefinition.getId());
				property.setLocalName(propertyDefinition.getLocalName());
				property.setLocalNamespace(propertyDefinition.getLocalNamespace());
				property.setQueryName(propertyDefinition.getQueryName());
				property.setDisplayName(propertyDefinition.getDisplayName());
				property.setDescription(propertyDefinition.getDescription());
				property.setPropertyType(propertyDefinition.getPropertyType());
				property.setCardinality(propertyDefinition.getCardinality());
				property.setUpdatability(propertyDefinition.getUpdatability());
				property.setRequired(propertyDefinition.isRequired());
				property.setQueryable(propertyDefinition.isQueryable());
				property.setOrderable(propertyDefinition.isOrderable());
				
				buildPropertyDefault(propertyDefinition, property);
				buildPropertyChoices(propertyDefinition, property);
				buildPropertySize(propertyDefinition, property);
				buildPropertyResolution(propertyDefinition, property);
				
				objectType.addObjectTypeProperty(property);
			}
		}
	}

	/**
	 * Builds the property default
	 * @param propertyDefinition
	 * @param property
	 */
	private static void buildPropertyDefault(PropertyDefinition propertyDefinition, ObjectTypeProperty property) {
		//defaultValue
		if (propertyDefinition.getDefaultValue() != null && !propertyDefinition.getDefaultValue().isEmpty()) {
			property.setDefaultValue(propertyDefinition.getDefaultValue().get(0).toString());
			
		}
	}

	/**
	 * Buils the property choices
	 * @param propertyDefinition
	 * @param property
	 */
	private static void buildPropertyChoices(PropertyDefinition propertyDefinition, ObjectTypeProperty property) {
		//choices
		if(propertyDefinition.getChoices() != null && !propertyDefinition.getChoices().isEmpty()) {
			StringBuffer choices = new StringBuffer();
			Iterator<Choice> iterator = propertyDefinition.getChoices().iterator();
			while (iterator.hasNext()) {
				List values = iterator.next().getValue();
				if(values != null && !values.isEmpty()) {
					if(choices.length() > 0) {
						choices.append(Constants.CMIS_MULTIVALUE_SEP);
					}
					choices.append(values.get(0));
				}
			}
			if(choices.length() > 0) {
				property.setChoices(choices.toString());
			}
			
		}
		//openchoice (only if there are choices)
		if(property.getChoices() != null) {
			property.setOpenChoice(propertyDefinition.isOpenChoice());
		}
	}

	/**
	 * Builds the property resolution
	 * @param propertyDefinition
	 * @param property
	 */
	private static void buildPropertyResolution(PropertyDefinition propertyDefinition, ObjectTypeProperty property) {
		//resolution
		if(propertyDefinition instanceof PropertyDateTimeDefinition) {
			PropertyDateTimeDefinition pdtd = (PropertyDateTimeDefinition)propertyDefinition;
			if(pdtd.getDateTimeResolution() != null) {
				property.setResolution(pdtd.getDateTimeResolution());
			}
		}
	}

	/**
	 * Builds the property sizes: max length, max value and min value
	 * @param propertyDefinition
	 * @param property
	 */
	private static void buildPropertySize(PropertyDefinition propertyDefinition, ObjectTypeProperty property) {
		//maxlength
		if(propertyDefinition instanceof PropertyStringDefinition) {
			PropertyStringDefinition psd = (PropertyStringDefinition)propertyDefinition;
			if(psd.getMaxLength() != null) {
				property.setMaxLength(psd.getMaxLength().intValue());
			}
		}
		
		//maxValue
		//minValue
		if(propertyDefinition instanceof PropertyIntegerDefinition) {
			PropertyIntegerDefinition pid = (PropertyIntegerDefinition)propertyDefinition;
			if(pid.getMaxValue() != null) {
				property.setMaxValue(pid.getMaxValue().intValue());
			}
			if(pid.getMinValue() != null) {
				property.setMinValue(pid.getMinValue().intValue());
			}
		}
		if(propertyDefinition instanceof PropertyDecimalDefinition) {
			PropertyDecimalDefinition pdd = (PropertyDecimalDefinition)propertyDefinition;
			if(pdd.getMaxValue() != null) {
				property.setMaxValue(pdd.getMaxValue().intValue());
			}
			if(pdd.getMinValue() != null) {
				property.setMinValue(pdd.getMinValue().intValue());
			}
			//precision
			if(pdd.getPrecision() != null) {
				property.setPrecision(pdd.getPrecision());
			}
		}
	}

}
