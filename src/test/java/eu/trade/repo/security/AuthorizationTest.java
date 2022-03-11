package eu.trade.repo.security;

import static eu.trade.repo.TestConstants.*;
import static eu.trade.repo.util.Constants.*;
import static org.junit.Assert.*;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;

public class AuthorizationTest extends BaseTestClass {

	private static final String ROOT_FOLDER_1 = "1.ROOT";

	@Test
	public void testCreateDocumentAuthorized() throws Exception {
		// default test user
		doTestCreateDocument();
	}

	@Test(expected=CmisPermissionDeniedException.class)
	public void testCreateDocumentNonAuthorized() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestCreateDocument();
	}

	public void testCreateDocumentNonAuthorizedNoneAcl() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestCreateDocument("baseRepo1.xml", TEST_REPO_1);
	}

	@Test
	public void testCreateDocumentAdminAuthorized() throws Exception {
		setUser(ADMIN_USER, ADMIN_PWD, TEST_REPO_1);
		doTestCreateDocument();
	}

	@Test
	public void testCreateDocumentWritePermissionAuthorizedWithAllPermission() throws Exception {
		setUser(TEST_USER, TEST_PWD, TEST_REPO_2);
		doTestCreateDocument("baseRepo2.xml", TEST_REPO_2);
	}

	@Test(expected=CmisPermissionDeniedException.class)
	public void testCreateDocumentWritePermissionNonAuthorizedWithReadPermission() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestCreateDocument();
	}

	public void testCreateDocumentWritePermissionNonAuthorizedWithReadPermissionNoneAcl() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestCreateDocument("baseRepo1.xml", TEST_REPO_1);
	}

	@Test
	public void testCreateFolderAuthorized() throws Exception {
		// default test user
		doTestCreateFolder();
	}

	@Test(expected=CmisPermissionDeniedException.class)
	public void testCreateFolderNonAuthorized() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestCreateFolder();
	}

	public void testCreateFolderNonAuthorizedNoneAcl() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestCreateFolder("baseRepo1.xml");
	}

	@Test
	public void testCreateFolderAdminAuthorized() throws Exception {
		setUser(ADMIN_USER, ADMIN_PWD, TEST_REPO_1);
		doTestCreateFolder();
	}

	@Test
	public void testGetAllowableActionsAuthorized() throws Exception {
		// default test user
		doTestGetAllowableActions();
	}

	@Test(expected=CmisPermissionDeniedException.class)
	public void testGetAllowableActionsNonAuthorized() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestGetAllowableActions();
	}

	public void testGetAllowableActionsNonAuthorizedNoneAcl() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestGetAllowableActions("baseRepo1.xml");
	}

	@Test
	public void testGetAllowableActionsAdminAuthorized() throws Exception {
		setUser(ADMIN_USER, ADMIN_PWD, TEST_REPO_1);
		doTestGetAllowableActions();
	}

	@Test
	public void testGetObjectAuthorized() throws Exception {
		// default test user
		doTestGetObject();
	}

	@Test(expected=CmisPermissionDeniedException.class)
	public void testGetObjectNonAuthorized() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestGetObject();
	}

	public void testGetObjectNonAuthorizedNoneAcl() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestGetObject("baseRepo1.xml");
	}

	@Test
	public void testGetObjectAdminAuthorized() throws Exception {
		setUser(ADMIN_USER, ADMIN_PWD, TEST_REPO_1);
		doTestGetObject();
	}

	@Test
	public void testGetContentStreamAuthorized() throws Exception {
		// default test user
		doTestGetContentStream();
	}

	@Test(expected=CmisPermissionDeniedException.class)
	public void testGetContentStreamNonAuthorized() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestGetContentStream();
	}

	public void testGetContentStreamNonAuthorizedNoneAcl() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestGetContentStream("baseRepo1.xml");
	}

	@Test
	public void testGetContentStreamAdminAuthorized() throws Exception {
		setUser(ADMIN_USER, ADMIN_PWD, TEST_REPO_1);
		doTestGetContentStream();
	}

	@Test
	public void testUpdatePropertiesAuthorized() throws Exception {
		// default test user
		doTestUpdateProperties();
	}

	@Test(expected=CmisPermissionDeniedException.class)
	public void testUpdatePropertiesNonAuthorized() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestUpdateProperties();
	}

	public void testUpdatePropertiesNonAuthorizedNoneAcl() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestUpdateProperties("baseRepo1.xml");
	}

	@Test
	public void testUpdatePropertiesAdminAuthorized() throws Exception {
		setUser(ADMIN_USER, ADMIN_PWD, TEST_REPO_1);
		doTestUpdateProperties();
	}

	@Test
	public void testMoveObjectAuthorized() throws Exception {
		// default test user
		doTestMoveObject();
	}

	@Test(expected=CmisPermissionDeniedException.class)
	public void testMoveObjectNonAuthorized() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestMoveObject();
	}

	public void testMoveObjectNonAuthorizedNoneAcl() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestMoveObject("baseRepo1.xml");
	}

	@Test(expected=CmisPermissionDeniedException.class)
	public void testMoveObjectTargetNonAuthorized() throws Exception {
		// default test user
		doTestMoveObject("baseRepo1_discover.xml", ROOT_FOLDER_1, TESTFOLDER_CMISID);
	}

	public void testMoveObjectTargetNonAuthorizedNoneAcl() throws Exception {
		// default test user
		doTestMoveObject("baseRepo1.xml", ROOT_FOLDER_1, TESTFOLDER_CMISID);
	}

	@Test
	public void testMoveObjectAdminAuthorized() throws Exception {
		setUser(ADMIN_USER, ADMIN_PWD, TEST_REPO_1);
		doTestMoveObject();
	}

	@Test (expected=CmisInvalidArgumentException.class)
	public void negativeScenarios_cannotMove_sourceNotaFolder() throws Exception {
		setUser(ADMIN_USER, ADMIN_PWD, TEST_REPO_1);
		setObjectsScenario();
		try {
			cmisObjectService.moveObject(TEST_REPO_2, new Holder<String>(TESTDOC_CMISID), TESTFOLDER_CMISID, TESTDOC_CMISID, null);
		} catch (Exception e) {
			assertTrue(e.getMessage().startsWith(SecurityImpl.TYPE_ERROR_PREFIX));
			throw e;
		}
	}

	@Test (expected=CmisInvalidArgumentException.class)
	public void negativeScenarios_cannotMove_targetNotaFolder() throws Exception{
		setUser(ADMIN_USER, ADMIN_PWD, TEST_REPO_1);
		setObjectsScenario();
		try {
			cmisObjectService.moveObject(TEST_REPO_2, new Holder<String>(TESTDOC_CMISID), TESTDOC_CMISID, TESTFOLDER_CMISID, null);
		} catch (Exception e) {
			assertTrue(e.getMessage().startsWith(SecurityImpl.TYPE_ERROR_PREFIX));
			throw e;
		}
	}

	@Test
	public void testDeleteObjectAuthorized() throws Exception {
		// default test user
		doTestDeleteObject();
	}

	@Test(expected=CmisPermissionDeniedException.class)
	public void testDeleteObjectNonAuthorized() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestDeleteObject();
	}

	public void testDeleteObjectNonAuthorizedNoneAcl() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestDeleteObject("baseRepo1.xml");
	}

	@Test
	public void testDeleteObjectAdminAuthorized() throws Exception {
		setUser(ADMIN_USER, ADMIN_PWD, TEST_REPO_1);
		doTestDeleteObject();
	}

	@Test
	public void testDeleteTreeAuthorized() throws Exception {
		// default test user
		doTestDeleteTree();
	}

	@Test(expected=CmisPermissionDeniedException.class)
	public void testDeleteTreeNonAuthorized() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestDeleteTree();
	}

	public void testDeleteTreeNonAuthorizedNoneAcl() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestDeleteTree("baseRepo1.xml");
	}

	@Test
	public void testDeleteTreeAdminAuthorized() throws Exception {
		setUser(ADMIN_USER, ADMIN_PWD, TEST_REPO_1);
		doTestDeleteTree();
	}

	@Test
	public void testSetContentStreamAuthorized() throws Exception {
		// default test user
		doTestSetContentStream();
	}

	@Test(expected=CmisPermissionDeniedException.class)
	public void testSetContentStreamNonAuthorized() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestSetContentStream();
	}

	public void testSetContentStreamNonAuthorizedNoneAcl() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestSetContentStream("baseRepo1.xml");
	}

	@Test
	public void testSetContentStreamAdminAuthorized() throws Exception {
		setUser(ADMIN_USER, ADMIN_PWD, TEST_REPO_1);
		doTestSetContentStream();
	}

	@Test
	public void testDeleteContentStreamAuthorized() throws Exception {
		// default test user
		doTestDeleteContentStream();
	}

	@Test(expected=CmisPermissionDeniedException.class)
	public void testDeleteContentStreamNonAuthorized() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestDeleteContentStream();
	}

	public void testDeleteContentStreamNonAuthorizedNoneAcl() throws Exception {
		setUser(TEST2_USER, TEST2_PWD, TEST_REPO_1);
		doTestDeleteContentStream("baseRepo1.xml");
	}

	@Test
	public void testDeleteContentStreamAdminAuthorized() throws Exception {
		setUser(ADMIN_USER, ADMIN_PWD, TEST_REPO_1);
		doTestDeleteContentStream();
	}

	private void doTestCreateDocument() throws Exception {
		doTestCreateDocument("baseRepo1_discover.xml", TEST_REPO_1);
	}

	private void doTestCreateDocument(String scenario, String repositoryId) throws Exception {
		setScenario(scenario, DatabaseOperation.CLEAN_INSERT);
		try {
			cmisObjectService.createDocument(repositoryId, null, ROOT_FOLDER_1, null, null, null, null, null, null);
		} catch (Exception e) {
			if (e instanceof CmisPermissionDeniedException) {
				throw e;
			}
			// ignore all other kind of exceptions for this test.
		}
	}

	private void doTestCreateFolder() throws Exception {
		doTestCreateFolder("baseRepo1_discover.xml");
	}

	private void doTestCreateFolder(String scenario) throws Exception {
		setScenario(scenario, DatabaseOperation.CLEAN_INSERT);
		try {
			cmisObjectService.createFolder(TEST_REPO_1, null, ROOT_FOLDER_1, null, null, null, null);
		} catch (Exception e) {
			if (e instanceof CmisPermissionDeniedException) {
				throw e;
			}
			// ignore all other kind of exceptions for this test.
		}

	}

	private void doTestGetAllowableActions() throws Exception {
		doTestGetAllowableActions("baseRepo1_discover.xml");
	}

	private void doTestGetAllowableActions(String scenario) throws Exception {
		setScenario(scenario, DatabaseOperation.CLEAN_INSERT);
		try {
			cmisObjectService.getAllowableActions(TEST_REPO_1, ROOT_FOLDER_1, null);
		} catch (Exception e) {
			if (e instanceof CmisPermissionDeniedException) {
				throw e;
			}
			// ignore all other kind of exceptions for this test.
		}
	}

	private void doTestGetObject() throws Exception {
		doTestGetObject("baseRepo1_discover.xml");
	}

	private void doTestGetObject(String scenario) throws Exception {
		setScenario(scenario, DatabaseOperation.CLEAN_INSERT);
		try {
			cmisObjectService.getObject(TEST_REPO_1, ROOT_FOLDER_1, null, null, null, null, null, null, null);
		} catch (Exception e) {
			if (e instanceof CmisPermissionDeniedException) {
				throw e;
			}
			// ignore all other kind of exceptions for this test.
		}
		try {
			cmisObjectService.getObjectByPath(TEST_REPO_1, CMIS_PATH_SEP, null, null, null, null, null, null, null);
		} catch (Exception e) {
			if (e instanceof CmisPermissionDeniedException) {
				throw e;
			}
			// ignore all other kind of exceptions for this test.
		}
		try {
			cmisObjectService.getProperties(TEST_REPO_1, ROOT_FOLDER_1, null, null);
		} catch (Exception e) {
			if (e instanceof CmisPermissionDeniedException) {
				throw e;
			}
			// ignore all other kind of exceptions for this test.
		}

	}

	private void doTestGetContentStream() throws Exception {
		doTestGetContentStream("baseRepo1_discover.xml");
	}

	private void doTestGetContentStream(String scenario) throws Exception {
		setObjectsScenario(scenario);
		try {
			cmisObjectService.getContentStream(TEST_REPO_1, TESTDOC_CMISID, null, null, null, null);
		} catch (Exception e) {
			if (e instanceof CmisPermissionDeniedException) {
				throw e;
			}
			// ignore all other kind of exceptions for this test.
		}
	}

	private void doTestUpdateProperties() throws Exception {
		doTestUpdateProperties("baseRepo1_discover.xml");
	}

	private void doTestUpdateProperties(String scenario) throws Exception {
		setObjectsScenario(scenario);
		try {
			cmisObjectService.updateProperties(TEST_REPO_1, new Holder<>(TESTDOC_CMISID), null, null, null);
		} catch (Exception e) {
			if (e instanceof CmisPermissionDeniedException) {
				throw e;
			}
			// ignore all other kind of exceptions for this test.
		}
	}

	private void doTestMoveObject() throws Exception {
		doTestMoveObject("baseRepo1_discover.xml");
	}

	private void doTestMoveObject(String scenario) throws Exception {
		doTestMoveObject(scenario, TESTFOLDER_CMISID, ROOT_FOLDER_1);
	}

	private void doTestMoveObject(String scenario, String sourceId, String targetId) throws Exception {
		setObjectsScenario(scenario);
		try {
			cmisObjectService.moveObject(TEST_REPO_1, new Holder<>(TESTDOC_CMISID), targetId, sourceId, null);
		} catch (Exception e) {
			if (e instanceof CmisPermissionDeniedException) {
				throw e;
			}
			// ignore all other kind of exceptions for this test.
		}
	}

	private void doTestDeleteObject() throws Exception {
		doTestDeleteObject("baseRepo1_discover.xml");
	}

	private void doTestDeleteObject(String scenario) throws Exception {
		setObjectsScenario(scenario);
		try {
			cmisObjectService.deleteObject(TEST_REPO_1, TESTDOC_CMISID, null, null);
		} catch (Exception e) {
			if (e instanceof CmisPermissionDeniedException) {
				throw e;
			}
			// ignore all other kind of exceptions for this test.
		}
	}

	private void doTestDeleteTree() throws Exception {
		doTestDeleteTree("baseRepo1_discover.xml");
	}

	private void doTestDeleteTree(String scenario) throws Exception {
		setObjectsScenario(scenario);
		try {
			cmisObjectService.deleteTree(TEST_REPO_1, ROOT_FOLDER_1, null, null, null, null);
		} catch (Exception e) {
			if (e instanceof CmisPermissionDeniedException) {
				throw e;
			}
			// ignore all other kind of exceptions for this test.
		}
	}

	private void doTestSetContentStream() throws Exception {
		doTestSetContentStream("baseRepo1_discover.xml");
	}

	private void doTestSetContentStream(String scenario) throws Exception {
		setObjectsScenario(scenario);
		try {
			cmisObjectService.setContentStream(TEST_REPO_1, new Holder<String>(TESTDOC_CMISID), null, null, null, null);
		} catch (Exception e) {
			if (e instanceof CmisPermissionDeniedException) {
				throw e;
			}
			// ignore all other kind of exceptions for this test.
		}
	}

	private void doTestDeleteContentStream() throws Exception {
		doTestDeleteContentStream("baseRepo1_discover.xml");
	}

	private void doTestDeleteContentStream(String scenario) throws Exception {
		setObjectsScenario(scenario);
		try {
			cmisObjectService.deleteContentStream(TEST_REPO_1, new Holder<String>(TESTDOC_CMISID), null, null);
		} catch (Exception e) {
			if (e instanceof CmisPermissionDeniedException) {
				throw e;
			}
			// ignore all other kind of exceptions for this test.
		}
	}

	private void setObjectsScenario() throws Exception {
		setObjectsScenario("baseRepo1_discover.xml");
	}

	private void setObjectsScenario(String scenario) throws Exception {
		setScenario(scenario, DatabaseOperation.CLEAN_INSERT);
		setScenario("rootObjects_01.xml", DatabaseOperation.INSERT);
	}
}
