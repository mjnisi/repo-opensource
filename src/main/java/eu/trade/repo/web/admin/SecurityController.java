package eu.trade.repo.web.admin;

import static eu.trade.repo.util.Constants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.trade.repo.model.HandlerType;
import eu.trade.repo.model.Repository;
import eu.trade.repo.model.SecurityHandler;
import eu.trade.repo.model.SecurityType;
import eu.trade.repo.security.SecurityHandlerDefinition;
import eu.trade.repo.security.SecurityHandlerDefinitions;

/**
 * Admin {@link Controller} for the security specification of a repository.
 * 
 * @author porrjai
 */
@Controller
@Secured(CHANGE_REPO_SECURITY)
public class SecurityController extends AdminController {

	private static final Logger LOG = LoggerFactory.getLogger(SecurityController.class);
	public static final String UPDATE_OK = "ok";
	public static final String UPDATE_ERROR = "error";
	public static final String UPDATE_MIGRATE = "migrate";
	public static final String AUTHENTICATION_PREFIX = "enableAuthenticationHandler";
	public static final String AUTHORISATION_PREFIX = "enableAuthorisationHandler";

	public static final String REDIRECT_STAUS = "redirect:/admin/security/%s?status=%s";

	@Autowired
	private SecurityHandlerDefinitions securityHandlerDefinitions;

	/**
	 * Returns the security specification for a given repository.
	 * 
	 * @param model
	 * @param repoId
	 * @param updateStatus
	 * @return
	 */
	@RequestMapping(value = "/security/{repoId}")
	public String security(Model model, @PathVariable String repoId, @RequestParam(value = "status", required = false) String updateStatus) {

		List<Repository> allRepositories = repositoryService.getAllRepositories();
		Repository repository = repositoryService.getRepositoryByIdWithHandlers(repoId);
		setAdminPageAttributes(model, allRepositories, generateRepositoryTitle(repository), repoId, null);
		model.addAttribute("status", updateStatus);
		model.addAttribute("repository", repository);

		boolean isSimple = repository.getSecurityType().equals(SecurityType.SIMPLE);
		model.addAttribute("isSimple", isSimple);
		model.addAttribute("securityType", repository.getSecurityType().name().toLowerCase());
		model.addAttribute("defaultAuthenticationHandler", repository.getAuthenticationHandler());
		model.addAttribute("defaultAuthorisationHandler", repository.getAuthorisationHandler());

		// Get available and enabled handlers
		Collection<? extends SecurityHandlerDefinition> availableAuthenticationHandlers = securityHandlerDefinitions.getAuthenticationHandlers();
		Collection<? extends SecurityHandlerDefinition> availableAuthorisationHandlers = securityHandlerDefinitions.getAuthorisationHandlers();
		Set<String> enabledAuthenticationHandlers, enabledAuthorisationHandlers;
		if (isSimple) {
			enabledAuthenticationHandlers = Collections.singleton(repository.getAuthenticationHandler());
			enabledAuthorisationHandlers = Collections.singleton(repository.getAuthorisationHandler());
		}
		else {
			enabledAuthenticationHandlers = getEnabledHandlers(repository, HandlerType.AUTHENTICATION, repository.getAuthenticationHandler());
			enabledAuthorisationHandlers = getEnabledHandlers(repository, HandlerType.AUTHORISATION, repository.getAuthorisationHandler());
		}

		model.addAttribute("availableAuthenticationHandlers", availableAuthenticationHandlers);
		model.addAttribute("availableAuthorisationHandlers", availableAuthorisationHandlers);
		model.addAttribute("enabledAuthenticationHandlers", getEnabledHandlersDefinitions(availableAuthenticationHandlers, enabledAuthenticationHandlers));
		model.addAttribute("enabledAuthorisationHandlers", getEnabledHandlersDefinitions(availableAuthorisationHandlers, enabledAuthorisationHandlers));

		return "security";
	}

	/**
	 * Updates the security specification for a given repository.
	 * <p>
	 * The security type is not modified.
	 * 
	 * @param repoId
	 * @param defaultAuthenticationHandler
	 * @param defaultAuthorisationHandler
	 * @return
	 */
	@RequestMapping(value = "/updateSecurity", method = RequestMethod.POST)
	public String updateSecurity(@RequestParam(value = "repoId") String repoId,
			@RequestParam(value = "defaultAuthenticationHandler") String defaultAuthenticationHandler,
			@RequestParam(value = "defaultAuthorisationHandler") String defaultAuthorisationHandler,
			HttpServletRequest request) {
		String updateStatus = UPDATE_OK;
		try {
			repositoryService.updateSecurity(repoId, defaultAuthenticationHandler, defaultAuthorisationHandler,
					getHandlersToEnable(AUTHENTICATION_PREFIX, request), getHandlersToEnable(AUTHORISATION_PREFIX, request));
		} catch (CmisBaseException | DataAccessException e) {
			LOG.error("Error occurred when updating repository", e);
			updateStatus = UPDATE_ERROR;
		}

		return String.format(REDIRECT_STAUS, repoId , updateStatus);
	}

	/**
	 * Toggles the security type for a given repository.
	 * <p>
	 * Note: Toggling the security type is restricted to the current repository status, therefor is not always possible.
	 * 
	 * @param repoId
	 * @return
	 */
	@RequestMapping(value = "/toggleSecurityType", method = RequestMethod.POST)
	public String toggleSecurityType(@RequestParam(value = "repoId") String repoId) {
		String updateStatus = UPDATE_OK;
		try {
			repositoryService.toggleSecurity(repoId);
		} catch (IllegalStateException e) {
			updateStatus = UPDATE_MIGRATE;
		} catch (CmisBaseException | DataAccessException e) {
			LOG.error("Error occurred when updating repository", e);
			updateStatus = UPDATE_ERROR;
		}

		return String.format(REDIRECT_STAUS, repoId , updateStatus);
	}

	private Map<String, SecurityHandlerDefinition> getEnabledHandlersDefinitions(Collection<? extends SecurityHandlerDefinition> available, Set<String> enabledHandlers) {
		Map<String, SecurityHandlerDefinition> enabledHandlersDefinitions = new HashMap<>();
		for (SecurityHandlerDefinition securityHandlerDefinition : available) {
			String name = securityHandlerDefinition.getName();
			if (enabledHandlers.contains(name)) {
				enabledHandlersDefinitions.put(name, securityHandlerDefinition);
			}
		}
		return enabledHandlersDefinitions;
	}

	private Set<String> getEnabledHandlers(Repository repository, HandlerType handlerType, String defaultHandler) {
		Set<String> enabledHandlers = new HashSet<>();
		enabledHandlers.add(defaultHandler);
		for (SecurityHandler securityHandler : repository.getSecurityHandlers()) {
			if (securityHandler.getHandlerType().equals(handlerType)) {
				enabledHandlers.add(securityHandler.getHandlerName());
			}
		}
		return enabledHandlers;
	}

	private Set<String> getHandlersToEnable(String prefix, HttpServletRequest request) {
		Set<String> handlersToEnable = new HashSet<>();
		Enumeration<String> parametersNames = request.getParameterNames();
		int length = prefix.length();
		while (parametersNames.hasMoreElements()) {
			String parameterName = parametersNames.nextElement();
			if (parameterName.startsWith(prefix)) {
				handlersToEnable.add(parameterName.substring(length));
			}
		}
		return handlersToEnable;
	}
}
