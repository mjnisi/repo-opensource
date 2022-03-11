package eu.trade.repo.service.cmis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.service.cmis.data.out.AclBuilder;

public class CmisAclServiceTest extends BaseTestClass {

	private String repositoryId = "tron_dev";
	
	private ExtensionsData mockExtension = mock(ExtensionsData.class);
	
	@Override
	@Before
	public void initUser() {
		
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, repositoryId);
	}
	
	@Before
	public void loadH2Database() throws Exception {
		
		setScenario("scenarioCmisAclService.xml", DatabaseOperation.CLEAN_INSERT);
	}
	
	/**
	 * input 	: objectId [cmisId=nonExistentId] have no rows on object table
	 * scenario : runnig cmisAclService.getAcl
	 * expected	: exception when trying to find the object
	 */
	@Test(expected=CmisObjectNotFoundException.class)
	public void testGetAcl_notFoundObject() {		
		
		cmisAclService.getAcl(repositoryId, "nonExistentId", false, mockExtension);							
	}
	
	/**
	 * input 	: objectId [cmisId=282a94d493643b6dda03f56d2e0f51fbdfe4083] have no rows on acl table
	 * scenario : runnig cmisAclService.getAcl
	 * expected : Acl object returned by cmisAclService.getAcl should return empty acls collection
	 */
	@Test
	public void testGetAcl_emptyAcl() {		
		
		assert_emptyResult( cmisAclService.getAcl(repositoryId, "282a94d493643b6dda03f56d2e0f51fbdfe4083", false, mockExtension) );						
	}
	
	/**
	 * input 	: objectId [cmisId=7e34082628445aee97f42e5bd2d3445a56e54b83] have 2 rows on acl table
	 * scenario : runnig cmisAclService.getAcl
	 * expected : Acl object returned by cmisAclService.getAcl should return acls collection with those 2 rows
	 */
	@Test	
	public void testGetAcl() {	
		String[] expectedPrincipalId = new String[] {"tron/cn=export credit,ou=tron,ou=groups,dc=example,dc=com", "tron/cn=export credit admin,ou=tron,ou=groups,dc=example,dc=com"};
		String[] expectedPermissionName = new String[] {"cmis:read", "cmis:all"};
		Map<String, String> expectedMapping = getExpectedMapping(new AclDefinition(expectedPrincipalId[0], expectedPermissionName[0])
																,new AclDefinition(expectedPrincipalId[1], expectedPermissionName[1]));
		
		assert_nonEmptyResult (expectedMapping, cmisAclService.getAcl(repositoryId, "7e34082628445aee97f42e5bd2d3445a56e54b83", false, mockExtension));						
	}
	
	/**
	 * input 	: objectId [cmisId=282a94d493643b6dda03f56d2e0f51fbdfe4083] have no rows on acl table
	 * scenario : runnig cmisAclService.applyAcl with empty removalAcl, empty addingAcl collections 
	 * expected : Acl object returned by cmisAclService.applyAcl should return empty acls collection
	 */
	@Test	
	public void testApplyAcl_noRemoval_noAdding_onNoAcl() {		
		Set<eu.trade.repo.model.Acl> emptyAcl = Collections.<eu.trade.repo.model.Acl>emptySet();
		Acl resultFromEmptyAcl = AclBuilder.build(emptyAcl, false);
		
		assert_emptyResult(cmisAclService.applyAcl(repositoryId, "282a94d493643b6dda03f56d2e0f51fbdfe4083", resultFromEmptyAcl, resultFromEmptyAcl, AclPropagation.OBJECTONLY, mockExtension));
						
	}
	
	/**
	 * input 	: objectId [cmisId=7e34082628445aee97f42e5bd2d3445a56e54b83] have 2 rows on acl table
	 * scenario : runnig cmisAclService.applyAcl with empty removalAcl, empty addingAcl collections 
	 * expected : Acl object returned by cmisAclService.applyAcl should return those 2 rows on acls collection
	 */
	@Test	
	public void testApplyAcl_noRemoval_noAdding_onExistentAcl() {
		String[] expectedPrincipalId = new String[] {"tron/cn=export credit,ou=tron,ou=groups,dc=example,dc=com", "tron/cn=export credit admin,ou=tron,ou=groups,dc=example,dc=com"};
		String[] expectedPermissionName = new String[] {"cmis:read", "cmis:all"};
		Map<String, String> expectedMapping = getExpectedMapping(new AclDefinition(expectedPrincipalId[0], expectedPermissionName[0])
																,new AclDefinition(expectedPrincipalId[1], expectedPermissionName[1]));		
		Set<eu.trade.repo.model.Acl> emptyAcl = Collections.<eu.trade.repo.model.Acl>emptySet();
		Acl resultFromEmptyAcl = AclBuilder.build(emptyAcl, false);
		
		assert_nonEmptyResult( expectedMapping, cmisAclService.applyAcl(repositoryId, "7e34082628445aee97f42e5bd2d3445a56e54b83", resultFromEmptyAcl, resultFromEmptyAcl, AclPropagation.OBJECTONLY, mockExtension));				
	}
	
	/**
	 * input 	: objectId [cmisId=282a94d493643b6dda03f56d2e0f51fbdfe4083] have 0 rows on acl table
	 * scenario1: runnig cmisAclService.applyAcl with empty removalAcl, no empty addingAcl(1 row)  
	 * expected : Acl object returned by cmisAclService.applyAcl should return 1 rows on acls collection
	 * scenario2: runnig cmisAclService.applyAcl with empty removalAcl, no empty addingAcl(1 row) 
	 * expected : Acl object returned by cmisAclService.applyAcl should return 2 rows on acls collection
	 * scenario3: runnig cmisAclService.applyAcl with non empty removalAcl(1 row), empty addingAcl collections
	 * expected : Acl object returned by cmisAclService.applyAcl should return 1 rows on acls collection
	 * scenario4: runnig cmisAclService.applyAcl with non empty removalAcl(1 row), empty addingAcl collection
	 * expected : Acl object returned by cmisAclService.applyAcl should throw an Exception (acl has to be NON empty)
	 * scenario5: runnig cmisAclService.applyAcl with non empty removalAcl(1 row-- the existent one), non empty addingAcl(1 row)
	 * expected : Acl object returned by cmisAclService.applyAcl should return empty acls collection
	 */
	@Test
	public void testApplyAcl_justRemoval_or_justAdding() {
		String objectCmisId = "282a94d493643b6dda03f56d2e0f51fbdfe4083";
		String[] expectedPrincipalId = new String[] {"tron/cn=export credit,ou=tron,ou=groups,dc=example,dc=com", "tron/cn=export credit admin,ou=tron,ou=groups,dc=example,dc=com"};
		String[] expectedPermissionName = new String[] {"cmis:read", "cmis:all"};
		Set<eu.trade.repo.model.Acl> emptyAcl = Collections.<eu.trade.repo.model.Acl>emptySet();
		Acl resultFromEmptyAcl = AclBuilder.build(emptyAcl, false);

		// scenario1 
		AclDefinition aclDefinitionScenario1 = new AclDefinition(expectedPrincipalId[0], expectedPermissionName[0]);
		Acl addingAclScenoario1 = getAclScenario(aclDefinitionScenario1);
		Map<String, String> expectedMapping1 = getExpectedMapping(aclDefinitionScenario1);
		
		assert_nonEmptyResult(expectedMapping1, cmisAclService.applyAcl(repositoryId, objectCmisId, addingAclScenoario1, resultFromEmptyAcl, AclPropagation.OBJECTONLY, mockExtension));

		// scenario2		
		AclDefinition aclDefinitionScenario2 = new AclDefinition(expectedPrincipalId[1], expectedPermissionName[1]);
		Acl addingAclScenoario2 = getAclScenario(aclDefinitionScenario2);
		Map<String, String> expectedMapping2 = getExpectedMapping(aclDefinitionScenario1, aclDefinitionScenario2);
		
		assert_nonEmptyResult(expectedMapping2, cmisAclService.applyAcl(repositoryId, objectCmisId, addingAclScenoario2, resultFromEmptyAcl, AclPropagation.OBJECTONLY, mockExtension));
		
		// scenario3
		AclDefinition aclDefinitionScenario3 = aclDefinitionScenario2;
		Acl removingAclScenoario3 = getAclScenario(aclDefinitionScenario3);
		Map<String, String> expectedMapping3 = getExpectedMapping(aclDefinitionScenario1);
		
		assert_nonEmptyResult(expectedMapping3, cmisAclService.applyAcl(repositoryId, objectCmisId, resultFromEmptyAcl, removingAclScenoario3, AclPropagation.OBJECTONLY, mockExtension));
		
		// scenario4
		AclDefinition aclDefinitionScenario4 = aclDefinitionScenario1;
		Acl removingAclScenoario4 = getAclScenario(aclDefinitionScenario4);				
		try {
			cmisAclService.applyAcl(repositoryId, objectCmisId, resultFromEmptyAcl, removingAclScenoario4, AclPropagation.OBJECTONLY, mockExtension);
			fail("cmisAclService should have thrown an exception");
		} catch(CmisConstraintException cmisConstraintException) {
			String startingExceptionMessage = "The resulting ACL for the object cannot be empty, please review it: ";
			assertTrue(cmisConstraintException.getMessage().startsWith(startingExceptionMessage));			
		}
				
		//scenario5
		AclDefinition aclRemovalDefinitionScenario5 = aclDefinitionScenario1;
		Acl removingAclScenoario5 = getAclScenario(aclRemovalDefinitionScenario5);
		AclDefinition aclAddingDefinitionScenario5 = aclDefinitionScenario2;
		Acl addingAclScenoario5 = getAclScenario(aclAddingDefinitionScenario5);
		Map<String, String> expectedMapping5 = getExpectedMapping(aclDefinitionScenario2);
		
		assert_nonEmptyResult(expectedMapping5, cmisAclService.applyAcl(repositoryId, objectCmisId, addingAclScenoario5, removingAclScenoario5, AclPropagation.OBJECTONLY, mockExtension));
						
	}
	
	private Acl getAclScenario(AclDefinition aclDefinitionScenario) {
		
		Set<eu.trade.repo.model.Acl> aclsScenario = getAcl(aclDefinitionScenario);
		Acl aclScenoario = AclBuilder.build(aclsScenario, false);
		return aclScenoario;
	}
	
	private Set<eu.trade.repo.model.Acl> getAcl(AclDefinition ... aclDefinitions) {
		
		Set<eu.trade.repo.model.Acl> acls = new HashSet();
		eu.trade.repo.model.Acl acl = new eu.trade.repo.model.Acl();
		for (AclDefinition aclDefinition : aclDefinitions) {
			acl.setPrincipalId(aclDefinition.getPrincipalId());
			acl.setPermission(permSelector.getPermission(aclDefinition.getPermissionName(), repositoryId));
			acls.add(acl);
		}
		return acls;
	}

	private void assert_emptyResult(Acl actualAcl) {
		assertNotNull(actualAcl);
		assertFalse(actualAcl.isExact());
		List<Ace> actualAces = actualAcl.getAces(); 
		assertNotNull(actualAces);
		assertTrue(actualAces.isEmpty());
	}
	
	private void assert_nonEmptyResult(Map<String, String> expectedMappingPrincipalAndPermission, Acl actualAcl) {
		assertNotNull(actualAcl);
		assertFalse(actualAcl.isExact());
		List<Ace> actualAces = actualAcl.getAces(); 
		assertNotNull(actualAces);
		assertTrue(actualAces.size() == expectedMappingPrincipalAndPermission.size());
		Map<String, String> actualMappingPrincipalAndPermission = new HashMap();
		for(Ace actualAce : actualAces ) {
			String actualPrincipal = actualAce.getPrincipal().getId();
			String actualPermission = actualAce.getPermissions().get(0);
			actualMappingPrincipalAndPermission.put(actualPrincipal, actualPermission);
		}
		for(String expectedPrincipal : expectedMappingPrincipalAndPermission.keySet()) {
			assertEquals(expectedMappingPrincipalAndPermission.get(expectedPrincipal), actualMappingPrincipalAndPermission.get(expectedPrincipal));
		}
	}	
	
	private Map<String, String> getEmptyExpectedMapping(){
		return getExpectedMapping();
	}
	
	private Map<String, String> getExpectedMapping(AclDefinition ... aclDefinitions){
		Map<String, String> expectedMappingPrincipalAndPermission = new HashMap();	
		for (AclDefinition aclDefinition : aclDefinitions) {
			expectedMappingPrincipalAndPermission.put(aclDefinition.getPrincipalId(), aclDefinition.getPermissionName());
		}
				
		return expectedMappingPrincipalAndPermission;
	}
	
	class AclDefinition {
		private String principalId;
		private String permissionName;
		public AclDefinition(String principalId, String permissionName) {
			this.principalId = principalId;
			this.permissionName = permissionName;
		}
		public String getPrincipalId() {
			return principalId;
		}
		public String getPermissionName() {
			return permissionName;
		}
		
	}
}
