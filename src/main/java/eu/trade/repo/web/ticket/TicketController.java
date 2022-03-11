package eu.trade.repo.web.ticket;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TicketController implements InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(TicketController.class);
	private static final String PROXY_TARGET_SERVICE = "PROXY_TARGET_SERVICE";
	private static final String PAGE_TITLE = "Repository ticket generation";
	/**
	 * Limit of request before to cleanup the PGTs cache.
	 */
	private static final int MAX_COUNT = 500;

	@Autowired
	private Configuration combinedConfig;
	@Autowired(required=false)
	private ProxyGrantingTicketStorage pgtStorage;
	private String proxyTargetService;
	private int requesCount;

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		if (combinedConfig != null) {
			proxyTargetService = combinedConfig.getString(PROXY_TARGET_SERVICE);
		}
	}

	@RequestMapping(value = "/ticket", method = RequestMethod.GET)
	public String createNewTicketForm(Model model) {
		model.addAttribute("pageTitle", PAGE_TITLE);
		return "newTicket";
	}

	@RequestMapping(value = "/createTicket")
	public String createTicket(Model model, HttpServletRequest request,
			@RequestParam("targetService") String targetService) {
		Principal principal = request.getUserPrincipal();
		String requestedTargetService = targetService;
		if (targetService == null || targetService.isEmpty()) {
			requestedTargetService = proxyTargetService;
		}
		String ticket = validatePrincipalAndGetTicket(principal, requestedTargetService);
		String user = principal.getName();
		LOG.info("User {} has been granted with a proxy ticket.", user);
		model.addAttribute("pageTitle", PAGE_TITLE);
		model.addAttribute("user", user);
		model.addAttribute("targetService", requestedTargetService);
		model.addAttribute("ticket", ticket);

		return "newTicketConfirmation";
	}

	private String validatePrincipalAndGetTicket(Principal principal, String targetService) {
		if ( !(principal instanceof CasAuthenticationToken) ){
			throw new IllegalStateException("The user must be already authenticated with CAS. Principal: " + principal);
		}
		CasAuthenticationToken casAuthenticationToken = (CasAuthenticationToken) principal;
		String ticket = casAuthenticationToken.getAssertion().getPrincipal().getProxyTicketFor(targetService);
		clean();
		return ticket;
	}

	private synchronized void clean() {
		if (pgtStorage != null) {
			requesCount++;
			if (requesCount >= MAX_COUNT) {
				pgtStorage.cleanUp();
			}
		}
	}
}
