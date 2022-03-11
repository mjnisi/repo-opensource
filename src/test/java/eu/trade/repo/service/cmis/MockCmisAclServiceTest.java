package eu.trade.repo.service.cmis;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.spi.AclService;
import org.junit.Test;
import org.mockito.InjectMocks;

import eu.trade.repo.security.CallContextHolder;
import eu.trade.repo.service.cmis.data.out.AclBuilder;

public class MockCmisAclServiceTest extends AbstractCmisAclService {
	
	@InjectMocks
	private AclService cmisAclService = new CmisAclService();			
		
	@Test(expected=Exception.class)
	public void testAclService_aclServiceException() {
		when(aclService.getAcl(mockRepositoryId, mockObjectId)).thenThrow(new Exception());
		
	 	cmisAclService.getAcl(mockRepositoryId, mockObjectId, false, mockExtension);
	}
	
	@Test
	public void testAclService_nullAcl() {
		when(aclService.getAcl(mockRepositoryId, mockObjectId)).thenReturn(null);
		
		assert_emptyResults(cmisAclService.getAcl(mockRepositoryId, mockObjectId, false, mockExtension));
					
		verify(aclService, times(1)).getAcl(mockRepositoryId, mockObjectId);
		verifyNoMoreInteractions(aclService, security);
	}
	
	@Test
	public void testAclService_emptyAcl() {
		Set<eu.trade.repo.model.Acl> emptySet = getEmptyAcl();
		when(aclService.getAcl(mockRepositoryId, mockObjectId)).thenReturn(emptySet);
		
		assert_emptyResults(cmisAclService.getAcl(mockRepositoryId, mockObjectId, false, mockExtension));
				
		verify(aclService, times(1)).getAcl(mockRepositoryId, mockObjectId);
		verifyNoMoreInteractions(aclService, security);
	}
	
	@Test
	public void testAclService_nonEmptyAcl() {
		Set<eu.trade.repo.model.Acl> expectedAcl = getAcls(15,12,17);
		when(aclService.getAcl(mockRepositoryId, mockObjectId)).thenReturn(expectedAcl);
		
		assert_nonEmptyResults(expectedAcl, cmisAclService.getAcl(mockRepositoryId, mockObjectId, false, mockExtension));
				
		verify(aclService, times(1)).getAcl(mockRepositoryId, mockObjectId);
		verifyNoMoreInteractions(aclService, security);
	}	
	
	@Test(expected=Exception.class)
	public void testCompleteAclService_addAndRemoveAcl_securityException() {
		when(security.getPermissionNames(mockRepositoryId)).thenThrow(new Exception());
		
		cmisAclService.applyAcl(mockRepositoryId, mockObjectId, mock(Acl.class), mock(Acl.class), aclPropagation, mockExtension);
	}
	
	@Test
	public void testCompleteAclService_addAndRemoveAcl_emptyAcl() {
		Set<String> permissionNamesAllowedByRepository = getPermissionNamesAllowedByRepository(100, 20, 30, 70, 10);
		String mockUsername = "testUserSilviu";
		Set<eu.trade.repo.model.Acl> emptyAcls = getEmptyAcls();
		Acl resultFromEmptyAcl = AclBuilder.build(emptyAcls, false);
		
		when(security.getPermissionNames(mockRepositoryId)).thenReturn(permissionNamesAllowedByRepository);
		CallContextHolder mockCallContextHolder = mock(CallContextHolder.class);
		when(security.getCallContextHolder()).thenReturn(mockCallContextHolder);		
		when(mockCallContextHolder.getUsername()).thenReturn(mockUsername);					
		when(aclService.applyAcl(anyString(), anyString(), anySet(), any(AclPropagation.class))).thenReturn(emptyAcls);
		
		assert_emptyResults( cmisAclService.applyAcl(mockRepositoryId, mockObjectId, resultFromEmptyAcl, resultFromEmptyAcl, aclPropagation, mockExtension) );
					
	}	

	@Test	
	public void testCompleteAclService_addAndRemoveAcl_nonAllowedAcl() {
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
			cmisAclService.applyAcl(mockRepositoryId, mockObjectId, AclBuilder.build(getEmptyAcls(), false), resultFromScenarioAcls, aclPropagation, mockExtension);
			fail("the requested acl, identified by 777 is not allowed by Repository" );
		} catch(CmisConstraintException cmisConstraintException) {
			//cmisConstraintException.getMessage() = The permission name %s is not allowed by the repository
			//todo maybe should I verify the message inside the exception
		}			
	}	
	
	@Test
	public void testCompleteAclService_addAndRemoveAcl() {
		Set<String> permissionNamesAllowedByRepository = getPermissionNamesAllowedByRepository(100, 20, 30, 70, 10);
		String mockUsername = "testUserSilviu";		
		Set<eu.trade.repo.model.Acl> scenarioAcls = getAcls(100, 20); 
		Acl resultFromScenarioAcls = AclBuilder.build(scenarioAcls, false);
		
		when(security.getPermissionNames(mockRepositoryId)).thenReturn(permissionNamesAllowedByRepository);
		CallContextHolder mockCallContextHolder = mock(CallContextHolder.class);
		when(security.getCallContextHolder()).thenReturn(mockCallContextHolder);		
		when(mockCallContextHolder.getUsername()).thenReturn(mockUsername);
		when(aclService.applyAcl(anyString(), anyString(), anySet(), anySet(), any(AclPropagation.class))).thenReturn(scenarioAcls);
							
		//adding some acls, removing no acls
		assert_nonEmptyResults (scenarioAcls, cmisAclService.applyAcl(mockRepositoryId, mockObjectId, resultFromScenarioAcls, AclBuilder.build(getEmptyAcls(), false), aclPropagation, mockExtension));
				
	}
		
}
