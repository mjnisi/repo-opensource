package eu.trade.repo.service.cmis;

import static eu.trade.repo.TestConstants.TEST_REPO_1;
import static eu.trade.repo.TestConstants.TEST_REPO_2;
import static eu.trade.repo.TestConstants.TEST_REPO_3;
import static eu.trade.repo.util.Constants.BASE_TYPE_CMIS_10;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.chemistry.opencmis.client.util.TypeUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.spi.RepositoryService;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.model.Repository;

public class CmisRepositoryServiceTest extends BaseTestClass {

	private static final int BASIC_TYPES_COUNT = BASE_TYPE_CMIS_10.toArray().length;

	@Autowired
	private RepositoryService cmisRepositoryService;

	@Test
	public void testGetRepositoryInfos_1() throws Exception {
		setScenario("baseRepo1.xml", DatabaseOperation.CLEAN_INSERT);
		List<RepositoryInfo> repositoryInfos = cmisRepositoryService.getRepositoryInfos(null);
		assertNotNull(repositoryInfos);
		assertEquals(1, repositoryInfos.size());
		RepositoryInfo repositoryInfo = repositoryInfos.get(0);
		check(TEST_REPO_1, null, repositoryInfo, true);
		assertNull(repositoryInfo.getAclCapabilities());
	}

	@Test
	public void testGetRepositoryInfos_2() throws Exception {
		setScenario("baseRepo2.xml", DatabaseOperation.CLEAN_INSERT);
		List<RepositoryInfo> repositoryInfos = cmisRepositoryService.getRepositoryInfos(null);
		assertNotNull(repositoryInfos);
		assertEquals(1, repositoryInfos.size());
		RepositoryInfo repositoryInfo = repositoryInfos.get(0);
		check(TEST_REPO_2, null, repositoryInfo, true);
		assertNull(repositoryInfo.getAclCapabilities());
	}

	@Test
	public void testGetRepositoryInfos_3() throws Exception {
		setScenario("baseRepo3.xml", DatabaseOperation.CLEAN_INSERT);
		List<RepositoryInfo> repositoryInfos = cmisRepositoryService.getRepositoryInfos(null);
		assertNotNull(repositoryInfos);
		assertEquals(1, repositoryInfos.size());
		RepositoryInfo repositoryInfo = repositoryInfos.get(0);
		check(TEST_REPO_3, null, repositoryInfo, true);
		assertNull(repositoryInfo.getAclCapabilities());
	}

	@Test
	public void testGetRepositoryInfo_1() throws Exception {
		setScenario("baseRepo1.xml", DatabaseOperation.CLEAN_INSERT);
		RepositoryInfo repositoryInfo = cmisRepositoryService.getRepositoryInfo(TEST_REPO_1, null);
		assertEquals("", repositoryInfo.getLatestChangeLogToken()); // Repo doesnt support change log
		assertNotNull(repositoryInfo);
		check(TEST_REPO_1, "1.ROOT", repositoryInfo, false);
		assertNull(repositoryInfo.getAclCapabilities());
	}

	@Test
	public void testGetRepositoryInfo_2() throws Exception {
		setScenario("baseRepo2.xml", DatabaseOperation.CLEAN_INSERT);
		RepositoryInfo repositoryInfo = cmisRepositoryService.getRepositoryInfo(TEST_REPO_2, null);
		assertNotNull(repositoryInfo);
		check(TEST_REPO_2, "2.ROOT", repositoryInfo, false);
		assertEquals(AclPropagation.OBJECTONLY, repositoryInfo.getAclCapabilities().getAclPropagation());
	}

	@Test
	public void testGetRepositoryInfo_3() throws Exception {
		setScenario("baseRepo3.xml", DatabaseOperation.CLEAN_INSERT);
		RepositoryInfo repositoryInfo = cmisRepositoryService.getRepositoryInfo(TEST_REPO_3, null);
		assertNotNull(repositoryInfo);
		check(TEST_REPO_3, "3.ROOT", repositoryInfo, false);
		assertEquals(AclPropagation.PROPAGATE, repositoryInfo.getAclCapabilities().getAclPropagation());
	}

	private void check(String repositoryId, String rootFolderId, RepositoryInfo repositoryInfo, boolean allRepo) {
		assertEquals(rootFolderId, repositoryInfo.getRootFolderId());
		Repository repository = repositoryService.getRepositoryById(repositoryId);
		if (allRepo) {
			assertNull(repositoryInfo.getCapabilities());
			assertNull(repositoryInfo.getRootFolderId());
		}
		else {
			assertEquals(repository.getAcl(), repositoryInfo.getCapabilities().getAclCapability());
			String rootFolder = repositoryService.getRootFolderId(repositoryId);
			assertEquals(rootFolder, repositoryInfo.getRootFolderId());
		}
	}

	@Test
	public void testGetRepositoryInfos3() throws Exception {
		setScenario("baseRepo1.xml", DatabaseOperation.CLEAN_INSERT);
		setScenario("baseRepo2.xml", DatabaseOperation.INSERT);
		setScenario("baseRepo3.xml", DatabaseOperation.INSERT);
		List<RepositoryInfo> repositoryInfos = cmisRepositoryService.getRepositoryInfos(null);
		assertNotNull(repositoryInfos);
		assertEquals(3, repositoryInfos.size());
		for (RepositoryInfo repositoryInfo : repositoryInfos) {
			check(repositoryInfo.getId(), repositoryInfo.getRootFolderId(), repositoryInfo, true);
		}
	}

	@Test
	public void testGetRepositoryInfo() throws Exception {
		setScenario("baseRepo1.xml", DatabaseOperation.CLEAN_INSERT);
		setScenario("baseRepo2.xml", DatabaseOperation.INSERT);
		setScenario("baseRepo3.xml", DatabaseOperation.INSERT);
		RepositoryInfo repositoryInfo = cmisRepositoryService.getRepositoryInfo(TEST_REPO_1, null);
		assertNotNull(repositoryInfo);
		check(repositoryInfo.getId(), repositoryInfo.getRootFolderId(), repositoryInfo, false);
	}

	@Test
	public void testGetTypeDefinition() throws Exception {
		setScenario("baseRepo1.xml", DatabaseOperation.CLEAN_INSERT);
		for (BaseTypeId baseTypeId : (BaseTypeId []) BASE_TYPE_CMIS_10.toArray()) {

			TypeDefinition typeDefinition = cmisRepositoryService.getTypeDefinition(TEST_REPO_1, baseTypeId.value(), null);
			assertNotNull(typeDefinition);
			Map<String, PropertyDefinition<?>> propertyDefinitions = typeDefinition.getPropertyDefinitions();
			assertNotNull(propertyDefinitions);
			assertFalse(propertyDefinitions.isEmpty());
			assertNotNull(propertyDefinitions.get(PropertyIds.NAME));
			assertEquals(PropertyType.STRING, propertyDefinitions.get(PropertyIds.NAME).getPropertyType());
			for (PropertyDefinition<?> propertyDefinition : propertyDefinitions.values()) {
				assertFalse(propertyDefinition.isInherited());
			}
		}
	}

	@Test
	public void testGetTypeChildren() throws Exception {
		setScenario("baseRepo1.xml", DatabaseOperation.CLEAN_INSERT);
		for (BaseTypeId baseTypeId : (BaseTypeId []) BASE_TYPE_CMIS_10.toArray()) {
			TypeDefinitionList typeDefinitionList = cmisRepositoryService.getTypeChildren(TEST_REPO_1, baseTypeId.value(), true, null, null, null);
			assertNotNull(typeDefinitionList);
			assertNotNull(typeDefinitionList.getList());
			assertTrue(typeDefinitionList.getList().isEmpty());
		}
	}

	@Test
	public void testGetTypeChildrenBasicTypes() throws Exception {
		testGetTypeChildren(false, null, BASIC_TYPES_COUNT);
		testGetTypeChildren(false, null, BigInteger.valueOf(4), BigInteger.valueOf(2), BASIC_TYPES_COUNT, BASIC_TYPES_COUNT, false);
		testGetTypeChildren(false, null, BigInteger.valueOf(2), BigInteger.valueOf(1), BASIC_TYPES_COUNT, BASIC_TYPES_COUNT, false);
	}

	@Test
	public void testGetTypeChildrenDocument() throws Exception {
		testGetTypeChildren(false, BaseTypeId.CMIS_DOCUMENT.value(), 4);
		testGetTypeChildren(false, BaseTypeId.CMIS_DOCUMENT.value(), BigInteger.valueOf(4), BigInteger.valueOf(2), 2, 4, false);
		testGetTypeChildren(false, BaseTypeId.CMIS_DOCUMENT.value(), BigInteger.valueOf(2), BigInteger.valueOf(1), 2, 4, true);
	}

	@Test
	public void testGetTypeChildrenDocumentPropertiesIncluded() throws Exception {
		testGetTypeChildren(true, BaseTypeId.CMIS_DOCUMENT.value(), 4);
		testGetTypeChildren(true, BaseTypeId.CMIS_DOCUMENT.value(), BigInteger.valueOf(4), BigInteger.valueOf(2), 2, 4, false);
		testGetTypeChildren(true, BaseTypeId.CMIS_DOCUMENT.value(), BigInteger.valueOf(2), BigInteger.valueOf(1), 2, 4, true);
	}
	
	
	/**
	 * Tests the creation of a custom type from a file (document)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateDocumentType() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "scenarioRepositoryService01");
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		resetSequence("sq_object_type_relationship");
		
		createTypeFromFile("scenarioRepositoryService01", "ComplexType.xml");
        
        compareTable("object_type", "scenarioRepositoryService-test20.xml");
        compareTable("object_type_property", "scenarioRepositoryService-test20.xml");
	}


	/**
	 * Tests the creation of a custom type from a file (folder)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateFolderType() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "scenarioRepositoryService01");
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		resetSequence("sq_object_type_relationship");
		
		createTypeFromFile("scenarioRepositoryService01", "trade_folder.xml");
		createTypeFromFile("scenarioRepositoryService01", "trade_subfolder.xml");
        
        compareTable("object_type", "scenarioRepositoryService-test21.xml");
        compareTable("object_type_property", "scenarioRepositoryService-test21.xml");
	}

	
	/**
	 * Tests the creation of a custom type from a file (relationship)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateRelatioshipType01() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "scenarioRepositoryService01");
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		resetSequence("sq_object_type_relationship");
		
		createTypeFromFile("scenarioRepositoryService01", "CrossReferenceType.xml");
        
        compareTable("object_type", "scenarioRepositoryService-test22.xml");
        compareTable("object_type_property", "scenarioRepositoryService-test22.xml");
	}
	
	
	/**
	 * Tests the creation of a custom type from a file (relationship)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateRelatioshipType02() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "scenarioRepositoryService01");
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		resetSequence("sq_object_type_relationship");
		
		createTypeFromFile("scenarioRepositoryService01", "trade_fav.xml");
        
        compareTable("object_type", "scenarioRepositoryService-test23.xml");
        compareTable("object_type_property", "scenarioRepositoryService-test23.xml");
        compareTable("object_type_relationship", "scenarioRepositoryService-test23.xml");
	}
	
	/**
	 * Tests the creation of a custom type from a file (relationship)
	 * and fails because the relationship MUST NOT be file-able
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateRelatioshipType03() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "scenarioRepositoryService01");
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		resetSequence("sq_object_type_relationship");
		
		try {
			createTypeFromFile("scenarioRepositoryService01", "invalid_relationship.xml");
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals("Not allowed to create this type, relationship subtypes MUST NOT be file-able.", e.getMessage());
		}
        
	}
	
	
	/**
	 * This test is for creating several types, it's not intented for
	 * exaustive checks, for detailed checks there are other tests
	 * 
	 * @see #testCreateDocumentType()
	 * @see #testCreateFolderType()
	 * @see #testCreateRelatioshipType01()
	 * @see #testCreateRelatioshipType02()
	 */
	@Test
	public void testCreateTypes01() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "scenarioRepositoryService01");
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		resetSequence("sq_object_type_relationship");
		
		createTypeFromFile("scenarioRepositoryService01", "MyDocType1.xml");
		createTypeFromFile("scenarioRepositoryService01", "MyDocType1.1.xml");
		createTypeFromFile("scenarioRepositoryService01", "MyDocType1.1.1.xml");
		createTypeFromFile("scenarioRepositoryService01", "MyDocType1.1.2.xml");
		createTypeFromFile("scenarioRepositoryService01", "MyDocType1.2.xml");
		
		createTypeFromFile("scenarioRepositoryService01", "MyDocType2.xml");
		createTypeFromFile("scenarioRepositoryService01", "MyDocType2.1.xml");
		createTypeFromFile("scenarioRepositoryService01", "MyDocType2.2.xml");
		createTypeFromFile("scenarioRepositoryService01", "MyDocType2.3.xml");
		createTypeFromFile("scenarioRepositoryService01", "MyDocType2.4.xml");
		createTypeFromFile("scenarioRepositoryService01", "MyDocType2.5.xml");
		createTypeFromFile("scenarioRepositoryService01", "MyDocType2.6.xml");
		createTypeFromFile("scenarioRepositoryService01", "MyDocType2.7.xml");
		createTypeFromFile("scenarioRepositoryService01", "MyDocType2.8.xml");
		createTypeFromFile("scenarioRepositoryService01", "MyDocType2.9.xml");
		
		createTypeFromFile("scenarioRepositoryService01", "ComplexType.xml");
		
		createTypeFromFile("scenarioRepositoryService01", "DocumentTopLevel.xml");
		createTypeFromFile("scenarioRepositoryService01", "DocumentLevel1.xml");
		createTypeFromFile("scenarioRepositoryService01", "DocumentLevel2.xml");
		
		createTypeFromFile("scenarioRepositoryService01", "VersionableType.xml");
		
		List<TypeDefinitionContainer> types = 
			cmisRepositoryService.getTypeDescendants("scenarioRepositoryService01", null, new BigInteger("100"), true, null);
		
		//traverse the tree
		//3 basic because the policy is filtered until it is implemented, +20 new created
		Assert.assertEquals(count(types), 4 + 20);
		
		String typesTreeDump = "<tree xmlns:cmis='http://docs.oasis-open.org/ns/cmis/core/200908/'>" + buildXml(types) + "</tree>";
		XMLUnit.setIgnoreAttributeOrder(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		Diff diff = XMLUnit.compareXML(new FileReader("src/test/resources/xmlunit/typesTree01.xml"), typesTreeDump);
		Assert.assertTrue("Differences found: " + diff, diff.similar());
	}
	
	/**
	 * This test is for creating several types, it's not intented for
	 * exaustive checks, for detailed checks there are other tests
	 * 
	 * @see #testCreateDocumentType()
	 * @see #testCreateFolderType()
	 * @see #testCreateRelatioshipType01()
	 * @see #testCreateRelatioshipType02()
	 */
	@Test
	public void testCreateTypes02() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "scenarioRepositoryService01");
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		resetSequence("sq_object_type_relationship");
		
		createTypeFromFile("scenarioRepositoryService01", "nest_document.xml");

		createTypeFromFile("scenarioRepositoryService01", "slk_caseFolder.xml");
		createTypeFromFile("scenarioRepositoryService01", "slk_publishedAttachment.xml");

		createTypeFromFile("scenarioRepositoryService01", "trade_document.xml");
		createTypeFromFile("scenarioRepositoryService01", "trade_attachment.xml");
		createTypeFromFile("scenarioRepositoryService01", "trade_email.xml");
		createTypeFromFile("scenarioRepositoryService01", "trade_fav.xml");
		createTypeFromFile("scenarioRepositoryService01", "trade_folder.xml");
		createTypeFromFile("scenarioRepositoryService01", "trade_subfolder.xml");
		createTypeFromFile("scenarioRepositoryService01", "trade_folder2.xml");

		List<TypeDefinitionContainer> types = 
			cmisRepositoryService.getTypeDescendants("scenarioRepositoryService01", null, new BigInteger("100"), true, null);
		
		//traverse the tree
		//3 basic because the policy is filtered until it is implemented, +10 new created
		Assert.assertEquals(count(types), 4 + 10);
		
		String typesTreeDump = "<tree" +
				" xmlns:cmis='http://docs.oasis-open.org/ns/cmis/core/200908/'" +
				" xmlns:slk='http://ec.europa.eu/trade/repo/sherlock/'" +
				" xmlns:nest='http://ec.europa.eu/trade/repo/nest/'" +
				" xmlns:trade='http://ec.europa.eu/trade/repo/trade/'>" + buildXml(types) + "</tree>";
		XMLUnit.setIgnoreAttributeOrder(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		Diff diff = XMLUnit.compareXML(new FileReader("src/test/resources/xmlunit/typesTree02.xml"), typesTreeDump);
		Assert.assertTrue("Differences found: " + diff, diff.similar());
	}
	
	
	/**
	 * Tests the update of a custom type from a file (folder)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdateType01() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "scenarioRepositoryService01");
		setScenario("scenarioRepositoryService01.xml", DatabaseOperation.CLEAN_INSERT);
		
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		resetSequence("sq_object_type_relationship");
		
		updateTypeFromFile("scenarioRepositoryService01", "updated/cmis_folder.xml");
        
		compareTable("object_type", "scenarioRepositoryService-testUpdate22.xml");
		compareTable("object_type_property", "scenarioRepositoryService-testUpdate22.xml");
	}
	
	
	/**
	 * Tests the update of a custom type from a file (folder)
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdateType02() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "scenarioRepositoryService04");
		setScenario("scenarioRepositoryService04.xml", DatabaseOperation.CLEAN_INSERT);
		
		resetSequence("sq_object_type");
		resetSequence("sq_object_type_property");
		resetSequence("sq_object_type_relationship");
		
		updateTypeFromFile("scenarioRepositoryService04", "updated/trade_subfolder.xml");
        
		compareTable("object_type", "scenarioRepositoryService-testUpdate23.xml");
		compareTable("object_type_property", "scenarioRepositoryService-testUpdate23.xml");
	}	
	
	
	private int count(List<TypeDefinitionContainer> in) {
		int total = 0;
		for(TypeDefinitionContainer t: in) {
			total++;
			total+=count(t.getChildren());
		}
		return total;
	}

	private String buildXml(List<TypeDefinitionContainer> in) {
		StringBuffer sb = new StringBuffer();
		for(TypeDefinitionContainer t: in) {
			sb.append("<"+t.getTypeDefinition().getId()+">");
			sb.append(buildXml(t.getChildren()));
			sb.append("</"+t.getTypeDefinition().getId()+">");
		}
		return sb.toString();
	}

	
	/**
	 * Creates a new type from the file definition specified
	 * 
	 * @param repositoryId
	 * @param filename
	 * @throws Exception
	 */
	private void createTypeFromFile(String repositoryId, String filename) throws Exception {
		InputStream in = new BufferedInputStream(new FileInputStream("src/test/resources/types/"+ filename));
        TypeDefinition type = TypeUtils.readFromXML(in);
        in.close();
        TypeDefinition out = cmisRepositoryService.createType(repositoryId, type, null);
	}
	
	/**
	 * Updates a type from the file definition specified
	 * 
	 * @param repositoryId
	 * @param filename
	 * @throws Exception
	 */
	private void updateTypeFromFile(String repositoryId, String filename) throws Exception {
		InputStream in = new BufferedInputStream(new FileInputStream("src/test/resources/types/"+ filename));
        TypeDefinition type = TypeUtils.readFromXML(in);
        in.close();
        TypeDefinition out = cmisRepositoryService.updateType(repositoryId, type, null);
	}
	
	private void testGetTypeChildren(boolean includeProperties, String typeId, int childSize) throws Exception {
		testGetTypeChildren(includeProperties, typeId, null, null, childSize, childSize, false);
	}

	private void testGetTypeChildren(boolean includeProperties, String typeId, BigInteger maxItems, BigInteger skipCount, int childSize, int numItems, boolean hasMoreItems) throws Exception {
		setScenario("baseRepo1.xml", DatabaseOperation.CLEAN_INSERT);
		setScenario("extendedTypes_01.xml", DatabaseOperation.INSERT);
		TypeDefinitionList typeDefinitionList = cmisRepositoryService.getTypeChildren(TEST_REPO_1, typeId, includeProperties, maxItems, skipCount, null);
		assertNotNull(typeDefinitionList);
		List<TypeDefinition> typeDefinitions = typeDefinitionList.getList();
		assertNotNull(typeDefinitions);
		assertEquals(childSize, typeDefinitions.size());
		assertEquals(numItems, typeDefinitionList.getNumItems().intValue());
		assertEquals(hasMoreItems, typeDefinitionList.hasMoreItems());
		for (TypeDefinition typeDefinition : typeDefinitions) {
			assertEquals(includeProperties, typeDefinition.getPropertyDefinitions() != null);
		}
	}
}
