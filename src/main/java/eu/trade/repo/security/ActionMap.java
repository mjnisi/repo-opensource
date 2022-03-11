package eu.trade.repo.security;

import static eu.trade.repo.util.Constants.BASE_TYPE_CMIS_10;
import static eu.trade.repo.util.Constants.CMIS_READ;
import static eu.trade.repo.util.Constants.CMIS_WRITE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;

import eu.trade.repo.model.ActionParameter;

public final class ActionMap {

	private static final class DbKeyTypes {
		private final String dbKey;
		private final Set<BaseTypeId> baseTypeIds;

		private DbKeyTypes(String dbKey, Set<BaseTypeId> baseTypeIds) {
			this.dbKey = dbKey;
			this.baseTypeIds = baseTypeIds;
		}

		/**
		 * @return the dbKey
		 */
		private String getDbKey() {
			return dbKey;
		}

		/**
		 * @return the baseTypeIds
		 */
		private Set<BaseTypeId> getBaseTypeIds() {
			return baseTypeIds;
		}
	}

	/*the key is the db_key and the value is the default permission*/
	private static final Map<String, String> DB_KEYS = new LinkedHashMap<String, String>();
	private static final Map<BaseTypeId, Map<Action, String>> TYPE_ALLOWABLE_ACTION_MAP = new HashMap<>();
	private static final Map<Action, Map<ActionParameter, DbKeyTypes>> ACTION_PARAMETERMAP = new HashMap<>();

	static {
		// Navigation Services: getDescendants (getFolderTree 1.1)
		// PermissionMapping.CAN_GET_DESCENDENTS_FOLDER Not correctly written - is descendants. See https://issues.apache.org/jira/browse/CMIS-662
		add(Action.CAN_GET_DESCENDANTS, ActionParameter.FOLDER, true, PermissionMapping.CAN_GET_DESCENDENTS_FOLDER, CMIS_READ, BaseTypeId.CMIS_FOLDER);

		// Navigation Services: getChildren
		add(Action.CAN_GET_CHILDREN, ActionParameter.FOLDER, true, PermissionMapping.CAN_GET_CHILDREN_FOLDER, CMIS_READ, BaseTypeId.CMIS_FOLDER);

		// Navigation Services: getFolderParent
		add(Action.CAN_GET_FOLDER_PARENT, ActionParameter.OBJECT, true, PermissionMapping.CAN_GET_FOLDER_PARENT_OBJECT, CMIS_READ, BaseTypeId.CMIS_FOLDER);

		/*
		 * TODO important!!! **********************************************************************************
		 * 
		 * When migrating to CMIS 1.1 the method getObjectParents() is not allowed to be used with folders.
		 * The AtomPub binding is using the getObjectParents() with folders and is not allowed any more.
		 * The bug CMIS-835 it's fixed in trunk branch but the code is not in the release version.
		 * This change will be introduced in version 0.13.0, then we could restore the CMIS 1.1 configuration  
		 */		
		
		// Navigation Services: getObjectParents
		// TODO: For the 1.1 the allowed types are Document, Policy and Item, but no Folder.
		add(Action.CAN_GET_OBJECT_PARENTS, ActionParameter.FOLDER, true, PermissionMapping.CAN_GET_PARENTS_FOLDER, CMIS_READ, 
				BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_POLICY, BaseTypeId.CMIS_ITEM,
				BaseTypeId.CMIS_FOLDER //<-- remove this line when CMIS-835 is in release version, provably 0.13.0
				);

		
		
		// Object Services: createDocument
		add(Action.CAN_CREATE_DOCUMENT, ActionParameter.FOLDER, true, PermissionMapping.CAN_CREATE_DOCUMENT_FOLDER, CMIS_READ, BaseTypeId.CMIS_FOLDER);

		// Object Services: createFolder
		add(Action.CAN_CREATE_FOLDER, ActionParameter.FOLDER, true, PermissionMapping.CAN_CREATE_FOLDER_FOLDER, CMIS_READ, BaseTypeId.CMIS_FOLDER);

		// Object Services: createPolicy
		// CAN CREATE POLICY 1.1
		//Action.CAN_CREATE_POLICY missing in Chemistry OpenCMIS 0.11.0 (Florian Muller explained that is not in the lib because it's not in the schema)
		//add(Action.CAN_CREATE_POLICY, ActionParameter.FOLDER, true, PermissionMapping.CAN_CREATE_POLICY_FOLDER, CMIS_READ, BaseTypeId.CMIS_FOLDER);

		// Object Services: createRelationship
		add(Action.CAN_CREATE_RELATIONSHIP, ActionParameter.SOURCE, true, PermissionMapping.CAN_CREATE_RELATIONSHIP_SOURCE, CMIS_READ, BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_FOLDER, BaseTypeId.CMIS_POLICY, BaseTypeId.CMIS_ITEM);
		add(Action.CAN_CREATE_RELATIONSHIP, ActionParameter.TARGET, false, PermissionMapping.CAN_CREATE_RELATIONSHIP_TARGET, CMIS_READ, BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_FOLDER, BaseTypeId.CMIS_POLICY, BaseTypeId.CMIS_ITEM);

		// Object Services: getProperties (getObject, getObjectByPath 1.1). All object types.
		add(Action.CAN_GET_PROPERTIES, ActionParameter.OBJECT, true, PermissionMapping.CAN_GET_PROPERTIES_OBJECT, CMIS_READ, BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_FOLDER, BaseTypeId.CMIS_RELATIONSHIP, BaseTypeId.CMIS_POLICY, BaseTypeId.CMIS_ITEM);

		// Object Services: updateProperties. All object types.
		add(Action.CAN_UPDATE_PROPERTIES, ActionParameter.OBJECT, true, PermissionMapping.CAN_UPDATE_PROPERTIES_OBJECT, CMIS_WRITE, BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_FOLDER, BaseTypeId.CMIS_RELATIONSHIP, BaseTypeId.CMIS_POLICY, BaseTypeId.CMIS_ITEM);

		// Object Services: moveObject
		add(Action.CAN_MOVE_OBJECT, ActionParameter.OBJECT, true, PermissionMapping.CAN_MOVE_OBJECT, CMIS_WRITE, BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_FOLDER, BaseTypeId.CMIS_POLICY, BaseTypeId.CMIS_ITEM);
		add(Action.CAN_MOVE_OBJECT, ActionParameter.TARGET, false, PermissionMapping.CAN_MOVE_TARGET, CMIS_READ, BaseTypeId.CMIS_FOLDER);
		add(Action.CAN_MOVE_OBJECT, ActionParameter.SOURCE, false, PermissionMapping.CAN_MOVE_SOURCE, CMIS_READ, BaseTypeId.CMIS_FOLDER);

		// Object Services: deleteObject. All object types.
		add(Action.CAN_DELETE_OBJECT, ActionParameter.OBJECT, true, PermissionMapping.CAN_DELETE_OBJECT, CMIS_WRITE, BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_FOLDER, BaseTypeId.CMIS_RELATIONSHIP, BaseTypeId.CMIS_POLICY, BaseTypeId.CMIS_ITEM);

		// Object Services: getContentStream
		add(Action.CAN_GET_CONTENT_STREAM, ActionParameter.OBJECT, true, PermissionMapping.CAN_VIEW_CONTENT_OBJECT, CMIS_WRITE, BaseTypeId.CMIS_DOCUMENT);

		// Object Services: setContentStream
		add(Action.CAN_SET_CONTENT_STREAM, ActionParameter.DOCUMENT, true, PermissionMapping.CAN_SET_CONTENT_DOCUMENT, CMIS_WRITE, BaseTypeId.CMIS_DOCUMENT);

		// Object Services: deleteContentStream
		add(Action.CAN_DELETE_CONTENT_STREAM, ActionParameter.DOCUMENT, true, PermissionMapping.CAN_DELETE_CONTENT_DOCUMENT, CMIS_WRITE, BaseTypeId.CMIS_DOCUMENT);

		// Object Services: deleteTree
		add(Action.CAN_DELETE_TREE, ActionParameter.FOLDER, true, PermissionMapping.CAN_DELETE_TREE_FOLDER, CMIS_WRITE, BaseTypeId.CMIS_FOLDER);

		// Filing Services: addObjectToFolder
		add(Action.CAN_ADD_OBJECT_TO_FOLDER, ActionParameter.OBJECT, true, PermissionMapping.CAN_ADD_TO_FOLDER_OBJECT, CMIS_READ, BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_POLICY, BaseTypeId.CMIS_ITEM);
		add(Action.CAN_ADD_OBJECT_TO_FOLDER, ActionParameter.FOLDER, false, PermissionMapping.CAN_ADD_TO_FOLDER_FOLDER, CMIS_READ, BaseTypeId.CMIS_FOLDER);

		// Filing Services: removeObjectFromFolder
		add(Action.CAN_REMOVE_OBJECT_FROM_FOLDER, ActionParameter.OBJECT, true, PermissionMapping.CAN_REMOVE_FROM_FOLDER_OBJECT, CMIS_READ, BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_POLICY, BaseTypeId.CMIS_ITEM);
		add(Action.CAN_REMOVE_OBJECT_FROM_FOLDER, ActionParameter.FOLDER, false, PermissionMapping.CAN_REMOVE_FROM_FOLDER_FOLDER, CMIS_READ, BaseTypeId.CMIS_FOLDER);

		// Versioning Services: checkOut
		add(Action.CAN_CHECK_OUT, ActionParameter.DOCUMENT, true, PermissionMapping.CAN_CHECKOUT_DOCUMENT, CMIS_WRITE, BaseTypeId.CMIS_DOCUMENT);

		// Versioning Services: cancelCheckOut
		add(Action.CAN_CANCEL_CHECK_OUT, ActionParameter.DOCUMENT, true, PermissionMapping.CAN_CANCEL_CHECKOUT_DOCUMENT, CMIS_WRITE, BaseTypeId.CMIS_DOCUMENT);

		// Versioning Services: checkIn
		add(Action.CAN_CHECK_IN, ActionParameter.DOCUMENT, true, PermissionMapping.CAN_CHECKIN_DOCUMENT, CMIS_WRITE, BaseTypeId.CMIS_DOCUMENT);

		// Versioning Services: getAllVersions
		add(Action.CAN_GET_ALL_VERSIONS, ActionParameter.VERSION_SERIES, true, PermissionMapping.CAN_GET_ALL_VERSIONS_VERSION_SERIES, CMIS_READ, BaseTypeId.CMIS_DOCUMENT);

		// Relationship Services: getObjectRelationships
		add(Action.CAN_GET_OBJECT_RELATIONSHIPS, ActionParameter.OBJECT, true, PermissionMapping.CAN_GET_OBJECT_RELATIONSHIPS_OBJECT, CMIS_READ, BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_FOLDER, BaseTypeId.CMIS_POLICY, BaseTypeId.CMIS_ITEM);

		// Policy Services: applyPolicy. All object types.
		add(Action.CAN_APPLY_POLICY, ActionParameter.OBJECT, true, PermissionMapping.CAN_ADD_POLICY_OBJECT, CMIS_READ, BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_FOLDER, BaseTypeId.CMIS_POLICY, BaseTypeId.CMIS_RELATIONSHIP, BaseTypeId.CMIS_ITEM);
		add(Action.CAN_APPLY_POLICY, ActionParameter.POLICY, false, PermissionMapping.CAN_ADD_POLICY_POLICY, CMIS_READ, BaseTypeId.CMIS_POLICY);

		// Policy Services: removePolicy. All object types.
		add(Action.CAN_REMOVE_POLICY, ActionParameter.OBJECT, true, PermissionMapping.CAN_REMOVE_POLICY_OBJECT, CMIS_READ, BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_FOLDER, BaseTypeId.CMIS_POLICY, BaseTypeId.CMIS_RELATIONSHIP, BaseTypeId.CMIS_ITEM);
		add(Action.CAN_REMOVE_POLICY, ActionParameter.POLICY, false, PermissionMapping.CAN_REMOVE_POLICY_POLICY, CMIS_READ, BaseTypeId.CMIS_POLICY);

		// Policy Services: getAppliedPolicies. All object types.
		add(Action.CAN_GET_APPLIED_POLICIES, ActionParameter.OBJECT, true, PermissionMapping.CAN_GET_APPLIED_POLICIES_OBJECT, CMIS_READ, BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_FOLDER, BaseTypeId.CMIS_POLICY, BaseTypeId.CMIS_RELATIONSHIP, BaseTypeId.CMIS_ITEM);

		// ACL Services: getACL. All object types.
		add(Action.CAN_GET_ACL, ActionParameter.OBJECT, true, PermissionMapping.CAN_GET_ACL_OBJECT, CMIS_READ, BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_FOLDER, BaseTypeId.CMIS_POLICY, BaseTypeId.CMIS_RELATIONSHIP, BaseTypeId.CMIS_ITEM);

		// ACL Services: applyACL. All object types.
		add(Action.CAN_APPLY_ACL, ActionParameter.OBJECT, true, PermissionMapping.CAN_APPLY_ACL_OBJECT, CMIS_WRITE, BaseTypeId.CMIS_DOCUMENT, BaseTypeId.CMIS_FOLDER, BaseTypeId.CMIS_POLICY, BaseTypeId.CMIS_RELATIONSHIP, BaseTypeId.CMIS_ITEM);

		// Checks (present in the XSD and missing in the spec)
		check(Action.CAN_GET_FOLDER_TREE, Action.CAN_GET_RENDITIONS, Action.CAN_CREATE_ITEM);
	}

	// Utility constructor.
	private ActionMap() {
	}

	/**
	 * 
	 * @param action {@link Action}
	 * @param actionParameter {@link ActionParameter}
	 * @param mainParameter TODO
	 * @param dbKey {@link String}
	 * @param defaultPermission
	 * @param baseTypeIds {@link BaseTypeId[]} Not empty array.
	 */
	private static void add(Action action, ActionParameter actionParameter, boolean mainParameter, String dbKey, String defaultPermission, BaseTypeId... baseTypeIds) {
		DB_KEYS.put(dbKey, defaultPermission);
		Set<BaseTypeId> baseTypeIdsSet = new HashSet<BaseTypeId>(Arrays.asList(baseTypeIds));
		if (mainParameter) {
			for (BaseTypeId baseTypeId : baseTypeIdsSet) {
				addTypeAllowableAction(action, dbKey, baseTypeId);
			}
		}
		Map<ActionParameter, DbKeyTypes> parameters = ACTION_PARAMETERMAP.get(action);
		if (parameters == null) {
			parameters = new HashMap<ActionParameter, DbKeyTypes>();
			ACTION_PARAMETERMAP.put(action, parameters);
		}
		parameters.put(actionParameter, new DbKeyTypes(dbKey, baseTypeIdsSet));
	}

	private static void addTypeAllowableAction(Action action, String dbKey, BaseTypeId baseTypeId) {
		Map<Action, String> actionsDbKeys = TYPE_ALLOWABLE_ACTION_MAP.get(baseTypeId);
		if (actionsDbKeys == null) {
			actionsDbKeys = new HashMap<>();
			TYPE_ALLOWABLE_ACTION_MAP.put(baseTypeId, actionsDbKeys);
		}
		actionsDbKeys.put(action, dbKey);
	}

	private static void check(Action... excluded) {
		// Check all actions (except excluded are registered)
		List<Action> excludedList = Arrays.asList(excluded);
		Set<Action> added = ACTION_PARAMETERMAP.keySet();
		for (Action action : Action.values()) {
			if (excludedList.contains(action) == added.contains(action)) {
				throw new IllegalStateException("Review the actions maps for: " + action);
			}
		}
		// Check all base types are registered.
		Set<BaseTypeId> addedTypes = TYPE_ALLOWABLE_ACTION_MAP.keySet();
		for (BaseTypeId baseTypeId : BASE_TYPE_CMIS_10) {
			if (!addedTypes.contains(baseTypeId)) {
				throw new IllegalStateException("Review the type actions maps for: " + baseTypeId);
			}
		}
	}

	/**
	 * Returns the {@link ActionParameter} set related with the specified {@link Action}.
	 * 
	 * @param action {@link Action}
	 * @return {@link Set<ActionParameter>} The {@link ActionParameter} set related with the specified {@link Action}.
	 */
	public static Set<ActionParameter> getActionParameters(Action action) {
		Map<ActionParameter, DbKeyTypes> parameterMap = getParameterMap(action);
		return parameterMap.keySet();
	}

	/**
	 * Returns the set of base types defined for the pair action/actionParameter.
	 * 
	 * @param action {@link Action}
	 * @param actionParameter {@link ActionParameter}
	 * @return {@link Set<BaseTypeId>} The set of base types defined for the pair action/actionParameter.
	 */
	public static Set<BaseTypeId> getBaseTypes(Action action, ActionParameter actionParameter) {
		DbKeyTypes dbKeyTypes = getDbKeyTypes(action, actionParameter);
		return dbKeyTypes.getBaseTypeIds();
	}

	/**
	 * Returns the set of allowable actions related with the specified {@link BaseTypeId}.
	 * 
	 * @param baseTypeId {@link BaseTypeId} Cmis base type.
	 * @return {@link Map} The set of allowable actions related with the specified {@link BaseTypeId}.
	 */
	public static Map<Action, String> getAllowableActions(BaseTypeId baseTypeId) {
		return new HashMap<>(TYPE_ALLOWABLE_ACTION_MAP.get(baseTypeId));
	}

	/**
	 * Returns the dbKey related with the pair action/actionParameter.
	 * 
	 * @param action {@link Action}
	 * @param actionParameter {@link ActionParameter}
	 * @return {@link String}  the dbKey related with the pair action/actionParameter.
	 */
	public static String getDbKey(Action action, ActionParameter actionParameter) {
		DbKeyTypes dbKeyTypes = getDbKeyTypes(action, actionParameter);
		return dbKeyTypes.getDbKey();
	}

	private static Map<ActionParameter, DbKeyTypes> getParameterMap(Action action) {
		Map<ActionParameter, DbKeyTypes> parameterMap = ACTION_PARAMETERMAP.get(action);
		if (parameterMap == null) {
			throw new IllegalArgumentException("There is no defined set of parameter for the action: " + action);
		}
		return parameterMap;
	}

	private static DbKeyTypes getDbKeyTypes(Action action, ActionParameter actionParameter) {
		Map<ActionParameter, DbKeyTypes> parameterMap = getParameterMap(action);
		DbKeyTypes dbKeyTypes = parameterMap.get(actionParameter);
		if (dbKeyTypes == null) {
			throw new IllegalArgumentException("There is no defined set of types for this action: " + action + " and parameter: " + actionParameter);
		}
		return dbKeyTypes;
	}

	/**
	 * Returns the db keys Map ordered as in the CMIS 1.1 definition.
	 * The key is the key and the value es the default permission.
	 * 
	 * @return {@link Map<String, String>} The db keys list ordered as in the CMIS 1.1 definition.
	 */
	public static Map<String, String> getDbKeys() {
		return DB_KEYS;
	}
}
