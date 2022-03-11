package eu.trade.repo.security;

/**
 * Interface for the repository authentication handlers.
 * <p>
 * A security handler will authenticate the provided pair of user/password.
 * 
 * @author porrjai
 */
public interface AuthenticationHandler extends SecurityHandler {

	/**
	 * Authenticate the pair user/password.
	 * 
	 * @param username {@link String} The username.
	 * @param password {@link String} the password.
	 * @return true if the user is authenticated.
	 */
	boolean authenticate(String username, String password);
}
