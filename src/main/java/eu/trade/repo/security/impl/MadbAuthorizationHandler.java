package eu.trade.repo.security.impl;

import eu.trade.repo.security.AuthorizationHandler;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.*;

/**
 * Created by hadamto on 14/09/2016.
 */
public class MadbAuthorizationHandler extends DbAuthorizationHandler implements AuthorizationHandler {

    public static final String DOMAIN = "domain";
    private static final Logger LOG = LoggerFactory.getLogger(MadbAuthorizationHandler.class);
    private static final String DEFAULT_PROVIDER_URL = "ldap://cedprod.cec.eu.int:10389/o=cec.eu.int";
    private static final String DEFAULT_SECURITY_AUTHENTICATION = "simple";
    private static final String DEFAULT_INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String ECAS_LDAP_BRANCH = "ou=People";
    private static final String PERSON_ID = "uid";
    private static final String COMMISSION = "COM";
    private static final String SOURCE_ORGANISATION_LDAP_PROP = "sourceOrganisation";
    private static final String DG_LDAP_PROP = "dg";
    private static final String TRADE = "TRADE";

    private final Hashtable ldapEnv = new Hashtable();
    private String domain;
    @Override
    public Set<String> getPrincipalIds(String username) {
//        Set<String> principalIds = super.getPrincipalIds(username);
//        principalIds.addAll(getPrincipalsFromLdap(username));
        Set<String> principals = new HashSet<>();
        principals.add("SPS:MANAGER");
        principals.add("SPS:ADMIN");
        principals.add("TRADE");
        principals.add("COM");
        return principals;
    }

    private Set<String> getPrincipalsFromLdap(String username) {
        DirContext ctx = null;
        Set<String> ldapPrincipals = new HashSet<>();
        try {
            ctx = new InitialDirContext(ldapEnv);
            Attributes matchAttrs = new BasicAttributes(true);
            matchAttrs.put(new BasicAttribute(PERSON_ID, username));
            NamingEnumeration<SearchResult> result = ctx.search(ECAS_LDAP_BRANCH, matchAttrs);
            while (result.hasMoreElements()) {
                SearchResult searchResult = result.nextElement();
                Attributes attributes = searchResult.getAttributes();
                NamingEnumeration<? extends Attribute> allAttributes = attributes.getAll();
                while (allAttributes.hasMore()) {
                    Attribute next = allAttributes.next();
                    if(next.getID().equals(SOURCE_ORGANISATION_LDAP_PROP) && next.get().toString().equals(COMMISSION)){
                        ldapPrincipals.add(COMMISSION);
                    }
                    if(next.getID().equals(DG_LDAP_PROP) && next.get().toString().equals(TRADE)){
                        ldapPrincipals.add(TRADE);
                    }
                }
            }
        }
        catch (NamingException e) {
            LOG.error("Error reading from LDAP, " + e.getMessage(), e);
            throw new IllegalStateException("Error reading from LDAP", e);
        }
        finally {
            close(ctx);
        }
        return ldapPrincipals;
    }

    private void close(DirContext ctx) {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException e) {
                LOG.error("Error closing LDAP connection, " + e.getMessage(), e);
                throw new IllegalStateException("Error closing LDAP connection", e);
            }
        }
    }
    @Override
    public boolean isAdmin(String username) {
        return false;
    }

    @Override
    public void init(Configuration configuration) {
        super.init(configuration);
        domain = configuration.getString(DOMAIN);
        ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, configuration.getString("ldapContextFactory", DEFAULT_INITIAL_CONTEXT_FACTORY));
        ldapEnv.put(Context.SECURITY_AUTHENTICATION, configuration.getString("ldapSecurityAuthentication", DEFAULT_SECURITY_AUTHENTICATION));
        ldapEnv.put(Context.PROVIDER_URL, configuration.getString("ldapUrl", DEFAULT_PROVIDER_URL));
        ldapEnv.put(Context.SECURITY_PRINCIPAL, configuration.getString("ldapUser"));
        ldapEnv.put(Context.SECURITY_CREDENTIALS, configuration.getString("ldapCredentials"));

    }

    @Override
    public String getDomain() {
        return domain;
    }
}
