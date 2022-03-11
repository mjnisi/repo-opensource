package eu.trade.repo.security.session;

import java.io.Serializable;

/**
 * Key for the session cache. All the fields are needed for the key.
 * 
 * @author porrjai
 */
public class UserSessionKey implements Serializable {

	private static final long serialVersionUID = 1L;

	private final UserKey userKey;
	private final String password;

	/**
	 * @param userKey
	 * @param password
	 */
	public UserSessionKey(UserKey userKey, String password) {
		this.userKey = userKey;
		this.password = password;
	}

	/**
	 * @return the userKey
	 */
	public UserKey getUserKey() {
		return userKey;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = userKey != null ? userKey.hashCode() : 0;
		result = 31 * result + (password != null ? password.hashCode() : 0);
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if ( !(o instanceof UserSessionKey) ) {
			return false;
		}

		UserSessionKey other = (UserSessionKey) o;
		if (password != null ? !password.equals(other.password) : other.password != null) {
			return false;
		}
		return userKey != null ? userKey.equals(other.userKey) : other.userKey == null;
	}
}