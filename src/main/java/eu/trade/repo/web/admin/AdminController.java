package eu.trade.repo.web.admin;


import eu.trade.repo.model.Repository;
import eu.trade.repo.service.interfaces.IObjectService;
import eu.trade.repo.service.interfaces.IRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import java.util.List;

public abstract class AdminController {

	@Autowired
	protected IRepositoryService repositoryService;
	@Autowired
	protected IObjectService objectService;

	public static final String NEW_REPO_ACTION = "newRepo";
	public static final String SESSION_ACTION = "session";
	public static final String CONFIGURATION_ACTION = "configuration";
	protected static final String SEPARATOR = " / ";

	protected Repository getSelectedRepository(String repoId, List<Repository> allRepositories) {
		Repository repository = null;
		String selectedRepo = repoId != null ? repoId : allRepositories.get(0).getCmisId();
		for (Repository repo : allRepositories) {
			String repositoryCmisId = repo.getCmisId();
			if (selectedRepo.equals(repositoryCmisId)) {
				repository = repo;
				break;
			}
		}
		return repository;
	}

	protected void setAdminPageAttributes(Model model, List<Repository> allRepositories, String pageTitle, String selectedRepository, String selectedAction) {
		model.addAttribute("repositories", allRepositories);
		model.addAttribute("pageTitle", pageTitle);
		model.addAttribute("selectedRepo", selectedRepository);
		model.addAttribute("selectedAction", selectedAction);
	}

	protected String generateRepositoryTitle(Repository repository) {
		return repository.getCmisId() + SEPARATOR + repository.getName() + SEPARATOR + repository.getDescription();
	}
}
