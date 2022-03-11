package eu.trade.repo.security;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.util.Constants;
import eu.trade.repo.util.Utilities;

public class CallContextHolderTest extends BaseTestClass {

	private static final String TEST2_USER = "test2";
	private static final String TEST2_PWD = "test2";
	private static final String TEST3_USER = "/test2";
	private static final String TEST3_PWD = "test2";
	private static final String TEST4_USER = "builtin/test2";
	private static final String TEST4_PWD = "test2";
	private static final String FAIL_USER_1 = "test";
	private static final String FAIL_PWD_1 = "test2";
	private static final String FAIL_USER_2 = "";
	private static final String FAIL_USER_3 = "none/none";

	private static final Set<String> TEST_PRINCIPAL_IDS = Utilities.toSet(new String[]{"reader", "writer", Constants.PRINCIPAL_ID_ANYONE});
	private static final Set<String> TEST2_PRINCIPAL_IDS = Utilities.toSet(new String[]{"nest.admin", "reader", Constants.PRINCIPAL_ID_ANYONE});

	private static final Set<String> TEST2_PRINCIPAL_IDS_DOMAIN = Utilities.toSet(new String[]{"/nest.admin", "/reader", Constants.PRINCIPAL_ID_ANYONE});

	@Before
	public void setScenario() throws Exception {
		setScenario(DatabaseOperation.CLEAN_INSERT, "callContext-scenario.xml");
	}

	@Test
	public void testDefaultTesUser() {
		checkUser(TestConstants.TEST_USER, false, TEST_PRINCIPAL_IDS);
	}

	@Test
	public void testUserTest2() {
		testUser(TEST2_USER, TEST2_PWD, TestConstants.TEST_REPO_1, TEST2_USER, false, TEST2_PRINCIPAL_IDS);
	}

	@Test
	public void testUserTest3() {
		testUser(TEST3_USER, TEST3_PWD, TestConstants.TEST_REPO_1, TEST2_USER, false, TEST2_PRINCIPAL_IDS);
	}

	@Test
	public void testUserTest4() {
		testUser(TEST4_USER, TEST4_PWD, TestConstants.TEST_REPO_1, TEST2_USER, false, TEST2_PRINCIPAL_IDS);
	}

	@Test
	public void testUserTest5() {
		testUser(TEST2_USER, TEST2_PWD, TestConstants.TEST_REPO_2, CallContextHolder.PROTOCOL_SEP + TEST2_USER, false, TEST2_PRINCIPAL_IDS_DOMAIN);
	}

	@Test
	public void testUserTest6() {
		testUser(TEST3_USER, TEST3_PWD, TestConstants.TEST_REPO_2, CallContextHolder.PROTOCOL_SEP + TEST2_USER, false, TEST2_PRINCIPAL_IDS_DOMAIN);
	}

	@Test
	public void testUserTest7() {
		testUser(TEST4_USER, TEST4_PWD, TestConstants.TEST_REPO_2, CallContextHolder.PROTOCOL_SEP + TEST2_USER, false, TEST2_PRINCIPAL_IDS_DOMAIN);
	}

	@Test
	public void testUserFail1() {
		testFailingUser(FAIL_USER_1, FAIL_PWD_1, TestConstants.TEST_REPO_1);
	}

	@Test
	public void testUserFail2() {
		testFailingUser(FAIL_USER_2, TEST2_PWD, TestConstants.TEST_REPO_1);
	}

	@Test
	public void testUserFail3() {
		testFailingUser(FAIL_USER_3, TEST2_PWD, TestConstants.TEST_REPO_1);
	}

	@Test(expected=IllegalStateException.class)
	public void testUserAdminNoRepository() {
		testUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, null, CallContextHolder.PROTOCOL_SEP + TestConstants.ADMIN_USER, false, new HashSet<String>());
	}

	@Test
	public void testUserAdmin() {
		testUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TestConstants.TEST_REPO_1, TestConstants.ADMIN_USER, true, Collections.singleton(Constants.PRINCIPAL_ID_ANYONE));
	}

	@Test
	public void testUserAdmin2() {
		testUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TestConstants.TEST_REPO_2, CallContextHolder.PROTOCOL_SEP + TestConstants.ADMIN_USER, true, Collections.singleton(Constants.PRINCIPAL_ID_ANYONE));
	}

	private void testFailingUser(String user, String pwd, String repo) {
		try {
			setUser(user, pwd, repo);
			callContextHolder.login();
			fail("CmisUnauthorizedException expected.");
		} catch (CmisUnauthorizedException e) {
		}
	}

	private void testUser(String user, String pwd, String repo, String expectedUser, boolean expectedAdmin, Set<String> expectedPrincipalIds) {
		setUser(user, pwd, repo);
		checkUser(expectedUser, expectedAdmin, expectedPrincipalIds);
	}

	private void checkUser(String expectedUser, boolean expectedAdmin, Set<String> expectedPrincipalIds) {
		assertEquals(expectedUser, callContextHolder.getUsername());
		assertEquals(expectedAdmin, callContextHolder.isAdmin());
		Set<String> augmentedPrincipalIds = new HashSet<>(expectedPrincipalIds);
		augmentedPrincipalIds.add(expectedUser);
		assertTrue(equals(augmentedPrincipalIds, callContextHolder.getPrincipalIds()));
	}

	private boolean equals(Collection<?> expected, Collection<?> actual) {
		if (expected == actual) {
			return true;
		}
		if (expected == null) {
			return false;
		}
		if (expected.size() != actual.size()) {
			return false;
		}
		for (Object expectedItem : expected) {
			if (!actual.contains(expectedItem)) {
				return false;
			}
		}
		return true;

	}

}
