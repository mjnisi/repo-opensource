package eu.trade.repo.security;

import eu.trade.repo.security.impl.MadbAuthorizationHandler;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.naming.Context;
import java.util.HashMap;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by hadamto on 14/09/2016.
 */
public class MadbAuthorizationHandlerTest {

    private MapConfiguration configuration;
    private static final String DOMAIN = "madb";

    @Before
    public void init() {
        configuration = new MapConfiguration(new HashMap<String, Object>());
        configuration.setDelimiterParsingDisabled(true);
        configuration.addProperty(MadbAuthorizationHandler.DOMAIN, DOMAIN);

        //db
        Configuration datasource = configuration.subset("datasource");
        datasource.addProperty("driverClassName", "oracle.jdbc.OracleDriver");
        datasource.addProperty("url", "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=serverdb.trade.cec.eu.int)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=dev.ec.europa.eu)))");
        datasource.addProperty("username", "username");
        datasource.addProperty("password", "******");
        configuration.addProperty("authoritiesByUsernameQuery", "select distinct R.ROLE_CODE from USERS U, USER_ROLES UR, ROLES R where U.USER_ID = UR.USER_ID and UR.ROLE_ID = R.ROLE_ID and U.LOGIN = 'hadamto' and R.ROLE_CODE like 'MADB.%'");
        //ldap
        configuration.addProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        configuration.addProperty(Context.SECURITY_AUTHENTICATION, "simple");
        configuration.addProperty(Context.PROVIDER_URL, "ldap://cedprod.cec.eu.int:10389/o=cec.eu.int");
        configuration.addProperty(Context.SECURITY_CREDENTIALS, "******");
        configuration.addProperty(Context.SECURITY_PRINCIPAL, "username");
    }

    @Test @Ignore
    public void testLdap() {
        MadbAuthorizationHandler ldapAuthorizationHandler = new MadbAuthorizationHandler();
        ldapAuthorizationHandler.init(configuration);

        Set<String> hadamtoLdapPrinciples = ldapAuthorizationHandler.getPrincipalIds("hadamto");
        assertTrue(hadamtoLdapPrinciples.contains("COM"));
        assertTrue(hadamtoLdapPrinciples.contains("TRADE"));

        Set<String> stravalLdapPrinciples = ldapAuthorizationHandler.getPrincipalIds("straval");
        assertTrue(stravalLdapPrinciples.contains("COM"));
    }

}
