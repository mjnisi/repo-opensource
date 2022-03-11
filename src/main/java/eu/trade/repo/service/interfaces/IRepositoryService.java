package eu.trade.repo.service.interfaces;

import static eu.trade.repo.util.Constants.CHANGE_REPO_CAPABILITIES;
import static eu.trade.repo.util.Constants.CHANGE_REPO_SECURITY;
import static eu.trade.repo.util.Constants.CREATE_REPO;
import static eu.trade.repo.util.Constants.DELETE_REPO;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.springframework.security.access.annotation.Secured;

import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.Permission;
import eu.trade.repo.model.PermissionMapping;
import eu.trade.repo.model.Repository;

/**
 * Repository Interface service that defines all the "business" methods related with repositories.
 */
public interface IRepositoryService {

	@Secured(CREATE_REPO)
	Repository createRepository(Repository repository);

	@Secured(DELETE_REPO)
	void deleteRepository(String repositoryId);

	/**
	 * Returns all repositories.
	 * 
	 * @return {@link List<Repository>}
	 */
	List<Repository> getAllRepositories();

	/**
	 * Returns the repository.
	 * 
	 * @param repositoryId {@link String} The repository cmis id.
	 * @return {@link Repository}
	 */
	Repository getRepositoryById(String repositoryId);

	/**
	 * The object type's children.
	 * <p>
	 * If <code>includeProperties</code> is true, then the object has to load all of its properties (direct or inherited)
	 * 
	 * @param repositoryId {@link String} The repository id.
	 * @param cmisId {@link String} The {@link ObjectType} cmis id.
	 * @param includeLazyLoadedDependencies
	 * @param maxItems
	 * @param skipCount
	 * @return
	 */
	Set<ObjectType> getObjectTypeChildren(String repositoryId, String cmisId, boolean includeLazyLoadedDependencies, int maxItems, int skipCount);

	/**
	 * Return the count of the object type;s children.
	 * 
	 * @param repositoryId {@link String} The repository id.
	 * @param cmisId {@link String} The {@link ObjectType} cmis id.
	 * @return {@link Long} the count of the object type;s children.
	 */
	Long countObjectTypeChildren(String repositoryId, String cmisId);

	/**
	 * The object type's children.
	 * <p>
	 * If <code>includeProperties</code> is true, then the object has to load all of its properties (direct or inherited)
	 * TODO Rwview
	 * @param repositoryId {@link String} The repository id.
	 * @param cmisId {@link String} The {@link ObjectType} cmis id.
	 * @param includeProperties boolean If true, then the object type must be loaded with the property type definitions.
	 * @return {@link Set<ObjectType>} The object type's children.
	 */
	Set<ObjectType> getObjectTypeChildren(String repositoryId, String cmisId, boolean includeProperties);

	/**
	 * Returns the definition of the specified object-type.
	 * 
	 * @param repositoryId {@link String} The repository id.
	 * @param cmisId {@link String} The {@link ObjectType} cmis id.
	 * @return {@link ObjectType} Object-type including all property definitions.
	 */
	ObjectType getObjectType(String repositoryId, String cmisId);

	
	/**
	 * Returns the list of all base types that exists in the repository
	 * 
	 * @param repositoryId {@link String} The repository id.
	 * @return
	 */
	List<BaseTypeId> getBaseObjectTypes(String repositoryId);
	
    /**
     * Creates a new type.
     * 
     * @param objectType
     *            the type definition
     * @return the newly created type
     */
    ObjectType createType(ObjectType objectType);

    /**
     * Updates a type.
     * 
     * @param objectType
     *            the type definition
     * @return the updated type
     */
    ObjectType updateType(ObjectType objectType);

    /**
     * Deletes a type.
     * 
     * @param repositoryId
     *            the identifier for the repository
     * @param typeId
     *            typeId of an object type specified in the repository
     */
    void deleteType(String repositoryId, String typeId);

	
	/**
	 * Returns the root folder id of the specified repository
	 * 
	 * @param repositoryId The repository id.
	 * @return {@link String} The root folder id of the specified repository
	 */
	String getRootFolderId(String repositoryId);

	/**
	 * Updates the repository with the new attributes.
	 * 
	 * @param repository {@link Repository} The repository with the new attributes.
	 * @return {@link Repository} The updated repository.
	 */
	@Secured({CHANGE_REPO_CAPABILITIES, CHANGE_REPO_SECURITY})
	Repository update(Repository repository);

	/**
	 * Toggles the security type for a given repository.
	 * <p>
	 * Note: Toggling the security type is restricted to the current repository status, therefor is not always possible.
	 * 
	 * @param repositoryId {@link String} The repository id.
	 * @return {@link Repository} The updated repository.
	 */
	@Secured(CHANGE_REPO_SECURITY)
	Repository toggleSecurity(String repositoryId);

	/**
	 * Updates the security specification for a given repository.
	 * <p>
	 * The security type is not modified.
	 * 
	 * @param repositoryId {@link String} The repository id.
	 * @param defaultAuthenticationHandler {@link String} The default authentication handler.
	 * @param defaultAuthorisationHandler {@link String} The default authorisation handler.
	 * @param enabledAuthenticationHandlers {@link Set<String>} The enabled authentication handlers, may include the default one. Mandatory for multiple security.
	 * @param enabledAuthorisationHandlers {@link Set<String>} The enabled authorisation handlers, may include the default one. Mandatory for multiple security.
	 * @return
	 */
	@Secured(CHANGE_REPO_SECURITY)
	Repository updateSecurity(String repositoryId, String defaultAuthenticationHandler, String defaultAuthorisationHandler, Set<String> enabledAuthenticationHandlers, Set<String> enabledAuthorisationHandlers);

	/**
	 * Returns the repository with the security handlers collection loaded.
	 * 
	 * @param repositoryId {@link String} The repository cmis id.
	 * @return {@link Repository}
	 */
	@Secured(CHANGE_REPO_SECURITY)
	Repository getRepositoryByIdWithHandlers(String repositoryId);

	void createPermission(Permission permission, String repositoryId);

	Permission getPermission(String name, String repositoryId);

	List<Permission> getRepositoryPermissions(String repositoryId);

	List<PermissionMapping> getRepositoryPermissionsMappings(String repositoryId);

	void updatePermission(String permissionName, String permissionNewName, String permissionNewDescription, String repositoryId);

	void deletePermission(String permissionToDelete, String repositoryId);

	void setPermissionMappings(Set<PermissionMapping> permissionMappings, String repositoryId);
	
	/**
	 * Gets the version series with documents disagregated (different parents)
	 * @param repositoryId
	 * @return
	 */
	List<String> getDisaggregatedVersionSeries(String repositoryId);
	
	/**
	 * Gets the change log token uniquely identifying the latest content change, of some repository, at the time of the request 
	 * @param repositoryId The repository whose latest change log token to get
	 * @return The latest change log token of the repository
	 */
	String getLatestChangeLogEvent(String repositoryId);
	
	/**
	 * Return the policies defined in the repository and if they are enabled.
	 * First checks the policy implementations, then if the type exists and
	 * finally if it is enabled or not.
	 * 
	 * Key: the objectType associated with the policy
	 * Value: true if the policy is enabled in the system 
	 *  
	 * @param repositoryId
	 * @return
	 */
	Map<ObjectType, Boolean> getPolicies(String repositoryId);
	
	/**
	 * Obtains the CMIS ID of the enabled policies for the given repository
	 *   
	 * @param repositoryId
	 * @return Set with the CMIS IDs of the policies types
	 */
	Set<String> getEnabledPolicies(String repositoryId);
	
	
	/**
	 * Set the enabled policies  
	 * 
	 * @param repositoryId 
	 * @param enabledPolicies Set with CMIS policy type IDs
	 */
	void setEnabledPolicies(String repositoryId, Set<String> enabledPolicies);
	
}
