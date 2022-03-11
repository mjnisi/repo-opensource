package eu.trade.repo.service;

import static eu.trade.repo.util.Constants.CMIS_PATH_SEP;
import static eu.trade.repo.util.Constants.DEFAULT_VERSION_LABEL;
import static eu.trade.repo.util.Constants.MODE_CHECKOUT_NEW;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.changelog.ChangeLog;
import eu.trade.repo.index.triggers.annotation.TriggerIndex;
import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.ObjectTypeRelationship;
import eu.trade.repo.model.ObjectTypeRelationship.RelationshipType;
import eu.trade.repo.model.Property;
import eu.trade.repo.model.Rendition;
import eu.trade.repo.model.Repository;
import eu.trade.repo.model.exception.PropertyNotFoundException;
import eu.trade.repo.model.view.ObjectParent;
import eu.trade.repo.selectors.ObjectParentSelector;
import eu.trade.repo.selectors.ObjectTypeSelector;
import eu.trade.repo.service.cmis.data.util.Utilities;
import eu.trade.repo.service.interfaces.IObjectService;
import eu.trade.repo.service.util.Node;
import eu.trade.repo.service.util.Tree;
import eu.trade.repo.util.PropertiesMap;

/**
 * 
 * @author azaridi, porrjai, abascis, kardaal
 *
 */
public class ObjectService extends CMISBaseObjectService implements IObjectService {

	private static final Logger LOG = LoggerFactory.getLogger(ObjectService.class);

	@Autowired
	private ObjectTypeSelector objTypeSelector;

	@Autowired
	private ObjectParentSelector objectParentSelector;

	@Autowired
	private ChangeLog changeLog;
	
	@TriggerIndex
	@Override
	public CMISObject createRelationship(String repositoryId, CMISObject object, Set<Acl> aclToAdd, Set<Acl> aclToRemove, List<String> policies) {
		
		ObjectType objType = object.getObjectType();

		//ObjectType cannot be null
		if(objType == null) {
			//since CMIS 1.1 the objectType is not required, if empty, using the default
			objType = new ObjectType(BaseTypeId.CMIS_RELATIONSHIP.value());
		}

		String objTypeCmisId = objType.getCmisId();
		//ObjectType must exist
		try {
			objType = objTypeSelector.getObjectTypeByCmisId(repositoryId, objTypeCmisId);
		} catch (Exception x) {
			throw new CmisInvalidArgumentException("Cannot create object, unknown Object Type : " + objTypeCmisId, x);
		}
		
		validateAclsApplicable(aclToAdd, aclToRemove, objType, objTypeCmisId);
		
		validateIsRelationshipType(objType, objTypeCmisId);
		
		String sourceId = Utilities.getPropertyTypedValue(object, PropertyIds.SOURCE_ID);
		String targetId = Utilities.getPropertyTypedValue(object, PropertyIds.TARGET_ID);
		
		//NOTE: the getObject(...) selector will throw a CmisObjectNotFoundException if the objects don't exist
		//TODO: catch and rethrow ...CmisConstraintException
		CMISObject sourceObject = getObject(repositoryId, sourceId);
		CMISObject targetObject = getObject(repositoryId, targetId);
		
		validateSrcAndTargetTypes(sourceId, sourceObject, targetObject);
		
		validateSourceAndTargetTypesInAllowedLists(objType, sourceObject, targetObject);
		
		return createObject(repositoryId, object, aclToAdd, aclToRemove, null, VersioningState.NONE, BaseTypeId.CMIS_RELATIONSHIP);
	}

	private void validateSourceAndTargetTypesInAllowedLists(ObjectType objType, CMISObject sourceObject, CMISObject targetObject) {
		
		Iterator<ObjectTypeRelationship> objectTypeRelationships = objType.getObjectTypeRelationships().iterator();
		
		boolean sourceTypeOK = false;
		boolean targetTypeOK = false;
		
		StringBuilder allowedSourceTypes = new StringBuilder();
		StringBuilder allowedTargetTypes = new StringBuilder();
		
		while(objectTypeRelationships.hasNext()) {
			ObjectTypeRelationship otr = objectTypeRelationships.next();
			if(RelationshipType.SOURCE.equals(otr.getType())) {
				allowedSourceTypes.append(" ").append(otr.getReferencedObjectType().getCmisId());
				if(sourceObject.getObjectType().getCmisId().equals(otr.getReferencedObjectType().getCmisId())) {
					sourceTypeOK = true;
				}
			} else if(RelationshipType.TARGET.equals(otr.getType())) {
				allowedTargetTypes.append(" ").append(otr.getReferencedObjectType().getCmisId());
				if(targetObject.getObjectType().getCmisId().equals(otr.getReferencedObjectType().getCmisId())) {
					targetTypeOK = true;
				}
			}
		}
		
		if(!sourceTypeOK && allowedSourceTypes.length() != 0) {
			throw new CmisConstraintException(String.format("The source Object-Type %s is not in the allowed source types:%s", sourceObject.getObjectType().getCmisId(), allowedSourceTypes));
		}
		if(!targetTypeOK && allowedTargetTypes.length() != 0) {
			throw new CmisConstraintException(String.format("The target Object-Type %s is not in the allowed target types:%s", sourceObject.getObjectType().getCmisId(), allowedTargetTypes));
		}
	}

	private void validateIsRelationshipType(ObjectType objType, String objTypeCmisId) {
		if(!BaseTypeId.CMIS_RELATIONSHIP.value().equalsIgnoreCase(objType.getBase().getCmisId())) {
			throw new CmisConstraintException(String.format("The specified ObjectType: %s does not extend %s", objTypeCmisId, BaseTypeId.CMIS_RELATIONSHIP));
		}
	}

	private void validateSrcAndTargetTypes(String sourceId, CMISObject sourceObject, CMISObject targetObject) {
		if(BaseTypeId.CMIS_RELATIONSHIP.value().equals(sourceObject.getObjectType().getBase().getCmisId())) {
			throw new CmisConstraintException(String.format("Source object: %s cannot extend type %s", sourceId, BaseTypeId.CMIS_RELATIONSHIP));
		}
		
		if(BaseTypeId.CMIS_RELATIONSHIP.value().equals(targetObject.getObjectType().getBase().getCmisId())) {
			throw new CmisConstraintException(String.format("Target object: %s cannot extend type %s", sourceId, BaseTypeId.CMIS_RELATIONSHIP));
		}
	}

	private void validateAclsApplicable(Set<Acl> aclToAdd, Set<Acl> aclToRemove, ObjectType objType, String objTypeCmisId) {
		
		if(!objType.isControllableAcl() && (isCollectionNotEmpty(aclToAdd) || isCollectionNotEmpty(aclToRemove))) {
			throw new CmisConstraintException(String.format("ACL's cannot be applied to objects of ObjectType: %s", objTypeCmisId));
		}
	}
	
	private boolean isCollectionNotEmpty(Collection collection) {
		return collection != null && !collection.isEmpty();
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IObjectService#createObject(java.lang.String, eu.trade.repo.model.CMISObject, java.util.Set, java.util.Set, org.apache.chemistry.opencmis.commons.data.ContentStream, org.apache.chemistry.opencmis.commons.enums.VersioningState)
	 */
	@TriggerIndex
	@Override
	public CMISObject createObject(String repositoryId, 
			CMISObject cmisObject, Set<Acl> aclToAdd, Set<Acl> aclToRemove, 
			ContentStream stream, VersioningState versioningState, BaseTypeId baseTypeId) {

		validateAndSetObjectType(repositoryId, cmisObject, baseTypeId);

		if (stream != null) {
			validateUpdateStreamCapability(repositoryId);
		}

		//get the object parent, if any, needed to validate the object
		CMISObject parent = null;
		Set<CMISObject> parents = cmisObject.getParents();
		if (!parents.isEmpty()) {
			// Load the parent from database using the cmisObjectId.
			// Also remove the fake object's parent in the set. In other case it would prevent to add the actual one. See below the parent.addChild(cmisObject) statement.
			// Note: The size of the set in this case is always 1 at start and 0 at the end of this block.
			parent = parents.iterator().next();
			cmisObject.removeParent(parent);
			parent = getObjSelector().getCMISObject(repositoryId, parent.getCmisObjectId());
		}

		// validate the object.
		// Note that the cmisObject.getParents() is always empty at this point, and that parent.getChildren() must not contain yet the new object (in order to validate the siblings)
		validateNewObject(repositoryId, cmisObject, parent);

		//populate new Object (parent, properties, acls)
		cmisObject.setCmisObjectId(getGenerator().next());
		if (parent != null) {
			// will also do the reverse move cmisObject.addParent(parent)
			parent.addChild(cmisObject);
		}

		populateGeneratedProperties(cmisObject, parent, versioningState);
		if (VersioningState.CHECKEDOUT.equals(versioningState)) {

			String currentUser = getSecurity().getCallContextHolder().getUsername();
			Map<String, SortedSet<ObjectTypeProperty>> propertyTypeMap = null;

			updateCloneProperties(cmisObject, currentUser, false, MODE_CHECKOUT_NEW, null, null);

			try {
				cmisObject.getProperty(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY).setTypedValue(currentUser);
				cmisObject.getProperty(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID).setTypedValue(cmisObject.getCmisObjectId());
			} catch (PropertyNotFoundException e) {
				// THESE ARE UNSET AT CHECK IN OR FIRST TIME CHECKOUT
				propertyTypeMap = cmisObject.getObjectType().getObjectTypePropertiesIncludeParents(true, true);
				ObjectTypeProperty vsico = propertyTypeMap.get(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY).first();
				Property versionSeriesIdCOBy = new Property(vsico, currentUser);
				cmisObject.addProperty(versionSeriesIdCOBy);

				propertyTypeMap = cmisObject.getObjectType().getObjectTypePropertiesIncludeParents(true, true);
				ObjectTypeProperty vsicoId = propertyTypeMap.get(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID).first();
				Property versionSeriesIdCOId = new Property(vsicoId,cmisObject.getCmisObjectId());
				cmisObject.addProperty(versionSeriesIdCOId);
			}
			updateCloneProperties(cmisObject, getSecurity().getCallContextHolder().getUsername(), false, MODE_CHECKOUT_NEW, null, null);
		}

		populateObjectTypeProperties(cmisObject);
		getSecurity().getAccessControl().create(repositoryId, cmisObject, parent, aclToAdd, aclToRemove, versioningState);

		//persist object and respective stream
		persist(cmisObject);
		createObjectStream(repositoryId, cmisObject, stream);
		//TODO: remove flush
		flush();

		changeLog.create(repositoryId, cmisObject.getCmisObjectId());
		if (parent != null) {
			changeLog.update(repositoryId, parent.getCmisObjectId());
		}
		return cmisObject;
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IObjectService#getObject(java.lang.String, java.lang.String)
	 */
	@Override
	public CMISObject getObject(String repositoryId, String pathOrId) {
		// Since the object is already loaded with the ACLs due to the Authorization, just retrieve it from cache
		return getObjSelector().getCMISObject(repositoryId, pathOrId);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IObjectService#getRenditions(java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public List<Rendition> getRenditions(
			String repositoryId,
			//String renditionFilter,
			String objectId,
			int maxItems,
			int skipCount){

		int skip = skipCount;
		CMISObject obj = getObjSelector().getCMISObject(repositoryId, objectId);
		List<Rendition> renditions = obj.getRenditions();
		if (skip <= 0 && maxItems >= renditions.size()) {
			return renditions;
		}

		List<Rendition> ans = new ArrayList<Rendition>();
		for (int i = 0; i < renditions.size(); i++) {
			if (skip <= 0) {
				ans.add(renditions.get(i));
				if (renditions.size() == maxItems) {
					return ans;
				}
			} else {
				skip--;
			}
		}
		return ans;
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IObjectService#deleteObject(java.lang.String, java.lang.String, boolean)
	 */
	@TriggerIndex
	@Override
	public void deleteObject(String repositoryId, String objectId, boolean allVersions) {
		String rootFolderId = getObjSelector().getRootFolderId(repositoryId);
		if(rootFolderId.equals(objectId)) {
			throw new CmisPermissionDeniedException("Cannot delete the root folder.");
		}
		CMISObject cmisObject = getObjSelector().getCMISObject(repositoryId, objectId);
		if (cmisObject.isPolicy()) {
			if (cmisObject.getObjectsUnderPolicy().size() > 0) {
				throw new CmisNotSupportedException("Cannot delete a policy with objects, :" + objectId);
			}
			remove(cmisObject);
			return;
		}

		if (cmisObject.hasChildren()) {
			throw new CmisNotSupportedException("Cannot delete an object with children :" + objectId);
		}
		// Note: access to getParents().isEmpty() to be sure the parents are loaded before deleting the entity.
		boolean updateParents = cmisObject.isDocument() && !cmisObject.getParents().isEmpty();
		deleteObject(repositoryId, cmisObject, allVersions);
		changeLog.delete(repositoryId, objectId);
		if (updateParents) {
			for (CMISObject parent : cmisObject.getParents()) {
				changeLog.update(repositoryId, parent.getCmisObjectId());
			}
		}
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IObjectService#updateProperties(java.lang.String, eu.trade.repo.model.CMISObject)
	 */
	@TriggerIndex
	@Override
	public String updateProperties(String repositoryId, String cmisObjectId, Properties properties) {
		CMISObject cmisObject = getObjSelector().getCMISObject(repositoryId, cmisObjectId);
		changeLog.update(repositoryId, cmisObjectId);
		return updateProperties(repositoryId, cmisObject, properties);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IObjectService#moveObject(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@TriggerIndex
	@Override
	public void moveObject(String repositoryId, String objectId, String sourceFolderId, String targetFolderId) {
		//object , source and target must exist .. if not found , a ResultNotFoundException will be thrown
		CMISObject cmisObject = getObjSelector().loadCMISObject(objectId);
		CMISObject source = getObjSelector().getCMISObjectWithChildrenAndProperties(sourceFolderId);
		CMISObject target = getObjSelector().getCMISObject(repositoryId, targetFolderId);

		validateMovedObject(objectId, sourceFolderId, cmisObject, target);

		Repository repository = getRepositorySelector().getRepository(repositoryId);
		if (fileAllVersionTogether(repository, cmisObject)) {
			moveVersionSeries(repositoryId, cmisObject, source, target);
		}
		else {
			validateSiblings(repositoryId, cmisObject, target);
			moveSingleObject(repositoryId, cmisObject, source, target);
		}

		//if obj is a folder, update all subfolder paths
		if (cmisObject.isFolder()) {
			try {
				Property p = target.getProperty(PropertyIds.PATH);
				String objectPath = cmisObject.getPropertyValue(PropertyIds.PATH);
				String targetPath = p.getTypedValue();
				updatePathTree(p.getObjectTypeProperty().getId(), targetPath, cmisObject.getPathSegment(), objectPath);
				//update the property cmis:parentId
				cmisObject.getProperty(PropertyIds.PARENT_ID).setTypedValue(targetFolderId);
			} catch (PropertyNotFoundException ignored) {
				//already checked in validateMovedObject
			}
		}

		changeLog.update(repositoryId, sourceFolderId);
		changeLog.update(repositoryId, targetFolderId);
	}

	/**
	 * Move a single object. Do not perform any validation.
	 * 
	 * @param repositoryId
	 * @param cmisObject
	 * @param source
	 * @param target
	 */
	private void moveSingleObject(String repositoryId, CMISObject cmisObject, CMISObject source, CMISObject target) {
		validateParentFolder(cmisObject, source);

		//add to new parent
		target.addChild(cmisObject);
		//remove from old parent
		source.removeChild(cmisObject);
		//move the acls
		getSecurity().getAccessControl().move(repositoryId, cmisObject, source, target);
	}

	/**
	 * Move the whole version series from the source to the target.
	 * <p>
	 * Validates the version can be moved and the version siblings.
	 * 
	 * @param repositoryId
	 * @param cmisObject
	 * @param source
	 * @param target
	 */
	private void moveVersionSeries(String repositoryId, CMISObject cmisObject, CMISObject source, CMISObject target) {
		String versionSeriesId = getVersionSeriesId(cmisObject);
		List<CMISObject> versions = getAllVersions(repositoryId, null, versionSeriesId);

		validateMovingVersionSeries(repositoryId, versionSeriesId, versions, target);
		alignVersionSeries(versions, source);

		for (CMISObject version : versions) {
			moveSingleObject(repositoryId, version, source, target);
		}
	}

	/**
	 * Validates all the version series can be moved. Also checks the siblings names in the target folder.
	 * 
	 * @param repositoryId
	 * @param versionSeriesId
	 * @param versions
	 * @param target
	 */
	private void validateMovingVersionSeries(String repositoryId, String versionSeriesId, List<CMISObject> versions, CMISObject target) {
		// Note: In order to validate the siblings a lock is set for each path to be validated, so it has to be done in an ordered way to avoid any possible deadlock.
		// In addition, only one validation is needed for any subset of version's objects with the same name. So a TreeMap does the trick
		Map<String, CMISObject> orderedPathSegments = new TreeMap<>();
		for (CMISObject version : versions) {
			// can be moved
			if (!getSecurity().isAllowableAction(version, Action.CAN_MOVE_OBJECT)) {
				throw new CmisPermissionDeniedException("Cannot move all the objects in the version series: " + versionSeriesId);
			}
			// put path segment ordered
			orderedPathSegments.put(version.getPathSegment(), version);
		}
		// This iteration is done ordered by pathSegment.
		for (CMISObject orderedVersion : orderedPathSegments.values()) {
			validateSiblings(repositoryId, orderedVersion, target);
		}
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IObjectService#deleteTree(java.lang.String, java.lang.String, boolean, org.apache.chemistry.opencmis.commons.enums.UnfileObject, boolean)
	 */
	@TriggerIndex
	@Override
	public List<String> deleteTree(String repositoryId, String folderId, boolean allVersions, UnfileObject unfileObjects, boolean continueOnFailure) {
		// Get the descendants tree without filtering the permissions
		Tree tree = getTree(repositoryId, folderId);
		List<String> ans = deleteTree(tree, repositoryId, allVersions, unfileObjects, continueOnFailure);
		changeLog.delete(repositoryId, folderId);
		return ans;

	}

	/**
	 * @see eu.trade.repo.service.interfaces.IObjectService#getContentStream(java.lang.String)
	 */
	@TriggerIndex
	@Override
	public ContentStream getContentStream( String repositoryId, String cmisObjectId ) {
		ContentStreamImpl cs = new ContentStreamImpl();

		CMISObject object = getObjSelector().getCMISObject(repositoryId, cmisObjectId);
		boolean hasStream = getObjSelector().hasStream(object.getId());
		if( !hasStream ){
			throw new CmisConstraintException("This object does not have an associated stream");
		}
		InputStream in = getJdbcDelegate().getStream(object.getId());
		cs.setStream(in);

		List<String> propTypeCmisIdList = ObjectTypeProperty.getStreamPropTypeCmisIdList();

		List<Property> propList = getPropSelector().getPropertiesOfTypes(object.getId(), propTypeCmisIdList);
		String propType = null;
		for (Property property : propList) {
			propType = property.getObjectTypeProperty().getCmisId();
			if( PropertyIds.CONTENT_STREAM_FILE_NAME.equals(propType) ){
				cs.setFileName((String)property.getTypedValue());
			}else if( PropertyIds.CONTENT_STREAM_MIME_TYPE.equals(propType)){
				cs.setMimeType((String)property.getTypedValue());
			}else if( PropertyIds.CONTENT_STREAM_LENGTH.equals(propType)){
				cs.setLength(property.<BigInteger>getTypedValue());
			}
		}
		return cs;
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IObjectService#setContentStream(String, String, boolean, ContentStream)
	 */
	@TriggerIndex
	@Override
	public String setContentStream(String repositoryId, String cmisObjectId, boolean overwriteFlag, ContentStream contentStream) {

		validateUpdateStreamCapability(repositoryId);
		CMISObject cmisObject = getObjSelector().getCMISObject(repositoryId, cmisObjectId);

		PropertiesMap<Object> streamProperties = setContentStream(repositoryId, cmisObject, overwriteFlag, contentStream);
		String newChangeToken = (String) streamProperties.first(PropertyIds.CHANGE_TOKEN);

		changeLog.update(repositoryId, cmisObjectId);
		return newChangeToken;
	}

	/**
	 * This service is considered modification to a content-stream's containing document object, and therefore change the object's LastModificationDate property
	 * upon successful completion.
	 */
	@TriggerIndex
	@Override
	public String deleteContentStream( String repositoryId, String cmisObjectId ) {
		validateUpdateStreamCapability(repositoryId);
		CMISObject object = getObjSelector().getCMISObject(repositoryId, cmisObjectId);

		if( ContentStreamAllowed.REQUIRED.equals(object.getObjectType().getContentStreamAllowed()) ){
			throw new CmisConstraintException("The stream is required for this objectType");
		}
		getJdbcDelegate().deleteStream(object.getId());

		//remove stream properties
		List<String> propTypeCmisIdList = ObjectTypeProperty.getStreamPropTypeCmisIdList();
		List<Property> propList = getPropSelector().getPropertiesOfTypes(object.getId(), propTypeCmisIdList);
		for (Property property : propList) {
			object.removeProperty(property);
		}

		String newChangeToken = setObjectUpdateProperties(object);
		merge(object);

		return newChangeToken;
	}

	@Override
	public List<?> getObjectsCountForRepository(String repoId) {
		return getObjSelector().getObjectsCountForRepository(repoId);
	}

	@Override
	public List<?> getObjectCountPerRepository() {
		return getObjSelector().getAllObjectsCountPerRepo();
	}

	@Override
	public List<?> getObjectsCountWithContentIndexStatus(String repoId) {
		return getObjSelector().getObjectCountWithContentIndexingStatus(repoId);
	}

	@Override
	public long getNumberOfObjectsWaitingForContentIndexing(String repoId) {
		return getObjSelector().getNumberOfObjectsWaitingForContentIndexing(repoId);
	}

	@Override
	public List<?> getObjectsCountWithMetadataIndexStatus(String repoId) {
		return getObjSelector().getObjectCountWithMetadataIndexingStatus(repoId);
	}

	//PRIVATE METHODS

	private void deleteObject(String repositoryId, CMISObject cmisObject, boolean allVersion) {
		if (allVersion) {
			String versionSeriesId = cmisObject.getPropertyValue(PropertyIds.VERSION_SERIES_ID);
			if (versionSeriesId == null) {
				// This would happen on non-versionable objects (v.g. folders).
				deleteObject(repositoryId, cmisObject);
			}
			else {
				List<CMISObject> versions = getAllVersions(repositoryId, null, versionSeriesId);
				if (cannotDeleteAllVersions(versions)) {
					// In order to delete all versions you have to be able to delete every single version. In other case, no object would be deleted at all.
					throw new CmisPermissionDeniedException("Cannot delete all the objects in the version series: " + versionSeriesId);
				}

				for (CMISObject version : getAllVersions(repositoryId, null, versionSeriesId)) {
					deleteObject(repositoryId, version);
				}
			}
		} else {
			deleteSingleObject(repositoryId, cmisObject);
		}
	}

	private void deleteSingleObject(String repositoryId, CMISObject cmisObject) {
		deleteObject(repositoryId, cmisObject);
		flush();
		try {
			updateLatestVersionFlags(repositoryId, cmisObject);
		} catch (PropertyNotFoundException ignored) {
		}
	}

	private void updateLatestVersionFlags(String repositoryId, CMISObject deleted) throws PropertyNotFoundException {
		
		if(!shouldUpdateVersionSeriesFlags(deleted)) {
			return;
		}

		String versionSeriesId = deleted.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue();
		List<CMISObject> versions = getAllVersions(repositoryId, deleted.getCmisObjectId(), versionSeriesId);
		if (versions.size() == 0) {
			return;
		}

		//only one left in the series, its def. the latest and the latest major if its a major version
		if (versions.size() == 1) {
			CMISObject version = versions.get(0);
			version.getProperty(PropertyIds.IS_LATEST_VERSION).setTypedValue(true);
			if (version.getProperty(PropertyIds.IS_MAJOR_VERSION).getTypedValue()) {
				version.getProperty(PropertyIds.IS_LATEST_MAJOR_VERSION).setTypedValue(true);
			}
			merge(version);
			return;
		}

		CMISObject latest = null;
		CMISObject latestMajor = null;
		
		Map<String, CMISObject> latestAndLatestMajor = getLatestAndLatestMajorVersion(versions);
		latest = latestAndLatestMajor.get("latest");
		latestMajor = latestAndLatestMajor.get("latestMajor");
		
		if (latest == null) {
			throw new CmisInvalidArgumentException("Cannot update Latest Version Flags "+deleted.getCmisObjectId());
		}

		latest.getProperty(PropertyIds.IS_LATEST_VERSION).setTypedValue(true);
		if (latestMajor != null) {
			latestMajor.getProperty(PropertyIds.IS_LATEST_MAJOR_VERSION).setTypedValue(true);
		}

		merge(latest);
		if (latestMajor != null && !latestMajor.equals(latest)) {
			merge(latestMajor);
		}
	}
	
	private Map<String, CMISObject> getLatestAndLatestMajorVersion(List<CMISObject> versions) throws PropertyNotFoundException {
		
		CMISObject latest = null;
		CMISObject latestMajor = null;
		
		long l = -1, lm = -1;
		
		for (CMISObject version : versions) {
            Calendar created = version.getProperty(PropertyIds.CREATION_DATE).getTypedValue();
            if (created.getTimeInMillis() > l) {
                latest = version;
                l = created.getTimeInMillis();
            }

            if ((boolean)version.getProperty(PropertyIds.IS_MAJOR_VERSION).getTypedValue() && created.getTimeInMillis() > lm) {
            	latestMajor = version;
            	lm = created.getTimeInMillis();
            }
        }

		Map<String, CMISObject> latestAndLatestMajor = new HashMap<>();
		latestAndLatestMajor.put("latest", latest);
		latestAndLatestMajor.put("latestMajor", latestMajor);
		
		return latestAndLatestMajor;
	}

	/**
	 * Checks whether the latest major or the latest version was deleted.
	 * 
	 * @param deleted the object we have deleted
	 * @return
	 * @throws PropertyNotFoundException
	 */
	private boolean shouldUpdateVersionSeriesFlags(CMISObject deleted) throws PropertyNotFoundException {
		
		boolean latestVersionDeleted = deleted.getProperty(PropertyIds.IS_LATEST_VERSION).getTypedValue();
		boolean latestMajorVersionDeleted = deleted.getProperty(PropertyIds.IS_LATEST_MAJOR_VERSION).getTypedValue();

		//nothing to do, the deleted object wasn't the latest or latest major.
		return latestMajorVersionDeleted || latestVersionDeleted;
	}

	
	//TODO: include parents/children of cmis-objects
	private Tree getTree(String repositoryId, String folderId) {
		CMISObject rootFolder = getObjSelector().getCMISObject(repositoryId, folderId);
		String basePath = rootFolder.getPropertyValue(PropertyIds.PATH);
		// Get all descendants (with no type, depth or acl filter).
		Set<ObjectParent> objectParents = objectParentSelector.getObjectParents(repositoryId, false, basePath, null, true);
		// Build tree retrieving the objects with acls for further allowable actions check.
		return new Tree(rootFolder, objectParents);
	}

	private List<String> deleteTree(Tree tree, String repositoryId, boolean allVersions, UnfileObject unfileObjects, boolean continueOnFailure) {
		List<String> failedIds = new ArrayList<String>();
		deleteTree(tree, tree.getRoot(),  null, failedIds, repositoryId, allVersions, unfileObjects, continueOnFailure, false);
		return failedIds;
	}

	/**
	 * 
	 * @param tree TODO
	 * @param node
	 * @param parent
	 * @param failedIds
	 * @param repositoryId
	 * @param allVersions
	 * @param unfileObjects
	 * @param continueOnFailure
	 * @param abort boolean If true then the method does not perform any deletion, only collects the ids as failedIds.
	 * @return
	 */
	private boolean deleteTree(Tree tree, Node node, Node parent, List<String> failedIds, String repositoryId, boolean allVersions, UnfileObject unfileObjects, boolean continueOnFailure, boolean abort) {
		boolean failed = abort;
		boolean isAbort = abort;
		List<Node> children = node.getChildren();
		if (!children.isEmpty()) {
			// Intermediate node. First delete the children and then the node.
			for (Node child : children) {
				failed |= deleteTree(tree, child, node, failedIds, repositoryId, allVersions, unfileObjects, continueOnFailure, isAbort);
				isAbort = isAbort || !continueOnFailure && failed;
			}
		}

		// Leaf node or after having deleted the children (only if no child has failed)
		if (!failed) {
			try {
				failed = deleteNode(tree, node, parent, repositoryId, allVersions, unfileObjects);
			} catch (RuntimeException e) {
				LOG.error("Error deleting/unfiling object: " + node.getCmisObject(), e);
				failed = true;
			}
		}

		if (failed) {
			failedIds.add(node.getCmisObject().getCmisObjectId());
		}
		return failed;
	}

	private boolean deleteNode(Tree tree, Node node, Node parent, String repositoryId, boolean allVersions, UnfileObject unfileObjects) {
		if (node.isDeleted()) {
			// Do not try again. Return successful this time, if the first was a failure, then it is already registered.
			return false;
		}
		CMISObject cmisObject = node.getCmisObject();
		if (cmisObject.isRootFolder()) {
			return true;
		}
		boolean failed = false;
		switch (unfileObjects) {
		case DELETE :
			failed = deleteNode(tree, node, cmisObject, repositoryId, allVersions);
			break;

		case DELETESINGLEFILED :
			if (cmisObject.getParents().size() == 1) {
				failed = deleteNode(tree, node, cmisObject, repositoryId, allVersions);
			}
			else {
				failed = unfileNode(cmisObject, parent.getCmisObject());
			}
			break;

		case UNFILE :
			if (cmisObject.isFolder()) {
				failed = deleteNode(tree, node, cmisObject, repositoryId, allVersions);
			}
			else {
				failed = unfileNode(cmisObject, parent.getCmisObject());
			}
			break;
		}
		return failed;
	}

	private boolean deleteNode(Tree tree, Node node, CMISObject cmisObject, String repositoryId, boolean allVersions) {
		if (!allVersions) {
			return deleteSingleNode(node, cmisObject, repositoryId);
		}

		String versionSeriesId = cmisObject.getPropertyValue(PropertyIds.VERSION_SERIES_ID);
		if (versionSeriesId == null) {
			// This would happen on non-versionable objects (v.g. folders).
			return deleteSingleNode(node, cmisObject, repositoryId);
		}

		List<CMISObject> versions = getAllVersions(repositoryId, null, versionSeriesId);
		// Mark all the version's nodes as deleted
		for (CMISObject version : versions) {
			Node versionNode = tree.getNode(version.getId());
			if (versionNode != null) {
				versionNode.setDeleted(true);
			}
		}

		if (cannotDeleteAllVersions(versions)) {
			// fail: In order to delete all versions you have to be able to delete every single version. In other case, no object would be deleted at all.
			return true;
		}

		// Delete all versions without update the latest version flags
		for (CMISObject version : versions) {
			removeFromParents(version);
			// This does not updates the latest version flags
			deleteObject(repositoryId, version);
		}

		return false;
	}

	private boolean cannotDeleteAllVersions(List<CMISObject> versions) {
		for (CMISObject version : versions) {
			if (!getSecurity().isAllowableAction(version, Action.CAN_DELETE_OBJECT)) {
				return true;
			}
		}
		return false;
	}

	private boolean deleteSingleNode(Node node, CMISObject cmisObject, String repositoryId) {
		node.setDeleted(true);
		if (!getSecurity().isAllowableAction(cmisObject, Action.CAN_DELETE_OBJECT)) {
			return true;
		}
		removeFromParents(cmisObject);
		// Updates the latest version flags
		deleteSingleObject(repositoryId, cmisObject);

		return false;
	}

	private boolean unfileNode(CMISObject cmisObject, CMISObject parent) {
		if (!getSecurity().isAllowableAction(cmisObject, Action.CAN_REMOVE_OBJECT_FROM_FOLDER)) {
			return true;
		}
		cmisObject.removeParent(parent);
		return false;
	}

	private void createObjectStream(String repositoryId, CMISObject cmisObject, ContentStream stream) {
		boolean isDocument = cmisObject.isDocument();

		if( null != stream && !isDocument ){
			throw new CmisConstraintException("It is only allowed to upload stream for " + BaseTypeId.CMIS_DOCUMENT + " objects");

		}else if( null == stream && ContentStreamAllowed.REQUIRED.equals(cmisObject.getObjectType().getContentStreamAllowed()) ){
			throw new CmisConstraintException("The stream is required for this object type");

		}else if ( null != stream && isDocument ) {
			flush();
			setContentStream(repositoryId, cmisObject, true, stream);
		}
	}


	/**
	 * Populate the ObjectTypeProperty of all the properties of the object.
	 * 
	 * It takes into consideration the secondary types properties.
	 * @param cmisObject
	 */
	private void populateObjectTypeProperties(CMISObject cmisObject){
		Map<String, SortedSet<ObjectTypeProperty>> allProperties = cmisObject.getObjectType().getObjectTypePropertiesIncludeParents(true, true);
		for(ObjectType secondary: cmisObject.getSecondaryTypes()) {
			allProperties.putAll(secondary.getObjectTypePropertiesIncludeParents(true, true));
		}
		for (Property property : cmisObject.getProperties()) {
			property.setObjectTypeProperty(allProperties.get(property.getObjectTypeProperty().getCmisId()).first());
		}
	}

	private boolean validateProperties(CMISObject obj, boolean checkRequired){
		//0. save object property names
		Set<String> objectPropertiesKeys = new HashSet<String>();
		for (Property property : obj.getProperties()) {
			objectPropertiesKeys.add(property.getObjectTypeProperty().getCmisId());
		}

		//1. check REQUIRED
		if (checkRequired) {
			Map<String, SortedSet<ObjectTypeProperty>> requiredProperties = obj.getObjectType().getObjectTypePropertiesIncludeParents(true, false);
			for(ObjectType secondary: obj.getSecondaryTypes()) {
				requiredProperties.putAll(secondary.getObjectTypePropertiesIncludeParents(true, false));
			}
			if ( !objectPropertiesKeys.containsAll(requiredProperties.keySet())){
				return false;
			}
		}

		//2. check OPTIONAL
		Map<String, SortedSet<ObjectTypeProperty>> allProperties = obj.getObjectType().getObjectTypePropertiesIncludeParents(true, true);
		for(ObjectType secondary: obj.getSecondaryTypes()) {
			allProperties.putAll(secondary.getObjectTypePropertiesIncludeParents(true, true));
		}

		return allProperties.keySet().containsAll(objectPropertiesKeys);
	}

	/*
	 * Populates the folder cmisobject's auto generated properties.Namely, cmis:path and cmis:parentId
	 */
	private void populateGeneratedFolderProperties(CMISObject cmisObject,CMISObject parent, Map<String, SortedSet<ObjectTypeProperty>> propertyTypeMap) {
		String parentCmisId = cmisObject.getParents().iterator().next().getCmisObjectId();
		String parentPath = parent.getPropertyValue(PropertyIds.PATH);

		//Property cmis:path, parent_path + SEPARATOR + cmis:name (path segment) of object
		ObjectTypeProperty pathPropertyType = propertyTypeMap.get(PropertyIds.PATH).first();
		String path;
		if (CMIS_PATH_SEP.equals(parentPath)) {
			path = CMIS_PATH_SEP + cmisObject.getPathSegment();
		}
		else {
			path = parentPath + CMIS_PATH_SEP + cmisObject.getPathSegment();
		}
		cmisObject.addProperty(new Property(pathPropertyType, path));

		//Property cmis:parentId, the cmisId of the parent object
		ObjectTypeProperty parentId = propertyTypeMap.get(PropertyIds.PARENT_ID).first();
		cmisObject.addProperty(new Property(parentId, parentCmisId));
	}

	private void populateGeneratedDocumentProperties(CMISObject cmisObject, Map<String, SortedSet<ObjectTypeProperty>> propertyTypeMap, VersioningState versioningState) {
		//Property isImmutable - false
		ObjectTypeProperty ism = propertyTypeMap.get(PropertyIds.IS_IMMUTABLE).first();
		cmisObject.addProperty(new Property(ism,  false));

		//Property Version Series Id - system generated
		ObjectTypeProperty vsi = propertyTypeMap.get(PropertyIds.VERSION_SERIES_ID).first();
		cmisObject.addProperty(new Property(vsi, getGenerator().next()));

		//Property Version Series Checked Out - system generated
		ObjectTypeProperty vsico = propertyTypeMap.get(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT).first();
		cmisObject.addProperty(new Property(vsico, false));

		//Property is private working copy - system generated
		ObjectTypeProperty pwc = propertyTypeMap.get(PropertyIds.IS_PRIVATE_WORKING_COPY).first();
		cmisObject.addProperty(new Property(pwc, false));

		//Property isLatestVersion - system generated
		ObjectTypeProperty il = propertyTypeMap.get(PropertyIds.IS_LATEST_VERSION).first();
		cmisObject.addProperty(new Property(il, true));

		//Property Version Label - system generated
		ObjectTypeProperty vl = propertyTypeMap.get(PropertyIds.VERSION_LABEL).first();
		ObjectTypeProperty imj = propertyTypeMap.get(PropertyIds.IS_MAJOR_VERSION).first();
		ObjectTypeProperty ilmj = propertyTypeMap.get(PropertyIds.IS_LATEST_MAJOR_VERSION).first();

		Property versionLabel = null;
		Property isMajor = null;
		Property isLatestMajor = null;

		boolean isMajorState = versioningState.equals(VersioningState.MAJOR);
		isMajor = new Property(imj, isMajorState);
		isLatestMajor = new Property(ilmj, isMajorState);
		versionLabel = new Property(vl, DEFAULT_VERSION_LABEL.get(versioningState));

		if (versionLabel != null) {
			cmisObject.addProperty(versionLabel);
		}
		if (isMajor != null) {
			cmisObject.addProperty(isMajor);
		}
		if (isLatestMajor != null) {
			cmisObject.addProperty(isLatestMajor);
		}
	}

	private void populateGeneratedProperties(CMISObject cmisObject, CMISObject parent, VersioningState versioningState) {
		Date now = getCurrentDate().getDate();
		String base = cmisObject.getObjectType().getBase().getCmisId();
		String currentUser = getSecurity().getCallContextHolder().getUsername();

		Map<String, SortedSet<ObjectTypeProperty>> propertyTypeMap = cmisObject.getObjectType().getObjectTypePropertiesIncludeParents(true, true);
		//COMMON PROPERTIES
		//Creation date - now
		ObjectTypeProperty cdate = propertyTypeMap.get(PropertyIds.CREATION_DATE).first();
		Property creationDate = new Property(cdate, now);
		cmisObject.addProperty(creationDate);

		//Last modified date - now
		ObjectTypeProperty lmdate = propertyTypeMap.get(PropertyIds.LAST_MODIFICATION_DATE).first();
		Property lastModDate = new Property(lmdate, now);
		cmisObject.addProperty(lastModDate);

		//base type, base cmisId
		ObjectTypeProperty baseId = propertyTypeMap.get(PropertyIds.BASE_TYPE_ID).first();
		Property baseType = new Property(baseId, base);
		cmisObject.addProperty(baseType);

		//Object type id - the object's cmisid
		ObjectTypeProperty objId = propertyTypeMap.get(PropertyIds.OBJECT_ID).first();
		Property objectId = new Property(objId, cmisObject.getCmisObjectId());
		cmisObject.addProperty(objectId);

		//Created by - the current user
		ObjectTypeProperty crtBy = propertyTypeMap.get(PropertyIds.CREATED_BY).first();
		Property createdBy = new Property(crtBy, currentUser);
		cmisObject.addProperty(createdBy);

		//Modified by - the current user
		ObjectTypeProperty modBy = propertyTypeMap.get(PropertyIds.LAST_MODIFIED_BY).first();
		Property lastModBy = new Property(modBy, currentUser);
		cmisObject.addProperty(lastModBy);

		//Change Token, auto generated
		ObjectTypeProperty cTok = propertyTypeMap.get(PropertyIds.CHANGE_TOKEN).first();
		Property changeToken = new Property(cTok, getGenerator().next());
		cmisObject.addProperty(changeToken);

		BaseTypeId baseTypeId = BaseTypeId.fromValue(base);
		switch (baseTypeId) {
		case CMIS_FOLDER:
			populateGeneratedFolderProperties(cmisObject, parent, propertyTypeMap);
			break;
		case CMIS_DOCUMENT:
			populateGeneratedDocumentProperties(cmisObject, propertyTypeMap,versioningState);
			break;
		case CMIS_POLICY:
			break;
		case CMIS_RELATIONSHIP:
			break;
		case CMIS_ITEM:
			break;
		case CMIS_SECONDARY:
			break;
		}
	}

	/**
	 * A valid object to be created
	 * 	1.has a parent folder
	 *  2.has a non-null and existing objectType
	 *  3.Does not contain the file sep. in its name
	 *  4.defines values for all the required properties of its property type.
	 *  5.defines values for properties that both exist and are appropriate to the respective propertyType
	 *  6. Does not share its name with any other of its siblings
	 * 
	 * @param object
	 * @param repositoryId
	 */

	private void validateNewObject(String repositoryId, CMISObject cmisObject, CMISObject parent ) {
		//name may not contain the file separator char
		String pathSegment = cmisObject.getPathSegment();
		if (pathSegment != null && pathSegment.indexOf(CMIS_PATH_SEP) != -1) {
			throw new CmisInvalidArgumentException("Name of object may not contain the file separator character : " + CMIS_PATH_SEP);
		}

		validateParent(cmisObject, parent);

		//Verify the required properties of this object's objectType and said objectType's hierarchy are supplied
		if( !validateProperties(cmisObject, true) ){
			throw new CmisInvalidArgumentException("Cannot create object, not all required properties supplied.");
		}

		if (parent != null) {
			//uniqueness of siblings' names within some parent folder
			validateSiblings(repositoryId, cmisObject, parent);
		}
	}

	/**
	 * Validates the object type and the secondary types
	 * 
	 * @param repositoryId
	 * @param cmisObject
	 * @param baseTypeId
	 */
	private void validateAndSetObjectType(String repositoryId, CMISObject cmisObject, BaseTypeId baseTypeId) {
		ObjectType objType = cmisObject.getObjectType();

		//ObjectType cannot be null
		if(objType == null) {
			//since CMIS 1.1, the cmis:objectTypeId is not required, if empty asuming base type
			objType = new ObjectType(baseTypeId.value());
			ObjectTypeProperty otp = new ObjectTypeProperty(PropertyIds.OBJECT_TYPE_ID);
			otp.setPropertyType(PropertyType.ID);
			cmisObject.addProperty(new Property(otp, baseTypeId.value()));
		}

		String objTypeCmisId = objType.getCmisId();
		//ObjectType must exist
		try {
			objType = objTypeSelector.getObjectTypeByCmisId(repositoryId, objTypeCmisId);
		} catch (Exception x) {
			throw new CmisInvalidArgumentException("Cannot create object, unknown Object Type : " + objTypeCmisId, x);
		}
		
		if(!baseTypeId.value().equals(objType.getBase().getCmisId())) {
			throw new CmisConstraintException("Cannot create " + baseTypeId.value() + " of type: " + objTypeCmisId);
		}
		
		if(!objType.isCreatable()) {
			throw new CmisInvalidArgumentException("Object-Type: " + objTypeCmisId + " is not creatable.");
		}
		
		//one needs to set this here since method getPathSegment on CMISObject need an object with objectType
		cmisObject.setObjectType(objType);
		
		//check and populate secondary types
		//TODO refactor and move this code and CMISBaseObjectService.persistOrMergeProperties() to a common place 
		List<Property> properties = cmisObject.getPropertyMap().get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
		if(properties != null) {
			for(Property p: properties) {
				String secondaryTypeId = p.getTypedValue();
				if (objTypeSelector.countObjectTypeByCmisId(repositoryId, secondaryTypeId) == 0) {
					throw new CmisConstraintException("Non-existent secondary type, you could use only cmis:secondary subtypes for the property values cmis:secondaryObjectTypeIds.");
				} else {
					ObjectType secondaryType = objTypeSelector.getObjectTypeByCmisId(repositoryId, secondaryTypeId);
					if(!BaseTypeId.CMIS_SECONDARY.value().equals(secondaryType.getBase().getCmisId()) ||
							BaseTypeId.CMIS_SECONDARY.value().equals(secondaryType.getCmisId())	) {
						throw new CmisConstraintException("You could use only cmis:secondary subtypes for the property values cmis:secondaryObjectTypeIds.");
					}
					cmisObject.addSecondaryType(secondaryType);
				}
			}
		}
	}

	private void validateParent(CMISObject cmisObject, CMISObject parent) {
		// Only one parent and must be a folder
		// In case of a folder we need to validate that parent is not null. No need to validate how many parents because on creation only 0 a 1 are possible values.
		if (cmisObject.isFolder() && parent == null) {
			throw new CmisInvalidArgumentException("Cannot create object, need one and only one parent object: " + cmisObject);
		}

		if (parent != null && !parent.isFolder()) {
			throw new CmisInvalidArgumentException("Cannot create object, invalid parent: " + parent);
		}

	}

	/**
	 * A valid object to be moved
	 * 	1.is file-able
	 *  2.moved from a known parent of said object
	 *  3.No siblings must exist under the new parent with the same name
	 *  4.the new parent may not be a child of the object to be moved
	 * 
	 */
	private void validateMovedObject(String objectId, String sourceFolderId, CMISObject cmisObject, CMISObject target) {
		if (sourceFolderId.equals(target.getCmisObjectId())) {
			throw new CmisInvalidArgumentException("Pointless trying to move an object under its current parent: "+sourceFolderId);
		}
		//object should be filable
		if (!cmisObject.getObjectType().isFileable()) {
			throw new CmisInvalidArgumentException("Cannot move a non-filable object :"+objectId);
		}

		// target may not be the object itself
		if (target.equals(cmisObject)) {
			throw new CmisInvalidArgumentException("Cannot move a folder to itself.");
		}

		//source must be a parent of object
		boolean foundParent = false;
		for (CMISObject parent : cmisObject.getParents()) {
			if (parent.getCmisObjectId().equals(sourceFolderId)) {
				foundParent = true;
				break;
			}
		} if (!foundParent) {
			throw new CmisInvalidArgumentException(sourceFolderId+" is not a parent of "+objectId);
		}

		if (cmisObject.isFolder()) {
			Property p = null;
			try {
				p = target.getProperty(PropertyIds.PATH);
			} catch (PropertyNotFoundException e) {
				throw new CmisRuntimeException("Cannot move to the target " + target + ", No property PATH could be retrieved", e);
			}
			//target may not be the object itself (checked above) or a descendant of the object...
			String objectPath = cmisObject.getPropertyValue(PropertyIds.PATH);
			String targetPath = p.getTypedValue();

			// Add the path separator to avoid common part names collision: /A/B to /A/BB
			if (targetPath.startsWith(objectPath + CMIS_PATH_SEP)) {
				throw new CmisInvalidArgumentException("Cannot move a folder to one of its children ");
			}
		}

		checkAllowedChildren(cmisObject.<String>getPropertyValue(PropertyIds.OBJECT_TYPE_ID), target);
	}

}
