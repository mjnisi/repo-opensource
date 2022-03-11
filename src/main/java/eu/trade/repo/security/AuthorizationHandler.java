package eu.trade.repo.security;

import java.util.Set;

/**
 * Interface for the repository authorization handlers.
 * <p>
 * An authorization handler will will return the set of principal ids (roles, groups, operations) associated with a previously authenticated user.
 * <p>
 * NOTE: According to the CMIS 1.1 specification:<br/>
 * <i>
 * 2.2.1.5 ACLs<br/>
 * Those services which allow for the setting of ACLs MAY take the optional macro cmis:user which allows the caller to indicate the operation applies to the current authenticated user. <br/>
 * If the repository info provides a value for principalAnonymous, this value can be used to in an ACE to specify permissions for anonymous users.<br/>
 * If the repository info provides a value for principalAnyone, this value can be used to in an ACE to specify permissions for any authenticated user.
 * <i/>
 * 
 * @author porrjai
 */
public interface AuthorizationHandler extends SecurityHandler {

	/**
	 * Returns the complete set of user principals ids, excluding the username itself.
	 * <p>
	 * The user principal ids is the set of unique names for all the user's groups, roles, etc. the user belongs to.
	 * 
	 * @param username {@link String} The username.
	 * @return {@link Set<String>} the complete set of user principals ids, excluding the username itself. Mandatory not null.
	 */
	Set<String> getPrincipalIds(String username);

	/**
	 * Returns true if the user has admin privileges for the repositories this handlers is enabled.
	 * 
	 * @return boolean true if the user has admin privileges.
	 */
	boolean isAdmin(String username);
}
