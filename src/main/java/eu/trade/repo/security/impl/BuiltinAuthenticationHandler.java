package eu.trade.repo.security.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;

import eu.trade.repo.security.AuthenticationHandler;
import eu.trade.repo.util.Utilities;

/**
 * Built in authentication handler for simple security.
 * 
 * @author porrjai
 */
public class BuiltinAuthenticationHandler extends AbstractSecurityHandler implements AuthenticationHandler {

	private Map<String, String> users;
	private String domain;
	private static final String ANY_USER = "*";
	/**
	 * @see eu.trade.repo.security.Configurable#init(org.apache.commons.configuration.Configuration)
	 */
	@Override
	public void init(Configuration configuration) {
		users = new HashMap<>();
		List<Object> names = configuration.getList("user.name");
		List<Object> pwds = configuration.getList("user.password");
		if (Utilities.isEmpty(names, pwds) || !Utilities.sameSize(names, pwds)) {
			throw new IllegalArgumentException("Invalid configuration.");
		}
		int size = names.size();
		for (int i = 0; i < size; i++) {
			String name = names.get(i).toString();
			String pwd = pwds.get(i).toString();
			users.put(name, pwd);
		}
		domain = configuration.getString("domain");
	}

	/**
	 * @see eu.trade.repo.security.AuthenticationHandler#authenticate(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean authenticate(String username, String password) {
		String pwd = users.get(username);
		if(pwd==null)
		{
			pwd = users.get(ANY_USER);
		}
		return pwd != null && pwd.equals(password);
	}

	/**
	 * @see eu.trade.repo.security.AuthenticationHandler#getDomain()
	 */
	@Override
	public String getDomain() {
		return domain;
	}
}
