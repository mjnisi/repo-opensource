package eu.trade.repo.web.admin;

import static eu.trade.repo.util.Constants.*;

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.trade.repo.model.Repository;

@Controller
@Secured(DELETE_REPO)
public class DeleteController extends AdminController{

	@RequestMapping(value = "/delete/{repoId}")
	public String delete(Model model, @PathVariable String repoId){

		List<Repository> allRepositories = repositoryService.getAllRepositories();
		Repository repository = getSelectedRepository(repoId, allRepositories);
		setAdminPageAttributes(model, allRepositories, generateRepositoryTitle(repository), repoId, null);
		model.addAttribute("repository", repository);
		return "delete";
	}

	@RequestMapping(value = "/deleteConfirmed/{repoId}")
	public String deleteConfirmed(Model model, @PathVariable String repoId){

		repositoryService.deleteRepository(repoId);
		return "redirect:/admin";
	}
}
