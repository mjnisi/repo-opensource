package eu.trade.repo.service.cmis.data.out;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeMutability;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeMutabilityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trade.repo.model.ObjectType;

public abstract class AbstractTypeDefinitionImpl extends NonExtensionsObject implements TypeDefinition {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractTypeDefinitionImpl.class);

	private static final long serialVersionUID = 1L;

	private final ObjectType objectType;
	private Map<String, PropertyDefinition<?>> propertyDefinitions;

	/**
	 * New instance.
	 * 
	 * @param objectType
	 * @param addPropertyDefinitions
	 */
	public AbstractTypeDefinitionImpl(ObjectType objectType, boolean addPropertyDefinitions) {
		this.objectType = objectType;
		if (addPropertyDefinitions) {
			propertyDefinitions = PropertyDefinitionsBuilder.buildPropertyDefinitions(objectType);
		}
	}

	/**
	 * @return the objectType
	 */
	protected ObjectType getObjectType() {
		return objectType;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.definitions.TypeDefinition#getId()
	 */
	@Override
	public String getId() {
		return objectType.getCmisId();
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.definitions.TypeDefinition#getLocalName()
	 */
	@Override
	public String getLocalName() {
		return objectType.getLocalName();
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.definitions.TypeDefinition#getLocalNamespace()
	 */
	@Override
	public String getLocalNamespace() {
		return objectType.getLocalNamespace();
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.definitions.TypeDefinition#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return objectType.getDisplayName();
	}

	@Override
	public String getQueryName() {
		return objectType.getQueryName();
	}

	@Override
	public String getDescription() {
		return objectType.getDescription();
	}

	@Override
	public BaseTypeId getBaseTypeId() {
		return BaseTypeId.fromValue(objectType.getBase().getCmisId());
	}

	@Override
	public String getParentTypeId() {
		ObjectType parent = objectType.getParent();
		if (parent == null) {
			return null;
		}
		return parent.getCmisId();
	}

	@Override
	public Boolean isCreatable() {
		return objectType.isCreatable();
	}

	@Override
	public Boolean isFileable() {
		return objectType.isFileable();
	}

	@Override
	public Boolean isQueryable() {
		return objectType.isQueryable();
	}

	@Override
	public Boolean isFulltextIndexed() {
		return objectType.isFulltextIndexed();
	}

	@Override
	public Boolean isIncludedInSupertypeQuery() {
		return objectType.isIncludedInSupertypeQuery();
	}

	@Override
	public Boolean isControllablePolicy() {
		return objectType.isControllablePolicy();
	}

	@Override
	public Boolean isControllableAcl() {
		return objectType.isControllableAcl();
	}

	@Override
	public Map<String, PropertyDefinition<?>> getPropertyDefinitions() {
		return propertyDefinitions;
	}

	@Override
	public TypeMutability getTypeMutability() {
		//TODO change to send to the client the proper values
		TypeMutabilityImpl typeMutability = new TypeMutabilityImpl();
		typeMutability.setCanCreate(true);
		typeMutability.setCanDelete(true);
		typeMutability.setCanUpdate(true);
		return typeMutability;
	}
}
