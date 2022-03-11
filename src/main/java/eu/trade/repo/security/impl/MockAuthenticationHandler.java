package eu.trade.repo.security.impl;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;

import eu.trade.repo.security.AuthenticationHandler;

/**
 * {@link AuthenticationHandler} mock implementation.
 * <p>
 * Always return true after a short delay.
 * 
 * @author porrjai
 */
public class MockAuthenticationHandler extends AbstractSecurityHandler implements AuthenticationHandler {

	private String domain;
	private long delay;

	/**
	 * @see eu.trade.repo.security.Configurable#init(org.apache.commons.configuration.Configuration)
	 */
	@Override
	public void init(Configuration configuration) {
		domain = configuration.getString("domain", "mock");
		delay = configuration.getLong("delay", 500);
	}

	/**
	 * @see eu.trade.repo.security.AuthenticationHandler#authenticate(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized boolean authenticate(String username, String password) {
		
		/* Empty usernames could brake the data model, they are needed in ACLs and change events */
		boolean emptyUsername = StringUtils.isEmpty(username);
		
		try {
			wait(delay);
		} catch (InterruptedException e) {
			// Explicitly ignored
		}
		return !emptyUsername;
	}

	/**
	 * @see eu.trade.repo.security.AuthenticationHandler#getDomain()
	 */
	@Override
	public String getDomain() {
		return domain;
	}
}
