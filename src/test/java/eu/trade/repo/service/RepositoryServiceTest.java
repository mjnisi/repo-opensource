package eu.trade.repo.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.id.IDGenerator;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.ObjectTypeRelationship;
import eu.trade.repo.model.ObjectTypeRelationship.RelationshipType;
import eu.trade.repo.model.Repository;
import eu.trade.repo.util.Constants;
import eu.trade.repo.util.ICurrentDate;

public class RepositoryServiceTest extends BaseTestClass {

	@Autowired
	private IDGenerator mockGenerator;
	
	@Autowired
	private ICurrentDate mockCurrentDate;

	@Test
	public void testCreateRepository01() throws Exception {

		setScenario("scenario01.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_permission");
		resetSequence("sq_permission_mapping");
		resetSequence("sq_repository");
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		resetSequence("sq_property");
		resetSequence("sq_object");
		resetSequence("sq_acl");

		//maybe the generator was used in another test and I am expecting to
		//start with 1
		mockGenerator.reset();
		mockCurrentDate.reset();
		

		Repository repo = new Repository();
		repo.setCmisId("repo001a");
		repo.setName(repo.getCmisId());
		repo.setDescription(repo.getCmisId());
		repositoryService.createRepository(repo);

		Repository repo2 = new Repository();
		repo2.setCmisId("repo002a");
		repo2.setName(repo2.getCmisId());
		repo2.setDescription(repo2.getCmisId());
		repositoryService.createRepository(repo2);

		compareTable(
				"repository",
				"id = 10000",
				"repository-service-test01.xml");

		compareTable(
				"object_type",
				"repository_id = 10000 order by id",
				"repository-service-test01.xml");

		compareTable(
				"object_type_property",
				"object_type_id in (select id from object_type where repository_id = 10000) order by id",
				"repository-service-test01.xml");

		compareTable(
				"permission",
				"repository_id = 10000 order by id",
				"repository-service-test01.xml");

		compareTable(
				"permission_mapping",
				"repository_id = 10000 order by id",
				"repository-service-test01.xml");

		compareTable(
				"object",
				"id = 10000 order by id",
				"repository-service-test01.xml");

		compareTable(
				"property",
				"object_id = 10000 order by id",
				"repository-service-test01.xml");

		compareTable(
				"acl",
				"id = 10000 order by id",
				"repository-service-test01.xml");
	}

	@Test
	public void testGetObjectTypeChildren_childrenOfCmisDocument() throws Exception {
		setScenario("scenario02.xml", DatabaseOperation.CLEAN_INSERT);

		Set<ObjectType> objectTypeSet = repositoryService.getObjectTypeChildren("test_repo_02", "cmis:document", true);
		assertEquals(4, objectTypeSet.size());

		ObjectType tronDocument = null;
		ObjectType nestDocument = null;
		ObjectType tradeDocument = null;

		for (ObjectType objectType : objectTypeSet) {
			if (objectType.getQueryName().equals("tron:document")) {
				tronDocument = objectType;
			} else if (objectType.getQueryName().equals("nest:document")) {
				nestDocument = objectType;
			} else if (objectType.getQueryName().equals("trade:document")) {
				tradeDocument = objectType;
			}
		}

		assertNotNull(tronDocument);
		assertNotNull(nestDocument);
		assertNotNull(tradeDocument);

		assertEquals("tron:document", tronDocument.getQueryName());
		assertEquals("tron:document", tronDocument.getLocalName());
		assertEquals("tron:document", tronDocument.getDisplayName());

		assertEquals("nest:document", nestDocument.getQueryName());
		assertEquals("nest:document", nestDocument.getLocalName());
		assertEquals("nest:document", nestDocument.getDisplayName());

		assertEquals("trade:document", tradeDocument.getQueryName());
		assertEquals("trade:document", tradeDocument.getLocalName());
		assertEquals("trade:document", tradeDocument.getDisplayName());
	}

	@Test
	public void testGetObjectTypeChildren_childrenOfCmisDocument_withLimit() throws Exception {
		setScenario("scenario02.xml", DatabaseOperation.CLEAN_INSERT);

		Set<ObjectType> objectTypeSet = repositoryService.getObjectTypeChildren("test_repo_02", "cmis:document", false, 2, 0);
		assertEquals(2, objectTypeSet.size());
	}

	@Test
	public void testGetObjectTypeChildren_childrenOfCmisDocument_withLimitAndOffset() throws Exception {
		setScenario("scenario02.xml", DatabaseOperation.CLEAN_INSERT);

		Set<ObjectType> objectTypeSet = repositoryService.getObjectTypeChildren("test_repo_02", "cmis:document", false, 3, 2);
		assertEquals(2, objectTypeSet.size());
	}

	@Test
	public void testGetObjectTypeChildren_childrenOfCmisDocument_withBigLimitAndOffset() throws Exception {
		setScenario("scenario02.xml", DatabaseOperation.CLEAN_INSERT);

		Set<ObjectType> objectTypeSet = repositoryService.getObjectTypeChildren("test_repo_02", "cmis:document", false, 9999, 1);
		assertEquals(3, objectTypeSet.size());
	}

	@Test
	public void testGetObjectTypeChildren_childrenOfNestDocument() throws Exception {
		setScenario("scenario02.xml", DatabaseOperation.CLEAN_INSERT);

		ObjectType[] result = repositoryService.getObjectTypeChildren("test_repo_02", "nest:document", true).toArray(new ObjectType[0]);
		assertEquals(1, result.length);
		ObjectType objectType = result[0];
		assertEquals("nest:special_document", objectType.getQueryName());
		assertEquals("nest:special_document", objectType.getLocalName());
		assertEquals("nest:special_document", objectType.getDisplayName());
		assertNotNull(objectType.getObjectTypeProperties());
	}

	@Test(expected = org.hibernate.LazyInitializationException.class)
	public void testGetObjectTypeChildren_childrenOfNestDocument_LazyLoadedExceptionThrown() throws Exception {
		setScenario("scenario02.xml", DatabaseOperation.CLEAN_INSERT);

		ObjectType[] result = repositoryService.getObjectTypeChildren("test_repo_02", "nest:document", false).toArray(new ObjectType[0]);
		assertEquals(1, result.length);
		ObjectType objectType = result[0];
		assertEquals("nest:special_document", objectType.getQueryName());
		assertEquals("nest:special_document", objectType.getLocalName());
		assertEquals("nest:special_document", objectType.getDisplayName());

		//should throw an exception
		Set<ObjectTypeProperty> objectTypeProperties = objectType.getObjectTypeProperties();
		objectTypeProperties.isEmpty();
	}


	@Test
	public void testGetObjectTypeChildren_childrenOfCmisFolder() throws Exception {
		setScenario("scenario02.xml", DatabaseOperation.CLEAN_INSERT);

		ObjectType[] result = repositoryService.getObjectTypeChildren("test_repo_02", Constants.TYPE_CMIS_FOLDER, true).toArray(new ObjectType[0]);
		assertEquals(2, result.length);
		ObjectType objectType = result[0];
		assertEquals("nest:folder", objectType.getQueryName());
		assertEquals("nest:folder", objectType.getLocalName());
		assertEquals("nest:folder", objectType.getDisplayName());

		objectType = result[1];
		assertEquals("test:folder", objectType.getQueryName());
		assertEquals("test:folder", objectType.getLocalName());
		assertEquals("test:folder", objectType.getDisplayName());
	}


	@Test
	public void testDeleteRepository01() throws Exception {

		setScenario("scenario02.xml", DatabaseOperation.CLEAN_INSERT);

		repositoryService.deleteRepository("test_repo_02");

		compareTable("repository", "repository-service-test02.xml");
	}
	

	
	/**
	 * 01 Creates a document type (ok)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeDocument01() throws Exception {
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType tradeDoc = createTradeDocumentType("scenarioRepositoryService01");
		repositoryService.createType(tradeDoc);
		
		compareTable("object_type", "scenarioRepositoryService-test01.xml");
		compareTable("object_type_property", "scenarioRepositoryService-test01.xml");
	}
	
	/**
	 * 02 Creates a document subtype (ok)
	 * @throws Exception
	 */
	@Test
	public void testCreateSubTypeDocument02() throws Exception {
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType tradeDoc = createTradeDocumentType("scenarioRepositoryService01");
		repositoryService.createType(tradeDoc);
		
		ObjectType tradeAttachment = createAttachmentDocumentType("scenarioRepositoryService01");
		repositoryService.createType(tradeAttachment);
		
		compareTable("object_type", "scenarioRepositoryService-test02.xml");
		compareTable("object_type_property", "scenarioRepositoryService-test02.xml");
	}
	
	/**
	 * 03 Creates a folder type (ok)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeFolder03() throws Exception {
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType tradeFolder = createTradeFolderType("scenarioRepositoryService01");
		repositoryService.createType(tradeFolder);

		ObjectType tradeEmail = createEmailFolderType("scenarioRepositoryService01");
		repositoryService.createType(tradeEmail);
		
		compareTable("object_type", "scenarioRepositoryService-test03.xml");
		compareTable("object_type_property", "scenarioRepositoryService-test03.xml");
	}
	
	/**
	 * 04 Creates a relationship type (ok)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeRelationship04() throws Exception {
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		resetSequence("sq_object_type_relationship");
		
		ObjectType tradeRel = createTradeRelationship("scenarioRepositoryService01");
		repositoryService.createType(tradeRel);
		
		compareTable("object_type", "scenarioRepositoryService-test04.xml");
		compareTable("object_type_property", "scenarioRepositoryService-test04.xml");
		compareTable("object_type_relationship", "scenarioRepositoryService-test04.xml");
	}
	
	/**
	 * 05 Creates a document type (fails because id is used)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeDocumentFails05() throws Exception {
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType tradeDoc = createTradeDocumentType("scenarioRepositoryService01");
		tradeDoc.setCmisId("cmis:document");
		
		try {
			repositoryService.createType(tradeDoc);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create this type, Id already in use.");
		}
	}
	
	
	/**
	 * 06 Creates a document type (fails because query name is used)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeDocumentFails06() throws Exception {
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType tradeDoc = createTradeDocumentType("scenarioRepositoryService01");
		tradeDoc.setQueryName("cmis:document");
		
		try {
			repositoryService.createType(tradeDoc);
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create this type, query name already in use.");
		}
	}
	
	/**
	 * 07 Creates a document type (fails because base type does not exist)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeDocumentFails07() throws Exception {
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType tradeDoc = createTradeDocumentType("scenarioRepositoryService01");
		tradeDoc.setBase(new ObjectType("xxx:yyy"));
		
		try {
			repositoryService.createType(tradeDoc);
			Assert.fail();
		} catch (CmisInvalidArgumentException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create this type, base type doesn't exist.");
		}
	}
	
	
	/**
	 * 08 Creates a document type (fails because parent type does not exist)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeDocumentFails08() throws Exception {
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType tradeDoc = createTradeDocumentType("scenarioRepositoryService01");
		tradeDoc.setParent(new ObjectType("xxx:yyy"));
		
		try {
			repositoryService.createType(tradeDoc);
			Assert.fail();
		} catch (CmisInvalidArgumentException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create this type, parent type doesn't exist.");
		}
	}
	
	/**
	 * 09 Creates a document type (fails because property id is used)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeDocumentFails09() throws Exception {
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType tradeDoc = createTradeDocumentType("scenarioRepositoryService01");
		tradeDoc.getObjectTypeProperties().iterator().next().setCmisId("cmis:name");
		
		try {
			repositoryService.createType(tradeDoc);
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create or update this type, property Id cmis:name already in use.");
		}			
	}

	
	/**
	 * 10 Creates a document type (fails because property query name is used)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeDocumentFails10() throws Exception {
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType tradeDoc = createTradeDocumentType("scenarioRepositoryService01");
		tradeDoc.getObjectTypeProperties().iterator().next().setQueryName("cmis:name");
		
		try {
			repositoryService.createType(tradeDoc);
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create or update this type, query name cmis:name already in use.");
		}			
	}
	
	/**
	 * 11 Deletes a type (ok)
	 * @throws Exception
	 */
	@Test
	public void testDeleteType11() throws Exception {
		setScenario("scenarioRepositoryService02.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		repositoryService.deleteType("scenarioRepositoryService02", "trade:attachment");
		
		compareTable("object_type", "scenarioRepositoryService-test11.xml");
		compareTable("object_type_property", "scenarioRepositoryService-test11.xml");
	}
	
	/**
	 * 12 Deletes a type (fails becuse is a base type)
	 * @throws Exception
	 */
	@Test
	public void testDeleteTypeFailsBaseType12() throws Exception {
		setScenario("scenarioRepositoryService02.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		try {
			repositoryService.deleteType("scenarioRepositoryService02", "cmis:document");
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to delete base types.");
		}			
	}
	
	/**
	 * 13 Deletes a type (fails because there are documents)
	 * @throws Exception
	 */
	@Test
	public void testDeleteTypeFailsWithObjects13() throws Exception {
		setScenario("scenarioRepositoryService03.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		try {
			repositoryService.deleteType("scenarioRepositoryService03", "trade:subfolder");
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to delete this type because there are objects created of this type.");
		}
	}
	
	
	/**
	 * 14 Deletes a type (fails because there is a subtype)
	 * @throws Exception
	 */
	@Test
	public void testDeleteTypeFailsSubtype14() throws Exception {
		setScenario("scenarioRepositoryService04.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		try {
			repositoryService.deleteType("scenarioRepositoryService04", "trade:folder");
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to delete this type because there are subtypes.");
		}			
	}
	
	/**
	 * 15 Create a relationship (fails because there is a reference to an non-existing type)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeRelationshipFails15() throws Exception {
		setScenario("scenarioRepositoryService04.xml", DatabaseOperation.CLEAN_INSERT);
		
		ObjectType tradeRel = createTradeRelationship("scenarioRepositoryService04");

		ObjectTypeRelationship otr = new ObjectTypeRelationship();
		otr.setReferencedObjectType(new ObjectType("xxx:yyy"));
		otr.setType(RelationshipType.SOURCE);
		tradeRel.addObjectTypeRelationship(otr);
		
		try {
			repositoryService.createType(tradeRel);
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create this type, relationship reference type doesn't exist.");
		}			
	}

	/**
	 * 16 Create a type (fails because missing definition data)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeFailsMissing16() throws Exception {
		setScenario("scenarioRepositoryService04.xml", DatabaseOperation.CLEAN_INSERT);
		
		ObjectType tradeDoc = createTradeDocumentType("scenarioRepositoryService04");
		tradeDoc.setFileable(null);
		
		try {
			repositoryService.createType(tradeDoc);
			Assert.fail();
		} catch (CmisInvalidArgumentException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create this type, missing definition data.");
		}			
	}

	/**
	 * 17 Create a type (fails because missing definition data on properties)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeFailsMissingProperty17() throws Exception {
		setScenario("scenarioRepositoryService04.xml", DatabaseOperation.CLEAN_INSERT);
		
		ObjectType tradeDoc = createTradeDocumentType("scenarioRepositoryService04");
		tradeDoc.getObjectTypePropertiesMap().get("trade:author").setRequired(null);
		
		try {
			repositoryService.createType(tradeDoc);
			Assert.fail();
		} catch (CmisInvalidArgumentException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create or update this type, missing definition data on property trade:author.");
		}			
	}


	/**
	 * 18 Create a type (fails because missing definition data)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeFailsMissing18() throws Exception {
		setScenario("scenarioRepositoryService04.xml", DatabaseOperation.CLEAN_INSERT);
		
		ObjectType tradeDoc = createTradeDocumentType("scenarioRepositoryService04");
		tradeDoc.setParent(null);
		
		try {
			repositoryService.createType(tradeDoc);
			Assert.fail();
		} catch (CmisInvalidArgumentException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create this type, missing definition data.");
		}
	}

	/**
	 * 19 create type (invalid repository)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeFailsWrongRepo19() throws Exception {
		setScenario("scenarioRepositoryService15.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = createTradeFolderType("scenarioRepositoryService15");
		folder.getRepository().setCmisId("wrong repo");
		
		try {
			repositoryService.createType(folder);
			Assert.fail();
		} catch(CmisInvalidArgumentException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create this type, invalid repository.");
		}
	}
	
	/**
	 * 20 incompatible base and parent type
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeFailsIncompatibleBase20() throws Exception {
		setScenario("scenarioRepositoryService15.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = createEmailFolderType("scenarioRepositoryService15");
		ObjectType doc = new ObjectType("cmis:document");
		Repository repo = new Repository();
		repo.setCmisId("scenarioRepositoryService15");
		doc.setRepository(repo);
		folder.setBase(doc);
		
		try {
			repositoryService.createType(folder);
			Assert.fail();
		} catch(CmisInvalidArgumentException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create this type, parent and base type are not compatible.");
		}
	}
	
	/**
	 * 21 Creates a relationship type (fails because shouldn't be fileable)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeRelationshipFailsFilable21() throws Exception {
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		resetSequence("sq_object_type_relationship");
		
		ObjectType tradeRel = createTradeRelationship("scenarioRepositoryService01");
		tradeRel.setFileable(true);

		try {
			repositoryService.createType(tradeRel);
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals("Not allowed to create this type, relationship subtypes MUST NOT be file-able.", e.getMessage());
		}
		
	}
	
	
	/**
	 * 27 Creates a item type (ok)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeItem27() throws Exception {
		setScenario("scenarioRepositoryService27.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType tradeItem = createTradeItemType("scenarioRepositoryService27");
		repositoryService.createType(tradeItem);
		
		compareTable("object_type", "scenarioRepositoryService-test27.xml");
		compareTable("object_type_property", "scenarioRepositoryService-test27.xml");
	}
	
	
	
	/**
	 * 28 Creates a secondary type (ok)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeSecondary28() throws Exception {
		setScenario("scenarioRepositoryService28.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType tradeSecondary = createTradeSecondaryType("scenarioRepositoryService28");
		repositoryService.createType(tradeSecondary);
		
		compareTable("object_type", "scenarioRepositoryService-test28.xml");
		compareTable("object_type_property", "scenarioRepositoryService-test28.xml");
	}


	/**
	 * 29 Creates a secondary type (fails because is creatable)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeSecondaryFailsCreatable29() throws Exception {
		setScenario("scenarioRepositoryService28.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType tradeSecondary = createTradeSecondaryType("scenarioRepositoryService28");
		tradeSecondary.setCreatable(true);
		try {
			repositoryService.createType(tradeSecondary);
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals("Not allowed to create this type, secondary subtypes MUST NOT be creatable.", e.getMessage());
		}
	}

	/**
	 * 30 Creates a secondary type (fails because is fileable)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeSecondaryFailsFileable30() throws Exception {
		setScenario("scenarioRepositoryService28.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType tradeSecondary = createTradeSecondaryType("scenarioRepositoryService28");
		tradeSecondary.setFileable(true);
		try {
			repositoryService.createType(tradeSecondary);
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals("Not allowed to create this type, secondary subtypes MUST NOT be file-able.", e.getMessage());
		}
	}

	/**
	 * 31 Creates a secondary type (fails because is controllable policy)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeSecondaryFailsControllablePolicy31() throws Exception {
		setScenario("scenarioRepositoryService28.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType tradeSecondary = createTradeSecondaryType("scenarioRepositoryService28");
		tradeSecondary.setControllablePolicy(true);
		try {
			repositoryService.createType(tradeSecondary);
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals("Not allowed to create this type, secondary subtypes MUST NOT be controllable by policy.", e.getMessage());
		}
	}


	/**
	 * 32 Creates a secondary type (fails because is controllable policy)
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeSecondaryFailsControllableACL32() throws Exception {
		setScenario("scenarioRepositoryService28.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType tradeSecondary = createTradeSecondaryType("scenarioRepositoryService28");
		tradeSecondary.setControllableAcl(true);
		try {
			repositoryService.createType(tradeSecondary);
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals("Not allowed to create this type, secondary subtypes MUST NOT be controllable by ACL.", e.getMessage());
		}
	}

	/**
	 * create secondary type fails because used query name
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeSecondaryFailesUsedQueryName() throws Exception {
		setScenario("scenarioRepositoryService28.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType tradeSecondary = createTradeSecondaryType("scenarioRepositoryService28");
		tradeSecondary.getObjectTypeProperties().iterator().next().setQueryName("cmis:name");

		try {
			repositoryService.createType(tradeSecondary);
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals("Not allowed to create or update this type, query name cmis:name already in use. Secondary types must have unique property query names in whole repository.", e.getMessage());
		}
	}

	/**
	 * create secondary type fails because used id
	 * @throws Exception
	 */
	@Test
	public void testCreateTypeSecondaryFailsUsedId() throws Exception {
		setScenario("scenarioRepositoryService28.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType tradeSecondary = createTradeSecondaryType("scenarioRepositoryService28");
		tradeSecondary.getObjectTypeProperties().iterator().next().setCmisId("cmis:name");

		try {
			repositoryService.createType(tradeSecondary);
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals("Not allowed to create or update this type, property Id cmis:name already in use. Secondary types must have unique property Ids in whole repository.", e.getMessage());
		}
	}
	
	/**
	 * update secondary type fails because used query name
	 */
	@Test
	public void testUpdateTypeSecondaryFailsUsedQueryName() throws Exception {
		setScenario("scenarioSecondaryTypes03.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType secondary = objTypeSelector.getObjectTypeByCmisIdWithProperties("secondary", "invoice");
		ObjectTypeProperty prop = buildObjectTypeProperty(
				"cmis:name00", PropertyType.STRING, Cardinality.SINGLE, Updatability.READWRITE, false, "name");
		prop.setQueryName("cmis:name");
		secondary.addObjectTypeProperty(prop);
		
		try {
			repositoryService.updateType(secondary);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create or update this type, query name cmis:name already in use. Secondary types must have unique property query names in whole repository.");
		}
	}
	
	/**
	 * update secondary type fails because used id
	 */
	@Test
	public void testUpdateTypeSecondaryFailsUsedId() throws Exception {
		setScenario("scenarioSecondaryTypes03.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType secondary = objTypeSelector.getObjectTypeByCmisIdWithProperties("secondary", "invoice");
		ObjectTypeProperty prop = buildObjectTypeProperty(
				"cmis:name00", PropertyType.STRING, Cardinality.SINGLE, Updatability.READWRITE, false, "name");
		prop.setCmisId("cmis:name");
		secondary.addObjectTypeProperty(prop);
		
		try {
			repositoryService.updateType(secondary);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create or update this type, property Id cmis:name already in use. Secondary types must have unique property Ids in whole repository.");
		}
	}
	
	
	/**
	 * 99 check valid query name
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsValidQueryName99() throws Exception {
		setScenario("scenarioRepositoryService15.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService15", "trade:subfolder");
		folder.getObjectTypePropertiesMap().get("trade:notes").setQueryName("wrong query name");
		
		try {
			repositoryService.updateType(folder);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create or update this type, query name 'wrong query name' is using invalid characters, must use only letters, underscore and colon.");
		}
	}
	
	
	/**
	 * 00 check several query names
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsUsedQueryName00() throws Exception {
		setScenario("scenarioRepositoryService15.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService15", "trade:subfolder");
		ObjectTypeProperty commentsA = buildObjectTypeProperty(
				"trade:description", PropertyType.STRING, Cardinality.SINGLE, Updatability.READWRITE, false, "Folder desc");
		ObjectTypeProperty commentsB = buildObjectTypeProperty(
				"trade:description2", PropertyType.STRING, Cardinality.SINGLE, Updatability.READWRITE, false, "Folder desc");
		
		commentsA.setQueryName("description");
		commentsB.setQueryName("description");
		
		folder.addObjectTypeProperty(commentsA);
		folder.addObjectTypeProperty(commentsB);
		
		
		try {
			repositoryService.updateType(folder);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create or update this type, query name description is duplicated.");
		}
	}
	
	/**
	 * 01 missing minimal input data
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsMissing01() throws Exception {
		setScenario("scenarioRepositoryService15.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService15", "trade:subfolder");
		folder.setCmisId(null);
		
		try {
			repositoryService.updateType(folder);
			Assert.fail();
		} catch(CmisInvalidArgumentException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to update this type, missing definition data.");
		}
	}
	
	/**
	 * 02 missing minimal input data properties
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsMissing02() throws Exception {
		setScenario("scenarioRepositoryService15.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService15", "trade:subfolder");
		folder.getObjectTypePropertiesMap().get("trade:notes").setRequired(null);
		
		try {
			repositoryService.updateType(folder);
			Assert.fail();
		} catch(CmisInvalidArgumentException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create or update this type, " +
						"missing definition data on property trade:notes.");
		}
	}
	
	/**
	 * 03 wrong repository id
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsWrongRepo03() throws Exception {
		setScenario("scenarioRepositoryService15.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService15", "trade:subfolder");
		folder.getRepository().setCmisId("wrong repo");
		
		try {
			repositoryService.updateType(folder);
			Assert.fail();
		} catch(CmisInvalidArgumentException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to update this type, type doesn't exist.");
		}
	}
	

	

	
	/**
	 * 06 property id used in the hierarchy
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsUsedId06() throws Exception {
		setScenario("scenarioRepositoryService15.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService15", "trade:subfolder");
		ObjectTypeProperty comments = buildObjectTypeProperty(
				"trade:comments", PropertyType.STRING, Cardinality.SINGLE, Updatability.READWRITE, false, "Folder comments");
		folder.addObjectTypeProperty(comments);
		
		try {
			repositoryService.updateType(folder);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create or update this type, " +
					"property Id trade:comments already in use.");
		}
	}
	
	/**
	 * 07 query name used in the hierarchy
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsQueryName07() throws Exception {
		setScenario("scenarioRepositoryService15.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService15", "trade:subfolder");
		ObjectTypeProperty comments = buildObjectTypeProperty(
				"trade:comments2", PropertyType.STRING, Cardinality.SINGLE, Updatability.READWRITE, false, "Folder comments");
		comments.setQueryName("trade:comments");
		folder.addObjectTypeProperty(comments);
		
		try {
			repositoryService.updateType(folder);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to create or update this type, " +
					"query name trade:comments already in use.");
		}
	}
	
	/**
	 * 08 object type doesn't exist
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsWrongType08() throws Exception {
		setScenario("scenarioRepositoryService15.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType doc = createTradeDocumentType("scenarioRepositoryService15");
		
		try {
			repositoryService.updateType(doc);
			Assert.fail();
		} catch(CmisInvalidArgumentException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to update this type, type doesn't exist.");
		}
	}
	
	

	/**
	 * 09 query name change ignored
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeIgnoreQueryName09() throws Exception {
		setScenario("scenarioRepositoryService04.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService04", "trade:subfolder");
		folder.setQueryName("cmis:document");//ignored
		
		repositoryService.updateType(folder);
		
		compareTable("object_type", "scenarioRepositoryService-testUpdate20.xml");
		compareTable("object_type_property", "scenarioRepositoryService-testUpdate20.xml");
	}

	
	
	/**
	 * 10 only leaf types could be updated
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsOnlyLeafs10() throws Exception {
		setScenario("scenarioRepositoryService15.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService15", "cmis:folder");
		folder.setDisplayName("new display name");
		
		try {
			repositoryService.updateType(folder);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to update this type, only leaf types may be be modified.");
		}
	}
	
	/**
	 * 11 new required properties must have default value
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsRequiredDefaultValue11() throws Exception {
		setScenario("scenarioRepositoryService15.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType doc = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService15", "cmis:document");
		ObjectTypeProperty comments = buildObjectTypeProperty(
				"cmis:comments", PropertyType.STRING, Cardinality.SINGLE, Updatability.READWRITE, true, "Folder comments");
		doc.addObjectTypeProperty(comments);
		
		try {
			repositoryService.updateType(doc);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to update this type, any added properties marked as required MUST have a default value.");
		}
	}
	
	/**
	 * 12 cmis properties must not be modified
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsPropertyModified12() throws Exception {
		setScenario("scenarioRepositoryService15.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType doc = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService15", "cmis:document");
		doc.getObjectTypePropertiesMap().get("cmis:name").setQueryName("name");
		
		try {
			repositoryService.updateType(doc);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to update this type, properties defined by the CMIS specification MUST NOT be modified.");
		}
	}
	
	/**
	 * 13 optional properties must not be changed to required
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsOptionalToRequired13() throws Exception {
		setScenario("scenarioRepositoryService15.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService15", "trade:subfolder");
		folder.getObjectTypePropertiesMap().get("trade:type").setRequired(true);
		
		try {
			repositoryService.updateType(folder);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to update this type, optional properties MUST NOT be changed to required.");
		}
	}	
	
	
	/**
	 * 14 property type and cardinality must not be changed
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsCardinality14() throws Exception {
		setScenario("scenarioRepositoryService15.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService15", "trade:subfolder");
		folder.getObjectTypePropertiesMap().get("trade:type").setCardinality(Cardinality.MULTI);
		
		try {
			repositoryService.updateType(folder);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to update this type, an existing property type's cardinality MUST NOT be changed.");
		}
	}	
	
	/**
	 * 14 property type and cardinality must not be changed
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsCardinality14bis() throws Exception {
		setScenario("scenarioRepositoryService15.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService15", "trade:subfolder");
		folder.getObjectTypePropertiesMap().get("trade:type").setPropertyType(PropertyType.DECIMAL);
		
		try {
			repositoryService.updateType(folder);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to update this type, an existing property type's data type MUST NOT be changed.");
		}
	}
	
	/**
	 * 15 open choice must not be changed from true to false
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsOpenChoice15() throws Exception {
		setScenario("scenarioRepositoryService15.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService15", "trade:subfolder");
		folder.getObjectTypePropertiesMap().get("trade:type").setOpenChoice(false);
		
		try {
			repositoryService.updateType(folder);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to update this type, 'open choice' MUST NOT change from TRUE to FALSE.");
		}
	}
	
	/**
	 * 16 choices must not be removed if open choice is false
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsChoicesRemoved16() throws Exception {
		setScenario("scenarioRepositoryService25.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService25", "trade:subfolder");
		folder.getObjectTypePropertiesMap().get("trade:type").setChoices("A;B");
		
		try {
			repositoryService.updateType(folder);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to update this type, choices MUST NOT be removed if 'open choice' is FALSE.");
		}
	}	
	

	/**
	 * 17 max length couldn't be further rectricted
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsMaxLength17() throws Exception {
		setScenario("scenarioRepositoryService24.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService24", "trade:subfolder");
		folder.getObjectTypePropertiesMap().get("trade:notes").setMaxLength(10);
		
		try {
			repositoryService.updateType(folder);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to update this type, validation constraints on existing properties MUST NOT be further restricted (max length).");
		}
	}	
	

	/**
	 * 18 min value couldn't be further rectricted
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsMinLength18() throws Exception {
		setScenario("scenarioRepositoryService19.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService19", "trade:subfolder");
		folder.getObjectTypePropertiesMap().get("trade:amount").setMinValue(10);
		
		try {
			repositoryService.updateType(folder);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to update this type, validation constraints on existing properties MUST NOT be further restricted (min value).");
		}
	}
	
	/**
	 * 19 max value couldn't be further rectricted
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsMaxValue19() throws Exception {
		setScenario("scenarioRepositoryService04.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService04", "trade:subfolder");
		folder.getObjectTypePropertiesMap().get("trade:notes").setMaxValue(10);
		
		try {
			repositoryService.updateType(folder);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to update this type, validation constraints on existing properties MUST NOT be further restricted (max value).");
		}
	}
	
	/**
	 * 19 max value couldn't be further rectricted
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsMaxValue19bis() throws Exception {
		setScenario("scenarioRepositoryService19.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService19", "trade:subfolder");
		folder.getObjectTypePropertiesMap().get("trade:amount").setMaxValue(90);
		
		try {
			repositoryService.updateType(folder);
			Assert.fail();
		} catch(CmisConstraintException e) {
			Assert.assertEquals(e.getMessage(), "Not allowed to update this type, validation constraints on existing properties MUST NOT be further restricted (max value).");
		}
	}	
	
	/**
	 * 20 property definitions must not be deleted
	 * 
	 * After updating a type with a property deleted the type should 
	 * remain the same (with the property)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeFailsProperty20() throws Exception {
		setScenario("scenarioRepositoryService04.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService04", "trade:subfolder");
		folder.removeObjectTypeProperty(folder.getObjectTypePropertiesMap().get("trade:notes"));
		
		repositoryService.updateType(folder);

		compareTable("object_type", "scenarioRepositoryService-testUpdate20.xml");
		compareTable("object_type_property", "scenarioRepositoryService-testUpdate20.xml");
	}
	
	/**
	 * 21 update type (indexable, include in super type, query name) 
	 * @throws Exception
	 */
	@Test
	public void testUpdateType21() throws Exception {
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType doc = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService01", "cmis:document");

		//all these changes will be ignored
		doc.setFulltextIndexed(false);
		doc.setIncludedInSupertypeQuery(false);
		doc.setQueryName("doc");
		repositoryService.updateType(doc);
		
		compareTable("object_type", "scenarioRepositoryService-testUpdate21.xml");
		compareTable("object_type_property", "scenarioRepositoryService-testUpdate21.xml");
	}
	
	
	/**
	 * 22 add new property
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeNewProperty22() throws Exception {
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType folder = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService01", "cmis:folder");
		ObjectTypeProperty comments = buildObjectTypeProperty(
			"cmis:comments", PropertyType.STRING, Cardinality.SINGLE, Updatability.READWRITE, false, "Folder comments");
		folder.addObjectTypeProperty(comments);
		
		repositoryService.updateType(folder);
		
		compareTable("object_type", "scenarioRepositoryService-testUpdate22.xml");
		compareTable("object_type_property", "scenarioRepositoryService-testUpdate22.xml");
	}
	
	/**
	 * 23 update query name of property
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeQueryName23() throws Exception {
		setScenario("scenarioRepositoryService04.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType doc = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService04", "trade:subfolder");
		//changes will be ignored
		ObjectTypeProperty property = doc.getObjectTypePropertiesMap().get("trade:notes");
		property.setQueryName("notes");
		
		repositoryService.updateType(doc);
		
		compareTable("object_type", "scenarioRepositoryService-testUpdate23.xml");
		compareTable("object_type_property", "scenarioRepositoryService-testUpdate23.xml");
	}
	
		
	/**
	 * 24 update property max length
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeMaxLength24() throws Exception {
		setScenario("scenarioRepositoryService24.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType doc = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService24", "trade:subfolder");
		ObjectTypeProperty property = doc.getObjectTypePropertiesMap().get("trade:notes");
		property.setMaxLength(200);
		
		repositoryService.updateType(doc);
		
		compareTable("object_type", "scenarioRepositoryService-testUpdate24.xml");
		compareTable("object_type_property", "scenarioRepositoryService-testUpdate24.xml");
	}	
	
	/**
	 * 25 update property choices
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeChoices25() throws Exception {
		setScenario("scenarioRepositoryService25.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType doc = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService25", "trade:subfolder");
		ObjectTypeProperty property = doc.getObjectTypePropertiesMap().get("trade:notes");
		property.setChoices("A;B;C;D");
		
		repositoryService.updateType(doc);
		
		compareTable("object_type", "scenarioRepositoryService-testUpdate25.xml");
		compareTable("object_type_property", "scenarioRepositoryService-testUpdate25.xml");
	}	
	
	/**
	 * 26 update property default value
	 * @throws Exception
	 */
	@Test
	public void testUpdateTypeDefaultValue26() throws Exception {
		setScenario("scenarioRepositoryService26.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		
		ObjectType doc = objTypeSelector.getObjectTypeByCmisIdWithProperties("scenarioRepositoryService26", "trade:subfolder");
		ObjectTypeProperty property = doc.getObjectTypePropertiesMap().get("trade:notes");
		property.setDefaultValue("draft");
		
		repositoryService.updateType(doc);
		
		compareTable("object_type", "scenarioRepositoryService-testUpdate26.xml");
		compareTable("object_type_property", "scenarioRepositoryService-testUpdate26.xml");
	}	
	
	
	@Test
	public void testGetPolicies() throws Exception {
		setScenario("scenarioRepositoryService30.xml", DatabaseOperation.CLEAN_INSERT);
		
		Map<ObjectType, Boolean> policies = repositoryService.getPolicies("scenarioRepositoryService30");
		
		Assert.assertEquals(1, policies.keySet().size());
		Assert.assertEquals(Boolean.FALSE, policies.values().iterator().next());
		Assert.assertEquals("trade:quotaPolicy", policies.keySet().iterator().next().getCmisId());

		//enable the policy
		setScenario("scenarioRepositoryService30-enabledPolicy.xml", DatabaseOperation.INSERT);

		policies = repositoryService.getPolicies("scenarioRepositoryService30");
		
		Assert.assertEquals(1, policies.keySet().size());
		Assert.assertEquals(Boolean.TRUE, policies.values().iterator().next());
		Assert.assertEquals("trade:quotaPolicy", policies.keySet().iterator().next().getCmisId());
	}
	
	
	@Test
	public void testGetEnabledPolicies() throws Exception {
		setScenario("scenarioRepositoryService30.xml", DatabaseOperation.CLEAN_INSERT);
	
		Set<String> enabled = repositoryService.getEnabledPolicies("scenarioRepositoryService30");
		Assert.assertEquals(0, enabled.size());
		
		//enable the policy
		setScenario("scenarioRepositoryService30-enabledPolicy.xml", DatabaseOperation.INSERT);
		
		enabled = repositoryService.getEnabledPolicies("scenarioRepositoryService30");
		Assert.assertEquals(1, enabled.size());
		Assert.assertEquals("trade:quotaPolicy" , enabled.iterator().next());
	}
	
	@Test
	public void testSetEnabledPolicies() throws Exception {
		
		setScenario("scenarioRepositoryService30.xml", DatabaseOperation.CLEAN_INSERT);
		
		//validations ---------------------------------------------------------- 
		try {
			repositoryService.setEnabledPolicies("scenarioRepositoryService30", Collections.singleton("wrong:policy"));
			Assert.fail();
		} catch (CmisInvalidArgumentException e) {
			Assert.assertEquals("Invalid Policy type (Server code not found).", e.getMessage());
		}
		
		
		try {
			repositoryService.setEnabledPolicies("scenarioRepositoryService30", Collections.singleton("A"));
			Assert.fail();
		} catch (CmisInvalidArgumentException e) {
			Assert.assertEquals("Invalid Policy type (Object Type not found).", e.getMessage());
		}
		
		// enabling trade:quotaPolicy ------------------------------------------
		
		repositoryService.setEnabledPolicies("scenarioRepositoryService30", Collections.singleton("trade:quotaPolicy"));
		//with one policy enabled 
		compareTable("repository_policy", "scenarioRepositoryService-testEnabledPolicy01.xml");

		//disabling all policies -----------------------------------------------
		
		//with no polices enabled
		repositoryService.setEnabledPolicies("scenarioRepositoryService30", Collections.EMPTY_SET);
		compareTable("repository_policy", "scenarioRepositoryService-testEnabledPolicy02.xml");


		//use of fake policies A and B
		setScenario("scenarioRepositoryService30-withFakePolicies.xml", DatabaseOperation.INSERT);
		Set<String> newPolicies = new HashSet<String>();
		newPolicies.add("A");//ID 15
		newPolicies.add("B");//ID 16
		//Add A and B
		repositoryService.setEnabledPolicies("scenarioRepositoryService30", newPolicies);
		compareTable("repository_policy", "scenarioRepositoryService-testEnabledPolicy03.xml");

		
		//remove B(16) ... keep A(15)
		repositoryService.setEnabledPolicies("scenarioRepositoryService30", Collections.singleton("A"));
		compareTable("repository_policy", "scenarioRepositoryService-testEnabledPolicy04.xml");

	}
	
	
	
	/**
	 * Create the type trade:document that extends cmis:document
	 * @return
	 */
	private ObjectType createTradeDocumentType(String repositoryId) {
		ObjectType doc = new ObjectType("trade:document");
		doc.setLocalName("ln:" + doc.getCmisId());
		doc.setLocalNamespace(Constants.NS);
		doc.setQueryName("qn:" + doc.getCmisId());
		doc.setDisplayName("dn:" + doc.getCmisId());
		doc.setBase(new ObjectType("cmis:document"));
		doc.setParent(new ObjectType("cmis:document"));
		doc.setDescription("Trade Document");
		doc.setCreatable(true);
		doc.setFileable(true);
		doc.setQueryable(true);
		doc.setControllablePolicy(true);
		doc.setControllableAcl(true);
		doc.setFulltextIndexed(true);
		doc.setIncludedInSupertypeQuery(true);
		doc.setVersionable(true);
		doc.setContentStreamAllowed(ContentStreamAllowed.REQUIRED);
		Repository repo = new Repository();
		repo.setCmisId(repositoryId);
        doc.setRepository(repo);
		doc.addObjectTypeProperty(buildObjectTypeProperty("trade:author", 			PropertyType.STRING,	Cardinality.MULTI, Updatability.READWRITE, false, "Author"));
		return doc;
	}
	
	private ObjectType createAttachmentDocumentType(String repositoryId) {
		ObjectType doc = new ObjectType("trade:attachment");
		doc.setLocalName("ln:" + doc.getCmisId());
		doc.setLocalNamespace(Constants.NS);
		doc.setQueryName("qn:" + doc.getCmisId());
		doc.setDisplayName("dn:" + doc.getCmisId());
		doc.setBase(new ObjectType("cmis:document"));
		doc.setParent(new ObjectType("trade:document"));
		doc.setDescription("Trade Document for email attackments");
		doc.setCreatable(true);
		doc.setFileable(true);
		doc.setQueryable(true);
		doc.setControllablePolicy(true);
		doc.setControllableAcl(true);
		doc.setFulltextIndexed(true);
		doc.setIncludedInSupertypeQuery(true);
		doc.setVersionable(true);
		doc.setContentStreamAllowed(ContentStreamAllowed.REQUIRED);
		Repository repo = new Repository();
		repo.setCmisId(repositoryId);
        doc.setRepository(repo);
		doc.addObjectTypeProperty(buildObjectTypeProperty("trade:emailId", 	PropertyType.ID,		Cardinality.MULTI, Updatability.READWRITE, true, "Email ID"));
		return doc;
	}
	
	private ObjectType createTradeFolderType(String repositoryId) {
		ObjectType folder = new ObjectType("trade:folder");
		
		folder.setLocalName("ln:" + folder.getCmisId());
		folder.setLocalNamespace(Constants.NS);
		folder.setQueryName("qn:"+ folder.getCmisId());
		folder.setDisplayName("dn:" + folder.getCmisId());
		folder.setBase(new ObjectType("cmis:folder"));
		folder.setParent(new ObjectType("cmis:folder"));
		folder.setDescription("Trade Folder");
		folder.setCreatable(true);
		folder.setFileable(true);
		folder.setQueryable(true);
		folder.setControllablePolicy(false);
		folder.setControllableAcl(true);
		folder.setFulltextIndexed(true);
		folder.setIncludedInSupertypeQuery(true);
		
		Repository repo = new Repository();
		repo.setCmisId(repositoryId);
        folder.setRepository(repo);

		folder.addObjectTypeProperty(buildObjectTypeProperty("trade:headingCode", 	PropertyType.ID,		Cardinality.SINGLE, Updatability.READWRITE, true, "Heading code"));
		folder.addObjectTypeProperty(buildObjectTypeProperty("trade:title",		 	PropertyType.STRING,	Cardinality.SINGLE, Updatability.READWRITE, true, "Title"));
		ObjectTypeProperty status = buildObjectTypeProperty("trade:status",			PropertyType.STRING,	Cardinality.SINGLE, Updatability.READWRITE, true, "Status");
		status.setOpenChoice(false);
		status.setChoices("ACTIVE" + Constants.CMIS_MULTIVALUE_SEP + "INACTIVE");
		folder.addObjectTypeProperty(status);
		folder.addObjectTypeProperty(buildObjectTypeProperty("trade:serviceOwner", 	PropertyType.STRING,	Cardinality.MULTI, Updatability.READWRITE, true, "Service owner"));

		return folder;
	}
	
	private ObjectType createEmailFolderType(String repositoryId) {
		ObjectType folder = new ObjectType("trade:email");
		
		folder.setLocalName("ln:"+ folder.getCmisId());
		folder.setLocalNamespace(Constants.NS);
		folder.setQueryName("qn:" + folder.getCmisId());
		folder.setDisplayName("dn:" + folder.getCmisId());
		folder.setBase(new ObjectType("cmis:folder"));
		folder.setParent(new ObjectType("cmis:folder"));
		folder.setDescription("Trade Email folder");
		folder.setCreatable(true);
		folder.setFileable(true);
		folder.setQueryable(true);
		folder.setControllablePolicy(false);
		folder.setControllableAcl(true);
		folder.setFulltextIndexed(true);
		folder.setIncludedInSupertypeQuery(true);
		
		Repository repo = new Repository();
		repo.setCmisId(repositoryId);
        folder.setRepository(repo);

		folder.addObjectTypeProperty(buildObjectTypeProperty("subject", 	PropertyType.STRING,	Cardinality.SINGLE, Updatability.READWRITE, true, "Subject"));
		folder.addObjectTypeProperty(buildObjectTypeProperty("from", 		PropertyType.STRING,	Cardinality.SINGLE, Updatability.READWRITE, true, "From"));
		folder.addObjectTypeProperty(buildObjectTypeProperty("to", 			PropertyType.STRING,	Cardinality.MULTI, Updatability.READWRITE, true, "To"));
		return folder;
	}

	
	private ObjectType createTradeRelationship(String repositoryId) {
		ObjectType relationship = new ObjectType("trade:relationship");
		
		relationship.setLocalName("ln:" + relationship.getCmisId());
		relationship.setLocalNamespace(Constants.NS);
		relationship.setQueryName("qn:"+ relationship.getCmisId());
		relationship.setDisplayName("dn:" + relationship.getCmisId());
		relationship.setBase(new ObjectType("cmis:relationship"));
		relationship.setParent(new ObjectType("cmis:relationship"));
		relationship.setDescription("Trade Relationship");
		relationship.setCreatable(true);
		relationship.setFileable(false);
		relationship.setQueryable(true);
		relationship.setControllablePolicy(false);
		relationship.setControllableAcl(true);
		relationship.setFulltextIndexed(true);
		relationship.setIncludedInSupertypeQuery(true);

		ObjectTypeRelationship otr = new ObjectTypeRelationship();
		otr.setReferencedObjectType(new ObjectType("cmis:document"));
		otr.setType(RelationshipType.SOURCE);
		relationship.addObjectTypeRelationship(otr);

		otr = new ObjectTypeRelationship();
		otr.setReferencedObjectType(new ObjectType("cmis:document"));
		otr.setType(RelationshipType.TARGET);
		relationship.addObjectTypeRelationship(otr);

		otr = new ObjectTypeRelationship();
		otr.setReferencedObjectType(new ObjectType("cmis:folder"));
		otr.setType(RelationshipType.SOURCE);
		relationship.addObjectTypeRelationship(otr);
		
		otr = new ObjectTypeRelationship();
		otr.setReferencedObjectType(new ObjectType("cmis:folder"));
		otr.setType(RelationshipType.TARGET);
		relationship.addObjectTypeRelationship(otr);
		
		Repository repo = new Repository();
		repo.setCmisId(repositoryId);
        relationship.setRepository(repo);

		relationship.addObjectTypeProperty(buildObjectTypeProperty("trade:comments", 	PropertyType.STRING,		Cardinality.SINGLE, Updatability.READWRITE, false, "Comments"));

		return relationship;
	}
	
	/**
	 * Helper method to create object type property definitions.
	 * All properties all queriables and orderables. 
	 * 
	 * @param id
	 * @param type
	 * @param cardinality
	 * @param updatability
	 * @param required
	 * @param description
	 * @return
	 */
	private ObjectTypeProperty buildObjectTypeProperty (
			String id, PropertyType type, 
			Cardinality cardinality, Updatability updatability, boolean required, String description) {
		
		ObjectTypeProperty p = new ObjectTypeProperty(id);
		p.setLocalName("ln:" + id);
		p.setLocalNamespace(Constants.NS);
		p.setQueryName("qn:" + id);
		p.setDisplayName("dn:" + id);
		p.setDescription(description);
		p.setPropertyType(type);
		p.setCardinality(cardinality);
		p.setUpdatability(updatability);
		p.setRequired(required);
		
		p.setQueryable(true);
		p.setOrderable(true);
		
		return p;
	}
	
	
	/**
	 * Create the type trade:item that extends cmis:item
	 * @return
	 */
	private ObjectType createTradeItemType(String repositoryId) {
		ObjectType doc = new ObjectType("trade:item");
		doc.setLocalName("ln:" + doc.getCmisId());
		doc.setLocalNamespace(Constants.NS);
		doc.setQueryName("qn:" + doc.getCmisId());
		doc.setDisplayName("dn:" + doc.getCmisId());
		doc.setBase(new ObjectType("cmis:item"));
		doc.setParent(new ObjectType("cmis:item"));
		doc.setDescription("Trade Item");
		doc.setCreatable(true);
		doc.setFileable(true);
		doc.setQueryable(true);
		doc.setControllablePolicy(true);
		doc.setControllableAcl(true);
		doc.setFulltextIndexed(true);
		doc.setIncludedInSupertypeQuery(true);
		Repository repo = new Repository();
		repo.setCmisId(repositoryId);
        doc.setRepository(repo);
		doc.addObjectTypeProperty(buildObjectTypeProperty("trade:author", 			PropertyType.STRING,	Cardinality.MULTI, Updatability.READWRITE, false, "Author"));
		return doc;
	}
	
	
	/**
	 * Create the type trade:review that extends cmis:secondary
	 * @return
	 */
	private ObjectType createTradeSecondaryType(String repositoryId) {
		ObjectType doc = new ObjectType("trade:review");
		doc.setLocalName("ln:" + doc.getCmisId());
		doc.setLocalNamespace(Constants.NS);
		doc.setQueryName("qn:" + doc.getCmisId());
		doc.setDisplayName("dn:" + doc.getCmisId());
		doc.setBase(new ObjectType("cmis:secondary"));
		doc.setParent(new ObjectType("cmis:secondary"));
		doc.setDescription("Trade Review");
		doc.setCreatable(false);
		doc.setFileable(false);
		doc.setQueryable(true);
		doc.setControllablePolicy(false);
		doc.setControllableAcl(false);
		doc.setFulltextIndexed(true);
		doc.setIncludedInSupertypeQuery(true);
		Repository repo = new Repository();
		repo.setCmisId(repositoryId);
        doc.setRepository(repo);
		doc.addObjectTypeProperty(buildObjectTypeProperty("trade:author", 			PropertyType.STRING,	Cardinality.MULTI, Updatability.READWRITE, false, "Author"));
		return doc;
	}
}
