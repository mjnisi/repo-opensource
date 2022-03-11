package eu.trade.repo.service.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.trade.repo.model.Permission;

public final class PermissionsUtil {

	public static final String CMIS_ALL = "cmis:all";
	public static final String CMIS_WRITE = "cmis:write";
	public static final String CMIS_READ = "cmis:read";

	/**
	 * Hide utility class
	 */
	private PermissionsUtil() {
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map permissionsAsTreeMap(Permission root, Collection<Permission> permissions) {
		
		Map rootNode = new HashMap();
		
		Map<Permission, Collection<Permission>> permissionListMap = buildPermissionsByParentMap(permissions);
		
		rootNode.put("name", root.getName());
		rootNode.put("children", getChildren(root, permissionListMap));
		
		return rootNode;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static List<Map> getChildren(Permission element, Map<Permission, Collection<Permission>> permissionListMap) {
		
		List<Map> childNodes = new ArrayList<>();
		
		Collection<Permission> children = permissionListMap.get(element);
		
		if(children != null) {
			for(Permission p : children) {
				Map node = new HashMap();
				node.put("name", p.getName());
				node.put("children", getChildren(p, permissionListMap));
				childNodes.add(node);
			}
		}
		
		return childNodes;
	}

	public static Map<Integer, Permission> buildPermissionIdMap(Collection<Permission> allPermissions) {
		Map<Integer, Permission> permissionsMap = new HashMap<>();

		for (Permission permission : allPermissions) {
			permissionsMap.put(permission.getId(), permission);
		}

		return permissionsMap;
	}

	public static Map<Permission, Collection<Permission>> buildPermissionsByParentMap(Collection<Permission> allPermissions) {
		Map<Permission, Collection<Permission>> permissionsMap = new HashMap<>();

		for (Permission permission : allPermissions) {
			Collection<Permission> permissions = permissionsMap.get(permission.getParent());
			if (permissions == null) {
				permissions = new ArrayList<>();
			}
			permissions.add(permission);
			permissionsMap.put(permission.getParent(), permissions);
		}

		return permissionsMap;
	}

	public static Map<Permission, Collection<Permission>> buildParentWithAllDescendantsChildren(Collection<Permission> allPermissions) {
		Map<Permission, Collection<Permission>> permissionCollectionMap = buildPermissionsByParentMap(allPermissions);

		for (Map.Entry<Permission, Collection<Permission>> permissionCollectionEntry : permissionCollectionMap.entrySet()) {
			Permission key = permissionCollectionEntry.getKey();
			if (key != null) {
				Permission parent = key.getParent();
				Collection<Permission> permissions = permissionCollectionEntry.getValue();
				fillParentList(permissionCollectionMap, parent, permissions);
			}
		}
		return permissionCollectionMap;
	}

	private static void fillParentList(Map<Permission, Collection<Permission>> permissionCollectionMap, Permission parent, Collection<Permission> permissions) {
		if (parent != null) {
			permissionCollectionMap.get(parent).addAll(permissions);
			fillParentList(permissionCollectionMap, parent.getParent(), permissionCollectionMap.get(parent));
		}
	}

}
