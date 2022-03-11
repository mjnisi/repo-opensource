package eu.trade.repo.service.cmis;

import static eu.trade.repo.util.Constants.BASE_TYPE_CMIS_10;
import static eu.trade.repo.util.Constants.BASE_TYPE_CMIS_11;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionListImpl;
import org.apache.chemistry.opencmis.commons.spi.RepositoryService;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.Repository;
import eu.trade.repo.security.CustomSecured;
import eu.trade.repo.security.CustomSecured.CustomAction;
import eu.trade.repo.security.Security;
import eu.trade.repo.service.cmis.data.in.ObjectTypeBuilder;
import eu.trade.repo.service.cmis.data.out.RepositoryInfoImpl;
import eu.trade.repo.service.cmis.data.out.TypeDefinitionBuilder;
import eu.trade.repo.service.cmis.data.util.Utilities;
import eu.trade.repo.service.interfaces.IRepositoryService;

/**
 * CMIS Repository Service implementation.
 * <p>
 * Implementation of the CMIS repository services that uses the {@link IRepositoryService} to perform the needed operations.
 * 
 * @author porrjai
 */
@Transactional
public class CmisRepositoryService implements RepositoryService {

	@Autowired
	private IRepositoryService repositoryService;

	@Autowired
	private Security security;

	@Autowired
	private Configuration combinedConfig;

	/**
	 * Returns the basic information about the repository.
	 * 
	 * @see org.apache.chemistry.opencmis.commons.spi.RepositoryService#getRepositoryInfos(org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
		List<RepositoryInfo> repositoryInfos = new ArrayList<>();
		List<Repository> repositories = repositoryService.getAllRepositories();
		for (Repository repository : repositories) {
			repositoryInfos.add(new RepositoryInfoImpl(repository, combinedConfig));
		}
		return repositoryInfos;
	}

	/**
	 * Returns the complete information about the repository.
	 * 
	 * @see org.apache.chemistry.opencmis.commons.spi.RepositoryService#getRepositoryInfo(java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
		Repository repository = repositoryService.getRepositoryById(repositoryId);
		String rootFolderId = repositoryService.getRootFolderId(repositoryId);
		return new RepositoryInfoImpl(repository, combinedConfig, rootFolderId, 
				security.getPermissionDefinitions(repositoryId), 
				security.getPermissionMappings(repositoryId), 
				repositoryService.getLatestChangeLogEvent(repositoryId));
	}

	/**
	 * TODO: Pagination not supported.
	 * @see org.apache.chemistry.opencmis.commons.spi.RepositoryService#getTypeChildren(java.lang.String, java.lang.String, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@CustomSecured(CustomAction.LOGIN)
	public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		Set<ObjectType> objectTypes;
		List<TypeDefinition> typeDefinitions = new ArrayList<>();
		TypeDefinitionListImpl result = new TypeDefinitionListImpl(typeDefinitions);
		boolean includeDefinitions = Boolean.TRUE.equals(includePropertyDefinitions);

		if (typeId == null) {
			// Basic CMIS types.
			objectTypes = getBasicTypes(repositoryId, result);
		}
		else if (maxItems != null && skipCount != null) {
			// Paginated query
			objectTypes = repositoryService.getObjectTypeChildren(repositoryId, typeId, includeDefinitions, maxItems.intValue(), skipCount.intValue());
			Long count = repositoryService.countObjectTypeChildren(repositoryId, typeId);
			result.setNumItems(BigInteger.valueOf(count));
			result.setHasMoreItems(skipCount.intValue() + objectTypes.size() < count);
		}
		else {
			// All children
			objectTypes = repositoryService.getObjectTypeChildren(repositoryId, typeId, includeDefinitions);
			result.setNumItems(BigInteger.valueOf(objectTypes.size()));
			result.setHasMoreItems(false);
		}

		for (ObjectType objectType : objectTypes) {
			typeDefinitions.add(TypeDefinitionBuilder.build(objectType, includeDefinitions));
		}
		return result;
	}

	private Set<ObjectType> getBasicTypes(String repositoryId, TypeDefinitionListImpl result) {
		Set<ObjectType> objectTypes = new LinkedHashSet<>();
		
		/* 
		 * The dabase could have base types only for CMIS 1.0 and the client
		 * binding could be CMIS 1.1
		 * Or the database could have all CMIS 1.1 types but the client is using
		 * the binding version 1.0
		 */
		List<BaseTypeId> baseTypes = repositoryService.getBaseObjectTypes(repositoryId);
		switch(security.getCallContextHolder().getClientCmisVersion()) {
			case CMIS_1_0:
				baseTypes.retainAll(BASE_TYPE_CMIS_10);
				break;
			case CMIS_1_1:
				baseTypes.retainAll(BASE_TYPE_CMIS_11);
				break;
		}

		
		
		for (BaseTypeId baseTypeId : baseTypes) {
			ObjectType objectType = repositoryService.getObjectType(repositoryId, baseTypeId.value());
			objectTypes.add(objectType);
		}
		result.setNumItems(BigInteger.valueOf(objectTypes.size()));
		result.setHasMoreItems(false);
		return objectTypes;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.RepositoryService#getTypeDescendants(java.lang.String, java.lang.String, java.math.BigInteger, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@CustomSecured(CustomAction.LOGIN)
	public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth, Boolean includePropertyDefinitions, ExtensionsData extension) {
		List<TypeDefinitionContainer> typeDefinitionContainers = new ArrayList<>();
		addDescendantsTypeDefinitionContainers(typeDefinitionContainers, repositoryId, typeId, depth, includePropertyDefinitions);
		return typeDefinitionContainers;
	}

	private void addDescendantsTypeDefinitionContainers(List<TypeDefinitionContainer> typeDefinitionContainers, String repositoryId, String typeId, BigInteger depth, Boolean includePropertyDefinitions) {
		if (Utilities.getChildrens(depth)) {
			BigInteger newDepth = Utilities.decDepth(depth);
			TypeDefinitionList childrenTypeDefinitions = getTypeChildren(repositoryId, typeId, includePropertyDefinitions, null, null, null);
			for (TypeDefinition childrenTypeDefinition : childrenTypeDefinitions.getList()) {
				TypeDefinitionContainerImpl typeDefinitionContainerImpl = new TypeDefinitionContainerImpl(childrenTypeDefinition);
				String childrenTypeId = childrenTypeDefinition.getId();
				addDescendantsTypeDefinitionContainers(typeDefinitionContainerImpl.getChildren(), repositoryId, childrenTypeId, newDepth, includePropertyDefinitions);
				typeDefinitionContainers.add(typeDefinitionContainerImpl);
			}
		}
	}


	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.RepositoryService#getTypeDefinition(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@CustomSecured(CustomAction.LOGIN)
	public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
		ObjectType objectType = repositoryService.getObjectType(repositoryId, typeId);
		return TypeDefinitionBuilder.build(objectType, true);
	}

	@Override
	@CustomSecured(CustomAction.ADMIN)
	public TypeDefinition createType(String repositoryId, TypeDefinition type, ExtensionsData extension) {
		ObjectType objectType = repositoryService.createType(ObjectTypeBuilder.build(type, repositoryId));
		return TypeDefinitionBuilder.build(objectType, true);
	}

	@Override
	@CustomSecured(CustomAction.ADMIN)
	public TypeDefinition updateType(String repositoryId, TypeDefinition type, ExtensionsData extension) {
		ObjectType objectType = repositoryService.updateType(ObjectTypeBuilder.build(type, repositoryId));
		return TypeDefinitionBuilder.build(objectType, true);
	}

	@Override
	@CustomSecured(CustomAction.ADMIN)
	public void deleteType(String repositoryId, String typeId, ExtensionsData extension) {
		repositoryService.deleteType(repositoryId, typeId);
	}
}
