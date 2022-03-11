package eu.trade.repo.security.session;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * User session list. This enables to set a limit of sessions per user.
 * 
 * @author porrjai
 */
public class UserSessions implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int limit;
	private final Map<String, UserSessionKey> userSessions = new LinkedHashMap<>();

	/**
	 * New instance
	 * 
	 * @param limit
	 */
	public UserSessions(int limit) {
		this.limit = limit;
	}

	/**
	 * Get the {@link UserSessionKey} associated with the password.
	 * 
	 * @param password
	 * @return {@link UserSessionKey}
	 */
	public UserSessionKey get(String password) {
		return userSessions.get(password);
	}

	/**
	 * Renews the {@link UserSessionKey} putting it ahead in the list.
	 * 
	 * @param userSessionKey {@link UserSessionKey} The user session key to be renewed.
	 */
	public void renew(UserSessionKey userSessionKey) {
		String password = userSessionKey.getPassword();
		UserSessionKey oldUserSessionKey = userSessions.remove(password);
		if ( oldUserSessionKey != null ) {
			userSessions.put(password, userSessionKey);
		}
	}

	/**
	 * Adds a new pair {@link UserSessionKey} / {@link CallContextData} to the user session, taking care about the limit per user.
	 * 
	 * @param userSessionKey {@link UserSessionKey} The session key to be added (or renewed).
	 * @param callContextData {@link CallContextData} The call context data.
	 * @param securityCache {@link Cache} The security cache where to store the new key.
	 */
	public void put(UserSessionKey userSessionKey, CallContextData callContextData, Cache securityCache) {
		String password = userSessionKey.getPassword();
		UserSessionKey oldUserSessionKey = userSessions.remove(password);
		if ( oldUserSessionKey == null && userSessions.size() >= limit ) {
			// is a new session and the limit has been reached, then remove first.
			Iterator<UserSessionKey> valuesIt = userSessions.values().iterator();
			oldUserSessionKey = valuesIt.next();
			valuesIt.remove();
			securityCache.remove(oldUserSessionKey);
		}
		userSessions.put(password, userSessionKey);
		securityCache.put(new Element(userSessionKey, callContextData));
	}

}
