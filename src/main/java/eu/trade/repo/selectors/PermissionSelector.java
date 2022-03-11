package eu.trade.repo.selectors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.trade.repo.model.Permission;

public class PermissionSelector extends BaseSelector {

	/**
	 * Returns all repository's permissions mapped by name.
	 * 
	 * @param repositoryId {@link String} The repositoryId.
	 * @return {@link Map<String, Permission>} All repository's permissions mapped by name.
	 */
	public Map<String, Permission> getAllPermissionsByName(String repositoryId) {
		Map<String, Permission> permissionsMap = new HashMap<>();
		for (Permission permission : getAllPermissions(repositoryId)) {
			permissionsMap.put(permission.getName(), permission);
		}
		return permissionsMap;
	}

	public List<Permission> getAllPermissions (String repositoryId) {
		return getEntityManager()
				.createNamedQuery("permission.all", Permission.class)
				.setParameter("repositoryId", repositoryId)
				.getResultList();
	}

	public Permission getPermission(String name, String repositoryId) {
		// TODO [porrjai] Review if it is needed and also if there is a unique restriction on the name... Probably the permission name is unique only within a repository.
		return getEntityManager()
				.createNamedQuery("permission.by_name", Permission.class)
				.setParameter("name", name)
				.setParameter("repositoryId", repositoryId)
				.getSingleResult();
	}
}
