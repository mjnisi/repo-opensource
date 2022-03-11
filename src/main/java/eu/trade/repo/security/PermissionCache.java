package eu.trade.repo.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionDefinitionDataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.model.Permission;
import eu.trade.repo.selectors.PermissionSelector;
import eu.trade.repo.util.Cleanable;

/**
 * Cache for {@link Permission}.
 * <p>
 * If a permission X has a parent Y, then the permission Y covers permission X, i.e. a user with the permission Y also has the permission X.
 * Based on this definition, the set of expanded permissions for a given permission is the permission itself plus all its ancestors.
 * 
 * @author porrjai
 */
class PermissionCache implements Cleanable {

	private static final Logger LOG = LoggerFactory.getLogger(PermissionCache.class);

	@Autowired
	private PermissionSelector permissionSelector;

	private final Map<String, List<PermissionDefinition>> permissionDefinitionsCache = new HashMap<>();
	private final Map<String, Map<String, Integer>> permissionCacheByName = new HashMap<>();
	private final Map<String, Set<String>> permissionCache = new HashMap<>();

	/**
	 * Returns the list of permission definitions for the specified repository.
	 * 
	 * @param repositoryId {@link String} The repository id.
	 * @return {@link List<PermissionDefinition>} the list of permission definitions for the specified repository.
	 */
	List<PermissionDefinition> getPermissionDefinitions(String repositoryId) {
		List<PermissionDefinition> permissionDefinitions = permissionDefinitionsCache.get(repositoryId);
		if (permissionDefinitions == null) {
			initPermissionCache(repositoryId);
			permissionDefinitions = permissionDefinitionsCache.get(repositoryId);
		}
		return permissionDefinitions;
	}

	/**
	 * Returns the set of permission names for the specified repository.
	 * 
	 * @param repositoryId {@link String} The repository id.
	 * @return {@link Set<String>} the set of permission names for the specified repository.
	 */
	Set<String> getPermissionNames(String repositoryId) {
		Set<String> permissionNames = permissionCache.get(repositoryId);
		if (permissionNames == null) {
			initPermissionCache(repositoryId);
			permissionNames = permissionCache.get(repositoryId);
		}
		return permissionNames;
	}

	Set<Integer> getPermissionsIds(String repositoryId, Set<String> permissions) {
		Set<Integer> permissionsIds = new HashSet<>();
		Map<String, Integer> permissionsIdsByName = getPermissionCacheByName(repositoryId);
		for (String permissionName : permissions) {
			permissionsIds.add(permissionsIdsByName.get(permissionName));
		}
		return permissionsIds;

	}

	private Map<String, Integer> getPermissionCacheByName(String repositoryId) {
		Map<String, Integer> permissionsIdsByName = permissionCacheByName.get(repositoryId);
		if (permissionsIdsByName == null) {
			initPermissionCache(repositoryId);
			permissionsIdsByName = permissionCacheByName.get(repositoryId);
		}
		return permissionsIdsByName;
	}

	private synchronized void initPermissionCache(String repositoryId) {
		List<PermissionDefinition> permissionDefinitions = permissionDefinitionsCache.get(repositoryId);
		if (permissionDefinitions == null) {
			permissionDefinitions = new ArrayList<>();
			Map<String, Integer> permissionIdsByName = new HashMap<>();
			Set<String> permissionNames = new HashSet<>();
			List<Permission> permissions = permissionSelector.getAllPermissions(repositoryId);
			for (Permission permission : permissions) {
				String name = permission.getName();
				permissionNames.add(name);
				permissionIdsByName.put(name, permission.getId());
				permissionDefinitions.add(getPermissionDefinition(permission));
			}
			permissionDefinitionsCache.put(repositoryId, permissionDefinitions);
			permissionCache.put(repositoryId, permissionNames);
			permissionCacheByName.put(repositoryId, permissionIdsByName);
		}
	}

	private PermissionDefinition getPermissionDefinition(Permission permission) {
		PermissionDefinitionDataImpl permissionDefinition = new PermissionDefinitionDataImpl();
		permissionDefinition.setDescription(permission.getDescription());
		permissionDefinition.setId(permission.getName());
		return permissionDefinition;
	}

	/**
	 * @see eu.trade.repo.util.Cleanable#clean()
	 */
	@Override
	public synchronized void clean() {
		LOG.info("Cleaning cache");
		permissionDefinitionsCache.clear();
		permissionCacheByName.clear();
		permissionCache.clear();
	}

	/**
	 * Cleans the security resources cached for the specified repository.
	 * 
	 * @param repositoryId {@link String} the repository id. Mandatory not null.
	 */
	public synchronized void clean(String repositoryId) {
		permissionDefinitionsCache.put(repositoryId, null);
		permissionCacheByName.put(repositoryId, null);
		permissionCache.put(repositoryId, null);
	}
}
