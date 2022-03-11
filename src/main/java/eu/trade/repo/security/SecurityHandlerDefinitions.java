package eu.trade.repo.security;

import java.util.Collection;

/**
 * Provide access to the available {@link SecurityHandlerDefinition}
 * 
 * @author porrjai
 */
public interface SecurityHandlerDefinitions {

	/**
	 * @return {@link SecurityHandlerDefinition} the authentication handlers definitions.
	 */
	Collection<? extends SecurityHandlerDefinition> getAuthenticationHandlers();

	/**
	 * @return {@link SecurityHandlerDefinition} the authorisation handlers definitions.
	 */
	Collection<? extends SecurityHandlerDefinition> getAuthorisationHandlers();
}
