package eu.trade.repo.security;

import java.util.Set;

import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.server.CallContext;

import eu.trade.repo.util.Cleanable;

/**
 * Holder for the call context.
 * <p>
 * As the entry point for the call, it also performs the user authentication and through the related {@link AuthorizationHandler} retrieves the set of the users principal ids.
 * 
 * @author porrjai
 */
public interface CallContextHolder extends Cleanable {

	String DEFAULT = "";

	/**
	 * The character to be used as protocol (authentication, authorization) separator in the username field.
	 * <p>
	 * The <code>:</code> cannot be used given the AtomPub binding uses it to separate the username and password fields, and this leads to parsing errors.
	 * Also, in order to avoid similar problems, none of the special characters allowed in the CAS tickets (<code>?-'</code>) can be used.
	 * 
	 * @see http://www.jasig.org/cas/protocol (3.7. ticket and ticket-granting cookie character set)
	 */
	String PROTOCOL_SEP = "/";

	/**
	 * Init the call context at thread level.
	 * 
	 * @param callContext {@link CallContext} The cmis method call context.
	 */
	void initContext(CallContext callContext);

	/**
	 * This is the point that performs the authentication.
	 */
	void login();

	/**
	 * Provides the current call context repository id .
	 * 
	 * @return {@link String} the current call context repository id .
	 */
	String getRepositoryId();

	/**
	 * Provides the current context username.
	 * 
	 * @return {@link String} username.
	 */
	String getUsername();

	/**
	 * Provides the set of principal ids for the current context user.
	 * <p>
	 * At least, it will include the username.
	 * 
	 * @return {@link Set<String>} the set of principal ids for the current context user.
	 */
	Set<String> getPrincipalIds();

	/**
	 * Returns true if the current user has admin privileges for the current context.
	 * 
	 * @return boolean true if the current user has admin privileges for the current context.
	 */
	boolean isAdmin();
	
	/**
	 * Returns the version used by the client to connect to the repository.
	 * 
	 * @return {@link CmisVersion}
	 */
	CmisVersion getClientCmisVersion();
}
