package eu.trade.repo.service.cmis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import eu.trade.repo.model.Permission;
import eu.trade.repo.security.Security;
import eu.trade.repo.service.interfaces.IAclService;

public abstract class AbstractCmisAclService {

	@Mock
	protected IAclService aclService;

	@Mock
	protected Security security;
	protected static final String mockRepositoryId = "mockRepositoryId";
	
	protected static final String mockObjectId = "mockObjectId";
	protected static final String MOCK_PRINCIPAL_PREFIX = "mockPrincipalPrefix";
	protected static final String MOCK_PERMISSIONNAME_PREFIX = "mockPermissionNamePrefix";
	
	protected ExtensionsData mockExtension = mock(ExtensionsData.class);
	protected AclPropagation aclPropagation = AclPropagation.OBJECTONLY;
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}
	
	protected void assert_nonEmptyResults(Set<eu.trade.repo.model.Acl> expectedAcls, Acl actualAcls) {
		assertNotNull(actualAcls);
		assertNotNull(actualAcls.getAces());
		List<org.apache.chemistry.opencmis.commons.data.Ace> actualAces = actualAcls.getAces();
		assertTrue(actualAces.size() == expectedAcls.size());
		Map<String, String> actualPrincipalIdByPermission = new HashMap();
		for (org.apache.chemistry.opencmis.commons.data.Ace actualAce : actualAces) {
			actualPrincipalIdByPermission.put(actualAce.getPrincipalId(), actualAce.getPermissions().get(0));
		}
		for (eu.trade.repo.model.Acl expectedAcl : expectedAcls) {
			assertTrue(actualPrincipalIdByPermission.get(expectedAcl.getPrincipalId()).equals(expectedAcl.getPermission().getName()));			
		}			
		
		assertFalse(actualAcls.isExact());
	}

	protected void assert_emptyResults(Acl actualAcl) {
		assertNotNull(actualAcl);
		assertNotNull(actualAcl.getAces());
		assertTrue(actualAcl.getAces().isEmpty());
		assertFalse(actualAcl.isExact());
	}
	
	protected Set<eu.trade.repo.model.Acl> getAcls(Integer ... aclIds) {
		Set<eu.trade.repo.model.Acl> acls = new HashSet();
		for (Integer aclId : aclIds) {
			acls.add(getAcl(aclId));
		}
		
		return acls;
	}
	
	protected Set<eu.trade.repo.model.Acl> getEmptyAcls() {
		
		return getAcls();
	}
	
	protected eu.trade.repo.model.Acl getAcl(Integer aclId) {
		eu.trade.repo.model.Acl acl = new eu.trade.repo.model.Acl();
		acl.setPrincipalId(MOCK_PRINCIPAL_PREFIX + aclId);	
		acl.setPermission(getMockPermission(aclId));
		return acl;
	}
	
	protected Permission getMockPermission(Integer aclId) {
		Permission mockPermission = mock(Permission.class);
		when(mockPermission.getName()).thenReturn(MOCK_PERMISSIONNAME_PREFIX + aclId);
		return mockPermission;
	}	

	protected Set<eu.trade.repo.model.Acl> getEmptyAcl() {		
		return Collections.emptySet();
		
	}
	
	protected Set<String> getPermissionNamesAllowedByRepository(Integer ... permissionIds) {
		Set<String> permissions = new HashSet();
		for(Integer permissionId : permissionIds) {
			permissions.add(MOCK_PERMISSIONNAME_PREFIX + permissionId);
		}
		
		return permissions;
	}	
}
