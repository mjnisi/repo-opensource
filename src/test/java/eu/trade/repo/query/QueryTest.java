package eu.trade.repo.query;

import static eu.trade.repo.TestConstants.TEST_REPO_2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.RecognitionException;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.server.support.query.CmisQueryException;
import org.apache.commons.collections.CollectionUtils;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.delegates.JDBCLobDelegate;
import eu.trade.repo.id.IDGenerator;
import eu.trade.repo.index.IndexPartOperator;
import eu.trade.repo.index.IndexSynchronizer;
import eu.trade.repo.index.impl.IndexTaskImpl;
import eu.trade.repo.index.model.IndexOperation;
import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.Property;
import eu.trade.repo.model.Repository;
import eu.trade.repo.util.Constants;

@Transactional
public class QueryTest extends BaseTestClass {

	private static final boolean NORMALIZED_QUERY_FALSE = false;

	// setup/teardown
	private static final String DOC_1 = "pdf/indexQueryTestFile1.txt";
	private static final String DOC_2 = "pdf/indexQueryTestFile2.txt";
	private static final String DOC_3 = "pdf/indexQueryTestFile3.txt";
	private static final String DOC_4 = "pdf/indexQueryTestFile4.txt";
	private static final String DOC_5 = "pdf/indexQueryTestFile5.txt";

	private static final String PDF_DOC = "pdf/pdf_one.pdf";

	@Autowired
	private IndexPartOperator contentIndexOperator;
	@Autowired
	private IndexSynchronizer indexSynchronizer;

	@Autowired
	private JDBCLobDelegate lobDelegate;
	
	@Autowired
	private IDGenerator mockGenerator;
	
	@Autowired
	@Qualifier("concreteQuery")
	private ConcreteQuery concreteQuery;

	@Override
	@Before
	public void initUser() {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TestConstants.TEST_REPO_2);
	}
	
	@Test
	public void fulltextAndPropertyQueries() throws Exception {
		
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioFulltextAndPropertyQuery.xml");
		
		repoSelector.getRepository("test_repo_02").setPwcSearchable(true);
		repoSelector.getRepository("test_repo_02").setAllVersionsSearchable(true);
		repoSelector.getRepository("test_repo_02").setQuery(CapabilityQuery.BOTHCOMBINED);
		
		List<Integer> qr = null;
		
		qr = executeQuery("select cmis:name, SCORE() from cmis:document where contains('Comment')", "test_repo_02");
		
		assertTrue(qr.size() == 3 && qr.contains(206)
				                  && qr.contains(207)
				                  && qr.contains(208));
		
		qr = executeQuery("select cmis:name from cmis:document where cmis:versionLabel='Version1'", "test_repo_02");
		
		assertTrue(qr.size() == 3 && qr.contains(207)
				                  && qr.contains(208)
				                  && qr.contains(209));
		
		qr = executeQuery("select cmis:name, SCORE() from cmis:document where contains('Comment') or cmis:versionLabel='Version1'", "test_repo_02");
		
		assertTrue(qr.size() == 4 && qr.contains(206)
				                  && qr.contains(207)
				                  && qr.contains(208)
				                  && qr.contains(209));
		
	}
	

	@Test
	@Ignore
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public void testQueries() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml", "scenarioQueryServiceIndex.xml");

		repoSelector.getRepository("test_repo_02").setPwcSearchable(true);
		repoSelector.getRepository("test_repo_02").setAllVersionsSearchable(true);
		repoSelector.getRepository("test_repo_02").setQuery(CapabilityQuery.BOTHCOMBINED);
		
		/*boolean searchAllVersions = true;
		int maxItems = -1;
		int skipCount = 0;
		
		String queryStr = "select cmis:name from cmis:document where contains('foo')";
		
		QueryResult qr = query.executeQuery(queryStr, "test_repo_02", searchAllVersions, maxItems, skipCount, NORMALIZED_QUERY_FALSE);
		
		setUser("test", "test", TestConstants.TEST_REPO_2);
		
		qr = query.executeQuery(queryStr, "test_repo_02", searchAllVersions, maxItems, skipCount, NORMALIZED_QUERY_FALSE);*/

		//"select cmis:createdBy from test:subfolder where cmis:createdBy='Jorge'"
		//select cmis:name, SCORE() as score from test:subfolder where contains('foo man choo') order by score asc
		
		/**
		 * 
		 * <property ID="104" OBJECT_ID="111" OBJECT_TYPE_PROPERTY_ID="50" VALUE="Allen" NUMERIC_VALUE="0" /> <!-- cmis:createdBy -->
  			<property ID="105" OBJECT_ID="111" OBJECT_TYPE_PROPERTY_ID="51" VALUE="3.0" NUMERIC_VALUE="3.0" /><!-- cmis:numberField -->
		 * 
		 */
		
		//"select cmis:numberField from test:subfolder where cmis:createdBy='Jorge' and cmis:numberField=3.0"
		
		//String queryStr = "select * from test:subfolder where cmis:createdBy='A\\\\\\'l\\'l\\\\e\\\\\\\\n'";
		//String queryStr = "select * from test:subfolder where cmis:createdBy='A\\\\\\\\l\\'\\'l_e%n'";
		
		//ORDER_BY, OR_PROPERTY...
		String queryStr = "select cmis:name, SCORE() from cmis:document where contains('foo')";
		
		boolean searchAllVersions = true;
		int maxItems = -1;
		int skipCount = 0;

		boolean doQueries = true;

		while(doQueries) {
			try {
				QueryResult qr = query.executeQuery(queryStr, "test_repo_02", searchAllVersions, maxItems, skipCount, NORMALIZED_QUERY_FALSE);
				String sqlQuery = qr.getQuery();
				System.out.println(sqlQuery);
				System.out.println(qr);
			} catch(Exception e) {
				System.out.println(e);
			}
		}

	}
	
	@Test
	public void testCMIStoSQLEscapeConversion() throws Exception {
		
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService8.xml");
		
		String queryStr = "select * from test:subfolder where cmis:createdBy='A\\\\\\'l\\'l\\\\e\\\\\\\\n'";
		
		QueryResult qr = query.executeQuery(queryStr, "test_repo_02", true, -1, 0, NORMALIZED_QUERY_FALSE);
		
		assertEquals(1, qr.getResult().size());
		assertEquals(110, qr.getResult().iterator().next().getId().intValue());
		
		queryStr = "select * from test:subfolder where cmis:createdBy='A\\\\\\\\l\\'\\'l_e%n'";
		
		qr = query.executeQuery(queryStr, "test_repo_02", true, -1, 0, NORMALIZED_QUERY_FALSE);
		
		assertEquals(1, qr.getResult().size());
		assertEquals(116, qr.getResult().iterator().next().getId().intValue());
		
		queryStr = "select * from test:subfolder where cmis:createdBy like 'Allen\\'s 20_3%'";
		List<Integer> ids = executeQuery(queryStr, "test_repo_02");
		
		assertEquals(2, ids.size());
		assertTrue(ids.contains(117) && ids.contains(118));
		
		queryStr = "select * from test:subfolder where cmis:createdBy like 'Allen\\'s 20\\_3%'";
		ids = executeQuery(queryStr, "test_repo_02");
		
		assertEquals(1, ids.size());
		assertTrue(ids.contains(117));
		
		queryStr = "select * from test:subfolder where cmis:createdBy like 'Allen\\'s 20_3\\%%'";
		ids = executeQuery(queryStr, "test_repo_02");
		
		assertEquals(1, ids.size());
		assertTrue(ids.contains(117));
		
		queryStr = "select * from test:subfolder where cmis:createdBy like 'Allen\\'s 20.3%'";
		ids = executeQuery(queryStr, "test_repo_02");
		
		assertEquals(1, ids.size());
		assertTrue(ids.contains(118));
		
		queryStr = "select * from test:subfolder where cmis:createdBy like 'Allen\\'s 20.3\\%'";
		ids = executeQuery(queryStr, "test_repo_02");
		
		assertEquals(0, ids.size());
		
		//test backslashes in saved value
		
		queryStr = "select * from test:subfolder where cmis:createdBy='Allen\\\\\\\\\\'s 20.3 euros'";
		ids = executeQuery(queryStr, "test_repo_02"); //--> 119
		
		queryStr = "select * from test:subfolder where cmis:createdBy like 'Allen\\\\\\\\\\'s 20.3 euros'";
		ids = executeQuery(queryStr, "test_repo_02");
	}
	
	@Test
	@Ignore
	public void testCreateAndQueryObjectWithSpecialChars() throws Exception {
		
		setScenario("scenarioCreateObject.xml", DatabaseOperation.CLEAN_INSERT);
		mockGenerator.reset();
		
		repoSelector.getRepository("test_repo_02").setPwcSearchable(true);
		repoSelector.getRepository("test_repo_02").setAllVersionsSearchable(true);
		repoSelector.getRepository("test_repo_02").setQuery(CapabilityQuery.BOTHCOMBINED);
		
		CMISObject rootFolder = cmisObjectSelector.loadCMISObject("/");

		assertEquals(0, rootFolder.getChildren().size());
		assertNotNull(rootFolder);

		//FOLDER, child of root
		CMISObject folder = new CMISObject();
		folder.setCmisObjectId("test folder random id");
		folder.setObjectType(objTypeSelector.getObjectTypeByCmisId(TEST_REPO_2, "test:folder"));
		folder.addProperty(getTestProperty("A\\\\l\\%l'en", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		folder.addProperty(getTestProperty("test:folder", TEST_REPO_2, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));
		folder.addParent(rootFolder);

		int objectId = objectService.createObject(TEST_REPO_2, folder, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_FOLDER).getId();
		
		List<Integer> ids = executeQuery("select * from test:folder where cmis:createdBy like 'A\\\\\\\\\\\\\\\\l\\\\\\\\\\%%'", "test_repo_02");
		
		assertEquals(objectId, ids.get(0).intValue());
	}
	
	@Test
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public void testScore() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml", "scenarioQueryServiceIndex.xml");

		repoSelector.getRepository("test_repo_02").setPwcSearchable(true);
		repoSelector.getRepository("test_repo_02").setAllVersionsSearchable(true);
		repoSelector.getRepository("test_repo_02").setQuery(CapabilityQuery.BOTHCOMBINED);

		//TODO: validations for column/alias, orderby, fulltext combinations

		QueryResult results = query.executeQuery("select cmis:name, SCORE() as score from test:subfolder where cmis:createdBy like 'J%' and contains('foo man choo') order by score asc", "test_repo_02", true, -1, 0, NORMALIZED_QUERY_FALSE);
		//114

		results = query.executeQuery("select cmis:name, SCORE() as score from test:subfolder where contains('foo man choo') order by score desc", "test_repo_02", true, -1, 0, NORMALIZED_QUERY_FALSE);
		//111, 114
		
	}

	@Test
	public void testMetadataScore() throws Exception {
		
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioFulltextMetadataScore.xml");
		
		repoSelector.getRepository("test_repo_02").setPwcSearchable(true);
		repoSelector.getRepository("test_repo_02").setAllVersionsSearchable(true);
		repoSelector.getRepository("test_repo_02").setQuery(CapabilityQuery.BOTHCOMBINED);
		
		QueryResult results = query.executeQuery("select cmis:objectId, SCORE() from cmis:document where contains('Allen') and contains('t2') order by SEARCH_SCORE desc", "test_repo_02", true, -1, 0, NORMALIZED_QUERY_FALSE);
		
		//-- START
		
		query.executeQuery("select * from cmis:document", "test_repo_02", true, 4, 2, NORMALIZED_QUERY_FALSE);
		query.queryCount("select * from cmis:document", "test_repo_02", true, NORMALIZED_QUERY_FALSE);
		query.executeQuery("select * from cmis:document", "test_repo_02", true, -1, 0, NORMALIZED_QUERY_FALSE);
		
		//-- END
		
		//score, order by score
		//QueryResult results = query.executeQuery("select cmis:name, SCORE() as score from cmis:document where contains('Allen') order by score asc", "test_repo_02", true, -1, 0, NORMALIZED_QUERY_FALSE);
		
		//filter, order by score
		query.executeQuery("select cmis:name, SCORE() as score from cmis:document where cmis:contentStreamId in ('208', '209') and contains('Allen') order by score asc", "test_repo_02", true, -1, 0, NORMALIZED_QUERY_FALSE);
		
		//filter, order by filter
		query.executeQuery("select * from cmis:document where cmis:contentStreamId in ('208', '209') and cmis:name like 'd%' order by cmis:name asc, cmis:contentStreamId desc", "test_repo_02", true, -1, 0, NORMALIZED_QUERY_FALSE);
		
		//score/filter order by score, filter
		query.executeQuery("select cmis:name, SCORE() from cmis:document where cmis:contentStreamId in ('208', '209') and contains('Allen') order by cmis:contentStreamId asc, SEARCH_SCORE desc", "test_repo_02", true, -1, 0, NORMALIZED_QUERY_FALSE);
		
		//score/filter order by score, filter, paging
		query.executeQuery("select cmis:name, SCORE() from cmis:document where cmis:contentStreamId in ('208', '209') and contains('Allen') order by cmis:contentStreamId asc, SEARCH_SCORE desc", "test_repo_02", true, 10, 1, NORMALIZED_QUERY_FALSE);
		
		//score/filter order by score, filter, paging
		query.executeQuery("select cmis:name, SCORE() from cmis:document where cmis:contentStreamId in ('208', '209') and contains('ll') order by cmis:contentStreamId asc, SEARCH_SCORE desc", "test_repo_02", true, 10, 1, NORMALIZED_QUERY_FALSE);
		
	}
	
	@Test
	public void testNot() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml", "scenarioQueryServiceIndex.xml");

		repoSelector.getRepository("test_repo_02").setPwcSearchable(true);
		repoSelector.getRepository("test_repo_02").setAllVersionsSearchable(true);

		/*InputStream in = ClassLoader.getSystemResourceAsStream(DOC_1);
		lobDelegate.saveStream(111, in, in.available());
		indexHelper.indexFromScratch(new IndexTaskImpl(1, 111, IndexTaskImpl.Operation.INDEX, indexSynchronizer, DOC_1.substring(4)));

		in = ClassLoader.getSystemResourceAsStream(DOC_2);
		lobDelegate.saveStream(112, in, in.available());
		indexHelper.indexFromScratch(new IndexTaskImpl(1, 112, IndexTaskImpl.Operation.INDEX, indexSynchronizer, DOC_2.substring(4)));

		in = ClassLoader.getSystemResourceAsStream(DOC_3);
		lobDelegate.saveStream(113, in, in.available());
		indexHelper.indexFromScratch(new IndexTaskImpl(1, 113, IndexTaskImpl.Operation.INDEX, indexSynchronizer, DOC_3.substring(4)));

		in = ClassLoader.getSystemResourceAsStream(DOC_4);
		lobDelegate.saveStream(114, in, in.available());
		indexHelper.indexFromScratch(new IndexTaskImpl(1, 114, IndexTaskImpl.Operation.INDEX, indexSynchronizer, DOC_4.substring(4)));*/

		///FULL-TEXT WITH COMBINATIONS

		List<Integer> ids = executeQuery("select * from test:subfolder where cmis:createdBy = 'Jorge' and contains('foo man choo') and cmis:numberField is not null", "test_repo_02");
		assertTrue(ids.size() == 1 && ids.contains(114));

		ids = executeQuery("select * from test:subfolder where not (not cmis:createdBy = 'Jorge' or not contains('foo man choo')) and cmis:numberField is not null", "test_repo_02");
		assertTrue(ids.size() == 1 && ids.contains(114));

		ids = executeQuery("select * from test:subfolder where not (cmis:createdBy = 'Jorge' and contains('foo man choo')) and cmis:numberField is not null", "test_repo_02");
		assertTrue(ids.size() == 6 && !ids.contains(114));

		//IN

		ids = executeQuery("select * from test:subfolder where cmis:createdBy in('Jorge')", "test_repo_02");

		assertTrue(ids.size() == 4 && ids.contains(112)
				&& ids.contains(113)
				&& ids.contains(114)
				&& ids.contains(115));

		assertTrue(CollectionUtils.isEqualCollection(ids, executeQuery("select * from test:subfolder where not cmis:createdBy not in('Jorge')", "test_repo_02")));

		ids = executeQuery("select * from test:subfolder where cmis:createdBy not in('Jorge')", "test_repo_02");

		assertTrue(ids.size() == 3 && ids.contains(110)
		                           && ids.contains(111)
		                           && ids.contains(102));

		assertTrue(CollectionUtils.isEqualCollection(ids, executeQuery("select * from test:subfolder where not cmis:createdBy in('Jorge')", "test_repo_02")));

		//FOLDER
		//TREE
		//NULL
		//QUANTIFIED
	}

	@Test
	public void testNull() throws Exception {

		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TestConstants.TEST_REPO_2);
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService6.xml");

		List<Integer> result = executeQuery("SELECT * FROM test:subfolder", TestConstants.TEST_REPO_2); //[111, 102, 110, 115, 114, 113]
		assertEquals(6, result.size());

		result = executeQuery("SELECT * FROM test:subfolder where cmis:numberField is not null", TestConstants.TEST_REPO_2); //[113]
		assertTrue(result.contains(113));

		result = executeQuery("SELECT * FROM test:subfolder where test:prop111 is null", TestConstants.TEST_REPO_2);
		assertEquals(5, result.size());
		assertTrue(!result.contains(113));

		repoSelector.getRepository("test_repo_02").setAcl(CapabilityAcl.DISCOVER);
		setUser(TestConstants.TEST2_USER, TestConstants.TEST2_PWD, TestConstants.TEST_REPO_2);

		result = executeQuery("SELECT * FROM test:subfolder", TestConstants.TEST_REPO_2); //[111, 115, 113]
		assertEquals(3, result.size());
	}

	@Test
	public void testQuantifiedComparison() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService7.xml");
		setUser(TestConstants.TEST2_USER, TestConstants.TEST2_USER, TestConstants.TEST_REPO_2);
		repoSelector.getRepository("test_repo_02").setPwcSearchable(true);
		repoSelector.getRepository("test_repo_02").setAllVersionsSearchable(true);

		List<Integer> result = null; //query.executeQuery("SELECT * FROM cmis:folder where cmis:allowedChildObjectTypeIds = '2'", TestConstants.TEST_REPO_2);

		result = executeQuery("SELECT * FROM cmis:folder where '2' = any cmis:allowedChildObjectTypeIds", TestConstants.TEST_REPO_2);

		assertEquals(3, result.size());
		assertTrue(result.contains(110));
		assertTrue(result.contains(111));
		assertTrue(result.contains(112));

		result = executeQuery("SELECT * FROM cmis:folder where '24' = any cmis:allowedChildObjectTypeIds", TestConstants.TEST_REPO_2);

		assertEquals(1, result.size());
		assertTrue(result.contains(112));

		result = executeQuery("SELECT * FROM cmis:folder where '21' = any cmis:allowedChildObjectTypeIds", TestConstants.TEST_REPO_2);

		assertEquals(2, result.size());
		assertTrue(result.contains(110));
		assertTrue(result.contains(111));

		result = executeQuery("SELECT * FROM cmis:folder where '22' = any cmis:allowedChildObjectTypeIds", TestConstants.TEST_REPO_2);

		assertEquals(1, result.size());
		assertTrue(result.contains(110));

		// combinations
		result = executeQuery("SELECT * FROM cmis:folder where '22' = any cmis:allowedChildObjectTypeIds or 'not_there' = any cmis:allowedChildObjectTypeIds", TestConstants.TEST_REPO_2);

		assertEquals(1, result.size());
		assertTrue(result.contains(110));

		result = executeQuery("SELECT * FROM cmis:folder where '23' = any cmis:allowedChildObjectTypeIds and '21' = any cmis:allowedChildObjectTypeIds", TestConstants.TEST_REPO_2);

		assertEquals(1, result.size());
		assertTrue(result.contains(111));

		// in combination with other operators
		result = executeQuery("SELECT * FROM cmis:folder where '21' = any cmis:allowedChildObjectTypeIds and (cmis:name = 'testFolder3' or cmis:name like '%testFolder1')", TestConstants.TEST_REPO_2);

		assertEquals(1, result.size());
		assertTrue(result.contains(110));

		//TODO: with not
		//select * from cmis:folder where not '234' = any cmis:allowedChildObjectTypeIds;
	}


	@Test
	public void testQuantifiedInPredicate() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService7.xml");
		setUser(TestConstants.TEST2_USER, TestConstants.TEST2_USER, TestConstants.TEST_REPO_2);
		repoSelector.getRepository("test_repo_02").setPwcSearchable(true);
		repoSelector.getRepository("test_repo_02").setAllVersionsSearchable(true);

		List<Integer> result = null;

		result = executeQuery("SELECT * FROM cmis:folder where any cmis:allowedChildObjectTypeIds in ('21', '23', '100')", TestConstants.TEST_REPO_2);
		assertEquals(3, result.size());
		assertTrue(result.contains(110));
		assertTrue(result.contains(111));
		assertTrue(result.contains(112));

		result = executeQuery("SELECT * FROM cmis:folder where any cmis:allowedChildObjectTypeIds not in ('2', '21', '23')", TestConstants.TEST_REPO_2);
		assertEquals(2, result.size());
		assertTrue(result.contains(110));
		assertTrue(result.contains(112));

		result = executeQuery("SELECT * FROM cmis:folder where any cmis:allowedChildObjectTypeIds not in ('2', '21', '23') and (cmis:name = 'foobar' or cmis:name in ('testFolder1', 'testFolder2'))", TestConstants.TEST_REPO_2);
		assertEquals(1, result.size());
		assertTrue(result.contains(110));
	}

	@Test
	public void restrictByRepo() throws Exception {

		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, "my_new_repo");
		setScenario(DatabaseOperation.CLEAN_INSERT, "repo_nest.xml");
		repoSelector.getRepository("my_new_repo").setPwcSearchable(true);
		repoSelector.getRepository("my_new_repo").setAllVersionsSearchable(true);

		List<Integer> result = executeQuery("SELECT * FROM cmis:document where contains('1.0')", "my_new_repo");

		assertEquals(1, result.size());
		assertEquals(20779, result.iterator().next().intValue());
	}

	@Test
	public void testColumnExtractor() throws Exception {

		Map<String, String> columns = query.getQueryColumns("select cmis:createdBy, cmis:isLatestVersion from cmis:document");

		//TODO: test aliases

		assertEquals(2, columns.size());
		assertTrue(columns.containsKey("cmis:createdBy"));
		assertTrue(columns.containsKey("cmis:isLatestVersion"));
	}

	@Test
	public void testVersioning() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService6.xml"); //TODO: ...7.xml
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TestConstants.TEST_REPO_2);
		repoSelector.getRepository("test_repo_02").setPwcSearchable(true);
		repoSelector.getRepository("test_repo_02").setAllVersionsSearchable(true);

		executeQuery("select * from cmis:folder", "test_repo_02", true);

		executeQuery("select cmis:createdBy, cmis:isLatestVersion from cmis:document", "test_repo_02", false);

		executeQuery("select * from cmis:document", "test_repo_02", false);

		List<Integer> results = executeQuery("select * from cmis:document order by cmis:createdBy asc", "test_repo_02", true); // should contain 119, 120
		assertTrue(results.contains(119));
		assertTrue(results.contains(120));

		results = executeQuery("select * from cmis:document order by cmis:createdBy asc", "test_repo_02", false); // should not contain 119, only 120
		assertFalse(results.contains(119));
		assertTrue(results.contains(120));

		results = executeQuery("select * from cmis:document where cmis:objectId like 'internal%' order by cmis:objectId asc", "test_repo_02", true);
		assertTrue(results.contains(119));
		assertTrue(results.contains(120));

		results = executeQuery("select * from cmis:document where cmis:objectId like 'internal%' order by cmis:objectId asc", "test_repo_02", false);
		assertFalse(results.contains(119));
		assertTrue(results.contains(120));

		repoSelector.getRepository("test_repo_02").setPwcSearchable(false);
		results = executeQuery("select * from cmis:document order by cmis:createdBy asc", "test_repo_02", true);
		assertTrue(results.contains(120));
	}

	@Test
	public void testCapabilityAcl() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService6.xml");

		//admin && Capability_ACL = NONE
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TestConstants.TEST_REPO_2);
		repoSelector.getRepository("test_repo_02").setAcl(CapabilityAcl.NONE);

		List<Integer> results = executeQuery("select * from cmis:folder order by cmis:createdBy asc", "test_repo_02");

		assertEquals(9, results.size());

		//admin && Capability_ACL = DISCOVER
		repoSelector.getRepository("test_repo_02").setAcl(CapabilityAcl.DISCOVER);

		results = executeQuery("select * from cmis:folder order by cmis:createdBy asc", "test_repo_02");

		assertEquals(9, results.size());

		//test2 && Capability_ACL = NONE
		setUser(TestConstants.TEST2_USER, TestConstants.TEST2_PWD, TestConstants.TEST_REPO_2);
		repoSelector.getRepository("test_repo_02").setAcl(CapabilityAcl.NONE);

		results = executeQuery("select * from cmis:folder order by cmis:createdBy asc", "test_repo_02");

		assertEquals(9, results.size());

		//test2 && Capability_ACL = DISCOVER
		repoSelector.getRepository("test_repo_02").setAcl(CapabilityAcl.DISCOVER);

		results = executeQuery("select * from cmis:folder order by cmis:createdBy asc", "test_repo_02");
		assertEquals(3, results.size());
	}

	@Test
	public void testInFolder() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService5.xml");
		repoSelector.getRepository("test_repo_02").setAcl(CapabilityAcl.MANAGE);

		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TestConstants.TEST_REPO_2);
		List<Integer> results = executeQuery("select * from test:subfolder where in_folder('internalTestFolder2')", "test_repo_02");
		assertEquals(1, results.size());
		assertTrue(results.contains(113));

		//test
		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TestConstants.TEST_REPO_2);
		assertEquals(0, executeQuery("select * from test:subfolder where in_folder('internalTestFolder2')", "test_repo_02").size());

		//test2
		setUser(TestConstants.TEST2_USER, TestConstants.TEST2_PWD, TestConstants.TEST_REPO_2);
		results = executeQuery("select * from test:subfolder where in_folder('internalTestFolder2')", "test_repo_02");
		assertEquals(1, results.size());
		assertTrue(results.contains(113));

		//test2
		setUser(TestConstants.TEST2_USER, TestConstants.TEST2_PWD, TestConstants.TEST_REPO_2);
		results = executeQuery("select * from cmis:document where in_folder('internalTestFolder2')", "test_repo_02");
		assertEquals(1, results.size());
		assertTrue(results.contains(119));

		//TODO: combined queries
	}

	@Test
	public void testInTree() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService6.xml");
		repoSelector.getRepository("test_repo_02").setAcl(CapabilityAcl.MANAGE);

		//admin
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TestConstants.TEST_REPO_2);
		List<Integer> results = executeQuery("select * from cmis:folder where in_tree('internalTestFolder2')", "test_repo_02");
		assertEquals(3, results.size());
		assertTrue(results.contains(113));
		assertTrue(results.contains(114));
		assertTrue(results.contains(115));

		results = executeQuery("select * from cmis:document where in_tree('internalTestFolder2')", "test_repo_02");
		assertEquals(4, results.size());
		assertTrue(results.contains(119));
		assertTrue(results.contains(116));
		assertTrue(results.contains(118));
		assertTrue(results.contains(117));

		//test --> no access
		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TestConstants.TEST_REPO_2);
		assertEquals(0, executeQuery("select * from test:subfolder where in_tree('internalTestFolder2')", "test_repo_02").size());

		//test2 --> partial access (should return only 118, 119)
		setUser(TestConstants.TEST2_USER, TestConstants.TEST2_PWD, TestConstants.TEST_REPO_2);
		results = executeQuery("select * from cmis:document where in_tree('internalTestFolder2')", "test_repo_02");
		assertEquals(2, results.size());
		assertTrue(results.contains(118));
		assertTrue(results.contains(119));
	}

	@Test
	public void testsUnderDevelopment() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml");

		executeQuery("select * from cmis:document", "test_repo_02");

		List<Integer> cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy like '%len' ", "test_repo_02"));

		assertEquals(2, cmisObjects.size());

		for(Integer objectId : cmisObjects) {
			CMISObject obj = utilService.find(CMISObject.class,objectId);
			Property property = propertySelector.getObjectPropertyByQueryName(obj.getCmisObjectId(), "cmis:createdBy");
			assertTrue(property.<String>getTypedValue().endsWith("len"));
		}

		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy not like '%len' ", "test_repo_02"));

		assertEquals(5, cmisObjects.size());

		for(Integer objectId : cmisObjects) {
			CMISObject obj = utilService.find(CMISObject.class,objectId);
			Property property = propertySelector.getObjectPropertyByQueryName(obj.getCmisObjectId(), "cmis:createdBy");
			assertTrue(!property.<String>getTypedValue().endsWith("len"));
		}

		String notInQuery = "select * from test:subfolder where cmis:createdBy not in ('Allen', 'Jorge')";

		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy in ('Allen', 'Jorge')", "test_repo_02"));

		assertEquals(5, cmisObjects.size());

		for(Integer objectId : cmisObjects) {
			CMISObject obj = utilService.find(CMISObject.class,objectId);
			Property property = propertySelector.getObjectPropertyByQueryName(obj.getCmisObjectId(), "cmis:createdBy");
			assertTrue(property.<String>getTypedValue().equals("Allen") || property.<String>getTypedValue().equals("Jorge"));
		}

		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:numberField in (3,4,5)", "test_repo_02"));

		assertEquals(6, cmisObjects.size());

		for(Integer objectId : cmisObjects) {
			CMISObject obj = utilService.find(CMISObject.class,objectId);
			Property property = propertySelector.getObjectPropertyByQueryName(obj.getCmisObjectId(), "cmis:numberField");
			BigDecimal current = property.<BigDecimal>getTypedValue();
			BigDecimal expected = new BigDecimal("3.0");
			//expected.equals(current) is returning false because is comparing 3 and 3.0
			assertTrue(expected.compareTo(current) == 0);
		}

		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:numberField not in (3,4,5)", "test_repo_02"));

		assertEquals(1, cmisObjects.size());

		Property property = propertySelector.getObjectPropertyByQueryName(utilService.find(CMISObject.class,cmisObjects.get(0)).getCmisObjectId(), "cmis:numberField");
		assertTrue(new BigDecimal("31.499").equals(property.<BigDecimal>getTypedValue()));

		//TODO: complex queries combining IN, NOT IN, LIKE, NOT LIKE

		// should throw exceptions
		// assertEquals(1, cmisQueryService.executeQuery("select * from test:subfolder where cmis:numberField like '%foo' ", "test_repo_02").size());
		// assertEquals(1, cmisQueryService.executeQuery("select * from test:subfolder where cmis:numberField like 45", "test_repo_02").size());

		//TODO: multi-repo queries with contains

		//query.executeQuery("SELECT * FROM cmis:document where contains('foo man')", "my_new_repo"); //phrase

		//query.executeQuery("SELECT * FROM cmis:document where contains('1.0') or contains('2.0')", "my_new_repo"); // multiple contains OR

		//query.executeQuery("SELECT * FROM cmis:document where contains('1.0') and contains('2.0')", "my_new_repo"); // multiple contains AND

		//query.executeQuery("SELECT * FROM cmis:document", "my_new_repo"); //TODO: also test orderby

		//query.executeQuery("SELECT * FROM cmis:document where cmis:createdBy like '%l%' or contains('1.0')", "my_new_repo");

		//query.executeQuery("SELECT * FROM cmis:document where cmis:createdBy like '%l%' and contains('1.0')", "my_new_repo");
	}

	@Test
	public void testSecurity() throws Exception {

		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TestConstants.TEST_REPO_2);

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService4.xml");
		repoSelector.getRepository("test_repo_02").setAcl(CapabilityAcl.MANAGE);

		assertEquals(2, executeQuery("select * from test:subfolder where cmis:createdBy like '%l%' or contains('foo man') order by cmis:createdBy asc", "test_repo_02").size()); //-> 102, 113

		setUser(TestConstants.TEST2_USER, TestConstants.TEST2_PWD, TestConstants.TEST_REPO_2);

		assertEquals(3, executeQuery("select * from test:subfolder where cmis:createdBy like '%l%' or contains('foo man')", "test_repo_02").size()); //-> 102, 113, 111
		
		assertEquals(2, executeQuery("select * from test:subfolder where cmis:createdBy like '%l%' and cmis:numberField > 2", "test_repo_02").size()); //-> 102, 113
	}

	@Test
	public void testExcludeSubtype() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml", "scenarioQueryService2.xml", "scenarioQueryService3.xml");

		List<Integer> cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy = 'Allen' and cmis:numberField = 3", "test_repo_02"));

		assertEquals(2, cmisObjects.size());

		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder", "test_repo_02"));

		assertEquals(8, cmisObjects.size());

		/*for(Integer objectId : cmisObjects) {
			CMISObject obj = utilService.find(CMISObject.class,objectId);
			Property property = propertySelector.getObjectPropertyByQueryName(obj.getCmisObjectId(), "cmis:createdBy");
			assertTrue(property.<String>getTypedValue().endsWith("len"));
		}*/

	}

	@Test(expected=CmisInvalidArgumentException.class)
	public void testOrderByNonExistantColumn() throws Exception {
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService4.xml");
		executeQuery("select * from test:subfolder order by cmis:idontexist asc", "test_repo_02");
	}

	@Test(expected=CmisInvalidArgumentException.class)
	public void testOrderByNonOrderableColumn() throws Exception {
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService4.xml");
		executeQuery("select * from test:subfolder order by cmis:changeToken asc", "test_repo_02");
	}

	@Test
	public void testOrderByWithInheritance() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService4.xml");

		executeQuery("select * from test:subfolder order by cmis:numberField asc", "test_repo_02", -1, 0); // NO PAGING

		try {
			executeQuery("select * from test:subfolder order by cmis:numberField asc", "test_repo_02", -1, 1); // ERROR
		} catch(Exception e) {
			assertTrue(e instanceof CmisQueryException);
		}

		try {
			executeQuery("select * from test:subfolder order by cmis:numberField asc", "test_repo_02", 0, 1); // ERROR
		} catch(Exception e) {
			assertTrue(e instanceof CmisQueryException);
		}

		try {
			executeQuery("select * from test:subfolder order by cmis:numberField asc", "test_repo_02", 5, -1); // ERROR
		} catch(Exception e) {
			assertTrue(e instanceof CmisQueryException);
		}

		assertEquals(5, executeQuery("select * from test:subfolder order by cmis:numberField asc", "test_repo_02", 5, 0).size()); // PAGING (FIRST PAGE) -> 5

		assertEquals(3, executeQuery("select * from test:subfolder order by cmis:numberField asc", "test_repo_02", 5, 2).size()); // PAGING (BETWEEN) -> 3

		assertEquals(5, query.queryCount("select * from test:subfolder order by cmis:numberField asc", "test_repo_02", true, NORMALIZED_QUERY_FALSE)); //count

		executeQuery("select * from test:subfolder order by cmis:createdBy asc", "test_repo_02"); //--> 5, ordered last(114, kardaal)

		executeQuery("select * from test:subfolder order by cmis:numberField asc", "test_repo_02"); //-->5, ordered (query here...)

		executeQuery("select * from test:orderByProperty1 order by cmis:createdBy asc", "test_repo_02"); //--> 2

		executeQuery("select * from test:orderByProperty2 order by cmis:createdBy asc", "test_repo_02"); //--> 1

		executeQuery("select * from test:subfolder where cmis:createdBy like '%l%' order by cmis:createdBy asc", "test_repo_02"); //--> 3, ordered

		executeQuery("select * from test:subfolder where cmis:createdBy like '%l%' order by cmis:numberField asc", "test_repo_02"); //--> 3, ordered
	}

	@Test
	public void testMultipleOrderBy() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService6.xml");

		List<Integer> resultSet = null;
		Integer [] results = null;

		//TODO:MULTIPLE ORDER BY + NO FILTER -> validate columns
		//TEST MULTIPLE ORDER BY + NO FILTER, ADMIN
		resultSet = executeQuery("select * from test:subfolder order by cmis:numberField, cmis:createdBy desc", "test_repo_02");
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(6, results.length);
		/*
		 * objectTypeId	objectId	cmis:numberField (51)	cmis:createdBy (28)
		 * 21			102			3.0						kardaal
		 * 21			110			3.0						kardaal
		 * 24			115			3.0						kardaal
		 * 24			114			3.0						kardaal
		 * 22			111			3.0						foobar
		 * 22			113			3.0						Allen
		 */
		List<Integer> first = Arrays.asList(new Integer[] {102, 110, 115, 114});
		assertTrue(first.contains(results[0].intValue()));
		assertEquals(113, results[5].intValue());

		//TEST MULTIPLE ORDER BY + NO FILTER, ADMIN, PAGING(NO OFFSET)
		resultSet = executeQuery("select * from test:subfolder order by cmis:numberField, cmis:createdBy desc", "test_repo_02", 3, 0);
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(3, results.length);
		assertTrue(first.contains(results[0].intValue()));
		assertTrue(first.contains(results[2].intValue()));

		//TEST MULTIPLE ORDER BY + NO FILTER, ADMIN, PAGING(WITH OFFSET)
		resultSet = executeQuery("select * from test:subfolder order by cmis:numberField, cmis:createdBy desc", "test_repo_02", 3, 1);
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(3, results.length);
		assertTrue(first.contains(results[0].intValue()));
		assertTrue(first.contains(results[2].intValue()));

		//TEST MULTIPLE ORDER BY + NO FILTER, ADMIN, COUNT
		assertEquals(6, query.queryCount("select * from test:subfolder order by cmis:numberField, cmis:createdBy desc", "test_repo_02", true, NORMALIZED_QUERY_FALSE));

		/*
		 * Enable ACLs, change to TEST2_USER
		 */
		repoSelector.getRepository("test_repo_02").setAcl(CapabilityAcl.DISCOVER);
		setUser(TestConstants.TEST2_USER, TestConstants.TEST2_PWD, TestConstants.TEST_REPO_2);

		//TEST MULTIPLE ORDER BY + NO FILTER, TEST2 USER
		resultSet = executeQuery("select * from test:subfolder order by cmis:numberField, cmis:createdBy desc", "test_repo_02");
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(3, results.length);
		assertEquals(115, results[0].intValue());
		assertEquals(113, results[2].intValue());

		//TEST MULTIPLE ORDER BY + NO FILTER, TEST2 USER, PAGING(NO OFFSET)
		resultSet = executeQuery("select * from test:subfolder order by cmis:numberField, cmis:createdBy desc", "test_repo_02", 3, 0);
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(3, results.length);
		assertEquals(115, results[0].intValue());
		assertEquals(113, results[2].intValue());

		//TEST MULTIPLE ORDER BY + NO FILTER, TEST2 USER, PAGING(WITH OFFSET)
		resultSet = executeQuery("select * from test:subfolder order by cmis:numberField, cmis:createdBy desc", "test_repo_02", 3, 1);
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(2, results.length);
		assertEquals(111, results[0].intValue());
		assertEquals(113, results[1].intValue());

		//TEST MULTIPLE ORDER BY + NO FILTER, TEST2 USER, COUNT
		assertEquals(3, query.queryCount("select * from test:subfolder order by cmis:numberField, cmis:createdBy desc", "test_repo_02", true, NORMALIZED_QUERY_FALSE));

		//---ADMIN USER---
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TestConstants.TEST_REPO_2);

		repoSelector.getRepository("test_repo_02").setPwcSearchable(true);
		repoSelector.getRepository("test_repo_02").setAllVersionsSearchable(true);

		//102, 110, 114, 115, 113
		
		resultSet = executeQuery("select * from test:subfolder where cmis:createdBy like '%l%' order by cmis:numberField, cmis:createdBy desc", "test_repo_02");
		
		assertTrue(resultSet.size() == 5 && resultSet.contains(102)
				                         && resultSet.contains(110)
				                         && resultSet.contains(114)
				                         && resultSet.contains(115)
				                         && resultSet.contains(113));
	}

	@Test
	public void testOrderBy() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml");

		List<Integer> resultSet = null;
		Integer [] results = null;

		//TEST DEFAULT ORDERING + NO FILTER
		resultSet = executeQuery("select * from test:subfolder order by cmis:numberField", "test_repo_02");
		/*
		 * objectId	cmis:numberField	cmis:createdBy
		 * 102		3.0					kardaal
		 * 110		31.499				Al'len
		 * 111		3.0					Allen
		 * 112		3.0					Jorge
		 * 113		3.0					Jorge
		 * 114		3.0					Jorge
		 * 115 		3.0					Jorge
		 */
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(7, results.length);
		List<Integer> first = Arrays.asList(new Integer[] {102, 111, 112, 113, 114, 115} );
		assertTrue(first.contains(results[0].intValue()));
		assertEquals(110, results[6].intValue());

		//TEST DEFAULT ORDERING + FILTER
		resultSet = executeQuery("select * from test:subfolder where cmis:createdBy like '%len' order by cmis:createdBy", "test_repo_02");
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(2, results.length);
		assertEquals(110, results[0].intValue());
		assertEquals(111, results[1].intValue());

		// NO FILTER
		//(1) pure
		assertEquals(7, executeQuery("select * from test:subfolder order by cmis:createdBy asc", "test_repo_02").size());
		//(2) pure (on numeric value)
		resultSet = executeQuery("select * from test:subfolder order by cmis:numberField asc", "test_repo_02");
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(7, results.length);
		assertTrue(first.contains(results[0].intValue()));
		assertEquals(110, results[6].intValue());
		//(3) order + paging
		assertEquals(3, executeQuery("select * from test:subfolder order by cmis:createdBy asc", "test_repo_02", 3, 0).size());

		// WITH FILTER
		//(1) pure (on string value)
		resultSet = executeQuery("select * from test:subfolder where cmis:createdBy like '%len' order by cmis:createdBy asc", "test_repo_02");
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(2, results.length);
		assertEquals(110, results[0].intValue());
		assertEquals(111, results[1].intValue());

		resultSet = executeQuery("select * from test:subfolder where cmis:createdBy like '%len' order by cmis:createdBy desc", "test_repo_02");
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(2, results.length);
		assertEquals(111, results[0].intValue());
		assertEquals(110, results[1].intValue());

		//(2) pure (on numeric value)
		resultSet = executeQuery("select * from test:subfolder where cmis:numberField >= -1 order by cmis:numberField desc", "test_repo_02");
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(7, results.length);
		assertEquals(110, results[0].intValue());

		resultSet = executeQuery("select * from test:subfolder where cmis:numberField >= -1 order by cmis:numberField asc", "test_repo_02");
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(7, results.length);
		assertEquals(110, results[6].intValue());

		//TODO: activate following tests when Hibernate/JPA 2.1 becomes available

		//(3) pure on different virtual column
		resultSet = executeQuery("select * from test:subfolder where cmis:createdBy like '%len' order by cmis:numberField asc", "test_repo_02");
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(2, results.length);
		assertEquals(111, results[0].intValue());
		assertEquals(110, results[1].intValue());

		
		//(4) order + paging
		resultSet = executeQuery("select * from test:subfolder where cmis:createdBy like '%len' order by cmis:createdBy asc", "test_repo_02", 1, 0);
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(1, results.length);
		assertEquals(110, results[0].intValue());

		
		//(5) paging + order on different column
		resultSet = executeQuery("select * from test:subfolder where cmis:createdBy like '%len' order by cmis:numberField asc", "test_repo_02", 3, 0);
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(2, results.length);
		assertEquals(111, results[0].intValue());
		assertEquals(110, results[1].intValue());

		resultSet = executeQuery("select * from test:subfolder where cmis:createdBy like '%len' order by cmis:numberField asc", "test_repo_02", 3, 1);
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(1, results.length);
		assertEquals(110, results[0].intValue());
	}

	@Test(expected = Exception.class)
	public void testParser() throws Exception {
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml");
		// in the following query, parser generates -> value=null
		executeQuery("select * from test:subfolder where cmis:createdBy = Al\\'len AND ", "test_repo_02").size();
	}

	@Test(expected = CmisInvalidArgumentException.class)
	public void testLikeOnNonStringFields() throws Exception {
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml");
		executeQuery("select * from test:subfolder where cmis:numberField like '%foo' ", "test_repo_02").size();
	}

	@Test
	public void testComplexFilterQueries() throws Exception {
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml");

		assertEquals(1, executeQuery("select * from test:subfolder where cmis:createdBy = 'Al\\'len'", "test_repo_02").size());
		assertEquals(1, executeQuery("select * from test:subfolder where cmis:numberField = 31.499", "test_repo_02").size());
		assertEquals(1, executeQuery("select * from test:subfolder where (cmis:createdBy = 'Al\\'len' and cmis:numberField = 31.499 and cmis:numberField <> 31) or (cmis:numberField = 31.499 and cmis:createdBy <> 'foo')", "test_repo_02").size());
		assertEquals(5, executeQuery("select * from test:subfolder where (cmis:createdBy = 'Allen' and cmis:numberField = 3) or (cmis:createdBy = 'Jorge' and cmis:numberField = 3)", "test_repo_02").size());
		assertEquals(1, executeQuery("select * from test:subfolder where cmis:createdBy = 'Allen' and (cmis:numberField = 3 or cmis:createdBy = 'Jorge') and cmis:numberField = 3", "test_repo_02").size());
	}

	@Test
	public void testQueryPaging() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml");

		assertEquals(5, executeQuery("select * from test:subfolder where (cmis:createdBy = 'Allen' and cmis:numberField = 3) or (cmis:createdBy = 'Jorge' and cmis:numberField = 3)", "test_repo_02").size());

		assertEquals(3, executeQuery("select * from test:subfolder where (cmis:createdBy = 'Allen' and cmis:numberField = 3) or (cmis:createdBy = 'Jorge' and cmis:numberField = 3)", "test_repo_02", 3, 0).size());
		assertEquals(2, executeQuery("select * from test:subfolder where (cmis:createdBy = 'Allen' and cmis:numberField = 3) or (cmis:createdBy = 'Jorge' and cmis:numberField = 3)", "test_repo_02", 3, 3).size());
	}

	@Test
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public void testFullTextSearch() throws Exception {

		/*setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml");

		InputStream in = ClassLoader.getSystemResourceAsStream(DOC_1);
		lobDelegate.saveStream(111, in, in.available());
		indexHelper.indexFromScratch(new IndexTask(1, 111, IndexTask.Operation.INDEX_FROM_SCRATCH, indexSynchronizer));

		in = ClassLoader.getSystemResourceAsStream(DOC_2);
		lobDelegate.saveStream(112, in, in.available());
		indexHelper.indexFromScratch(new IndexTask(1, 112, IndexTask.Operation.INDEX_FROM_SCRATCH, indexSynchronizer));

		//DBUnit.export here...

		assertEquals(2, query.executeQuery("select * from test:subfolder where contains('foo')", "test_repo_02").size());
		assertEquals(1, query.executeQuery("select * from test:subfolder where contains('bar')", "test_repo_02").size());
		assertEquals(1, query.executeQuery("select * from test:subfolder where contains('foo') and cmis:createdBy = 'Allen'", "test_repo_02").size());
		assertEquals(2, query.executeQuery("select * from test:subfolder where contains('foo') and (cmis:createdBy = 'Allen' or cmis:createdBy = 'Jorge')", "test_repo_02").size());
		assertEquals(1, query.executeQuery("select * from test:subfolder where contains('bar') and (cmis:createdBy = 'Allen' or cmis:createdBy = 'Jorge')", "test_repo_02").size());
		assertEquals(0, query.executeQuery("select * from test:subfolder where contains('bar') and cmis:createdBy = 'Allen'", "test_repo_02").size());*/

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml");

		InputStream in = ClassLoader.getSystemResourceAsStream(DOC_1);
		lobDelegate.saveStream(111, in, in.available());
		//IndexPartOperator.createIndexPart(IndexTask indexTask)
		contentIndexOperator.createIndexPart(new IndexTaskImpl(1, 111, IndexOperation.CONTENT_INDEX, DOC_1.substring(4), BigInteger.valueOf(0), indexSynchronizer));

		in = ClassLoader.getSystemResourceAsStream(DOC_2);
		lobDelegate.saveStream(112, in, in.available());
		contentIndexOperator.createIndexPart(new IndexTaskImpl(1, 112, IndexOperation.CONTENT_INDEX, DOC_2.substring(4), BigInteger.valueOf(0), indexSynchronizer));

		in = ClassLoader.getSystemResourceAsStream(DOC_3);
		lobDelegate.saveStream(113, in, in.available());
		contentIndexOperator.createIndexPart(new IndexTaskImpl(1, 113, IndexOperation.CONTENT_INDEX, DOC_3.substring(4), BigInteger.valueOf(0), indexSynchronizer));

		in = ClassLoader.getSystemResourceAsStream(DOC_4);
		lobDelegate.saveStream(114, in, in.available());
		contentIndexOperator.createIndexPart(new IndexTaskImpl(1, 114, IndexOperation.CONTENT_INDEX, DOC_4.substring(4), BigInteger.valueOf(0), indexSynchronizer));

		/*in = ClassLoader.getSystemResourceAsStream(PDF_DOC);
		lobDelegate.saveStream(115, in, in.available());
		indexHelper.indexFromScratch(new IndexTask(1, 115, IndexTask.Operation.INDEX_FROM_SCRATCH, indexSynchronizer));*/

		//TODO: reenable
		List<Integer> ids;/* = executeQuery("select * from test:subfolder where contains('foo man choo')", "test_repo_02");

		assertEquals(2, ids.size());
		assertTrue(ids.contains(111));
		assertTrue(ids.contains(114));*/
		
		//------------------
		//ids = executeQuery("select * from test:subfolder where contains('foo')", "test_repo_02");
		
		//TODO: TDR-195
		query.executeQuery("select * from test:subfolder where contains('foobar')", "test_repo_02", true, 10, 0, NORMALIZED_QUERY_FALSE);
		
		query.executeQuery("select cmis:name, SCORE() from test:subfolder where cmis:createdBy like '%ll%' and contains('foo')", "test_repo_02", true, 10, 0, NORMALIZED_QUERY_FALSE);
		
		query.executeQuery("select cmis:name, SCORE() from test:subfolder where cmis:createdBy like '%ll%' and contains('foo') order by cmis:createdBy", "test_repo_02", true, 10, 0, NORMALIZED_QUERY_FALSE);
		
		query.executeQuery("select cmis:name, SCORE() from test:subfolder where cmis:createdBy like '%ll%' and contains('foo') order by SEARCH_SCORE", "test_repo_02", true, 10, 0, NORMALIZED_QUERY_FALSE);
		
		//query.executeQuery("select cmis:name, SCORE() from cmis:document where cmis:createdBy like '%ll%' and contains('%foo%') order by cmis:createdBy asc, cmis:numberField, SEARCH_SCORE", "test_repo_02", true, 10, 0, NORMALIZED_QUERY_FALSE);
		
		
		query.executeQuery("select cmis:name from test:subfolder where cmis:createdBy like '%ll%' and contains('foo') order by cmis:createdBy asc", "test_repo_02", true, 10, 0, NORMALIZED_QUERY_FALSE);
		// query_count, score()
		//------------------

		ids = executeQuery("select * from test:subfolder where contains('foo man choo') or contains('man choo')", "test_repo_02");

		assertEquals(3, ids.size());
		assertTrue(ids.contains(111));
		assertTrue(ids.contains(112));
		assertTrue(ids.contains(114));

		ids = executeQuery("select * from test:subfolder where contains('Pneumonoultramicroscopicsilicovolcanoconiosis foo man choo')", "test_repo_02");

		assertEquals(0, ids.size());

		ids = executeQuery("select * from test:subfolder where contains('Lopadotemachoselachogaleokranioleipsanodrimhypotrimmatosilphioparaomelitokatakechymenokichlepikossyphophattoperisteralektryonoptekephalliokigklopeleiolagoiosiraiobaphetraganopterygon foo man choo')", "test_repo_02");

		assertEquals(2, ids.size());
		assertTrue(ids.contains(111));
		assertTrue(ids.contains(114));

		try {
			executeQuery("select * from test:subfolder where contains('Lopadotemachoselachogaleokranioleipsanodrimhypotrimmatosilphioparaomelitokatakechymenokichlepikossyphophattoperisteralektryonoptekephalliokigklopeleiolagoiosiraiobaphetraganopterygon')", "test_repo_02");
		} catch(Exception e) {
			assertTrue(e instanceof CmisInvalidArgumentException);
		}

		try {
			executeQuery("select * from test:subfolder where contains()", "test_repo_02");
		} catch(Exception e) {
			assertTrue(e instanceof CmisRuntimeException);
		}

		try {
			executeQuery("select * from test:subfolder where contains('')", "test_repo_02");
		} catch(Exception e) {
			assertTrue(e instanceof CmisInvalidArgumentException);
		}

		try {
			executeQuery("select * from test:subfolder where contains('     ')", "test_repo_02");
		} catch(Exception e) {
			assertTrue(e instanceof CmisInvalidArgumentException);
		}

		/*ids = query.executeQuery("select * from test:subfolder where contains('Alfresco Public RESTFul method')", "test_repo_02");

		assertEquals(1, ids.size());
		assertTrue(ids.contains(115));*/
	}

	@Test
	public void testQueryCount() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml");

		assertEquals(5, query.queryCount("select * from test:subfolder where (cmis:createdBy = 'Allen' and cmis:numberField = 3) or (cmis:createdBy = 'Jorge' and cmis:numberField = 3)", "test_repo_02", true, NORMALIZED_QUERY_FALSE));
		assertEquals(7, query.queryCount("select * from test:subfolder", "test_repo_02", true, NORMALIZED_QUERY_FALSE));
	}

	@Test
	public void testQuery() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml");

		assertEquals(1, executeQuery("select * from test:subfolder where cmis:createdBy = 'Al\\'len'", "test_repo_02").size());

		assertEquals(7, executeQuery("select * from test:subfolder", "test_repo_02").size());

		assertEquals(1, executeQuery("select * from test:subfolder where cmis:createdBy = 'Al\\'len' and cmis:numberField = 31.499", "test_repo_02").size());

		assertEquals(7, executeQuery("select * from test:subfolder where cmis:numberField < 31.4999", "test_repo_02").size());
		assertEquals(1, executeQuery("select * from test:subfolder where cmis:numberField > 31.498", "test_repo_02").size());

		assertEquals(1, executeQuery("select * from test:subfolder where ((cmis:createdBy = 'Al\\'len' and cmis:numberField = 31.499) or (cmis:createdBy = 'Al\\'len' and cmis:numberField <> 32)) and cmis:numberField < 35", "test_repo_02").size());

		// TODO: extract to separate 'TEST ALIASES' method
		assertEquals(7, executeQuery("select F.* from test:subfolder F", "test_repo_02").size());

		assertEquals(1, executeQuery("select * from test:subfolder where cmis:createdBy = 'Al\\'len'", "test_repo_02").size());

		assertEquals(1, executeQuery("select * from test:subfolder where cmis:objectId = '10'", "test_repo_02").size());
		assertEquals(0, executeQuery("select * from test:subfolder where cmis:objectId = '11'", "test_repo_02").size());

		assertEquals(2, executeQuery("select * from test:subfolder where cmis:objectId > '09'", "test_repo_02").size());
		assertEquals(1, executeQuery("select * from test:subfolder where cmis:objectId > '10'", "test_repo_02").size());

		assertEquals(1, executeQuery("select * from test:subfolder where cmis:objectId < '11'", "test_repo_02").size());
		assertEquals(0, executeQuery("select * from test:subfolder where cmis:objectId < '10'", "test_repo_02").size());

		assertEquals(2, executeQuery("select * from test:subfolder where cmis:objectId >= '10'", "test_repo_02").size());
		assertEquals(1, executeQuery("select * from test:subfolder where cmis:objectId >= '11'", "test_repo_02").size());
		assertEquals(2, executeQuery("select * from test:subfolder where cmis:objectId >= '09'", "test_repo_02").size());

		assertEquals(1, executeQuery("select * from test:subfolder where cmis:objectId <= '10'", "test_repo_02").size());
		assertEquals(1, executeQuery("select * from test:subfolder where cmis:objectId <= '11'", "test_repo_02").size());
		assertEquals(0, executeQuery("select * from test:subfolder where cmis:objectId <= '09'", "test_repo_02").size());

		assertEquals(1, executeQuery("select * from test:subfolder where cmis:numberField = 31.499", "test_repo_02").size());

		/*
		 * assertEquals(0, cmisQueryService.executeQuery("select * from cmis:folder where cmis:createdBy = 'hadamto'",1).size()); assertEquals(0, cmisQueryService.executeQuery("select * from cmis:folder where cmis:name <> 'MyFolder'", 1).size()); assertEquals(0, cmisQueryService.executeQuery("select * from cmis:folder where cmis:name = 'MyFolder' and cmis:createdBy = 'hadamto'", 1).size()); assertEquals(0, cmisQueryService.executeQuery("select * from cmis:folder where cmis:name = 'SOME_NAME' or (cmis:createdBy = 'hadamto' and cmis:lastModifiedBy = 'Jorge') or cmis:checkinComment = 'comment'", 1).size()); assertEquals(0, cmisQueryService.executeQuery("select * from cmis:folder where cmis:name = 'MyFolder' and cmis:createdBy = 'hadamto' and cmis:lastModifiedBy = 'Jorge'", 1).size());
		 */

		// assertEquals(0, cmisQueryService.executeQuery("select * from cmis:folder where cmis:createdBy = 'hadamto'",1).size());

		/*
		 * assertEquals(1, cmisQueryService.executeQuery("select * from cmis:document", 1).size()); assertEquals(1, cmisQueryService.executeQuery("select * from cmis:folder where cmis:createdBy = 'hadamto'",1).size()); assertEquals(0, cmisQueryService.executeQuery("select * from cmis:folder where cmis:name <> 'MyFolder'", 1).size()); assertEquals(1, cmisQueryService.executeQuery("select * from cmis:folder where cmis:name = 'MyFolder' and cmis:createdBy = 'hadamto'", 1).size()); assertEquals(1, cmisQueryService.executeQuery("select * from cmis:folder where cmis:name = 'SOME_NAME' or (cmis:createdBy = 'hadamto' and cmis:lastModifiedBy = 'Jorge') or cmis:checkinComment = 'comment'", 1).size()); assertEquals(1, cmisQueryService.executeQuery("select * from cmis:folder where cmis:name = 'MyFolder' and cmis:createdBy = 'hadamto' and cmis:lastModifiedBy = 'Jorge'", 1).size());
		 */
	}
	
	@Test
	@Ignore
	public void testDTQueries() throws Exception {
		
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml");
		
		assertEquals(1, executeQuery("select * from test:subfolder where cmis:createdBy = '\\'Al\\'len\\''", "test_repo_02").size());
		
		
		//assertEquals(1, executeQuery("select * from test:subfolder where cmis:creationDate = '2010-04-01T12:15:00.000+01:00'", "test_repo_02").size());
		//assertEquals(1, executeQuery("select * from test:subfolder where cmis:creationDate = TIMESTAMP '2010-04-01T12:15:00.000+01:00'", "test_repo_02").size());
	}
	
	@Test
	public void testQueryReturnType() throws Exception {
		
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml");
		
		QueryResult qr = query.executeQuery("select * from test:subfolder where cmis:creationDate > '2010-04-01T12:14:59.000+01:00'", "test_repo_02", true, -1, 0, NORMALIZED_QUERY_FALSE);
		
		CMISObject obj = qr.getResult().iterator().next();
		
		boolean correctObjectType = false;
		
		for (Property property : obj.getProperties()) {
			if(property.getObjectTypeProperty().getCmisId().equals("cmis:objectTypeId") && property.<String>getTypedValue().equals("test:subfolder")) {
				correctObjectType = true;
			}
		}
		
		assertTrue(correctObjectType);
	}
	
	@Test
	public void testSecondaryTypes() throws Exception {
		
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "secondary");
		setScenario("scenarioSecondaryTypes02.xml", DatabaseOperation.CLEAN_INSERT);
		
		List<Integer> results = executeQuery("select * from tag", "secondary");
		assertEquals(results.size(), 2);
		assertTrue(results.contains(3) && results.contains(4));
		
		results = executeQuery("select * from tag where any name in ('tree', 'book', 'house', 'foo', 'bar')", "secondary");
		assertEquals(results.size(), 1);
		assertTrue(results.contains(3));
		
		results = executeQuery("select * from tag where any name in ('tree', 'book', 'house', 'foo', 'bar', 'blue')", "secondary");
		assertEquals(results.size(), 2);
		assertTrue(results.contains(3) && results.contains(4));
		
		results = executeQuery("select * from tag where any name in ('foo', 'bar', 'blue')", "secondary");
		assertEquals(results.size(), 1);
		assertTrue(results.contains(4));
		
	}

	@Test
	public void testDateTimeQueries() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml");

		assertEquals(1, executeQuery("select * from test:subfolder where cmis:creationDate = '2010-04-01T12:15:00.000+01:00'", "test_repo_02").size());
		assertEquals(1, executeQuery("select * from test:subfolder where cmis:creationDate = '2010-04-01T10:15:00.000-01:00'", "test_repo_02").size());
		assertEquals(1, executeQuery("select * from test:subfolder where cmis:creationDate = '2010-04-01T11:15:00.000Z'", "test_repo_02").size());

		assertEquals(1, executeQuery("select * from test:subfolder where cmis:creationDate > '2010-04-01T12:14:59.000+01:00'", "test_repo_02").size());
		assertEquals(0, executeQuery("select * from test:subfolder where cmis:creationDate > '2010-04-01T12:15:00.000+01:00'", "test_repo_02").size());

		assertEquals(0, executeQuery("select * from test:subfolder where cmis:creationDate <> '2010-04-01T09:15:00.000-02:00'", "test_repo_02").size());

		assertEquals(1, executeQuery("select * from test:subfolder where cmis:creationDate >= '2010-04-01T12:15:00.000+01:00'", "test_repo_02").size());
		assertEquals(1, executeQuery("select * from test:subfolder where cmis:creationDate >= '2010-04-01T14:15:00.000+03:00'", "test_repo_02").size());

		assertEquals(0, executeQuery("select * from test:subfolder where cmis:creationDate < '2010-04-01T11:15:00.000+02:00'", "test_repo_02").size());
		assertEquals(1, executeQuery("select * from test:subfolder where cmis:creationDate < '2010-04-01T12:15:00.000+00:00'", "test_repo_02").size());

		assertEquals(1, executeQuery("select * from test:subfolder where cmis:creationDate <= '2010-04-01T12:15:00.000+01:00'", "test_repo_02").size());
		assertEquals(1, executeQuery("select * from test:subfolder where cmis:creationDate <= '2010-04-01T12:15:00.000+00:00'", "test_repo_02").size());
		assertEquals(0, executeQuery("select * from test:subfolder where cmis:creationDate <= '2010-04-01T12:15:00.000+02:00'", "test_repo_02").size());
	}

	@Test(expected = RuntimeException.class)
	public void testQueryAgainstUnQueryableObjectType() throws Exception {
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml");
		executeQuery("select * from cmis:unqueryable_folder where cmis:name != 'MyFolder'", "test_repo_02");
	}

	@Test(expected = RuntimeException.class)
	public void testQueryWithParametersInDoubleQuotes() throws Exception {
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryService1.xml");
		executeQuery("select * from test:subfolder where cmis:createdBy = \"Allen\"", "test_repo_02");
	}

	@Test(expected = RuntimeException.class)
	public void testQueryWithInvalidSyntax() throws Exception {
		// Operator != is not allowed, RuntimeException should be thrown
		executeQuery("select * from cmis:folder where cmis:name != 'MyFolder'", "test_repo_02").size();
	}

	@Test(expected = RuntimeException.class)
	public void testQueryWithInvalidEnding() throws Exception {
		// Query not ended properly
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml");
		executeQuery("select * from cmis:folder where cmis:name <> 'MyFolder' and cmis", "test_repo_02").size();
	}
	
	@Test
	public void test_containsOneToken() throws Exception {
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioRepo_fulltextIndexOneWord.xml");
		String queryContainsOneWord = "select cmis:name,score() s from cmis:document where contains('a') order by s desc";
		Repository repository = repoSelector.getRepository("test_repo_02");
		
		Map<String,? extends Object> actualResult = concreteQuery.executeLowLevelQuery(queryContainsOneWord, repository, true, false, 0, -1, false);		
		assertNotNull(actualResult);		
		String queryString = (String)actualResult.get("query");
		assertNotNull("Expected result should contain the query string :", queryString);		
		List<Object> ids = (List<Object>)actualResult.get("results");
		assertNotNull("Expected result should contain Expected result ids:", ids);
		assertTrue("Expected result size should be 103", ids.size() == 103);		
		
		String queryContainsOneOfMoreWords = "select cmis:name,score() s from cmis:document where contains('a') or contains('notAvailableWord')  order by s desc";
		actualResult = concreteQuery.executeLowLevelQuery(queryContainsOneOfMoreWords, repository, true, false, 0, -1, false);		
		assertNotNull(actualResult);		
		queryString = (String)actualResult.get("query");
		assertNotNull("Expected result should contain the query string :", queryString);		
		ids = (List<Object>)actualResult.get("results");
		assertNotNull("Expected result should contain Expected result ids:", ids);
		assertTrue("Expected result size should be 103", ids.size() == 103);
		
		String queryContainsAllWords = "select cmis:name,score() s from cmis:document where contains('a') and contains('notAvailableWord')  order by s desc";
		actualResult = concreteQuery.executeLowLevelQuery(queryContainsAllWords, repository, true, false, 0, -1, false);		
		assertNotNull(actualResult);		
		queryString = (String)actualResult.get("query");
		assertNotNull("Expected result should contain the query string :", queryString);		
		ids = (List<Object>)actualResult.get("results");
		assertNotNull("Expected result should contain Expected result ids:", ids);
		assertTrue("Expected result size should be 0", ids.size() == 0);
		
	}
	
	@Test
	public void test_containsMoreTokens() throws Exception {		
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioRepo_fulltextIndexMoreWords.xml");
		String cmisQuery = "select cmis:name,score() s from cmis:document where contains('a') order by s desc";
		Repository repository = repoSelector.getRepository("test_repo_02");
				
		Map<String,? extends Object> actualResult = concreteQuery.executeLowLevelQuery(cmisQuery, repository, true, false, 0, -1, false);		
		assertNotNull(actualResult);		
		String queryString = (String)actualResult.get("query");
		assertNotNull("Expected result should contain the query string :", queryString);		
		List<Object> ids = (List<Object>)actualResult.get("results");
		assertNotNull("Expected result should contain Expected result ids:", ids);
		assertTrue("Expected result size should be 100", ids.size() == 100);	
		
		cmisQuery = "select cmis:name,score() s from cmis:document where contains('a') and contains('abc') order by s desc";
		actualResult = concreteQuery.executeLowLevelQuery(cmisQuery, repository, true, false, 0, -1, false);		
		assertNotNull(actualResult);		
		queryString = (String)actualResult.get("query");
		assertNotNull("Expected result should contain the query string :", queryString);		
		ids = (List<Object>)actualResult.get("results");
		assertNotNull("Expected result should contain Expected result ids:", ids);
		assertTrue("Expected result size should be 1", ids.size() == 1);
	
		
		cmisQuery = "select cmis:name,score() s from cmis:document where contains('a') or contains('abc') order by s desc";
		actualResult = concreteQuery.executeLowLevelQuery(cmisQuery, repository, true, false, 0, -1, false);		
		assertNotNull(actualResult);		
		queryString = (String)actualResult.get("query");
		assertNotNull("Expected result should contain the query string :", queryString);		
		ids = (List<Object>)actualResult.get("results");
		assertNotNull("Expected result should contain Expected result ids:", ids);
		assertTrue("Expected result size should be 1", ids.size() == 100);
	}

	private List<Integer> executeQuery(String statement, String repositoryId) throws RecognitionException {
		return extractIds(query.executeQuery(statement, repositoryId, true, -1, 0, NORMALIZED_QUERY_FALSE));
	}

	private List<Integer> executeQuery(String statement, String repositoryId, int maxItems, int skipCount) throws RecognitionException {
		return extractIds(query.executeQuery(statement, repositoryId, true, maxItems, skipCount, NORMALIZED_QUERY_FALSE));
	}

	private List<Integer> executeQuery(String statement, String repositoryId, boolean searchAllVersions) throws RecognitionException {
		return extractIds(query.executeQuery(statement, repositoryId, searchAllVersions, -1, 0, NORMALIZED_QUERY_FALSE));
	}

	private List<Integer> extractIds(QueryResult qr) {
		List<Integer> ids = new ArrayList<>();

		for(CMISObject cmisObject : qr.getResult()) {
			ids.add(cmisObject.getId());
		}
		return ids;
	}

	//int queryCount(String statement, String repositoryId) throws RecognitionException;
}

















