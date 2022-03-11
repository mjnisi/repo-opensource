package eu.trade.repo.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.model.Acl;
import eu.trade.repo.model.ActionParameter;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.Permission;
import eu.trade.repo.model.Repository;
import eu.trade.repo.security.CustomSecured.CustomAction;
import eu.trade.repo.selectors.CMISObjectSelector;

/**
 * Default {@link Authorization} and {@link Security} implementation.
 * 
 * @author porrjai
 */
class SecurityImpl implements Authorization, Security {

	private static final Logger LOG = LoggerFactory.getLogger(SecurityImpl.class);
	static final String TYPE_ERROR_PREFIX = "The object's base type doesn't match the allowed type for this operation: ";
	private static final String TYPE_ERROR = TYPE_ERROR_PREFIX + "[action: %s, parameter: %s, object: %s, type: %s, allowed types: %s]";

	@Autowired
	private CallContextHolder callContextHolder;

	@Autowired
	private AccessControl accessControl;

	@Autowired
	private CMISObjectSelector cmisObjectSelector;

	@Autowired
	private PermissionMappingCache permissionMappingCache;

	@Autowired
	private PermissionCache permissionCache;

	/**
	 * @see eu.trade.repo.security.Security#getPermissionMappings(java.lang.String)
	 */
	@Override
	public List<org.apache.chemistry.opencmis.commons.data.PermissionMapping> getPermissionMappings(String repositoryId) {
		return permissionMappingCache.getPermissionMappings(repositoryId);
	}

	/**
	 * @see eu.trade.repo.security.Security#getPermissionDefinitions(java.lang.String)
	 */
	@Override
	public List<PermissionDefinition> getPermissionDefinitions(String repositoryId) {
		return permissionCache.getPermissionDefinitions(repositoryId);
	}

	/**
	 * @see eu.trade.repo.security.Security#getPermissionNames(java.lang.String)
	 */
	@Override
	public Set<String> getPermissionNames(String repositoryId) {
		return permissionCache.getPermissionNames(repositoryId);
	}

	/**
	 * @see eu.trade.repo.security.Security#getPermissionIds(java.lang.String, org.apache.chemistry.opencmis.commons.enums.Action)
	 */
	@Override
	public Set<Integer> getPermissionIds(String repositoryId, Action action) {
		Set<String> permissionsNames = new HashSet<>();
		Set<ActionParameter> actionParameters = ActionMap.getActionParameters(action);
		for (ActionParameter actionParameter : actionParameters) {
			String dbKey = ActionMap.getDbKey(action, actionParameter);
			Set<String> grantingPermissionsNamesByDbKey = permissionMappingCache.getGrantingPermissions(repositoryId, dbKey);
			if (permissionsNames.isEmpty()) {
				permissionsNames.addAll(grantingPermissionsNamesByDbKey);
			}
			else {
				// For more than one dbKey we need the intersection (i.e. the expanded permissions that satisfies both).
				permissionsNames = intersect(permissionsNames, grantingPermissionsNamesByDbKey);
			}
		}
		return permissionCache.getPermissionsIds(repositoryId, permissionsNames);
	}

	private Set<String> intersect(Set<String> setA, Set<String> setB) {
		Set<String> intersection = new HashSet<>();
		for (String string : setA) {
			if (setB.contains(string)) {
				intersection.add(string);
			}
		}
		return intersection;
	}

	/**
	 * @see eu.trade.repo.security.Security#getAllowableActions(java.lang.String)
	 */
	@Override
	public AllowableActions getAllowableActions(String cmisObjectId) {
		CMISObject cmisObject = getCmisObject(cmisObjectId);
		return getAllowableActions(cmisObject);
	}

	/**
	 * @see eu.trade.repo.security.Security#getAllowableActions(eu.trade.repo.model.CMISObject)
	 */
	@Override
	public AllowableActions getAllowableActions(CMISObject cmisObject) {
		Set<Action> allowableActions = new HashSet<>();

		for (Map.Entry<Action, String> entry : getEligibleActions(cmisObject).entrySet()) {
			if (isGrantedPermission(entry.getValue(), cmisObject)) {
				allowableActions.add(entry.getKey());
			}
		}

		return getAllowableActions(allowableActions);
	}

	/**
	 * @see eu.trade.repo.security.Security#isAllowableAction(eu.trade.repo.model.CMISObject, org.apache.chemistry.opencmis.commons.enums.Action)
	 */
	@Override
	public boolean isAllowableAction(CMISObject cmisObject, Action... actions) {
		Map<Action, String> eligibleActions = getEligibleActions(cmisObject);
		if (actions == null || actions.length == 0) {
			return false;
		}
		for (Action action : actions) {
			String dbKey = eligibleActions.get(action);
			if (dbKey == null || !isGrantedPermission(dbKey, cmisObject)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @see eu.trade.repo.security.Security#isSimilarGranted(eu.trade.repo.model.CMISObject, java.lang.String)
	 */
	@Override
	public boolean isSimilarGranted(CMISObject cmisObject, String permissionName) {
		if (callContextHolder.isAdmin()) {
			// Admins are granted for all permissions.
			return true;
		}

		Set<String> userGrantedPermissions = new HashSet<>();
		List<Acl> objectAcls = getObjectAcls(cmisObject);
		if (objectAcls != null) {
			for (Acl acl : objectAcls) {
				userGrantedPermissions.add(acl.getPermission().getName());
			}
		}

		if (userGrantedPermissions.contains(permissionName)) {
			return true;
		}

		String repositoryId = cmisObject.getObjectType().getRepository().getCmisId();
		Set<String> dbKeys = ActionMap.getDbKeys().keySet();
		for (String dbKey : dbKeys) {
			Set<String> grantingPermissions = permissionMappingCache.getGrantingPermissions(repositoryId, dbKey);
			if (grantingPermissions.contains(permissionName) && !intersects(userGrantedPermissions, grantingPermissions)) {
				return false;
			}
		}

		return true;
	}

	private boolean intersects(Set<String> userGrantedPermissions, Set<String> grantingPermissions) {
		for (String userGrantedPermission : userGrantedPermissions) {
			if (grantingPermissions.contains(userGrantedPermission)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see eu.trade.repo.security.Authorization#checkPermission(org.apache.chemistry.opencmis.commons.enums.Action, java.util.Map)
	 */
	@Override
	@Transactional
	public void checkPermission(Action action, Map<ActionParameter, String> actionParameters) {
		// First, check action vs action parameters. Must be complete. Also If ActionParameter.DEFAULT then replace it
		checkActionParameters(action, actionParameters);
		// For each pair check the permission
		for (Map.Entry<ActionParameter, String> actionParameterEntry : actionParameters.entrySet()) {
			String parameter = actionParameterEntry.getValue();
			if (parameter != null) {
				CMISObject cmisObject = getCmisObject(parameter);
				ActionParameter actionParameter = actionParameterEntry.getKey();
				checkPermission(action, actionParameter, cmisObject);
			}
		}
	}

	/**
	 * @see eu.trade.repo.security.Authorization#checkPermission(eu.trade.repo.security.CustomSecured.CustomAction, java.util.Map)
	 */
	@Override
	@Transactional
	public void checkPermission(CustomAction action, Map<ActionParameter, String> actionParameters) {
		switch (action) {
			case LOGIN:
				// This is only used to force the user to be authenticated, so no additional checks are needed.
				break;
			case ADMIN:
				if (!callContextHolder.isAdmin()) {
					throw new CmisPermissionDeniedException("The user is not authorized to proceed with the action. Admin user is required.");
				}
				break;
			case CREATE_DOCUMENT_FROM_SOURCE :
				// Can create document (folder)
				checkPermission(Action.CAN_CREATE_DOCUMENT, subMap(actionParameters, ActionParameter.FOLDER));
				checkPermission(Action.CAN_GET_PROPERTIES, subMap(actionParameters, ActionParameter.OBJECT));
				// Only if the object has content stream
				String cmisObjectId = actionParameters.get(ActionParameter.OBJECT);
				CMISObject cmisObject = getCmisObject(cmisObjectId);
				if (cmisObject.hasContentStream()) {
					checkPermission(Action.CAN_GET_CONTENT_STREAM, subMap(actionParameters, ActionParameter.OBJECT));
				}
				break;
		}
	}


	/**
	 * @see eu.trade.repo.security.Security#getCallContextHolder()
	 */
	@Override
	public CallContextHolder getCallContextHolder() {
		return callContextHolder;
	}

	/**
	 * @see eu.trade.repo.security.Security#getAccessControl()
	 */
	@Override
	public AccessControl getAccessControl() {
		return accessControl;
	}

	/**
	 * @see eu.trade.repo.util.Cleanable#clean()
	 */
	@Override
	public void clean() {
		permissionCache.clean();
		permissionMappingCache.clean();
	}

	/**
	 * @see eu.trade.repo.security.Security#clean(java.lang.String)
	 */
	@Override
	public void clean(String repositoryId) {
		permissionCache.clean(repositoryId);
		permissionMappingCache.clean(repositoryId);
	}

	private Map<ActionParameter, String> subMap(Map<ActionParameter, String> actionParameters, ActionParameter... includedActionParameters) {
		Map<ActionParameter, String> subMap = new HashMap<>();
		for (ActionParameter actionParameter : includedActionParameters) {
			subMap.put(actionParameter, actionParameters.get(actionParameter));
		}
		return subMap;
	}

	private void checkActionParameters(Action action, Map<ActionParameter, String> actionParameters) {
		Set<ActionParameter> definedActionParameters = ActionMap.getActionParameters(action);
		if (!checkActionParameters(definedActionParameters, actionParameters)) {
			throw new IllegalArgumentException(String.format("The actual parameters %s do not match the action defined parameters %s.", actionParameters, definedActionParameters));
		}
	}

	private boolean checkActionParameters(Set<ActionParameter> definedActionParameters, Map<ActionParameter, String> actionParameters) {
		int definedSize = definedActionParameters.size();
		int size = actionParameters.size();
		if (definedSize != size) {
			return false;
		}
		if (size == 1) {
			return checkActionParameter(definedActionParameters, actionParameters);
		}

		return isEquals(definedActionParameters, actionParameters);
	}

	private boolean isEquals(Set<ActionParameter> definedActionParameters, Map<ActionParameter, String> actionParameters) {
		for (ActionParameter actionParameter : definedActionParameters) {
			if (!actionParameters.containsKey(actionParameter)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param definedActionParameters {@link Set<ActionParameter>} The set size must be one.
	 * @param actionParameters {@link Map<ActionParameter, String>>}
	 * @return boolean
	 */
	private boolean checkActionParameter(Set<ActionParameter> definedActionParameters, Map<ActionParameter, String> actionParameters) {
		ActionParameter definedActionParameter = definedActionParameters.iterator().next();
		if (actionParameters.containsKey(definedActionParameter)) {
			return true;
		}
		if (actionParameters.containsKey(ActionParameter.DEFAULT)) {
			actionParameters.put(definedActionParameter, actionParameters.get(ActionParameter.DEFAULT));
			actionParameters.remove(ActionParameter.DEFAULT);
			return true;
		}
		return false;
	}

	private Map<Action, String> getEligibleActions(CMISObject cmisObject) {
		String baseTypeIdValue = cmisObject.getObjectType().getBase().getCmisId();
		BaseTypeId baseTypeId = BaseTypeId.fromValue(baseTypeIdValue);
		Map<Action, String> eligibleActions = ActionMap.getAllowableActions(baseTypeId);
		removeNotElegibleActions(cmisObject, eligibleActions);
		return eligibleActions;
	}

	private void removeNotElegibleActions(CMISObject cmisObject, Map<Action, String> eligibleActions) {
		ObjectType objectType = cmisObject.getObjectType();
		Repository repository = objectType.getRepository();
		if (cmisObject.isFolder()) {
			removeFolderNotElegibleActions(cmisObject, repository, eligibleActions);
		}
		else {
			removeNonFolderNotElegibleActions(cmisObject, repository, objectType, eligibleActions);
		}
		removeCommonNotElegibleActions(repository, objectType, eligibleActions);
	}

	private void removeFolderNotElegibleActions(CMISObject cmisObject, Repository repository, Map<Action, String> eligibleActions) {
		if (cmisObject.isRootFolder()) {
			eligibleActions.remove(Action.CAN_GET_FOLDER_PARENT);
			eligibleActions.remove(Action.CAN_MOVE_OBJECT);
			eligibleActions.remove(Action.CAN_DELETE_OBJECT);
		}
		if (cmisObject.hasChildren()) {
			eligibleActions.remove(Action.CAN_DELETE_OBJECT);
		}

		if (!repository.getGetDescendants() && !repository.getGetFolderTree()) {
			eligibleActions.remove(Action.CAN_GET_DESCENDANTS);
		}
	}

	private void removeNonFolderNotElegibleActions(CMISObject cmisObject, Repository repository, ObjectType objectType, Map<Action, String> eligibleActions) {
		if (!objectType.isFileable() || !repository.getMultifiling()) {
			eligibleActions.remove(Action.CAN_ADD_OBJECT_TO_FOLDER);
			eligibleActions.remove(Action.CAN_REMOVE_OBJECT_FROM_FOLDER);
		}
		else if (!cmisObject.hasParents() || cannotUnfileLastParent(cmisObject, repository)) {
			eligibleActions.remove(Action.CAN_REMOVE_OBJECT_FROM_FOLDER);
		}

		if (!cmisObject.hasParents()) {
			eligibleActions.remove(Action.CAN_MOVE_OBJECT);
		}

		if (cmisObject.isDocument()) {
			removeDocumentNotElegibleActions(cmisObject, repository, objectType, eligibleActions);
		}
	}

	private boolean cannotUnfileLastParent(CMISObject cmisObject, Repository repository) {
		return cmisObject.getParentsSize() == 1 && (!repository.getUnfiling() || allAclAreInherited(cmisObject));
	}

	private boolean allAclAreInherited(CMISObject cmisObject) {
		for (Acl acl : cmisObject.getAcls()) {
			if (acl.getIsDirect()) {
				return false;
			}
		}
		return true;
	}

	private void removeDocumentNotElegibleActions(CMISObject cmisObject, Repository repository, ObjectType objectType, Map<Action, String> eligibleActions) {
		// TODO: Version 1.1 update properties capability
		removeStreamNotElegibleActions(cmisObject, repository, objectType, eligibleActions);
		removeVersionNotElegibleActions(cmisObject, repository, objectType, eligibleActions);
	}

	private void removeStreamNotElegibleActions(CMISObject cmisObject, Repository repository, ObjectType objectType, Map<Action, String> eligibleActions) {
		switch  (objectType.getContentStreamAllowed()) {
			case ALLOWED:
				if (!cmisObject.hasContentStream()) {
					eligibleActions.remove(Action.CAN_GET_CONTENT_STREAM);
					eligibleActions.remove(Action.CAN_DELETE_CONTENT_STREAM);
				}
				break;
			case NOTALLOWED:
				eligibleActions.remove(Action.CAN_GET_CONTENT_STREAM);
				eligibleActions.remove(Action.CAN_SET_CONTENT_STREAM);
				eligibleActions.remove(Action.CAN_DELETE_CONTENT_STREAM);
				break;
			case REQUIRED:
				eligibleActions.remove(Action.CAN_DELETE_CONTENT_STREAM);
				break;
		}

		switch (repository.getContentStreamUpdatability()) {
			case ANYTIME :
				// Empty
				break;
			case NONE :
				eligibleActions.remove(Action.CAN_SET_CONTENT_STREAM);
				eligibleActions.remove(Action.CAN_DELETE_CONTENT_STREAM);
				break;
			case PWCONLY :
				if (!cmisObject.isPwc()) {
					eligibleActions.remove(Action.CAN_SET_CONTENT_STREAM);
					eligibleActions.remove(Action.CAN_DELETE_CONTENT_STREAM);
				}
				break;
		}
	}

	private void removeVersionNotElegibleActions(CMISObject cmisObject, Repository repository, ObjectType objectType, Map<Action, String> eligibleActions) {
		if (objectType.isVersionable()) {
			if (cmisObject.isPwc()) {
				eligibleActions.remove(Action.CAN_CHECK_OUT);
				if (!repository.getPwcUpdatable()) {
					// The Pwc cannot be updated through object services
					eligibleActions.remove(Action.CAN_UPDATE_PROPERTIES);
					eligibleActions.remove(Action.CAN_MOVE_OBJECT);
					eligibleActions.remove(Action.CAN_DELETE_OBJECT);
					eligibleActions.remove(Action.CAN_SET_CONTENT_STREAM);
					eligibleActions.remove(Action.CAN_DELETE_CONTENT_STREAM);
				}
			}
			else {
				// Only Pwc can be checkin or cancelled.
				eligibleActions.remove(Action.CAN_CHECK_IN);
				eligibleActions.remove(Action.CAN_CANCEL_CHECK_OUT);
				// Versioning Trade way
				if (!cmisObject.<Boolean>getPropertyValue(PropertyIds.IS_LATEST_VERSION) || cmisObject.isVersionSeriesCheckout()) {
					eligibleActions.remove(Action.CAN_CHECK_OUT);
					eligibleActions.remove(Action.CAN_UPDATE_PROPERTIES);
					eligibleActions.remove(Action.CAN_SET_CONTENT_STREAM);
					eligibleActions.remove(Action.CAN_DELETE_CONTENT_STREAM);
				}
			}
		}
		else {
			eligibleActions.remove(Action.CAN_CHECK_OUT);
			eligibleActions.remove(Action.CAN_CHECK_IN);
			eligibleActions.remove(Action.CAN_CANCEL_CHECK_OUT);
		}
	}

	private void removeCommonNotElegibleActions(Repository repository, ObjectType objectType, Map<Action, String> eligibleActions) {
		if (repository.getRenditions().equals(CapabilityRenditions.NONE)) {
			eligibleActions.remove(Action.CAN_GET_RENDITIONS);
		}

		if (!objectType.isControllablePolicy()) {
			eligibleActions.remove(Action.CAN_APPLY_POLICY);
			eligibleActions.remove(Action.CAN_REMOVE_POLICY);
			eligibleActions.remove(Action.CAN_GET_APPLIED_POLICIES);
		}

		if (!objectType.isControllableAcl()) {
			eligibleActions.remove(Action.CAN_APPLY_ACL);
		}

		switch (repository.getAcl()) {
			case MANAGE:
				// empty
				break;
				
			case NONE:
				eligibleActions.remove(Action.CAN_GET_ACL);
				eligibleActions.remove(Action.CAN_APPLY_ACL);
				break;
				
			case DISCOVER :
				eligibleActions.remove(Action.CAN_APPLY_ACL);
				break;
		}
	}

	private AllowableActions getAllowableActions(Set<Action> actions) {
		AllowableActionsImpl result = new AllowableActionsImpl();
		result.setAllowableActions(actions);
		return result;

	}

	private void checkPermission(Action action, ActionParameter actionParameter, CMISObject cmisObject) {
		if (cmisObject != null) {
			checkType(action, actionParameter, cmisObject);
			String dbKey = ActionMap.getDbKey(action, actionParameter);
			if (!isGrantedPermission(dbKey, cmisObject)) {
				LOG.warn("The user is not authorized to proceed with the action: {} for the object: {}", dbKey, cmisObject.getCmisObjectId());
				throw new CmisPermissionDeniedException("The user is not authorized to proceed with the action.");
			}
		}
		// TODO: else, we should throw an exception?
	}

	private boolean isGrantedPermission(String dbKey, CMISObject cmisObject) {
		if (callContextHolder.isAdmin() || cmisObject.getObjectType().getRepository().getAcl().equals(CapabilityAcl.NONE)) {
			// Admins are granted for all permissions.
			return true;
		}
		String repositoryId = cmisObject.getObjectType().getRepository().getCmisId();
		Set<String> grantingPermissions = permissionMappingCache.getGrantingPermissions(repositoryId, dbKey);
		List<Acl> objectAcls = getObjectAcls(cmisObject);
		if (objectAcls != null) {
			for (Acl acl : objectAcls) {
				Permission permission = acl.getPermission();
				if (grantingPermissions.contains(permission.getName())) {
					// Permission granted.
					return true;
				}
			}
		}
		return false;
	}

	private void checkType(Action action, ActionParameter actionParameter, CMISObject cmisObject) {
		String baseTypeIdValue = cmisObject.getObjectType().getBase().getCmisId();
		BaseTypeId baseTypeId = BaseTypeId.fromValue(baseTypeIdValue);
		Set<BaseTypeId> baseTypeIds = ActionMap.getBaseTypes(action, actionParameter);
		if (!baseTypeIds.contains(baseTypeId)) {
			throw new CmisInvalidArgumentException(String.format(TYPE_ERROR, action, actionParameter, cmisObject, baseTypeId, baseTypeIds));
		}
	}

	private List<Acl> getObjectAcls(CMISObject cmisObject) {
		// TODO: Evaluate performance in both scenarios (get always the object with all ACLs -> in the selector.getObject or leave it lazy and perform here an adhoc query: without caching.)
		// This is the worst approach: retrieve the object without ACL an then get all ACLs in other query
		Set<String> principalIds = callContextHolder.getPrincipalIds();
		List<Acl> filterdAcls = new ArrayList<>();
		for (Acl acl : cmisObject.getAcls()) {
			if (principalIds.contains(acl.getPrincipalId())) {
				filterdAcls.add(acl);
			}
		}
		return filterdAcls;
	}

	private CMISObject getCmisObject(String cmisObjectId) {
		return cmisObjectSelector.getCMISObject(callContextHolder.getRepositoryId(), cmisObjectId);
	}
}
