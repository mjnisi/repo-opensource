package eu.trade.repo.web.admin;

import static eu.trade.repo.util.Constants.*;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import eu.trade.repo.model.Repository;

@Controller
@Secured(CREATE_REPO)
public class NewRepositoryController extends AdminController {
	private static final Logger LOGGER = LoggerFactory.getLogger(NewRepositoryController.class);
	private static final String PAGE_TITLE = "Create new repository";

	@RequestMapping(value = "/newRepository")
	public String createNewRepositoryForm(Model model, HttpServletRequest request, HttpServletResponse response) {

		List<Repository> allRepositories = repositoryService.getAllRepositories();
		setAdminPageAttributes(model, allRepositories, PAGE_TITLE, null, AdminController.NEW_REPO_ACTION);
		return "newRepository";
	}

	@RequestMapping(value = "/createRepository")
	public String createNewRepositoryAction(
			Model model,
			@RequestParam("repositoryId") String repositoryId,
			@RequestParam("repositoryName") String repositoryName,
			@RequestParam("repositoryDesc") String repositoryDesc) {

		Repository repository = new Repository();
		repository.setCmisId(repositoryId);
		repository.setName(repositoryName);
		repository.setDescription(repositoryDesc);

		Repository reporitory = null;
		try {
			reporitory = repositoryService.createRepository(repository);
		} catch (Exception e) {
			LOGGER.error("Error occurred when creating repository", e);
			model.addAttribute("error", e.getMessage());
		}
		model.addAttribute("repository", reporitory);
		List<Repository> allRepositories = repositoryService.getAllRepositories();

		setAdminPageAttributes(model, allRepositories, PAGE_TITLE, null, AdminController.NEW_REPO_ACTION);
		return "newRepositoryConfirmation";
	}
}
