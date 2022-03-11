package eu.trade.repo.security;

import static eu.trade.repo.TestConstants.*;
import static org.junit.Assert.*;

import java.util.Set;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.junit.Ignore;

import eu.trade.repo.BaseTestClass;

/**
 * TODO: Mock it
 * @author porrjai
 */
@Ignore
public class DbAuthorizationHandlerTest extends BaseTestClass {

	private static final String USERNAME = "mock/porrjai";
	private static final String CONTEXT_USERNAME = "ecas/porrjai";
	private static final String[] PRINCIPALS = {"gaca/DUMA2.ModifyExternalUser", "gaca/DUMA2.ResetUserPassword", "gaca/DUMA2.LockUser"};

	@Test
	public void testGacaUser() throws Exception {
		setUser(USERNAME, "", TEST_REPO_3);
		setScenario(DatabaseOperation.CLEAN_INSERT, "callContext-scenario.xml");
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
}
