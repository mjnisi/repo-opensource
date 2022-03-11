package eu.trade.repo.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.persistence.NoResultException;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.enums.ChangeType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.trade.repo.id.IDGenerator;
import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ChangeEvent;
import eu.trade.repo.model.HandlerType;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.ObjectTypeRelationship;
import eu.trade.repo.model.Permission;
import eu.trade.repo.model.PermissionMapping;
import eu.trade.repo.model.Property;
import eu.trade.repo.model.Repository;
import eu.trade.repo.model.SecurityHandler;
import eu.trade.repo.model.SecurityType;
import eu.trade.repo.policy.AbstractBasePolicy;
import eu.trade.repo.security.ActionMap;
import eu.trade.repo.security.Security;
import eu.trade.repo.selectors.AclSelector;
import eu.trade.repo.selectors.CMISObjectSelector;
import eu.trade.repo.selectors.ChangeEventSelector;
import eu.trade.repo.selectors.ObjectTypePropertySelector;
import eu.trade.repo.selectors.ObjectTypeSelector;
import eu.trade.repo.selectors.PermissionMappingSelector;
import eu.trade.repo.selectors.PermissionSelector;
import eu.trade.repo.selectors.RepositorySelector;
import eu.trade.repo.service.interfaces.IRepositoryService;
import eu.trade.repo.service.util.PermissionsUtil;
import eu.trade.repo.util.Cleanable;
import eu.trade.repo.util.Constants;
import eu.trade.repo.util.ICurrentDate;

public class RepositoryService extends CMISBaseService implements IRepositoryService, Cleanable {
	private static final Logger LOG = LoggerFactory.getLogger(RepositoryService.class);
	
	@Autowired
	private RepositorySelector repoSelector;
	@Autowired
	private ObjectTypeSelector objectTypeSelector;
	@Autowired
	private ObjectTypePropertySelector objectTypePropertySelector;
	@Autowired
	private CMISObjectSelector cmisObjectSelector;
	@Autowired
	private ChangeEventSelector changeLogSelector;
	@Autowired
	private AclSelector aclSelector;

	@Autowired
	private IDGenerator generator;
	@Autowired
	private ICurrentDate currentDate;
	@Autowired
	private PermissionSelector permissionSelector;
	@Autowired
	private PermissionMappingSelector permissionMappingSelector;

	@Autowired
	private Security security;
	
	private Map<String, AbstractBasePolicy> policyImpls;
	public void setPolicyImpls(Map<String, AbstractBasePolicy> policyImpls) {
		this.policyImpls = policyImpls;
	}

	private final Map<String, String> rootFolderIdCache = new HashMap<>();

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#getObjectTypeChildren(java.lang.String,
	 *      java.lang.String, boolean)
	 */
	@Override
	public Set<ObjectType> getObjectTypeChildren(String repositoryId,
			String cmisId, boolean includeLazyLoadedDependencies) {
		return objectTypeSelector.getObjectTypeChildren(repositoryId, cmisId,
				includeLazyLoadedDependencies);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#getObjectTypeChildren(java.lang.String,
	 *      java.lang.String, boolean, int, int)
	 */
	@Override
	public Set<ObjectType> getObjectTypeChildren(String repositoryId,
			String cmisId, boolean includeLazyLoadedDependencies, int maxItems,
			int skipCount) {
		return objectTypeSelector.getObjectTypeChildren(repositoryId, cmisId,
				includeLazyLoadedDependencies, skipCount, maxItems);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#countObjectTypeChildren(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public Long countObjectTypeChildren(String repositoryId, String cmisId) {
		return objectTypeSelector.countObjectTypeChildren(repositoryId, cmisId);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#getObjectType(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public ObjectType getObjectType(String repositoryId, String cmisId) {
		
		if(objectTypeSelector.countObjectTypeByCmisId(repositoryId, cmisId) > 0) {
			return objectTypeSelector.getObjectTypeByCmisIdWithProperties(
					repositoryId, cmisId);
		} else {
			throw new CmisObjectNotFoundException("Type " + cmisId + " doesn't exist.");
		}
		
	}
	
	
	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#getBaseObjectTypes(java.lang.String)
	 */
	@Override
	public List<BaseTypeId> getBaseObjectTypes(String repositoryId) {
		List<BaseTypeId> baseTypes = new ArrayList<BaseTypeId>();
		for(ObjectType objectType : objectTypeSelector.getBaseObjectTypes(repositoryId)) {
			baseTypes.add(BaseTypeId.fromValue(objectType.getCmisId()));
		}
		return baseTypes;
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#createType(java.lang.String, eu.trade.repo.model.ObjectType)
	 */
	@Override
	public ObjectType createType(final ObjectType objectType) {
		
		ObjectType ot = objectType;
		ot = validateObjectTypeCommon(ot);
		ot = validateObjectTypeCreate(ot);
		persist(ot);
		
		return ot;
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#updateType(java.lang.String, eu.trade.repo.model.ObjectType)
	 */
	@Override
	public ObjectType updateType(final ObjectType objectType) {
		
		ObjectType ot = objectType;
		ot = validateObjectTypeCommon(ot);
		ot = validateObjectTypeUpdate(ot);
		merge(ot);
		
		return ot;
	}
	
	/**
	 * Common validations 
	 * 
	 * @param objectType
	 */
	private ObjectType validateObjectTypeCommon(ObjectType objectType) {

		//properties minimum input
		Set<String> queryNames = new HashSet<String>(); 
		for(ObjectTypeProperty otp: objectType.getObjectTypeProperties()) {
			if(isMissingDefinition(otp)) {
					throw new CmisInvalidArgumentException(
						String.format("Not allowed to create or update this type, missing definition data on property %s.", otp.getCmisId()) );
			}
			
			//check the query name has no spaces
			if(!otp.getQueryName().matches("[\\w_:]+")) {
				throw new CmisConstraintException(String.format(
					"Not allowed to create or update this type, query name '%s' is using invalid characters, must use only letters, underscore and colon.", otp.getQueryName()));
			}

			//checking the query name is not twice
			if(!queryNames.add(otp.getQueryName())) {
				throw new CmisConstraintException(String.format(
					"Not allowed to create or update this type, query name %s is duplicated.", otp.getQueryName()));
			}
			
		}
		
		return objectType;
	}

	/**
	 * Check the minimal data needed in the property
	 * @param otp
	 * @return
	 */
	private boolean isMissingDefinition(ObjectTypeProperty otp) {
		String[] requiredAttributesMethodNames = new String[] {
				"getCmisId",
				"getOrderable",
				"getQueryable",
				"getRequired", 
				"getUpdatability", 
				"getCardinality", 
				"getPropertyType", 
				"getQueryName"};
		
		return hasRequiredAttributesMissing(otp, requiredAttributesMethodNames);
	}
	
	/**
	 * Check the minimal data needed by the object type
	 * @param objectType
	 * @return
	 */
	private boolean isMissingDefinition(ObjectType objectType) {
		
		boolean baseMissing = objectType.getBase() == null ||
				objectType.getBase().getCmisId() == null ||
				objectType.getBase().getCmisId().length() == 0;
		
		boolean parentMissing = objectType.getParent() == null ||
				objectType.getParent().getCmisId() == null ||
				objectType.getParent().getCmisId().length() == 0;
		
		String[] requiredAttributesMethodNames = new String[] {
				"getCmisId",
				"getQueryName",
				"isIncludedInSupertypeQuery",
				"isFulltextIndexed", 
				"isControllableAcl", 
				"isControllablePolicy", 
				"isCreatable", 
				"isFileable", 
				"isQueryable"};
		
		boolean capabilityMissing = hasRequiredAttributesMissing(objectType, requiredAttributesMethodNames);

		return  baseMissing || parentMissing || capabilityMissing;
	}

	/**
	 * Check the required attributes executing the getter methods requested.
	 * If any of the methods' value is null or is an empty String returns false.
	 * 
	 * IMPORTANT: If the method name is incorrect or is a private method will 
	 * return false. 
	 *  
	 * @param object
	 * @param requiredAttributesMethodNames
	 * @return
	 */
	private boolean hasRequiredAttributesMissing(Object object, String[] requiredAttributesMethodNames) {
		boolean capabilityMissing = false;
		for(String methodName: requiredAttributesMethodNames) {
			try {
				Method method = object.getClass().getMethod(methodName);
				Object value = method.invoke(object);
				if(value == null) {
					capabilityMissing = true;
				}
				if(value instanceof String && ((String)value).length() == 0) {
					capabilityMissing = true;
				}
			} catch (ReflectiveOperationException e) {
				capabilityMissing = true;
			}
		}
		return capabilityMissing;
	} 
	
	/**
	 * Creation validation
	 *  
	 * @param objectType
	 */
	private ObjectType validateObjectTypeCreate(ObjectType objectType) {
		
		String repositoryId = objectType.getRepository().getCmisId();
		
		//validate minimal input data
		if(isMissingDefinition(objectType)) {
				throw new CmisInvalidArgumentException("Not allowed to create this type, missing definition data.");
		}
		
		//the id is not used in this repository
		if (objectTypeSelector.countObjectTypeByCmisId(repositoryId, objectType.getCmisId()) == 1) {
			throw new CmisConstraintException("Not allowed to create this type, Id already in use.");
		}
		
		//the query name is not used in this repository
		if (objectTypeSelector.countObjectTypeByQueryName(repositoryId, objectType.getQueryName()) == 1) {
			throw new CmisConstraintException("Not allowed to create this type, query name already in use.");
		}

		//reconnect repository
		try {
			objectType.setRepository(repoSelector.getRepository(repositoryId));
		} catch (NoResultException e) {
			throw new CmisInvalidArgumentException("Not allowed to create this type, invalid repository.", e);
		}
		
		//reconnect base
		try {
			objectType.setBase(objectTypeSelector.getObjectTypeByCmisId(repositoryId, objectType.getBase().getCmisId()));
		} catch (NoResultException e) {
			throw new CmisInvalidArgumentException("Not allowed to create this type, base type doesn't exist.", e);
		}
		
		//reconnect and parent type
		try {
			objectType.setParent(objectTypeSelector.getObjectTypeByCmisId(repositoryId, objectType.getParent().getCmisId()));
		} catch (NoResultException e) {
			throw new CmisInvalidArgumentException("Not allowed to create this type, parent type doesn't exist.", e);
		}
		
		//check base and parent type are compatible
		if(!objectType.getParent().getBase().equals(objectType.getBase())) {
			throw new CmisInvalidArgumentException("Not allowed to create this type, parent and base type are not compatible.");
		}
		
		//the id and the query name of the properties are not used in the hierarchy
		validateObjectTypePropertyHierarchy(objectType);
		
		//check if the cmis:secondary properties are unique in whole repository
		validateObjectTypePropertyUnique(objectType);

		//reconnect relationships types
		try {
			for(ObjectTypeRelationship otr: objectType.getObjectTypeRelationships()) {
				otr.setReferencedObjectType(objectTypeSelector.getObjectTypeByCmisId(repositoryId, otr.getReferencedObjectType().getCmisId()));
			}
		} catch (NoResultException e) {
			throw new CmisConstraintException("Not allowed to create this type, relationship reference type doesn't exist.", e);
		}
		
		//the cmis:relationship subtypes MUST NOT be file-able
		if(BaseTypeId.CMIS_RELATIONSHIP.value().equals(objectType.getBase().getCmisId()) 
				&& objectType.isFileable()) {
			throw new CmisConstraintException("Not allowed to create this type, relationship subtypes MUST NOT be file-able.");
		}
		
		//the cmis:secondary MUST NOT be creatable, fileable, controllable policy or controllable ACL
		if(BaseTypeId.CMIS_SECONDARY.value().equals(objectType.getBase().getCmisId()) 
				&& objectType.isCreatable()) {
			throw new CmisConstraintException("Not allowed to create this type, secondary subtypes MUST NOT be creatable.");
		}
		if(BaseTypeId.CMIS_SECONDARY.value().equals(objectType.getBase().getCmisId()) 
				&& objectType.isFileable()) {
			throw new CmisConstraintException("Not allowed to create this type, secondary subtypes MUST NOT be file-able.");
		}
		if(BaseTypeId.CMIS_SECONDARY.value().equals(objectType.getBase().getCmisId()) 
				&& objectType.isControllablePolicy()) {
			throw new CmisConstraintException("Not allowed to create this type, secondary subtypes MUST NOT be controllable by policy.");
		}
		if(BaseTypeId.CMIS_SECONDARY.value().equals(objectType.getBase().getCmisId()) 
				&& objectType.isControllableAcl()) {
			throw new CmisConstraintException("Not allowed to create this type, secondary subtypes MUST NOT be controllable by ACL.");
		}
		

		return objectType;
	}


	/**
	 * Validates that the property name and id is not used in whole resository.
	 * 
	 * If the type is extending cmis:secondary the id and name must be unique
	 * because the user can assign several secondary types.
	 * 
	 * @param objectType
	 */
	private void validateObjectTypePropertyUnique(ObjectType objectType) {
		if(BaseTypeId.CMIS_SECONDARY.value().equals(objectType.getBase().getCmisId()))  {
			String repositoryId = objectType.getRepository().getCmisId();
			for(ObjectTypeProperty otp: objectType.getObjectTypeProperties()) {
				if(objectTypePropertySelector.countObjectTypePropertyById(repositoryId, objectType.getCmisId(), otp.getCmisId()) > 0) {
					throw new CmisConstraintException(String.format(
							"Not allowed to create or update this type, property Id %s already in use. Secondary types must have unique property Ids in whole repository.", otp.getCmisId()));
				} else if (objectTypePropertySelector.countObjectTypePropertyByQueryName(repositoryId, objectType.getCmisId(), otp.getQueryName()) > 0) {
					throw new CmisConstraintException(String.format(
							"Not allowed to create or update this type, query name %s already in use. Secondary types must have unique property query names in whole repository.", otp.getQueryName()));
				}
			}
		}
	}
	
	/**
	 * Validates if the query name and the id is not used in the object type hierarchy.
	 * @param objectType
	 */
	private void validateObjectTypePropertyHierarchy(ObjectType objectType) {

		//maybe the user is trying to update a base type
		if(objectType.getParent() != null) {
			//the id and the query name of the properties are not used in the hierarchy
			Map<String, SortedSet<ObjectTypeProperty>> propertiesByCmisId = 
					objectType.getParent().getObjectTypePropertiesIncludeParents(true, true);
			Map<String, SortedSet<ObjectTypeProperty>> propertiesByQueryName = 
					objectType.getParent().getObjectTypePropertiesIncludeParents(false, true);
			
			for(ObjectTypeProperty otp: objectType.getObjectTypeProperties()) {
				if (propertiesByCmisId.get(otp.getCmisId()) != null) {
					throw new CmisConstraintException(String.format(
						"Not allowed to create or update this type, property Id %s already in use.", otp.getCmisId()));
				} else if (propertiesByQueryName.get(otp.getQueryName()) != null) {
					throw new CmisConstraintException(String.format(
						"Not allowed to create or update this type, query name %s already in use.", otp.getQueryName()));
				}
			}
		}
	}
	
	/**
	 * Update validations
	 * 
	 * @param objectType
	 */
	private ObjectType validateObjectTypeUpdate(ObjectType objectType) {
		
		//validate minimal input data
		if(objectType.getCmisId() == null || objectType.getCmisId().length() == 0) {
			throw new CmisInvalidArgumentException("Not allowed to update this type, missing definition data.");
		}
		
		//first check if the objectType exists
		ObjectType dbObjectType = null;
		try {
			dbObjectType = objectTypeSelector.getObjectTypeByCmisIdWithProperties(
					objectType.getRepository().getCmisId(), objectType.getCmisId());
		} catch (NoResultException e) {
			throw new CmisInvalidArgumentException("Not allowed to update this type, type doesn't exist.", e);
		}

		//get the new properties: (P - DB)
		Set<String> toAdd = new LinkedHashSet<String>(objectType.getObjectTypePropertiesMap().keySet());
		toAdd.removeAll(dbObjectType.getObjectTypePropertiesMap().keySet());

		//get the updated properties: (DB intersection P)
		Set<String> toUpdate = new LinkedHashSet<String>(dbObjectType.getObjectTypePropertiesMap().keySet());
		toUpdate.retainAll(objectType.getObjectTypePropertiesMap().keySet());

		//check constraints ----------------------------------------------------
		//1: inherited properties MUST NOT be modified (check no needed: inherited properties are filtered in the CMIS layer)
		//5: required properties MAY be changed to optional (check not needed)
		//7: property definitions MUST NOT be removed (check not needed)
		
		//3: only leaf types may be be modified
		if(objectTypeSelector.countObjectTypeChildren(
				objectType.getRepository().getCmisId(), objectType.getCmisId()) > 0) {
			throw new CmisConstraintException("Not allowed to update this type, only leaf types may be be modified.");
		}

		//4: any added properties marked as required MUST have a default value
		//TODO update the current objects creating this property value?
		for(String cmisId: toAdd) {
			ObjectTypeProperty otp = objectType.getObjectTypePropertiesMap().get(cmisId);
			validateObjectTypePropertyDefaultValue(otp);
			
			//add into the db object
			dbObjectType.addObjectTypeProperty(otp);
		}
		
		//the id and the query name of the properties are not used in the hierarchy
		validateObjectTypePropertyHierarchy(dbObjectType);

		//check if the new added cmis:secondary properties are unique in whole repository
		validateObjectTypePropertyUnique(dbObjectType);
		
		/*
		 * If the update is applied on a root type, we MUST void modifying the
		 * CMIS properties (other extra added properties could me altered) 
		 */
		ObjectType baseObjectType = getBaseTypeDefinition(dbObjectType);
		
		for(String cmisId: toUpdate) {
			ObjectTypeProperty otp = objectType.getObjectTypePropertiesMap().get(cmisId);
			ObjectTypeProperty dbOtp = dbObjectType.getObjectTypePropertiesMap().get(cmisId);

			//ignore unaltered properties
			if(ObjectTypeProperty.FULL_COMPARATOR.compare(otp, dbOtp) == 0) {
				continue;
			}
			
			validateObjectTypePropertyBase(dbObjectType, baseObjectType, cmisId);
			validateObjectTypePropertyRequired(otp, dbOtp);
			validateObjectTypePropertyChoices(otp, dbOtp);
			validateObjectTypePropertyMaxLength(otp, dbOtp);
			validateObjectTypePropertyMinValue(otp, dbOtp);
			validateObjectTypePropertyMaxValue(otp, dbOtp);
			validateObjectTypePropertyCardinality(otp, dbOtp);
			
			
			//update the db object
			try {
				BeanUtils.copyProperties(otp, dbOtp);
			} catch (IllegalAccessException e) {
				throw new CmisConstraintException("Not allowed to update this type, internal error.", e);
			} catch (InvocationTargetException e) {
				throw new CmisConstraintException("Not allowed to update this type, internal error.", e);
			}
			
		}//update ObjectTypeProperties
		

		//if everything is ok the object type is the db object type with the changes.
		return dbObjectType;
	}


	/**
	 * Validates required properties have a default value.
	 * @param otp
	 */
	private void validateObjectTypePropertyDefaultValue(ObjectTypeProperty otp) {
		if(otp.getRequired() && otp.getDefaultValue() == null) {
			throw new CmisConstraintException("Not allowed to update this type, any added properties marked as required MUST have a default value.");
		}
	}

	/**
	 * Validates the properties from CMIS types are not modified.
	 * 
	 * @param dbObjectType
	 * @param baseObjectType
	 * @param cmisId
	 */
	private void validateObjectTypePropertyBase(ObjectType dbObjectType, ObjectType baseObjectType, String cmisId) {
		//2: properties defined by the CMIS specification MUST NOT be modified
		if(dbObjectType.getParent() == null && baseObjectType.getObjectTypePropertiesMap().containsKey(cmisId)) {
			throw new CmisConstraintException(
				"Not allowed to update this type, properties defined by the CMIS specification MUST NOT be modified.");
		}
	}

	/**
	 * Validates the property optional hasn't changed to required.
	 * 
	 * @param otp
	 * @param dbOtp
	 */
	private void validateObjectTypePropertyRequired(ObjectTypeProperty otp, ObjectTypeProperty dbOtp) {
		//6: optional properties MUST NOT be changed to required
		if(!dbOtp.getRequired() && otp.getRequired()) {
			throw new CmisConstraintException("Not allowed to update this type, optional properties MUST NOT be changed to required.");	
		}
	}

	/**
	 * Validates the properties choices
	 * @param otp
	 * @param dbOtp
	 */
	private void validateObjectTypePropertyChoices(ObjectTypeProperty otp, ObjectTypeProperty dbOtp) {
		//8: property choices MAY be changed
		if(Boolean.TRUE.equals(dbOtp.getOpenChoice()) && Boolean.FALSE.equals(otp.getOpenChoice())) {
			throw new CmisConstraintException("Not allowed to update this type, 'open choice' MUST NOT change from TRUE to FALSE.");
		}
		if(Boolean.FALSE.equals(otp.getOpenChoice())) {
			Set<String> dbChoices = new HashSet<String>(Arrays.asList(dbOtp.getChoices().split(Constants.CMIS_MULTIVALUE_SEP)));
			Set<String> choices = new HashSet<String>(Arrays.asList(otp.getChoices().split(Constants.CMIS_MULTIVALUE_SEP)));
			if(!choices.containsAll(dbChoices)) {
				throw new CmisConstraintException("Not allowed to update this type, choices MUST NOT be removed if 'open choice' is FALSE.");
			}
		}
	}

	/**
	 * Validates the property constraints: max length
	 * @param otp
	 * @param dbOtp
	 */
	private void validateObjectTypePropertyMaxLength(ObjectTypeProperty otp, ObjectTypeProperty dbOtp) {
		//9: validations constraints on existing properties MAY be relaxed but they MUST NOT be further restricted
		
		final CmisConstraintException constraintException = 
				new CmisConstraintException("Not allowed to update this type, validation constraints on existing properties MUST NOT be further restricted (max length).");
		
		if(dbOtp.getMaxLength() == null && otp.getMaxLength() != null) {
			throw constraintException;
		}
		if(dbOtp.getMaxLength() != null && otp.getMaxLength() != null && dbOtp.getMaxLength().compareTo(otp.getMaxLength()) > 0) {
			throw constraintException;
		}

	}

	/**
	 * Validates the property constraints: max value
	 * @param otp
	 * @param dbOtp
	 */
	private void validateObjectTypePropertyMaxValue(ObjectTypeProperty otp, ObjectTypeProperty dbOtp) {
		//9: validations constraints on existing properties MAY be relaxed but they MUST NOT be further restricted
		
		final CmisConstraintException constraintException = 
				new CmisConstraintException("Not allowed to update this type, validation constraints on existing properties MUST NOT be further restricted (max value).");
		
		if(dbOtp.getMaxValue() == null && otp.getMaxValue() != null) {
			throw constraintException;
		}
		if (dbOtp.getMaxValue() != null && otp.getMaxValue() != null && dbOtp.getMaxValue().compareTo(otp.getMaxValue()) > 0) {
			throw constraintException;
		}

	}
	
	/**
	 * Validates the property constraints: min value
	 * @param otp
	 * @param dbOtp
	 */
	private void validateObjectTypePropertyMinValue(ObjectTypeProperty otp, ObjectTypeProperty dbOtp) {
		//9: validations constraints on existing properties MAY be relaxed but they MUST NOT be further restricted
		
		final CmisConstraintException constraintException = 
				new CmisConstraintException("Not allowed to update this type, validation constraints on existing properties MUST NOT be further restricted (min value).");
		
		if(dbOtp.getMinValue() == null && otp.getMinValue() != null) {
			throw constraintException;
		}
		if(dbOtp.getMinValue() != null && otp.getMinValue() != null && dbOtp.getMinValue().compareTo(otp.getMinValue()) < 0) {
			throw constraintException;
		}
	}
	
	
	/**
	 * Validates the property cardinality and type were not altered.
	 *  
	 * @param otp
	 * @param dbOtp
	 */
	private void validateObjectTypePropertyCardinality(ObjectTypeProperty otp, ObjectTypeProperty dbOtp) {
		//10: An existing property type's data type and cardinality MUST NOT be changed
		if(dbOtp.getPropertyType() != otp.getPropertyType()) {
			throw new CmisConstraintException("Not allowed to update this type, an existing property type's data type MUST NOT be changed.");	
		}
		if(dbOtp.getCardinality() != otp.getCardinality()) {
			throw new CmisConstraintException("Not allowed to update this type, an existing property type's cardinality MUST NOT be changed.");	
		}
	}

	/**
	 * Returns the base type with the CMIS spec defaults. 
	 * 
	 * This method is not hitting the database, it's using the static definition.
	 *  
	 * @param objectType
	 * @return
	 */
	private ObjectType getBaseTypeDefinition(ObjectType objectType) {

		ObjectType ot = null;
		if(objectType.getParent() == null) {
			String baseId = objectType.getBase().getCmisId();
			Repository repository = new Repository();
			repository.setCmisId(objectType.getRepository().getCmisId());
			
			if(BaseTypeId.CMIS_DOCUMENT.value().equals(baseId)) {
				ot = createDocumentType(repository);
			} else if(BaseTypeId.CMIS_FOLDER.value().equals(baseId)) {
				ot = createFolderType(repository);
			} else if(BaseTypeId.CMIS_RELATIONSHIP.value().equals(baseId)) {
				ot = createRelationshipType(repository);
			} else if(BaseTypeId.CMIS_POLICY.value().equals(baseId)){
				ot = createPolicyType(repository);
			} else if(BaseTypeId.CMIS_ITEM.value().equals(baseId)){
				ot = createItemType(repository);
			} else if(BaseTypeId.CMIS_SECONDARY.value().equals(baseId)){
				ot = createSecondaryType(repository);
			} else {
				throw new CmisConstraintException("Not allowed to update this type, unsupported base type, contact support.");
			}
		}
		return ot;
	}
	

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#deleteType(java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteType(String repositoryId, String typeId) {

		boolean isBasicType = false;
		try {
			BaseTypeId.fromValue(typeId);
			isBasicType = true;
		} catch (IllegalArgumentException e) {
			
		}
		
		if(isBasicType) {
			throw new CmisConstraintException("Not allowed to delete base types.");
		} else if(objectTypeSelector.countObjectTypeChildren(repositoryId, typeId) > 0){
			throw new CmisConstraintException("Not allowed to delete this type because there are subtypes.");
		} else if (cmisObjectSelector.countObjectByType(repositoryId, typeId) > 0) {
			throw new CmisConstraintException("Not allowed to delete this type because there are objects created of this type.");
		} else {
			ObjectType objectType = objectTypeSelector.getObjectTypeByCmisId(repositoryId, typeId);
			remove(objectType);
		}
		
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#getAllRepositories()
	 */
	@Override
	public List<Repository> getAllRepositories() {
		return repoSelector.getAllRepositories();
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#getRepositoryById(java.lang.String)
	 */
	@Override
	public Repository getRepositoryById(String repositoryId) {
		return repoSelector.getRepository(repositoryId);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#getRepositoryByIdWithHandlers(java.lang.String)
	 */
	@Override
	public Repository getRepositoryByIdWithHandlers(String repositoryId) {
		return repoSelector.getRepositoryWithSecurityHandlers(repositoryId);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#createPermission(eu.trade.repo.model.Permission, java.lang.String)
	 */
	@Override
	public void createPermission(Permission permission, String repositoryId) {
		Repository repository = repoSelector.getRepository(repositoryId);
		repository.addPermission(permission);
		persist(repository);
		security.clean(repositoryId);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#getPermission(java.lang.String, java.lang.String)
	 */
	@Override
	public Permission getPermission(String name, String repositoryId) {
		return permissionSelector.getPermission(name, repositoryId);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#getRepositoryPermissions(java.lang.String)
	 */
	@Override
	public List<Permission> getRepositoryPermissions(String repositoryId) {
		return permissionSelector.getAllPermissions(repositoryId);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#getRepositoryPermissionsMappings(java.lang.String)
	 */
	@Override
	public List<PermissionMapping> getRepositoryPermissionsMappings(String repositoryId) {
		return permissionMappingSelector.getRepositoryPermissionMappingsWithPermission(repositoryId);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#updatePermission(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void updatePermission(String permissionName, String permissionNewName, String permissionNewDescription, String repositoryId) {
		Permission permission = permissionSelector.getPermission(permissionName, repositoryId);
		permission.setName(permissionNewName);
		permission.setDescription(permissionNewDescription);
		security.clean(repositoryId);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#deletePermission(java.lang.String, java.lang.String)
	 */
	@Override
	public void deletePermission(String permissionToDelete, String repositoryId) {
		Permission permission = permissionSelector.getPermission(permissionToDelete, repositoryId);
		// delete children
		Map<Permission, Collection<Permission>> permissionWithChildren = PermissionsUtil.buildPermissionsByParentMap(permissionSelector.getAllPermissions(repositoryId));
		Collection<Permission> children = permissionWithChildren.get(permission);
		if (children != null) {
			for (Permission child : children) {
				getEntityManager().remove(child);
			}
		}
		getEntityManager().remove(permission);
		security.clean(repositoryId);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#setPermissionMappings(java.util.Set, java.lang.String)
	 */
	@Override
	public void setPermissionMappings(Set<PermissionMapping> permissionMappings, String repositoryId) {
		Repository repository = getRepositoryById(repositoryId);
		for (PermissionMapping permissionMapping : permissionMappings) {
			permissionMapping.setRepository(repository);
		}
		repository.setPermissionMappings(permissionMappings);
		getEntityManager().merge(repository);
		security.clean(repositoryId);
	}

	/**
	 * Since the root folder ant its cmis id are immutable, this can be cached.
	 * 
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#getRootFolderId(java.lang.String)
	 */
	@Override
	public String getRootFolderId(String repositoryId) {
		String rootFolerId = rootFolderIdCache.get(repositoryId);
		if (rootFolerId == null) {
			rootFolerId = initRootFolderId(repositoryId);
		}
		return rootFolerId;
	}

	private String initRootFolderId(String repositoryId) {
		synchronized (rootFolderIdCache) {
			String rootFolerId = rootFolderIdCache.get(repositoryId);
			if (rootFolerId == null) {
				rootFolerId = cmisObjectSelector.getRootFolderId(repositoryId);
				rootFolderIdCache.put(repositoryId, rootFolerId);
			}
			return rootFolerId;
		}
	}

	private void removeRootFolderId(String repositoryId) {
		synchronized (rootFolderIdCache) {
			rootFolderIdCache.remove(repositoryId);
		}
	}

	/**
	 * @see eu.trade.repo.util.Cleanable#clean()
	 */
	@Override
	public void clean() {
		synchronized (rootFolderIdCache) {
			rootFolderIdCache.clear();
		}
	}

	@Override
	public Repository update(Repository repository) {
		if (LOG.isInfoEnabled()) {
			String log = String.format(
								"\nRepository updated by %s [\n%s\n]", 
								SecurityContextHolder.getContext().getAuthentication().getName(),
								repository.toString());
			LOG.info(log);
		}
		return merge(repository);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#toggleSecurity(java.lang.String)
	 */
	@Override
	public Repository toggleSecurity(String repositoryId) {
		Repository repository = getRepositoryById(repositoryId);
		// Check if migration is required and set the new type.
		if (aclSelector.isThereNonDefaultAcl(repositoryId)) {
			throw new IllegalStateException(
					"The repository's security type cannot be changed dut to the presence of non default ACL principal Ids.");
		}
		switch (repository.getSecurityType()) {
			case SIMPLE:
				repository.setSecurityType(SecurityType.MULTIPLE);
				break;

			case MULTIPLE:
				// remove additional handlers
				repository.setSecurityType(SecurityType.SIMPLE);
				repository.getSecurityHandlers().clear();
				break;
		}
		return update(repository);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#updateSecurity(java.lang.String,
	 *      java.lang.String, java.lang.String, java.util.Set, java.util.Set)
	 */
	@Override
	public Repository updateSecurity(String repositoryId,
			String defaultAuthenticationHandler,
			String defaultAuthorisationHandler,
			Set<String> enabledAuthenticationHandlers,
			Set<String> enabledAuthorisationHandlers) {
		Repository repository = getRepositoryById(repositoryId);
		repository.setAuthenticationHandler(defaultAuthenticationHandler);
		repository.setAuthorisationHandler(defaultAuthorisationHandler);
		if (!repository.getSecurityType().equals(SecurityType.SIMPLE)) {
			updateSecurityHandlers(repository, enabledAuthenticationHandlers,
					enabledAuthorisationHandlers);
		}
		return update(repository);
	}

	private void updateSecurityHandlers(Repository repository,
			Set<String> enabledAuthenticationHandlers,
			Set<String> enabledAuthorisationHandlers) {
		updateSecurityHandlers(repository, enabledAuthenticationHandlers,
				repository.getAuthenticationHandler(),
				HandlerType.AUTHENTICATION);
		updateSecurityHandlers(repository, enabledAuthorisationHandlers,
				repository.getAuthorisationHandler(), HandlerType.AUTHORISATION);
	}

	private void updateSecurityHandlers(Repository repository,
			Set<String> handlersToEnable, String defaultHandler,
			HandlerType handlerType) {
		handlersToEnable.remove(defaultHandler);
		Set<SecurityHandler> handlersToRemove = new HashSet<>();
		for (SecurityHandler securityHandler : repository.getSecurityHandlers()) {
			if (securityHandler.getHandlerType().equals(handlerType)) {
				if (handlersToEnable.contains(securityHandler.getHandlerName())) {
					handlersToEnable.remove(securityHandler.getHandlerName());
				} else {
					handlersToRemove.add(securityHandler);
				}
			}
		}
		for (SecurityHandler securityHandler : handlersToRemove) {
			repository.removeSecurityHandler(securityHandler);
		}
		for (String handlerName : handlersToEnable) {
			SecurityHandler securityHandler = new SecurityHandler();
			securityHandler.setHandlerName(handlerName);
			securityHandler.setHandlerType(handlerType);
			repository.addSecurityHandler(securityHandler);
		}
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#createRepository(eu.trade.repo.model.Repository)
	 */
	@Override
	public Repository createRepository(Repository repository) {
		repository.setGetDescendants(true);
		repository.setGetFolderTree(true);
		repository.setContentStreamUpdatability(CapabilityContentStreamUpdates.ANYTIME);
		repository.setChanges(CapabilityChanges.OBJECTIDSONLY);
		repository.setRenditions(CapabilityRenditions.READ);
		repository.setMultifiling(true);
		repository.setUnfiling(true);
		repository.setVersionSpecificFiling(true);
		repository.setPwcUpdatable(true);
		repository.setPwcSearchable(true);
		repository.setAllVersionsSearchable(true);
		repository.setQuery(CapabilityQuery.BOTHCOMBINED);
		repository.setJoin(CapabilityJoin.INNERANDOUTER);
		repository.setAcl(CapabilityAcl.MANAGE);
		repository.setAclPropagation(AclPropagation.PROPAGATE);
		// Default security model
		repository.setSecurityType(SecurityType.SIMPLE);
		repository.setAuthenticationHandler(Constants.BUILTIN);
		repository.setAuthorisationHandler(Constants.BUILTIN);

		// define default permissions
		Permission all = new Permission(Constants.CMIS_ALL, "All");
		Permission write = new Permission(Constants.CMIS_WRITE, "Write");
		Permission read = new Permission(Constants.CMIS_READ, "Read");

		write.setParent(all);
		read.setParent(write);

		repository.addPermission(all);
		repository.addPermission(write);
		repository.addPermission(read);

		/*
		 * Saved the repository with the permissions, because this is respecting
		 * the order. In the other hand, permissions are saved in the
		 * permission_mapping order.
		 */
		persist(repository);

		// assign permission mapping
		Map<String, Permission> permissions = new LinkedHashMap<String, Permission>();
		permissions.put(Constants.CMIS_READ, read);
		permissions.put(Constants.CMIS_WRITE, write);
		permissions.put(Constants.CMIS_ALL, all);
		for (Map.Entry<String, String> entry : ActionMap.getDbKeys().entrySet()) {
			repository.addPermissionMapping(new PermissionMapping(entry.getKey(), permissions.get(entry.getValue())));
		}

		// create basic types
		repository.addObjectType(createDocumentType(repository));
		ObjectType folder = createFolderType(repository);
		repository.addObjectType(folder);
		repository.addObjectType(createRelationshipType(repository));
		repository.addObjectType(createPolicyType(repository));
		repository.addObjectType(createItemType(repository));
		repository.addObjectType(createSecondaryType(repository));

		// create the root folder
		CMISObject rootFolder = createRootFolder(folder, all);

		persist(repository);
		persist(rootFolder);

		// TODO [porrjai] Review if it is possible to do this using ChangeLog component
		// create first event log entry
		ChangeEvent event = createInitialChangeEvent(repository, rootFolder);
		persist(event);

		return repository;
	}

	private ChangeEvent createInitialChangeEvent(Repository repository, CMISObject rootFolder) {
		ChangeEvent event = new ChangeEvent();
		event.setObjectId(rootFolder.getCmisObjectId());
		event.setChangeLogToken(generator.next());
		event.setChangeTime(new Date());
		event.setChangeType(ChangeType.CREATED);
		event.setRepository(repository);
		event.setUsername("internal:system");
		return event;
	}

	private CMISObject createRootFolder(ObjectType folder, Permission defaultPermission) {
		CMISObject rootFolder = new CMISObject(folder);

		String generatedId = generator.next();
		Date now = currentDate.getDate();

		rootFolder.addProperty(buildProperty(folder, PropertyIds.NAME, "rootFolder"));
		rootFolder.addProperty(buildProperty(folder, PropertyIds.OBJECT_ID, generatedId));
		rootFolder.addProperty(buildProperty(folder, PropertyIds.BASE_TYPE_ID, Constants.TYPE_CMIS_FOLDER));
		rootFolder.addProperty(buildProperty(folder, PropertyIds.OBJECT_TYPE_ID, Constants.TYPE_CMIS_FOLDER));
		rootFolder.addProperty(buildProperty(folder, PropertyIds.CREATED_BY, "admin"));
		rootFolder.addProperty(buildProperty(folder, PropertyIds.CREATION_DATE, now));
		rootFolder.addProperty(buildProperty(folder, PropertyIds.LAST_MODIFIED_BY, "admin"));
		rootFolder.addProperty(buildProperty(folder, PropertyIds.LAST_MODIFICATION_DATE, now));
		rootFolder.addProperty(buildProperty(folder, PropertyIds.CHANGE_TOKEN, generator.next()));
		rootFolder.addProperty(buildProperty(folder, PropertyIds.PATH, "/"));
		rootFolder.setCmisObjectId(generatedId);
		
		// give access to everyone to the root folder
		Acl acl = new Acl();
		acl.setIsDirect(true);
		acl.setPermission(defaultPermission);
		acl.setPrincipalId(Constants.PRINCIPAL_ID_ANYONE);
		rootFolder.addAcl(acl);
		
		return rootFolder;
	}

	private Property buildProperty(ObjectType objectType, String name, Object value) {
		return new Property(findObjectTypeProperty(objectType, name), value);
	}

	private ObjectTypeProperty findObjectTypeProperty(ObjectType objectType, String name) {
		for (ObjectTypeProperty prop : objectType.getObjectTypeProperties()) {
			if (prop.getCmisId().equals(name)) {
				return prop;
			}
		}
		return null;
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#deleteRepository(java.lang.String)
	 */
	@Override
	public void deleteRepository(String repositoryId) {
		getEntityManager().createNamedQuery("Repository.deleteById-native").setParameter("id", repositoryId).executeUpdate();
		removeRootFolderId(repositoryId);
		security.clean(repositoryId);
	}

	public void setGenerator(IDGenerator generator) {
		this.generator = generator;
	}

	public void setCurrentDate(ICurrentDate currentDate) {
		this.currentDate = currentDate;
	}

	@Override
	public String getLatestChangeLogEvent(String repositoryId) {
		Repository repo = repoSelector.getRepository(repositoryId);
		if (repo.getChanges() == CapabilityChanges.NONE) {
			return "";
		}

		return changeLogSelector.getLatestChangeLogToken(repoSelector.getRepository(repositoryId).getId());
	}

	/* (non-Javadoc)
	 * @see eu.trade.repo.service.interfaces.IRepositoryService#getDisaggregatedVersionSeries(java.lang.String)
	 */
	@Override
	public List<String> getDisaggregatedVersionSeries(String repositoryId) {
		return repoSelector.getDisaggregatedVersionSeries(repositoryId);
	}

	
	
	@Override
	public Map<ObjectType, Boolean> getPolicies(String repositoryId) {
		Map<ObjectType, Boolean> results = new LinkedHashMap<ObjectType, Boolean>();
		Repository repository = getRepositoryById(repositoryId);
		//first checks the implementation lists
		for(String policyTypeId: policyImpls.keySet()) {
			
			//if exists
			if(objectTypeSelector.countObjectTypeByCmisId(repositoryId, policyTypeId) > 0) {
				ObjectType policyType = objectTypeSelector.getObjectTypeByCmisId(repositoryId, policyTypeId);
				results.put(policyType, repository.getEnabledPolicies().contains(policyType));
			}
		}
		return results;
	}

	@Override
	public Set<String> getEnabledPolicies(String repositoryId) {
		Set<String> enabledPolicies = new HashSet<String>(); 
		for(ObjectType policyType: getRepositoryById(repositoryId).getEnabledPolicies()) {
			enabledPolicies.add(policyType.getCmisId());
		}
		return enabledPolicies;
	}

	@Override
	public void setEnabledPolicies(String repositoryId, Set<String> enabledPolicies) {
		//some initial validations
		if(!policyImpls.keySet().containsAll(enabledPolicies)) {
			throw new CmisInvalidArgumentException("Invalid Policy type (Server code not found).");
		}
		
		Set<ObjectType> enabledPoliciesToPersist = new HashSet<ObjectType>();
		for(String policyTypeId: enabledPolicies) {
			if(objectTypeSelector.countObjectTypeByCmisId(repositoryId, policyTypeId) > 0) {
				ObjectType policyType = objectTypeSelector.getObjectTypeByCmisId(repositoryId, policyTypeId);
				enabledPoliciesToPersist.add(policyType);
			} else {
				throw new CmisInvalidArgumentException("Invalid Policy type (Object Type not found).");
			}
		}
		
		Repository repository = getRepositoryById(repositoryId);
		repository.setEnabledPolicies(enabledPoliciesToPersist);
		merge(repository);		
	}
	
	
	
	
}
