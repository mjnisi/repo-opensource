package eu.trade.repo.web.admin;

import static eu.trade.repo.util.Constants.CHANGE_REPO_MAPPINGS;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.trade.repo.model.Permission;
import eu.trade.repo.model.PermissionMapping;
import eu.trade.repo.model.Repository;
import eu.trade.repo.service.util.PermissionsUtil;

@Controller
@Secured(CHANGE_REPO_MAPPINGS)
public class MappingsController extends AdminController {

	public static class PermissionMappingMap {
		private final Set<String> keys = new LinkedHashSet<>();
		private final Map<String, Map<Integer, Boolean>> permissionsByKey = new LinkedHashMap<>();

		/**
		 * @return the keys
		 */
		public Set<String> getKeys() {
			return keys;
		}


		/**
		 * @return the permissionsByKey
		 */
		public Map<String, Map<Integer, Boolean>> getPermissionsByKey() {
			return permissionsByKey;
		}


		private void add(String key, Integer permission) {
			keys.add(key);
			Map<Integer, Boolean> permissions = permissionsByKey.get(key);
			if (permissions == null) {
				permissions = new HashMap<>();
				permissionsByKey.put(key, permissions);
			}
			permissions.put(permission, Boolean.TRUE);
		}
	}

	public static final String SQUARE_BRACKETS_CONTENT = "\\[(.*?)\\]";
	public static final String SPLIT_CHARACTER = "\\[";

	@RequestMapping(value = "/mappings/{repoId}")
	public String sessions(Model model, @PathVariable String repoId) {
		List<Permission> allPermissions = repositoryService.getRepositoryPermissions(repoId);
		model.addAttribute("allPermissions", allPermissions);

		List<Repository> allRepositories = repositoryService.getAllRepositories();
		List<PermissionMapping> permissionMappings = repositoryService.getRepositoryPermissionsMappings(repoId);
		Map<Permission, Collection<Permission>> permissionsWithChildren = PermissionsUtil.buildParentWithAllDescendantsChildren(allPermissions);
		model.addAttribute("permissionMappings", getPermissionMappingMap(permissionMappings));
		model.addAttribute("permissionsWithChildren", permissionsWithChildren);
		setAdminPageAttributes(model, allRepositories, generateRepositoryTitle(getSelectedRepository(repoId, allRepositories)), repoId, null);
		return "mappings";
	}

	private PermissionMappingMap getPermissionMappingMap(List<PermissionMapping> permissionMappings) {
		PermissionMappingMap permissionMappingMap = new PermissionMappingMap();
		for (PermissionMapping permissionMapping : permissionMappings) {
			permissionMappingMap.add(permissionMapping.getKey(), permissionMapping.getPermission().getId());
		}
		return permissionMappingMap;
	}

	@RequestMapping(value = "/mappings/update/{repoId}", method = RequestMethod.POST)
	public String updateMappings(Model model, @PathVariable String repoId, @RequestParam(value = "permissionMapping[]") String[] myParams) {

		Map<Integer, Permission> permissionsIdMap = PermissionsUtil.buildPermissionIdMap(repositoryService.getRepositoryPermissions(repoId));
		Set<PermissionMapping> permissionMappings = new HashSet<>();

		Pattern bracketContentPattern = Pattern.compile(SQUARE_BRACKETS_CONTENT);
		for (String parameter : myParams) {
			Matcher m = bracketContentPattern.matcher(parameter);
			Permission permission = null;
			String key = null;
			if (m.find()) {
				String permissionId = m.group(1);
				permission = permissionsIdMap.get(Integer.parseInt(permissionId));
			}
			key = parameter.split(SPLIT_CHARACTER)[0];
			if (key != null && permission != null) {
				permissionMappings.add(new PermissionMapping(key, permission));
			}
		}
		if (!permissionMappings.isEmpty()) {
			repositoryService.setPermissionMappings(permissionMappings, repoId);
		}
		return "redirect:/admin/mappings/" + repoId;
	}

}
