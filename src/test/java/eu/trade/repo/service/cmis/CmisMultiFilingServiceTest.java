package eu.trade.repo.service.cmis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.spi.MultiFilingService;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;

public class CmisMultiFilingServiceTest extends BaseTestClass {
	
	private final String repositoryId = "tron_dev";
	private final ExtensionsData mockExtension = mock(ExtensionsData.class);
		
	@Autowired
	private MultiFilingService cmisMultiFilingService;
	
	@Override
	@Before
	public void initUser() {
		
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, repositoryId);
	}
	
	@Before
	public void loadH2Database() throws Exception {
		
		setScenario("scenarioMultiFilingService.xml", DatabaseOperation.CLEAN_INSERT);
	}
	
	/**
	 * input 	: documentId & his parents { folderIds[0], folderIds[1] } 
	 * scenario1: running cmisMultiFilingService.removeObjectFromFolder(documentId, folderIds[0])
	 * expected : remaining parents for documentId : {folderIds[1]}
	 * scenario2: running cmisMultiFilingService.removeObjectFromFolder(documentId, folderIds[1])
	 * expected : CmisPermissionDeniedException("Cannot unfile all the objects in the version series:")
	 */
	@Test
	public void testRemoveObjectFromFolder() {
		String[] expectedParentFolderIds = new String[] {"f2b09196822fb0bc1b3bb6103248ba93f700e055", "4a41d6a497d3d1db5ef4db1d10e424585817a952"};	
		String firstRemovalFromParentId = expectedParentFolderIds[0];
		String expectedDocumentId = "4afef5226c0d2bcb7f7d7ac1065df8544f27b75a";
	
		//scenario1 :runnig the testing scenario
		cmisMultiFilingService.removeObjectFromFolder(repositoryId, expectedDocumentId, firstRemovalFromParentId, mockExtension);
		
		//verifying the parents relation ship after running the scenario
		List<ObjectParentData> parentsAfterRemoval = cmisNavigationService.getObjectParents(repositoryId, expectedDocumentId, null, false, IncludeRelationships.NONE, "mockRenditionFilter", false, mockExtension);
		assertNotNull(parentsAfterRemoval);
		assertFalse(checkIfIsParentForChild(expectedParentFolderIds[0], parentsAfterRemoval));
		assertTrue(checkIfIsParentForChild(expectedParentFolderIds[1], parentsAfterRemoval));
				
		String secondRemovalFromParentId = expectedParentFolderIds[1];
				
		try {
			//scenario2: runnig the testing scenario, and the expected should be an CatchedCmisPermissionDeniedException because there should be at least one parent
			cmisMultiFilingService.removeObjectFromFolder(repositoryId, expectedDocumentId, secondRemovalFromParentId, mockExtension);
			fail("you can not remove from all folders. it has be there at least one parent");
		} catch(CmisPermissionDeniedException permissionDeniedException) {
			//todo maybe should be checked the exact message from exception
		}
				
	}
	
	/**
	 * input 	: folderId & his parents { folderIds[0] } 
	 * scenario : running cmisMultiFilingService.removeObjectFromFolder(documentId, folderIds[0])
	 * expected : CmisInvalidArgumentException("can not remove a folder from another folder")
	 */
	@Test
	public void testRemoveObjectFromFolder_sourceAsFolder() {
		String[] expectedParentFolderIds = new String[] {"202d2d4d18f889c87276f72a0e5472e623089200"};
		//object is actually just a Folder,( but the CMIS restriction says that should be a document)		
		String expectedChildFolderId = "4a41d6a497d3d1db5ef4db1d10e424585817a952"; 				
		
		try {
		//runnig the testing scenario
			cmisMultiFilingService.removeObjectFromFolder(repositoryId, expectedChildFolderId, expectedParentFolderIds[0], mockExtension);
			fail("can not remove a folder from another folder");
		} catch (CmisInvalidArgumentException removingOneFolderFromAnotherException) {
			//todo maybe should be checked the exact message from exception
		}
	}	
	
	/**
	 * input 	: documentId & his parents { folderIds[0] } 
	 * 			  anohterNewParentFolder
	 * scenario : running cmisMultiFilingService.addObjectToFolder(documentId, anohterNewParentFolder)
	 * expected : documentId has as parents : {folderIds[0], anohterNewParentFolder}
	 */
	@Test
	public void testAddObjectToFolder() {
		String[] expectedParentFolderIds = new String[] {"33bd874459b7f014154e15eb263ef0b7c4204a18"};
		String expectedChildDocumentId = "7e34082628445aee97f42e5bd2d3445a56e54b83";				
		
		String anohterNewParentFolder = "e5e696d11555cab50c2740289b3112dd1370afd4";
		// running the testing scenario
		cmisMultiFilingService.addObjectToFolder(repositoryId, expectedChildDocumentId, anohterNewParentFolder, false, mockExtension);
		
		//verifying the parents relation ship before running the scenario 
		List<ObjectParentData> parentsAfterAnotherNewParent = cmisNavigationService.getObjectParents(repositoryId, expectedChildDocumentId, null, false, IncludeRelationships.NONE, "mockRenditionFilter", false, mockExtension);
		assertNotNull(parentsAfterAnotherNewParent);
		for(String expectedParentId : getNewExpectedParentIds(expectedParentFolderIds, anohterNewParentFolder)) {
			assertTrue(checkIfIsParentForChild(expectedParentId, parentsAfterAnotherNewParent));
		}
	}
	
	/**
	 * input 	: documentId & his parents { ...//not important if any or are one / many ... } 
	 * 			  oneNonExistentParent
	 * scenario : running cmisMultiFilingService.addObjectToFolder(documentId, oneNonExistentParent)
	 * expected : CmisObjectNotFoundException (can not add into a not found FOLDER)
	 */
	@Test
	public void testAddObjectFolder_destinationNonExistentFolder() {
		String expectedChildId = "7e34082628445aee97f42e5bd2d3445a56e54b83";			
		
		try {
		// running the testing scenario
			cmisMultiFilingService.addObjectToFolder(repositoryId, expectedChildId, "oneNonExistentParent", false, mockExtension);
			fail("object not found exception for oneNonExistentParent");
		} catch(CmisObjectNotFoundException cmisObjectNotFoundException) {
			//todo maybe should be checked the exact message from exception
		}
	}
	
	/**
	 * input 	: documentId & his parents { ...//not important if any or are one / many ... } 
	 * 			  destinationAsDocument
	 * scenario : running cmisMultiFilingService.addObjectToFolder(documentId, destinationAsDocument)
	 * expected : CmisInvalidArgumentException(can not add one document to anohter document)
	 */
	@Test
	public void testAddObjectFolder_destinationAsDocument() {
		String destinationAsDocument = "4afef5226c0d2bcb7f7d7ac1065df8544f27b75a";
		String expectedChildDocumentId = "7e34082628445aee97f42e5bd2d3445a56e54b83";
	
		try {
		// running the testing scenario
			cmisMultiFilingService.addObjectToFolder(repositoryId, expectedChildDocumentId, destinationAsDocument, false, mockExtension);
		} catch(CmisInvalidArgumentException cmisInvalidArgumentException) {
			//todo maybe should be checked the exact message from exception
		}
	}
	
	private String[] getNewExpectedParentIds(String[] obsoletParentIds, String anohterNewParent) {
		String[] latestParentIds = new String[obsoletParentIds.length + 1];
		for (int i=0; i< obsoletParentIds.length ; i++) {
			latestParentIds[i] = obsoletParentIds[i];
		}
		latestParentIds[obsoletParentIds.length] = anohterNewParent;
		return latestParentIds;
		
	}
		
	private boolean checkIfIsParentForChild(String expectedParentId, List<ObjectParentData> latestParents) {
		boolean isParentForChild = false;
		for(ObjectParentData objectParentData : latestParents) {
			 if(expectedParentId.equals(objectParentData.getObject().getId())) {
				 isParentForChild = true;
				 break;
			 }			 
		}
		return isParentForChild;
	}			
	
}
