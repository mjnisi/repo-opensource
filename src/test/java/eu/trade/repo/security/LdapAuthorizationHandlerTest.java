package eu.trade.repo.security;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Set;

import javax.naming.Context;

import org.apache.commons.configuration.MapConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.trade.repo.security.impl.LdapAuthorizationHandler;

/**
 * These tests are marked as ignored given they depend on nest dev availability and configuration.
 * <p>
 * Currently their purpose is only for helping in development and troubleshooting.
 * 
 * @author porrjai
 */
public class LdapAuthorizationHandlerTest {

	private static final String DOMAIN = "domain";

	private static final String GROUP_BASE_DN = "ou=nest,ou=Groups,dc=example,dc=com";
	private static final String GROUP_FILTER_EXPR = "uniqueMember=uid={0},*";
	private static final String GROUP_ATTRIBUTE = "entryDN";
	private static final boolean GROUP_RECURSIVE = true;
	private static final boolean ADMIN_USERS = true;
	private static final String ADMIN_BASE_DN = "ou=special users,dc=example,dc=com";
	private static final String ADMIN_FILTER_EXPR = "uid={0}";

	private static final String INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
	private static final String PROVIDER_URL = "ldap://nestdev.trade.cec.eu.int:1389";
	private static final String SECURITY_CREDENTIALS = "admin";
	private static final String SECURITY_PRINCIPAL = "cn=Directory Manager";

	private static final String ADMIN = "admin";
	private static final String NOT_ADMIN = "rusuioa";

	private MapConfiguration configuration;

	@Before
	public void init() {
		configuration = new MapConfiguration(new HashMap<String, Object>());
		configuration.setDelimiterParsingDisabled(true);
		configuration.addProperty(LdapAuthorizationHandler.DOMAIN, DOMAIN);
		configuration.addProperty(LdapAuthorizationHandler.GROUP_BASE_DN, GROUP_BASE_DN);
		configuration.addProperty(LdapAuthorizationHandler.GROUP_FILTER_EXPR, GROUP_FILTER_EXPR);
		configuration.addProperty(LdapAuthorizationHandler.GROUP_ATTRIBUTE, GROUP_ATTRIBUTE);
		configuration.addProperty(LdapAuthorizationHandler.GROUP_RECURSIVE, GROUP_RECURSIVE);

		configuration.addProperty(LdapAuthorizationHandler.ADMIN_USERS, ADMIN_USERS);
		configuration.addProperty(LdapAuthorizationHandler.ADMIN_BASE_DN, ADMIN_BASE_DN);
		configuration.addProperty(LdapAuthorizationHandler.ADMIN_FILTER_EXPR, ADMIN_FILTER_EXPR);

		configuration.addProperty(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
		configuration.addProperty(Context.PROVIDER_URL, PROVIDER_URL);
		configuration.addProperty(Context.SECURITY_CREDENTIALS, SECURITY_CREDENTIALS);
		configuration.addProperty(Context.SECURITY_PRINCIPAL, SECURITY_PRINCIPAL);

	}

	@Ignore
	@Test
	public void testNotAdmin() {
		LdapAuthorizationHandler ldapAuthorizationHandler = new LdapAuthorizationHandler();
		ldapAuthorizationHandler.init(configuration);
		Set<String> groups = ldapAuthorizationHandler.getPrincipalIds(NOT_ADMIN);
		for (String group : groups) {
			System.out.println(group);
		}
		assertFalse(ldapAuthorizationHandler.isAdmin(NOT_ADMIN));
	}

	@Ignore
	@Test
	public void testAdmin() {
		LdapAuthorizationHandler ldapAuthorizationHandler = new LdapAuthorizationHandler();
		ldapAuthorizationHandler.init(configuration);
		Set<String> groups = ldapAuthorizationHandler.getPrincipalIds(ADMIN);
		for (String group : groups) {
			System.out.println(group);
		}
		assertTrue(ldapAuthorizationHandler.isAdmin(ADMIN));
	}
}
