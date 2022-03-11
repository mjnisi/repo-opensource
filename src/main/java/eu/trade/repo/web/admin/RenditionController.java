package eu.trade.repo.web.admin;

import eu.trade.repo.model.Repository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class RenditionController extends AdminController{

	@RequestMapping(value = "/renditions/{repoId}")
	public String capabilities(Model model, @PathVariable String repoId) {

		List<Repository> allRepositories = repositoryService.getAllRepositories();
		Repository repository = getSelectedRepository(repoId, allRepositories);
		setAdminPageAttributes(model, allRepositories, generateRepositoryTitle(repository), repoId, null);

		return "renditions";
	}

}
