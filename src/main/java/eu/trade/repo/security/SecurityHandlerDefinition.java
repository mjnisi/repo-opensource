package eu.trade.repo.security;

/**
 * Interface for {@link SecurityHandler} that defines the handler's name and description.
 * 
 * @author porrjai
 */
public interface SecurityHandlerDefinition {

	/**
	 * Returns the security handler name.
	 * 
	 * @return {@link String} The security handler name.
	 */
	String getName();

	/**
	 * Returns the security handler description.
	 * 
	 * @return {@link String} The security handler description.
	 */
	String getDescription();

	/**
	 * Returns the authentication domain name.
	 * 
	 * @return {@link String} the authentication domain name.
	 */
	String getDomain();
}
