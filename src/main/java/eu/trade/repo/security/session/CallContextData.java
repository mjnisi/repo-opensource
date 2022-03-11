package eu.trade.repo.security.session;

import java.util.HashSet;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.server.CallContext;

/**
 * {@link CallContext} data for a user session.
 * 
 * @author porrjai
 */
public class CallContextData {

	private final CallContext callContext;
	private final String username;
	private final Set<String> principalIds;
	private final boolean isAdmin;

	/**
	 * New instance
	 * 
	 * @param callContext
	 * @param username
	 * @param principalIds
	 * @param isAdmin
	 */
	public CallContextData(CallContext callContext, String username, Set<String> principalIds, boolean isAdmin) {
		this.callContext = callContext;
		this.username = username;
		this.isAdmin = isAdmin;
		if (principalIds == null) {
			this.principalIds = new HashSet<>();
			this.principalIds.add(username);
		}
		else {
			this.principalIds = principalIds;
		}
	}

	/**
	 * @return the callContext
	 */
	public CallContext getCallContext() {
		return callContext;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the principalIds
	 */
	public Set<String> getPrincipalIds() {
		return principalIds;
	}

	/**
	 * @return the isAdmin
	 */
	public boolean isAdmin() {
		return isAdmin;
	}
}
