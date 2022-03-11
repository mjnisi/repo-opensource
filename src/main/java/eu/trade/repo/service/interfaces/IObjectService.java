package eu.trade.repo.service.interfaces;
import static eu.trade.repo.util.Constants.*;

import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.springframework.security.access.annotation.Secured;

import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.Rendition;

/**
 * Interface for object business services
 */
public interface IObjectService {

	/**
	 * Creates a cmis object.
	 * 
	 * @param repositoryId
	 * @param object
	 * @param aclToAdd {@link Set<Acl>} Not null acl set to be added. May be empty.
	 * @param aclToRemove {@link Set<Acl>} Not null acl set to be removed. May be empty.
	 * @param stream
	 * @return {@link CMISObject} The cmis object created.
	 */
	CMISObject createObject(String repositoryId, CMISObject object, Set<Acl> aclToAdd, Set<Acl> aclToRemove, ContentStream stream, VersioningState versioningState, BaseTypeId baseTypeId);
	
	/**
	 * 
	 * @param repositoryId
	 * @param object
	 * @param aclToAdd
	 * @param aclToRemove
	 */
	CMISObject createRelationship(String repositoryId, CMISObject object, Set<Acl> aclToAdd, Set<Acl> aclToRemove, List<String> policies);

	/**
	 * Returns the CMIS Object specified by its CMIS id or by its path.
	 * <p>
	 * Since the object is already loaded with the ACLs due to the Authorization, the includeAcl parameter is no longer needed at this level.
	 * 
	 * @param repositoryId {@link String} The repository id
	 * @param pathOrId {@link String} The CMIS Object path or id
	 * @return {@link CMISObject} The CMIS Object
	 */
	CMISObject getObject(String repositoryId, String pathOrId);

	List<Rendition> getRenditions(
			String repositoryId,
			//String renditionFilter,
			String objectId,
			int maxItems,
			int skipCount);

	void deleteObject(String repositoryId, String objectId, boolean allVersions);

	/**
	 * TODO Ranges
	 * @param repositoryId
	 * @param cmisObjectId
	 * @return
	 */
	ContentStream getContentStream(String repositoryId, String cmisObjectId
			//    		, String streamId,
			//    		long offset,
			//            long length
			);

	String updateProperties(String repositoryId, String cmisObjectId, Properties properties);

	/**
	 * Move an object from source folder to target folder.
	 * 
	 * @param repositoryId
	 * @param objectId
	 * @param sourceFolderId
	 * @param targetFolderId
	 */
	void moveObject(
			String repositoryId,
			String objectId,
			String sourceFolderId,
			String targetFolderId);




	//returns IDs of the failing objects
	List<String> deleteTree(String repositoryId, String folderId, boolean allVersions,
			UnfileObject unfileObjects, boolean continueOnFailure);


	/**
	 * Either creates a new content-stream for a document object or replaces an existing content-stream.
	 * 
	 * This service is considered modification to a content-stream's containing document object, and therefore change the object's LastModificationDate property
	 * upon successful completion.
	 * 
	 * @param repositoryId
	 * @param cmisObjectId
	 * @param overwriteFlag
	 * @param inputStream
	 * @return
	 */
	String setContentStream(String repositoryId, String cmisObjectId, boolean overwriteFlag, ContentStream inputStream);

	String deleteContentStream(
			String repositoryId,
			String cmisObjectId
			);

	@Secured(VIEW_REPO_SUMMARY)
	List<?> getObjectsCountForRepository(String repoId);

	@Secured(VIEW_REPO_SUMMARY)
	List<?> getObjectCountPerRepository();

	@Secured(VIEW_REPO_SUMMARY)
	long getNumberOfObjectsWaitingForContentIndexing(String repoId);

	@Secured(VIEW_REPO_SUMMARY)
	List<?> getObjectsCountWithContentIndexStatus(String repoId);
	
	@Secured(VIEW_REPO_SUMMARY)
	List<?> getObjectsCountWithMetadataIndexStatus(String repoId);
}
