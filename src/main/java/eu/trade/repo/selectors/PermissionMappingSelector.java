package eu.trade.repo.selectors;

import java.util.List;

import eu.trade.repo.model.PermissionMapping;
import eu.trade.repo.util.Constants;

public class PermissionMappingSelector extends BaseSelector {

	private static final String REPO_ID = "repositoryId";
	private static final String PERMISSION_NAME = "permissionName";

	public List<PermissionMapping> getPermissionMappings () {
		return getEntityManager()
				.createNamedQuery("permissionMapping.all", PermissionMapping.class)
				.getResultList();
	}

	public List<PermissionMapping> loadRepositoryPermissionMappings (String repositoryId) {
		return getEntityManager()
				.createNamedQuery("permissionMapping.load_by_repocmisId", PermissionMapping.class)
				.setParameter(REPO_ID, repositoryId)
				.getResultList();
	}

	public List<PermissionMapping> getPermissionMappings (String permissionName) {
		return getEntityManager()
				.createNamedQuery("permissionMapping.by_permissionName", PermissionMapping.class)
				.setParameter(PERMISSION_NAME, permissionName)
				.getResultList();
	}

	public List<PermissionMapping> getRepositoryPermissionMappingsWithPermission (String repositoryId) {
		return getEntityManager()
				.createNamedQuery("permissionMapping.by_repo_with_permission", PermissionMapping.class)
				.setParameter(REPO_ID, repositoryId)
				.getResultList();
	}

	public List<PermissionMapping> getREADPermissionMappings () {
		return getPermissionMappings(Constants.CMIS_READ);
	}

	public List<PermissionMapping> getWRITEPermissionMappings () {
		return getPermissionMappings(Constants.CMIS_WRITE);
	}

	public List<PermissionMapping> getALLPermissionMappings () {
		return getPermissionMappings(Constants.CMIS_ALL);
	}
}
