/**
 * 
 */
package eu.trade.repo.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.changelog.ChangeLog;
import eu.trade.repo.index.triggers.annotation.TriggerIndex;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.Repository;
import eu.trade.repo.service.interfaces.IMultifilingService;

/**
 * Multifiling Service implementation.
 * 
 * @author porrjai
 */
public class MultifilingService extends CMISBaseObjectService implements IMultifilingService {

	@Autowired
	private ChangeLog changeLog;

	/**
	 * @see eu.trade.repo.service.interfaces.IMultifilingService#addObjectToFolder(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean)
	 */
	@TriggerIndex
	@Override
	public void addObjectToFolder(String repositoryId, String objectId, String folderId, Boolean allVersions) {
		Repository repository = checkMultifiling(repositoryId);
		CMISObject cmisObject = getObjSelector().getCMISObject(repositoryId, objectId);
		CMISObject parent = getObjSelector().getCMISObject(repositoryId, folderId);

		if (fileAllVersionTogether(repository, cmisObject)) {
			fileVersionSeries(repositoryId, cmisObject, parent);
		}
		else {
			// file single
			validateSiblings(repositoryId, cmisObject, parent);
			cmisObject.addParent(parent);
		}

		changeLog.update(repositoryId, folderId);
		getSecurity().getAccessControl().file(repositoryId, cmisObject, parent);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IMultifilingService#removeObjectFromFolder(java.lang.String, java.lang.String, java.lang.String)
	 */
	@TriggerIndex
	@Override
	public void removeObjectFromFolder(String repositoryId, String objectId, String folderId) {
		Repository repository = checkMultifiling(repositoryId);
		CMISObject cmisObject = getObjSelector().getCMISObject(repositoryId, objectId);
		CMISObject parent = getObjSelector().getCMISObject(repositoryId, folderId);

		if (fileAllVersionTogether(repository, cmisObject)) {
			unfileVersionSeries(repositoryId, cmisObject, parent);
		}
		else {
			// unfile single
			cmisObject.removeParent(parent);
		}

		if (cmisObject.getParents().isEmpty() && !repository.getUnfiling()) {
			throw new CmisConstraintException("The repository has no unfiling capability. Every object must have at least one parent.");
		}
		changeLog.update(repositoryId, folderId);
		getSecurity().getAccessControl().unfile(repositoryId, cmisObject, parent);
	}

	private Repository checkMultifiling(String repositoryId) {
		Repository repository = getRepositorySelector().getRepository(repositoryId);
		if (!repository.getMultifiling()) {
			throw new CmisConstraintException("The repository has no multifiling capability.");
		}
		return repository;
	}

	private void fileVersionSeries(String repositoryId, CMISObject cmisObject, CMISObject parent) {
		String versionSeriesId = getVersionSeriesId(cmisObject);
		Set<String> checkedPathSegments = new HashSet<>();
		for (CMISObject version : getAllVersions(repositoryId, null, versionSeriesId)) {
			// can be filed
			if (!getSecurity().isAllowableAction(version, Action.CAN_ADD_OBJECT_TO_FOLDER)) {
				throw new CmisPermissionDeniedException("Cannot file all the objects in the version series: " + versionSeriesId);
			}

			// validate siblings
			String pathSegment = cmisObject.getPathSegment();
			if (!checkedPathSegments.contains(pathSegment)) {
				validateSiblings(repositoryId, cmisObject, parent);
				checkedPathSegments.add(pathSegment);
			}

			// file
			cmisObject.addParent(parent);
		}
	}

	private void unfileVersionSeries(String repositoryId, CMISObject cmisObject, CMISObject parent) {
		String versionSeriesId = getVersionSeriesId(cmisObject);
		List<CMISObject> versions = getAllVersions(repositoryId, null, versionSeriesId);
		alignVersionSeries(versions, parent);
		for (CMISObject version : versions) {
			// can be unfiled
			if (!getSecurity().isAllowableAction(version, Action.CAN_REMOVE_OBJECT_FROM_FOLDER)) {
				throw new CmisPermissionDeniedException("Cannot unfile all the objects in the version series: " + versionSeriesId);
			}

			// unfile
			cmisObject.removeParent(parent);
		}
	}
}
