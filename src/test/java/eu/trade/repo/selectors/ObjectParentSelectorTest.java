package eu.trade.repo.selectors;

import static eu.trade.repo.TestConstants.*;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

import javax.persistence.Persistence;

import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.model.Repository;
import eu.trade.repo.model.view.ObjectParent;
import eu.trade.repo.security.Security;

public class ObjectParentSelectorTest extends BaseTestClass {

	@Autowired
	private ObjectParentSelector objectParentSelector;

	@Autowired
	private Security security;

	private static final String TEST_REPO_ID = "nest_dev";
	private static final String PATH_TEST_1 = "/";
	private static final int EXPECTED_TEST_1 = 651;
	private static final String PATH_TEST_2 = "/jmeter_3_4";
	private static final int EXPECTED_TEST_2 = 124;

	public void setNoAclScenario() {
		try {
			setScenario(DatabaseOperation.CLEAN_INSERT, "scenario05.xml");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public void setAclScenario() {
		try {
			setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioAclManageObjectonly.xml", "rootObjects_02.xml");
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Test
	public void testObjectParentEmpty_1() {
		setNoAclScenario();
		Set<ObjectParent> objectParents = objectParentSelector.getObjectParents("wronf_rep", false, "/", null, true);
		assertNotNull(objectParents);
		assertTrue(objectParents.isEmpty());
	}

	@Test
	public void testObjectParentEmpty_2() {
		setNoAclScenario();
		Set<ObjectParent> objectParents = objectParentSelector.getObjectParents(TEST_REPO_ID, false, "/fake_path", null, true);
		assertNotNull(objectParents);
		assertTrue(objectParents.isEmpty());
	}

	@Test
	public void testObjectParentNoDepthNoAcl_1() {
		testObjectParentNoDepthNoAcl(PATH_TEST_1, EXPECTED_TEST_1);
	}

	@Test
	public void testObjectParentNoDepthNoAcl_2() {
		testObjectParentNoDepthNoAcl(PATH_TEST_2, EXPECTED_TEST_2);
	}

	@Test
	public void testObjectParentDepthNoAcl_1() {
		setNoAclScenario();
		Set<ObjectParent> objectParents = objectParentSelector.getObjectParents(TEST_REPO_ID, false, PATH_TEST_1, BigInteger.ONE, true);
		assertNotNull(objectParents);
		assertEquals(24, objectParents.size());
	}

	@Test
	public void testObjectParentDepthNoAcl_2() {
		setNoAclScenario();
		Set<ObjectParent> objectParents = objectParentSelector.getObjectParents(TEST_REPO_ID, false, PATH_TEST_1, BigInteger.valueOf(100l), true);
		Set<ObjectParent> objectParents2 = objectParentSelector.getObjectParents(TEST_REPO_ID, false, PATH_TEST_1, null, true);
		assertNotNull(objectParents);
		assertNotNull(objectParents2);
		checkEquals(objectParents, objectParents2);
	}

	@Test
	public void testObjectParentDepthNoAcl_3() {
		setNoAclScenario();
		Set<ObjectParent> objectParents = objectParentSelector.getObjectParents(TEST_REPO_ID, false, PATH_TEST_2, BigInteger.valueOf(100l), true);
		Set<ObjectParent> objectParents2 = objectParentSelector.getObjectParents(TEST_REPO_ID, false, PATH_TEST_2, null, true);
		assertNotNull(objectParents);
		assertNotNull(objectParents2);
		checkEquals(objectParents, objectParents2);
	}

	@Test
	public void testObjectParentDepthNoAcl_4() {
		setNoAclScenario();
		Set<ObjectParent> objectParents = objectParentSelector.getObjectParents(TEST_REPO_ID, false, PATH_TEST_2, BigInteger.ONE, true);
		assertNotNull(objectParents);
		assertEquals(26, objectParents.size());
	}

	@Test
	public void testObjectParentAclNoDepth_1() {
		setAclScenario();
		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TEST_REPO_1);
		Set<ObjectParent> objectParents = objectParentSelector.getObjectParents(TEST_REPO_1, PATH_TEST_1, false, null, Collections.singleton("/test"), security.getPermissionIds(TEST_REPO_1, Action.CAN_GET_PROPERTIES), null, true);
		assertNotNull(objectParents);
		assertEquals(12, objectParents.size());
	}

	@Test
	public void testObjectParentDepthAcl_1() {
		setAclScenario();
		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TEST_REPO_1);
		Set<ObjectParent> objectParents = objectParentSelector.getObjectParents(TEST_REPO_1, PATH_TEST_1, false, BigInteger.ONE, Collections.singleton("/test"), security.getPermissionIds(TEST_REPO_1, Action.CAN_GET_PROPERTIES), null, true);
		assertNotNull(objectParents);
		assertEquals(9, objectParents.size());
	}
	
	@Test
	public void testSelectiveLoadParentsAndChildrenOrCounts() throws Exception {
		
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		
		Repository repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setGetDescendants(Boolean.TRUE);
		repository.setGetFolderTree(Boolean.TRUE);
		repositoryService.update(repository);

		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TestConstants.TEST_REPO_2);
		Holder<String> obejctId =  new Holder<String>("Test Document");
		Holder<Boolean> contentCopied =  new Holder<Boolean>();
		versioningService.checkOut(TestConstants.TEST_REPO_2, obejctId, contentCopied);
		
		Set<ObjectParent> objectParents = objectParentSelector.getObjectParents(TEST_REPO_2, false, "/", null, true);
		
		for(ObjectParent op : objectParents) {
			assertTrue(Persistence.getPersistenceUtil().isLoaded(op.getCmisObject(), "children"));
			assertTrue(Persistence.getPersistenceUtil().isLoaded(op.getCmisObject(), "parents"));
			assertEquals(op.getCmisObject().getChildren().size(), op.getCmisObject().getChildrenSize());
			assertEquals(op.getCmisObject().getParents().size(), op.getCmisObject().getParentsSize());
		}
		
		objectParents = objectParentSelector.getObjectParents(TEST_REPO_2, false, "/", null, false);
		
		for(ObjectParent op : objectParents) {
			assertFalse(Persistence.getPersistenceUtil().isLoaded(op.getCmisObject(), "children"));
			assertFalse(Persistence.getPersistenceUtil().isLoaded(op.getCmisObject(), "parents"));
		}
	}

	private void testObjectParentNoDepthNoAcl(String path, int expected) {
		setNoAclScenario();
		Set<ObjectParent> objectParents = objectParentSelector.getObjectParents(TEST_REPO_ID, false, path, null, true);
		assertNotNull(objectParents);
		assertEquals(expected, objectParents.size());
	}

	private void checkEquals(Set<ObjectParent> rows1, Set<ObjectParent> rows2) {
		assertEquals(rows1.size(), rows2.size());
		rows1.containsAll(rows2);
	}
}
