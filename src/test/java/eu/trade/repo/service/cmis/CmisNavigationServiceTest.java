package eu.trade.repo.service.cmis;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;

public class CmisNavigationServiceTest extends BaseTestClass {
	
	private String repositoryId = "tron_dev";
	private ExtensionsData mockExtension = mock(ExtensionsData.class);
	private String mockRenditionFilter = "mockRenditionFilter";
	
	@Override
	@Before
	public void initUser() {
		
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, repositoryId);
	}
	
	@Before
	public void loadH2Database() throws Exception {
		
		setScenario("scenarioNavigationService.xml", DatabaseOperation.CLEAN_INSERT);
	}
			
	@Test
	public void testGetFolderParent_folderIdByCmisId() throws Exception {			
		
		doTestGetFolderParent("202d2d4d18f889c87276f72a0e5472e623089200");		
	}
		
	@Test
	public void testGetFolderParent_folderIdByPath() throws Exception {			
							
		doTestGetFolderParent("/TRON/ECDB/2014");
	}
		
	private void doTestGetFolderParent(String folderId) throws Exception {			
		
		ObjectData actualObjectData = cmisNavigationService.getFolderParent(repositoryId, folderId, null, mockExtension);
		assertNotNull("", actualObjectData);
		assertTrue(actualObjectData.getId().equals("4591a57f84fe5235701ec38b5e56078899f76c58"));		
		
		actualObjectData = cmisNavigationService.getFolderParent(repositoryId, "/TRON/ECDB", null, mockExtension);
		assertNotNull("", actualObjectData);
		assertTrue(actualObjectData.getId().equals("53d538441e4b3ce664a5508a776b0fe15de2363a"));
		
		actualObjectData = cmisNavigationService.getFolderParent(repositoryId, "/TRON", null, mockExtension);
		assertNotNull("", actualObjectData);
		assertTrue(actualObjectData.getId().equals("b616f67dcc1626ef4f4e0c26a7f224162ba07304"));
	}
			
	@Test(expected=CmisObjectNotFoundException.class)
	public void testGetFolderParent_noParent() throws Exception {			
							
		cmisNavigationService.getFolderParent(repositoryId, "b616f67dcc1626ef4f4e0c26a7f224162ba07304", null, mockExtension);				
	}
		
	@Test
	public void testGetFolderTree_folderIdByPath() throws Exception {	
		doTestGetFolderTree_folderId("/TRON");		
	}
		
	@Test
	public void testGetFolderTree_folderIdByCmisId() throws Exception {	
		doTestGetFolderTree_folderId("53d538441e4b3ce664a5508a776b0fe15de2363a");	
	}		
		
	@Test
	public void testGetFolderTree_noResults() throws Exception {
		List<ObjectInFolderContainer> actualResults = cmisNavigationService.getFolderTree(repositoryId, "/TRON/Notifications", new BigInteger("-1"), null, false, IncludeRelationships.NONE, "mockRenditionFilter", false, mockExtension);
		
		assertNotNull(actualResults);
		assertTrue(actualResults.isEmpty());
	}
		
	@Test
	public void testDescendands_noResults() throws Exception {
		List<ObjectInFolderContainer> actualResults = cmisNavigationService.getDescendants(repositoryId, "/TRON/Notifications", new BigInteger("1"), null, false, IncludeRelationships.NONE, "mockRenditionFilter", false, mockExtension);
																						 		
		assertNotNull(actualResults);
		assertTrue(actualResults.isEmpty());
	}
		
	@Test
	public void testDescendands_folderIdByPath() throws Exception {	
		doTestDescendands_folderId("/TRON", new BigInteger("1"));
	}
		
	@Test
	public void testDescendands_folderIdByCmisId() throws Exception {	
		doTestDescendands_folderId("53d538441e4b3ce664a5508a776b0fe15de2363a", new BigInteger("1"));	
	}
		
	// wiht no depth defined, whitch in reality couldn't be happening,
	// because the client will call this service method throu CMIS Api services 
	// that it's validating the input parameters(including Depth) before calling the service
	// so. it's an accepted ERROR
	@Test(expected=java.lang.IllegalArgumentException.class)
	public void testDescendands_folderIdByPath_noDepth() throws Exception {	
		doTestDescendands_folderId("/TRON/Notifications", new BigInteger("0"));	
	}
		
	private void doTestGetFolderTree_folderId(String folderId) {
		String[] expectedCmisIds_asArray = new String[] {"8c82a61058b5c6030b99c93b33faf33a28e82ea4", "a4f2bbca5cb52e7de6bcc92d56f13b25d71bdfa4", "6749d23164225e7f1eff01d1644d05ab41476f0", "4fce3803afbc21911fa628f3d60ea964b0fd586b"};
			
		List<ObjectInFolderContainer> actualResults = cmisNavigationService.getFolderTree(repositoryId, folderId, new BigInteger("-1"), null, false, IncludeRelationships.NONE, "mockRenditionFilter", false, mockExtension);
		
		assertNotNull(actualResults);		
		assertTrue(actualResults.size() == 4);		
		assertActualAgainstExpected(expectedCmisIds_asArray, actualResults);
	}
		
	private void doTestDescendands_folderId(String folderId,BigInteger depth) {
		String[] expectedCmisIds_asArray = new String[] {"8c82a61058b5c6030b99c93b33faf33a28e82ea4", "a4f2bbca5cb52e7de6bcc92d56f13b25d71bdfa4", "6749d23164225e7f1eff01d1644d05ab41476f0", "4fce3803afbc21911fa628f3d60ea964b0fd586b"};
				
		List<ObjectInFolderContainer> actualResults = cmisNavigationService.getDescendants(repositoryId, folderId, depth, null, false, IncludeRelationships.NONE, "mockRenditionFilter", false, mockExtension);
		
		assertNotNull(actualResults);		
		assertTrue(actualResults.size() == 4);		
		assertActualAgainstExpected(expectedCmisIds_asArray, actualResults);
	}	
	
	@Test
	public void testGetChildrenByCmisId() {
		String[] expectedCmisIds_asArray = new String[] {"a4f2bbca5cb52e7de6bcc92d56f13b25d71bdfa4","6749d23164225e7f1eff01d1644d05ab41476f0","4fce3803afbc21911fa628f3d60ea964b0fd586b","4591a57f84fe5235701ec38b5e56078899f76c58","8c82a61058b5c6030b99c93b33faf33a28e82ea4"};
		 		
		ObjectInFolderList actualObjectInFolderList = cmisNavigationService.getChildren(repositoryId, "53d538441e4b3ce664a5508a776b0fe15de2363a", null, null, false, IncludeRelationships.NONE, mockRenditionFilter, false, new BigInteger("-1"), new BigInteger("0"), mockExtension);
		
		assertNotNull(actualObjectInFolderList);
		assertTrue(actualObjectInFolderList.getObjects().size() == 5);		
		assertActualAgainstExpected(expectedCmisIds_asArray, actualObjectInFolderList);
	}
		
	@Test
	public void testGetObjectParentsByCmisId() {
		String expectedParentId = "53d538441e4b3ce664a5508a776b0fe15de2363a";
		setUser(TestConstants.TEST2_USER, TestConstants.TEST2_PWD, repositoryId);
		
		List<ObjectParentData> actualParentResults = cmisNavigationService.getObjectParents(repositoryId, "a4f2bbca5cb52e7de6bcc92d56f13b25d71bdfa4", null, false, IncludeRelationships.NONE, mockRenditionFilter, false, mockExtension);
		
		assertNotNull(actualParentResults);
		assertTrue(actualParentResults.size() == 1);
		assertTrue(expectedParentId.equals(actualParentResults.get(0).getObject().getId()));
	}
	
	private void assertActualAgainstExpected(String[] expectedCmisIds_asArray, List<ObjectInFolderContainer> actualResults) {
		List<String> expectedCmisIds_asList = Arrays.asList(expectedCmisIds_asArray);
		List<String> actualIds = new ArrayList();
		for (ObjectInFolderContainer actualResult : actualResults) {			
			actualIds.add(actualResult.getObject().getObject().getId());	
		}
		for(String expectedCmisId : expectedCmisIds_asList) {
			assertTrue(actualIds.contains(expectedCmisId));
		}
	}
	
	private void assertActualAgainstExpected(String[] expectedCmisIds_asArray, ObjectInFolderList actualObjectInFolderList) {
		List<String> expectedCmisIds_asList = Arrays.asList(expectedCmisIds_asArray);
		List<String> actualIds = new ArrayList();
		for (ObjectInFolderData actualResult : actualObjectInFolderList.getObjects()) {			
			actualIds.add(actualResult.getObject().getId());	
		}
		for(String expectedCmisId : expectedCmisIds_asList) {
			assertTrue(actualIds.contains(expectedCmisId));
		}
	}

}
