package eu.trade.repo;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService;
import org.apache.chemistry.opencmis.commons.server.ObjectInfo;
import org.apache.chemistry.opencmis.commons.spi.DiscoveryService;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.commons.spi.MultiFilingService;
import org.apache.chemistry.opencmis.commons.spi.NavigationService;
import org.apache.chemistry.opencmis.commons.spi.ObjectService;
import org.apache.chemistry.opencmis.commons.spi.PolicyService;
import org.apache.chemistry.opencmis.commons.spi.RelationshipService;
import org.apache.chemistry.opencmis.commons.spi.RepositoryService;
import org.apache.chemistry.opencmis.commons.spi.VersioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.service.cmis.CompleteAclService;

/**
 * Repo service implementation.
 */
public class RepoService extends AbstractCmisService {

	private static final Logger LOG = LoggerFactory.getLogger(RepoService.class);

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ObjectService objectService;
	@Autowired
	private NavigationService navigationService;
	@Autowired
	private MultiFilingService multiFilingService;
	@Autowired
	private DiscoveryService discoveryService;
	@Autowired
	private VersioningService versioningService;
	@Autowired
	private RelationshipService relationshipService;
	@Autowired
	private PolicyService policyService;
	@Autowired
	private CompleteAclService aclService;

	/**
	 * RepoService is currently configured as prototype, so to synchronize
	 * methods using the key (repository + object id) we need a static collection 
	 * to store the objects among all the RepoService instances.
	 */
	private static final Map<String, Long> synchronizedKeys = new ConcurrentHashMap<String, Long>(); 
	
	/**
	 * Constructor.
	 */
	public RepoService() {
	}

	// --- repository service ---

	/**
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getRepositoryInfo(java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
		return repositoryService.getRepositoryInfo(repositoryId, extension);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getRepositoryInfos(org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
		return repositoryService.getRepositoryInfos(extension);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getTypeChildren(java.lang.String, java.lang.String, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
			BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		return repositoryService.getTypeChildren(repositoryId, typeId, includePropertyDefinitions, maxItems, skipCount, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getTypeDefinition(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
		return repositoryService.getTypeDefinition(repositoryId, typeId, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getTypeDescendants(java.lang.String, java.lang.String, java.math.BigInteger, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
			Boolean includePropertyDefinitions, ExtensionsData extension) {
		return repositoryService.getTypeDescendants(repositoryId, typeId, depth, includePropertyDefinitions, extension);
	}

	@Override
	public TypeDefinition createType(String repositoryId, TypeDefinition type, ExtensionsData extension) {
		return repositoryService.createType(repositoryId, type, extension);
	}

	@Override
	public TypeDefinition updateType(String repositoryId, TypeDefinition type, ExtensionsData extension) {
		return repositoryService.updateType(repositoryId, type, extension);
	}

	@Override
	public void deleteType(String repositoryId, String typeId, ExtensionsData extension) {
		repositoryService.deleteType(repositoryId, typeId, extension);
	}

	// --- navigation service ---
	
	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getChildren(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
			Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
			Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		return navigationService.getChildren(repositoryId, folderId, filter, orderBy, includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, maxItems, skipCount, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getDescendants(java.lang.String, java.lang.String, java.math.BigInteger, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth,
			String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
			String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
		return navigationService.getDescendants(repositoryId, folderId, depth, filter, includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getFolderParent(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {
		return navigationService.getFolderParent(repositoryId, folderId, filter, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getFolderTree(java.lang.String, java.lang.String, java.math.BigInteger, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth,
			String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
			String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
		return navigationService.getFolderTree(repositoryId, folderId, depth, filter, includeAllowableActions, includeRelationships, renditionFilter, includePathSegment, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getObjectParents(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
			Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
			Boolean includeRelativePathSegment, ExtensionsData extension) {
		return navigationService.getObjectParents(repositoryId, objectId, filter, includeAllowableActions, includeRelationships, renditionFilter, includeRelativePathSegment, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getCheckedOutDocs(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
			Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
			BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		return navigationService.getCheckedOutDocs(repositoryId, folderId, filter, orderBy, includeAllowableActions, includeRelationships, renditionFilter, maxItems, skipCount, extension);
	}

	// --- object service ---

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#createDocument(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, org.apache.chemistry.opencmis.commons.data.ContentStream, org.apache.chemistry.opencmis.commons.enums.VersioningState, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public String createDocument(String repositoryId, Properties properties, String folderId,
			ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		return objectService.createDocument(repositoryId, properties, folderId, contentStream, versioningState, policies, addAces, removeAces, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#createDocumentFromSource(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, org.apache.chemistry.opencmis.commons.enums.VersioningState, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public String createDocumentFromSource(String repositoryId, String sourceId, Properties properties, String folderId,
			VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		return objectService.createDocumentFromSource(repositoryId, sourceId, properties, folderId, versioningState, policies, addAces, removeAces, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#createFolder(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies,
			Acl addAces, Acl removeAces, ExtensionsData extension) {
		return objectService.createFolder(repositoryId, properties, folderId, policies, addAces, removeAces, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#deleteContentStream(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken, ExtensionsData extension) {
		objectService.deleteContentStream(repositoryId, objectId, changeToken, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#deleteObject(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension) {
		objectService.deleteObject(repositoryId, objectId, allVersions, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#deleteObjectOrCancelCheckOut(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public void deleteObjectOrCancelCheckOut(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension) {

		Properties props = objectService.getObject(repositoryId, objectId, null, false, IncludeRelationships.NONE, null, false, false, extension).getProperties();

		PropertyData<?> pdata = props.getProperties().get(PropertyIds.IS_PRIVATE_WORKING_COPY);

		if ( pdata != null && Boolean.TRUE.equals(pdata.getFirstValue()) ) {
			versioningService.cancelCheckOut(repositoryId, objectId, extension);
		} else {
			objectService.deleteObject(repositoryId, objectId, allVersions, extension);
		}
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#deleteTree(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.UnfileObject, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
			UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {
		return objectService.deleteTree(repositoryId, folderId, allVersions, unfileObjects, continueOnFailure, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getAllowableActions(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {
		return objectService.getAllowableActions(repositoryId, objectId, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getContentStream(java.lang.String, java.lang.String, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
			BigInteger length, ExtensionsData extension) {
		return objectService.getContentStream(repositoryId, objectId, streamId, offset, length, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getObject(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
			IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
			Boolean includeAcl, ExtensionsData extension) {
		return objectService.getObject(repositoryId, objectId, filter, includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds, includeAcl, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getObjectByPath(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
			IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
			Boolean includeAcl, ExtensionsData extension) {
		return objectService.getObjectByPath(repositoryId, path, filter, includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds, includeAcl, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getProperties(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {
		return objectService.getProperties(repositoryId, objectId, filter, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getRenditions(java.lang.String, java.lang.String, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter,
			BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		return objectService.getRenditions(repositoryId, objectId, renditionFilter, maxItems, skipCount, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#moveObject(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
			ExtensionsData extension) {
		objectService.moveObject(repositoryId, objectId, targetFolderId, sourceFolderId, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#setContentStream(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.Boolean, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.ContentStream, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
			Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {
		objectService.setContentStream(repositoryId, objectId, overwriteFlag, changeToken, contentStream, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#updateProperties(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.Properties, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
			Properties properties, ExtensionsData extension) {
		
		String key = objectId.getValue();
		
		try {
			synchronized (getSyncKey(key)) {
				objectService.updateProperties(repositoryId, objectId, changeToken, properties, extension);
			}
		} finally {
			cleanSyncKey(key);
		}
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#createRelationship(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public String createRelationship(String repositoryId, Properties properties, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		return objectService.createRelationship(repositoryId, properties, policies, addAces, removeAces, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#createPolicy(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public String createPolicy(String repositoryId, Properties properties, String folderId, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		return objectService.createPolicy(repositoryId, properties, folderId, policies, addAces, removeAces, extension);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#createItem(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public String createItem(String repositoryId, Properties properties,
			String folderId, List<String> policies, Acl addAces,
			Acl removeAces, ExtensionsData extension) {
		return objectService.createItem(repositoryId, properties, folderId, policies, addAces,removeAces, extension);
	}
	
	// --- versioning service ---

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getAllVersions(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter,
			Boolean includeAllowableActions, ExtensionsData extension) {
		return versioningService.getAllVersions(repositoryId, objectId, versionSeriesId, filter, includeAllowableActions, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getObjectOfLatestVersion(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
			Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
			String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
		return versioningService.getObjectOfLatestVersion(repositoryId, objectId, versionSeriesId, major, filter, includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds, includeAcl, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getPropertiesOfLatestVersion(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
			Boolean major, String filter, ExtensionsData extension) {
		return versioningService.getPropertiesOfLatestVersion(repositoryId, objectId, versionSeriesId, major, filter, extension);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#checkOut(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.ExtensionsData, org.apache.chemistry.opencmis.commons.spi.Holder)
	 */
	@Override
	public void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension, Holder<Boolean> contentCopied) {
		String key = objectId.getValue();
		
		try {
			synchronized (getSyncKey(key)) {
				versioningService.checkOut(repositoryId, objectId, extension, contentCopied);
			}
		} finally {
			cleanSyncKey(key);
		}
	}
	
	/**
	 * Fully synchronized method to get a syncronization object per key
	 * 
	 * @param key
	 * @return
	 */
	private static synchronized Long getSyncKey(String key) {
		if (!synchronizedKeys.containsKey(key) ) {
			synchronizedKeys.put(key, new Long(System.currentTimeMillis()));
		}
		return synchronizedKeys.get(key);
	}
	
	/**
	 * Clean the synchronization key when the execution is done
	 * @param key
	 */
	private static void cleanSyncKey(String key) {
		synchronizedKeys.remove(key);
	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#cancelCheckOut(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {
		String key = objectId;
		
		try {
			synchronized (getSyncKey(key)) {
				versioningService.cancelCheckOut(repositoryId, objectId, extension);
			}
		} finally {
			cleanSyncKey(key);
		}		

	}

	/**
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#checkIn(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.Properties, org.apache.chemistry.opencmis.commons.data.ContentStream, java.lang.String, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public void checkIn(String repositoryId, Holder<String> objectId, Boolean major, Properties properties, ContentStream contentStream,
			String checkinComment, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {

		String key = objectId.getValue();
		
		try {
			synchronized (getSyncKey(key)) {
				versioningService.checkIn(repositoryId, objectId, major, properties, contentStream, checkinComment, policies, addAces, removeAces, extension);
			}
		} finally {
			cleanSyncKey(key);
		}		
	}

	// --- ACL service ---

	/**
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getAcl(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {
		return aclService.getAcl(repositoryId, objectId, onlyBasicPermissions, extension);
	}

	/**
	 * ApplyACL service implementation for AtomPub.
	 * <p>
	 * The ACL provided is intended to be the final ACL for the object (built by Atom from the current ACL plus the added ACL and removed ACL)
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#applyAcl(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.enums.AclPropagation)
	 */
	@Override
	public Acl applyAcl(String repositoryId, String objectId, Acl aces, AclPropagation aclPropagation) {
		return aclService.applyAcl(repositoryId, objectId, aces, aclPropagation);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#applyAcl(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.enums.AclPropagation, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public Acl applyAcl(String repositoryId, String objectId, Acl addAces, Acl removeAces, AclPropagation aclPropagation, ExtensionsData extension) {
		return aclService.applyAcl(repositoryId, objectId, addAces, removeAces, aclPropagation, extension);
	}

	// --- Discovery service ---

	@Override
	public ObjectList getContentChanges(String repositoryId, Holder<String> changeLogToken, Boolean includeProperties,
			String filter, Boolean includePolicyIds, Boolean includeAcl, BigInteger maxItems, ExtensionsData extension) {
		return discoveryService.getContentChanges(repositoryId, changeLogToken, includeProperties, filter, includePolicyIds, includeAcl, maxItems, extension);
	}

	@Override
	public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions, Boolean includeAllowableActions,
			IncludeRelationships includeRelationships, String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		return discoveryService.query(repositoryId, statement, searchAllVersions, includeAllowableActions, includeRelationships, renditionFilter, maxItems, skipCount, extension);
	}

	// --- Multifiling service ---

	@Override
	public void addObjectToFolder(String repositoryId, String objectId, String folderId, Boolean allVersions, ExtensionsData extension) {
		multiFilingService.addObjectToFolder(repositoryId, objectId, folderId, allVersions, extension);
	}

	@Override
	public void removeObjectFromFolder(String repositoryId, String objectId, String folderId, ExtensionsData extension) {
		multiFilingService.removeObjectFromFolder(repositoryId, objectId, folderId, extension);
	}

	// --- Relationship service ---

	@Override
	public ObjectList getObjectRelationships(String repositoryId, String objectId, Boolean includeSubRelationshipTypes,
			RelationshipDirection relationshipDirection, String typeId, String filter, Boolean includeAllowableActions,
			BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		return relationshipService.getObjectRelationships(repositoryId, objectId, includeSubRelationshipTypes, relationshipDirection, typeId, filter, includeAllowableActions, maxItems, skipCount, extension);
	}

	// --- Policy service ---

	@Override
	public void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
		policyService.applyPolicy(repositoryId, policyId, objectId, extension);
	}

	@Override
	public List<ObjectData> getAppliedPolicies(String repositoryId, String objectId, String filter, ExtensionsData extension) {
		return policyService.getAppliedPolicies(repositoryId, objectId, filter, extension);
	}

	@Override
	public void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
		policyService.removePolicy(repositoryId, policyId, objectId, extension);
	}

	/**
	 * Overridden just to log the exception just u case the object info returned is null.
	 * 
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService#getObjectInfo(java.lang.String, java.lang.String)
	 */
	@Override
	public ObjectInfo getObjectInfo(String repositoryId, String objectId) {
		ObjectInfo info = super.getObjectInfo(repositoryId, objectId);
		if (info == null) {
			// Code copied from super implementation just to log the exception.
			try {
				ObjectData object = getObject(repositoryId, objectId, null, Boolean.TRUE, IncludeRelationships.BOTH,
						"*", Boolean.TRUE, Boolean.FALSE, null);
				info = getObjectInfoIntern(repositoryId, object);
			} catch (Exception e) {
				LOG.error("Error getting the object info: " + e.getLocalizedMessage(), e);
			}
		}
		return info;
	}
}
