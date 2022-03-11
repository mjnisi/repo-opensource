package eu.trade.repo.security.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import eu.trade.repo.security.AuthorizationHandler;
import eu.trade.repo.util.Utilities;

/**
 * Built in authorization handler for simple security.
 * 
 * @author porrjai
 */
public class BuiltinAuthorizationHandler extends AbstractSecurityHandler implements AuthorizationHandler {

	private static final String PRINCIPAL_ID_SEP = ",";

	private Map<String, Boolean> isAdminByUser;
	private Map<String, Set<String>> principalIdsByUser;
	private String domain;

	/**
	 * @see eu.trade.repo.security.Configurable#init(org.apache.commons.configuration.Configuration)
	 */
	@Override
	public void init(Configuration configuration) {
		isAdminByUser = new HashMap<>();
		principalIdsByUser = new HashMap<>();
		domain = configuration.getString("domain");
		List<Object> names = configuration.getList("user.name");
		List<Object> isAdmins = configuration.getList("user.isAdmin");
		List<Object> principalIds = configuration.getList("user.principalIds");
		if (Utilities.isEmpty(names, isAdmins, principalIds) || !Utilities.sameSize(names, isAdmins, principalIds)) {
			throw new IllegalArgumentException("Invalid configuration.");
		}
		int size = names.size();
		for (int i = 0; i < size; i++) {
			String name = names.get(i).toString();
			String isAdminString = isAdmins.get(i).toString();
			isAdminByUser.put(name, Boolean.valueOf(isAdminString));
			String principalIdString = principalIds.get(i).toString();
			Set<String> principalIdsSet = Utilities.toSet(principalIdString.split(PRINCIPAL_ID_SEP));
			principalIdsByUser.put(name, principalIdsSet);
		}
	}

	/**
	 * @see eu.trade.repo.security.AuthorizationHandler#getPrincipalIds(java.lang.String)
	 */
	@Override
	public Set<String> getPrincipalIds(String username) {
		Set<String> tmp = new HashSet<>();
		Set<String> principals = principalIdsByUser.get(username);
		//Maybe the user doesn't exist in the configuration.
		if(principals != null) {
			tmp.addAll(principals);
		}
		return tmp;
	}

	/**
	 * @see eu.trade.repo.security.AuthorizationHandler#isAdmin(java.lang.String)
	 */
	@Override
	public boolean isAdmin(String username) {
		//If the user is not in the configuration should return false
		//With the previous code "return isAdminByUser.get(username);"
		//The autoboxing was executing the method booleanValue() and generating
		//a NullPointerException
		Boolean isAdmin = isAdminByUser.get(username);
		return isAdmin != null && isAdmin.booleanValue();
	}

	/**
	 * @see eu.trade.repo.security.AuthorizationHandler#getDomain()
	 */
	@Override
	public String getDomain() {
		return domain;
	}
}
