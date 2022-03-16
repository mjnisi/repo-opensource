package eu.trade.repo.service.cmis;

import static eu.trade.repo.TestConstants.TEST_REPO_2;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.id.IDGenerator;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.service.cmis.data.out.PropertiesBuilder;
import eu.trade.repo.util.Constants;
import eu.trade.repo.util.ICurrentDate;
public class CmisObjectServiceTest extends BaseTestClass {
	@Autowired
	private IDGenerator mockGenerator;
	
	@Autowired
	private ICurrentDate mockCurrentDate;

	@Before
	public void setup() throws Exception{
		setScenario("scenarioCreateObject.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object");
		resetSequence("sq_property");
		
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TEST_REPO_2);
		mockGenerator.reset();
		mockCurrentDate.reset();
	}

	@Test (expected=CmisInvalidArgumentException.class)
	public void negativeScenarios_cannotCreate_noName() throws Exception {
		CMISObject doc1 = new CMISObject();

		ObjectType tp4 = new ObjectType();
		tp4.setCmisId("test:document");
		doc1.setObjectType(tp4);

		doc1.addProperty(getTestProperty("test:document", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));

		Properties props = PropertiesBuilder.build(doc1, "");
		cmisObjectService.createDocument(TEST_REPO_2, props, "/", null, null, null, null, null, null);
	}

	@Test (expected=CmisInvalidArgumentException.class)
	public void negativeScenarios_cannotCreate_wrongObjecttype() throws Exception {
		CMISObject doc1 = new CMISObject();

		ObjectType tp4 = new ObjectType();
		tp4.setCmisId("wrong:type");
		doc1.setObjectType(tp4);

		doc1.addProperty(getTestProperty("My Document", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		doc1.addProperty(getTestProperty("wrong:type", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));

		Properties props = PropertiesBuilder.build(doc1, "");
		cmisObjectService.createDocument(TEST_REPO_2, props, "/", null, null, null, null, null, null);
	}

	@Test
	public void testCreateDocument() throws Exception {
		CMISObject doc1 = new CMISObject();

		ObjectType tp4 = new ObjectType();
		tp4.setCmisId("test:document");
		doc1.setObjectType(tp4);

		doc1.addProperty(getTestProperty("My Document", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		doc1.addProperty(getTestProperty("test:document", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));

		Properties props = PropertiesBuilder.build(doc1, "");
		cmisObjectService.createDocument(TEST_REPO_2, props, "/", null, null, null, null, null, null);

		compareTable(
				"object",
				"1 = 1",
				"cmisobjectService-testCreate.xml");

		compareTable(
				"object_child",
				"1 = 1",
				"cmisobjectService-testCreate3.xml");
	}


	@Test
	public void testCreateDocumentWithoutType() throws Exception {
		PropertiesImpl props = new PropertiesImpl();
		props.addProperty(new PropertyStringImpl(PropertyIds.NAME, "mydoc.txt"));
		
		cmisObjectService.createDocument(TEST_REPO_2, props, "/", null, null, null, null, null, null);
		
		compareTable("object", "cmisobjectService-testCreateDoc.xml");
		compareTable("object_child", "cmisobjectService-testCreateDoc.xml");
		compareTable("property", "cmisobjectService-testCreateDoc.xml");
	}

	@Test
	public void testCreateDocumentWithType() throws Exception {
		PropertiesImpl props = new PropertiesImpl();
		props.addProperty(new PropertyStringImpl(PropertyIds.NAME, "mydoc.txt"));
		props.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, "cmis:document"));
		
		cmisObjectService.createDocument(TEST_REPO_2, props, "/", null, null, null, null, null, null);
		
		compareTable("object", "cmisobjectService-testCreateDoc2.xml");
		compareTable("object_child", "cmisobjectService-testCreateDoc2.xml");
		compareTable("property", "cmisobjectService-testCreateDoc2.xml");
	}
	
	@Test
	public void testCreateFolderWithoutType() throws Exception {
		PropertiesImpl props = new PropertiesImpl();
		props.addProperty(new PropertyStringImpl(PropertyIds.NAME, "myfolder"));
		
		cmisObjectService.createFolder(TEST_REPO_2, props, "/", null, null, null, null);
		
		compareTable("object", "cmisobjectService-testCreateFolder.xml");
		compareTable("object_child", "cmisobjectService-testCreateFolder.xml");
		compareTable("property", "cmisobjectService-testCreateFolder.xml");
	}
	

	@Test
	public void testCreateFolderWithType() throws Exception {
		PropertiesImpl props = new PropertiesImpl();
		props.addProperty(new PropertyStringImpl(PropertyIds.NAME, "myfolder"));
		props.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, "cmis:folder"));
		
		cmisObjectService.createFolder(TEST_REPO_2, props, "/", null, null, null, null);
		
		compareTable("object", "cmisobjectService-testCreateFolder2.xml");
		compareTable("object_child", "cmisobjectService-testCreateFolder2.xml");
		compareTable("property", "cmisobjectService-testCreateFolder2.xml");
	}


	@Test
	public void testCreateRelationshipWithoutType() throws Exception {
		PropertiesImpl props = new PropertiesImpl();
		props.addProperty(new PropertyStringImpl(PropertyIds.NAME, "myrelationship"));
		props.addProperty(new PropertyIdImpl(PropertyIds.SOURCE_ID, "/"));
		props.addProperty(new PropertyIdImpl(PropertyIds.TARGET_ID, "/"));
		
		cmisObjectService.createRelationship(TEST_REPO_2, props, null, null, null, null);
		
		compareTable("object", "cmisobjectService-testCreateRelationship.xml");
		compareTable("object_child", "cmisobjectService-testCreateRelationship.xml");
		compareTable("property", "cmisobjectService-testCreateRelationship.xml");
	}
	
	@Test
	public void testCreateRelationshipWithType() throws Exception {
		PropertiesImpl props = new PropertiesImpl();
		props.addProperty(new PropertyStringImpl(PropertyIds.NAME, "myrelationship"));
		props.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, "cmis:relationship"));
		props.addProperty(new PropertyIdImpl(PropertyIds.SOURCE_ID, "/"));
		props.addProperty(new PropertyIdImpl(PropertyIds.TARGET_ID, "/"));
		
		cmisObjectService.createRelationship(TEST_REPO_2, props, null, null, null, null);
		
		compareTable("object", "cmisobjectService-testCreateRelationship2.xml");
		compareTable("object_child", "cmisobjectService-testCreateRelationship2.xml");
		compareTable("property", "cmisobjectService-testCreateRelationship2.xml");
	}

	@Test
	public void testCreatePolicyWithoutType() throws Exception {
		PropertiesImpl props = new PropertiesImpl();
		props.addProperty(new PropertyStringImpl(PropertyIds.NAME, "mypolicy"));
		
		cmisObjectService.createPolicy(TEST_REPO_2, props, "/", null, null, null, null);
		
		compareTable("object", "cmisobjectService-testCreatePolicy.xml");
		compareTable("object_child", "cmisobjectService-testCreatePolicy.xml");
		compareTable("property", "cmisobjectService-testCreatePolicy.xml");
	}

	@Test
	public void testCreatePolicyWithType() throws Exception {
		PropertiesImpl props = new PropertiesImpl();
		props.addProperty(new PropertyStringImpl(PropertyIds.NAME, "mypolicy"));
		props.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, "cmis:policy"));
		
		cmisObjectService.createPolicy(TEST_REPO_2, props, "/", null, null, null, null);
		
		compareTable("object", "cmisobjectService-testCreatePolicy2.xml");
		compareTable("object_child", "cmisobjectService-testCreatePolicy2.xml");
		compareTable("property", "cmisobjectService-testCreatePolicy2.xml");
	}
	
	@Test
	public void testCreateItemWithoutType() throws Exception {
		PropertiesImpl props = new PropertiesImpl();
		props.addProperty(new PropertyStringImpl(PropertyIds.NAME, "myitem"));
		
		cmisObjectService.createItem(TEST_REPO_2, props, "/", null, null, null, null);
		
		compareTable("object", "cmisobjectService-testCreateItem.xml");
		compareTable("object_child", "cmisobjectService-testCreateItem.xml");
		compareTable("property", "cmisobjectService-testCreateItem.xml");
	}

	@Test
	public void testCreateItemWithType() throws Exception {
		PropertiesImpl props = new PropertiesImpl();
		props.addProperty(new PropertyStringImpl(PropertyIds.NAME, "myitem"));
		props.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, "cmis:item"));
		
		cmisObjectService.createItem(TEST_REPO_2, props, "/", null, null, null, null);
		
		compareTable("object", "cmisobjectService-testCreateItem2.xml");
		compareTable("object_child", "cmisobjectService-testCreateItem2.xml");
		compareTable("property", "cmisobjectService-testCreateItem2.xml");
	}

	
	@Test
	public void testCreateUnfilledDocument() throws Exception {
		mockGenerator.reset();
		CMISObject doc1 = new CMISObject();

		ObjectType tp4 = new ObjectType();
		tp4.setCmisId("cmis:document");
		doc1.setObjectType(tp4);

		doc1.addProperty(getTestProperty("My Document", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		doc1.addProperty(getTestProperty("test:document", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));

		Properties props = PropertiesBuilder.build(doc1, "");
		cmisObjectService.createDocument(TEST_REPO_2, props, null, null, null, null, null, null, null);

		compareTable(
				"object",
				"1 = 1",
				"cmisobjectService-testCreate.xml");

		compareTable(
				"object_child",
				"1 = 1",
				"cmisobjectService-testCreate.xml");

	}

	@Test
	public void testCreateFolder() throws Exception {
		CMISObject doc1 = new CMISObject();
		ObjectType tp4 = new ObjectType();
		tp4.setCmisId("test:folder");
		doc1.setObjectType(tp4);

		doc1.addProperty(getTestProperty("My Folder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		doc1.addProperty(getTestProperty("test:folder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));

		Properties props = PropertiesBuilder.build(doc1, "");
		cmisObjectService.createFolder(TEST_REPO_2, props, "/", null, null, null, null);

		compareTable(
				"object",
				"1 = 1",
				"cmisobjectService-testCreate2.xml");

		compareTable(
				"object_child",
				"OBJECT_ID = 99",
				"cmisobjectService-testCreate2.xml");

	}
	
	@Test
	public void testCreateWrongFolder() throws Exception {
		CMISObject doc1 = new CMISObject();
		ObjectType tp4 = new ObjectType();
		tp4.setCmisId("test:folder");
		doc1.setObjectType(tp4);

		doc1.addProperty(getTestProperty("/My/Folder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		doc1.addProperty(getTestProperty("test:folder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));

		Properties props = PropertiesBuilder.build(doc1, "");
		try {
			cmisObjectService.createFolder(TEST_REPO_2, props, "/", null, null, null, null);
			Assert.fail();
		} catch (Exception CmisInvalidArgumentException) {
			//no-op
		}


	}
	
	@Test
	public void testAclInheritance() throws Exception {
		
		setScenario("scenarioRelationships.xml", DatabaseOperation.CLEAN_INSERT);
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TEST_REPO_2);
		mockGenerator.reset();
		
		CMISObject folderA = new CMISObject(new ObjectType("cmis:folder"));
		folderA.addProperty(getTestProperty("FolderA", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		folderA.addProperty(getTestProperty("cmis:folder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));
		
		String folderAId = cmisObjectService.createFolder(TEST_REPO_2, PropertiesBuilder.build(folderA), "6bf5f28c10a4cad545a52b9f7160fc59d731fbe5", null, createAcl(true, Arrays.asList(TestConstants.TEST_USER), "cmis:read"), null, null);
		
		CMISObject folderB = new CMISObject(new ObjectType("cmis:folder"));
		folderB.addProperty(getTestProperty("FolderB", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		folderB.addProperty(getTestProperty("cmis:folder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));
		
		String folderBId = cmisObjectService.createFolder(TEST_REPO_2, PropertiesBuilder.build(folderB), folderAId, null, createAcl(true, Arrays.asList(TestConstants.TEST2_USER), "cmis:read"), null, null);
		
		//folderC
		CMISObject folderC = new CMISObject(new ObjectType("cmis:folder"));
		folderC.addProperty(getTestProperty("FolderBC", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		folderC.addProperty(getTestProperty("cmis:folder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));
		
		String folderCId = cmisObjectService.createFolder(TEST_REPO_2, PropertiesBuilder.build(folderC), folderBId, null, createAcl(true, Arrays.asList(TestConstants.TEST_USER, TestConstants.TEST2_USER), "cmis:read"), null, null);
		
		
		CMISObject document = new CMISObject(new ObjectType("cmis:document"));

		document.addProperty(getTestProperty("document", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		document.addProperty(getTestProperty("cmis:document", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));
		
		String documentId = cmisObjectService.createDocument(TEST_REPO_2, PropertiesBuilder.build(document), folderCId, null, null, null, null, null, null);
		
		//remove acl
		cmisAclService.applyAcl(TEST_REPO_2, documentId, null, createAcl(false, Arrays.asList(TestConstants.TEST2_USER), "cmis:read"), AclPropagation.REPOSITORYDETERMINED, null);
		
		//apply acl to FolderA
		cmisAclService.applyAcl(TEST_REPO_2, folderAId, createAcl(true, Arrays.asList(TestConstants.TEST2_USER), "cmis:read"), null, AclPropagation.REPOSITORYDETERMINED, null);
		
		//verify acl propagation
		ObjectData obj = cmisObjectService.getObject(TEST_REPO_2, documentId, null, false, IncludeRelationships.NONE, null, false, true, null);
		
		boolean foundAce = false;
		
		List<Ace> aces = obj.getAcl().getAces();
		
		for(Ace ace : aces) {
			String principalId = ace.getPrincipal().getId();
			if("test2".equals(principalId) && ace.getPermissions().contains("cmis:read")) {
				foundAce = true;
				break;
			}
		}
		
		assertTrue(foundAce);
	}
	
	@Test
	public void testValidateBaseTypeDocument() throws Exception { //FOLDER, POLICY, RELATIONSHIP, ITEM?
		
		setScenario("scenarioRelationships.xml", DatabaseOperation.CLEAN_INSERT);
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TEST_REPO_2);
		mockGenerator.reset();
		
		//CREATE DOCUMENT WITH SUPERTYPE
		CMISObject doc = new CMISObject(new ObjectType("cmis:folder"));

		doc.addProperty(getTestProperty("From document", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		doc.addProperty(getTestProperty("cmis:folder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));
		
		boolean caughtExpectedException = false;
		
		try {
			cmisObjectService.createDocument(TEST_REPO_2, PropertiesBuilder.build(doc), 
					"6bf5f28c10a4cad545a52b9f7160fc59d731fbe5", null, null, null, createAcl(true, Arrays.asList(TestConstants.TEST_USER, TestConstants.TEST2_USER)), null, null);
		} catch(CmisConstraintException e) {
			
			//TODO: we should define error codes, this is too fragile
			if("Cannot create cmis:document of type: cmis:folder".equals(e.getMessage())) {
				caughtExpectedException = true;
			}
		} finally {
			assertTrue("Managed to create cmis:document of base type cmis:folder!", caughtExpectedException);
		}
	}
	
	@Test
	public void testValidateBaseTypeFolder() throws Exception {
		
		setScenario("scenarioRelationships.xml", DatabaseOperation.CLEAN_INSERT);
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TEST_REPO_2);
		mockGenerator.reset();
		
		//CREATE DOCUMENT WITH SUPERTYPE
		CMISObject doc = new CMISObject(new ObjectType("cmis:folder"));

		doc.addProperty(getTestProperty("From document", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		doc.addProperty(getTestProperty("cmis:document", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));
		
		boolean caughtExpectedException = false;
		
		try {
			cmisObjectService.createFolder(TEST_REPO_2, PropertiesBuilder.build(doc), 
					"6bf5f28c10a4cad545a52b9f7160fc59d731fbe5", null, createAcl(true, Arrays.asList(TestConstants.TEST_USER, TestConstants.TEST2_USER)), null, null);
		} catch(CmisConstraintException e) {
			
			//TODO: we should define error codes, this is too fragile
			if("Cannot create cmis:folder of type: cmis:document".equals(e.getMessage())) {
				caughtExpectedException = true;
			}
		} finally {
			assertTrue("Managed to create cmis:document of base type cmis:folder!", caughtExpectedException);
		}
	}
	
	@Test
	public void testRelationships() throws Exception {
		
		//TODO: test ACLs on relationship & objects
		
		setScenario("scenarioRelationships.xml", DatabaseOperation.CLEAN_INSERT);
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TEST_REPO_2);
		mockGenerator.reset();
		
		//create 2 documents
		CMISObject doc = new CMISObject(new ObjectType("cmis:document"));

		doc.addProperty(getTestProperty("From document", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc.addProperty(getTestProperty("cmis:document", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));
		
		String doc1Id = cmisObjectService.createDocument(TEST_REPO_2, PropertiesBuilder.build(doc), "6bf5f28c10a4cad545a52b9f7160fc59d731fbe5", null, null, null, createAcl(true, Arrays.asList(TestConstants.TEST_USER, TestConstants.TEST2_USER)), null, null);
		
		doc = new CMISObject(new ObjectType("nest:document"));

		doc.addProperty(getTestProperty("To document", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc.addProperty(getTestProperty("nest:document", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));
		
		String doc2Id = cmisObjectService.createDocument(TEST_REPO_2, PropertiesBuilder.build(doc), "6bf5f28c10a4cad545a52b9f7160fc59d731fbe5", null, null, null, createAcl(true, Arrays.asList(TestConstants.TEST_USER, TestConstants.TEST2_USER)), null, null);
		
		//create relationship between docs
		String relId = createRelationship(doc1Id, doc2Id,"tdr:pdf_attachment", "Document with PDF attachment", true, "cmis:anyone");
		
		//retrieve relationships with query service
		ObjectList results = cmisDiscoveryService.query(TEST_REPO_2, "select * from cmis:relationship where cmis:sourceId='" + doc1Id + "'", true, false, IncludeRelationships.NONE, null, BigInteger.TEN, BigInteger.ZERO, null);
		
		//retrieve relationships with Relationship Services
		ObjectList relationships = cmisRelationshipService.getObjectRelationships(TEST_REPO_2, doc1Id, false, RelationshipDirection.EITHER, null, null, true, BigInteger.TEN, BigInteger.ZERO, null);
		
		assertTrue(relationships != null);
		assertTrue(relationships.getObjects() != null);
		assertTrue(relationships.getObjects().size() == 1);
		assertTrue(relationships.getObjects().get(0) != null);
		
		assertTrue(relationships.getObjects().get(0).getId().equals(relId));
		
		PropertyData<?> pd = relationships.getObjects().get(0).getProperties().getProperties().get(PropertyIds.SOURCE_ID);
		assertNotNull(pd);
		assertTrue(pd.getFirstValue().equals(doc1Id));
		
		pd = relationships.getObjects().get(0).getProperties().getProperties().get(PropertyIds.TARGET_ID);
		assertNotNull(pd);
		assertTrue(pd.getFirstValue().equals(doc2Id));
		
		//as a source
		relationships = cmisRelationshipService.getObjectRelationships(TEST_REPO_2, doc1Id, false, RelationshipDirection.SOURCE, null, null, true, BigInteger.TEN, BigInteger.ZERO, null);
		
		assertTrue(relationships != null);
		assertTrue(relationships.getObjects() != null);
		assertTrue(relationships.getObjects().size() == 1);
		assertTrue(relationships.getObjects().get(0) != null);
		
		assertTrue(relationships.getObjects().get(0).getId().equals(relId));
		
		pd = relationships.getObjects().get(0).getProperties().getProperties().get(PropertyIds.SOURCE_ID);
		assertNotNull(pd);
		assertTrue(pd.getFirstValue().equals(doc1Id));
		
		pd = relationships.getObjects().get(0).getProperties().getProperties().get(PropertyIds.TARGET_ID);
		assertNotNull(pd);
		assertTrue(pd.getFirstValue().equals(doc2Id));
		
		relationships = cmisRelationshipService.getObjectRelationships(TEST_REPO_2, doc1Id, false, RelationshipDirection.TARGET, null, null, true, BigInteger.TEN, BigInteger.ZERO, null);
		assertTrue(relationships != null);
		assertTrue(relationships.getObjects() != null);
		assertTrue(relationships.getObjects().size() == 0);
		
		//TODO inexistent object
		//TODO object without relationships
		
		//TODO
		//doc2 -> either, source, target
		
		boolean caughtExpectedException = false;
		try {
			cmisRelationshipService.getObjectRelationships(TEST_REPO_2, doc1Id, false, RelationshipDirection.EITHER, "cmis:relationpip", null, true, BigInteger.TEN, BigInteger.ZERO, null);
		} catch(CmisObjectNotFoundException e) {
			// cmis:relationship MISSPELT -> cmis:relationpip
			caughtExpectedException = true;
		} finally {
			assertTrue(caughtExpectedException);
		}
		
		relationships = cmisRelationshipService.getObjectRelationships(TEST_REPO_2, doc1Id, false, RelationshipDirection.EITHER, "cmis:relationship", null, true, BigInteger.TEN, BigInteger.ZERO, null);
		
		assertTrue(relationships != null);
		assertTrue(relationships.getObjects() != null);
		assertTrue(relationships.getObjects().size() == 0);
		
		relationships = cmisRelationshipService.getObjectRelationships(TEST_REPO_2, doc1Id, true, RelationshipDirection.EITHER, "cmis:relationship", null, true, BigInteger.TEN, BigInteger.ZERO, null);
		
		assertTrue(relationships != null);
		assertTrue(relationships.getObjects() != null);
		assertTrue(relationships.getObjects().size() == 1);
		assertTrue(relationships.getObjects().get(0) != null);
		
		assertTrue(relationships.getObjects().get(0).getId().equals(relId));
		
		pd = relationships.getObjects().get(0).getProperties().getProperties().get(PropertyIds.SOURCE_ID);
		assertNotNull(pd);
		assertTrue(pd.getFirstValue().equals(doc1Id));
		
		pd = relationships.getObjects().get(0).getProperties().getProperties().get(PropertyIds.TARGET_ID);
		assertNotNull(pd);
		assertTrue(pd.getFirstValue().equals(doc2Id));
		
		caughtExpectedException = false;
		try {
			// tdr:attachment is an abstract relationship type
			createRelationship(doc1Id, doc2Id,"tdr:attachment", "This should not be created!", true, "cmis:anyone");
		} catch(CmisInvalidArgumentException ex) {
			caughtExpectedException = true;
		} finally {
			assertTrue(caughtExpectedException);
		}
		
		String rel2Id = createRelationship(doc1Id, doc2Id,"tdr:bookmark", "Second relationship", true, "cmis:anyone");
		
		relationships = cmisRelationshipService.getObjectRelationships(TEST_REPO_2, doc1Id, true, RelationshipDirection.EITHER, "cmis:relationship", null, true, BigInteger.TEN, BigInteger.ZERO, null);
		assertTrue(relationships.getNumItems().intValue() == 2);
		assertTrue(relationships.hasMoreItems() == false);
		assertTrue(relationships.getObjects().size() == 2);
		
		relationships = cmisRelationshipService.getObjectRelationships(TEST_REPO_2, doc1Id, true, RelationshipDirection.EITHER, "cmis:relationship", null, true, BigInteger.ONE, BigInteger.ZERO, null);
		assertTrue(relationships.getNumItems().intValue() == 2);
		assertTrue(relationships.hasMoreItems() == true);
		assertTrue(relationships.getObjects().size() == 1);
		
		relationships = cmisRelationshipService.getObjectRelationships(TEST_REPO_2, doc1Id, true, RelationshipDirection.EITHER, "cmis:relationship", null, true, BigInteger.ONE, BigInteger.ONE, null);
		assertTrue(relationships.getNumItems().intValue() == 2);
		assertTrue(relationships.hasMoreItems() == false);
		assertTrue(relationships.getObjects().size() == 1);
		
		relationships = cmisRelationshipService.getObjectRelationships(TEST_REPO_2, doc2Id, true, RelationshipDirection.EITHER, "cmis:relationship", null, true, BigInteger.TEN, BigInteger.ZERO, null);
		assertTrue(relationships.getNumItems().intValue() == 2);
		assertTrue(relationships.hasMoreItems() == false);
		assertTrue(relationships.getObjects().size() == 2);
		
		relationships = cmisRelationshipService.getObjectRelationships(TEST_REPO_2, doc2Id, true, RelationshipDirection.EITHER, "cmis:relationship", null, true, BigInteger.ONE, BigInteger.ZERO, null);
		assertTrue(relationships.getNumItems().intValue() == 2);
		assertTrue(relationships.hasMoreItems() == true);
		assertTrue(relationships.getObjects().size() == 1);
		
		relationships = cmisRelationshipService.getObjectRelationships(TEST_REPO_2, doc2Id, true, RelationshipDirection.EITHER, "cmis:relationship", null, true, BigInteger.ONE, BigInteger.ONE, null);
		assertTrue(relationships.getNumItems().intValue() == 2);
		assertTrue(relationships.hasMoreItems() == false);
		assertTrue(relationships.getObjects().size() == 1);
		
		String rel3Id = createRelationship(doc1Id, doc2Id,"tdr:bookmark", "Third relationship", false, "cmis:anyone");
		
		//START DISCOVERY SERVICE TESTS
		results = cmisDiscoveryService.query(TEST_REPO_2, "select * from cmis:document", true, false, IncludeRelationships.NONE, null, BigInteger.TEN, BigInteger.ZERO, null);
		results = cmisDiscoveryService.query(TEST_REPO_2, "select * from cmis:document", true, false, IncludeRelationships.BOTH, null, BigInteger.TEN, BigInteger.ZERO, null);
		results = cmisDiscoveryService.query(TEST_REPO_2, "select * from cmis:document where cmis:objectId='"+doc1Id+"'", true, false, IncludeRelationships.SOURCE, null, BigInteger.TEN, BigInteger.ZERO, null);
		assertTrue(results.getObjects().get(0).getId().equals(doc1Id));
		assertTrue(results.getObjects().get(0).getRelationships().size() == 2);
		results = cmisDiscoveryService.query(TEST_REPO_2, "select * from cmis:document where cmis:objectId='"+doc1Id+"'", true, false, IncludeRelationships.TARGET, null, BigInteger.TEN, BigInteger.ZERO, null);
		assertTrue(results.getObjects().get(0).getId().equals(doc1Id));
		assertTrue(results.getObjects().get(0).getRelationships().size() == 1);
		//END DISCOVERY SERVICE TESTS
		
		//ACL Tests
		String rel4Id = createRelationship(doc1Id, doc2Id,"tdr:bookmark", "Fourth relationship", true, TestConstants.TEST_USER);
		
		relationships = cmisRelationshipService.getObjectRelationships(TEST_REPO_2, doc1Id, true, RelationshipDirection.EITHER, "cmis:relationship", null, true, BigInteger.TEN, BigInteger.ZERO, null);
		assertTrue(relationships.getNumItems().intValue() == 4);
		assertTrue(relationships.hasMoreItems() == false);
		assertTrue(relationships.getObjects().size() == 4);
		
		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TEST_REPO_2);
		
		relationships = cmisRelationshipService.getObjectRelationships(TEST_REPO_2, doc1Id, true, RelationshipDirection.EITHER, "cmis:relationship", null, true, BigInteger.TEN, BigInteger.ZERO, null);
		assertTrue(relationships.getNumItems().intValue() == 4);
		assertTrue(relationships.hasMoreItems() == false);
		assertTrue(relationships.getObjects().size() == 4);
		
		setUser(TestConstants.TEST2_USER, TestConstants.TEST2_PWD, TEST_REPO_2);
		
		relationships = cmisRelationshipService.getObjectRelationships(TEST_REPO_2, doc1Id, true, RelationshipDirection.EITHER, "cmis:relationship", null, true, BigInteger.TEN, BigInteger.ZERO, null);
		assertTrue(relationships.getNumItems().intValue() == 3);
		assertTrue(relationships.hasMoreItems() == false);
		assertTrue(relationships.getObjects().size() == 3);
		
		//delete relationship
		//create relationship (verify it exists), then delete relationship but not objects
		
		//retrieve relationships & related objects from object
		
	}

	@Test
	public void testCreateDocumentFromSource() throws IOException {
		ObjectType docType = new ObjectType();
		docType.setCmisId("cmis:document");

		/*
		 * set up the scenario, create a document to be copied
		 */
		CMISObject doc1 = new CMISObject();
		doc1.setObjectType(docType);
		doc1.addProperty(getTestProperty("MyDocumentSource.txt", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc1.addProperty(getTestProperty("cmis:document", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));

		Properties props = PropertiesBuilder.build(doc1, "");
		byte[] text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam ut purus purus.".getBytes();
		InputStream is = new ByteArrayInputStream(text);
		
		ContentStream contentStream = new ContentStreamImpl("MyDocumentSource.txt", BigInteger.valueOf(is.available()), "text/plain", is);
		String sourceId = cmisObjectService.createDocument(
				TEST_REPO_2, props, "/", contentStream, 
				VersioningState.NONE, Collections.<String>emptyList(), null, null, null);
		
		/*
		 * check the scenario
		 */
		{
		ContentStream cs = cmisObjectService.getContentStream(TEST_REPO_2, sourceId, null, BigInteger.ZERO, BigInteger.ZERO, null);
		BufferedReader br = new BufferedReader(new InputStreamReader(cs.getStream()));
		String line = br.readLine();
		Assert.assertArrayEquals(text, line.getBytes());
		}
		
		/*
		 * create a document copy
		 */
		CMISObject docCopy = new CMISObject();
		docCopy.setObjectType(docType);
		docCopy.addProperty(getTestProperty("MyDocumentCopy.txt", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		docCopy.addProperty(getTestProperty("cmis:document", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));

		Properties propsCopy = PropertiesBuilder.build(docCopy, "");
		
		String copyId = cmisObjectService.createDocumentFromSource(
				TEST_REPO_2, sourceId, propsCopy, "/", VersioningState.NONE, Collections.<String>emptyList(), 
				null, null, null);
		

		
		//check the copied document matches with the original
		{
		ContentStream cs = cmisObjectService.getContentStream(TEST_REPO_2, copyId, null, BigInteger.ZERO, BigInteger.ZERO, null);
		BufferedReader br = new BufferedReader(new InputStreamReader(cs.getStream()));
		String line = br.readLine();
		Assert.assertArrayEquals(text, line.getBytes());
		}
		
		Assert.assertEquals("generatedId-1", sourceId);
		Assert.assertEquals("generatedId-5", copyId);
		
		ObjectData sourceObject = cmisObjectService.getObject(TEST_REPO_2, sourceId, 
			null, Boolean.FALSE, IncludeRelationships.NONE, null, Boolean.FALSE, Boolean.FALSE, null);
		ObjectData copiedObject = cmisObjectService.getObject(TEST_REPO_2, copyId, 
			null, Boolean.FALSE, IncludeRelationships.NONE, null, Boolean.FALSE, Boolean.FALSE, null);

		Assert.assertEquals(
			sourceObject.getProperties().getPropertyList().size(),
			copiedObject.getProperties().getPropertyList().size());
		
		Map<String, PropertyData<?>> mapSource = sourceObject.getProperties().getProperties();
		Map<String, PropertyData<?>> mapCopy = copiedObject.getProperties().getProperties();
		
		Assert.assertEquals(mapSource.get(PropertyIds.NAME).getFirstValue(), "MyDocumentSource.txt");
		Assert.assertEquals(mapCopy.get(PropertyIds.NAME).getFirstValue(), "MyDocumentCopy.txt");
		
		checkPropertyIsDifferent(PropertyIds.OBJECT_ID, mapSource, mapCopy);
		checkPropertyIsDifferent(PropertyIds.CREATION_DATE, mapSource, mapCopy);
		checkPropertyIsDifferent(PropertyIds.LAST_MODIFICATION_DATE, mapSource, mapCopy);
		checkPropertyIsDifferent(PropertyIds.VERSION_SERIES_ID, mapSource, mapCopy);
		checkPropertyIsDifferent(PropertyIds.CHANGE_TOKEN, mapSource, mapCopy);
		checkPropertyIsDifferent(PropertyIds.CONTENT_STREAM_ID, mapSource, mapCopy);
		
		checkPropertyIsEqual(PropertyIds.BASE_TYPE_ID, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.CREATED_BY, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.LAST_MODIFIED_BY, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.IS_LATEST_VERSION, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.IS_MAJOR_VERSION, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.IS_LATEST_MAJOR_VERSION, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.VERSION_LABEL, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.CHECKIN_COMMENT, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.CONTENT_STREAM_LENGTH, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.CONTENT_STREAM_MIME_TYPE, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.CONTENT_STREAM_FILE_NAME, mapSource, mapCopy);
	}
	
	
	@Test
	public void testCreateDocumentFromSource02() throws IOException {
		ObjectType docType = new ObjectType();
		docType.setCmisId("test:document");

		/*
		 * set up the scenario, create a document to be copied
		 */
		CMISObject doc1 = new CMISObject();
		doc1.setObjectType(docType);
		doc1.addProperty(getTestProperty("MyDocumentSource.txt", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc1.addProperty(getTestProperty("test:document", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));
		doc1.addProperty(getTestProperty("ID1", TEST_REPO_2, "test:document", "test:prop1"));
		doc1.addProperty(getTestProperty("string", TEST_REPO_2, "test:document", "test:prop2"));
		doc1.addProperty(getTestProperty("ID2", TEST_REPO_2, "test:document", "test:prop3"));

		Properties props = PropertiesBuilder.build(doc1, "");
		byte[] text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam ut purus purus.".getBytes();
		InputStream is = new ByteArrayInputStream(text);
		
		ContentStream contentStream = new ContentStreamImpl("MyDocumentSource.txt", BigInteger.valueOf(is.available()), "text/plain", is);
		String sourceId = cmisObjectService.createDocument(
				TEST_REPO_2, props, "/", contentStream, 
				VersioningState.NONE, Collections.<String>emptyList(), null, null, null);
		
		/*
		 * check the scenario
		 */
		{
		ContentStream cs = cmisObjectService.getContentStream(TEST_REPO_2, sourceId, null, BigInteger.ZERO, BigInteger.ZERO, null);
		BufferedReader br = new BufferedReader(new InputStreamReader(cs.getStream()));
		String line = br.readLine();
		Assert.assertArrayEquals(text, line.getBytes());
		}
		
		/*
		 * create a document copy
		 */
		CMISObject docCopy = new CMISObject();
		docCopy.setObjectType(docType);
		docCopy.addProperty(getTestProperty("MyDocumentCopy.txt", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		docCopy.addProperty(getTestProperty("cmis:document", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));
		docCopy.addProperty(getTestProperty("ID11", TEST_REPO_2, "test:document", "test:prop1"));


		Properties propsCopy = PropertiesBuilder.build(docCopy, "");
		
		String copyId = cmisObjectService.createDocumentFromSource(
				TEST_REPO_2, sourceId, propsCopy, "/", VersioningState.NONE, Collections.<String>emptyList(), 
				null, null, null);
		

		
		//check the copied document matches with the original
		{
		ContentStream cs = cmisObjectService.getContentStream(TEST_REPO_2, copyId, null, BigInteger.ZERO, BigInteger.ZERO, null);
		BufferedReader br = new BufferedReader(new InputStreamReader(cs.getStream()));
		String line = br.readLine();
		Assert.assertArrayEquals(text, line.getBytes());
		}
		
		Assert.assertEquals("generatedId-1", sourceId);
		Assert.assertEquals("generatedId-5", copyId);
		
		ObjectData sourceObject = cmisObjectService.getObject(TEST_REPO_2, sourceId, 
			null, Boolean.FALSE, IncludeRelationships.NONE, null, Boolean.FALSE, Boolean.FALSE, null);
		ObjectData copiedObject = cmisObjectService.getObject(TEST_REPO_2, copyId, 
			null, Boolean.FALSE, IncludeRelationships.NONE, null, Boolean.FALSE, Boolean.FALSE, null);

		Assert.assertEquals(
			sourceObject.getProperties().getPropertyList().size(),
			copiedObject.getProperties().getPropertyList().size());
		
		Map<String, PropertyData<?>> mapSource = sourceObject.getProperties().getProperties();
		Map<String, PropertyData<?>> mapCopy = copiedObject.getProperties().getProperties();
		
		Assert.assertEquals(mapSource.get(PropertyIds.NAME).getFirstValue(), "MyDocumentSource.txt");
		Assert.assertEquals(mapCopy.get(PropertyIds.NAME).getFirstValue(), "MyDocumentCopy.txt");
		
		Assert.assertEquals(mapSource.get("test:prop1").getFirstValue(), "ID1");
		Assert.assertEquals(mapCopy.get("test:prop1").getFirstValue(), "ID11");
		
		checkPropertyIsDifferent(PropertyIds.OBJECT_ID, mapSource, mapCopy);
		checkPropertyIsDifferent(PropertyIds.CREATION_DATE, mapSource, mapCopy);
		checkPropertyIsDifferent(PropertyIds.LAST_MODIFICATION_DATE, mapSource, mapCopy);
		checkPropertyIsDifferent(PropertyIds.VERSION_SERIES_ID, mapSource, mapCopy);
		checkPropertyIsDifferent(PropertyIds.CHANGE_TOKEN, mapSource, mapCopy);
		checkPropertyIsDifferent(PropertyIds.CONTENT_STREAM_ID, mapSource, mapCopy);
		
		checkPropertyIsEqual(PropertyIds.BASE_TYPE_ID, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.CREATED_BY, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.LAST_MODIFIED_BY, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.IS_LATEST_VERSION, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.IS_MAJOR_VERSION, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.IS_LATEST_MAJOR_VERSION, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.VERSION_LABEL, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.CHECKIN_COMMENT, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.CONTENT_STREAM_LENGTH, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.CONTENT_STREAM_MIME_TYPE, mapSource, mapCopy);
		checkPropertyIsEqual(PropertyIds.CONTENT_STREAM_FILE_NAME, mapSource, mapCopy);
		
		checkPropertyIsEqual("test:prop2", mapSource, mapCopy);
		checkPropertyIsEqual("test:prop3", mapSource, mapCopy);
	}
	
	private void checkPropertyIsDifferent(String propertyId, Map<String, PropertyData<?>> mapSource, Map<String, PropertyData<?>> mapCopy) {
		Assert.assertNotSame(mapSource.get(propertyId).getValues(), mapCopy.get(propertyId).getValues());
	}

	private void checkPropertyIsEqual(String propertyId, Map<String, PropertyData<?>> mapSource, Map<String, PropertyData<?>> mapCopy) {
		Assert.assertEquals(mapSource.get(propertyId).getValues(), mapCopy.get(propertyId).getValues());
	}
	
	@Transactional(propagation=Propagation.MANDATORY)
	private String createRelationship(String doc1Id, String doc2Id, String relationshipType, String label, boolean leftToRight, String principalId) {
		
		CMISObject rel = new CMISObject(new ObjectType(relationshipType));
		rel.addProperty(getTestProperty(label, TEST_REPO_2, Constants.TYPE_CMIS_RELATIONSHIP, PropertyIds.NAME));
		rel.addProperty(getTestProperty(relationshipType, TEST_REPO_2, Constants.TYPE_CMIS_RELATIONSHIP, PropertyIds.OBJECT_TYPE_ID));
		rel.addProperty(getTestProperty(doc1Id, TEST_REPO_2, Constants.TYPE_CMIS_RELATIONSHIP, leftToRight?PropertyIds.SOURCE_ID:PropertyIds.TARGET_ID));
		rel.addProperty(getTestProperty(doc2Id, TEST_REPO_2, Constants.TYPE_CMIS_RELATIONSHIP, leftToRight?PropertyIds.TARGET_ID:PropertyIds.SOURCE_ID));
		
		Acl acl = createAcl(true, Collections.singletonList(principalId));
		
		String relId = cmisObjectService.createRelationship(TEST_REPO_2, PropertiesBuilder.build(rel), null, acl, null, null);
		return relId;
	}
}
