package eu.trade.repo.web.admin;

import static eu.trade.repo.util.Constants.CHANGE_REPO_MAPPINGS;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.Repository;

@Controller
@Secured(CHANGE_REPO_MAPPINGS)
public class PoliciesController extends AdminController {

	
	@RequestMapping(value = "/policies/{repoId}")
	public String policies(Model model, @PathVariable String repoId) {
		Map<ObjectType, Boolean> allPolicies = repositoryService.getPolicies(repoId);
		model.addAttribute("allPolicies", allPolicies);

		List<Repository> allRepositories = repositoryService.getAllRepositories();
		setAdminPageAttributes(model, allRepositories, generateRepositoryTitle(getSelectedRepository(repoId, allRepositories)), repoId, null);
		return "policies";
	}



	@RequestMapping(value = "/policies/update/{repoId}", method = RequestMethod.POST)
	public String updatePolicies(Model model, @PathVariable String repoId, 
			@RequestParam(value = "enabledPolicies[]", required=false) String[] enabledPolicies) {
		
		Set<String> enabledPoliciesSet = new HashSet<String>();
		if(enabledPolicies != null) { 
			enabledPoliciesSet.addAll(Arrays.asList(enabledPolicies));
		}
		
		repositoryService.setEnabledPolicies(repoId, enabledPoliciesSet);
		
		return "redirect:/admin/policies/" + repoId;
	}

}
