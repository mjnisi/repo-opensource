package eu.trade.repo.service.cmis;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.junit.Test;
import org.mockito.InjectMocks;

import eu.trade.repo.security.CallContextHolder;
import eu.trade.repo.service.cmis.data.out.AclBuilder;

public class MockCmisCompleteAclServiceTest extends AbstractCmisAclService {
	
	@InjectMocks
	private CompleteAclService completeAclService = new CmisAclService();					
			
	@Test(expected=Exception.class)
	public void testCompleteAclService_setAcl_securityException() {
		when(security.getPermissionNames(mockRepositoryId)).thenThrow(new Exception());
		
		completeAclService.applyAcl(mockRepositoryId, mockObjectId, mock(Acl.class), aclPropagation);
	}
	
	@Test
	public void testCompleteAclService_setAcl_emptyAcl() {
		Set<String> permissionNamesAllowedByRepository = getPermissionNamesAllowedByRepository(100, 20, 30, 70, 10);
		String mockUsername = "testUserSilviu";
		Set<eu.trade.repo.model.Acl> emptyAcls = getEmptyAcls();
		Acl resultFromEmptyAcl = AclBuilder.build(emptyAcls, false);
		
		when(security.getPermissionNames(mockRepositoryId)).thenReturn(permissionNamesAllowedByRepository);
		CallContextHolder mockCallContextHolder = mock(CallContextHolder.class);
		when(security.getCallContextHolder()).thenReturn(mockCallContextHolder);		
		when(mockCallContextHolder.getUsername()).thenReturn(mockUsername);					
		when(aclService.applyAcl(anyString(), anyString(), anySet(), any(AclPropagation.class))).thenReturn(emptyAcls);
		
		assert_emptyResults( completeAclService.applyAcl(mockRepositoryId, mockObjectId, resultFromEmptyAcl, aclPropagation) );
					
	}
	
	@Test
	public void testCompleteAclService_setAcl_nonAllowedAcl() {
		Set<String> permissionNamesAllowedByRepository = getPermissionNamesAllowedByRepository(100, 20, 30, 70, 10);
		String mockUsername = "testUserSilviu";
		// the build permission for 777 value is not between the allowed repository permissions 
		Set<eu.trade.repo.model.Acl> scenarioAcls = getAcls(777, 20); 
		Acl resultFromScenarioAcls = AclBuilder.build(scenarioAcls, false);
		
		when(security.getPermissionNames(mockRepositoryId)).thenReturn(permissionNamesAllowedByRepository);
		CallContextHolder mockCallContextHolder = mock(CallContextHolder.class);
		when(security.getCallContextHolder()).thenReturn(mockCallContextHolder);		
		when(mockCallContextHolder.getUsername()).thenReturn(mockUsername);
		when(aclService.applyAcl(anyString(), anyString(), anySet(), any(AclPropagation.class))).thenReturn(scenarioAcls);			
		
		try {
			completeAclService.applyAcl(mockRepositoryId, mockObjectId, resultFromScenarioAcls, aclPropagation);
			fail("the requested acl, identified by 777 is not allowed by Repository" );
		} catch(CmisConstraintException cmisConstraintException) {
			//cmisConstraintException.getMessage() = The permission name %s is not allowed by the repository
			//todo maybe should I verify the message inside the exception
		}
				
	}	
	
	@Test
	public void testCompleteAclService_setAcl_nonEmptyAcl() {
		Set<String> permissionNamesAllowedByRepository = getPermissionNamesAllowedByRepository(100, 20, 30, 70, 10);
		String mockUsername = "testUserSilviu";
		Set<eu.trade.repo.model.Acl> scenarioAcls = getAcls(100, 20);
		Acl resultFromScenarioAcls = AclBuilder.build(scenarioAcls, false);
		
		when(security.getPermissionNames(mockRepositoryId)).thenReturn(permissionNamesAllowedByRepository);
		CallContextHolder mockCallContextHolder = mock(CallContextHolder.class);
		when(security.getCallContextHolder()).thenReturn(mockCallContextHolder);		
		when(mockCallContextHolder.getUsername()).thenReturn(mockUsername);					
		when(aclService.applyAcl(anyString(), anyString(), anySet(), any(AclPropagation.class))).thenReturn(scenarioAcls);
				
		assert_nonEmptyResults(scenarioAcls, completeAclService.applyAcl(mockRepositoryId, mockObjectId, resultFromScenarioAcls, aclPropagation));				
				
	}			
		
}
