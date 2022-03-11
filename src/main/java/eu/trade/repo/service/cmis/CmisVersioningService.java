package eu.trade.repo.service.cmis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.commons.spi.VersioningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.security.ApplyTo;
import eu.trade.repo.security.Secured;
import eu.trade.repo.security.Security;
import eu.trade.repo.service.cmis.data.in.AclBuilder;
import eu.trade.repo.service.cmis.data.out.ObjectDataImpl;
import eu.trade.repo.service.cmis.data.out.PropertiesBuilder;
import eu.trade.repo.service.interfaces.IRelationshipService;
import eu.trade.repo.service.interfaces.IVersioningService;

/**
/**
 * CMIS Versioning Service implementation.
 * <p>
 * Implementation of the CMIS ACL services that uses the {@link IVersioningService} to perform the needed operations.
 */
@Transactional
public class CmisVersioningService implements VersioningService {
	@Autowired
	private IVersioningService versioningService;
	public void setObjectService(IVersioningService versioningService) {	this.versioningService = versioningService;	}
	
	@Autowired
	private IRelationshipService relationshipService;

	@Autowired
	private Security security;
	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.VersioningService#checkOut(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.data.ExtensionsData, org.apache.chemistry.opencmis.commons.spi.Holder)
	 */
	@Override
	@Secured(Action.CAN_CHECK_OUT)
	public void checkOut(String repositoryId,@ApplyTo Holder<String> objectId, ExtensionsData extension, Holder<Boolean> contentCopied) {
		versioningService.checkOut(repositoryId, objectId, contentCopied);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.VersioningService#cancelCheckOut(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_CANCEL_CHECK_OUT)
	public void cancelCheckOut(String repositoryId,@ApplyTo String objectId, ExtensionsData extension) {
		versioningService.cancelCheckOut(repositoryId, objectId);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.VersioningService#checkIn(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.Properties, org.apache.chemistry.opencmis.commons.data.ContentStream, java.lang.String, java.util.List, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_CHECK_IN)
	public void checkIn(String repositoryId,@ApplyTo Holder<String> objectId, Boolean major, Properties properties, ContentStream contentStream,
			String checkinComment, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		Set<String> permissionNames = security.getPermissionNames(repositoryId);
		String currentUsername = security.getCallContextHolder().getUsername();
		Set<eu.trade.repo.model.Acl> aclToAdd = AclBuilder.build(addAces, permissionNames, currentUsername);
		Set<eu.trade.repo.model.Acl> aclToRemove = AclBuilder.build(removeAces, permissionNames, currentUsername);
		versioningService.checkIn(repositoryId, objectId, major, properties, contentStream, checkinComment, policies, aclToAdd, aclToRemove);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.VersioningService#getObjectOfLatestVersion(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId, Boolean major, String filter, Boolean includeAllowableActions,
			IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {

		CMISObject cmisObject = versioningService.getObjectOfLatestVersion(repositoryId, objectId, versionSeriesId, major);
		if (cmisObject == null) {
			throw new CmisObjectNotFoundException("Not found object of latest version.");
		}
		secureObject(cmisObject);
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
	 * @see org.apache.chemistry.opencmis.commons.spi.VersioningService#getPropertiesOfLatestVersion(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId, Boolean major, String filter, ExtensionsData extension) {
		CMISObject cmisObject = versioningService.getObjectOfLatestVersion(repositoryId, objectId, versionSeriesId, major);
		secureObject(cmisObject);
		return PropertiesBuilder.build(cmisObject, filter);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.VersioningService#getAllVersions(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter, Boolean includeAllowableActions, ExtensionsData extension) {
		List<ObjectData> ans = new ArrayList<ObjectData>();
		if (objectId == null && versionSeriesId == null) {
			throw new CmisInvalidArgumentException(String.format("One of object ID or version series ID must not be null"));
		}

		Map<String, String> filterSet = PropertiesBuilder.buildFilter(filter);
		List<CMISObject> versions = versioningService.getAllVersions(repositoryId, objectId, versionSeriesId);
		filterPWC(versions);
		secureAllVersions(versions);
		for (CMISObject version : versions) {
			AllowableActions allowableActions = null;
			if (Boolean.TRUE.equals(includeAllowableActions)) {
				allowableActions = security.getAllowableActions(version);
			}
			ans.add(new ObjectDataImpl(version, false, false, allowableActions, null, filterSet));
		}
		return ans;
	}

	private void filterPWC(List<CMISObject> versions) {
		if (!versions.isEmpty()) {
			CMISObject first = versions.get(0);
			if ( first.isPwc() && !security.isAllowableAction(first, Action.CAN_GET_PROPERTIES, Action.CAN_GET_ALL_VERSIONS) ) {
				versions.remove(0);
			}
		}
	}

	/**
	 * TODO [porrjai] implement as a custom secure aspect.
	 */
	private void secureAllVersions(List<CMISObject> versions) {
		for (CMISObject cmisObject : versions) {
			if (!security.isAllowableAction(cmisObject, Action.CAN_GET_ALL_VERSIONS)) {
				throw new CmisPermissionDeniedException("The user is not authorized to proceed with the action.");
			}
		}
	}

	private void secureObject(CMISObject cmisObject) {
		if (!security.isAllowableAction(cmisObject, Action.CAN_GET_PROPERTIES)) {
			throw new CmisPermissionDeniedException("The user is not authorized to proceed with the action.");
		}
	}
}
