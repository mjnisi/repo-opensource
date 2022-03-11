package eu.trade.repo.service.cmis.data.in;

import static eu.trade.repo.util.Constants.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.junit.Test;

public class AclBuilderTest {

	private static final String[] PRINCIPAL_TEST_1 = {"test1", "test2", "test1", "test3"};
	private static final String[][] PERMISSION_TEST_1 = {
		{"cmis:read", "cmis:all", "cmis:read"},
		{"cmis:read"},
		{"cmis:read", "cmis:all", "cmis:write"},
		{"cmis:write"}
	};
	private static final boolean[] IS_DIRECT_TEST_1 = {true, true, false, false};

	private static final String[] PRINCIPAL_TEST_2 = {PRINCIPAL_ID_USER, "test2", "test1", PRINCIPAL_ID_USER};
	private static final String[][] PERMISSION_TEST_2 = {
		{"cmis:read", "cmis:all", "cmis:read"},
		{"cmis:read"},
		{"cmis:read", "cmis:all", "cmis:write"},
		{"cmis:write"}
	};
	private static final boolean[] IS_DIRECT_TEST_2 = {true, true, false, false};

	private static final String CURRENT_USER = "CURRENT";
	private static final String[] PRINCIPAL_EXPECTED_2 = {CURRENT_USER, "test2", "test1", CURRENT_USER};

	public AclBuilderTest() {
	}

	@Test
	public void testBuildACL_1() {
		Acl acl = buildTestAcl(PRINCIPAL_TEST_1, PERMISSION_TEST_1, IS_DIRECT_TEST_1);
		Set<eu.trade.repo.model.Acl> builtAcl = AclBuilder.build(acl, CMIS_BASIC_PERMISSIONS, null);
		check(builtAcl, PRINCIPAL_TEST_1, PERMISSION_TEST_1, IS_DIRECT_TEST_1);
	}

	@Test
	public void testBuildACL_2() {
		Acl acl = buildTestAcl(PRINCIPAL_TEST_2, PERMISSION_TEST_2, IS_DIRECT_TEST_2);
		Set<eu.trade.repo.model.Acl> builtAcl = AclBuilder.build(acl, CMIS_BASIC_PERMISSIONS, CURRENT_USER);
		check(builtAcl, PRINCIPAL_EXPECTED_2, PERMISSION_TEST_1, IS_DIRECT_TEST_1);
	}

	private Acl buildTestAcl(String[] principalIds, String[][] permissions, boolean[] isDirect) {
		AccessControlListImpl acl = new AccessControlListImpl();
		List<Ace> aces = new ArrayList<>();
		acl.setAces(aces);
		acl.setExact(true);
		for (int i = 0; i < principalIds.length; i++) {
			AccessControlEntryImpl ace = new AccessControlEntryImpl();
			ace.setPrincipal(new AccessControlPrincipalDataImpl(principalIds[i]));
			ace.setDirect(isDirect[i]);
			List<String> acePermissions = new ArrayList<>();
			ace.setPermissions(acePermissions);
			for (int j = 0; j < permissions[i].length; j++) {
				acePermissions.add(permissions[i][j]);
			}
			aces.add(ace);
		}
		return acl;
	}

	private void check(Set<eu.trade.repo.model.Acl> builtAcl, String[] principalIds, String[][] permissions, boolean[] isDirect) {
		int size = 0;
		for (int i = 0; i < principalIds.length; i++) {
			for (int j = 0; j < permissions[i].length; j++) {
				assertTrue("Not found: " + principalIds[i] + ", " + permissions[i][j] + ", " + isDirect[i], find(builtAcl, principalIds[i], permissions[i][j], isDirect[i]));
				size++;
			}
		}
		assertEquals(size, builtAcl.size());
	}

	private boolean find(Set<eu.trade.repo.model.Acl> builtAcl, String principalId, String permission, boolean isDirect) {
		for (eu.trade.repo.model.Acl acl : builtAcl) {
			if (acl.getPrincipalId().equals(principalId) && acl.getIsDirect().equals(isDirect) && acl.getPermission().getName().equals(permission)) {
				return true;
			}
		}
		return false;
	}
}
