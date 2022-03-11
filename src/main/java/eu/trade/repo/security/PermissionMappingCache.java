package eu.trade.repo.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionMappingDataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.model.Permission;
import eu.trade.repo.model.PermissionMapping;
import eu.trade.repo.selectors.PermissionMappingSelector;
import eu.trade.repo.util.Cleanable;

/**
 * Cache for the {@link PermissionMapping}
 * 
 * @author porrjai
 */
class PermissionMappingCache implements Cleanable {

	private static final Logger LOG = LoggerFactory.getLogger(PermissionMappingCache.class);

	@Autowired
	private PermissionMappingSelector permissionMappingSelector;

	private final Map<String, List<org.apache.chemistry.opencmis.commons.data.PermissionMapping>> permissionMappingCache = new HashMap<>();
	private final Map<String, Map<String, Set<String>>> grantingPermissionsCache = new HashMap<>();

	/**
	 * Returns the granting permission id set for the specified action. I.e. the expanded list of permissions that grant the key action.
	 * 
	 * @param repositoryId {@link String} The repostoryId
	 * @param key {@link String} The action db key.
	 * @return {@link Set<String>} The granting permission id set for the specified action.
	 */
	Set<String> getGrantingPermissions(String repositoryId, String key) {
		Map<String, Set<String>> permissionMappings = getGrantingPermissionCache(repositoryId);
		return permissionMappings.get(key);
	}

	/**
	 * Returns the list of permission mapping using the chemisry interface.
	 * 
	 * @param repositoryId {@link String} The repostoryId
	 * @return {@link List} The list of permission mapping using the chemisry interface.
	 */
	List<org.apache.chemistry.opencmis.commons.data.PermissionMapping> getPermissionMappings(String repositoryId) {
		List<org.apache.chemistry.opencmis.commons.data.PermissionMapping> permissionMappings =  permissionMappingCache.get(repositoryId);
		if (permissionMappings == null) {
			initPermissionMappings(repositoryId);
			permissionMappings =  permissionMappingCache.get(repositoryId);
		}
		return permissionMappings;
	}

	private Map<String, Set<String>> getGrantingPermissionCache(String repositoryId) {
		Map<String, Set<String>> permissionMappings = grantingPermissionsCache.get(repositoryId);
		if (permissionMappings == null) {
			initPermissionMappings(repositoryId);
			permissionMappings = grantingPermissionsCache.get(repositoryId);
		}
		return permissionMappings;
	}

	private synchronized void initPermissionMappings(String repositoryId) {
		Map<String, Set<String>> permissionMappings = grantingPermissionsCache.get(repositoryId);
		if (permissionMappings == null) {
			permissionMappings = new HashMap<>();
			Map<String, Set<String>> expandedByPermission = new HashMap<>();
			List<PermissionMapping> permissionMappingsFromDb = permissionMappingSelector.loadRepositoryPermissionMappings(repositoryId);
			for (PermissionMapping permissionMapping : permissionMappingsFromDb) {
				String key = permissionMapping.getKey();
				Set<String> permissions = permissionMappings.get(key);
				if (permissions == null) {
					permissions = new HashSet<>();
					permissionMappings.put(key, permissions);
				}
				permissions.addAll(addExpandedPermission(permissionMapping.getPermission(), expandedByPermission));
			}
			fillPermissionMappingCache(repositoryId, permissionMappings);
			grantingPermissionsCache.put(repositoryId, permissionMappings);
		}
	}

	private Set<String> addExpandedPermission(Permission permission, Map<String, Set<String>> expandedByPermission) {
		String permissionName = permission.getName();
		Set<String> expandedPermissions = expandedByPermission.get(permissionName);
		if (expandedPermissions == null) {
			Set<String> parentExpandedPermissions = getParentPermissions(permission, expandedByPermission);
			expandedPermissions = new HashSet<>(parentExpandedPermissions);
			expandedPermissions.add(permissionName);
			expandedByPermission.put(permissionName, expandedPermissions);
		}
		return expandedPermissions;
	}

	private Set<String> getParentPermissions(Permission permission, Map<String, Set<String>> expandedByPermission) {
		Set<String> expandedPermissions;
		Permission parentPermission = permission.getParent();
		if (parentPermission != null) {
			String parentPermissionName = parentPermission.getName();
			expandedPermissions = expandedByPermission.get(parentPermissionName);
			if (expandedPermissions == null) {
				expandedPermissions = addExpandedPermission(parentPermission, expandedByPermission);
			}
		}
		else {
			expandedPermissions = new HashSet<>();
		}
		return expandedPermissions;
	}

	private void fillPermissionMappingCache(String repositoryId, Map<String, Set<String>> permissionMappings) {
		List<org.apache.chemistry.opencmis.commons.data.PermissionMapping> permissionMappingsList = new ArrayList<>();
		for (Map.Entry<String, Set<String>> entry : permissionMappings.entrySet()) {
			PermissionMappingDataImpl permissionMapping = new PermissionMappingDataImpl();
			permissionMapping.setKey(entry.getKey());
			permissionMapping.setPermissions(new ArrayList<>(entry.getValue()));
			permissionMappingsList.add(permissionMapping);
		}
		permissionMappingCache.put(repositoryId, permissionMappingsList);
	}

	/**
	 * @see eu.trade.repo.util.Cleanable#clean()
	 */
	@Override
	public synchronized void clean() {
		LOG.info("Cleaning cache");
		permissionMappingCache.clear();
		grantingPermissionsCache.clear();
	}

	/**
	 * Cleans the security resources cached for the specified repository.
	 * 
	 * @param repositoryId {@link String} the repository id. Mandatory not null.
	 */
	public synchronized void clean(String repositoryId) {
		permissionMappingCache.put(repositoryId, null);
		grantingPermissionsCache.put(repositoryId, null);
	}
}
