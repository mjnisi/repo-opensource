package eu.trade.repo.web.admin;

import eu.trade.repo.model.Permission;
import eu.trade.repo.model.Repository;
import eu.trade.repo.service.util.PermissionsUtil;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static eu.trade.repo.util.Constants.*;

@Controller
@Secured(CHANGE_REPO_PERMISSIONS)
public class PermissionsController extends AdminController{

	@RequestMapping(value = "/permissions/{repoId}")
	public String sessions(Model model, @PathVariable String repoId){
		List<Repository> allRepositories = repositoryService.getAllRepositories();
		setAdminPageAttributes(model, allRepositories, generateRepositoryTitle(getSelectedRepository(repoId, allRepositories)), repoId, null);

		model.addAttribute("permissionsTree", PermissionsUtil.permissionsAsTreeMap(repositoryService.getPermission("cmis:all", repoId), repositoryService.getRepositoryPermissions(repoId)));

		return "permissions";
	}

	@RequestMapping(value = "/permissions/rename/{repoId}", method = RequestMethod.POST)
	public String renamePermission(Model model, @PathVariable String repoId,
									  @RequestParam(value = "permissionName", required=true) String currentName,
									  @RequestParam(value = "new-permission-name", required=true) String newName,
									  @RequestParam(value = "new-permission-description", required=true) String newDescription){

		repositoryService.updatePermission(currentName, newName, newDescription, repoId);
		return "redirect:/admin/permissions/" + repoId;
	}

	@RequestMapping(value = "/permissions/create/{repoId}", method = RequestMethod.POST)
	public String createNewPermission(Model model, @PathVariable String repoId,
									  @RequestParam(value = "parentId", required=true) String parentId,
									  @RequestParam(value = "permission-name", required=true) String name,
									  @RequestParam(value = "permission-description", required=true) String description){

		Permission newPermission = new Permission(name, description);
		newPermission.setParent(repositoryService.getPermission(parentId, repoId));
		repositoryService.createPermission(newPermission, repoId);

		return "redirect:/admin/permissions/" + repoId;
	}

	@RequestMapping(value = "/permissions/delete/{repoId}", method = RequestMethod.POST)
	public String deletePermission(Model model, @PathVariable String repoId,
									  @RequestParam(value = "permissionToDelete", required=true) String permissionToDelete){

		repositoryService.deletePermission(permissionToDelete, repoId);

		return "redirect:/admin/permissions/" + repoId;
	}
}
