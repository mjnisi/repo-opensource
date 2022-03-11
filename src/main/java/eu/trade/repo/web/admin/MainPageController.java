package eu.trade.repo.web.admin;


import static eu.trade.repo.util.Constants.*;

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.trade.repo.model.Repository;

@Controller
@Secured({CREATE_REPO, DELETE_REPO, CHANGE_REPO_CAPABILITIES, CHANGE_REPO_SECURITY, VIEW_REPO_SESSIONS, VIEW_REPO_SUMMARY})
public class MainPageController extends AdminController{

	public static final String PAGE_TITLE = "Administration page";

	@RequestMapping(value = "/admin")
	public String mainPage(Model model){
		List<Repository> allRepositories = repositoryService.getAllRepositories();
		List objectCountPerRepository = objectService.getObjectCountPerRepository();
		model.addAttribute("objectCountPerRepository", objectCountPerRepository);
		model.addAttribute("repositories", allRepositories);
		setAdminPageAttributes(model, allRepositories, PAGE_TITLE, null, "home");
		return "adminMain";
	}
}
