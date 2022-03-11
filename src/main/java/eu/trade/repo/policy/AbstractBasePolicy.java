package eu.trade.repo.policy;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.BulkUpdateObjectIdAndChangeToken;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
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
import org.apache.chemistry.opencmis.commons.spi.AclService;
import org.apache.chemistry.opencmis.commons.spi.DiscoveryService;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.commons.spi.MultiFilingService;
import org.apache.chemistry.opencmis.commons.spi.NavigationService;
import org.apache.chemistry.opencmis.commons.spi.ObjectService;
import org.apache.chemistry.opencmis.commons.spi.PolicyService;
import org.apache.chemistry.opencmis.commons.spi.RelationshipService;
import org.apache.chemistry.opencmis.commons.spi.RepositoryService;
import org.apache.chemistry.opencmis.commons.spi.VersioningService;

public abstract class AbstractBasePolicy implements RepositoryService, NavigationService, ObjectService, VersioningService,
	DiscoveryService, MultiFilingService, RelationshipService, AclService, PolicyService {
	

	/**
	 * return the Policy Context from the ExtensionData
	 * @param extension
	 * @return
	 */
	protected PolicyContext getPolicyContext(ExtensionsData extension) {
		for(CmisExtensionElement element: extension.getExtensions()) {
			if(element instanceof PolicyContext) {
				return (PolicyContext)element;
			}
		}
		return null;
	}
	
	/**
	 * Return the names of the methods overwritten in the policy implementation
	 * 
	 * @return
	 */
	public Set<String> getMethodsUsedByPolicy() {
		Set<String> methodNames = new HashSet<String>();
		Class currentClass = this.getClass();  
		
		for(Method method: currentClass.getMethods()) {
			if(method.getDeclaringClass().equals(currentClass)) {
				methodNames.add(method.getName());
			}
		}
		
		return methodNames;
	}
	
	@Override
	final public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
		throw new RuntimeException("Unable to implement policy logic in the method getRepositoryInfos().");
	}

	@Override
	final public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
		throw new RuntimeException("Unable to implement policy logic in the method getRepositoryInfo().");	
	}

	@Override
	public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth, Boolean includePropertyDefinitions, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition createType(String repositoryId, TypeDefinition type, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeDefinition updateType(String repositoryId, TypeDefinition type, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteType(String repositoryId, String typeId, ExtensionsData extension) {
		// TODO Auto-generated method stub

	}

	@Override
	public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, Boolean includeRelativePathSegment, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createDocument(String repositoryId, Properties properties, String folderId, ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createDocumentFromSource(String repositoryId, String sourceId, Properties properties, String folderId, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createRelationship(String repositoryId, Properties properties, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createPolicy(String repositoryId, Properties properties, String folderId, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createItem(String repositoryId, Properties properties, String folderId, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset, BigInteger length, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken, Properties properties, ExtensionsData extension) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<BulkUpdateObjectIdAndChangeToken> bulkUpdateProperties(String repositoryId, List<BulkUpdateObjectIdAndChangeToken> objectIdsAndChangeTokens, Properties properties, List<String> addSecondaryTypeIds, List<String> removeSecondaryTypeIds, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId, ExtensionsData extension) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension) {
		// TODO Auto-generated method stub

	}

	@Override
	public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions, UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag, Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken, ExtensionsData extension) {
		// TODO Auto-generated method stub

	}

	@Override
	public void appendContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken, ContentStream contentStream, boolean isLastChunk, ExtensionsData extension) {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension, Holder<Boolean> contentCopied) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkIn(String repositoryId, Holder<String> objectId, Boolean major, Properties properties, ContentStream contentStream, String checkinComment, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		// TODO Auto-generated method stub

	}

	@Override
	public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId, Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId, Boolean major, String filter, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter, Boolean includeAllowableActions, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions, Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectList getContentChanges(String repositoryId, Holder<String> changeLogToken, Boolean includeProperties, String filter, Boolean includePolicyIds, Boolean includeAcl, BigInteger maxItems, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addObjectToFolder(String repositoryId, String objectId, String folderId, Boolean allVersions, ExtensionsData extension) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeObjectFromFolder(String repositoryId, String objectId, String folderId, ExtensionsData extension) {
		// TODO Auto-generated method stub

	}

	@Override
	public ObjectList getObjectRelationships(String repositoryId, String objectId, Boolean includeSubRelationshipTypes, RelationshipDirection relationshipDirection, String typeId, String filter, Boolean includeAllowableActions, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Acl applyAcl(String repositoryId, String objectId, Acl addAces, Acl removeAces, AclPropagation aclPropagation, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ObjectData> getAppliedPolicies(String repositoryId, String objectId, String filter, ExtensionsData extension) {
		// TODO Auto-generated method stub
		return null;
	}

}
