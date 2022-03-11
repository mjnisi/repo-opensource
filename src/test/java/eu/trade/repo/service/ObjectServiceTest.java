package eu.trade.repo.service;

import static eu.trade.repo.TestConstants.TESTDOC_CMISID;
import static eu.trade.repo.TestConstants.TESTFOLDER_CMISID;
import static eu.trade.repo.TestConstants.TESTSUBFOLDER11_CMISID;
import static eu.trade.repo.TestConstants.TESTSUBFOLDER1_CMISID;
import static eu.trade.repo.TestConstants.TESTSUBFOLDER2_CMISID;
import static eu.trade.repo.TestConstants.TESTSUBFOLDER3_CMISID;
import static eu.trade.repo.TestConstants.TESTSUBFOLDER4_CMISID;
import static eu.trade.repo.TestConstants.TESTSUBFOLDER5_CMISID;
import static eu.trade.repo.TestConstants.TESTSUBFOLDER_CMISID;
import static eu.trade.repo.TestConstants.TEST_DOCTYPE;
import static eu.trade.repo.TestConstants.TEST_PWD;
import static eu.trade.repo.TestConstants.TEST_REPO_2;
import static eu.trade.repo.TestConstants.TEST_USER;
import static eu.trade.repo.util.Constants.CMIS_PATH_SEP;
import static eu.trade.repo.util.Constants.CMIS_READ;
import static eu.trade.repo.util.Constants.CMIS_WRITE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.BaseLobTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.id.IDGenerator;
import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Property;
import eu.trade.repo.model.Repository;
import eu.trade.repo.service.cmis.data.in.CMISObjectBuilder;
import eu.trade.repo.service.cmis.data.out.PropertiesBuilder;
import eu.trade.repo.util.Constants;
import eu.trade.repo.util.ICurrentDate;
import eu.trade.repo.util.Utilities;

public class ObjectServiceTest extends BaseLobTestClass {
	@Autowired
	private IDGenerator mockGenerator;
	
	@Autowired
	private ICurrentDate mockCurrentDate;

	@Before
	public void initIndex() {
	}

	@Before
	public void setScenario() throws Exception {
		setUser(TEST_USER, TEST_PWD, TEST_REPO_2);
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		callContextHolder.login();
		
		mockGenerator.reset();
		mockCurrentDate.reset();
	}


	@Test
	public void testReadWithAcls() throws Exception {
		CMISObject obj = cmisObjectSelector.getCMISObject(TEST_REPO_2, TESTDOC_CMISID);

		Set<Acl> acls = obj.getAcls();
		assertEquals(2, acls.size());

		for (Acl acl : acls) {
			if (acl.getId() == 100) {
				assertEquals(TESTDOC_CMISID, acl.getObject().getCmisObjectId());
				assertEquals(CMIS_READ, acl.getPermission().getName());
				assertEquals("Test PID1", acl.getPrincipalId());
			} else
				if (acl.getId() == 101) {
					assertEquals(TESTDOC_CMISID, acl.getObject().getCmisObjectId());
					assertEquals(CMIS_WRITE, acl.getPermission().getName());
					assertEquals("Test PID2", acl.getPrincipalId());
				}
		}
	}

	@Test
	@Transactional
	public void testUpdateMultivaluedProperties() {
		CMISObject obj = cmisObjectSelector.getCMISObject(TEST_REPO_2, TESTDOC_CMISID);
		//		CMISObject rootFolder = cmisObjectSelector.loadCMISObject("/");

		Properties ps = PropertiesBuilder.build(obj, "test:prop3");
		objectService.updateProperties(TEST_REPO_2, obj.getCmisObjectId(), ps);

	}

	@Test (expected=CmisInvalidArgumentException.class)
	public void negativeScenarios_cannotCreate_InvalidName() throws Exception {
		setScenario("scenarioCreateObject.xml", DatabaseOperation.CLEAN_INSERT);
		//PARENT OBJECT
		CMISObject rootFolder = cmisObjectSelector.loadCMISObject("/");

		assertEquals(0, rootFolder.getChildren().size());
		assertNotNull(rootFolder);

		//FOLDER, child of root
		CMISObject folder = new CMISObject();
		ObjectType tp = new ObjectType();
		tp.setCmisId("test:folder");
		folder.setObjectType(tp);
		folder.addProperty(getTestProperty("My/Folder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		folder.addProperty(getTestProperty("test:folder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));
		folder.addParent(rootFolder);

		objectService.createObject(TEST_REPO_2, folder, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_FOLDER);
	}

	@Test (expected=CmisNameConstraintViolationException.class)
	public void negativeScenarios_cannotCreate_ChildwithNameExists() throws Exception {
		setScenario("scenarioCreateObject.xml", DatabaseOperation.CLEAN_INSERT);
		//PARENT OBJECT
		CMISObject rootFolder = cmisObjectSelector.loadCMISObject("/");

		assertEquals(0, rootFolder.getChildren().size());
		assertNotNull(rootFolder);
		//FOLDER, child of root
		CMISObject folder = new CMISObject();

		ObjectType tp = objTypeSelector.getObjectTypeByCmisId(TEST_REPO_2, "test:folder");
		folder.setObjectType(tp);
		folder.addProperty(getTestProperty("MyFolder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		folder.addProperty(getTestProperty("test:folder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));
		folder.addParent(rootFolder);

		//CREATE FOLDERs
		objectService.createObject(TEST_REPO_2, folder, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_FOLDER);

		//CREATE OBJECTS
		//DOC1, child of root
		CMISObject doc1 = new CMISObject();
		doc1.setObjectType(objTypeSelector.getObjectTypeByCmisId(TEST_REPO_2, TEST_DOCTYPE));
		doc1.addProperty(getTestProperty("doc 1", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc1.addProperty(getTestProperty(TEST_DOCTYPE, TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));
		doc1.addParent(folder);
		objectService.createObject(TEST_REPO_2, doc1, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);

		//DOC2, child of root
		CMISObject doc2 = new CMISObject();
		doc2.setObjectType(objTypeSelector.getObjectTypeByCmisId(TEST_REPO_2, TEST_DOCTYPE));
		doc2.addProperty(getTestProperty("doc 1", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME)); //UH OH, SAME NAME AS DOC1!!! --> EXCEPTION EXPECTED...
		doc2.addProperty(getTestProperty(TEST_DOCTYPE, TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));
		doc2.addParent(folder);
		objectService.createObject(TEST_REPO_2, doc2, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);
	}

	@Test
	public void testCreate_UnfilledDocsWithSameName() throws Exception {
		setScenario("scenarioCreateObject.xml", DatabaseOperation.CLEAN_INSERT);
		//PARENT OBJECT
		CMISObject rootFolder = cmisObjectSelector.loadCMISObject("/");

		assertEquals(0, rootFolder.getChildren().size());
		assertNotNull(rootFolder);
		//FOLDER, child of root
		CMISObject folder = new CMISObject();
		ObjectType tp = new ObjectType();
		tp.setCmisId("test:folder");
		folder.setObjectType(tp);
		folder.addProperty(getTestProperty("MyFolder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		folder.addProperty(getTestProperty("test:folder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));
		folder.addParent(rootFolder);

		//DOC1, child of root
		CMISObject doc1 = new CMISObject();
		ObjectType tp4 = new ObjectType();
		tp4.setCmisId(TEST_DOCTYPE);
		doc1.setObjectType(tp4);
		doc1.addProperty(getTestProperty("doc 1", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc1.addProperty(getTestProperty(TEST_DOCTYPE, TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));

		CMISObject doc2 = new CMISObject();
		ObjectType tp5 = new ObjectType();
		tp5.setCmisId(TEST_DOCTYPE);
		doc2.setObjectType(tp5);
		doc2.addProperty(getTestProperty("doc 1", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc2.addProperty(getTestProperty(TEST_DOCTYPE, TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));

		//CREATE FOLDERs
		objectService.createObject(TEST_REPO_2, folder, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_FOLDER);
		//CREATE OBJECTS
		objectService.createObject(TEST_REPO_2, doc1, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);
		objectService.createObject(TEST_REPO_2, doc2, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);
	}

	@Test (expected=RuntimeException.class)
	public void negativeScenarios_cannotCreate_NoType() throws Exception {
		CMISObject cmisObject = new CMISObject();
		objectService.createObject(TEST_REPO_2, cmisObject, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);
	}

	@Test (expected=RuntimeException.class)
	public void negativeScenarios_cannotCreate_UnknownType() throws Exception {
		CMISObject cmisObject = new CMISObject();
		ObjectType tp = new ObjectType();
		tp.setCmisId("test:unknown");

		cmisObject.setObjectType(tp);
		objectService.createObject(TEST_REPO_2, cmisObject, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);
	}

	@Ignore
	@Test (expected=RuntimeException.class)
	public void negativeScenarios_cannotCreate_missingRequired() throws Exception {
		CMISObject cmisObject = new CMISObject();

		ObjectType tp = new ObjectType();
		tp.setCmisId("test:subfolder");
		cmisObject.setObjectType(tp);

		ObjectTypeProperty namePropertyType = objTypePropSelector.getObjTypeProperty(Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME, TEST_REPO_2);
		Property name = new Property(namePropertyType, "MyFolder");
		name.setObject(cmisObject);
		cmisObject.addParent(cmisObjectSelector.loadCMISObject("/"));
		cmisObject.addProperty(name);


		objectService.createObject(TEST_REPO_2, cmisObject, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_FOLDER);
	}

	@Test (expected=CmisNameConstraintViolationException.class)
	public void negativeScenarios_cannotCreate_emptyName() throws Exception {
		CMISObject rootFolder = cmisObjectSelector.loadCMISObject("/");

		CMISObject doc2 = new CMISObject();
		ObjectType tp5 = new ObjectType();
		tp5.setCmisId(TEST_DOCTYPE);
		doc2.setObjectType(tp5);
		doc2.addProperty(getTestProperty("", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc2.addProperty(getTestProperty(TEST_DOCTYPE, TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));
		doc2.addParent(rootFolder);

		objectService.createObject(TEST_REPO_2, doc2, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);
	}

	@Test(expected=CmisInvalidArgumentException.class)
	public void negativeScenarios_cannotMove_TargetDescendatOfObject() throws Exception{
		setScenario("scenarioCreateObject.xml", DatabaseOperation.CLEAN_INSERT);
		//PARENT OBJECT
		CMISObject rootFolder = cmisObjectSelector.loadCMISObject("/");

		assertEquals(0, rootFolder.getChildren().size());
		assertNotNull(rootFolder);
		//FOLDER, child of root
		CMISObject folder = new CMISObject();
		folder.setObjectType(objTypeSelector.getObjectTypeByCmisId(TEST_REPO_2, "test:folder"));
		folder.addProperty(getTestProperty("MyFolder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		folder.addProperty(getTestProperty("test:folder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));
		folder.addParent(rootFolder);

		objectService.createObject(TEST_REPO_2, folder, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_FOLDER);

		//SUBFOLDER1 , child of folder
		CMISObject subfolder1 = new CMISObject();
		subfolder1.setObjectType(objTypeSelector.getObjectTypeByCmisId(TEST_REPO_2, "test:subfolder"));
		subfolder1.addProperty(getTestProperty("My Subfolder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		subfolder1.addProperty(getTestProperty("test:subfolder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));
		subfolder1.addParent(folder);

		objectService.createObject(TEST_REPO_2, subfolder1, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_FOLDER);

		objectService.moveObject(TEST_REPO_2, folder.getCmisObjectId(), rootFolder.getCmisObjectId(), subfolder1.getCmisObjectId());
	}

	@Test(expected=CmisInvalidArgumentException.class)
	public void negativeScenarios_cannotMove_TargetEqualsObject() throws Exception{
		setScenario("scenarioCreateObject.xml", DatabaseOperation.CLEAN_INSERT);
		//PARENT OBJECT
		CMISObject rootFolder = cmisObjectSelector.loadCMISObject("/");

		assertEquals(0, rootFolder.getChildren().size());
		assertNotNull(rootFolder);
		//FOLDER, child of root
		CMISObject folder = new CMISObject();
		folder.setObjectType(objTypeSelector.getObjectTypeByCmisId(TEST_REPO_2, "test:folder"));
		folder.addProperty(getTestProperty("MyFolder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		folder.addProperty(getTestProperty("test:folder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));
		folder.addParent(rootFolder);

		objectService.createObject(TEST_REPO_2, folder, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_FOLDER);

		objectService.moveObject(TEST_REPO_2, folder.getCmisObjectId(), rootFolder.getCmisObjectId(), folder.getCmisObjectId());
	}

	@Test
	public void testCreatePolicy() throws Exception{
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		CMISObject policy = new CMISObject();
		policy.setObjectType(objTypeSelector.getObjectTypeByCmisId(TEST_REPO_2, "cmis:policy"));
		policy.addProperty(getTestProperty("My Policy", TEST_REPO_2, Constants.TYPE_CMIS_POLICY, PropertyIds.NAME));
		policy.addProperty(getTestProperty("cmis:policy", TEST_REPO_2, Constants.TYPE_CMIS_POLICY, PropertyIds.OBJECT_TYPE_ID));
		policy.addProperty(getTestProperty("my test policy!", TEST_REPO_2, Constants.TYPE_CMIS_POLICY, PropertyIds.POLICY_TEXT));

		Integer id = objectService.createObject(TEST_REPO_2, policy, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(),  null, null, BaseTypeId.CMIS_POLICY).getId();
		compareTable(
				"object",
				"CMIS_OBJECT_ID = 'generatedId-1'",
				"objectService-testCreatePolicy.xml");

		compareTable(
				"Property",
				"OBJECT_ID = '"+id.toString()+"'",
				"objectService-testCreatePolicy.xml");

	}

	@Test(expected=CmisInvalidArgumentException.class)
	public void negativeScenario_noPolicyext() throws Exception{
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		CMISObject policy = new CMISObject();
		policy.setObjectType(objTypeSelector.getObjectTypeByCmisId(TEST_REPO_2, "cmis:policy"));
		policy.addProperty(getTestProperty("My Policy", TEST_REPO_2, Constants.TYPE_CMIS_POLICY, PropertyIds.NAME));
		policy.addProperty(getTestProperty("cmis:policy", TEST_REPO_2, Constants.TYPE_CMIS_POLICY, PropertyIds.OBJECT_TYPE_ID));

		objectService.createObject(TEST_REPO_2, policy, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(),  null, null, BaseTypeId.CMIS_POLICY);
	}

	@Test
	public void testCreateObject() throws Exception{

		setScenario("scenarioCreateObject.xml", DatabaseOperation.CLEAN_INSERT);
		//PARENT OBJECT

		CMISObject rootFolder = cmisObjectSelector.loadCMISObject("/");

		assertEquals(0, rootFolder.getChildren().size());
		assertNotNull(rootFolder);

		//FOLDER, child of root
		CMISObject folder = new CMISObject();
		folder.setObjectType(objTypeSelector.getObjectTypeByCmisId(TEST_REPO_2, "test:folder"));
		folder.addProperty(getTestProperty("MyFolder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		folder.addProperty(getTestProperty("test:folder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));
		folder.addParent(rootFolder);

		objectService.createObject(TEST_REPO_2, folder, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_FOLDER);

		//SUBFOLDER1 , child of root
		CMISObject subfolder1 = new CMISObject();
		subfolder1.setObjectType(objTypeSelector.getObjectTypeByCmisId(TEST_REPO_2, "test:subfolder"));
		subfolder1.addProperty(getTestProperty("My Subfolder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		subfolder1.addProperty(getTestProperty("test:subfolder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));
		subfolder1.addParent(rootFolder);

		objectService.createObject(TEST_REPO_2, subfolder1, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_FOLDER);

		//SUBFOLDER2, child of SUBFOLDER1
		CMISObject subfolder2 = new CMISObject();
		subfolder2.setObjectType(objTypeSelector.getObjectTypeByCmisId(TEST_REPO_2, "test:subfolder"));
		subfolder2.addProperty(getTestProperty("My Subfolder 2", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		subfolder2.addProperty(getTestProperty("test:subfolder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));
		subfolder2.addParent(subfolder1);

		objectService.createObject(TEST_REPO_2, subfolder2, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_FOLDER);

		//DOC1, child of root
		CMISObject doc1 = new CMISObject();
		doc1.setObjectType(objTypeSelector.getObjectTypeByCmisId(TEST_REPO_2, TEST_DOCTYPE));
		doc1.addProperty(getTestProperty("My Subfolder 2", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc1.addProperty(getTestProperty(TEST_DOCTYPE, TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));

		objectService.createObject(TEST_REPO_2, doc1, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);

		CMISObject doc2 = new CMISObject();
		doc2.setObjectType(objTypeSelector.getObjectTypeByCmisId(TEST_REPO_2, TEST_DOCTYPE));
		doc2.addProperty(getTestProperty("My Subfolder 2", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc2.addProperty(getTestProperty(TEST_DOCTYPE, TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));

		objectService.createObject(TEST_REPO_2, doc2, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);

		/*compareTable(
				"object",
				"1 = 1",
				"objectService-testCreate2.xml");

		compareTable(
				"object_child",
				"1 = 1",
				"objectService-testCreate2.xml");*/
	}

	@Test
	//@Transactional
	public void testCreateObject_PopulatedFolderProps() throws Exception{

		CMISObject cmisObject = new CMISObject();

		//OBJECT TYPE
		cmisObject.setObjectType(objTypeSelector.getObjectTypeByCmisId(TEST_REPO_2, "test:subfolder"));

		//REQUIRED FOLDER PROPERTIES, name and objtypeid
		cmisObject.addProperty(getTestProperty("MyFolder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		cmisObject.addProperty(getTestProperty("test:subfolder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));

		//PARENT OBJECT
		cmisObjectSelector.loadCMISObject(TESTSUBFOLDER1_CMISID);
		CMISObject parent = cmisObjectSelector.loadCMISObject(cmisObjectSelector.getCMISObject(TestConstants.TEST_REPO_2, "/my folder/my subfolder 1/my subfolder 3").getCmisObjectId());
		cmisObject.addParent(parent);

		cmisObject = objectService.createObject(TEST_REPO_2, cmisObject, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_FOLDER);

		Integer id = cmisObject.getId();
		assertNotNull(id);

		compareTable(
				"object_child",
				"CHILD_OBJECT_ID="+id.intValue(),
				"objectService-testCreate.xml");

		compareTable(
				"object",
				"ID="+id.intValue(),
				"objectService-testCreate.xml");

		compareTable(
				"property",
				"OBJECT_ID="+id.intValue(),
				"objectService-testCreate.xml");

		CMISObject object = cmisObjectSelector.loadCMISObject(cmisObjectSelector.getCMISObject(TestConstants.TEST_REPO_2, "/my folder/my subfolder 1/my subfolder 3/MyFolder").getCmisObjectId());

		assertNotNull(object.getId());
		assertEquals(11, object.getProperties().size()); // 2mandatory (type and name), 4derived (created, lastmod, path and basetype)
	}

	@Test
	public void testCreateObjectWithStream() throws Exception {

		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioCreateObject.xml");

		CMISObject doc1 = new CMISObject();
		ObjectType tp4 = new ObjectType();
		tp4.setCmisId(TEST_DOCTYPE);
		doc1.setObjectType(tp4);
		doc1.addProperty(getTestProperty("Document 1", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc1.addProperty(getTestProperty(TEST_DOCTYPE, TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));

		InputStream in = new ByteArrayInputStream(TXT_FILE_CONTENT_1.getBytes());
		ContentStream cs = new ContentStreamImpl("Test Document 1 Name", BigInteger.valueOf(in.available()), "text/plain", in);

		doc1 = objectService.createObject(TEST_REPO_2, doc1, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), cs, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);

		compareLobTable(
				"stream",
				"ID = ( SELECT ID FROM OBJECT WHERE CMIS_OBJECT_ID='generatedId-1')",
				"objectService-testContentStream.xml");

		compareLobTable(
				"property",
				"OBJECT_ID = ( SELECT ID FROM OBJECT WHERE CMIS_OBJECT_ID='generatedId-1') and OBJECT_TYPE_PROPERTY_ID in (108, 120,121,122,123)",
				"OBJECT_TYPE_PROPERTY_ID",
				"objectService-testContentStream.xml");
	}

	@Test (expected=CmisInvalidArgumentException.class)
	public void negativeScenarios_cannotCreateObject_noparent() throws Exception{
		CMISObject cmisObject = new CMISObject();

		ObjectType tp = new ObjectType();
		tp.setCmisId("test:subfolder");
		cmisObject.setObjectType(tp);

		objectService.createObject(TEST_REPO_2, cmisObject, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_FOLDER);
	}

	@Test (expected=CmisInvalidArgumentException.class)
	public void negativeScenarios_cannotCreateObject_invalidObjectType() throws Exception{
		CMISObject cmisObject = new CMISObject();

		ObjectType tp = new ObjectType();
		tp.setCmisId("invalid");
		cmisObject.setObjectType(tp);

		objectService.createObject(TEST_REPO_2, cmisObject, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);
	}

	@Test (expected=CmisInvalidArgumentException.class)
	public void negativeScenarios_cannotCreateObject_noObjectType() throws Exception{
		CMISObject cmisObject = new CMISObject();

		objectService.createObject(TEST_REPO_2, cmisObject, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);
	}

	@Test
	public void testGetObjectByPath() throws Exception {
		CMISObject myFolder = objectService.getObject(TEST_REPO_2, "/my folder");
		CMISObject mySubFolder = objectService.getObject(TEST_REPO_2, "/my folder/my subfolder");
		CMISObject myDoc = objectService.getObject(TEST_REPO_2, "/my folder/my document");

		CMISObject mySubFldr3 = objectService.getObject(TEST_REPO_2, "/my folder/my subfolder 1/my subfolder 3");
		CMISObject mySubFldr4 = objectService.getObject(TEST_REPO_2, "/my folder/my subfolder 1/my subfolder 4");
		CMISObject myDoc3 = objectService.getObject(TEST_REPO_2, "/my folder/my subfolder 1/my subfolder 3/my document 3");

		assertEquals(103, myDoc.getId().intValue());
		assertEquals(100, myFolder.getId().intValue());
		assertEquals(101, mySubFolder.getId().intValue());

		assertEquals(105, myDoc3.getId().intValue());
		assertEquals(106, mySubFldr3.getId().intValue());
		assertEquals(107, mySubFldr4.getId().intValue());

	}

	@Test (expected=CmisObjectNotFoundException.class)
	public void negativeScenarios_cannotFind() throws Exception {
		try {
			objectService.getObject(TEST_REPO_2, "/unknown folder");
		} catch (Exception e) {
			assertTrue(e.getMessage().equals("Cannot find Object described by path : /unknown folder"));
			throw e;
		}
	}

	@Test (expected=CmisNameConstraintViolationException.class)
	public void negativeScenarios_cannotMove_ChildwithNameExists() throws Exception {
		CMISObject subfolder1 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER1_CMISID);
		CMISObject subfolder2 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER2_CMISID);

		CMISObject doc1 = new CMISObject();
		ObjectType tp4 = new ObjectType();
		tp4.setCmisId(TEST_DOCTYPE);
		doc1.setObjectType(tp4);
		doc1.addProperty(getTestProperty("doc 1", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc1.addProperty(getTestProperty(TEST_DOCTYPE, TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));
		doc1.addParent(subfolder1);

		CMISObject doc2 = new CMISObject();
		ObjectType tp5 = new ObjectType();
		tp5.setCmisId(TEST_DOCTYPE);
		doc2.setObjectType(tp5);
		doc2.addProperty(getTestProperty("doc 1", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc2.addProperty(getTestProperty(TEST_DOCTYPE, TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));
		doc2.addParent(subfolder2);

		//CREATE OBJECTS
		objectService.createObject(TEST_REPO_2, doc1, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);
		objectService.createObject(TEST_REPO_2, doc2, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);

		restart();
		setUser(TEST_USER, TEST_PWD, TEST_REPO_2);

		//move doc1 of subfolder 1 under subfolder 2 (doc2 with same name already there)
		objectService.moveObject(TEST_REPO_2, doc1.getCmisObjectId(), subfolder1.getCmisObjectId(), subfolder2.getCmisObjectId());
	}

	@Test
	public void testMoveObject_updatePaths() throws Exception {
		CMISObject rootFolder = cmisObjectSelector.loadCMISObject(TESTFOLDER_CMISID);

		CMISObject subfolder1 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER1_CMISID);
		CMISObject subfolder11 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER11_CMISID);
		CMISObject subfolder2 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER2_CMISID);
		CMISObject subfolder3 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER3_CMISID);
		CMISObject subfolder4 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER4_CMISID);
		CMISObject subfolder5 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER5_CMISID);

		String rootPath = propertySelector.getPropertyValue(rootFolder.getCmisObjectId(), PropertyIds.PATH);
		String sf1Path = propertySelector.getPropertyValue(subfolder1.getCmisObjectId(), PropertyIds.PATH);
		String sf11Path = propertySelector.getPropertyValue(subfolder11.getCmisObjectId(), PropertyIds.PATH);
		String sf2Path = propertySelector.getPropertyValue(subfolder2.getCmisObjectId(), PropertyIds.PATH);
		String sf3Path = propertySelector.getPropertyValue(subfolder3.getCmisObjectId(), PropertyIds.PATH);
		String sf4Path = propertySelector.getPropertyValue(subfolder4.getCmisObjectId(), PropertyIds.PATH);
		String sf5Path = propertySelector.getPropertyValue(subfolder5.getCmisObjectId(), PropertyIds.PATH);

		assertEquals("/my folder", rootPath);

		assertEquals("/my folder/my subfolder 1", sf1Path);
		assertEquals("/my folder/my subfolder 11", sf11Path);
		assertEquals("/my folder/my subfolder 2", sf2Path);
		assertEquals("/my folder/my subfolder 1/my subfolder 3", sf3Path);
		assertEquals("/my folder/my subfolder 1/my subfolder 4", sf4Path);
		assertEquals("/my folder/my subfolder 1/my subfolder 4/my subfolder 5", sf5Path);

		//move subfolder 1 under subfolder 2
		objectService.moveObject(TEST_REPO_2, subfolder1.getCmisObjectId(), rootFolder.getCmisObjectId(), subfolder2.getCmisObjectId());

		restart();

		subfolder1 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER1_CMISID);
		subfolder11 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER11_CMISID);
		subfolder2 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER2_CMISID);
		subfolder3 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER3_CMISID);
		subfolder4 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER4_CMISID);

		rootPath = propertySelector.getPropertyValue(rootFolder.getCmisObjectId(), PropertyIds.PATH);
		sf1Path = propertySelector.getPropertyValue(subfolder1.getCmisObjectId(), PropertyIds.PATH);
		sf11Path = propertySelector.getPropertyValue(subfolder11.getCmisObjectId(), PropertyIds.PATH);
		sf2Path = propertySelector.getPropertyValue(subfolder2.getCmisObjectId(), PropertyIds.PATH);
		sf3Path = propertySelector.getPropertyValue(subfolder3.getCmisObjectId(), PropertyIds.PATH);
		sf4Path = propertySelector.getPropertyValue(subfolder4.getCmisObjectId(), PropertyIds.PATH);
		sf5Path = propertySelector.getPropertyValue(subfolder5.getCmisObjectId(), PropertyIds.PATH);

		assertEquals("/my folder", rootPath);

		assertEquals("/my folder/my subfolder 11", sf11Path);
		assertEquals("/my folder/my subfolder 2", sf2Path);
		assertEquals("/my folder/my subfolder 2/my subfolder 1", sf1Path);
		assertEquals("/my folder/my subfolder 2/my subfolder 1/my subfolder 3", sf3Path);
		assertEquals("/my folder/my subfolder 2/my subfolder 1/my subfolder 4", sf4Path);
		assertEquals("/my folder/my subfolder 2/my subfolder 1/my subfolder 4/my subfolder 5", sf5Path);
	}

	@Test
	public void testRenameObject_updatePaths() throws Exception {
		CMISObject rootFolder = cmisObjectSelector.loadCMISObject(TESTFOLDER_CMISID);

		CMISObject subfolder1 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER1_CMISID);
		CMISObject subfolder11 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER11_CMISID);
		CMISObject subfolder2 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER2_CMISID);
		CMISObject subfolder3 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER3_CMISID);
		CMISObject subfolder4 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER4_CMISID);
		CMISObject subfolder5 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER5_CMISID);

		String rootPath = propertySelector.getPropertyValue(rootFolder.getCmisObjectId(), PropertyIds.PATH);
		String sf1Path = propertySelector.getPropertyValue(subfolder1.getCmisObjectId(), PropertyIds.PATH);
		String sf11Path = propertySelector.getPropertyValue(subfolder11.getCmisObjectId(), PropertyIds.PATH);
		String sf2Path = propertySelector.getPropertyValue(subfolder2.getCmisObjectId(), PropertyIds.PATH);
		String sf3Path = propertySelector.getPropertyValue(subfolder3.getCmisObjectId(), PropertyIds.PATH);
		String sf4Path = propertySelector.getPropertyValue(subfolder4.getCmisObjectId(), PropertyIds.PATH);
		String sf5Path = propertySelector.getPropertyValue(subfolder5.getCmisObjectId(), PropertyIds.PATH);

		assertEquals("/my folder", rootPath);

		assertEquals("/my folder/my subfolder 1", sf1Path);
		assertEquals("/my folder/my subfolder 11", sf11Path);
		assertEquals("/my folder/my subfolder 2", sf2Path);
		assertEquals("/my folder/my subfolder 1/my subfolder 3", sf3Path);
		assertEquals("/my folder/my subfolder 1/my subfolder 4", sf4Path);
		assertEquals("/my folder/my subfolder 1/my subfolder 4/my subfolder 5", sf5Path);

		//move subfolder 1 under subfolder 2
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, "renamed"));
		objectService.updateProperties(TEST_REPO_2, subfolder1.getCmisObjectId(), properties);

		restart();

		subfolder1 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER1_CMISID);
		subfolder11 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER11_CMISID);
		subfolder2 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER2_CMISID);
		subfolder3 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER3_CMISID);
		subfolder4 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER4_CMISID);

		rootPath = propertySelector.getPropertyValue(rootFolder.getCmisObjectId(), PropertyIds.PATH);
		sf1Path = propertySelector.getPropertyValue(subfolder1.getCmisObjectId(), PropertyIds.PATH);
		sf11Path = propertySelector.getPropertyValue(subfolder11.getCmisObjectId(), PropertyIds.PATH);
		sf2Path = propertySelector.getPropertyValue(subfolder2.getCmisObjectId(), PropertyIds.PATH);
		sf3Path = propertySelector.getPropertyValue(subfolder3.getCmisObjectId(), PropertyIds.PATH);
		sf4Path = propertySelector.getPropertyValue(subfolder4.getCmisObjectId(), PropertyIds.PATH);
		sf5Path = propertySelector.getPropertyValue(subfolder5.getCmisObjectId(), PropertyIds.PATH);

		assertEquals("/my folder", rootPath);

		assertEquals("/my folder/renamed", sf1Path);
		assertEquals("/my folder/my subfolder 11", sf11Path);
		assertEquals("/my folder/my subfolder 2", sf2Path);
		assertEquals("/my folder/renamed/my subfolder 3", sf3Path);
		assertEquals("/my folder/renamed/my subfolder 4", sf4Path);
		assertEquals("/my folder/renamed/my subfolder 4/my subfolder 5", sf5Path);
	}

	@Test
	public void testRenameObject_updatePaths_2() throws Exception {
		CMISObject rootFolder = cmisObjectSelector.loadCMISObject(TESTFOLDER_CMISID);

		CMISObject subfolder1 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER1_CMISID);
		CMISObject subfolder11 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER11_CMISID);
		CMISObject subfolder2 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER2_CMISID);
		CMISObject subfolder3 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER3_CMISID);
		CMISObject subfolder4 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER4_CMISID);
		CMISObject subfolder5 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER5_CMISID);

		String rootPath = propertySelector.getPropertyValue(rootFolder.getCmisObjectId(), PropertyIds.PATH);
		String sf1Path = propertySelector.getPropertyValue(subfolder1.getCmisObjectId(), PropertyIds.PATH);
		String sf11Path = propertySelector.getPropertyValue(subfolder11.getCmisObjectId(), PropertyIds.PATH);
		String sf2Path = propertySelector.getPropertyValue(subfolder2.getCmisObjectId(), PropertyIds.PATH);
		String sf3Path = propertySelector.getPropertyValue(subfolder3.getCmisObjectId(), PropertyIds.PATH);
		String sf4Path = propertySelector.getPropertyValue(subfolder4.getCmisObjectId(), PropertyIds.PATH);
		String sf5Path = propertySelector.getPropertyValue(subfolder5.getCmisObjectId(), PropertyIds.PATH);

		assertEquals("/my folder", rootPath);

		assertEquals("/my folder/my subfolder 1", sf1Path);
		assertEquals("/my folder/my subfolder 11", sf11Path);
		assertEquals("/my folder/my subfolder 2", sf2Path);
		assertEquals("/my folder/my subfolder 1/my subfolder 3", sf3Path);
		assertEquals("/my folder/my subfolder 1/my subfolder 4", sf4Path);
		assertEquals("/my folder/my subfolder 1/my subfolder 4/my subfolder 5", sf5Path);

		//move subfolder 1 under subfolder 2
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, "renamed"));
		objectService.updateProperties(TEST_REPO_2, rootFolder.getCmisObjectId(), properties);

		restart();

		subfolder1 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER1_CMISID);
		subfolder11 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER11_CMISID);
		subfolder2 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER2_CMISID);
		subfolder3 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER3_CMISID);
		subfolder4 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER4_CMISID);

		rootPath = propertySelector.getPropertyValue(rootFolder.getCmisObjectId(), PropertyIds.PATH);
		sf1Path = propertySelector.getPropertyValue(subfolder1.getCmisObjectId(), PropertyIds.PATH);
		sf11Path = propertySelector.getPropertyValue(subfolder11.getCmisObjectId(), PropertyIds.PATH);
		sf2Path = propertySelector.getPropertyValue(subfolder2.getCmisObjectId(), PropertyIds.PATH);
		sf3Path = propertySelector.getPropertyValue(subfolder3.getCmisObjectId(), PropertyIds.PATH);
		sf4Path = propertySelector.getPropertyValue(subfolder4.getCmisObjectId(), PropertyIds.PATH);
		sf5Path = propertySelector.getPropertyValue(subfolder5.getCmisObjectId(), PropertyIds.PATH);

		assertEquals("/renamed", rootPath);

		assertEquals("/renamed/my subfolder 1", sf1Path);
		assertEquals("/renamed/my subfolder 11", sf11Path);
		assertEquals("/renamed/my subfolder 2", sf2Path);
		assertEquals("/renamed/my subfolder 1/my subfolder 3", sf3Path);
		assertEquals("/renamed/my subfolder 1/my subfolder 4", sf4Path);
		assertEquals("/renamed/my subfolder 1/my subfolder 4/my subfolder 5", sf5Path);
	}

	@Test
	public void testRenameRoot_updatePaths() throws Exception {
		CMISObject rootFolder = cmisObjectSelector.loadCMISObject(CMIS_PATH_SEP);
		CMISObject testRootFolder = cmisObjectSelector.loadCMISObject(TESTFOLDER_CMISID);

		CMISObject subfolder1 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER1_CMISID);
		CMISObject subfolder11 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER11_CMISID);
		CMISObject subfolder2 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER2_CMISID);
		CMISObject subfolder3 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER3_CMISID);
		CMISObject subfolder4 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER4_CMISID);
		CMISObject subfolder5 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER5_CMISID);

		String rootPath = propertySelector.getPropertyValue(rootFolder.getCmisObjectId(), PropertyIds.PATH);
		String testRootPath = propertySelector.getPropertyValue(testRootFolder.getCmisObjectId(), PropertyIds.PATH);
		String sf1Path = propertySelector.getPropertyValue(subfolder1.getCmisObjectId(), PropertyIds.PATH);
		String sf11Path = propertySelector.getPropertyValue(subfolder11.getCmisObjectId(), PropertyIds.PATH);
		String sf2Path = propertySelector.getPropertyValue(subfolder2.getCmisObjectId(), PropertyIds.PATH);
		String sf3Path = propertySelector.getPropertyValue(subfolder3.getCmisObjectId(), PropertyIds.PATH);
		String sf4Path = propertySelector.getPropertyValue(subfolder4.getCmisObjectId(), PropertyIds.PATH);
		String sf5Path = propertySelector.getPropertyValue(subfolder5.getCmisObjectId(), PropertyIds.PATH);

		assertEquals("/", rootPath);
		assertEquals("/my folder", testRootPath);

		assertEquals("/my folder/my subfolder 1", sf1Path);
		assertEquals("/my folder/my subfolder 11", sf11Path);
		assertEquals("/my folder/my subfolder 2", sf2Path);
		assertEquals("/my folder/my subfolder 1/my subfolder 3", sf3Path);
		assertEquals("/my folder/my subfolder 1/my subfolder 4", sf4Path);
		assertEquals("/my folder/my subfolder 1/my subfolder 4/my subfolder 5", sf5Path);

		//move subfolder 1 under subfolder 2
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, "renamed"));
		objectService.updateProperties(TEST_REPO_2, rootFolder.getCmisObjectId(), properties);

		restart();

		subfolder1 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER1_CMISID);
		subfolder11 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER11_CMISID);
		subfolder2 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER2_CMISID);
		subfolder3 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER3_CMISID);
		subfolder4 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER4_CMISID);

		testRootPath = propertySelector.getPropertyValue(testRootFolder.getCmisObjectId(), PropertyIds.PATH);
		sf1Path = propertySelector.getPropertyValue(subfolder1.getCmisObjectId(), PropertyIds.PATH);
		sf11Path = propertySelector.getPropertyValue(subfolder11.getCmisObjectId(), PropertyIds.PATH);
		sf2Path = propertySelector.getPropertyValue(subfolder2.getCmisObjectId(), PropertyIds.PATH);
		sf3Path = propertySelector.getPropertyValue(subfolder3.getCmisObjectId(), PropertyIds.PATH);
		sf4Path = propertySelector.getPropertyValue(subfolder4.getCmisObjectId(), PropertyIds.PATH);
		sf5Path = propertySelector.getPropertyValue(subfolder5.getCmisObjectId(), PropertyIds.PATH);

		assertEquals("/", rootPath);
		assertEquals("/my folder", testRootPath);

		assertEquals("/my folder/my subfolder 1", sf1Path);
		assertEquals("/my folder/my subfolder 11", sf11Path);
		assertEquals("/my folder/my subfolder 2", sf2Path);
		assertEquals("/my folder/my subfolder 1/my subfolder 3", sf3Path);
		assertEquals("/my folder/my subfolder 1/my subfolder 4", sf4Path);
		assertEquals("/my folder/my subfolder 1/my subfolder 4/my subfolder 5", sf5Path);
	}

	@Test (expected=CmisInvalidArgumentException.class)
	public void negativeScenarios_cannotMoveUnderSameParent() throws Exception {
		CMISObject doc = cmisObjectSelector.loadCMISObject(TESTDOC_CMISID);
		CMISObject subfolder = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER_CMISID);
		CMISObject folder = cmisObjectSelector.loadCMISObject(TESTFOLDER_CMISID);

		assertEquals(1, folder.getParents().size());
		assertEquals(8, folder.getChildren().size());
		assertEquals(1, doc.getParents().size());
		assertEquals(1, subfolder.getParents().size());
		assertEquals(0, doc.getChildren().size());
		assertEquals(0, subfolder.getChildren().size()); //subfolder has no children atm

		assertEquals(TESTFOLDER_CMISID, doc.getParents().iterator().next().getCmisObjectId()); //folder is the parent of doc
		assertEquals(TESTFOLDER_CMISID, subfolder.getParents().iterator().next().getCmisObjectId()); //folder is the parent of subfolder

		//move document from folder to subfolder
		objectService.moveObject(TEST_REPO_2, doc.getCmisObjectId(), folder.getCmisObjectId(), folder.getCmisObjectId());
	}
	
	@Test
	public void testMoveObject() throws Exception {
		//Folder has two children subfolder and document
		CMISObject doc = cmisObjectSelector.loadCMISObject(TESTDOC_CMISID);
		CMISObject subfolder = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER_CMISID);
		CMISObject folder = cmisObjectSelector.loadCMISObject(TESTFOLDER_CMISID);

		assertEquals(1, folder.getParents().size());
		assertEquals(8, folder.getChildren().size());
		assertEquals(1, doc.getParents().size());
		assertEquals(1, subfolder.getParents().size());
		assertEquals(0, doc.getChildren().size());
		assertEquals(0, subfolder.getChildren().size()); //subfolder has no children atm

		assertEquals(TESTFOLDER_CMISID, doc.getParents().iterator().next().getCmisObjectId()); //folder is the parent of doc
		assertEquals(TESTFOLDER_CMISID, subfolder.getParents().iterator().next().getCmisObjectId()); //folder is the parent of subfolder

		//move document from folder to subfolder
		objectService.moveObject(TEST_REPO_2, doc.getCmisObjectId(), folder.getCmisObjectId(), subfolder.getCmisObjectId());

		restart();

		doc = cmisObjectSelector.loadCMISObject(TESTDOC_CMISID);
		subfolder = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER_CMISID);
		folder = cmisObjectSelector.loadCMISObject(TESTFOLDER_CMISID);

		assertEquals(1, folder.getParents().size());
		assertEquals(7, folder.getChildren().size());
		assertEquals(1, subfolder.getParents().size());
		assertEquals(1, subfolder.getChildren().size());
		assertEquals(1, doc.getParents().size());
		assertEquals(0, doc.getChildren().size());
		assertEquals(TESTSUBFOLDER_CMISID, doc.getParents().iterator().next().getCmisObjectId()); //subfolder is the parent of doc now
		assertEquals(TESTDOC_CMISID, subfolder.getChildren().iterator().next().getCmisObjectId()); //testdoc is the child of subfolder
	}

	@Test(expected=CmisPermissionDeniedException.class)
	public void testMovePWC_negative() throws Exception {
		Holder<String> obejctId =  new Holder<String>("Test Document");
		Holder<Boolean> contentCopied =  new Holder<Boolean>();
		versioningService.checkOut(TestConstants.TEST_REPO_2, obejctId, contentCopied);

		CMISObject doc = cmisObjectSelector.loadCMISObject(obejctId.getValue());
		CMISObject source = cmisObjectSelector.loadCMISObject(TESTFOLDER_CMISID);
		CMISObject target = cmisObjectSelector.loadCMISObject(getRootFolderId(TestConstants.TEST_REPO_2));

		objectService.moveObject(TEST_REPO_2, doc.getCmisObjectId(), source.getCmisObjectId(), target.getCmisObjectId());
	}

	@Test
	public void testMoveObjectNotSpecificFiling() throws Exception {
		Repository repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setPwcUpdatable(Boolean.TRUE);
		repositoryService.update(repository);

		Holder<String> obejctId =  new Holder<String>("Test Document");
		Holder<Boolean> contentCopied =  new Holder<Boolean>();
		versioningService.checkOut(TestConstants.TEST_REPO_2, obejctId, contentCopied);

		CMISObject doc = cmisObjectSelector.loadCMISObject(obejctId.getValue());
		CMISObject source = cmisObjectSelector.loadCMISObject(TESTFOLDER_CMISID);
		CMISObject target = cmisObjectSelector.loadCMISObject(getRootFolderId(TestConstants.TEST_REPO_2));

		int sourceChildren = source.getChildren().size();
		int targetChildren = target.getChildren().size();

		objectService.moveObject(TEST_REPO_2, doc.getCmisObjectId(), source.getCmisObjectId(), target.getCmisObjectId());

		source = cmisObjectSelector.loadCMISObject(TESTFOLDER_CMISID);
		target = cmisObjectSelector.loadCMISObject(getRootFolderId(TestConstants.TEST_REPO_2));

		assertEquals(sourceChildren - 2, source.getChildren().size());
		assertEquals(targetChildren + 2, target.getChildren().size());
	}

	@Test
	public void testMoveObjectSpecificFiling() throws Exception {
		Repository repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setPwcUpdatable(Boolean.TRUE);
		repository.setVersionSpecificFiling(Boolean.TRUE);
		repositoryService.update(repository);

		Holder<String> obejctId =  new Holder<String>("Test Document");
		Holder<Boolean> contentCopied =  new Holder<Boolean>();
		versioningService.checkOut(TestConstants.TEST_REPO_2, obejctId, contentCopied);

		CMISObject doc = cmisObjectSelector.loadCMISObject(obejctId.getValue());
		CMISObject source = cmisObjectSelector.loadCMISObject(TESTFOLDER_CMISID);
		CMISObject target = cmisObjectSelector.loadCMISObject(getRootFolderId(TestConstants.TEST_REPO_2));

		int sourceChildren = source.getChildren().size();
		int targetChildren = target.getChildren().size();

		objectService.moveObject(TEST_REPO_2, doc.getCmisObjectId(), source.getCmisObjectId(), target.getCmisObjectId());

		source = cmisObjectSelector.loadCMISObject(TESTFOLDER_CMISID);
		target = cmisObjectSelector.loadCMISObject(getRootFolderId(TestConstants.TEST_REPO_2));

		assertEquals(sourceChildren - 1, source.getChildren().size());
		assertEquals(targetChildren + 1, target.getChildren().size());
	}

	@Transactional
	@Test (expected=CmisObjectNotFoundException.class)
	public void testDeletePolicy() {
		CMISObject policy = cmisObjectSelector.loadCMISObject("testpolicy-cmisid");
		objectService.deleteObject(TEST_REPO_2, policy.getCmisObjectId(), false);

		cmisObjectSelector.getEntityManager().flush();
		policy = cmisObjectSelector.loadCMISObject("testpolicy-cmisid"); // is deleted
	}

	@Transactional
	@Test (expected=CmisObjectNotFoundException.class)
	public void testDeleteObject() {

		CMISObject subfolder = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER_CMISID);
		objectService.deleteObject(TestConstants.TEST_REPO_2, subfolder.getCmisObjectId(), false); // only 1 version

		cmisObjectSelector.getEntityManager().flush();
		subfolder = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER_CMISID);
	}
	
	@Transactional
	@Test 
	public void testDeleteObject_ok() throws Exception{
		CMISObject document = objectService.getObject(TEST_REPO_2, "/my folder/my document");
		objectService.deleteObject(TestConstants.TEST_REPO_2, document.getCmisObjectId(), false); // only 1 version
		cmisObjectSelector.getEntityManager().flush();
	}

	@Test
	@Ignore //TODO: Indexes disabled. Remove this when fixed
	public void testDeleteObjectWithIndex() throws Exception{

		setLobScenario( DatabaseOperation.CLEAN_INSERT, "scenarioDeleteObject_withIndex.xml");

		CMISObject document = cmisObjectSelector.loadCMISObject("Test Document WITH STREAM");
		assertNotNull(document.getId());

		objectService.deleteObject(TestConstants.TEST_REPO_2, document.getCmisObjectId(), true);

		compareTable(
				"object",
				"CMIS_OBJECT_ID = 'Test Document WITH STREAM'",
				"objectService-testDelete-withIndex.xml");

		compareTable(
				"stream",
				"ID = '202'",
				"objectService-testDelete-withIndex.xml");

		compareTable(
				"index_transient",
				"object_id = '202'",
				"objectService-testDelete-withIndex.xml");

		compareTable(
				"index_word",
				"REPOSITORY_ID = '100'",
				"objectService-testDelete-withIndex.xml");

		compareTable(
				"index_word_object",
				"object_id = '202'",
				"objectService-testDelete-withIndex.xml");
		compareTable(
				"index_word_position",
				null,
				"objectService-testDelete-withIndex.xml");

	}

	@Test
	@Ignore //TODO
	public void testDeleteObjectTree() throws Exception{
		CMISObject subfolder1 = cmisObjectSelector.loadCMISObject(TESTSUBFOLDER1_CMISID);
		assertNotNull(subfolder1.getId());
		assertEquals(0, subfolder1.getAcls().size());
		assertEquals(4, subfolder1.getProperties().size());
		assertEquals(0, subfolder1.getRenditions().size());
		assertEquals(2, subfolder1.getChildren().size());
		assertEquals(1, subfolder1.getParents().size());

		objectService.deleteTree(TestConstants.TEST_REPO_2, subfolder1.getCmisObjectId(),true, null ,true);

		compareTable(
				"object_child",
				"OBJECT_ID=108",
				"objectService-test.xml");

		compareTable(
				"object_child",
				"CHILD_OBJECT_ID=108",
				"objectService-test.xml");

		compareTable(
				"Object",
				"CMIS_OBJECT_ID = 'Test Subfolder 1'",
				"objectService-test.xml");

		compareTable(
				"acl",
				"OBJECT_ID = '108'",
				"objectService-test.xml");

		compareTable(
				"rendition",
				"OBJECT_ID = '108'",
				"objectService-test.xml");

		compareTable(
				"property",
				"OBJECT_ID = '108'",
				"objectService-test.xml");

	}

	@Test (expected=CmisInvalidArgumentException.class)
	public void negativeScenarios_cannotMove_invalidParent() throws Exception{
		try {
			objectService.moveObject(TEST_REPO_2, TESTDOC_CMISID, TESTSUBFOLDER_CMISID, TESTFOLDER_CMISID);
		} catch (Exception e) {
			assertEquals(TESTSUBFOLDER_CMISID+" is not a parent of "+TESTDOC_CMISID, e.getMessage());
			throw e;
		}
	}

	@Test (expected=CmisNotSupportedException.class)
	public void negativeScenarios_cannotDelete() throws Exception{
		//Cannot delete object which has children
		CMISObject subfolder = cmisObjectSelector.loadCMISObject(TESTFOLDER_CMISID);
		objectService.deleteObject(TestConstants.TEST_REPO_2, subfolder.getCmisObjectId(), true);
	}

	@Test (expected=CmisObjectNotFoundException.class)
	public void negativeScenarios_cannotMove_invalidTarget() throws Exception{
		try {
			objectService.moveObject(TEST_REPO_2, TESTDOC_CMISID, TESTFOLDER_CMISID, "invalid");
		} catch (Exception e) {
			assertEquals("Cannot find Object with cmisId: invalid", e.getMessage());
			throw e;
		}
	}

	@Test (expected=CmisObjectNotFoundException.class)
	public void negativeScenarios_cannotMove_invalidSource() throws Exception{
		try {
			objectService.moveObject(TEST_REPO_2, TESTDOC_CMISID, "invalid", TESTSUBFOLDER_CMISID);
		} catch (Exception e) {
			assertEquals("Cannot find Object with cmisId: invalid", e.getMessage());
			throw e;
		}
	}

	@Test (expected=CmisObjectNotFoundException.class)
	public void negativeScenarios_cannotMove_invalidObject() throws Exception{
		try {
			objectService.moveObject(TEST_REPO_2, "invalid", TESTFOLDER_CMISID, TESTSUBFOLDER_CMISID);
		} catch (Exception e) {
			assertEquals("Cannot find Object with cmisId: invalid", e.getMessage());
			throw e;
		}
	}


	@Test
	@Rollback(false)
	public void testSaveContentStream() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenario04_stream.xml");

		InputStream in = new ByteArrayInputStream(TXT_FILE_CONTENT_1.getBytes());
		ContentStream cs = new ContentStreamImpl("Test Document 1 Name", BigInteger.valueOf(in.available()), "text/plain", in);
		objectService.setContentStream("test_repo_02", "Test Document 1", true, cs);

		compareLobTable(
				"stream",
				"ID = 200",
				"stream_create_1.xml");

		compareLobTable(
				"property",
				"OBJECT_ID = 200",
				"OBJECT_TYPE_PROPERTY_ID",
				"stream_create_1.xml");
	}

	@Test(expected = CmisNotSupportedException.class)
	public void testSaveContentStreamWithNoCapabilityToDoThat() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		InputStream in = new ByteArrayInputStream(TXT_FILE_CONTENT_1.getBytes());
		ContentStream cs = new ContentStreamImpl("Test Document 1 Name", BigInteger.valueOf(in.available()), "text/plain", in);

		objectService.setContentStream("test_repo_02", "Test Document 1", true, cs);
	}

	@Test
	public void testSaveContentStreamIfPreviousExist() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenario04_stream.xml");

		InputStream in = new ByteArrayInputStream(TXT_FILE_CONTENT_2.getBytes());
		ContentStream cs = new ContentStreamImpl("Test Document WITH STREAM Name", BigInteger.valueOf(in.available()), "text/plain", in);
		objectService.setContentStream("test_repo_02", "Test Document WITH STREAM", true, cs);

		compareLobTable(
				"stream",
				"ID = 202",
				"stream_create_2.xml");
	}

	@Test(expected=CmisContentAlreadyExistsException.class)
	public void testSaveContentStreamIfPreviousExistOverWriteFlagIsFalse() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenario04_stream.xml");

		InputStream in = new ByteArrayInputStream(TXT_FILE_CONTENT_2.getBytes());
		ContentStream cs = new ContentStreamImpl("Test Document WITH STREAM Name", BigInteger.valueOf(in.available()), "text/plain", in);
		objectService.setContentStream("test_repo_02", "Test Document WITH STREAM", false, cs);

	}

	@Test
	public void testSaveContentStreamWhenFileEmpty() throws Exception {
		setLobScenario( DatabaseOperation.CLEAN_INSERT, "scenario04_stream.xml");

		InputStream in = new ByteArrayInputStream(new byte[0]);
		ContentStream cs = new ContentStreamImpl("Test Document 3 Name", BigInteger.valueOf(0), "text/plain", in);
		objectService.setContentStream("test_repo_02", "Test Document 1", true, cs);

		compareLobTable(
				"stream",
				"ID = 200",
				"stream_create_3.xml");

		compareLobTable(
				"property",
				"OBJECT_ID = 200 and OBJECT_TYPE_PROPERTY_ID in (108, 120,121,122,123)",
				"OBJECT_TYPE_PROPERTY_ID",
				"stream_create_3.xml");
	}

	@Test
	public void testGetContentStream() throws Exception {
		setLobScenario( DatabaseOperation.CLEAN_INSERT, "scenario04_stream.xml");
		ContentStream cs = objectService.getContentStream("test_repo_02", "Test Document WITH STREAM");
		String result = getString(cs.getStream());
		assertEquals(TXT_FILE_CONTENT_1, result);
	}

	@Test(expected=CmisConstraintException.class)
	public void testGetContentStreamIfStreamDoesNotExist() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenario04_stream.xml");
		objectService.getContentStream("test_repo_02", "Test Document 2");
	}

	@Test
	public void testGetContentStreamWhenFileEmpty() throws Exception {
		setLobScenario( DatabaseOperation.CLEAN_INSERT, "scenario04_stream.xml");
		ContentStream cs = objectService.getContentStream("test_repo_02", "Test Document WITH EMPTY STREAM");
		String result = getString(cs.getStream());
		assertEquals(TXT_FILE_CONTENT_3, result);
	}

	@Test
	public void testDeleteContentStream() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenario04_stream.xml");
		objectService.deleteContentStream("test_repo_02", "Test Document WITH STREAM");
	}

	@Test(expected = CmisNotSupportedException.class)
	public void testDeleteContentStreamWithNoCapabilityToDoThat() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioIndex_02.xml");
		objectService.deleteContentStream("test_repo_02", "Test Document WITH STREAM");
	}

	@Test(expected = CmisNotSupportedException.class)
	public void testCreateObjectWithStreamWithNoCapabilityToDoThat() throws Exception {

		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioIndex_02.xml");

		CMISObject doc1 = new CMISObject();
		ObjectType tp4 = new ObjectType();
		tp4.setCmisId("cmis:document");
		doc1.setObjectType(tp4);
		doc1.addProperty(getTestProperty("Document 1", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc1.addProperty(getTestProperty(TEST_DOCTYPE, TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));

		InputStream in = new ByteArrayInputStream(TXT_FILE_CONTENT_1.getBytes());
		ContentStream cs = new ContentStreamImpl("Test Document 1 Name", BigInteger.valueOf(in.available()), "text/plain", in);

		objectService.createObject(TEST_REPO_2, doc1, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), cs, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);
	}

	@Test(expected = CmisConstraintException.class)
	public void testCreateObjectWithStreamWithNullMymeType() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioCreateObject.xml");

		CMISObject doc1 = new CMISObject();
		ObjectType tp4 = new ObjectType();
		tp4.setCmisId(TEST_DOCTYPE);
		doc1.setObjectType(tp4);
		doc1.addProperty(getTestProperty("Document 1", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc1.addProperty(getTestProperty(TEST_DOCTYPE, TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));

		InputStream in = new ByteArrayInputStream(TXT_FILE_CONTENT_1.getBytes());
		ContentStream cs = new ContentStreamImpl("Test Document 1 Name", BigInteger.valueOf(in.available()), null, in);

		doc1 = objectService.createObject(TEST_REPO_2, doc1, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), cs, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);
	}

	@Test(expected = CmisConstraintException.class)
	public void testCreateObjectWithStreamWithEmptyMymeType() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioCreateObject.xml");

		CMISObject doc1 = new CMISObject();
		ObjectType tp4 = new ObjectType();
		tp4.setCmisId(TEST_DOCTYPE);
		doc1.setObjectType(tp4);
		doc1.addProperty(getTestProperty("Document 1", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc1.addProperty(getTestProperty(TEST_DOCTYPE, TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));

		InputStream in = new ByteArrayInputStream(TXT_FILE_CONTENT_1.getBytes());
		ContentStream cs = new ContentStreamImpl("Test Document 1 Name", BigInteger.valueOf(in.available()), "", in);

		doc1 = objectService.createObject(TEST_REPO_2, doc1, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), cs, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);
	}

	@Test
	public void testCreateObjectWithEmptyStream() throws Exception {
		//no capability to set content stream, when content stream is null object should be created
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioIndex_02.xml");

		CMISObject doc1 = new CMISObject();
		ObjectType tp4 = new ObjectType();
		tp4.setCmisId("cmis:document");
		doc1.setObjectType(tp4);
		doc1.addProperty(getTestProperty("Document 1", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc1.addProperty(getTestProperty(TEST_DOCTYPE, TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));

		CMISObject object = objectService.createObject(TEST_REPO_2, doc1, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);
		assertNotNull(object);
	}

	@Test
	public void testCreateMultivaluedObject() throws Exception {
		CMISObject testDoc = doCreateMultivalueObject();
		checkProperty(testDoc, "test:multi", Utilities.asSet("Value1", "Value2", "Value3"));
	}

	@Test
	public void testUpdateMultivaluedObject_1() throws Exception {
		CMISObject testDoc = doCreateMultivalueObject();

		// update single property and chek multivalue property is not modified.
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, "renamed"));
		objectService.updateProperties(TEST_REPO_2, testDoc.getCmisObjectId(), properties);

		checkProperty(testDoc, PropertyIds.NAME, Collections.singleton("renamed"));
		checkProperty(testDoc, "test:multi", Utilities.asSet("Value1", "Value2", "Value3"));
	}

	@Test
	public void testUpdateMultivaluedObject_2() throws Exception {
		CMISObject testDoc = doCreateMultivalueObject();

		// update multivalue property. Delete one item.
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl("test:multi", Arrays.asList("Value1", "Value2")));
		objectService.updateProperties(TEST_REPO_2, testDoc.getCmisObjectId(), properties);

		checkProperty(testDoc, PropertyIds.NAME, Collections.singleton("test doc"));
		checkProperty(testDoc, "test:multi", Utilities.asSet("Value1", "Value2"));
	}

	@Test
	public void testUpdateMultivaluedObject_3() throws Exception {
		CMISObject testDoc = doCreateMultivalueObject();

		// update multivalue property. Add one item.
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl("test:multi", Arrays.asList("Value1", "Value2", "Value3", "Value4")));
		objectService.updateProperties(TEST_REPO_2, testDoc.getCmisObjectId(), properties);

		checkProperty(testDoc, PropertyIds.NAME, Collections.singleton("test doc"));
		checkProperty(testDoc, "test:multi", Utilities.asSet("Value1", "Value2", "Value3", "Value4"));
	}

	@Test
	public void testUpdateMultivaluedObject_4() throws Exception {
		CMISObject testDoc = doCreateMultivalueObject();

		// update multivalue property. Rename one item.
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl("test:multi", Arrays.asList("Value1", "Value2", "Value4")));
		objectService.updateProperties(TEST_REPO_2, testDoc.getCmisObjectId(), properties);

		checkProperty(testDoc, PropertyIds.NAME, Collections.singleton("test doc"));
		checkProperty(testDoc, "test:multi", Utilities.asSet("Value1", "Value2", "Value4"));
	}

	@Test
	public void testUpdateMultivaluedObject_5() throws Exception {
		CMISObject testDoc = doCreateMultivalueObject();

		// update multivalue property. Delete it.
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl("test:multi", ""));
		objectService.updateProperties(TEST_REPO_2, testDoc.getCmisObjectId(), properties);

		checkProperty(testDoc, PropertyIds.NAME, Collections.singleton("test doc"));
		checkProperty(testDoc, "test:multi", Collections.<String>emptySet());
	}

	@Test(expected=CmisConstraintException.class)
	public void testUnsetRequiredProperty() throws Exception {
		CMISObject testDoc = doCreateMultivalueObject();

		// update multivalue property. unset required property it.
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, Collections.<String>emptyList()));
		objectService.updateProperties(TEST_REPO_2, testDoc.getCmisObjectId(), properties);
	}

	private void checkProperty(CMISObject cmisObject, String propertyCmisId, Set<String> values) {
		List<Property> properties = propertySelector.getPropertiesOfTypes(cmisObject.getId(), Collections.singletonList(propertyCmisId));
		assertEquals(values.size(), properties.size());
		for (Property property : properties) {
			String value = property.getTypedValue();
			assertTrue(values.contains(value));
		}
	}

	private CMISObject doCreateMultivalueObject() throws Exception {
		setScenario("multivalued.xml", DatabaseOperation.CLEAN_INSERT);
		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TestConstants.TEST_REPO_2);
		callContextHolder.login();

		CMISObject rootFolder = cmisObjectSelector.loadCMISObject("/");

		assertEquals(0, rootFolder.getChildren().size());
		assertNotNull(rootFolder);

		CMISObject doc2 = new CMISObject();
		doc2.addParent(rootFolder);
		doc2.setObjectType(objTypeSelector.getObjectTypeByCmisId(TEST_REPO_2, TEST_DOCTYPE));
		doc2.addProperty(getTestProperty("test doc", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc2.addProperty(getTestProperty(TEST_DOCTYPE, TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));
		doc2.addProperty(getTestProperty("Value1", TEST_REPO_2, TEST_DOCTYPE, "test:multi"));
		doc2.addProperty(getTestProperty("Value2", TEST_REPO_2, TEST_DOCTYPE, "test:multi"));
		doc2.addProperty(getTestProperty("Value3", TEST_REPO_2, TEST_DOCTYPE, "test:multi"));

		objectService.createObject(TEST_REPO_2, doc2, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);
		return objectService.getObject(TEST_REPO_2, "/test doc");
	}
	
	/**
	 * create object with secondary type
	 * @throws Exception
	 */
	@Test
	public void testCreateAddSecondary() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes02.xml", DatabaseOperation.CLEAN_INSERT);
		callContextHolder.login();
		resetSequence("sq_object");
		resetSequence("sq_property");
		
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, "item10"));
		properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, "cmis:item"));
		
		properties.addProperty(new PropertyIdImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "invoice"));
		
		//create in the root
		CMISObject cmisObject = CMISObjectBuilder.build(properties, "c97ed305a3314e74eb72523bb5417fcdb140d57c");
		objectService.createObject("secondary", cmisObject, 
				Collections.EMPTY_SET, Collections.EMPTY_SET, 
				null, null, BaseTypeId.CMIS_ITEM);
		
		compareTable("object", "scenarioSecondaryTypes02-createbasic.xml");
		compareTable("object_child", "scenarioSecondaryTypes02-createbasic.xml");
		compareTable("property", "scenarioSecondaryTypes02-createbasic.xml");
		compareTable("object_secondary_type", "scenarioSecondaryTypes02-createbasic.xml");
	}
	
	/**
	 * create object with secondary type + properties
	 */
	@Test
	public void testCreateAddSecondaryWrong() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes02.xml", DatabaseOperation.CLEAN_INSERT);
		callContextHolder.login();
		resetSequence("sq_object");
		resetSequence("sq_property");
		
		try {
			PropertiesImpl properties = new PropertiesImpl();
			properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, "item10"));
			properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, "cmis:item"));
			
			properties.addProperty(new PropertyIdImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "cmis:document"));
			
			//create in the root
			CMISObject cmisObject = CMISObjectBuilder.build(properties, "c97ed305a3314e74eb72523bb5417fcdb140d57c");
			objectService.createObject("secondary", cmisObject, 
					Collections.EMPTY_SET, Collections.EMPTY_SET, 
					null, null, BaseTypeId.CMIS_ITEM);
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "You could use only cmis:secondary subtypes for the property values cmis:secondaryObjectTypeIds.");
		}
		
		try {
			PropertiesImpl properties = new PropertiesImpl();
			properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, "item10"));
			properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, "cmis:item"));
			
			properties.addProperty(new PropertyIdImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "cmis:secondary"));
			
			//create in the root
			CMISObject cmisObject = CMISObjectBuilder.build(properties, "c97ed305a3314e74eb72523bb5417fcdb140d57c");
			objectService.createObject("secondary", cmisObject, 
					Collections.EMPTY_SET, Collections.EMPTY_SET, 
					null, null, BaseTypeId.CMIS_ITEM);
			
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "You could use only cmis:secondary subtypes for the property values cmis:secondaryObjectTypeIds.");
		}

		try {
			PropertiesImpl properties = new PropertiesImpl();
			properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, "item10"));
			properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, "cmis:item"));
			
			properties.addProperty(new PropertyIdImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "xxx:yyy"));
			
			//create in the root
			CMISObject cmisObject = CMISObjectBuilder.build(properties, "c97ed305a3314e74eb72523bb5417fcdb140d57c");
			objectService.createObject("secondary", cmisObject, 
					Collections.EMPTY_SET, Collections.EMPTY_SET, 
					null, null, BaseTypeId.CMIS_ITEM);
			
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Non-existent secondary type, you could use only cmis:secondary subtypes for the property values cmis:secondaryObjectTypeIds.");
		}

	}
	
	
	/**
	 * create object, wrong secondary type (cmis:document, cmis:secondary and unexisting type)
	 */
	@Test
	public void testCreateAddSecondaryWithProperties() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes02.xml", DatabaseOperation.CLEAN_INSERT);
		callContextHolder.login();
		resetSequence("sq_object");
		resetSequence("sq_property");
		
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, "item10"));
		properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, "cmis:item"));
		
		properties.addProperty(new PropertyIdImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "tag"));
		properties.addProperty(new PropertyStringImpl("name", Arrays.asList(new String[] { "book", "tree"})));
		
		//create in the root
		CMISObject cmisObject = CMISObjectBuilder.build(properties, "c97ed305a3314e74eb72523bb5417fcdb140d57c");
		objectService.createObject("secondary", cmisObject, 
				Collections.EMPTY_SET, Collections.EMPTY_SET, 
				null, null, BaseTypeId.CMIS_ITEM);
		
		compareTable("object", "scenarioSecondaryTypes02-create.xml");
		compareTable("object_child", "scenarioSecondaryTypes02-create.xml");
		compareTable("property", "scenarioSecondaryTypes02-create.xml");
		compareTable("object_secondary_type", "scenarioSecondaryTypes02-create.xml");
	}	
	
	/**
	 * update properties, add secondary type + properties
	 * @throws Exception
	 */
	@Test
	public void testUpdatePropertiesAddSecondary() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes02.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_property");
		
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "invoice"));
		properties.addProperty(new PropertyStringImpl("amount", "123"));
		objectService.updateProperties("secondary", "f70e0a543d51a86f61d9303de4f46205a53bff74", properties);
		
		compareTable("property", "scenarioSecondaryTypes02-add.xml");
		compareTable("object_secondary_type", "scenarioSecondaryTypes02-add.xml");
	}

	/**
	 * update properties, add secondary type
	 * @throws Exception
	 */
	@Test
	public void testUpdatePropertiesAddSecondaryTypeSimple() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes02.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_property");
		
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "tag"));
		objectService.updateProperties("secondary", "f70e0a543d51a86f61d9303de4f46205a53bff74", properties);
		
		compareTable("property", "scenarioSecondaryTypes02-addsimple.xml");
		compareTable("object_secondary_type", "scenarioSecondaryTypes02-addsimple.xml");
	}

	
	/**
	 * update properties, add secondary type property
	 * 
	 * Object with existing secondary type asigned, add a property of the secondary type
	 * @throws Exception
	 */
	@Test
	public void testUpdatePropertiesAddSecondaryTypeProperty() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes02.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_property");
		
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl("trade:commonProperty", "Value123"));
		objectService.updateProperties("secondary", "4c2f05dd9853f57c6c44c4c117d6381199767ebe", properties);
		
		compareTable("property", "scenarioSecondaryTypes02-addproperty.xml");
		compareTable("object_secondary_type", "scenarioSecondaryTypes02-addproperty.xml");
	}
	
	/**
	 * update properties, remove secondary type
	 */
	@Test
	public void testUpdatePropertiesRemoveSecondaryType() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes02.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_property");
		
		/*
		 * f70e0a543d51a86f61d9303de4f46205a53bff74 item1
		 * 7788aaa6b457bd1abe1951b13b15a1b6cf51ba50 item2 tag (name: {tree, book, house}
		 * f3f258ea8556127a33db258725b593c0a22a9a5b item3 tag (name: {blue} ) invoice (amount: 124, payed: true)
		 * 4c2f05dd9853f57c6c44c4c117d6381199767ebe item4 trade:secondary 
		 */

		//clean item2 and item4
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, Collections.EMPTY_LIST));
		objectService.updateProperties("secondary", "7788aaa6b457bd1abe1951b13b15a1b6cf51ba50", properties);
		objectService.updateProperties("secondary", "4c2f05dd9853f57c6c44c4c117d6381199767ebe", properties);
		
		//remove one secondary from item3
		properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, Arrays.asList(new String[] { "tag" })));
		objectService.updateProperties("secondary", "f3f258ea8556127a33db258725b593c0a22a9a5b", properties);
		
		compareTable("property", "scenarioSecondaryTypes02-removeproperty.xml");
		compareTable("object_secondary_type", "scenarioSecondaryTypes02-removeproperty.xml");
	}
	
	/**
	 * update properties, remove one secondary type, keep other secondary type with the same common parent property
	 * 
	 * item5 has two secondary types trade:secondary1 and trade:secondary2
	 * 
	 * the types are defined in the following way:
	 * 
	 * 1 cmis:secondary
	 *  1.1 trade:secondary (trade:commonProperty)
	 *   1.1.1 trade:secondary1 (trade:secondary1)
	 *   1.1.2 trade:secondary2 (trade:secondary2)
	 */
	@Test
	public void testUpdatePropertiesRemoveSecondaryTypeWithOtherPath() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes02.xml", DatabaseOperation.CLEAN_INSERT);
		setScenario("scenarioSecondaryTypes02-item5.xml", DatabaseOperation.INSERT);
		resetSequence("sq_property");
		
		//remove trade:secondary1 of item5 
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, Arrays.asList(new String[] {"trade:secondary2"})));
		objectService.updateProperties("secondary", "2074a56c74f222866f3e3dfe744fd70b74605a96", properties);
		
		compareTable("property", "scenarioSecondaryTypes02-path.xml");
		compareTable("object_secondary_type", "scenarioSecondaryTypes02-path.xml");
	}
	
	/**
	 * update properties, wrong secondary type (cmis:document, cmis:secondary and unexisting type)
	 */
	@Test
	public void testUpdatePropertiesSecondaryFails() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes02.xml", DatabaseOperation.CLEAN_INSERT);
		setScenario("scenarioSecondaryTypes02-item5.xml", DatabaseOperation.INSERT);
		resetSequence("sq_property");

		try {
			PropertiesImpl properties = new PropertiesImpl();
			properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, Arrays.asList(new String[] {"cmis:document"})));
			objectService.updateProperties("secondary", "2074a56c74f222866f3e3dfe744fd70b74605a96", properties);
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "You could use only cmis:secondary subtypes for the property values cmis:secondaryObjectTypeIds.");
		}
		
		try {
			PropertiesImpl properties = new PropertiesImpl();
			properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, Arrays.asList(new String[] {"cmis:secondary"})));
			objectService.updateProperties("secondary", "2074a56c74f222866f3e3dfe744fd70b74605a96", properties);
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "You could use only cmis:secondary subtypes for the property values cmis:secondaryObjectTypeIds.");
		}

		try {
			PropertiesImpl properties = new PropertiesImpl();
			properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, Arrays.asList(new String[] {"xxx:yyy"})));
			objectService.updateProperties("secondary", "2074a56c74f222866f3e3dfe744fd70b74605a96", properties);
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Non-existent secondary type, you could use only cmis:secondary subtypes for the property values cmis:secondaryObjectTypeIds.");
		}
	}
	
	
	
	/**
	 * checkin with properties, add secondary type
	 */
	@Test
	public void testUpdatePropertiesSecondaryCheckin() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes03.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_property");
		resetSequence("sq_object");
		
		Holder<String> id = new Holder<String>("bf2166ccac9bb2813cb0736d07024fa756b07bf4");
		versioningService.checkOut("secondary",	id,	new Holder<Boolean>(true));
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "invoice"));
		properties.addProperty(new PropertyStringImpl("amount", "123"));
		properties.addProperty(new PropertyStringImpl("comments", "To be payed before 01/02/03"));
		properties.addProperty(new PropertyStringImpl("company", "Megacorp"));
		
		versioningService.checkIn("secondary", id, true, properties, null, "checking comment", 
				new ArrayList<String>(), new HashSet<Acl>(), new HashSet<Acl>());
		
		compareTable("object", "scenarioSecondaryTypes03-checkin.xml");
		compareTable("property", "scenarioSecondaryTypes03-checkin.xml");
		compareTable("object_secondary_type", "scenarioSecondaryTypes03-checkin.xml");
		compareTable("object_child", "scenarioSecondaryTypes03-checkin.xml");
		
	}
	
	
	/**
	 * checkin with properties, remove secondary type
	 */
	@Test
	public void testUpdatePropertiesSecondaryCheckinWithPropsRemove() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes03.xml", DatabaseOperation.CLEAN_INSERT);
		setScenario("scenarioSecondaryTypes03-document2.xml", DatabaseOperation.INSERT);
		resetSequence("sq_property");
		resetSequence("sq_object");

		Holder<String> id = new Holder<String>("002166ccac9bb2813cb0736d07024fa756b07bf4");
		versioningService.checkOut("secondary",	id,	new Holder<Boolean>(true));
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, Collections.EMPTY_LIST));
		
		versioningService.checkIn("secondary", id, true, properties, null, "checking comment", 
				new ArrayList<String>(), new HashSet<Acl>(), new HashSet<Acl>());
		
		compareTable("object", "scenarioSecondaryTypes03-checkin-remove.xml");
		compareTable("property", "scenarioSecondaryTypes03-checkin-remove.xml");
		compareTable("object_secondary_type", "scenarioSecondaryTypes03-checkin-remove.xml");
		compareTable("object_child", "scenarioSecondaryTypes03-checkin-remove.xml");
	}
	
	/**
	 * checkin with properties, wrong secondary type (cmis:document, cmis:secondary and unexisting type)
	 */
	@Test
	public void testUpdatePropertiesSecondaryCheckinWrong() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes03.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_property");
		
		Holder<String> id = new Holder<String>("bf2166ccac9bb2813cb0736d07024fa756b07bf4");
		versioningService.checkOut("secondary",	id,	new Holder<Boolean>(true));

		try {
			PropertiesImpl properties = new PropertiesImpl();
			properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, Arrays.asList(new String[] {"cmis:document"})));
			
			versioningService.checkIn("secondary", id, true, properties, null, "checking comment", 
					new ArrayList<String>(), new HashSet<Acl>(), new HashSet<Acl>());
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "You could use only cmis:secondary subtypes for the property values cmis:secondaryObjectTypeIds.");
		}
		
		try {
			PropertiesImpl properties = new PropertiesImpl();
			properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, Arrays.asList(new String[] {"cmis:secondary"})));
			
			versioningService.checkIn("secondary", id, true, properties, null, "checking comment", 
					new ArrayList<String>(), new HashSet<Acl>(), new HashSet<Acl>());
			
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "You could use only cmis:secondary subtypes for the property values cmis:secondaryObjectTypeIds.");
		}

		try {
			PropertiesImpl properties = new PropertiesImpl();
			properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, Arrays.asList(new String[] {"xxx:yyy"})));
			
			versioningService.checkIn("secondary", id, true, properties, null, "checking comment", 
					new ArrayList<String>(), new HashSet<Acl>(), new HashSet<Acl>());
			
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Non-existent secondary type, you could use only cmis:secondary subtypes for the property values cmis:secondaryObjectTypeIds.");
		}
	}
	
	/**
	 * Add a secondary type with missing required properties 
	 */
	@Test
	public void testUpdatePropertiesSecondaryWithRequired() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes03.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_property");
		
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "trade:secondaryWithRequired"));
		try {
			//item1
			objectService.updateProperties("secondary", "f70e0a543d51a86f61d9303de4f46205a53bff74", properties);
			Assert.fail("Secondary type with missing required properties");
		} catch (CmisInvalidArgumentException e) {
			Assert.assertEquals("Cannot update object, missing required properties of type trade:secondaryWithRequired.", e.getMessage());
		}
		
	}
	
	
	/**
	 * Create a document with a secondary type with missing required properties 
	 */
	@Test
	public void testCreateItemSecondaryWithRequired() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes03.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_property");
		resetSequence("sq_object");
		
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "trade:secondaryWithRequired"));
		properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, "item10"));
		properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, "cmis:item"));
		
		CMISObject cmisObject = CMISObjectBuilder.build(properties, "c97ed305a3314e74eb72523bb5417fcdb140d57c");
		try {
			objectService.createObject("secondary", cmisObject, 
				Collections.EMPTY_SET, Collections.EMPTY_SET, 
				null, null, BaseTypeId.CMIS_ITEM);
			Assert.fail("Secondary type with missing required properties");
		} catch (CmisInvalidArgumentException e) {
			Assert.assertEquals("Cannot create object, not all required properties supplied.", e.getMessage());
		}
	}
	
	
	/**
	 * checkin with properties, add secondary type with required properties 
	 */
	@Test
	public void testUpdatePropertiesSecondaryCheckinWithRequired() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes03.xml", DatabaseOperation.CLEAN_INSERT);
		setScenario("scenarioSecondaryTypes03-document2.xml", DatabaseOperation.INSERT);
		resetSequence("sq_property");
		resetSequence("sq_object");

		Holder<String> id = new Holder<String>("002166ccac9bb2813cb0736d07024fa756b07bf4");
		versioningService.checkOut("secondary",	id,	new Holder<Boolean>(true));
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "trade:secondaryWithRequired"));
		
		try {
			
			versioningService.checkIn("secondary", id, true, properties, null, "checking comment", 
				new ArrayList<String>(), new HashSet<Acl>(), new HashSet<Acl>());
			Assert.fail("Secondary type with missing required properties");
		} catch (CmisInvalidArgumentException e) {
			Assert.assertEquals("Cannot update object, missing required properties of type trade:secondaryWithRequired.", e.getMessage());
		}
		
		
	}
	
	/**
	 * Updated properties removing a required property of a secondary type
	 */
	@Test
	public void testUpdatePropertiesSecondaryWithRequired02() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes03.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_property");
		
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "trade:secondaryWithRequired"));
		properties.addProperty(new PropertyStringImpl("trade:required1", "A"));
		properties.addProperty(new PropertyStringImpl("trade:required2", "B"));
		
		//item1
		objectService.updateProperties("secondary", "f70e0a543d51a86f61d9303de4f46205a53bff74", properties);

		properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl("trade:required2", Collections.EMPTY_LIST));
		try {
			//should fail
			objectService.updateProperties("secondary", "f70e0a543d51a86f61d9303de4f46205a53bff74", properties);
			Assert.fail("Secondary type with missing required properties");
		} catch (CmisConstraintException e) {
			Assert.assertEquals("Cannot unset the required property trade:required2 of object f70e0a543d51a86f61d9303de4f46205a53bff74", e.getMessage());
		}
	}		


	/**
	 * Add a secondary type with required properties and then add 
	 * a new property not required.
	 * 
	 * This tests validates that the secondary required properties are 
	 * not checked every update.
	 */
	@Test
	public void testUpdatePropertiesSecondaryWithRequired03() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes03.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_property");
		
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "trade:secondaryWithRequired"));
		properties.addProperty(new PropertyStringImpl("trade:required1", "A"));
		properties.addProperty(new PropertyStringImpl("trade:required2", "B"));
		
		//item1
		objectService.updateProperties("secondary", "f70e0a543d51a86f61d9303de4f46205a53bff74", properties);

		properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.DESCRIPTION, "description added"));
		objectService.updateProperties("secondary", "f70e0a543d51a86f61d9303de4f46205a53bff74", properties);
	}		

	
	/**
	 * Add a secondary type with required properties and then replace secondary type.
	 * 
	 * trade:secondaryWithRequiredSubA and trade:secondaryWithRequiredSubB
	 * extends trade:secondaryWithRequired with several required properties.
	 * 
	 * Update initially with SubA, and then replaced to SubB
	 */
	@Test
	public void testUpdatePropertiesSecondaryWithRequired04() throws Exception {
		setUser(TEST_USER, TEST_PWD, "secondary");
		setScenario("scenarioSecondaryTypes03.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_property");
		
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "trade:secondaryWithRequiredSubA"));
		properties.addProperty(new PropertyStringImpl("trade:required1", "A"));
		properties.addProperty(new PropertyStringImpl("trade:required2", "B"));
		
		//item1
		objectService.updateProperties("secondary", "f70e0a543d51a86f61d9303de4f46205a53bff74", properties);

		properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "trade:secondaryWithRequiredSubB"));
		objectService.updateProperties("secondary", "f70e0a543d51a86f61d9303de4f46205a53bff74", properties);
	}
}
