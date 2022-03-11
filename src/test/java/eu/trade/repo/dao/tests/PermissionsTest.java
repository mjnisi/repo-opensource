package eu.trade.repo.dao.tests;

import static junit.framework.Assert.*;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.model.Permission;
import eu.trade.repo.util.Constants;

public class PermissionsTest extends BaseTestClass {

	@Test
	public void testCreate() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		
		Permission perm = new Permission();
		perm.setDescription("test permission");
		perm.setName("test:permission1");
		perm.setRepository(repoSelector.getRepository(TestConstants.TEST_REPO_2));
		perm.setParent(permSelector.getPermission(Constants.CMIS_WRITE, "test_repo_02"));

		utilService.persist(perm);
		
		compareTable(
				"PERMISSION",
				"NAME = 'test:permission1'", 
				"permission-test.xml");
	}
	
	@Test
	@Transactional
	public void testRead() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		Permission perm = permSelector.getPermission("test:permission", "test_repo_02");
		assertEquals("test write permission", perm.getDescription());
		assertEquals("test:permission", perm.getName());
		assertEquals("Write", perm.getParent().getDescription());
		assertEquals("test_repo_02", perm.getRepository().getCmisId());
	}
	
	@Test
	@Transactional
	public void testUpdate() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		Permission perm = permSelector.getPermission("test:permission", "test_repo_02");
		perm.setDescription("...");
		perm.setParent(permSelector.getPermission("cmis:read", "test_repo_02"));

		utilService.persist(perm);
		
		perm = permSelector.getPermission("test:permission", "test_repo_02");
		assertEquals("...", perm.getDescription());
		assertEquals("test:permission", perm.getName());
		assertEquals("Read", perm.getParent().getDescription());
		assertEquals("test_repo_02", perm.getRepository().getCmisId());
	}
	
	@Test
	public void testDelete () throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		assertEquals(4, permSelector.getAllPermissions(TestConstants.TEST_REPO_2).size());
		utilService.removeDetached(permSelector.getPermission("cmis:read", "test_repo_02"));
		assertEquals(3, permSelector.getAllPermissions(TestConstants.TEST_REPO_2).size());
	}

	
    @Test(expected=DataIntegrityViolationException.class)
	public void negativeScenarios_uniqueness() throws Exception {
    	setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
    	
		Permission perm = new Permission();
		perm.setDescription("test permission");
		perm.setName("cmis:read");  // ALREADY EXISTS
		perm.setRepository(repoSelector.getRepository(TestConstants.TEST_REPO_2));
		utilService.persist(perm);
	}
    
    public void negativeScenarios_withNoParent()throws Exception {
    	setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
    	
    	Permission perm = permSelector.getPermission("test:permission", "test_repo_02");
    	perm.getParent().getId();
    }
    
    public void negativeScenarios_withNoRepo()throws Exception {
    	setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
    	
    	Permission perm = permSelector.getPermission("test:permission", "test_repo_02");
    	perm.getRepository().getId();
    }
}
