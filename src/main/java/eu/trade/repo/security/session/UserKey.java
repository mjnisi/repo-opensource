package eu.trade.repo.security.session;

import java.io.Serializable;

/**
 * Key for the user cache. All the fields are needed for the key.
 * <p>
 * Note that the repositoryId is only used to speed up the check of the security handlers allowed, so the same user will have one key per repository.
 */
public class UserKey implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String repositoryId;
	private final String username;
	private final String authenticationName;
	private final String remoteIP;

	/**
	 * New instance
	 *
	 * @param repositoryId
	 * @param username
	 * @param authenticationName
	 * @param remoteIP
	 */
	public UserKey(String repositoryId, String username, String authenticationName, String remoteIP) {
		this.repositoryId = repositoryId;
		this.username = username;
		this.authenticationName = authenticationName;
		this.remoteIP = remoteIP;
	}

	/**
	 * @return the repositoryId
	 */
	public String getRepositoryId() {
		return repositoryId;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the authenticationName
	 */
	public String getAuthenticationName() {
		return authenticationName;
	}

	/**
	 * @return the remoteIP
	 */
	public String getRemoteIP() {
		return remoteIP;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if ( !(o instanceof UserKey) ) {
			return false;
		}

		UserKey userKey = (UserKey) o;

		if (authenticationName != null ? !authenticationName.equals(userKey.authenticationName) : userKey.authenticationName != null) {
			return false;
		}
		if (username != null ? !username.equals(userKey.username) : userKey.username != null) {
			return false;
		}
		if (remoteIP != null ? !remoteIP.equals(userKey.remoteIP) : userKey.remoteIP != null) {
			return false;
		}
		return repositoryId != null ? repositoryId.equals(userKey.repositoryId) : userKey.repositoryId == null;
	}

	@Override
	public int hashCode() {
		int result = repositoryId != null ? repositoryId.hashCode() : 0;
		result = 31 * result + (username != null ? username.hashCode() : 0);
		result = 31 * result + (authenticationName != null ? authenticationName.hashCode() : 0);
		result = 31 * result + (remoteIP != null ? remoteIP.hashCode() : 0);
		return result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder("[").append(repositoryId)
				.append(", ")
				.append(username)
				.append(", ")
				.append(authenticationName)
				.append(", ")
				.append(remoteIP)
				.append("]")
				.toString();
	}
}
