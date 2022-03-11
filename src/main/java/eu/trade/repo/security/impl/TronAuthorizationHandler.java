package eu.trade.repo.security.impl;

import eu.trade.repo.security.AuthorizationHandler;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * User: Ioan-Octavian Rusu
 * Date: 4/9/14
 * Time: 9:54 AM
 */
public class TronAuthorizationHandler extends AbstractSecurityHandler implements AuthorizationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TronAuthorizationHandler.class);
    public static final String DOMAIN = "domain";
    private static final String DEFAULT_INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String DEFAULT_SECURITY_AUTHENTICATION = "simple";
    private static final int DEFAULT_TIME_LIMIT = 15000;

    public static final String GMS_GROUP_BASE_DN = "gmsGroupBaseDn";
    public static final String GMS_INCLUDE_GROUP_PARENTS = "gmsIncludeGroupParents";
    public static final String GMS_LDAP_URL = "gmsLdapUrl";
    public static final String GMS_LDAP_PRINCIPAL = "gmsLdapPrincipal";
    public static final String GMS_LDAP_CREDENTIALS = "gmsLdapCredentials";
    public static final String GMS_LDAP_SEARCH_TIME_LIMIT = "gmsSearchTimeLimit";

    public static final String ECAS_PEOPLE_BASE_DN = "ecasPeopleBaseDn";
    public static final String ECAS_LDAP_URL = "ecasLdapUrl";
    public static final String ECAS_LDAP_PRINCIPAL = "ecasLdapPrincipal";
    public static final String ECAS_LDAP_CREDENTIALS = "ecasLdapCredentials";
    public static final String ECAS_LDAP_SEARCH_TIME_LIMIT = "ecasSearchTimeLimit";

    private String domain;
    private String gmsGroupBaseDn;
    private int gmsSearchTimeLimit;
    private String ecasPeopleBaseDn;
    private int ecasSearchTimeLimit;
    private boolean gmsIncludeGroupParents;
    private final Hashtable<String, Object> ecasLdapEnv = new Hashtable<String, Object>();
    private final Hashtable<String, Object> gmsLdapEnv = new Hashtable<String, Object>();

    @Override
    public Set<String> getPrincipalIds(String username) {
        DirContext ctx = null;

        Set<String> principalIds = getECASUserIdentities(username);
        principalIds.add(username);
        try {
            ctx = new InitialDirContext(gmsLdapEnv);
            StringBuilder filter = new StringBuilder("(&(objectClass=groupOfUniqueNames)(|");
            for (String userId : principalIds) {
                filter.append("(uniqueMember=uid=").append(userId).append(",ou=people*").append(")");
            }
            filter.append("))");

            Enumeration<SearchResult> results = ctx.search(gmsGroupBaseDn, filter.toString(), newSearchControls(gmsSearchTimeLimit, new String[]{"entryDN"}));
            while (results.hasMoreElements()) {
                SearchResult sr = results.nextElement();
                String entryDN = readLDAPAttribute(sr.getAttributes(), "entryDN");
                if (entryDN != null) {
                    if (gmsIncludeGroupParents) {
                        principalIds.addAll(getDnAncestors(entryDN));
                    } else {
                        principalIds.add(entryDN);
                    }
                }
            }
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return filterPrincipalId(principalIds);
    }

    private Set<String> getDnAncestors(String groupDN) throws InvalidNameException {
        LdapName dn = new LdapName(groupDN);
        Set<String> ancestors = new LinkedHashSet<>();
        StringBuilder sb = new StringBuilder();
        for (int i = dn.size() - 1; i >= 0; i--) {
            sb.setLength(0);
            for (int j = i; j >= 0; j--) {
                if (j != i) {
                    sb.append(",");
                }
                sb.append(dn.get(j));
            }
            ancestors.add(sb.toString());
        }
        return ancestors;
    }

    @Override
    public boolean isAdmin(String username) {
        return false;
    }

    @Override
    public void init(Configuration configuration) {
        domain = configuration.getString(DOMAIN);

        gmsGroupBaseDn = configuration.getString(GMS_GROUP_BASE_DN);
        gmsIncludeGroupParents = configuration.getBoolean(GMS_INCLUDE_GROUP_PARENTS, true);
        gmsSearchTimeLimit = configuration.getInt(GMS_LDAP_SEARCH_TIME_LIMIT, DEFAULT_TIME_LIMIT);
        gmsLdapEnv.put(Context.INITIAL_CONTEXT_FACTORY, configuration.getString(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_INITIAL_CONTEXT_FACTORY));
        gmsLdapEnv.put(Context.SECURITY_AUTHENTICATION, configuration.getString(Context.SECURITY_AUTHENTICATION, DEFAULT_SECURITY_AUTHENTICATION));
        gmsLdapEnv.put(Context.PROVIDER_URL, configuration.getString(GMS_LDAP_URL));
        gmsLdapEnv.put(Context.SECURITY_PRINCIPAL, configuration.getString(GMS_LDAP_PRINCIPAL));
        gmsLdapEnv.put(Context.SECURITY_CREDENTIALS, configuration.getString(GMS_LDAP_CREDENTIALS));

        ecasPeopleBaseDn = configuration.getString(ECAS_PEOPLE_BASE_DN);
        ecasSearchTimeLimit = configuration.getInt(ECAS_LDAP_SEARCH_TIME_LIMIT, DEFAULT_TIME_LIMIT);
        ecasLdapEnv.put(Context.INITIAL_CONTEXT_FACTORY, configuration.getString(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_INITIAL_CONTEXT_FACTORY));
        ecasLdapEnv.put(Context.SECURITY_AUTHENTICATION, configuration.getString(Context.SECURITY_AUTHENTICATION, DEFAULT_SECURITY_AUTHENTICATION));
        ecasLdapEnv.put(Context.PROVIDER_URL, configuration.getString(ECAS_LDAP_URL));
        ecasLdapEnv.put(Context.SECURITY_PRINCIPAL, configuration.getString(ECAS_LDAP_PRINCIPAL));
        ecasLdapEnv.put(Context.SECURITY_CREDENTIALS, configuration.getString(ECAS_LDAP_CREDENTIALS));
    }

    @Override
    public String getDomain() {
        return domain;
    }

    private Set<String> getECASUserIdentities(String user) {
        Set<String> ecasUserIds = new HashSet<>();
        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(ecasLdapEnv);
            StringBuilder filter = new StringBuilder("(&(objectClass=inetOrgPerson)(|");
            filter.append("(uid=").append(user).append(")");
            filter.append("(mail=").append(user).append(")");
            filter.append("))");

            Enumeration<SearchResult> results = ctx.search(ecasPeopleBaseDn, filter.toString(), newSearchControls(ecasSearchTimeLimit, new String[]{"uid", "entryDN", "mail"}));
            while (results.hasMoreElements()) {
                SearchResult sr = results.nextElement();
                String mail = readLDAPAttribute(sr.getAttributes(), "mail");
                if (mail != null) {
                    ecasUserIds.add(mail);
                }
                String uid = readLDAPAttribute(sr.getAttributes(), "uid");
                if (uid != null) {
                    ecasUserIds.add(uid);
                }
            }
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return ecasUserIds;
    }

    private String readLDAPAttribute(Attributes attributes, String attributeName) throws NamingException {
        javax.naming.directory.Attribute attr = attributes.get(attributeName);
        return attr != null ? (String) attr.get() : null;
    }

    private SearchControls newSearchControls(int timeLimit, String[] attrs) {
        return new SearchControls(SearchControls.SUBTREE_SCOPE, 0, timeLimit, attrs, false, false);
    }

    private Set<String> filterPrincipalId(Set<String> input) {
        Set<String> output = new HashSet<String>(input.size());
        for (String principalId : input) {
            output.add(principalId.toLowerCase());
        }
        return output;
    }
}
