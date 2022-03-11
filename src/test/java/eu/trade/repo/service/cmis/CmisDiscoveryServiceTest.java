package eu.trade.repo.service.cmis;


import static eu.trade.repo.util.Constants.CMIS_SCORE_PROP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CmisExtensionElementImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ExtensionDataImpl;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.query.QueryResult;
import eu.trade.repo.util.Constants;

public class CmisDiscoveryServiceTest extends BaseTestClass {
	@Override
	@Before
	public void initUser() {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "nest_dev");
	}

	@Test
	@Ignore
	public void testFullTextQueryScores() throws Exception {

		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TestConstants.TEST_REPO_2);
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml", "scenarioQueryServiceIndex.xml");

		repoSelector.getRepository("test_repo_02").setPwcSearchable(true);
		repoSelector.getRepository("test_repo_02").setAllVersionsSearchable(true);
		repoSelector.getRepository("test_repo_02").setQuery(CapabilityQuery.BOTHCOMBINED);

		//NO SCORE
		ObjectList objectList = cmisDiscoveryService.query("test_repo_02", "select cmis:name from test:subfolder where contains('foo man choo')", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(Long.MAX_VALUE), BigInteger.ZERO, null);
		assertTrue(objectList.getNumItems().intValue() == 2);

		//SCORE, NO ALIAS, NO ORDERING
		objectList = cmisDiscoveryService.query("test_repo_02", "select cmis:name, SCORE() from test:subfolder where contains('foo man choo')", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(Long.MAX_VALUE), BigInteger.ZERO, null);
		assertTrue(objectList.getNumItems().intValue() == 2);
		assertTrue(objectList.getObjects().get(0).getProperties().getProperties().get(CMIS_SCORE_PROP) != null);
		assertTrue(objectList.getObjects().get(1).getProperties().getProperties().get(CMIS_SCORE_PROP) != null);

		//SCORE, ALIAS, ORDER BY SCORE ASC
		objectList = cmisDiscoveryService.query("test_repo_02", "select cmis:name, SCORE() as score from test:subfolder where contains('foo man choo') order by score asc", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(Long.MAX_VALUE), BigInteger.ZERO, null);
		assertTrue(objectList.getNumItems().intValue() == 2);
		double score1 = ((java.math.BigDecimal) objectList.getObjects().get(0).getProperties().getProperties().get(CMIS_SCORE_PROP).getValues().get(0)).doubleValue();
		double score2 = ((java.math.BigDecimal) objectList.getObjects().get(0).getProperties().getProperties().get(CMIS_SCORE_PROP).getValues().get(0)).doubleValue();
		assert score2>=score1;

		//SCORE, ALIAS, ORDER BY SCORE DESC
		objectList = cmisDiscoveryService.query("test_repo_02", "select cmis:name, SCORE() as score from test:subfolder where contains('foo man choo') order by score desc", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(Long.MAX_VALUE), BigInteger.ZERO, null);
		assertTrue(objectList.getNumItems().intValue() == 2);
		score1 = ((java.math.BigDecimal) objectList.getObjects().get(0).getProperties().getProperties().get(CMIS_SCORE_PROP).getValues().get(0)).doubleValue();
		score2 = ((java.math.BigDecimal) objectList.getObjects().get(0).getProperties().getProperties().get(CMIS_SCORE_PROP).getValues().get(0)).doubleValue();
		assert score1>=score2;

		//SCORE, ALIAS, ORDER BY SCORE DESC, cmis:name asc
		objectList = cmisDiscoveryService.query("test_repo_02", "select cmis:created, SCORE() as score from test:subfolder where contains('foo man choo') and (cmis:createdBy='Allen' or cmis:createdBy='Jorge') order by score desc, cmis:createdBy asc", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(Long.MAX_VALUE), BigInteger.ZERO, null);
		assertTrue(objectList.getNumItems().intValue() == 2);
		score1 = ((java.math.BigDecimal) objectList.getObjects().get(0).getProperties().getProperties().get(CMIS_SCORE_PROP).getValues().get(0)).doubleValue();
		score2 = ((java.math.BigDecimal) objectList.getObjects().get(0).getProperties().getProperties().get(CMIS_SCORE_PROP).getValues().get(0)).doubleValue();
		assert score1>=score2;
	}

	@Test
	public void testQueryAllCmisDocuments() throws Exception {
		setScenario("scenario05.xml", DatabaseOperation.CLEAN_INSERT);
		ObjectList objectList = cmisDiscoveryService.query("nest_dev", "select * from cmis:document", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(350), BigInteger.valueOf(300), null);
		assertEquals(33, objectList.getObjects().size()); //331 x cmis:document + 2 x nest:document
	}

	@Test
	public void testQueryDocumentsWhereBaseTypeIsDocument() throws Exception {
		setScenario("scenario05.xml", DatabaseOperation.CLEAN_INSERT);
		ObjectList objectList = cmisDiscoveryService.query("nest_dev", "select * from cmis:document where cmis:baseTypeId = 'cmis:document'", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(Long.MAX_VALUE), BigInteger.ZERO, null);
		assertFalse(objectList.hasMoreItems());
		assertEquals(333, objectList.getObjects().size());
	}

	@Test
	public void testQueryDocumentsWhereBaseTypeIsDocument_limit100() throws Exception {
		setScenario("scenario05.xml", DatabaseOperation.CLEAN_INSERT);
		ObjectList objectList = cmisDiscoveryService.query("nest_dev", "select * from cmis:document where cmis:baseTypeId = 'cmis:document'", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(100), BigInteger.ZERO, null);
		assertTrue(objectList.hasMoreItems());
		assertEquals(100, objectList.getObjects().size());
	}

	@Test
	public void testQueryDocumentsWhereBaseTypeIsDocumentWithMaxItemLimit() throws Exception {
		setScenario("scenario05.xml", DatabaseOperation.CLEAN_INSERT);
		ObjectList objectList = cmisDiscoveryService.query("nest_dev", "select * from cmis:document where cmis:baseTypeId = 'cmis:document'", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(50), BigInteger.ZERO, null);
		assertTrue(objectList.hasMoreItems());
		assertEquals(333, objectList.getNumItems().intValue());
		assertEquals(50, objectList.getObjects().size());
	}

	@Test
	public void testQueryDocumentsWhereBaseTypeIsDocumentWithOffset() throws Exception {
		setScenario("scenario05.xml", DatabaseOperation.CLEAN_INSERT);
		ObjectList objectList = cmisDiscoveryService.query("nest_dev", "select * from cmis:document where cmis:baseTypeId = 'cmis:document'", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(350), BigInteger.valueOf(300), null);
		assertFalse(objectList.hasMoreItems());
		assertEquals(333, objectList.getNumItems().intValue());
		assertEquals(33, objectList.getObjects().size());
	}

	@Test
	public void testQueryDocumentsWhereBaseTypeIsDocumentWtthMaxItemLimit() throws Exception {
		setScenario("scenario05.xml", DatabaseOperation.CLEAN_INSERT);
		ObjectList objectList = cmisDiscoveryService.query("nest_dev", "select * from cmis:document where cmis:baseTypeId = 'cmis:document'", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(50), BigInteger.valueOf(300), null);
		assertFalse(objectList.hasMoreItems());
		assertEquals(333, objectList.getNumItems().intValue());
		assertEquals(33, objectList.getObjects().size());
	}

	@Test
	public void testQueryAllCmisDocumentsWhereAuthorIs() throws Exception {
		setScenario("scenario05.xml", DatabaseOperation.CLEAN_INSERT);
		

		ObjectList objectList = cmisDiscoveryService.query("nest_dev", "select * from cmis:document where cmis:createdBy = '/admin'", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(350), BigInteger.ZERO, getExtensionData());
		assertFalse(objectList.hasMoreItems());
		assertEquals(333, objectList.getObjects().size());
	}

	@Test
	public void testQueryAndCondition() throws Exception {
		setScenario("scenario05.xml", DatabaseOperation.CLEAN_INSERT);
		ObjectList objectList = cmisDiscoveryService.query("nest_dev", "select * from cmis:document where cmis:createdBy = '/admin' and cmis:objectTypeId = 'nest:document'", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(350), BigInteger.ZERO, getExtensionData());
		assertFalse(objectList.hasMoreItems());
		assertEquals(2, objectList.getObjects().size());
	}
	
	@Test
	public void testQueryReturnType() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "test_repo_02");
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml");
		ObjectList objectList = cmisDiscoveryService.query("test_repo_02", "select * from test:subfolder where cmis:creationDate > '2010-04-01T12:14:59.000+01:00'", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(350), BigInteger.ZERO, getExtensionData());
		ObjectData obj = objectList.getObjects().get(0);
		assertFalse(objectList.hasMoreItems());
		assertEquals(1, objectList.getObjects().size());
	}

	@Test
	public void testQueryAndConditionWithOffset() throws Exception {
		setScenario("scenario05.xml", DatabaseOperation.CLEAN_INSERT);
		ObjectList objectList = cmisDiscoveryService.query("nest_dev", "select * from cmis:document where cmis:createdBy = '/admin' and cmis:objectTypeId = 'nest:document'", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(350), BigInteger.ONE, getExtensionData());
		assertFalse(objectList.hasMoreItems());
		assertEquals(2, objectList.getNumItems().intValue());
		assertEquals(1, objectList.getObjects().size());
	}

	@Test
	public void testQueryOrCondition() throws Exception {
		setScenario("scenario05.xml", DatabaseOperation.CLEAN_INSERT);
		ObjectList objectList = cmisDiscoveryService.query("nest_dev", "select * from cmis:document where cmis:createdBy = '/admin' or cmis:objectTypeId = 'cmis:nest'", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(350), BigInteger.ZERO, getExtensionData());
		// hasMoreItems: TRUE if the Repository contains additional items AFTER those contained in the response.
		// FALSE otherwise. If TRUE, a request with a larger skipCount or
		// larger maxItems is expected to return additional results (unless the contents of the repository has changed)
		assertFalse(objectList.hasMoreItems());
		assertEquals(333, objectList.getObjects().size());
	}

	@Test
	public void testQueryNestedCondition() throws Exception {
		setScenario("scenario05.xml", DatabaseOperation.CLEAN_INSERT);
		ObjectList objectList = cmisDiscoveryService.query("nest_dev", "select * from cmis:document where cmis:createdBy = '/admin' and (cmis:objectTypeId = 'nest:document' or (cmis:name = 'TradeDocument1' or cmis:name = 'TradeDocument2'))", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(350), BigInteger.ZERO, getExtensionData());
		assertFalse(objectList.hasMoreItems());
		assertEquals(4, objectList.getObjects().size());
	}

	@Test
	public void testQueryByName() throws Exception {
		setScenario("scenario05.xml", DatabaseOperation.CLEAN_INSERT);
		ObjectList objectList = cmisDiscoveryService.query("nest_dev", "select * from cmis:document where cmis:name = 'TradeDocument1' or cmis:name = 'TradeDocument2'", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(350), BigInteger.ZERO, getExtensionData());
		assertFalse(objectList.hasMoreItems());
		assertEquals(2, objectList.getObjects().size());
	}

	@Test
	public void testQueryByContentStreamFileName() throws Exception {
		setScenario("scenario05.xml", DatabaseOperation.CLEAN_INSERT);
		ObjectList objectList = cmisDiscoveryService.query("nest_dev", "select * from cmis:document where cmis:contentStreamFileName = 'tomasz_test6.txt'", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(350), BigInteger.ZERO, getExtensionData());
		assertFalse(objectList.hasMoreItems());
		assertEquals(1, objectList.getObjects().size());
	}

	@Test
	public void testQueryAllCmisDocumentsWhereTypeIsNest() throws Exception {
		setScenario("scenario05.xml", DatabaseOperation.CLEAN_INSERT);
		ObjectList objectList = cmisDiscoveryService.query("nest_dev", "select * from cmis:document where cmis:objectTypeId = 'nest:document'", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(350), BigInteger.ZERO, null);
		assertFalse(objectList.hasMoreItems());
		assertEquals(2, objectList.getObjects().size());
	}

	@Test
	public void testQueryAllNestDocument() throws Exception {
		setScenario("scenario05.xml", DatabaseOperation.CLEAN_INSERT);
		ObjectList objectList = cmisDiscoveryService.query("nest_dev", "select * from nest:document", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(350), BigInteger.ZERO, null);
		assertFalse(objectList.hasMoreItems());
		assertEquals(2, objectList.getObjects().size());
	}

	@Test
	public void testQuery_subfolderType() throws Exception {
		setScenario("scenario05.xml", DatabaseOperation.CLEAN_INSERT);
		ObjectList objectList = cmisDiscoveryService.query("nest_dev", "select * from cmis:folder", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(350), BigInteger.ZERO, null);
		assertFalse(objectList.hasMoreItems());
		assertEquals(319, objectList.getObjects().size());
	}

	@Test (expected = Exception.class)
	public void testQueryWhereCmisId() throws Exception {
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenario02.xml");
		ObjectList objectList = cmisDiscoveryService.query("test_repo_02", "select * from cmis:document where", true, false, IncludeRelationships.NONE, "", BigInteger.valueOf(40), BigInteger.ZERO, null);
		assertFalse(objectList.hasMoreItems());
		List<ObjectData> objects = objectList.getObjects();
		assertEquals(5, objects.size());
	}
	
	private ExtensionsData getExtensionData(){
		ExtensionsData data = new ExtensionDataImpl();
		data.setExtensions(new ArrayList<CmisExtensionElement>());
		data.getExtensions().add(new CmisExtensionElementImpl(null, Constants.QUERY_CASE_SENSITIVE, null, "true"));
		return data;
	}
}
