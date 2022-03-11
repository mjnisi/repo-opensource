package eu.trade.repo.security;

import static eu.trade.repo.TestConstants.*;
import static org.junit.Assert.*;

import java.util.Set;

import net.sf.ehcache.Cache;

import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.commons.configuration.Configuration;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.security.impl.OTPAuthenticationHandler;

public class OTPAuthenticationHandlerTest extends BaseTestClass {

	private static final String USER_SESSION_LIMIT = "security.userSessionLimit";
	private static final int DEFAULT_USER_SESSION_LIMIT = 4;
	private static final String USERNAME = "otp-test/test";
	private static final String CONTEXT_USERNAME = "otp/test";
	private static final String[] PRINCIPALS = {"/reader", "/writer"};

	@Autowired
	private Configuration combinedConfig;

	@Autowired
	private Cache securityCache;

	private int userSessionLimit;

	@Before
	public void resetHandler() throws Exception {
		OTPAuthenticationHandler.reset();
		setUser(USERNAME, "", TEST_REPO_4);
		setScenario(DatabaseOperation.CLEAN_INSERT, "callContext-scenario.xml");
		userSessionLimit = combinedConfig.getInt(USER_SESSION_LIMIT, DEFAULT_USER_SESSION_LIMIT);
	}

	@Test
	public void tesUser() throws Exception {
		assertEquals(CONTEXT_USERNAME, callContextHolder.getUsername());
		Set<String> principalIds = callContextHolder.getPrincipalIds();
		assertNotNull(principalIds);
		assertTrue(principalIds.contains(CONTEXT_USERNAME));
		principalIds.remove(CONTEXT_USERNAME);
		assertFalse(principalIds.isEmpty());
		for (String expectedPrincipal : PRINCIPALS) {
			assertTrue(principalIds.contains(expectedPrincipal));
		}
	}

	@Test(expected=CmisUnauthorizedException.class)
	public void tesOTP() throws Exception {
		try {
			callContextHolder.login();
		} catch (CmisUnauthorizedException e) {
			throw new IllegalStateException("Unexpected exception");
		}
		securityCache.removeAll();
		callContextHolder.login();
	}

	@Test
	public void tesOTPWithCache() throws Exception {
		callContextHolder.login();
		callContextHolder.login();
	}

	@Test(expected=CmisUnauthorizedException.class)
	public void tesOTPWithCacheExpiredKey() throws Exception {
		try {
			callContextHolder.login();
			for (int i = 0; i < userSessionLimit; i++) {
				setUser(USERNAME, "" + i, TEST_REPO_4);
				callContextHolder.login();
			}
			setUser(USERNAME, "", TEST_REPO_4);
		} catch (CmisUnauthorizedException e) {
			throw new IllegalStateException("Unexpected exception");
		}
		callContextHolder.login();
	}

	@Test(expected=CmisUnauthorizedException.class)
	public void tesOTPWithCacheRenewedKey() throws Exception {
		try {
			callContextHolder.login();
			for (int i = 0; i < userSessionLimit; i++) {
				setUser(USERNAME, "" + i, TEST_REPO_4);
				callContextHolder.login();
			}
			setUser(USERNAME, "" + 0, TEST_REPO_4);
			callContextHolder.login();
			setUser(USERNAME, "" + userSessionLimit, TEST_REPO_4);
			callContextHolder.login();
			// Is in cache because it has been renewed
			setUser(USERNAME, "" + 0, TEST_REPO_4);
			callContextHolder.login();
			// Is in cache because it has been removed
			setUser(USERNAME, "" + 1, TEST_REPO_4);
		} catch (CmisUnauthorizedException e) {
			throw new IllegalStateException("Unexpected exception");
		}
		callContextHolder.login();
	}

	@Test
	public void tesOTPAndCacheSize() throws Exception {
		for (int i = 0; i < userSessionLimit * 5; i++) {
			setUser(USERNAME, "" + i, TEST_REPO_4);
			callContextHolder.login();
		}
		int cacheSize = securityCache.getKeysWithExpiryCheck().size();
		assertEquals(userSessionLimit + 1, cacheSize);
	}
}
