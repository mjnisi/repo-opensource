package eu.trade.repo.security;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.model.ActionParameter;
import eu.trade.repo.security.CustomSecured.CustomAction;

/**
 * Interface for the basic operations regarding the following authorization cmis concepts:
 * <ul>
 * <li>Authorize(Action, Object): Must check if the current user is authorized to perform the requested action.</li>
 * </ul>
 * 
 * @see Action
 * @see AuthorizationAspect
 * @author porrjai
 */
interface Authorization {

	/**
	 * Checks the current user permission to proceed with the requested action. Each parameter refers to a cmis object.
	 * <p>
	 * First it will check that the specified action parameters match the action definition. After that, for each pair actionParameter/parameter,
	 * it will check the user has the needed permission granted by at least one of the related cmis object's ACL.
	 * 
	 * @param action {@link Action} The requested action.
	 * @param actionParameters {@link Map<ActionParameter, String>} The map between action parameters and the parameters value. Must be not null.
	 * @throws CmisPermissionDeniedException When the user is not granted to proceed with the requested action.
	 */
	@Transactional
	void checkPermission(Action action, Map<ActionParameter, String> actionParameters);

	/**
	 * Checks the current user permission to proceed with the requested action. Each parameter refers to a cmis object.
	 * <p>
	 * The action restriction is custom based on the specified {@link CustomAction}. Basically, it will define the access to the service using more than one basic {@link Action}.
	 * The set of such actions depends on each custom logic.
	 * 
	 * @param action {@link CustomAction} The requested custom action.
	 * @param actionParameters {@link Map<ActionParameter, String>} The map between action parameters and the parameters value. Must be not null.
	 * @throws CmisPermissionDeniedException When the user is not granted to proceed with the requested action.
	 */
	@Transactional
	void checkPermission(CustomAction action, Map<ActionParameter, String> actionParameters);
}
