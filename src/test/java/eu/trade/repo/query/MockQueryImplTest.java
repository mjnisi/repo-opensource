package eu.trade.repo.query;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.antlr.runtime.RecognitionException;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.lucene.analysis.Analyzer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.Repository;
import eu.trade.repo.selectors.CMISObjectSelector;
import eu.trade.repo.selectors.RepositorySelector;

public class MockQueryImplTest {	
	
	@InjectMocks
	private QueryImpl concreteQuery = new ConcreteQueryImpl();
				
	abstract class MockAbstractQueryImpl extends QueryImpl {
		@Override
		public Map<String, ? extends Object> executeScoreQuery() {
			
			return null;
		}

		@Override
		protected Analyzer createPropertiesAnalyzer() {
			
			return null;
		}

		@Override
		protected Analyzer createFullTextAnalyzer() {
			
			return null;
		}
	}	
	
	class ConcreteQueryImpl extends MockAbstractQueryImpl {	
		
		@Override
		protected Map<String, ? extends Object> executeQuery(String cmisQuery, Repository repository, boolean searchAllVersions, boolean count, int offset, int pageSize, boolean isNormalizedQuery) throws RecognitionException { 
			return mockResultMap;
		}		
	}
	
	@Mock 
	private Map<String, Object> mockResultMap; 
				
	@Mock
	private RepositorySelector repSelector;

	@Mock
	private CMISObjectSelector objSelector;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);		
	}
	
	@Test(expected=CmisNotSupportedException.class)
	public void testExecuteQuery_QueyCapabilityNone() {		
		String repositoryId = "mockRepoId";
		Repository mockRepository = getMockRepository();
		CapabilityQuery capabilityQuery = CapabilityQuery.NONE;
		
		when(mockRepository.getQuery()).thenReturn(capabilityQuery);
	// it actually doesn't matter the OUTCOME of the getAllVersionsSearchable method : true/false. 
		when(mockRepository.getAllVersionsSearchable()).thenReturn(true); 
		when(repSelector.getRepository("mockRepoId")).thenReturn(mockRepository);
		
		concreteQuery.executeQuery("mockStatement", repositoryId, true, 1, 1, true);			
	}
	
	@Test(expected=CmisNotSupportedException.class)
	public void testExecuteQuery_SearchAllVersionsCapabilityNone() {		
		String repositoryId = "mockRepoId";
		Repository mockRepository = getMockRepository();
	// could be anything, but NONE
		CapabilityQuery capabilityQuery_notNONE = CapabilityQuery.BOTHCOMBINED; 
		when(mockRepository.getQuery()).thenReturn(capabilityQuery_notNONE);
		when(mockRepository.getAllVersionsSearchable()).thenReturn(false); 
		when(repSelector.getRepository("mockRepoId")).thenReturn(mockRepository);
		
		concreteQuery.executeQuery("mockStatement", repositoryId, true, 1, 1, true);				
	}
	
	@Test
	public void testExecuteQuery_QueryReturnEmptyResultset() {		
		String repositoryId = "mockRepoId";
		Repository mockRepository = getMockRepository();
	// could be anything, but NONE
		CapabilityQuery capabilityQuery_notNONE = CapabilityQuery.BOTHCOMBINED;		
		when(mockRepository.getQuery()).thenReturn(capabilityQuery_notNONE);
		when(mockRepository.getAllVersionsSearchable()).thenReturn(true); 
		when(repSelector.getRepository("mockRepoId")).thenReturn(mockRepository);		
		when(mockResultMap.get("query")).thenReturn("mockQueryString");
		when(mockResultMap.get("results")).thenReturn(new ArrayList<Object>());
		
		QueryResult actualResult = concreteQuery.executeQuery("mockStatement", repositoryId, true, 1, 1, true);
		
		assertNotNull("Query result shouldn't be NULL", actualResult);
		assertTrue("The query string should be exposed unmodified to the client", actualResult.getQuery().equals("mockQueryString"));
		assertTrue("Result should be empty", actualResult.getResult().isEmpty());
		verify(repSelector, times(1)).getRepository("mockRepoId");
		verify(mockRepository, times(1)).getQuery();		
		verify(mockRepository, times(1)).getAllVersionsSearchable();		
		verify(mockResultMap, times(1)).get("query");
		verify(mockResultMap, times(1)).get("results");
		verifyNoMoreInteractions(repSelector, mockRepository, mockResultMap);
	}
		
	@Test
	public void testExecuteQuery_QueryReturnNonEmptyResultset_noScore() {
		String statement = "mockStatement";
		String repositoryId = "mockRepoId";
		Repository mockRepository = getMockRepository();
		CapabilityQuery capabilityQuery_notNONE = CapabilityQuery.BOTHCOMBINED;		
		when(mockRepository.getQuery()).thenReturn(capabilityQuery_notNONE);
		when(mockRepository.getAllVersionsSearchable()).thenReturn(true); 
		when(repSelector.getRepository("mockRepoId")).thenReturn(mockRepository);		
		when(mockResultMap.get("query")).thenReturn("mockQueryString");
		Integer[] idResults = new Integer[]{3,1,2};
		when(mockResultMap.get("results")).thenReturn(Arrays.asList(idResults));
		Set unorderedObjects = getUnorderedObjectsFromService(idResults);
		when(objSelector.getObjectsWhereIdInList(Arrays.asList(idResults))).thenReturn(unorderedObjects);
		
		QueryResult actualResult = concreteQuery.executeQuery(statement, repositoryId, true, 1, 1, true);
		
		assertNotNull("Query result shouldn't be NULL", actualResult);
		assertTrue("The query string should be exposed unmodified to the client", actualResult.getQuery().equals("mockQueryString"));
		assertFalse("Result shouldn't be empty", actualResult.getResult().isEmpty());
		assertTrue("Result size actualResult should be equal with result size of expectedResult", actualResult.getResult().size() == idResults.length);
		assertTrue("Result set should be of type TreeSet", actualResult.getResult() instanceof TreeSet);
		int i=0;
		for (CMISObject actualObject : actualResult.getResult()) {
			assertTrue("The value&position should mathch", actualObject.getId().equals(idResults[i]));i++;
		}				
		verify(repSelector, times(1)).getRepository("mockRepoId");
		verify(mockRepository, times(1)).getQuery();		
		verify(mockRepository, times(1)).getAllVersionsSearchable();		
		verify(mockResultMap, times(1)).get("query");
		verify(mockResultMap, times(1)).get("results");
		verifyNoMoreInteractions(repSelector, mockRepository, mockResultMap);
	}	
		
	private Set<CMISObject> getUnorderedObjectsFromService(Integer[] idResults) {
		Set unorderedObjects = new HashSet();
		for(Integer idResult : idResults) {
			CMISObject object = new CMISObject();
			object.setId(idResult);
			unorderedObjects.add(object);
		}
		return unorderedObjects;
	}
	
	private Repository getMockRepository() {
		return mock(Repository.class);
	}	
	
}
