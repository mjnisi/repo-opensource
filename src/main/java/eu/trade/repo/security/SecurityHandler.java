package eu.trade.repo.security;

/**
 * Common interface for {@link AuthenticationHandler} and {@link AuthorizationHandler} that defines the handler's name and description setters.
 * 
 * @author porrjai
 */
public interface SecurityHandler extends SecurityHandlerDefinition, Configurable {

	/**
	 * @param name {@link String} The name to set.
	 */
	void setName(String name);

	/**
	 * @param description {@link String} The description to set.
	 */
	void setDescription(String description);
}
