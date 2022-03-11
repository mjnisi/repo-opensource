/**
 * 
 */
package eu.trade.repo.security.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import eu.trade.repo.security.AuthenticationHandler;

/**
 * Test {@link AuthenticationHandler}.
 * <p>
 * It authenticates any user/password combination but only once.
 * 
 * @author porrjai
 */
public class OTPAuthenticationHandler extends AbstractSecurityHandler implements AuthenticationHandler {

	private static final Map<String, Set<String>> ALREADY_AUTHENTICATED = new HashMap<>();

	/**
	 * @see eu.trade.repo.security.SecurityHandlerDefinition#getDomain()
	 */
	@Override
	public String getDomain() {
		return "otp";
	}

	/**
	 * @see eu.trade.repo.security.Configurable#init(org.apache.commons.configuration.Configuration)
	 */
	@Override
	public void init(Configuration configuration) {
	}

	/**
	 * @see eu.trade.repo.security.AuthenticationHandler#authenticate(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean authenticate(String username, String password) {
		Set<String> passwords = ALREADY_AUTHENTICATED.get(username);
		if (passwords == null) {
			passwords = new HashSet<>();
			ALREADY_AUTHENTICATED.put(username, passwords);
		}
		// True if the password doesn't exist yet in the set
		return passwords.add(password);
	}

	public static void reset() {
		ALREADY_AUTHENTICATED.clear();
	}
}
