package eu.trade.repo.dao.tests;

import static eu.trade.repo.TestConstants.*;
import static junit.framework.Assert.*;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.Permission;
import eu.trade.repo.util.Constants;

public class AclTest extends BaseTestClass {
	@Test
	public void testCreate() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		Acl acl = new Acl();
		acl.setPermission(permSelector.getPermission(Constants.CMIS_READ, "test_repo_02"));
		acl.setObject(cmisObjectSelector.getCMISObject(TEST_REPO_2, TESTFOLDER_CMISID));
		acl.setPrincipalId("test pid");
		utilService.persist(acl);

		compareTable(
				"acl",
				"PRINCIPAL_ID = 'test pid'",
				"acl-test.xml");
	}

	@Test
	public void testRead() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		Acl acl = aclSelector.getAclsByPID("Test PID1").get(0); // PID not unique
		assertEquals(100, acl.getId().intValue());
		assertEquals("Test PID1", acl.getPrincipalId());
		//assertEquals("test:document", acl.getObject().getObjectType().getQueryName());
		//assertEquals(Constants.CMIS_READ, acl.getPermission().getName());

	}

	@Test
	public void testUpdate() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		Acl acl = aclSelector.getAclsByPID("Test PID1").get(0);

		assertEquals("Test PID1", acl.getPrincipalId());
		//		assertEquals("Test Document", acl.getObject().getCmisObjectId());
		//		assertEquals(Constants.CMIS_READ, acl.getPermission().getName());

		CMISObject object = cmisObjectSelector.getCMISObject(TEST_REPO_2, TESTFOLDER_CMISID);
		Permission perm = permSelector.getPermission(Constants.CMIS_WRITE, "test_repo_02");
		acl.setObject(object);
		acl.setPermission(perm);


		utilService.merge(acl);
		aclSelector.getAclsByPID("Test PID1").get(0);
		assertEquals("Test PID1", acl.getPrincipalId());
		//		assertEquals("test:folder", acl.getObject().getObjectType().getQueryName());
		//		assertEquals(Constants.CMIS_WRITE, acl.getPermission().getName());
	}

	@Test
	public void testDelete () throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		assertEquals(5, aclSelector.getAcls().size());
		utilService.removeDetached(aclSelector.getAclsByPID("Test PID1").get(0));

		assertEquals(4, aclSelector.getAcls().size());
	}
}
