package eu.trade.repo.service.cmis;

import static eu.trade.repo.TestConstants.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.id.IDGenerator;
import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.Permission;
import eu.trade.repo.service.cmis.data.out.AclBuilder;
import eu.trade.repo.service.cmis.data.out.PropertiesBuilder;
import eu.trade.repo.util.Constants;

public class CmisObjectServiceDefaultAclTest extends BaseTestClass {

	private static final String ROOT_FOLDER_ID = "1.ROOT";
	private static final String[] TEST_PERMISSIONS_ADD_1 = {"cmis:read", "cmis:write", "cmis:all", "cmis:read", "cmis:write", "cmis:all"};
	private static final String[] TEST_PRINCIPAL_ADD_1 = {"/test", "/test2", "/test4", "/test", "/test3", "/admin"};
	private static final String[] TEST_PERMISSIONS_REMOVE_1 = {"cmis:read", "cmis:write", "cmis:all"};
	private static final String[] TEST_PRINCIPAL_REMOVE_1 = {"/test", "/test3", "/admin"};
	private static final String[] TEST_PERMISSIONS_ROOT = {"cmis:all"};
	private static final String[] TEST_PRINCIPAL_ROOT = {"/test"};

	private static final String TEST_DOC_A = "DocA";
	private static final String TEST_DOC_B = "DocB";
	private static final String TEST_DOC_AB = "DocAB";
	private static final String TEST_DOC_ABC = "DocABC";
	private static final String TEST_DOC_A2 = "DocA2";
	private static final String TEST_DOC_AB2 = "DocAB2";
	private static final String TEST_DOC_ABC2 = "DocABC2";
	private static final String TEST_DOC_B2 = "DocB2";
	private static final String TEST_DOC_B3 = "DocB3";
	private static final String TEST_DOC_A3 = "DocA3";
	private static final String TEST_DOC_A4 = "DocA4";

	private static final String TEST_FOLDER_A = "FolderA";
	private static final String TEST_FOLDER_B = "FolderB";
	private static final String TEST_FOLDER_C = "FolderC";
	private static final String TEST_FOLDER_AB = "FolderAB";

	private static final String[] TEST_PERMISSIONS_A = {"cmis:read"};
	private static final String[] TEST_PRINCIPAL_A = {"/testA"};
	private static final String[] TEST_PERMISSIONS_B = {"cmis:all"};
	private static final String[] TEST_PRINCIPAL_B = {"/test"};
	private static final String[] TEST_PERMISSIONS_C = {"cmis:read"};
	private static final String[] TEST_PRINCIPAL_C = {"/testC"};
	private static final String[] TEST_PERMISSIONS_AB = build(TEST_PERMISSIONS_A, TEST_PERMISSIONS_B);
	private static final String[] TEST_PRINCIPAL_AB = build(TEST_PRINCIPAL_A, TEST_PRINCIPAL_B);
	private static final String[] TEST_PERMISSIONS_AC = build(TEST_PERMISSIONS_A, TEST_PERMISSIONS_C);
	private static final String[] TEST_PRINCIPAL_AC = build(TEST_PRINCIPAL_A, TEST_PRINCIPAL_C);
	private static final String[] TEST_PERMISSIONS_BC = build(TEST_PERMISSIONS_B, TEST_PERMISSIONS_C);
	private static final String[] TEST_PRINCIPAL_BC = build(TEST_PRINCIPAL_B, TEST_PRINCIPAL_C);
	private static final String[] TEST_PERMISSIONS_ABC = build(TEST_PERMISSIONS_A, TEST_PERMISSIONS_B, TEST_PERMISSIONS_C);
	private static final String[] TEST_PRINCIPAL_ABC = build(TEST_PRINCIPAL_A, TEST_PRINCIPAL_B, TEST_PRINCIPAL_C);

	private static String[] build(String[]... arrays) {
		String[] result = new String[arrays.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = arrays[i][0];
		}
		return result;
	}

	@Autowired
	private IDGenerator mockGenerator;

	@Test
	public void testCreateDocumentAclNone_1() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclNone.xml", "cmis:document", ROOT_FOLDER_ID, null, null, null, null);
		assertNotNull(cmisId);
	}

	@Test
	public void testCreateDocumentAclNone_2() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclNone.xml", "nest:document", ROOT_FOLDER_ID, null, null, null, null);
		assertNotNull(cmisId);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclNone_3() throws Exception {
		doTestCreateDocumentAcl("scenarioAclNone.xml", "cmis:document", ROOT_FOLDER_ID, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclNone_4() throws Exception {
		doTestCreateDocumentAcl("scenarioAclNone.xml", "cmis:document", ROOT_FOLDER_ID, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, null, null);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclNone_5() throws Exception {
		doTestCreateDocumentAcl("scenarioAclNone.xml", "cmis:document", ROOT_FOLDER_ID, null, null, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclNone_6() throws Exception {
		doTestCreateDocumentAcl("scenarioAclNone.xml", "nest:document", ROOT_FOLDER_ID, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, null, null);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclNone_7() throws Exception {
		doTestCreateDocumentAcl("scenarioAclNone.xml", "nest:document", ROOT_FOLDER_ID, null, null, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test
	public void testCreateDocumentAclNone_unfiled_1() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclNone.xml", "cmis:document", null, null, null, null, null);
		assertNotNull(cmisId);
	}

	@Test
	public void testCreateDocumentAclNone_unfiled_2() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclNone.xml", "nest:document", null, null, null, null, null);
		assertNotNull(cmisId);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclNone_unfiled_3() throws Exception {
		doTestCreateDocumentAcl("scenarioAclNone.xml", "cmis:document", null, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclNone_unfiled_4() throws Exception {
		doTestCreateDocumentAcl("scenarioAclNone.xml", "cmis:document", null, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, null, null);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclNone_unfiled_5() throws Exception {
		doTestCreateDocumentAcl("scenarioAclNone.xml", "cmis:document", null, null, null, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclNone_unfiled_6() throws Exception {
		doTestCreateDocumentAcl("scenarioAclNone.xml", "nest:document", null, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, null, null);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclNone_unfiled_7() throws Exception {
		doTestCreateDocumentAcl("scenarioAclNone.xml", "nest:document", null, null, null, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test
	public void testCreateDocumentAclManageObjectonly_1() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclManageObjectonly.xml", "nest:document", ROOT_FOLDER_ID, null, null, null, null);
		assertNotNull(cmisId);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManageObjectonly_2() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManageObjectonly.xml", "nest:document", ROOT_FOLDER_ID, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManageObjectonly_3() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManageObjectonly.xml", "nest:document", ROOT_FOLDER_ID, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, null, null);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManageObjectonly_4() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManageObjectonly.xml", "nest:document", ROOT_FOLDER_ID, null, null, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManageObjectonly_5() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManageObjectonly.xml", "cmis:document", ROOT_FOLDER_ID, null, null, null, null);
	}

	@Test
	public void testCreateDocumentAclManageObjectonly_6() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclManageObjectonly.xml", "cmis:document", ROOT_FOLDER_ID, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
		checkAcl(cmisId, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test
	public void testCreateDocumentAclManageObjectonly_7() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclManageObjectonly.xml", "cmis:document", ROOT_FOLDER_ID, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, null, null);
		checkAcl(cmisId, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, null, null);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManageObjectonly_8() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManageObjectonly.xml", "cmis:document", ROOT_FOLDER_ID, null, null, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test
	public void testCreateDocumentAclManageObjectonly_unfiled_1() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclManageObjectonly.xml", "nest:document", null, null, null, null, null);
		assertNotNull(cmisId);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManageObjectonly_unfiled_2() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManageObjectonly.xml", "nest:document", null, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManageObjectonly_unfiled_3() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManageObjectonly.xml", "nest:document", null, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, null, null);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManageObjectonly_unfiled_4() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManageObjectonly.xml", "nest:document", null, null, null, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManageObjectonly_unfiled_5() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManageObjectonly.xml", "cmis:document", null, null, null, null, null);
	}

	@Test
	public void testCreateDocumentAclManageObjectonly_unfiled_6() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclManageObjectonly.xml", "cmis:document", null, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
		checkAcl(cmisId, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test
	public void testCreateDocumentAclManageObjectonly_unfiled_7() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclManageObjectonly.xml", "cmis:document", null, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, null, null);
		checkAcl(cmisId, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, null, null);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManageObjectonly_unfiled_8() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManageObjectonly.xml", "cmis:document", null, null, null, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test
	public void testCreateDocumentAclManagePropagate_1() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclManagePropagate.xml", "nest:document", ROOT_FOLDER_ID, null, null, null, null);
		assertNotNull(cmisId);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManagePropagate_2() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManagePropagate.xml", "nest:document", ROOT_FOLDER_ID, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManagePropagate_3() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManagePropagate.xml", "nest:document", ROOT_FOLDER_ID, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, null, null);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManagePropagate_4() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManagePropagate.xml", "nest:document", ROOT_FOLDER_ID, null, null, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test
	public void testCreateDocumentAclManagePropagate_5() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclManagePropagate.xml", "cmis:document", ROOT_FOLDER_ID, null, null, null, null);
		checkAcl(cmisId, TEST_PERMISSIONS_ROOT, TEST_PRINCIPAL_ROOT, null, null, false);
		checkEmptyAcl(cmisId, true);
	}

	@Test
	public void testCreateDocumentAclManagePropagate_6() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclManagePropagate.xml", "cmis:document", ROOT_FOLDER_ID, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
		checkAcl(cmisId, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ROOT, TEST_PRINCIPAL_ROOT, null, null, false); // inherited ACL
	}

	@Test
	public void testCreateDocumentAclManagePropagate_7() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclManagePropagate.xml", "cmis:document", ROOT_FOLDER_ID, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, null, null);
		checkAcl(cmisId, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, null, null, true); // direct ACL
		checkAcl(cmisId, TEST_PERMISSIONS_ROOT, TEST_PRINCIPAL_ROOT, null, null, false); // inherited ACL
	}

	@Test
	public void testCreateDocumentAclManagePropagate_8() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclManagePropagate.xml", "cmis:document", ROOT_FOLDER_ID, null, null, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
		checkAcl(cmisId, TEST_PERMISSIONS_ROOT, TEST_PRINCIPAL_ROOT, null, null, false);
		checkEmptyAcl(cmisId, true);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManagePropagate_9() throws Exception {
		setScenario("scenarioAclManagePropagate.xml", DatabaseOperation.CLEAN_INSERT);
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TEST_REPO_1);
		mockGenerator.reset();

		CMISObject doc1 = new CMISObject();

		ObjectType tp4 = new ObjectType();
		tp4.setCmisId("cmis:document");
		doc1.setObjectType(tp4);

		doc1.addProperty(getTestProperty("My Document", TEST_REPO_1, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc1.addProperty(getTestProperty("cmis:document", TEST_REPO_1, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));

		Properties props = PropertiesBuilder.build(doc1, "");
		org.apache.chemistry.opencmis.commons.data.Acl aclToAdd = buildAcl(doc1, null, null);
		org.apache.chemistry.opencmis.commons.data.Acl aclToRemove = buildAcl(doc1, TEST_PERMISSIONS_ROOT, TEST_PRINCIPAL_ROOT, false);
		cmisObjectService.createDocument(TEST_REPO_1, props, ROOT_FOLDER_ID, null, null, null, aclToAdd, aclToRemove, null);
	}

	@Test
	public void testCreateDocumentAclManagePropagate_unfiled_1() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclManagePropagate.xml", "nest:document", null, null, null, null, null);
		assertNotNull(cmisId);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManagePropagate_unfiled_2() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManagePropagate.xml", "nest:document", null, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManagePropagate_unfiled_3() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManagePropagate.xml", "nest:document", null, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, null, null);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManagePropagate_unfiled_4() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManagePropagate.xml", "nest:document", null, null, null, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManagePropagate_unfiled_5() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManagePropagate.xml", "cmis:document", null, null, null, null, null);
	}

	@Test
	public void testCreateDocumentAclManagePropagate_unfiled_6() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclManagePropagate.xml", "cmis:document", null, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
		checkAcl(cmisId, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test
	public void testCreateDocumentAclManagePropagate_unfiled_7() throws Exception {
		String cmisId = doTestCreateDocumentAcl("scenarioAclManagePropagate.xml", "cmis:document", null, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, null, null);
		checkAcl(cmisId, TEST_PERMISSIONS_ADD_1, TEST_PRINCIPAL_ADD_1, null, null);
	}

	@Test(expected=CmisConstraintException.class)
	public void testCreateDocumentAclManagePropagate_unfiled_8() throws Exception {
		doTestCreateDocumentAcl("scenarioAclManagePropagate.xml", "cmis:document", null, null, null, TEST_PERMISSIONS_REMOVE_1, TEST_PRINCIPAL_REMOVE_1);
	}

	@Test
	public void testMoveDocumentAclNone_1() throws Exception {
		String cmisId = doTestMove("scenarioAclNone.xml", "rootObjects_02.xml", TEST_DOC_A, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(cmisId, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(cmisId, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclNone_2() throws Exception {
		String cmisId = doTestMove("scenarioAclNone.xml", "rootObjects_02.xml", TEST_DOC_AB, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclNone_3() throws Exception {
		String cmisId = doTestMove("scenarioAclNone.xml", "rootObjects_02.xml", TEST_DOC_ABC, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AC, TEST_PRINCIPAL_AC, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AC, TEST_PRINCIPAL_AC, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclNone_4() throws Exception {
		String cmisId = doTestMove("scenarioAclNone.xml", "rootObjects_02.xml", TEST_DOC_B, TEST_FOLDER_B, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_B, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
	}

	@Test
	public void testMoveDocumentAclNone_5() throws Exception {
		String cmisId = doTestMove("scenarioAclNone.xml", "rootObjects_02.xml", TEST_DOC_B, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_AB, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
	}

	@Test
	public void testMoveDocumentAclNone_6() throws Exception {
		String cmisId = doTestMove("scenarioAclNone.xml", "rootObjects_02.xml", TEST_DOC_B, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclNone_7() throws Exception {
		String cmisId = doTestMove("scenarioAclNone.xml", "rootObjects_02.xml", TEST_DOC_A, TEST_FOLDER_A, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(cmisId, false);
		String folderCmisId = doTestMove(TEST_FOLDER_A, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(cmisId, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclNone_8() throws Exception {
		String cmisId = doTestMove("scenarioAclNone.xml", "rootObjects_02.xml", TEST_DOC_A2, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(cmisId, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(cmisId, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclNone_9() throws Exception {
		String cmisId = doTestMove("scenarioAclNone.xml", "rootObjects_02.xml", TEST_DOC_AB2, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclNone_10() throws Exception {
		String cmisId = doTestMove("scenarioAclNone.xml", "rootObjects_02.xml", TEST_DOC_ABC2, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_BC, TEST_PRINCIPAL_BC, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_BC, TEST_PRINCIPAL_BC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclNone_11() throws Exception {
		String cmisId = doTestMove("scenarioAclNone.xml", "rootObjects_02.xml", TEST_DOC_B2, TEST_FOLDER_B, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_B, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(folderCmisId, true);
	}

	@Test
	public void testMoveDocumentAclNone_12() throws Exception {
		String cmisId = doTestMove("scenarioAclNone.xml", "rootObjects_02.xml", TEST_DOC_B2, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_AB, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
	}

	@Test
	public void testMoveDocumentAclNone_13() throws Exception {
		String cmisId = doTestMove("scenarioAclNone.xml", "rootObjects_02.xml", TEST_DOC_B3, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclNone_14() throws Exception {
		String cmisId = doTestMove("scenarioAclNone.xml", "rootObjects_02.xml", TEST_DOC_A3, TEST_FOLDER_A, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(cmisId, false);
		String folderCmisId = doTestMove(TEST_FOLDER_A, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(cmisId, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(cmisId, false);
	}

	@Test
	public void testMoveDocumentAclNone_15() throws Exception {
		String cmisId = doTestMove("scenarioAclNone.xml", "rootObjects_02.xml", TEST_DOC_A4, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_AB, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
	}


	@Test
	public void testMoveDocumentAclManageObjectonly_1() throws Exception {
		String cmisId = doTestMove("scenarioAclManageObjectonly.xml", "rootObjects_02.xml", TEST_DOC_A, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(cmisId, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(cmisId, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclManageObjectonly_2() throws Exception {
		String cmisId = doTestMove("scenarioAclManageObjectonly.xml", "rootObjects_02.xml", TEST_DOC_AB, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclManageObjectonly_3() throws Exception {
		String cmisId = doTestMove("scenarioAclManageObjectonly.xml", "rootObjects_02.xml", TEST_DOC_ABC, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AC, TEST_PRINCIPAL_AC, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AC, TEST_PRINCIPAL_AC, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclManageObjectonly_4() throws Exception {
		String cmisId = doTestMove("scenarioAclManageObjectonly.xml", "rootObjects_02.xml", TEST_DOC_B, TEST_FOLDER_B, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_B, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(folderCmisId, true);
	}

	@Test
	public void testMoveDocumentAclManageObjectonly_5() throws Exception {
		String cmisId = doTestMove("scenarioAclManageObjectonly.xml", "rootObjects_02.xml", TEST_DOC_B, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_AB, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManageObjectonly_6() throws Exception {
		String cmisId = doTestMove("scenarioAclManageObjectonly.xml", "rootObjects_02.xml", TEST_DOC_B, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclManageObjectonly_7() throws Exception {
		String cmisId = doTestMove("scenarioAclManageObjectonly.xml", "rootObjects_02.xml", TEST_DOC_A, TEST_FOLDER_A, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(cmisId, false);
		String folderCmisId = doTestMove(TEST_FOLDER_A, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(cmisId, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclManageObjectonly_8() throws Exception {
		String cmisId = doTestMove("scenarioAclManageObjectonly.xml", "rootObjects_02.xml", TEST_DOC_A2, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(cmisId, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(cmisId, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclManageObjectonly_9() throws Exception {
		String cmisId = doTestMove("scenarioAclManageObjectonly.xml", "rootObjects_02.xml", TEST_DOC_AB2, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclManageObjectonly_10() throws Exception {
		String cmisId = doTestMove("scenarioAclManageObjectonly.xml", "rootObjects_02.xml", TEST_DOC_ABC2, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_BC, TEST_PRINCIPAL_BC, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_BC, TEST_PRINCIPAL_BC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclManageObjectonly_11() throws Exception {
		String cmisId = doTestMove("scenarioAclManageObjectonly.xml", "rootObjects_02.xml", TEST_DOC_B2, TEST_FOLDER_B, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_B, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(folderCmisId, true);
	}

	@Test
	public void testMoveDocumentAclManageObjectonly_12() throws Exception {
		String cmisId = doTestMove("scenarioAclManageObjectonly.xml", "rootObjects_02.xml", TEST_DOC_B2, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_AB, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManageObjectonly_13() throws Exception {
		String cmisId = doTestMove("scenarioAclManageObjectonly.xml", "rootObjects_02.xml", TEST_DOC_B3, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclManageObjectonly_14() throws Exception {
		String cmisId = doTestMove("scenarioAclManageObjectonly.xml", "rootObjects_02.xml", TEST_DOC_A3, TEST_FOLDER_A, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(cmisId, false);
		String folderCmisId = doTestMove(TEST_FOLDER_A, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(cmisId, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkEmptyAcl(folderCmisId, false);
	}

	@Test
	public void testMoveDocumentAclManageObjectonly_15() throws Exception {
		String cmisId = doTestMove("scenarioAclManageObjectonly.xml", "rootObjects_02.xml", TEST_DOC_A4, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_AB, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagate_1() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjects_02.xml", TEST_DOC_A, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagate_2() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjects_02.xml", TEST_DOC_AB, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagate_3() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjects_02.xml", TEST_DOC_ABC, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AC, TEST_PRINCIPAL_AC, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AC, TEST_PRINCIPAL_AC, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagate_4() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjects_02.xml", TEST_DOC_B, TEST_FOLDER_B, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_B, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
		checkEmptyAcl(folderCmisId, true);
	}

	@Test
	public void testMoveDocumentAclManagePropagate_5() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjects_02.xml", TEST_DOC_B, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_AB, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AC, TEST_PRINCIPAL_AC, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagate_6() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjects_02.xml", TEST_DOC_B, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagate_7() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjects_02.xml", TEST_DOC_A, TEST_FOLDER_A, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_A, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagate_8() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjects_02.xml", TEST_DOC_A2, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagate_9() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjects_02.xml", TEST_DOC_AB2, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagate_10() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjects_02.xml", TEST_DOC_ABC2, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagate_11() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjects_02.xml", TEST_DOC_B2, TEST_FOLDER_B, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_B, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
		checkEmptyAcl(folderCmisId, true);
	}

	@Test
	public void testMoveDocumentAclManagePropagate_12() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjects_02.xml", TEST_DOC_B2, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_AB, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AC, TEST_PRINCIPAL_AC, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagate_13() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjects_02.xml", TEST_DOC_B3, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagate_14() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjects_02.xml", TEST_DOC_A3, TEST_FOLDER_A, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, false);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		String folderCmisId = doTestMove(TEST_FOLDER_A, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
	}

	@Test
	public void testMoveDocumentAclManagePropagate_15() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjects_02.xml", TEST_DOC_A4, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
		checkEmptyAcl(cmisId, true);
		String folderCmisId = doTestMove(TEST_FOLDER_AB, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AC, TEST_PRINCIPAL_AC, null, null, false);
		checkEmptyAcl(cmisId, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagateDirect_1() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjectsDirectAcl_02.xml", TEST_DOC_A, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagateDirect_2() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjectsDirectAcl_02.xml", TEST_DOC_AB, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagateDirect_3() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjectsDirectAcl_02.xml", TEST_DOC_ABC, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagateDirect_4() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjectsDirectAcl_02.xml", TEST_DOC_B, TEST_FOLDER_B, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_B, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagateDirect_5() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjectsDirectAcl_02.xml", TEST_DOC_B, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_AB, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagateDirect_6() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjectsDirectAcl_02.xml", TEST_DOC_B, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagateDirect_7() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjectsDirectAcl_02.xml", TEST_DOC_A, TEST_FOLDER_A, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_A, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagateDirect_8() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjectsDirectAcl_02.xml", TEST_DOC_A2, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagateDirect_9() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjectsDirectAcl_02.xml", TEST_DOC_AB2, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagateDirect_10() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjectsDirectAcl_02.xml", TEST_DOC_ABC2, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagateDirect_11() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjectsDirectAcl_02.xml", TEST_DOC_B2, TEST_FOLDER_B, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_B, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagateDirect_12() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjectsDirectAcl_02.xml", TEST_DOC_B2, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_AB, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagateDirect_13() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjectsDirectAcl_02.xml", TEST_DOC_B3, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_C, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_B, TEST_PRINCIPAL_B, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagateDirect_14() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjectsDirectAcl_02.xml", TEST_DOC_A3, TEST_FOLDER_A, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_A, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
	}

	@Test
	public void testMoveDocumentAclManagePropagateDirect_15() throws Exception {
		String cmisId = doTestMove("scenarioAclManagePropagate.xml", "rootObjectsDirectAcl_02.xml", TEST_DOC_A4, TEST_FOLDER_AB, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, false);
		String folderCmisId = doTestMove(TEST_FOLDER_AB, TEST_FOLDER_C, ROOT_FOLDER_ID);
		checkAcl(cmisId, TEST_PERMISSIONS_A, TEST_PRINCIPAL_A, null, null, true);
		checkAcl(cmisId, TEST_PERMISSIONS_ABC, TEST_PRINCIPAL_ABC, null, null, false);
		checkAcl(folderCmisId, TEST_PERMISSIONS_AB, TEST_PRINCIPAL_AB, null, null, true);
		checkAcl(folderCmisId, TEST_PERMISSIONS_C, TEST_PRINCIPAL_C, null, null, false);
	}

	//PRIVATE
	private String doTestMove(String baseScenario, String objectScenario, String cmisId, String targetFolderId, String sourceFolderId) throws Exception {
		setScenario(DatabaseOperation.CLEAN_INSERT, baseScenario, objectScenario);
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TEST_REPO_1);
		Holder<String> holderCmisId = new Holder<>(cmisId);
		cmisObjectService.moveObject(TEST_REPO_1, holderCmisId, targetFolderId, sourceFolderId, null);
		return holderCmisId.getValue();
	}

	private String doTestMove(String cmisId, String targetFolderId, String sourceFolderId) throws Exception {
		Holder<String> holderCmisId = new Holder<>(cmisId);
		cmisObjectService.moveObject(TEST_REPO_1, holderCmisId, targetFolderId, sourceFolderId, null);
		return holderCmisId.getValue();
	}

	private String doTestCreateDocumentAcl(String scenario, String objectType, String folderId, String[] permissionNamesToAdd, String[] principalIdsToAdd, String[] permissionNamesToRemove, String[] principalIdsToRemove) throws Exception {
		setScenario(scenario, DatabaseOperation.CLEAN_INSERT);
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TEST_REPO_1);
		mockGenerator.reset();

		CMISObject doc1 = new CMISObject();

		ObjectType tp4 = new ObjectType();
		tp4.setCmisId(objectType);
		doc1.setObjectType(tp4);

		doc1.addProperty(getTestProperty("My Document", TEST_REPO_1, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc1.addProperty(getTestProperty(objectType, TEST_REPO_1, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));

		Properties props = PropertiesBuilder.build(doc1, "");
		org.apache.chemistry.opencmis.commons.data.Acl aclToAdd = buildAcl(doc1, permissionNamesToAdd, principalIdsToAdd);
		org.apache.chemistry.opencmis.commons.data.Acl aclToRemove = buildAcl(doc1, permissionNamesToRemove, principalIdsToRemove);
		return cmisObjectService.createDocument(TEST_REPO_1, props, folderId, null, null, null, aclToAdd, aclToRemove, null);
	}

	private void checkAcl(String cmisId, String[] permissionNamesToAdd, String[] principalIdsToAdd, String[] permissionNamesToRemove, String[] principalIdsToRemove) {
		checkAcl(cmisId, permissionNamesToAdd, principalIdsToAdd, permissionNamesToRemove, principalIdsToRemove, true);
		checkEmptyAcl(cmisId, false);
	}

	private void checkAcl(String cmisId, String[] permissionNamesToAdd, String[] principalIdsToAdd, String[] permissionNamesToRemove, String[] principalIdsToRemove, boolean isDirect) {
		assertNotNull(cmisId);
		ObjectData objectData = cmisObjectService.getObject(TEST_REPO_1, cmisId, null, true, null, null, false, true, null);
		assertNotNull(objectData);
		org.apache.chemistry.opencmis.commons.data.Acl acl = objectData.getAcl();
		assertNotNull(acl);
		assertNotNull(acl.getAces());
		assertFalse(acl.getAces().isEmpty());
		Map<String, Set<String>> testAcl = getTestAcl(permissionNamesToAdd, principalIdsToAdd, permissionNamesToRemove, principalIdsToRemove);
		Set<String> principalIds = new HashSet<>();
		for (Ace ace : acl.getAces()) {
			if (ace.isDirect() == isDirect) {
				assertTrue("Expected ACL doesn't contains the principalId: " + ace.getPrincipalId(), testAcl.containsKey(ace.getPrincipalId()));
				Set<String> permissions = testAcl.get(ace.getPrincipalId());
				assertTrue(ace.getPermissions().containsAll(permissions));
				assertTrue(permissions.containsAll(ace.getPermissions()));
				principalIds.add(ace.getPrincipalId());
			}
		}
		assertTrue("Some expected principalIds are not in the object ACL ", principalIds.containsAll(testAcl.keySet()));
	}

	private void checkEmptyAcl(String cmisId, boolean isDirect) {
		assertNotNull(cmisId);
		ObjectData objectData = cmisObjectService.getObject(TEST_REPO_1, cmisId, null, true, null, null, false, true, null);
		assertNotNull(objectData);
		org.apache.chemistry.opencmis.commons.data.Acl acl = objectData.getAcl();
		assertNotNull(acl);
		assertNotNull(acl.getAces());
		assertFalse(acl.getAces().isEmpty());
		for (Ace ace : acl.getAces()) {
			assertNotSame(isDirect, ace.isDirect());
		}
	}

	private org.apache.chemistry.opencmis.commons.data.Acl buildAcl(CMISObject cmisObject, String[] permissionNames, String[] principalIds) {
		return buildAcl(cmisObject, permissionNames, principalIds, true);
	}

	private org.apache.chemistry.opencmis.commons.data.Acl buildAcl(CMISObject cmisObject, String[] permissionNames, String[] principalIds, boolean isDirect) {
		cmisObject.getAcls().clear();
		if (permissionNames != null) {
			for (int i = 0; i < permissionNames.length; i++) {
				addAcl(cmisObject, permissionNames[i], principalIds[i], isDirect);
			}
		}
		return AclBuilder.build(cmisObject);
	}

	private void addAcl(CMISObject cmisObject, String permissionName, String principalId, boolean isDirect) {
		Permission permission = permSelector.getPermission(permissionName, TEST_REPO_1);

		Acl acl = new Acl();
		acl.setIsDirect(isDirect);
		acl.setPermission(permission);
		acl.setPrincipalId(principalId);
		cmisObject.addAcl(acl);
	}

	private Map<String, Set<String>> getTestAcl(String[] permissionNamesToAdd, String[] principalIdsToAdd, String[] permissionNamesToRemove, String[] principalIdsToRemove) {
		Map<String, Set<String>> testAcl = new HashMap<>();
		if (permissionNamesToAdd != null) {
			for (int i = 0; i < permissionNamesToAdd.length; i++) {
				Set<String> permissions = testAcl.get(principalIdsToAdd[i]);
				if (permissions == null) {
					permissions = new HashSet<>();
					testAcl.put(principalIdsToAdd[i], permissions);
				}
				permissions.add(permissionNamesToAdd[i]);
			}
		}
		if (permissionNamesToRemove != null) {
			for (int i = 0; i < permissionNamesToRemove.length; i++) {
				Set<String> permissions = testAcl.get(principalIdsToRemove[i]);
				if (permissions != null) {
					permissions.remove(permissionNamesToRemove[i]);
					if (permissions.isEmpty()) {
						testAcl.remove(principalIdsToRemove[i]);
					}
				}
			}
		}
		return testAcl;
	}
}
