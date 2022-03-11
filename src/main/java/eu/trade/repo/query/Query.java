package eu.trade.repo.query;

import java.util.Map;

import org.antlr.runtime.RecognitionException;

public interface Query {

	QueryResult executeQuery(String statement, String repositoryId, boolean searchAllVersions, int maxItems, int skipCount, boolean isNormalizedQuery);
	int queryCount(String statement, String repositoryId, boolean searchAllVersions, boolean isNormalizedQuery) throws RecognitionException;
	Map<String, String> getQueryColumns(String cmisQuery) throws RecognitionException;
	
	//--- TEMP FOR TESTING --
    Map<String, ? extends Object> executeScoreQuery();
}
