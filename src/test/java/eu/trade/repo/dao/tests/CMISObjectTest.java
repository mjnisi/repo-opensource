package eu.trade.repo.dao.tests;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.Set;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;

public class CMISObjectTest extends BaseTestClass {
	@Test
	public void testCreate() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		ObjectType ot = objTypeSelector.getObjectTypeByCmisId("test_repo_02", "test:document");
		assertNotNull(ot);

		CMISObject cmisObject = new CMISObject();
		cmisObject.setCmisObjectId("Test Doc");
		cmisObject.setObjectType(ot);

		utilService.persist(cmisObject);

		compareTable(
				"Object",
				"CMIS_OBJECT_ID = 'Test Doc'",
				"cmisobject-test.xml");
	}

	@Test
	public void testRead() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		CMISObject object = cmisObjectSelector.getCMISObject(TestConstants.TEST_REPO_2, TestConstants.TESTSUBFOLDER_CMISID);
		assertEquals(TestConstants.TESTSUBFOLDER_CMISID, object.getCmisObjectId());
		assertEquals(101, object.getId().intValue());
		//assertEquals("test:subfolder", object.getObjectType().getCmisId());

		//ACLS
		Set<Acl> acls = object.getAcls();
		assertEquals(2, acls.size());
		for (Acl acl : acls) {
			if (acl.getId() == 103) {
				assertEquals("Test PID4", acl.getPrincipalId());
			} else {
				assertEquals("Test PID5", acl.getPrincipalId());
			}
		}
	}

	@Test
	public void testUpdate() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		//ACL
		Acl acl = aclSelector.getAclsByPID("Test PID1").get(0);
		CMISObject object = cmisObjectSelector.getCMISObject(TestConstants.TEST_REPO_2, TestConstants.TESTSUBFOLDER_CMISID);
		assertEquals(2, object.getAcls().size());
		//ADD ACL
		object.addAcl(acl);
		utilService.merge(object);
		utilService.merge(acl);
		object = cmisObjectSelector.getCMISObject(TestConstants.TEST_REPO_2, TestConstants.TESTSUBFOLDER_CMISID);
		assertEquals(3, object.getAcls().size());
		//REMOVE ACL
		object.removeAcl(acl);
		utilService.merge(object);
		object = cmisObjectSelector.getCMISObject(TestConstants.TEST_REPO_2, TestConstants.TESTSUBFOLDER_CMISID);
		assertEquals(2, object.getAcls().size());

	}

	@Test
	public void testDelete () throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		utilService.removeDetached(cmisObjectSelector.getCMISObject(TestConstants.TEST_REPO_2, TestConstants.TESTSUBFOLDER_CMISID));
	}

	@Test
	public void addChild01() throws Exception {

		setScenario("scenario02.xml", DatabaseOperation.CLEAN_INSERT);

		CMISObject child = cmisObjectSelector.loadCMISObject("Test Folder");
		CMISObject o = cmisObjectSelector.loadCMISObject("Test Document");
		
		o.addChild(child);

		utilService.merge(o);
		compareTable("object_child", "object_id = 100", "cmisobject-addchild01.xml");
	}


	@Test
	public void deleteCascade01() throws Exception {

		setScenario("scenario02.xml", DatabaseOperation.CLEAN_INSERT);

		utilService.removeDetached(utilService.find(CMISObject.class, 106));

		compareTable("object", "cmisobject-deletecascade01.xml");
	}
	
	@Autowired
	private AbstractPlatformTransactionManager transactionManager;
	
	@Test
	public void addSecondaryType() throws Exception {
		setScenario("scenarioSecondaryTypes.xml", DatabaseOperation.CLEAN_INSERT);
		
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		CMISObject rootFolder = utilService.find(CMISObject.class, 1); 
		ObjectType secondary = repositoryService.getObjectType("scenarioSecondaryTypes", "cmis:secondary");
		rootFolder.addSecondaryType(secondary);
		transactionManager.commit(txStatus);
	
		compareTable("object_secondary_type", "cmisobject-secondary01.xml");
	}
	

	@Test
	public void deleteSecondaryType() throws Exception {
		setScenario("scenarioSecondaryTypes.xml", DatabaseOperation.CLEAN_INSERT);
		setScenario("scenarioSecondaryTypes-withData.xml", DatabaseOperation.INSERT);
		
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		CMISObject rootFolder = utilService.find(CMISObject.class, 1); 
		ObjectType secondary = repositoryService.getObjectType("scenarioSecondaryTypes", "cmis:secondary");
		rootFolder.removeSecondaryType(secondary);
		transactionManager.commit(txStatus);
	
		compareTable("object_secondary_type", "cmisobject-secondary02.xml");
	}

	
}
