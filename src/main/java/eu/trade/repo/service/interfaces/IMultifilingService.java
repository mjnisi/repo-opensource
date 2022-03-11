package eu.trade.repo.service.interfaces;


/**
 * Interface for Multifiling business services.
 * 
 * @author porrjai
 */
public interface IMultifilingService {

	/**
	 * Adds an existing fileable non-folder object to a folder.
	 * 
	 * @param repositoryId {@link String} The repository id
	 * @param cmisObjectId {@link String} The object's CMIS id.
	 * @param folderId {@link String} The target folder's CMIS id.
	 * @param allVersions
	 */
	void addObjectToFolder(String repositoryId, String objectId, String folderId, Boolean allVersions);

	/**
	 * Removes an existing fileable non-folder object from a folder.
	 * 
	 * @param repositoryId {@link String} The repository id
	 * @param cmisObjectId {@link String} The object's CMIS id.
	 * @param folderId {@link String} The sorce folder's CMIS id.
	 */
	void removeObjectFromFolder(String repositoryId, String objectId, String folderId);
}
