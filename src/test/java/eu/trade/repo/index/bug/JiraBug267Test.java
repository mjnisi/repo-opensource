package eu.trade.repo.index.bug;

import static eu.trade.repo.TestConstants.TEST_REPO_2;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.commons.configuration.Configuration;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.index.triggers.ChangeTrackerMap;
import eu.trade.repo.index.triggers.IndexChangeData;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.service.cmis.data.out.PropertiesBuilder;
import eu.trade.repo.util.Constants;

public class JiraBug267Test extends BaseTestClass {
		
	@Autowired 
	private StubIndexTriggersDelegate stubIndexTriggersDelegate;
	
	@Autowired @Qualifier("indexConfig")
	private Configuration config;		
	
	@Before
	public void setup() throws Exception {
			
		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TEST_REPO_2);		
	}
	
	@After
	public void reset() {
		config.setProperty(Constants.PROPNAME_INDEX_ATOMIC_ENABLED, false);	
		stubIndexTriggersDelegate.setRaiseException(false);
	}
	
	/**
	 * input 		: load the database structure
	 * 				  load some records (no impact to the actual test) based on the scenario file : scenarioJiraBug.TDR-267.xml 
	 * scenario1 	: create folderA, delete it 
	 * 				  environment is the real one
	 * expected1 	: indexTriggersDelegate.getChangeTrackerMap() should return an empty map
	 * scenario2 	: create folderB, delete it
	 * 				  environment is the real one, but IndexTriggersDelegate.getRepositoryByObjectMap method is forced to throw an Exception
	 * 					
	 * expected2 	: indexTriggersDelegate.getChangeTrackerMap() should return an empty map
	 * @throws Throwable
	 */
	@Test	
	@Ignore("this test is not working, to see further why")
	public void testCmisServiceLayer() throws Throwable {
		
		setScenario("scenarioJiraBug.TDR-267.xml", DatabaseOperation.CLEAN_INSERT);
		config.setProperty(Constants.PROPNAME_INDEX_ATOMIC_ENABLED, true);	
								
		//run first scenario : create folder, delete it, get the ID 
		Integer deletedFolderAId = doDeleteOperationAndGetDeletedFolderId( "FolderA", false);
		//assert that IndexTriggersDelegate.getCMISObjectsRemoved is EMPTY
		assertEmptyTrackerMap();
				
		//run second scenario : create folder, delete it, get the ID
		Integer deletedFolderBId = doDeleteOperationAndGetDeletedFolderId( "FolderB", true);
		//assert that IndexTriggersDelegate.getCMISObjectsRemoved is EMPTY
		assertEmptyTrackerMap();		
	}
	
	/**
	 * executes a sequences of operations : create folder & delete it 
	 * @param folderName
	 * @param expectingException
	 * @return the Id of the folder
	 */
	private Integer doDeleteOperationAndGetDeletedFolderId(String folderName, boolean expectingException) {
				
		String folderCmisId = createFolder(folderName);
		Integer folderId = getFolderId(folderCmisId);
		try {
			deleteFolder(folderCmisId, expectingException); 
		} catch (Exception actualException) {
			
			if(expectingException) {
				assertEquals ( StubIndexTriggersDelegate.MESSAGE_EXCEPTION , actualException.getMessage() );
			}
			 
		}
		return folderId;
	}
	
	/**
	 * creates a folder in the root folder
	 * @param folderName
	 * @return the Id of the folder
	 */
	private String createFolder(String folderName) {
		CMISObject folder = new CMISObject(new ObjectType("cmis:folder"));
		folder.addProperty(getTestProperty(folderName, TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		folder.addProperty(getTestProperty("cmis:folder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));
		return cmisObjectService.createFolder(TEST_REPO_2, PropertiesBuilder.build(folder), "/", null, createAcl(true, Arrays.asList(new String [] {TestConstants.TEST_USER}), "cmis:read"), null, null);
	}
	
	/**
	 * retrieve the Id folder
	 * @param folderCmisId
	 * @return the Id of the folder
	 */
	private Integer getFolderId(String folderCmisId){
		CMISObject object = cmisObjectSelector.getCMISObject(TEST_REPO_2, folderCmisId);		
		return object.getId();				
	}
	
	/**
	 * apply behaviour on IndexTriggersDelegate class based on a client expectingException parameter				
	 * delete folder by cmisId, 
	 * @param folderCmisId
	 * @param expectingException
	 */
	private void deleteFolder(String folderCmisId, boolean expectingException) {
		//setting up behavior before actual folder deletion 
		if (expectingException) {
			//forcing index layer to throw an Exception 
			stubIndexTriggersDelegate.setRaiseException(true);
		} else {
			//letting the index layer executing the actual code 
			stubIndexTriggersDelegate.setRaiseException(false);
		}
		
		cmisObjectService.deleteObject(TEST_REPO_2, folderCmisId, true, null);
	}
				
	/**
	 * when an array of ids are received, it is asserted that those are between IndexTriggersDelegate.getChangeTrackerMap() values
	 * @param ids
	 */
	private void assertTrackerMap(Integer ... ids) {
		ChangeTrackerMap map = stubIndexTriggersDelegate.getChangeTrackerMap();
		if(ids != null && ids.length > 0) {
			assertFalse( null == map);
			assertTrue(ids.length == map.getCMISObjectsRemoved().size());
			for (Integer id : ids) {
				assertTrue(map.getCMISObjectsRemoved().contains(new IndexChangeData(id)));
			} 
		} else {
			
			assertTrue(map == null || map.getCMISObjectsRemoved() == null || map.getCMISObjectsRemoved().isEmpty());
		}		
	}
				
	/**
	 * when an array of ids are received, it is asserted that those are between IndexTriggersDelegate.getChangeTrackerMap() values
	 * @param ids
	 */
	private void assertNonEmptyTrackerMap(Integer ... ids) {
		assertTrackerMap(ids);
	}
	
	private void assertEmptyTrackerMap() {
		assertTrackerMap();
	}

}
