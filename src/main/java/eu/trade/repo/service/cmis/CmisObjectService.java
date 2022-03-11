package eu.trade.repo.service.cmis;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.BulkUpdateObjectIdAndChangeToken;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FailedToDeleteDataImpl;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.commons.spi.ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.model.ActionParameter;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.Rendition;
import eu.trade.repo.security.ApplyTo;
import eu.trade.repo.security.CustomSecured;
import eu.trade.repo.security.CustomSecured.CustomAction;
import eu.trade.repo.security.Secured;
import eu.trade.repo.security.Security;
import eu.trade.repo.service.cmis.data.in.AclBuilder;
import eu.trade.repo.service.cmis.data.in.CMISObjectBuilder;
import eu.trade.repo.service.cmis.data.out.ObjectDataImpl;
import eu.trade.repo.service.cmis.data.out.PropertiesBuilder;
import eu.trade.repo.service.cmis.data.out.RenditionDataImpl;
import eu.trade.repo.service.cmis.data.util.Utilities;
import eu.trade.repo.service.interfaces.IObjectService;
import eu.trade.repo.service.interfaces.IRelationshipService;

/**
 * CMIS Object Service implementation.
 * <p>
 * Implementation of the CMIS object services that uses the {@link IObjectService} to perform the needed operations.
 * 
 * <p>
 * NOTE: The security restrictions on some methods are not defined in the specification. The choosen implementation is mostly base on Florian Muller answers.
 * See "Re: Services without a clearly defined Permission Mapping filter" @ chemistry-dev mailing list archives: June 2013
 * <p>
 * Permalink: http://mail-archives.apache.org/mod_mbox/chemistry-dev/201306.mbox/%3C20302b313fda4bfd4b93d92a619163ad-EhVcXl9JQQFXRwQFDQkEXR0wfgZLV15fQUBFBEFYXS9aBFgIVQkjAVVfDwkJFE8AXVpYSlJWWAgwXnUGVVdbX1ZIQQ==-webmailer2@server01.webmailer.hosteurope.de%3E
 */
@Transactional
public class CmisObjectService implements ObjectService {
	private static final CmisNotSupportedException NOT_YET = new CmisNotSupportedException("Not yet implemented");

	@Autowired
	private IObjectService objectService;
	public void setObjectService(IObjectService objectService) {	this.objectService = objectService;	}
	
	@Autowired
	private IRelationshipService relationshipService;

	@Autowired
	private Security security;

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#createDocument(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, org.apache.chemistry.opencmis.commons.data.ContentStream, org.apache.chemistry.opencmis.commons.enums.VersioningState, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_CREATE_DOCUMENT)
	public String createDocument(String repositoryId, Properties properties, @ApplyTo(mandatory=false) String folderId, ContentStream contentStream,
			VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		CMISObject cmisObject = CMISObjectBuilder.build(properties, folderId);
		return createObject(repositoryId, cmisObject, addAces, removeAces, contentStream, versioningState, BaseTypeId.CMIS_DOCUMENT); //pass base-type to createobject
	}

	/**
	 * This method requires some kind of CustomSecured annotation.
	 * 
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#createDocumentFromSource(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, org.apache.chemistry.opencmis.commons.enums.VersioningState, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@CustomSecured(CustomAction.CREATE_DOCUMENT_FROM_SOURCE)
	public String createDocumentFromSource(String repositoryId, @ApplyTo(ActionParameter.OBJECT) String sourceId, Properties properties, @ApplyTo(ActionParameter.FOLDER) String folderId,
			VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {

		CMISObject source = objectService.getObject(repositoryId, sourceId);
		ContentStream sourceStream = objectService.getContentStream(repositoryId, sourceId);

		CMISObject clone = null;
		try {
			clone = source.clone();
		} catch (CloneNotSupportedException e) {
			throw new CmisInvalidArgumentException("Failed to clone: " + source, e);
		}
		clone.updateProperties(CMISObjectBuilder.buildProperties(properties));

		// add the new folderId as a parent
		if (folderId != null && !folderId.isEmpty()) {
			CMISObject parent = new CMISObject();
			parent.setCmisObjectId(folderId);
			clone.addParent(parent);
		}

		return createObject(repositoryId, clone, addAces, removeAces, sourceStream, versioningState, BaseTypeId.CMIS_DOCUMENT);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#createFolder(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_CREATE_FOLDER)
	public String createFolder(String repositoryId, Properties properties, @ApplyTo String folderId, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		CMISObject cmisObject = CMISObjectBuilder.build(properties, folderId);
		return createObject(repositoryId, cmisObject, addAces, removeAces, null, null, BaseTypeId.CMIS_FOLDER);
	}

	/**
	 * Source and target are extracted from the properties object in order to use the regular secured annotation.
	 * <p>
	 * Currently both properties are treated as mandatory. In CMIS 1.1 the source and the target seems to be optional.
	 * 
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#createRelationship(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public final String createRelationship(String repositoryId, Properties properties, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		String sourceId = Utilities.getProperty(properties, PropertyIds.SOURCE_ID, String.class);
		String targetId = Utilities.getProperty(properties, PropertyIds.TARGET_ID, String.class);
		return createRelationship(repositoryId, sourceId, targetId, properties, policies, addAces, removeAces, extension);
	}

	/**
	 * Secured public method for create a relationship.
	 * 
	 * @param repositoryId
	 * @param sourceId {@link String} The source id extracted from the properties.
	 * @param targetId {@link String} The target id extracted from the properties.
	 * @param properties
	 * @param policies
	 * @param addAces
	 * @param removeAces
	 * @param extension
	 * @return {@link String} The new object id.
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#createRelationship(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Secured(Action.CAN_CREATE_RELATIONSHIP)
	public String createRelationship(String repositoryId, @ApplyTo(ActionParameter.SOURCE) String sourceId, @ApplyTo(ActionParameter.TARGET) String targetId, Properties properties, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		
		//Add sourceId, targetId to properties if not there?
		CMISObject cmisObject = CMISObjectBuilder.build(properties, null);
		
		Set<String> permissionNames = security.getPermissionNames(repositoryId);
		String currentUsername = security.getCallContextHolder().getUsername();
		Set<eu.trade.repo.model.Acl> aclToAdd = AclBuilder.build(addAces, permissionNames, currentUsername);
		Set<eu.trade.repo.model.Acl> aclToRemove = AclBuilder.build(removeAces, permissionNames, currentUsername);
		
		return objectService.createRelationship(repositoryId, cmisObject, aclToAdd, aclToRemove, policies).getCmisObjectId();
	}

	/**
	 * Until canCreatePolicy has been finally defined in CMIS 1.1 this method will be restricted according to canCreateDocument permission mappings.
	 * 
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#createPolicy(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_CREATE_DOCUMENT)
	public String createPolicy(String repositoryId, Properties properties, @ApplyTo(mandatory=false) String folderId, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		CMISObject cmisObject = CMISObjectBuilder.build(properties, folderId);
		return createObject(repositoryId, cmisObject, addAces, removeAces, null, null, BaseTypeId.CMIS_POLICY);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#getAllowableActions(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_GET_PROPERTIES)
	public AllowableActions getAllowableActions(String repositoryId, @ApplyTo String objectId, ExtensionsData extension) {
		return security.getAllowableActions(objectId);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#getObject(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_GET_PROPERTIES)
	public ObjectData getObject(String repositoryId, @ApplyTo String objectId, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
			String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
		return getObjectByPathOrId(repositoryId, objectId, includeAllowableActions, includeRelationships, includePolicyIds, includeAcl, filter);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#getProperties(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_GET_PROPERTIES)
	public Properties getProperties(String repositoryId, @ApplyTo String objectId, String filter, ExtensionsData extension) {
		CMISObject cmisObject = objectService.getObject(repositoryId, objectId);
		return PropertiesBuilder.build(cmisObject, filter);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#getRenditions(java.lang.String, java.lang.String, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_GET_PROPERTIES)
	public List<RenditionData> getRenditions(String repositoryId, @ApplyTo String objectId, String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		List<RenditionData> ans = new ArrayList<RenditionData>();
		for (Rendition rendition : objectService.getRenditions(repositoryId, objectId, maxItems.intValue(), skipCount.intValue())) {
			ans.add(new RenditionDataImpl(rendition));
		}
		return ans;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#getObjectByPath(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_GET_PROPERTIES)
	public ObjectData getObjectByPath(String repositoryId, @ApplyTo String path, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
			String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
		return getObjectByPathOrId(repositoryId, path, includeAllowableActions, includeRelationships, includePolicyIds, includeAcl, filter);
	}

	private ObjectData getObjectByPathOrId(String repositoryId, String pathOrId, Boolean includeAllowableActions, IncludeRelationships includeRelationships, Boolean includePolicyIds, Boolean includeAcl, String filter) {
		CMISObject cmisObject = objectService.getObject(repositoryId, pathOrId);
		AllowableActions allowableActions = null;
		List<CMISObject> relationships = null;
		if (Boolean.TRUE.equals(includeAllowableActions)) {
			allowableActions = security.getAllowableActions(cmisObject);
		}
		
		Set<CMISObject> unorderedRels = relationshipService.getObjectRelationships(repositoryId, cmisObject.getCmisObjectId(), includeRelationships).getPageElements();
		 
		if(unorderedRels != null) {
			relationships = new ArrayList<>(unorderedRels);
		}
				 			
		return new ObjectDataImpl(cmisObject, includePolicyIds, includeAcl, allowableActions, relationships, filter);
	}
	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#getContentStream(java.lang.String, java.lang.String, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_GET_CONTENT_STREAM)
	public ContentStream getContentStream(String repositoryId, @ApplyTo String objectId, String streamId, BigInteger offset, BigInteger length, ExtensionsData extension) {
		return objectService.getContentStream(repositoryId, objectId);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#updateProperties(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.Properties, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_UPDATE_PROPERTIES)
	public void updateProperties(String repositoryId, @ApplyTo Holder<String> objectId, Holder<String> changeToken, Properties properties, ExtensionsData extension) {
		verifyChangeToken(repositoryId, objectId, changeToken, false);
		String newChangeToken = objectService.updateProperties(repositoryId, objectId.getValue(), properties);
		changeToken.setValue(newChangeToken);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#moveObject(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_MOVE_OBJECT)
	public void moveObject(String repositoryId, @ApplyTo(ActionParameter.OBJECT) Holder<String> objectId,  @ApplyTo(ActionParameter.TARGET) String targetFolderId,
			@ApplyTo(ActionParameter.SOURCE) String sourceFolderId, ExtensionsData extension) {
		objectService.moveObject(repositoryId, objectId.getValue(), sourceFolderId, targetFolderId);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#deleteObject(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_DELETE_OBJECT)
	public void deleteObject(String repositoryId, @ApplyTo String objectId, Boolean allVersions, ExtensionsData extension) {
		objectService.deleteObject(repositoryId, objectId, allVersions);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#deleteTree(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.UnfileObject, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_DELETE_TREE)
	public FailedToDeleteData deleteTree(String repositoryId, @ApplyTo String folderId, Boolean allVersions, UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {
		FailedToDeleteDataImpl ans = new FailedToDeleteDataImpl();
		ans.setIds(objectService.deleteTree(repositoryId, folderId, allVersions, unfileObjects, continueOnFailure));
		return ans;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#setContentStream(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.Boolean, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.ContentStream, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_SET_CONTENT_STREAM)
	public void setContentStream(String repositoryId, @ApplyTo Holder<String> objectId, Boolean overwriteFlag, Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {
		verifyChangeToken(repositoryId, objectId, changeToken, false);
		String newChangeToken = objectService.setContentStream(repositoryId, objectId.getValue(), overwriteFlag, contentStream);
		changeToken.setValue(newChangeToken);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#deleteContentStream(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_DELETE_CONTENT_STREAM)
	public void deleteContentStream(String repositoryId, @ApplyTo Holder<String> objectId, Holder<String> changeToken, ExtensionsData extension) {
		verifyChangeToken(repositoryId, objectId,  changeToken, false);
		String newChangeToken = objectService.deleteContentStream(repositoryId, objectId.getValue());
		changeToken.setValue(newChangeToken);
	}

	/**
	 * CMIS 1.1 specification does not include canCreateItem action, 
	 * contrary the XML schema includes this action.
	 * 
	 * Temporarily used canCreateDocument action until situation is clarified.
	 * 
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#createItem(java.lang.String, org.apache.chemistry.opencmis.commons.data.Properties, java.lang.String, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_CREATE_DOCUMENT)
	public String createItem(String repositoryId, Properties properties, @ApplyTo(mandatory=false) String folderId, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		CMISObject cmisObject = CMISObjectBuilder.build(properties, folderId);
		return createObject(repositoryId, cmisObject, addAces, removeAces, null, null, BaseTypeId.CMIS_ITEM);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#bulkUpdateProperties(java.lang.String, java.util.List, org.apache.chemistry.opencmis.commons.data.Properties, java.util.List, java.util.List, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public List<BulkUpdateObjectIdAndChangeToken> bulkUpdateProperties(String repositoryId, List<BulkUpdateObjectIdAndChangeToken> objectIdsAndChangeTokens,
			Properties properties, List<String> addSecondaryTypeIds, List<String> removeSecondaryTypeIds, ExtensionsData extension) {
		throw NOT_YET;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.ObjectService#appendContentStream(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.ContentStream, boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public void appendContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken, ContentStream contentStream, boolean isLastChunk, ExtensionsData extension) {
		throw NOT_YET;
	}

	//PRIVATE
	/**
	 * 
	 * @param repositoryId
	 * @param cmisObject
	 * @param addAces
	 * @param removeAces
	 * @param contentStream
	 * @param versioningState
	 * @return {@link String} The created object's cmis id.
	 */
	private String createObject(String repositoryId, CMISObject cmisObject, Acl addAces, Acl removeAces, ContentStream contentStream, VersioningState versioningState, BaseTypeId baseTypeId) {
		
		Set<String> permissionNames = security.getPermissionNames(repositoryId);
		String currentUsername = security.getCallContextHolder().getUsername();
		Set<eu.trade.repo.model.Acl> aclToAdd = AclBuilder.build(addAces, permissionNames, currentUsername);
		Set<eu.trade.repo.model.Acl> aclToRemove = AclBuilder.build(removeAces, permissionNames, currentUsername);

		return objectService.createObject(repositoryId, cmisObject, aclToAdd, aclToRemove, contentStream, versioningState != null ? versioningState : VersioningState.NONE, baseTypeId).getCmisObjectId();
	}

	private void verifyChangeToken(String repositoryId, Holder<String> objectId, Holder<String> token, boolean relaxedConstraint) {
		if (objectId == null || objectId.getValue().length() <= 0) {
			throw new CmisUpdateConflictException("Cannot verify change token, null or empty object ID");
		}

		if (relaxedConstraint && token == null) {
			return;
		}
		if (token != null && token.getValue().length() > 0) {
			String changeTokenDB = objectService.getObject(repositoryId, objectId.getValue()).getPropertyValue(PropertyIds.CHANGE_TOKEN);
			if (changeTokenDB.equals(token.getValue())) {
				return;
			}
		}
		throw new CmisUpdateConflictException("Change Tokens do not match. Cannot modify CmisObject with id:"+objectId.getValue());
	}
}
