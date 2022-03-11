package eu.trade.repo.dao.tests;

import static junit.framework.Assert.assertEquals;

import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.model.ObjectType;

public class ObjectTypeTest extends BaseTestClass {
	private final String TEST_CMISID = "test:type";
	
	@Test
	public void testCreate() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		
		ObjectType base = objTypeSelector.getObjectTypeByCmisId(TestConstants.TEST_REPO_2, "cmis:document");
		ObjectType parent = objTypeSelector.getObjectTypeByCmisId(TestConstants.TEST_REPO_2, "test:document");
		
		ObjectType ot = new ObjectType();
		ot.setCmisId(TEST_CMISID);
		ot.setQueryName(TEST_CMISID);
		ot.setFileable(false);
		ot.setControllableAcl(false);
		ot.setControllablePolicy(false);
		ot.setCreatable(false);
		ot.setVersionable(false);
		ot.setQueryable(false);
		ot.setIncludedInSupertypeQuery(false);
		ot.setFulltextIndexed(false);
		ot.setBase(base);
		ot.setParent(parent);
		ot.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);
		ot.setDescription("test Desc");
		ot.setDisplayName(TEST_CMISID);
		ot.setLocalName(TEST_CMISID);
		ot.setLocalNamespace("http://ec.europa.eu/trade/repo");
		ot.setRepository(repoSelector.getRepository(TestConstants.TEST_REPO_2));
		utilService.persist(ot);
		
		compareTable(
				"OBJECT_TYPE",
				"CMIS_ID = 'test:type'", 
				"cmisobjecttype-test.xml");
	}
	
	@Test
	public void testRead() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
	
		ObjectType ot = objTypeSelector.getObjectTypeByCmisId(TestConstants.TEST_REPO_2, "test:document");
    	assertEquals(true, ot.isFileable().booleanValue());
    	assertEquals(true, ot.isFulltextIndexed().booleanValue());
    	assertEquals(true, ot.isControllableAcl().booleanValue());
    	assertEquals(true, ot.isControllablePolicy().booleanValue());
    	assertEquals(true, ot.isVersionable().booleanValue());
    	assertEquals(true, ot.isQueryable().booleanValue());
    	assertEquals(true, ot.isCreatable().booleanValue());
    	assertEquals(true, ot.isIncludedInSupertypeQuery().booleanValue());
    	
    	assertEquals(110, ot.getId().intValue());
    	assertEquals("test:document", ot.getCmisId());
    	assertEquals("test:document", ot.getLocalName());
    	assertEquals("test Desc", ot.getDescription());
    	assertEquals("http://ec.europa.eu/trade/repo", ot.getLocalNamespace());
    	assertEquals("test:document", ot.getDisplayName());
    	assertEquals(ContentStreamAllowed.ALLOWED, ot.getContentStreamAllowed());
    	
    	//assertEquals("cmis:document", ot.getBase().getId());
    	//assertEquals("cmis:document", ot.getParent().getCmisId());

	}
	
	@Test
	public void testUpdate() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		
		ObjectType ot = objTypeSelector.getObjectTypeByCmisId(TestConstants.TEST_REPO_2, "test:document");
		ObjectType base = objTypeSelector.getObjectTypeByCmisId(TestConstants.TEST_REPO_2, "cmis:document");
		ObjectType parent = objTypeSelector.getObjectTypeByCmisId(TestConstants.TEST_REPO_2, "test:document");

		ot.setQueryName(TEST_CMISID);
		ot.setFileable(false);
		ot.setControllableAcl(false);
		ot.setControllablePolicy(false);
		ot.setCreatable(false);
		ot.setVersionable(false);
		ot.setQueryable(false);
		ot.setIncludedInSupertypeQuery(false);
		ot.setFulltextIndexed(false);
		ot.setBase(base);
		ot.setParent(parent);
		ot.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);
		ot.setDescription("test Desc");
		ot.setDisplayName(TEST_CMISID);
		ot.setLocalName(TEST_CMISID);
		ot.setLocalNamespace("http://ec.europa.eu/trade/repo");
		ot.setRepository(repoSelector.getRepository(TestConstants.TEST_REPO_2));
		
		utilService.merge(ot);
		ot = objTypeSelector.getObjectTypeByCmisId(TestConstants.TEST_REPO_2, "test:document");
		
    	assertEquals(false, ot.isFileable().booleanValue());
    	assertEquals(false, ot.isFulltextIndexed().booleanValue());
    	assertEquals(false, ot.isControllableAcl().booleanValue());
    	assertEquals(false, ot.isControllablePolicy().booleanValue());
    	assertEquals(false, ot.isVersionable().booleanValue());
    	assertEquals(false, ot.isQueryable().booleanValue());
    	assertEquals(false, ot.isCreatable().booleanValue());
    	assertEquals(false, ot.isIncludedInSupertypeQuery().booleanValue());
    	
    	assertEquals(110, ot.getId().intValue());
    	assertEquals(TEST_CMISID, ot.getQueryName());
    	assertEquals(TEST_CMISID, ot.getLocalName());
    	assertEquals("test Desc", ot.getDescription());
    	assertEquals("http://ec.europa.eu/trade/repo", ot.getLocalNamespace());
    	assertEquals(TEST_CMISID, ot.getDisplayName());
    	assertEquals(ContentStreamAllowed.ALLOWED, ot.getContentStreamAllowed());

	}

	@Test
	public void testDelete () throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		
		assertEquals(13, objTypeSelector.getObjectTypes(TestConstants.TEST_REPO_2, false).size());
		utilService.removeDetached(objTypeSelector.getObjectTypeByCmisId(TestConstants.TEST_REPO_2, "test:document"));
		assertEquals(12, objTypeSelector.getObjectTypes(TestConstants.TEST_REPO_2, false).size());
	}
}
