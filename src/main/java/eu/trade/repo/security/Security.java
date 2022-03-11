package eu.trade.repo.security;

import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.util.Cleanable;


/**
 * Interface for the basic operations regarding the following authorization cmis concepts:
 * <ul>
 * <li>Repositoryservice.getRepositoryInfo(repositoryId): Must return all the permissions mappings and permission definitions for the repository.</li>
 * <li>Repositoryservice.getAllowableActions(objectId): Must return the set of allowable actions for the object granted to the current user.</li>
 * </ul>
 * 
 * @see PermissionMapping
 * @see Action
 * @author porrjai
 */
public interface Security extends Cleanable {

	/**
	 * Returns the list of permission mapping using the chemistry interface.
	 * 
	 * @param repositoryId {@link String} The repostoryId
	 * @return {@link List} The list of permission mapping using the chemistry interface.
	 */
	List<PermissionMapping> getPermissionMappings(String repositoryId);

	/**
	 * Returns the list of permission definitions using the chemistry interface.
	 * 
	 * @param repositoryId {@link String} The repostoryId
	 * @return {@link List} The list of permission definitions using the chemistry interface.
	 */
	List<PermissionDefinition> getPermissionDefinitions(String repositoryId);

	/**
	 * Returns the set of permission names.
	 * 
	 * @param repositoryId {@link String} The repostoryId
	 * @return {@link Set} The set of permission names.
	 */
	Set<String> getPermissionNames(String repositoryId);

	/**
	 * Returns the set of the permissions ids that satisfies the action.
	 * 
	 * @param repositoryId {@link String} the repository id. Mandatory not null.
	 * @param actions {@link Action} the action to be satisfied. Mandatory not null.
	 * @return {@link Set<Integer>} the set of the permissions ids that satisfies the action.
	 */
	Set<Integer> getPermissionIds(String repositoryId, Action action);

	/**
	 * Returns the list of allowable actions for this object regarding the access level of the current user to the object specified by its cmis id.
	 * <p>
	 * COLLATERAL: If the object is not already in the thread local cache, this method will load the object (along with its ACL) and will cache it.
	 * 
	 * @param cmisObjectId {@link String} The cmis object id.
	 * @return {@link List<AllowableActions>} The list of allowable actions for this object regarding the access level of the current user to the object specified by its cmis id.
	 */
	AllowableActions getAllowableActions(String cmisObjectId);

	/**
	 * Returns the list of allowable actions for this object regarding the access level of the current user to the specified object.
	 * <p>
	 * This method will access to the object's ACL. If the ACL it is not already loaded then it will get from database.<br/>
	 * So, in the case of iterating over a list of objects, it is convenient to be sure that all those objects have its ACL already loaded.
	 * 
	 * @param cmisObject {@link CMISObject} The cmis object.
	 * @return {@link AllowableActions} The list of allowable actions for this object regarding the access level of the current user to the specified object.
	 */
	AllowableActions getAllowableActions(CMISObject cmisObject);

	/**
	 * Returns whether ALL the allowable actions can be performed on the specified object.
	 * 
	 * @param cmisObject {@link CMISObject} The cmis object. Mandatory not null.
	 * @param actions {@link Actions} The actions to be satisfied. Mandatory not null.
	 * @return boolean Whether ALL the allowable action can be performed on the specified object.
	 */
	boolean isAllowableAction(CMISObject cmisObject, Action... actions);

	/**
	 * Returns whether the current user is granted for similar (the same or more) actions that the ones granted by the specified permission.
	 * 
	 * @param cmisObject
	 * @param permissionName
	 * @return boolean Whether the current user is granted for similar (the same or more) actions that the ones granted by the specified permission.
	 */
	boolean isSimilarGranted(CMISObject cmisObject, String permissionName);
	
	/**
	 * Returns the call context holder.
	 * 
	 * @return {@link CallContextHolder} the call context holder.
	 */
	CallContextHolder getCallContextHolder();

	/**
	 * Returns the access control.
	 * 
	 * @return {@link AccessControl} the access control.
	 */
	AccessControl getAccessControl();

	/**
	 * Cleans the security resources cached for the specified repository.
	 * 
	 * @param repositoryId {@link String} the repository id. Mandatory not null.
	 */
	void clean(String repositoryId);
}
