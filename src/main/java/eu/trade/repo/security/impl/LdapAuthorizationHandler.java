/**
 * 
 */
package eu.trade.repo.security.impl;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trade.repo.security.AuthorizationHandler;

/**
 *  {@link AuthorizationHandler} implementation based on an authorization LDAP.
 *  <p>
 *  The configuration must follow the following template:<br/>
&lt;authorizationHandler&gt;<br/>
	&lt;name&gt;Name&lt;/name&gt;<br/>
	&lt;enable&gt;true&lt;/enabled&gt;<br/>
	&lt;description&gt;Description&lt;/description&gt;<br/>
	&lt;class&gt;eu.trade.repo.security.impl.LdapAuthorizationHandler&lt;/class&gt;<br/>
	&lt;properties&gt;<br/>
		&lt;domain&gt;test&lt;/domain&gt;<br/>
		&lt;java&gt;<br/>
			&lt;naming&gt;<br/>
				&lt;factory&gt;<br/>
					&lt;initial&gt;&lt;/initial&gt;<br/>
				&lt;/factory&gt;<br/>
				&lt;provider&gt;<br/>
					&lt;url&gt;ldaps://host:port&lt;/url&gt;<br/>
				&lt;/provider&gt;<br/>
				&lt;security&gt;<br/>
					&lt;authentication&gt;simple&lt;/authentication&gt;<br/>
					&lt;principal&gt;readOnlyUserName&lt;/principal&gt;<br/>
					&lt;credentials&gt;readOnlyUserPassword&lt;/credentials&gt;<br/>
				&lt;/security&gt;<br/>
			&lt;/naming&gt;<br/>
		&lt;/java&gt;<br/>
		&lt;groupBaseDn&gt;ou=groups,dc=company,dc=com&lt;/groupBaseDn&gt;<br/>
		&lt;groupFilterExpr&gt;uniqueMember=uid={0},ou=people,dc=company,dc=com&lt;/groupFilterExpr&gt;<br/>
		&lt;groupAttribute&gt;entryDN&lt;/groupAttribute&gt;<br/>
		&lt;groupRecursive&gt;true&lt;/groupRecursive&gt;<br/>
		&lt;adminUsers&gt;true&lt;/adminUsers&gt;<br/>
		&lt;adminBaseDn&gt;true&lt;/adminBaseDn&gt;<br/>
		&lt;adminFilterExpr&gt;true&lt;/adminFilterExpr&gt;<br/>
		&lt;searchTimeLimit&gt;true&lt;/searchTimeLimit&gt;<br/>
	&lt;/properties&gt;<br/>
&lt;/authorizationHandler&gt;<br/>
 * <p>
 * The detail for the parameters is:<br/>
 * <ul>
 * <li>domain: The authorization handler domain. Mandatory</li><br/>
 * <li>java.naming.factory.initial: The {@code javax.naming.Context.INITIAL_CONTEXT_FACTORY} property. Optional, default to {@code com.sun.jndi.ldap.LdapCtxFactory}.</li><br/>
 * <li>java.naming.provider.url: The {@code javax.naming.Context.PROVIDER_URL} property. Mandatory.</li><br/>
 * <li>java.naming.security.authentication: The {@code javax.naming.Context.SECURITY_AUTHENTICATION} property. Optional, default to false.</li><br/>
 * <li>java.naming.security.principal: The {@code javax.naming.Context.SECURITY_PRINCIPAL} property. Mandatory.</li><br/>
 * <li>java.naming.security.credentials: The {@code javax.naming.Context.SECURITY_CREADENTIALS} property. Mandatory.</li><br/>
 * <li>groupBaseDn: Base context for the group search. Mandatory.</li><br/>
 * <li>groupFilterExpr: Filter expression for the group search. Note that, as maximum, only one parameter should appear in the filter ({0}), the user login name. Mandatory.</li><br/>
 * <li>groupAttribute: The attribute in the results that contains the value for the user's groups. Mandatory.</li><br/>
 * <li>groupRecursive: Whether the parent groups should be resolved or only the directly assign groups. Optional, default to false.</li><br/>
 * <li>adminUsers: Whether the handler should resolve admin users. If not, then <function>isAdmin()</function> returns always false.</li><br/>
 * <li>adminBaseDn: Base context for the admin user search. Mandatory only when adminUsers is true.</li><br/>
 * <li>adminFilterExpr: Filter expression for the admin search. Note that, as maximum, only one parameter should appear in the filter ({0}), the user login name. Mandatory only when adminUsers is true.</li><br/>
 * <li>searchTimeLimit: Time limit for any search. Optional, default to 15 seconds = {@code 15000}.</li><br/>
 * </ul>
 * @author porrjai
 */
public class LdapAuthorizationHandler extends AbstractSecurityHandler implements AuthorizationHandler {

	private static final Logger LOG = LoggerFactory.getLogger(LdapAuthorizationHandler.class);
	private static final String DEFAULT_INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
	private static final String DEFAULT_SECURITY_AUTHENTICATION = "simple";
	private static final String LDAP_ERROR = "Authorization information cannot be retrieved due to a ldap error.";
	private static final int DEFAULT_TIME_LIMIT = 15000;
	private static final Pattern GROUP_EXTRACTOR = Pattern.compile("cn=.*?,(cn=.*)$", Pattern.CASE_INSENSITIVE);

	public static final String DOMAIN = "domain";
	public static final String GROUP_BASE_DN = "groupBaseDn";
	public static final String GROUP_FILTER_EXPR = "groupFilterExpr";
	public static final String GROUP_ATTRIBUTE = "groupAttribute";
	public static final String GROUP_RECURSIVE = "groupRecursive";
	public static final String ADMIN_USERS = "adminUsers";
	public static final String ADMIN_BASE_DN = "adminBaseDn";
	public static final String ADMIN_FILTER_EXPR = "adminFilterExpr";
	public static final String SEARCH_TIME_LIMIT = "searchTimeLimit";

	private final Hashtable<String, Object> env = new Hashtable<String, Object>();
	private String domain;

	private String groupBaseDn;
	private String groupFilterExpr;
	private String groupAttribute;
	private boolean groupRecursive;
	private SearchControls groupSearchControls;

	private boolean adminUsers;
	private String adminBaseDn;
	private String adminFilterExpr;
	private SearchControls adminSearchControls;

	/**
	 * @see eu.trade.repo.security.Configurable#init(org.apache.commons.configuration.Configuration)
	 */
	@Override
	public void init(Configuration configuration) {
		domain = configuration.getString(DOMAIN);
		env.put(Context.INITIAL_CONTEXT_FACTORY, configuration.getString(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_INITIAL_CONTEXT_FACTORY));
		env.put(Context.SECURITY_AUTHENTICATION, configuration.getString(Context.SECURITY_AUTHENTICATION, DEFAULT_SECURITY_AUTHENTICATION));
		env.put(Context.PROVIDER_URL, configuration.getString(Context.PROVIDER_URL));
		env.put(Context.SECURITY_PRINCIPAL, configuration.getString(Context.SECURITY_PRINCIPAL));
		env.put(Context.SECURITY_CREDENTIALS, configuration.getString(Context.SECURITY_CREDENTIALS));

		int timeLimit = configuration.getInt(SEARCH_TIME_LIMIT, DEFAULT_TIME_LIMIT);

		groupBaseDn = configuration.getString(GROUP_BASE_DN);
		groupFilterExpr = configuration.getString(GROUP_FILTER_EXPR);
		groupAttribute = configuration.getString(GROUP_ATTRIBUTE);
		groupRecursive = configuration.getBoolean(GROUP_RECURSIVE, false);
		groupSearchControls = newSearchControls(timeLimit, new String[]{groupAttribute});

		adminUsers = configuration.getBoolean(ADMIN_USERS, false);
		if (adminUsers) {
			adminBaseDn = configuration.getString(ADMIN_BASE_DN);
			adminFilterExpr = configuration.getString(ADMIN_FILTER_EXPR);
			adminSearchControls = newSearchControls(timeLimit, new String[]{});
		}

	}

	private SearchControls newSearchControls(int timeLimit, String[] attrs) {
		return new SearchControls(SearchControls.SUBTREE_SCOPE, 0, timeLimit, attrs, false, false);
	}
	/**
	 * @see eu.trade.repo.security.SecurityHandlerDefinition#getDomain()
	 */
	@Override
	public String getDomain() {
		return domain;
	}

	/**
	 * @see eu.trade.repo.security.AuthorizationHandler#getPrincipalIds(java.lang.String)
	 */
	@Override
	public Set<String> getPrincipalIds(String username) {
		DirContext ctx = null;
		Set<String> ldapGroups = new HashSet<>();
		try {
			ctx = new InitialDirContext(env);
			Enumeration<SearchResult> results = ctx.search(groupBaseDn, groupFilterExpr, new String[]{username}, groupSearchControls);
			while (results.hasMoreElements()) {

				addGroups(ldapGroups, results.nextElement());

			}
		}
		catch (NamingException e) {
			LOG.error(LDAP_ERROR + e.getLocalizedMessage(), e);
			throw new IllegalStateException(LDAP_ERROR, e);
		}
		finally {
			close(ctx);
		}

		return ldapGroups;
	}

	/**
	 * Return true if the admin filter search returns any result.
	 * 
	 * @see eu.trade.repo.security.AuthorizationHandler#isAdmin(java.lang.String)
	 */
	@Override
	public boolean isAdmin(String username) {
		if (!adminUsers) {
			return false;
		}
		DirContext ctx = null;
		boolean isAdmin = false;
		try {
			ctx = new InitialDirContext(env);
			Enumeration<SearchResult> results = ctx.search(adminBaseDn, adminFilterExpr, new String[]{username}, adminSearchControls);
			isAdmin = results.hasMoreElements();
		}
		catch (NamingException e) {
			LOG.error(LDAP_ERROR + e.getLocalizedMessage(), e);
			throw new IllegalStateException(LDAP_ERROR, e);
		}
		finally {
			close(ctx);
		}

		return isAdmin;
	}

	private void addGroups(Set<String> ldapGroups, SearchResult searchResult) throws NamingException {
		Attribute attribute = searchResult.getAttributes().get(groupAttribute);
		if (attribute != null) {
			NamingEnumeration<?> groups = attribute.getAll();
			while (groups.hasMore()) {
				Object group = groups.next();
				if (group != null) {
					String baseGroup = group.toString();
					ldapGroups.add(baseGroup);
					if (groupRecursive) {
						addRecursiveGroups(ldapGroups, baseGroup);
					}
				}
			}
		}
	}

	private void addRecursiveGroups(Set<String> ldapGroups, String baseGroup) {
		Matcher matcher = GROUP_EXTRACTOR.matcher(baseGroup);
		if (matcher.matches()) {
			String recursiveGroup = matcher.group(1);
			ldapGroups.add(recursiveGroup);
			addRecursiveGroups(ldapGroups, recursiveGroup);
		}
	}

	private void close(DirContext ctx) {
		if (ctx != null) {
			try {
				ctx.close();
			} catch (NamingException e) {
				LOG.error("Error closing context. Silently ignored.", e);
			}
		}
	}
}
