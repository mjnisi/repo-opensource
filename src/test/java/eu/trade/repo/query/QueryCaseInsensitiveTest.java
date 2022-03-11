package eu.trade.repo.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.RecognitionException;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.commons.collections.CollectionUtils;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.delegates.JDBCLobDelegate;
import eu.trade.repo.index.IndexSynchronizer;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.Property;

@Transactional
public class QueryCaseInsensitiveTest extends BaseTestClass {

	private static final boolean NORMALIZED_QUERY_TRUE = true;
	

	@Autowired
	private IndexSynchronizer indexSynchronizer;

	@Autowired
	private JDBCLobDelegate lobDelegate;

	@Override
	@Before
	public void initUser() {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TestConstants.TEST_REPO_2);
	}
	
	
	@Test
	public void testNot() throws Exception {
		
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml");
		setScenario(DatabaseOperation.INSERT, "scenarioQueryServiceNormalized1.xml");
		setScenario(DatabaseOperation.INSERT, "scenarioQueryServiceNormalizedIndex.xml");
		
		repoSelector.getRepository("test_repo_02").setPwcSearchable(true);
		repoSelector.getRepository("test_repo_02").setAllVersionsSearchable(true);

		///FULL-TEXT WITH COMBINATIONS
		
		List<Integer> ids = executeQuery("select * from test:subfolder where cmis:createdBy = 'Jorge' and contains('foo man choo') and cmis:numberField is not null", "test_repo_02");
		assertTrue(ids.size() == 2 && ids.contains(114) && ids.contains(116));
		
		ids = executeQuery("select * from test:subfolder where not (not cmis:createdBy = 'Jorge' or not contains('foo man choo')) and cmis:numberField is not null", "test_repo_02");
		assertTrue(ids.size() == 2 && ids.contains(114) && ids.contains(116));
		
		ids = executeQuery("select * from test:subfolder where not (cmis:createdBy = 'Jorge' and contains('foo man choo')) and cmis:numberField is not null", "test_repo_02");
		assertTrue(ids.size() == 6 && !ids.contains(114) && !ids.contains(116));
		
		//IN
		
		ids = executeQuery("select * from test:subfolder where cmis:createdBy in('Jorge')", "test_repo_02");
		
		assertTrue(ids.size() == 5 && ids.contains(112)
				                   && ids.contains(113)
				                   && ids.contains(114)
				                   && ids.contains(115)
				                   && ids.contains(116));
		
		assertTrue(CollectionUtils.isEqualCollection(ids, executeQuery("select * from test:subfolder where not cmis:createdBy not in('Jorge')", "test_repo_02")));
		
		ids = executeQuery("select * from test:subfolder where cmis:createdBy not in('Jorge')", "test_repo_02");
		
		assertTrue(ids.size() == 3 && ids.contains(102)
				                   && ids.contains(110)
				                   && ids.contains(111));
		
		assertTrue(CollectionUtils.isEqualCollection(ids, executeQuery("select * from test:subfolder where not cmis:createdBy in('Jorge')", "test_repo_02")));
		
	}

	@Test
	public void testNull() throws Exception {

		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TestConstants.TEST_REPO_2);
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryServiceNormalized6.xml");

		List<Integer> result = executeQuery("SELECT * FROM test:subfolder", TestConstants.TEST_REPO_2); //[111, 102, 110, 115, 114, 113]
		assertEquals(7, result.size());

		result = executeQuery("SELECT * FROM test:subfolder where test:prop111 is not null", TestConstants.TEST_REPO_2); //[113]
		assertEquals(5, result.size());

		result = executeQuery("SELECT * FROM test:subfolder where test:prop111 is null", TestConstants.TEST_REPO_2); //[111, 102, 110, 115, 114]
		assertEquals(2, result.size());
		assertTrue(result.contains(102));
		assertTrue(result.contains(110));

		repoSelector.getRepository("test_repo_02").setAcl(CapabilityAcl.DISCOVER);
		setUser(TestConstants.TEST2_USER, TestConstants.TEST2_PWD, TestConstants.TEST_REPO_2);

		result = executeQuery("SELECT * FROM test:subfolder", TestConstants.TEST_REPO_2); //[111, 115, 113]
		assertEquals(2, result.size());
	}

	@Test
	public void testQuantifiedComparison() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryServiceNormalized7.xml");
		setUser(TestConstants.TEST2_USER, TestConstants.TEST2_USER, TestConstants.TEST_REPO_2);
		repoSelector.getRepository("test_repo_02").setPwcSearchable(true);
		repoSelector.getRepository("test_repo_02").setAllVersionsSearchable(true);

		List<Integer> result = null; 

		result = executeQuery("SELECT * FROM cmis:folder where '21' = any cmis:allowedChildObjectTypeIds and (cmis:name = 'testFolder3')", TestConstants.TEST_REPO_2);
		assertEquals(0, result.size());
		
		result = executeQuery("SELECT * FROM cmis:folder where '21' = any cmis:allowedChildObjectTypeIds and (cmis:name like '%testFolder3%')", TestConstants.TEST_REPO_2);
		assertEquals(1, result.size());
		assertTrue(result.contains(113));
		
		result = executeQuery("SELECT * FROM cmis:folder where '21' = any cmis:allowedChildObjectTypeIds and (cmis:name like '%testFolder1%')", TestConstants.TEST_REPO_2);
		assertEquals(3, result.size());
		assertTrue(result.contains(110));
		
		result = executeQuery("SELECT * FROM cmis:folder where '21' = any cmis:allowedChildObjectTypeIds and (cmis:name like '%testFolder1')", TestConstants.TEST_REPO_2);
		assertEquals(1, result.size());
		assertTrue(result.contains(110));
		
		result = executeQuery("SELECT * FROM cmis:folder where '21' = any cmis:allowedChildObjectTypeIds and (cmis:name like '%testFolder1_2013')", TestConstants.TEST_REPO_2);
		assertEquals(1, result.size());
		assertTrue(result.contains(114));
		
		// in combination with other operators
		result = executeQuery("SELECT * FROM cmis:folder where '21' = any cmis:allowedChildObjectTypeIds and (cmis:name like '%testFolder3%' or cmis:name like '%testFolder1%')", TestConstants.TEST_REPO_2);
		assertEquals(4, result.size());
		assertTrue(result.contains(110));
	
	}
	

	@Test
	public void testsLikeInNotIn() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryServiceNormalized1.xml");

		executeQuery("select * from cmis:document", "test_repo_02");

		List<Integer> cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy like '%len' ", "test_repo_02"));

		assertEquals(2, cmisObjects.size());

		for(Integer objectId : cmisObjects) {
			CMISObject obj = utilService.find(CMISObject.class,objectId);
			Property property = propertySelector.getObjectPropertyByQueryName(obj.getCmisObjectId(), "cmis:createdBy");
			assertTrue(property.<String>getTypedValue().endsWith("len"));
		}

		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy not like '%len' ", "test_repo_02"));
		assertEquals(6, cmisObjects.size());

		for(Integer objectId : cmisObjects) {
			CMISObject obj = utilService.find(CMISObject.class,objectId);
			Property property = propertySelector.getObjectPropertyByQueryName(obj.getCmisObjectId(), "cmis:createdBy");
			assertTrue(!property.<String>getTypedValue().endsWith("len"));
		}

		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy in ('Allen', 'Jorge')", "test_repo_02"));
		assertEquals(6, cmisObjects.size());

		for(Integer objectId : cmisObjects) {
			CMISObject obj = utilService.find(CMISObject.class,objectId);
			Property property = propertySelector.getObjectPropertyByQueryName(obj.getCmisObjectId(), "cmis:createdBy");
			assertTrue(property.<String>getTypedValue().equalsIgnoreCase("Allen") || property.<String>getTypedValue().equalsIgnoreCase("Jorge")
					|| property.<String>getTypedValue().equalsIgnoreCase("Jorgé"));
		}

		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy not in ('Allen', 'Jorge')", "test_repo_02"));
		assertEquals(2, cmisObjects.size());
		assertTrue(cmisObjects.contains(102) && cmisObjects.contains(110));
		
	}

	@Test
	public void testEscaping() throws Exception {
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryServiceNormalized8.xml");
		
		//equals
		
		List<Integer> cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy ='al\\'len'", "test_repo_02"));
		assertEquals(1, cmisObjects.size());
		assertTrue(cmisObjects.contains(110));
		
		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy ='allen_100%'", "test_repo_02"));
		assertEquals(1, cmisObjects.size());
		assertTrue(cmisObjects.contains(117));
		
		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy ='allen\\_100\\%'", "test_repo_02"));
		assertEquals(0, cmisObjects.size());
		assertTrue(!cmisObjects.contains(117));
		
		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy ='al\\'len'", "test_repo_02"));
		assertEquals(1, cmisObjects.size());
		assertTrue(cmisObjects.contains(110));
		
		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy ='escape\"other'", "test_repo_02"));
		assertEquals(1, cmisObjects.size());
		assertTrue(cmisObjects.contains(121));
		
		//like
		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy like 'allen\\_100\\%'", "test_repo_02"));
		assertEquals(1, cmisObjects.size());
		assertTrue(cmisObjects.contains(117));
		
		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy like 'allen_'", "test_repo_02"));
		assertEquals(1, cmisObjects.size());
		assertTrue(cmisObjects.contains(119));
		
		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy like 'allen%'", "test_repo_02"));
		assertEquals(4, cmisObjects.size());
		assertTrue(cmisObjects.contains(111) && cmisObjects.contains(117) && cmisObjects.contains(118) && cmisObjects.contains(119));
		
		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy like 'al\\'%'", "test_repo_02"));
		assertEquals(1, cmisObjects.size());
		assertTrue(cmisObjects.contains(110));
		
		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy like 'user\\\\other'", "test_repo_02"));
		assertEquals(1, cmisObjects.size());
		assertTrue(cmisObjects.contains(120));
		
		cmisObjects = new ArrayList<Integer>(executeQuery("select * from test:subfolder where cmis:createdBy like 'escape\"other'", "test_repo_02"));
		assertEquals(1, cmisObjects.size());
		assertTrue(cmisObjects.contains(121));
	
	}

	@Test
	public void testOrderBy() throws Exception {

		setScenario(DatabaseOperation.CLEAN_INSERT, "scenarioQueryService.xml", "scenarioQueryServiceNormalized8.xml");

		List<Integer> resultSet = null;
		Integer [] results = null;

		//TEST DEFAULT ORDERING + NO FILTER
		resultSet = executeQuery("select * from test:subfolder order by cmis:createdBy", "test_repo_02");
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(13, results.length);
		assertEquals(110, results[0].intValue());
		assertEquals(120, results[12].intValue());
		
		resultSet = executeQuery("select * from test:subfolder order by cmis:createdBy desc", "test_repo_02");
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(13, results.length);
		assertEquals(120, results[0].intValue());
		assertEquals(110, results[12].intValue());

		//TEST DEFAULT ORDERING + FILTER
		resultSet = executeQuery("select * from test:subfolder where cmis:createdBy like '%len' order by cmis:createdBy", "test_repo_02");
		results = resultSet.toArray(new Integer[resultSet.size()]);
		assertEquals(2, results.length);
		assertEquals(110, results[0].intValue());
		assertEquals(111, results[1].intValue());

		// NO FILTER
		//(1) pure
		assertEquals(13, executeQuery("select * from test:subfolder order by cmis:createdBy asc", "test_repo_02").size());
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
		
	}

	
	
	
	private List<Integer> executeQuery(String statement, String repositoryId) throws RecognitionException {
		return extractIds(query.executeQuery(statement, repositoryId, true, -1, 0, NORMALIZED_QUERY_TRUE));
	}

	private List<Integer> executeQuery(String statement, String repositoryId, int maxItems, int skipCount) throws RecognitionException {
		return extractIds(query.executeQuery(statement, repositoryId, true, maxItems, skipCount, NORMALIZED_QUERY_TRUE));
	}
	
	private List<Integer> extractIds(QueryResult qr) {
		List<Integer> ids = new ArrayList<>();
		
		for(CMISObject cmisObject : qr.getResult()) {
			ids.add(cmisObject.getId());
		}
		return ids;
	}
	
}

















