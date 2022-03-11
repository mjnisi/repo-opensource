package eu.trade.repo.security;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Set;

import org.apache.commons.configuration.MapConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.trade.repo.security.impl.TronAuthorizationHandler;

/**
 * User: Ioan-Octavian Rusu
 * Date: 4/9/14
 * Time: 11:03 AM
 * 
 * Ignored because the LDAP production credentials were hard coded.
 */
@Ignore
public class TronAuthorizationHandlerTest {

    private static final String TEST_ECAS_ID = "gonzaen";
    private static final String TEST_ECAS_EMAIL = "esteban.gonzalez-juarros@ec.europa.eu";

    private static final String DOMAIN = "domain";

    private static final String GMS_GROUP_BASE_DN = "ou=Test,ou=Groups,dc=example,dc=com";
    private static final String GMS_LDAP_URL = "ldap://ldapdev.trade.cec.eu.int:1389";
    private static final String GMS_LDAP_PRINCIPAL = "cn=Directory Manager";
    private static final String GMS_LDAP_CREDENTIALS = "";
    private static final String GMS_LDAP_SEARCH_TIME_LIMIT = "1500";

    private static final String ECAS_PEOPLE_BASE_DN = "ou=people";
    private static final String ECAS_LDAP_URL = "ldap://cedprod.cec.eu.int:10389/o=cec.eu.int";
    private static final String ECAS_LDAP_PRINCIPAL = "uid=TRADE-Gaca,ou=TrustedApps,o=cec.eu.int";
    private static final String ECAS_LDAP_CREDENTIALS = "";
    private static final String ECAS_LDAP_SEARCH_TIME_LIMIT = "1500";

    private MapConfiguration configuration;

    @Before
    public void init() {
        configuration = new MapConfiguration(new HashMap<String, Object>());
        configuration.setDelimiterParsingDisabled(true);
        configuration.addProperty(TronAuthorizationHandler.DOMAIN, DOMAIN);

        configuration.addProperty(TronAuthorizationHandler.GMS_GROUP_BASE_DN, GMS_GROUP_BASE_DN);
        configuration.addProperty(TronAuthorizationHandler.GMS_LDAP_URL, GMS_LDAP_URL);
        configuration.addProperty(TronAuthorizationHandler.GMS_LDAP_PRINCIPAL, GMS_LDAP_PRINCIPAL);
        configuration.addProperty(TronAuthorizationHandler.GMS_LDAP_CREDENTIALS, GMS_LDAP_CREDENTIALS);
        configuration.addProperty(TronAuthorizationHandler.GMS_INCLUDE_GROUP_PARENTS, "true");
        configuration.addProperty(TronAuthorizationHandler.GMS_LDAP_SEARCH_TIME_LIMIT, GMS_LDAP_SEARCH_TIME_LIMIT);

        configuration.addProperty(TronAuthorizationHandler.ECAS_PEOPLE_BASE_DN, ECAS_PEOPLE_BASE_DN);
        configuration.addProperty(TronAuthorizationHandler.ECAS_LDAP_URL, ECAS_LDAP_URL);
        configuration.addProperty(TronAuthorizationHandler.ECAS_LDAP_PRINCIPAL, ECAS_LDAP_PRINCIPAL);
        configuration.addProperty(TronAuthorizationHandler.ECAS_LDAP_CREDENTIALS, ECAS_LDAP_CREDENTIALS);
        configuration.addProperty(TronAuthorizationHandler.ECAS_LDAP_SEARCH_TIME_LIMIT, ECAS_LDAP_SEARCH_TIME_LIMIT);
    }

    @Test
    public void testGetPrincipalIdsByECASEmailAddress() {
        TronAuthorizationHandler authorizationHandler = new TronAuthorizationHandler();
        authorizationHandler.init(configuration);
        Set<String> groups = authorizationHandler.getPrincipalIds(TEST_ECAS_EMAIL);
        assertTrue(groups.size() >= 4);
        boolean found = false;
        for (String dns : groups) {
            if(dns.equalsIgnoreCase(GMS_GROUP_BASE_DN)){
                found = true;
            }
        }
        assertTrue(found);
        assertTrue(groups.contains(TEST_ECAS_EMAIL));
        assertTrue(groups.contains(TEST_ECAS_ID));
    }

    @Test
    public void testGetPrincipalIdsByECASId() {
        TronAuthorizationHandler authorizationHandler = new TronAuthorizationHandler();
        authorizationHandler.init(configuration);
        Set<String> groups = authorizationHandler.getPrincipalIds(TEST_ECAS_ID);
        assertTrue(groups.size() >= 4);
        boolean found = false;
        for (String dns : groups) {
            if(dns.equalsIgnoreCase(GMS_GROUP_BASE_DN)){
                found = true;
            }
        }
        assertTrue(found);
        assertTrue(groups.contains(TEST_ECAS_EMAIL));
        assertTrue(groups.contains(TEST_ECAS_ID));
    }
}
