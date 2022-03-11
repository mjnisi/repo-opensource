package eu.trade.repo.service.cmis.data.out;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.AclCapabilities;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.SupportedPermissions;

import eu.trade.repo.model.Repository;
import eu.trade.repo.util.Constants;

/**
 * Helper implementation class that provides the {@link AclCapabilities} interface from a list of {@link PermissionDefinition} and a list of {@link PermissionMapping}.
 * <p>
 * {@link SupportedPermissions}.BOTH and {@link AclPropagation}.OBJECTONLY are hardcoded.
 * 
 * @author porrjai
 */
public class AclCapabilitiesImpl extends NonExtensionsObject implements AclCapabilities {

	private static final long serialVersionUID = 1L;

	private final Repository repository;
	private final List<PermissionDefinition> permissionDefinitions;
	private final Map<String, PermissionMapping> permissionMappings;

	/**
	 * New instance.
	 * 
	 * @param repository {@link Repository} The repository
	 * @param permissionDefinitions
	 * @param permissionMappings
	 */
	AclCapabilitiesImpl(Repository repository, List<PermissionDefinition> permissionDefinitions, List<PermissionMapping> permissionMappings) {
		this.repository = repository;
		this.permissionDefinitions = permissionDefinitions;
		this.permissionMappings = getPermissionMappings(permissionMappings);
	}

	private Map<String, PermissionMapping> getPermissionMappings(List<PermissionMapping> permissionMappings) {
		Map<String, PermissionMapping> permissionMappingsMap = new HashMap<String, PermissionMapping>();
		for (PermissionMapping permissionMapping : permissionMappings) {
			permissionMappingsMap.put(permissionMapping.getKey(), permissionMapping);
		}
		return permissionMappingsMap;
	}

	/**
	 * Since the set of CMIS basic permission is mandatory in this implementation, Only {@link SupportedPermissions#BASIC} and {@link SupportedPermissions#BOTH} are supported
	 * depending on the list of {@link PermissionDefinition}.
	 * 
	 * @see org.apache.chemistry.opencmis.commons.data.AclCapabilities#getSupportedPermissions()
	 */
	@Override
	public SupportedPermissions getSupportedPermissions() {
		for (PermissionDefinition permissionDefinition : permissionDefinitions) {
			if (!Constants.CMIS_BASIC_PERMISSIONS.contains(permissionDefinition.getId())) {
				return SupportedPermissions.BOTH;
			}
		}
		return SupportedPermissions.BASIC;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.AclCapabilities#getAclPropagation()
	 */
	@Override
	public AclPropagation getAclPropagation() {
		return repository.getAclPropagation();
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.AclCapabilities#getPermissions()
	 */
	@Override
	public List<PermissionDefinition> getPermissions() {
		return permissionDefinitions;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.AclCapabilities#getPermissionMapping()
	 */
	@Override
	public Map<String, PermissionMapping> getPermissionMapping() {
		return permissionMappings;
	}
}
