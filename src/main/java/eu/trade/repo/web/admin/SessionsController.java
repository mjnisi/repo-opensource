package eu.trade.repo.web.admin;

import static eu.trade.repo.util.Constants.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.trade.repo.model.Repository;
import eu.trade.repo.security.session.SessionInfo;
import eu.trade.repo.security.session.UserKey;

@Controller
@Secured(VIEW_REPO_SUMMARY)
public class SessionsController extends AdminController {

	@Autowired
	private SessionInfo sessionInfo;

	private static final String PAGE_TITLE = "User sessions";

	@RequestMapping(value = "/sessions")
	public String sessions(Model model) {
		List<Repository> allRepositories = repositoryService.getAllRepositories();
		setAdminPageAttributes(model, allRepositories, PAGE_TITLE, null, null);

		List<UserKey> currentSessions = sessionInfo.getCurrentSessions();
		model.addAttribute("sessions", currentSessions);
		return "sessions";
	}
}
